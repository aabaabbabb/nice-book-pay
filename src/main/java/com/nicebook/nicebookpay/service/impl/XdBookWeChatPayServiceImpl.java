package com.nicebook.nicebookpay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nicebook.nicebookpay.entity.XdBookOrder;
import com.nicebook.nicebookpay.entity.XdBookWeChatPay;
import com.nicebook.nicebookpay.mapper.XdBookWeChatPayMapper;
import com.nicebook.nicebookpay.service.XdBookOrderService;
import com.nicebook.nicebookpay.service.XdBookWeChatPayService;
import com.nicebook.nicebookpay.utils.HotelNameCleaner;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.cert.CertificatesManager;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @description Service for xd_book_we_chat_pay
 * @createDate 2026-03-11 20:06:08
 */
@Slf4j
@Service
public class XdBookWeChatPayServiceImpl extends ServiceImpl<XdBookWeChatPayMapper, XdBookWeChatPay>
        implements XdBookWeChatPayService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static final String WEIXIN_URL_H5 = "https://api.mch.weixin.qq.com/v3/pay/transactions/h5";
    private static final int DEFAULT_EXPIRE_MINUTES = 10;

    @Autowired
    private XdBookWeChatPayMapper bookWeChatPayMapper;

    @Autowired
    private XdBookOrderService orderService;

    @Override
    public XdBookWeChatPay selectByISDefaultAndParentId(Integer isDefault, String partnerId) {
        return bookWeChatPayMapper.selectByISDefaultAndParentId(isDefault, partnerId);
    }

    @Override
    public Map<String, String> createOrder(XdBookOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("Order is null");
        }
        XdBookWeChatPay weChatPay = resolveWeChatPay(order);
        validateWeChatPayConfig(weChatPay);

        String outTradeNo = resolveOutTradeNo(order);
        int totalFen = toFen(order.getPayprice());
        String description = buildDescription(order);

        ObjectNode rootNode = OBJECT_MAPPER.createObjectNode();
        rootNode.put("appid", weChatPay.getAppId())
                .put("mchid", weChatPay.getMchId())
                .put("description", description)
                .put("out_trade_no", outTradeNo)
                .put("time_expire", buildExpireTime())
                .put("notify_url", buildNotifyUrl(weChatPay.getNotifyUrl()));

        ObjectNode amountNode = rootNode.putObject("amount");
        amountNode.put("total", totalFen);
        amountNode.put("currency", "CNY");

        ObjectNode sceneInfo = rootNode.putObject("scene_info");
        sceneInfo.put("payer_client_ip", requireNonBlank(order.getIp(), "order.ip"));
        ObjectNode h5Info = sceneInfo.putObject("h5_info");
        h5Info.put("type", "Wap");
        h5Info.put("app_name", description);
        h5Info.put("app_url", weChatPay.getNotifyUrl());

        String payload;
        try {
            payload = OBJECT_MAPPER.writeValueAsString(rootNode);
        } catch (Exception e) {
            throw new RuntimeException("微信创建订单有效载荷序列化失败", e);
        }

        String requestUrl = resolveRequestUrl(weChatPay);
        HttpPost httpPost = new HttpPost(requestUrl);
        httpPost.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        httpPost.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));

        try (CloseableHttpClient httpClient = buildHttpClient(weChatPay);
             CloseableHttpResponse response = httpClient.execute(httpPost)) {
            String bodyAsString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            log.info("微信回复数据-----"+bodyAsString);
            int status = response.getStatusLine().getStatusCode();
            if (status < 200 || status >= 300) {
                throw new RuntimeException("WeChat create order failed: HTTP " + status + ", body=" + bodyAsString);
            }

            JsonNode json = OBJECT_MAPPER.readTree(bodyAsString);
            String h5Url = text(json, "h5_url");
            if (isBlank(h5Url)) {
                throw new RuntimeException("WeChat create order response missing h5_url");
            }

            updateOrderMchId(order, weChatPay.getMchId());

            Map<String, String> result = new HashMap<>();
            result.put("h5_url", h5Url);
            result.put("url", weChatPay.getNotifyUrl());
            return result;
        } catch (IOException e) {
            throw new RuntimeException("WeChat create order failed", e);
        }
    }

    @Override
    public String decryptOrder(String body) {
        if (isBlank(body)) {
            throw new IllegalArgumentException("Body是空");
        }
        XdBookWeChatPay wxPay = resolveWeChatPay(null);
        validateWeChatPayConfig(wxPay);
        try {
            AesUtil util = new AesUtil(wxPay.getApiV3key().getBytes(StandardCharsets.UTF_8));
            JsonNode node = OBJECT_MAPPER.readTree(body);
            JsonNode resource = node.get("resource");
            if (resource == null || resource.isNull()) {
                throw new RuntimeException("微信通知资源缺失");
            }
            String ciphertext = text(resource, "ciphertext");
            String associatedData = text(resource, "associated_data");
            String nonce = text(resource, "nonce");
            if (isBlank(ciphertext) || isBlank(nonce)) {
                throw new RuntimeException("微信通知丢失密文或随机数");
            }
            return util.decryptToString(
                    associatedData == null ? new byte[0] : associatedData.getBytes(StandardCharsets.UTF_8),
                    nonce.getBytes(StandardCharsets.UTF_8),
                    ciphertext);
        } catch (Exception e) {
            throw new RuntimeException("微信通知解密失败", e);
        }
    }

    private CloseableHttpClient buildHttpClient(XdBookWeChatPay weChatPay) {
        try {
            PrivateKey merchantPrivateKey = PemUtil.loadPrivateKey(weChatPay.getPrivateKey());
            CertificatesManager certificatesManager = CertificatesManager.getInstance();
            certificatesManager.putMerchant(
                    weChatPay.getMchId(),
                    new WechatPay2Credentials(weChatPay.getMchId(),
                            new PrivateKeySigner(weChatPay.getMchSerialNo(), merchantPrivateKey)),
                    weChatPay.getApiV3key().getBytes(StandardCharsets.UTF_8));
            Verifier verifier = certificatesManager.getVerifier(weChatPay.getMchId());
            return WechatPayHttpClientBuilder.create()
                    .withMerchant(weChatPay.getMchId(), weChatPay.getMchSerialNo(), merchantPrivateKey)
                    .withValidator(new WechatPay2Validator(verifier))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("微信客户端初始化失败", e);
        }
    }

    private XdBookWeChatPay resolveWeChatPay(XdBookOrder order) {
        XdBookWeChatPay weChatPay = null;
        if (order != null && order.getParentid() != null) {
            weChatPay = selectByISDefaultAndParentId(1, "2");
        }
        if (weChatPay == null) {
            weChatPay = bookWeChatPayMapper.selectDefaultOne(1);
        }
        if (weChatPay == null) {
            throw new RuntimeException("未找到微信支付配置");
        }
        return weChatPay;
    }

    private void validateWeChatPayConfig(XdBookWeChatPay weChatPay) {
        requireNonBlank(weChatPay.getAppId(), "appId");
        requireNonBlank(weChatPay.getMchId(), "mchId");
        requireNonBlank(weChatPay.getMchSerialNo(), "mchSerialNo");
        requireNonBlank(weChatPay.getApiV3key(), "apiV3key");
        requireNonBlank(weChatPay.getPrivateKey(), "privateKey");
        requireNonBlank(weChatPay.getNotifyUrl(), "notifyUrl");
    }

    private String resolveOutTradeNo(XdBookOrder order) {
        if (!isBlank(order.getOrderid())) {
            return order.getOrderid();
        }
        if (order.getId() != null) {
            return String.valueOf(order.getId());
        }
        throw new IllegalArgumentException("订单 ID 为空");
    }

    private String buildDescription(XdBookOrder order) {
        String hotelName = order.getHotelName();
        hotelName  = HotelNameCleaner.clean(hotelName);
        if (isBlank(hotelName)) {
            return "订单支付";
        }
        return hotelName + "-订单支付";
    }

    private String buildExpireTime() {
        return OffsetDateTime.now(ZoneOffset.UTC)
                .plusMinutes(DEFAULT_EXPIRE_MINUTES)
                .truncatedTo(ChronoUnit.SECONDS)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private String resolveRequestUrl(XdBookWeChatPay weChatPay) {
        String payUrl = weChatPay.getPayUrl();
        return isBlank(payUrl) ? WEIXIN_URL_H5 : payUrl;
    }

    private String buildNotifyUrl(String baseUrl) {
        String trimmed = requireNonBlank(baseUrl, "notifyUrl").trim();
        return trimmed.endsWith("/") ? trimmed + "api/wechatpay/notify" : trimmed + "/api/wechatpay/notify";
    }

    private int toFen(Double amount) {
        if (amount == null) {
            throw new IllegalArgumentException("订单支付价格为空");
        }
        BigDecimal fen = BigDecimal.valueOf(amount)
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP);
        if (fen.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("订单支付价格无效");
        }
        return fen.intValueExact();
    }

    private void updateOrderMchId(XdBookOrder order, String mchId) {
        if (order.getId() == null) {
            return;
        }
        order.setMchid(mchId);
        orderService.updateById(order);
    }

    private String text(JsonNode node, String field) {
        if (node == null) {
            return null;
        }
        JsonNode child = node.get(field);
        return child == null || child.isNull() ? null : child.textValue();
    }

    private String requireNonBlank(String value, String fieldName) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(fieldName + " is blank");
        }
        return value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

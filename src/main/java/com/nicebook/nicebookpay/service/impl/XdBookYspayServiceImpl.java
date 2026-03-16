package com.nicebook.nicebookpay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eptok.yspay.opensdkjava.util.DateUtil;
import com.eptok.yspay.opensdkjava.util.HttpClientUtil;
import com.eptok.yspay.opensdkjava.util.StringUtil;
import com.eptok.yspay.opensdkjava.util.YsOnlineSignUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nicebook.nicebookpay.entity.XdBookOrder;
import com.nicebook.nicebookpay.entity.XdBookYspay;
import com.nicebook.nicebookpay.mapper.XdBookYspayMapper;
import com.nicebook.nicebookpay.service.XdBookYspayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
* @author Administrator
* @description 閽堝琛ㄣ€恱d_book_yspay銆戠殑鏁版嵁搴撴搷浣淪ervice瀹炵幇
* @createDate 2026-03-11 14:55:16
*/
@Service
public class XdBookYspayServiceImpl extends ServiceImpl<XdBookYspayMapper, XdBookYspay>
    implements XdBookYspayService{

    public static final String URL = "https://openapi.ysepay.com/gateway.do";
    private static final String CASHIER_URL = "https://wapcashier.ysepay.com/cashier";

    @Autowired
    private XdBookYspayMapper bookYspayMapper;

    @Override
    public XdBookYspay selectById(Integer id) {
        return bookYspayMapper.selectById(id);
    }

    @Override
    public XdBookYspay selectByIsDefaultAndParentId(Integer isDefault, String partnerid) {
        return bookYspayMapper.selectByIsDefaultAndParentId(isDefault, partnerid);
    }

    @Override
    public String createOrder(XdBookOrder xdBookOrder) {
        if (xdBookOrder == null) {
            throw new IllegalArgumentException("Order is null");
        }
        XdBookYspay xdBookYspay = resolveYsPayConfig(xdBookOrder);
        /** 2、组装入参的数据 */
        Map<String, String> params = new HashMap<>();
        //接口名称
        params.put("method", "ysepay.online.wap.directpay.createbyuser");
        params.put("partner_id", xdBookYspay.getPartnerid());
        params.put("timestamp", DateUtil.getDateNow());
        params.put("charset", xdBookYspay.getCharset());
        params.put("sign_type", xdBookYspay.getSigntype());
        params.put("notify_url", xdBookYspay.getNotifyurl()+"/yspay/showNotify");
        params.put("return_url", xdBookYspay.getNotifyurl()+"/yspay/showReturn");
        params.put("version", xdBookYspay.getVersion());
        params.put("tran_type", xdBookYspay.getTrantype().toString());
        params.put("out_trade_no", xdBookOrder.getOrderid());
        params.put("shopdate", DateUtil.getDateFormat(new Date(), "yyyyMMdd"));
        params.put("subject", xdBookOrder.getHotelName());
        params.put("total_amount", xdBookOrder.getPayprice().toString());
        params.put("seller_id", xdBookYspay.getSellerid());
        params.put("seller_name", xdBookYspay.getSellername());
        params.put("timeout_express", xdBookYspay.getTimeoutexpress());
        params.put("business_code", xdBookYspay.getBusinesscode());
        params.put("pay_mode", "native");
        params.put("bank_type", "1903000");
        try {
            String rel = xdBookYspay.getPrivatekeyfilepath();
            if (rel == null || rel.isBlank()) {
                throw new IllegalArgumentException("YSPay private key path is blank");
            }
            if (rel.startsWith("/")) {
                rel = rel.substring(1);
            }
            String keyPath = new ClassPathResource(rel).getFile().getAbsolutePath();
            String sign = YsOnlineSignUtils.sign(params, xdBookYspay.getPrivatekeypassword(), keyPath);
            params.put("sign", sign);
            String result = HttpClientUtil.sendPostParam(URL, StringUtil.mapToString(params));
            return toBrowserHtml(result,params);
        } catch (Exception e) {
            throw new RuntimeException("YSPay create order failed", e);
        }

    }

    private XdBookYspay resolveYsPayConfig(XdBookOrder order) {
        XdBookYspay ysPay = null;
        if (order != null && order.getParentid() != null) {
            ysPay = selectByIsDefaultAndParentId(1, String.valueOf(order.getParentid()));
        }
        if (ysPay == null) {
            ysPay = bookYspayMapper.selectDefaultOne(1);
        }
        if (ysPay == null) {
            throw new RuntimeException("未找到银盛支付配置");
        }
        return ysPay;
    }

    private String toBrowserHtml(String result,Map<String, String>  params) {
        if (result == null || result.isBlank()) {
            throw new RuntimeException("YSPay empty response");
        }
        String trimmed = result.trim();
        if (trimmed.startsWith("<")) {
            return trimmed;
        }

        Map<String, Object> payload = new Gson().fromJson(
            trimmed, new TypeToken<Map<String, Object>>() {}.getType());

        String partnerCode = valueOf(payload.get("partnerCode"));
        String packageStr = valueOf(payload.get("packageStr"));
        String platform = valueOf(payload.get("platform"));
        String signature = valueOf(payload.get("signature"));

        if (partnerCode.isEmpty() || packageStr.isEmpty() || signature.isEmpty()) {
            return trimmed;
        }
        StringBuffer sbHtml=new StringBuffer("<html>");
        sbHtml.append("<head>");
        sbHtml.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
        sbHtml.append("<meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'/>");
        sbHtml.append("<meta content='always' name='referrer'/>");
        sbHtml.append("<meta name='theme-color' content='#ffffff'/>");
        sbHtml.append("</head>");
        sbHtml.append("<body>");
        sbHtml.append("<form style='text-align:center;display:none;' id='topay' method='post' action='").append(URL).append("'>");
        for (Map.Entry entry : params.entrySet()) {
            sbHtml.append("<input type='text' name='").append(entry.getKey()).append("' value='").append(entry.getValue()).append("'/>");
        }
        sbHtml.append("<input type='submit'/>");
        sbHtml.append("</form>");
        sbHtml.append("<script>");
        sbHtml.append("var u = navigator.userAgent;" +
                "var isAndroid = u.indexOf('Android') > -1 || u.indexOf('Adr') > -1;" +
                "var isiOS = !!u.match(/\\(i[^;]+;( U;)? CPU.+Mac OS X/);   if(isiOS){" +
                "document.forms[0].submit();" +
                "}else{" +
                //"document.forms[0].setAttribute('target', '_blank');" +
                "document.forms[0].submit();" +
                "} ");
        sbHtml.append("</script>");
        sbHtml.append("</body>");
        sbHtml.append("</html>");
        System.out.println(sbHtml);
        return sbHtml.toString();
    }

    private String valueOf(Object obj) {
        return obj == null ? "" : Objects.toString(obj, "");
    }

}

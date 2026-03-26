package com.nicebook.nicebookpay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
@TableName(value = "xd_book_payment_methods")
public class XdBookPaymentMethods implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 支付渠道（1-微信，2-支付宝）
     */
    @TableField(value = "payment_channels")
    private Integer paymentChannels;

    /**
     * 商户号
     */
    @TableField(value = "merchant_no")
    private String merchantNo;

    /**
     * 开发者id
     */
    @TableField(value = "developer_id")
    private String developerId;

    /**
     * 签名类型（1-类型一、2-类型二）
     */
    @TableField(value = "sign_type")
    private String signType;

    /**
     * 支付回调地址
     */
    @TableField(value = "payment_callback_address")
    private String paymentCallbackAddress;

    /**
     * 商户证书序列号
     */
    @TableField(value = "mch_serial_no")
    private String mchSerialNo;

    /**
     * 公钥
     */
    @TableField(value = "public_key")
    private String publicKey;

    /**
     * 微信包地址
     */
    @TableField(value = "package_wx")
    private String packageWx;

    /**
     * 私钥
     */
    @TableField(value = "private_key")
    private String privateKey;

    /**
     * 状态（1001-关闭、1002开启）
     */
    @TableField(value = "`status`")
    private String status;

    /**
     * 录入时间
     */
    @TableField(value = "create_time")
    private Integer createTime;

    /**
     * 修改时间
     */
    @TableField(value = "alter_time")
    private Integer alterTime;

    /**
     * 备注
     */
    @TableField(value = "memo")
    private String memo;

    @TableField(value = "partner_id")
    private Integer partnerId;

    /**
     * 编码格式
     */
    @TableField(value = "char_set")
    private String charSet;

    /**
     * 类型
     */
    @TableField(value = "tran_type")
    private String tranType;

    /**
     * 版本号（银盛支付宝用）
     */
    @TableField(value = "ys_version")
    private String ysVersion;

    @TableField(value = "seller_id")
    private String sellerId;

    @TableField(value = "seller_name")
    private String sellerName;

    @TableField(value = "business_code")
    private String businessCode;

    @TableField(value = "parent_id")
    private Integer parentId;

    /**
     * 超时时间
     */
    @TableField(value = "timeout_express")
    private String timeoutExpress;

    /**
     * 私钥密码
     */
    @TableField(value = "private_key_password")
    private String privateKeyPassword;

    /**
     * 私钥储存路径
     */
    @TableField(value = "private_key_file_path")
    private String privateKeyFilePath;
}
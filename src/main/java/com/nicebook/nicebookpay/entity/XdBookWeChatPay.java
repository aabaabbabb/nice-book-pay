package com.nicebook.nicebookpay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName xd_book_we_chat_pay
 */
@TableName(value ="xd_book_we_chat_pay")
@Data
public class XdBookWeChatPay implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private String appId;

    /**
     * 
     */
    private String mchId;

    /**
     * 
     */
    private String notifyUrl;

    /**
     * 
     */
    private String mchSerialNo;

    /**
     * 
     */
    private String apiV3key;

    /**
     * 
     */
    private String packages;

    /**
     * 
     */
    private String privateKey;

    /**
     * 
     */
    private String payUrl;

    /**
     * 
     */
    private Integer parentid;

    /**
     * 
     */
    private Integer isDefault;

    /**
     * 
     */
    private Integer isDelete;

    /**
     * 
     */
    private Date created;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        XdBookWeChatPay other = (XdBookWeChatPay) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getAppId() == null ? other.getAppId() == null : this.getAppId().equals(other.getAppId()))
            && (this.getMchId() == null ? other.getMchId() == null : this.getMchId().equals(other.getMchId()))
            && (this.getNotifyUrl() == null ? other.getNotifyUrl() == null : this.getNotifyUrl().equals(other.getNotifyUrl()))
            && (this.getMchSerialNo() == null ? other.getMchSerialNo() == null : this.getMchSerialNo().equals(other.getMchSerialNo()))
            && (this.getApiV3key() == null ? other.getApiV3key() == null : this.getApiV3key().equals(other.getApiV3key()))
            && (this.getPackages() == null ? other.getPackages() == null : this.getPackages().equals(other.getPackages()))
            && (this.getPrivateKey() == null ? other.getPrivateKey() == null : this.getPrivateKey().equals(other.getPrivateKey()))
            && (this.getPayUrl() == null ? other.getPayUrl() == null : this.getPayUrl().equals(other.getPayUrl()))
            && (this.getParentid() == null ? other.getParentid() == null : this.getParentid().equals(other.getParentid()))
            && (this.getIsDefault() == null ? other.getIsDefault() == null : this.getIsDefault().equals(other.getIsDefault()))
            && (this.getIsDelete() == null ? other.getIsDelete() == null : this.getIsDelete().equals(other.getIsDelete()))
            && (this.getCreated() == null ? other.getCreated() == null : this.getCreated().equals(other.getCreated()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getAppId() == null) ? 0 : getAppId().hashCode());
        result = prime * result + ((getMchId() == null) ? 0 : getMchId().hashCode());
        result = prime * result + ((getNotifyUrl() == null) ? 0 : getNotifyUrl().hashCode());
        result = prime * result + ((getMchSerialNo() == null) ? 0 : getMchSerialNo().hashCode());
        result = prime * result + ((getApiV3key() == null) ? 0 : getApiV3key().hashCode());
        result = prime * result + ((getPackages() == null) ? 0 : getPackages().hashCode());
        result = prime * result + ((getPrivateKey() == null) ? 0 : getPrivateKey().hashCode());
        result = prime * result + ((getPayUrl() == null) ? 0 : getPayUrl().hashCode());
        result = prime * result + ((getParentid() == null) ? 0 : getParentid().hashCode());
        result = prime * result + ((getIsDefault() == null) ? 0 : getIsDefault().hashCode());
        result = prime * result + ((getIsDelete() == null) ? 0 : getIsDelete().hashCode());
        result = prime * result + ((getCreated() == null) ? 0 : getCreated().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", appId=").append(appId);
        sb.append(", mchId=").append(mchId);
        sb.append(", notifyUrl=").append(notifyUrl);
        sb.append(", mchSerialNo=").append(mchSerialNo);
        sb.append(", apiV3key=").append(apiV3key);
        sb.append(", package=").append(packages);
        sb.append(", privateKey=").append(privateKey);
        sb.append(", payUrl=").append(payUrl);
        sb.append(", parentid=").append(parentid);
        sb.append(", isDefault=").append(isDefault);
        sb.append(", isDelete=").append(isDelete);
        sb.append(", created=").append(created);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}
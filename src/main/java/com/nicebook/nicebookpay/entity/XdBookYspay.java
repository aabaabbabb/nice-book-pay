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
 * @TableName xd_book_yspay
 */
@TableName(value ="xd_book_yspay")
@Data
public class XdBookYspay implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     *
     */
    private String partnerid;

    /**
     *
     */
    private String charset;

    /**
     *
     */
    private String signtype;

    /**
     *
     */
    private String notifyurl;

    /**
     *
     */
    private Integer trantype;

    /**
     *
     */
    private String version;

    /**
     *
     */
    private String sellerid;

    /**
     *
     */
    private String sellername;

    /**
     *
     */
    private String businesscode;

    /**
     *
     */
    private Integer parentid;

    /**
     *
     */
    private String timeoutexpress;

    /**
     *
     */
    private String privatekeypassword;

    /**
     *
     */
    private String privatekeyfilepath;

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
        com.nicebook.nicebookpay.entity.XdBookYspay other = (com.nicebook.nicebookpay.entity.XdBookYspay) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getPartnerid() == null ? other.getPartnerid() == null : this.getPartnerid().equals(other.getPartnerid()))
                && (this.getCharset() == null ? other.getCharset() == null : this.getCharset().equals(other.getCharset()))
                && (this.getSigntype() == null ? other.getSigntype() == null : this.getSigntype().equals(other.getSigntype()))
                && (this.getNotifyurl() == null ? other.getNotifyurl() == null : this.getNotifyurl().equals(other.getNotifyurl()))
                && (this.getTrantype() == null ? other.getTrantype() == null : this.getTrantype().equals(other.getTrantype()))
                && (this.getVersion() == null ? other.getVersion() == null : this.getVersion().equals(other.getVersion()))
                && (this.getSellerid() == null ? other.getSellerid() == null : this.getSellerid().equals(other.getSellerid()))
                && (this.getSellername() == null ? other.getSellername() == null : this.getSellername().equals(other.getSellername()))
                && (this.getBusinesscode() == null ? other.getBusinesscode() == null : this.getBusinesscode().equals(other.getBusinesscode()))
                && (this.getParentid() == null ? other.getParentid() == null : this.getParentid().equals(other.getParentid()))
                && (this.getTimeoutexpress() == null ? other.getTimeoutexpress() == null : this.getTimeoutexpress().equals(other.getTimeoutexpress()))
                && (this.getPrivatekeypassword() == null ? other.getPrivatekeypassword() == null : this.getPrivatekeypassword().equals(other.getPrivatekeypassword()))
                && (this.getPrivatekeyfilepath() == null ? other.getPrivatekeyfilepath() == null : this.getPrivatekeyfilepath().equals(other.getPrivatekeyfilepath()))
                && (this.getIsDefault() == null ? other.getIsDefault() == null : this.getIsDefault().equals(other.getIsDefault()))
                && (this.getIsDelete() == null ? other.getIsDelete() == null : this.getIsDelete().equals(other.getIsDelete()))
                && (this.getCreated() == null ? other.getCreated() == null : this.getCreated().equals(other.getCreated()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getPartnerid() == null) ? 0 : getPartnerid().hashCode());
        result = prime * result + ((getCharset() == null) ? 0 : getCharset().hashCode());
        result = prime * result + ((getSigntype() == null) ? 0 : getSigntype().hashCode());
        result = prime * result + ((getNotifyurl() == null) ? 0 : getNotifyurl().hashCode());
        result = prime * result + ((getTrantype() == null) ? 0 : getTrantype().hashCode());
        result = prime * result + ((getVersion() == null) ? 0 : getVersion().hashCode());
        result = prime * result + ((getSellerid() == null) ? 0 : getSellerid().hashCode());
        result = prime * result + ((getSellername() == null) ? 0 : getSellername().hashCode());
        result = prime * result + ((getBusinesscode() == null) ? 0 : getBusinesscode().hashCode());
        result = prime * result + ((getParentid() == null) ? 0 : getParentid().hashCode());
        result = prime * result + ((getTimeoutexpress() == null) ? 0 : getTimeoutexpress().hashCode());
        result = prime * result + ((getPrivatekeypassword() == null) ? 0 : getPrivatekeypassword().hashCode());
        result = prime * result + ((getPrivatekeyfilepath() == null) ? 0 : getPrivatekeyfilepath().hashCode());
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
        sb.append(", partnerid=").append(partnerid);
        sb.append(", charset=").append(charset);
        sb.append(", signtype=").append(signtype);
        sb.append(", notifyurl=").append(notifyurl);
        sb.append(", trantype=").append(trantype);
        sb.append(", version=").append(version);
        sb.append(", sellerid=").append(sellerid);
        sb.append(", sellername=").append(sellername);
        sb.append(", businesscode=").append(businesscode);
        sb.append(", parentid=").append(parentid);
        sb.append(", timeoutexpress=").append(timeoutexpress);
        sb.append(", privatekeypassword=").append(privatekeypassword);
        sb.append(", privatekeyfilepath=").append(privatekeyfilepath);
        sb.append(", isDefault=").append(isDefault);
        sb.append(", isDelete=").append(isDelete);
        sb.append(", created=").append(created);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}

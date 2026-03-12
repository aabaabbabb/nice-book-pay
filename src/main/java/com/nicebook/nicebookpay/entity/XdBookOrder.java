package com.nicebook.nicebookpay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("xd_book_order")
public class XdBookOrder implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String orderid;
    private String orderno;
    private String confirmationnumber;
    private Integer zhunaid;
    private String eid;
    private Integer rid;
    private Integer pid;
    private String hotelName;
    private Integer level;
    private String address;
    private String cityId;
    private String roomName;
    private String roomnameErr;
    private Integer roomNum;
    private Integer roomnumReal;
    private String checkIn;
    private String checkOut;
    private String realTm1;
    private String realTm2;
    private String timeEarly;
    private String timeLate;

    private Double price;
    private String priceErr;
    private Double totalPrice;
    private Double payprice;
    private Double totalMoney;
    private Double paySum;
    private Double floorprice;

    private Integer isallpayprice;
    private Double refundprice;
    private Double refundedprice;
    private Integer payState;

    private String breakfast;
    private Integer bed;
    private String guest;
    private String guestErr;
    private String booker;
    private String mobile;

    private Integer bankcardid;
    private Integer isDelete;
    private Integer isSend;
    private Integer isFeedback;
    private Integer isInvoice;

    private String state;
    private Integer freeRoomState;
    private String cancelresult;

    private Integer paymentId;
    private String paymentErr;
    private String fromV;
    private String bookurl;
    private Integer supplierId;

    private String reservedmobile;
    private String notes;
    private String reqDate;
    private String rule;
    private String reconColor;
    private String guid;

    private Integer zhunaorderid;
    private Integer accountid;
    private String realname;
    private Integer parentid;

    private Date created;
    private String ip;
    private String transactionid;
    private String outrefundno;

    private Integer eotid;
    private Integer arrivetime;

    private Double rate;
    private Double commission;

    private String mchid;

    private Integer cardno;
    private String mergeid;
    private Integer paymentMethod;
    private Integer paymentSend;

    private String bodycard;
    private Integer tixing;

    private String gaodeordernum;
    private String gaodestate;

    private Integer bookingaccountid;
    private String bookingrealname;

    private Integer payer;
    private Integer sellerid;

    private Integer ret;
    private String subMsg;

    private String sellerId;

    private Integer orderStatus;

    private Integer aid;
    private Integer customerId;

    private Integer createTime;
    private Integer alterTime;

    private BigDecimal totalCost;

    private Integer sendSms;
    private Integer isGrabbing;

    private String contact;
    private String contactPhone;

    private Integer source;
}
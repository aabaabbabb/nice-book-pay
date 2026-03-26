package com.nicebook.nicebookpay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nicebook.nicebookpay.entity.XdBookPaymentMethods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface XdBookPaymentMethodsMapper extends BaseMapper<XdBookPaymentMethods> {

    XdBookPaymentMethods selectOneByPaymentChannelsAndStatus(@Param("paymentChannels") String paymentChannels, @Param("status") String status);
}
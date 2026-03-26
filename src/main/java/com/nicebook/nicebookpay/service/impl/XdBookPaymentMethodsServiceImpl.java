package com.nicebook.nicebookpay.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nicebook.nicebookpay.mapper.XdBookPaymentMethodsMapper;
import com.nicebook.nicebookpay.entity.XdBookPaymentMethods;
import com.nicebook.nicebookpay.service.XdBookPaymentMethodsService;
@Service
public class XdBookPaymentMethodsServiceImpl extends ServiceImpl<XdBookPaymentMethodsMapper, XdBookPaymentMethods> implements XdBookPaymentMethodsService{

}

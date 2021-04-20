package com.lanyu.multdao.mybatis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: suibin
 * @Date: 2021/04/11/5:47
 * @Description:
 */

//@Configuration(value = "multdao-interceptor")
public class MybatisMultdbConfiguration {

    /**
     * mybatis专有的sql拦截器,再项目的的mybatis配置里添加如下
     */
    //@Bean
    public TransactionInterceptorFactory mybatisInterceptor() {
        TransactionInterceptorFactory interceptor = new TransactionInterceptorFactory();
        Properties properties = new Properties();
        // 可以调用properties.setProperty方法来给拦截器设置一些自定义参数
        interceptor.setProperties(properties);
        return interceptor;
    }


}

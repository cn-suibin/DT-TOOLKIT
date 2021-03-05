package AnnoCases;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/*
1、@Component会被注册到ApplicationContext，被spring容器管理
2、@service是对@component进一步拓展。用于标注业务层组件
3、@Autowired与@Resource,前者是spring bean加载方式、需要set,get方法，后者是j2ee bean加载方式
*/

/**
 * @Author: suibin
 * @Description:重写通过类名称或类从app上下文获取bean对象的工具类，ApplicationContextAware是获取上下文bean的接口
 * @创建时间：2021年2月27日 上午2:17:44 
 */
@Component
public class BeanTool implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        if (applicationContext == null) {
            applicationContext = context;
        }
    }

    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

}

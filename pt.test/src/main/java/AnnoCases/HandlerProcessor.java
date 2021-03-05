package AnnoCases;



import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
@SuppressWarnings("unchecked")
public class HandlerProcessor implements BeanFactoryPostProcessor {

    private static final String HANDLER_PACKAGE = "AnnoCases.biz";

    /**
     * 扫描@HandlerType，初始化HandlerContext，将其注册到spring容器
     *
     * @param beanFactory bean工厂
     * @see HandlerType
     * @see HandlerContext
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    	Map<String, Class> handlerMap =new HashMap<String,Class>();
    	//1、扫描包路径下的类集合，并遍历放入handlerMap里。
    	//Map<String, Class> handlerMap = Maps.newHashMapWithExpectedSize(3);//google提供的guava包，比hashmap更快
        ClassScaner.scan(HANDLER_PACKAGE, HandlerType.class).forEach(clazz -> {
            String type = clazz.getAnnotation(HandlerType.class).value();
            handlerMap.put(type, clazz);
        });
        //2、创建一个包含handlerMap实例对象
        HandlerContext context = new HandlerContext(handlerMap);
        //3、注册到spring容器内
        beanFactory.registerSingleton(HandlerContext.class.getName(), context);
    }

}



package LearnCases;

import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * CGLIB动态代理实现
 *
 * @author pjmike
 * @create 2018-08-06 16:55
 */
public class CglibProxy {
    public static void main(String[] args) {
        //Enhancer是CGLIB的核心工具类,是一个字节码增强器，它可以方便的对类进行扩展
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(PersonService.class);
        //设置回调所需的拦截器
        enhancer.setCallback((Callback) new MyMethodInterceptor());
        //通过enhancer.create()方法获取代理对象
        //对代理对象所有非final的方法调用都会转发给MethodInterceptor.intercept方法,
        //作用跟JDK动态代理的InvocationHandler类似
        PersonService personService = (PersonService) enhancer.create();
        System.out.println(personService.sayHello("pjmike"));
    }
}

class PersonService {
    public String sayHello(String name) {
        return "Hello, " + name;
    }
}

class MyMethodInterceptor implements MethodInterceptor {

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        return methodProxy.invokeSuper(obj, args);
    }
}
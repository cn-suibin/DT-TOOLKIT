package LearnCases;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/*
 * 

	优点：使用者只关心业务逻辑，不需要着眼内部实现，方便后期的变更和部分共用代码的统一处理。
	缺点：当代理类中出现的被代理类越来越多时，内部就会显得非常臃肿。反而不利于管理阅读。
	
	动态代理和静态代理的区别和联系
	
	静态代理类：由程序员创建或由特定工具自动生成源代码，再对其编译。在程序运行前，代理类的.class文件就已经存在了。
	动态代理类：在程序运行时，运用反射机制动态创建而成。
	静态代理通常只代理一个类，动态代理是代理一个接口下的多个实现类。
	静态代理事先知道要代理的是什么，而动态代理不知道要代理什么东西，只有在运行时才知道。
	动态代理是实现JDK里的InvocationHandler接口的invoke方法，但注意的是代理的是接口，也就是你的业务类必须要实现接口，通过Proxy里的newProxyInstance得到代理对象。
	还有一种动态代理CGLIB，代理的是类，不需要业务类继承接口，通过派生的子类来实现代理。通过在运行时，动态修改字节码达到修改类的目的。
	

 * 
 * 
 * 
 */

/**
 * 动态代理的实现
 *
 * @author pjmike
 * @create 2018-08-04 17:42
 */
public class JDKDynamicProxy {
    public static void main(String[] args) {
        IHelloImpl hello = new IHelloImpl();
        MyInvocationHandler handler = new MyInvocationHandler(hello);
        //获取目标用户的代理对象
        IHello proxyHello = (IHello) Proxy.newProxyInstance(IHelloImpl.class.getClassLoader(), IHelloImpl.class.getInterfaces(), (InvocationHandler) handler);
        //调用代理方法
        proxyHello.sayHello("11");
        
        IHello proxyHello1 =new IHelloImpl();
        proxyHello1.sayHello("22");
        
    }
}

/**
 * 被访问者接口
 */
interface IHello{
    void sayHello(String a);
}

/**
 * 被访问者的具体实现类
 */
class IHelloImpl implements IHello {

    @Override
    public void sayHello(String a) {
        System.out.println("Hello World"+a);
    }
}

class MyInvocationHandler implements InvocationHandler {
    private Object target;

    /**
     *
     * @param target 被代理的目标对象
     */
    public MyInvocationHandler(Object target) {
        this.target = target;
    }

    /**
     * 执行目标对象的方法
     *
     * @param proxy 代理对象
     * @param method 代理方法
     * @param args 方法参数
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("invoke method");
        System.out.println("Method name : "+method.getName());
        Object result = method.invoke(target, args);
        return result;
    }


}
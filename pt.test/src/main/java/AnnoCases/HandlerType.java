package AnnoCases;

import java.lang.annotation.*;

/*自定义注解*/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface HandlerType {

    String value();

}


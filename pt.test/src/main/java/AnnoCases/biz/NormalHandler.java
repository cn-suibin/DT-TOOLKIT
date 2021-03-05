package AnnoCases.biz;

import AnnoCases.AbstractHandler;
import AnnoCases.HandlerType;
import org.springframework.stereotype.Component;


@Component
@HandlerType("1")
public class NormalHandler extends AbstractHandler {

    @Override
    public String handle(String bo) {
        return "处理普通订单";
    }

}


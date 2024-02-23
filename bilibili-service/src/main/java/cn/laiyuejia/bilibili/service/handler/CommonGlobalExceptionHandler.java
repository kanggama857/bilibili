package cn.laiyuejia.bilibili.service.handler;

import cn.laiyuejia.bilibili.domain.JsonResponse;
import cn.laiyuejia.bilibili.domain.exception.ConditionException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

//给Controller控制器添加统一的操作或处理。
@ControllerAdvice
//最高优先级
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CommonGlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    //表示该方法的返回结果直接写入 HTTP response body 返回 json 数据
    @ResponseBody
    public JsonResponse<String> commonExceptionHandler(HttpServletRequest request,Exception e){
        String eMessage = e.getMessage();
        // instanceof 当对象是右边类或子类所创建对象时，返回true；否则，返回false
        if(e instanceof ConditionException){
            String code = ((ConditionException) e).getCode();
            return new JsonResponse<>(code,eMessage);
        }else{
            return new JsonResponse<>("500",eMessage);
        }
    }
}

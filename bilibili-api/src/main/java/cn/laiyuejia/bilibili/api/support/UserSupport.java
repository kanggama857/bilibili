package cn.laiyuejia.bilibili.api.support;

import cn.laiyuejia.bilibili.domain.exception.ConditionException;
import cn.laiyuejia.bilibili.service.util.TokenUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class UserSupport {
    public Long getCurrentUserId(){
        // RequestContextHolder顾名思义,持有上下文的Request容器
        // 将ServletRequestAttributes绑定到RequestContextHolder类的两个ThreadLocal中，
        // 从而通过ThreadLocal的get方法获取ServletRequestAttributes。
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String token = requestAttributes.getRequest().getHeader("token");
        Long userId = TokenUtil.verifyToken(token);
        if(userId<0){
            throw new ConditionException("非法用户！");
        }
        return userId;
    }
}

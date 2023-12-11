package com.headline.app.gateway.filter;

import com.headline.app.gateway.util.AppJwtUtil;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthorizeFilter implements Ordered, GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1.获取request和response对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //2.判断是否登录
        if(request.getURI().getPath().contains("/login")){
            return chain.filter(exchange);
        }
        //3.获取token，判断token是否存在
        String token = request.getHeaders().getFirst("token");
        if(StringUtils.isBlank(token)){
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        //4.判断token是否有效
        try{
            Claims claimsBody = AppJwtUtil.getClaimsBody(token);
            //是否过期
            int res = AppJwtUtil.verifyToken(claimsBody);
            if(res==1||res==2){
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
        }catch (Exception e){
            e.printStackTrace();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        //5.放行
       return  chain.filter(exchange);



    }
    //优先值设置，值越小，优先级越大
    @Override
    public int getOrder() {
        return 0;
    }
}

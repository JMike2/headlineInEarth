package com.headline.wemedia.interceptor;

import com.headline.model.wemedia.pojos.WmUser;
import com.headline.utils.thread.WmTreadLocalUtil;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WmTokenInterceptor implements HandlerInterceptor {
    /**
     * 得到header中的用户信息，并存入到当前线程中
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userid = request.getHeader("userid");
        if(userid != null){
            WmUser wmUser = new WmUser();
            wmUser.setId(Integer.valueOf(userid));
            WmTreadLocalUtil.setUser(wmUser);
        }
        return true;
    }

    /**
     * 清理线程中的数据
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        WmTreadLocalUtil.clear();
    }
}

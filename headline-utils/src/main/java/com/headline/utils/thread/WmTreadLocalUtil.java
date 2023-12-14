package com.headline.utils.thread;

import com.headline.model.wemedia.pojos.WmUser;

public class WmTreadLocalUtil {
    private final static ThreadLocal<WmUser> WM_USER_THREAD_LOCAL = new ThreadLocal<>();

    public static void setUser(WmUser wmUser) {
        WM_USER_THREAD_LOCAL.set(wmUser);
    }

    public static WmUser getUser(){
        return WM_USER_THREAD_LOCAL.get();
    }
    public static void clear(){
        WM_USER_THREAD_LOCAL.remove();
    }
}

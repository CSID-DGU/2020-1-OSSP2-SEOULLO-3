package com.seoullo.seoullotour.Utils;

import com.seoullo.seoullotour.Models.Route;

import cz.msebera.android.httpclient.client.utils.CloneUtils;

public final class SharedRoute {
    private static Route shareRoute;

    public static synchronized void setShareRoute(Route ref) throws CloneNotSupportedException {
        shareRoute = (Route) CloneUtils.clone(ref);
        System.out.println("[ shared ] : " + shareRoute.getGuideArray().get(0).toString());
    }
    public static synchronized Route getShareRoute() {
        return shareRoute;
    }
}

package com.seoullo.seoullotour.Models;

import java.io.Serializable;

public class Point implements Serializable {
    // 위도
    public double x;
    // 경도
    public double y;

    public String location;
    // 포인트를 받았는지 여부
    public boolean havePoint;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("x : ");
        builder.append(x);
        builder.append(" y : ");
        builder.append(y);
        builder.append(" addr : ");
        builder.append(location);

        return builder.toString();
    }
}
package com.thousandsunny.common;

import org.hamcrest.core.IsNull;

import static java.util.Objects.isNull;

/**
 * 如果这些代码有用，那它们是guitarist在29/11/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public class DistanceUtil {
    //返回km
    public static double getDistance(Double lon1, Double lat1, Double lon2, Double lat2) {
        final double EARTH_RADIUS = 6378137;
        lon1 = isNull(lon1) ? 0 : lon1;
        lat1 = isNull(lat1) ? 0 : lat1;
        lon2 = isNull(lon2) ? 0 : lon2;
        lat2 = isNull(lat2) ? 0 : lat2;

        double radLat1 = lat1 * Math.PI / 180.0;
        double radLat2 = lat2 * Math.PI / 180.0;
        double radLon1 = lon1 * Math.PI / 180.0;
        double radLon2 = lon2 * Math.PI / 180.0;

        if (radLat1 < 0)
            radLat1 = Math.PI / 2 + Math.abs(radLat1);// south
        if (radLat1 > 0)
            radLat1 = Math.PI / 2 - Math.abs(radLat1);// north
        if (radLon1 < 0)
            radLon1 = Math.PI * 2 - Math.abs(radLon1);// west
        if (radLat2 < 0)
            radLat2 = Math.PI / 2 + Math.abs(radLat2);// south
        if (radLat2 > 0)
            radLat2 = Math.PI / 2 - Math.abs(radLat2);// north
        if (radLon2 < 0)
            radLon2 = Math.PI * 2 - Math.abs(radLon2);// west
        double x1 = EARTH_RADIUS * Math.cos(radLon1) * Math.sin(radLat1);
        double y1 = EARTH_RADIUS * Math.sin(radLon1) * Math.sin(radLat1);
        double z1 = EARTH_RADIUS * Math.cos(radLat1);

        double x2 = EARTH_RADIUS * Math.cos(radLon2) * Math.sin(radLat2);
        double y2 = EARTH_RADIUS * Math.sin(radLon2) * Math.sin(radLat2);
        double z2 = EARTH_RADIUS * Math.cos(radLat2);

        double d = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2));
        //余弦定理求夹角
        double theta = Math.acos((EARTH_RADIUS * EARTH_RADIUS + EARTH_RADIUS * EARTH_RADIUS - d * d) / (2 * EARTH_RADIUS * EARTH_RADIUS));
        double dist = theta * EARTH_RADIUS;
        return dist;
    }

    /**
     * 比较版本号的大小,前者大则返回一个正数,后者大返回一个负数,相等则返回0
     *
     * @Author mu.jie
     * @Date 2017/1/19
     */
    public static int compareToVersion(String thisVersion, String thatVersion) {
        if (thatVersion == null)
            return 1;
        String[] thisParts = thisVersion.split("\\.");
        String[] thatParts = thatVersion.split("\\.");
        int length = Math.max(thisParts.length, thatParts.length);
        for (int i = 0; i < length; i++) {
            int thisPart = i < thisParts.length ?
                    Integer.parseInt(thisParts[i]) : 0;
            int thatPart = i < thatParts.length ?
                    Integer.parseInt(thatParts[i]) : 0;
            if (thisPart < thatPart)
                return -1;
            if (thisPart > thatPart)
                return 1;
        }
        return 0;
    }

}

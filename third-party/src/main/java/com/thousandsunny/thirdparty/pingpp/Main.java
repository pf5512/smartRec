package com.thousandsunny.thirdparty.pingpp;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by Afon on 16/4/26.
 */
public class Main {

    private static SecureRandom random = new SecureRandom();

    public static String randomString(int length) {
        String str = new BigInteger(130, random).toString(32);
        return str.substring(0, length);
    }
}

package com.kcirqueit.spinandearn.util;

import java.util.ArrayList;
import java.util.List;

public class SpinConstant {

    public static final String[] LOCAL_PAYMENT_METHOD = new String[] {
            "bKash",
            "Rocket",
            "Mobile Recharge"
    };

    public static final String[] INTERNATIONAL_PAYMENT_METHOD = new String[] {
            "Paypal",
            "Payonneer",
            "Payeer",
            "Paytm"
    };

    public static List<String> getLocalPaymentMethod() {
        List<String> list = new ArrayList<>();
        list.add("bKash");
        list.add("Rocket");
        list.add("Mobile Recharge");
        return list;

    }

    public static List<String> getInternationalPaymentMethod() {
        List<String> list = new ArrayList<>();
        list.add("Paypal");
        list.add("Payonneer");
        list.add("Paytm");
        list.add("Payeer");
        return list;

    }

}

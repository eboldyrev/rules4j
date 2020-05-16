package com.github.eboldyrev.ruleengine.utils;

import java.util.Collection;
import java.util.Map;

public class Utils {

    private Utils(){
    }

    public static boolean isEmpty(Collection c){
        return c == null || c.isEmpty();
    }

    public static boolean isEmpty(Map m){
        return m == null || m.isEmpty();
    }

    public static void nonEmpty(Collection c){
        if (isEmpty(c)) {
            throw new IllegalArgumentException("Should be not empty");
        }
    }

    public static void nonEmpty(Map c){
        if (isEmpty(c)) {
            throw new IllegalArgumentException("Should be not empty");
        }
    }



}

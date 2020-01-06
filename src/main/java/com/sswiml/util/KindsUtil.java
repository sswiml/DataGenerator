package com.sswiml.util;

import java.util.Random;

/**
 * 通用包
 * 支持...字段类型的生成
 */
public class KindsUtil {

    public static String t1(String obj){
        Random r=new Random();
        return r.nextInt(10)+obj;
    }
    public static String t2(){
        return "t2";
    }
    public static String t3(){
        return "t3";
    }


}

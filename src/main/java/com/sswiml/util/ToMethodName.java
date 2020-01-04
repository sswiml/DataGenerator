package com.sswiml.util;

public enum ToMethodName {

    函数1("t1"),函数2("t2");

    private String methodName;
    ToMethodName(String methodName) {
        this.methodName=methodName;
    }
    public String getMethodName(){
        return methodName;
    }

    public static boolean contains(String type){
        System.out.println("ToMethodName.contains");
        if(type==null) return false;
        for(ToMethodName typeEnum : ToMethodName.values()){
            if(typeEnum.name().equals(type)){
                return true;
            }
        }
        return false;
    }



}

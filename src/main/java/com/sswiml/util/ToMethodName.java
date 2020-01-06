package com.sswiml.util;

public enum ToMethodName {


    姓名("getName"),年龄("getAge"),身份证号("getIdCardNum"),整数("getInteger"),浮点数("getFloat");


    private String methodName;
    ToMethodName(String methodName) {
        this.methodName=methodName;
    }
    public String getMethodName(){
        return methodName;
    }

    public static boolean contains(String type){

        if(type==null) return false;
        for(ToMethodName typeEnum : ToMethodName.values()){
            if(typeEnum.name().equals(type)){
                return true;
            }
        }
        return false;
    }


}

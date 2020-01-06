package com.sswiml.util;

import java.util.LinkedList;
import java.util.List;

public class StringUtil {

    /**
     * 格式分割
     * @param oldString 原始字符
     * @param split 分隔符
     * @return
     */
    public static List<String> parseWord(List<String> newStringList,String oldString,char split){
        String temp="";
        for (int i=0;i<oldString.length();++i){
            char c=oldString.charAt(i);
            if (c==split){
                newStringList.add(temp);
                temp="";
            }
            else {
                temp+=c;
            }
        }
        newStringList.add(temp);
        return newStringList;
    }

    //格式判断
    public static boolean formatJudge(String fieldname){
        return true;
    }

    public static String getTableName(String str){
       return str.substring(0,str.indexOf("."));
    }

    public static void main(String[] args){
        System.out.println(getTableName("aaa.bbb"));
    }

    public static String getTableField(String str) {
        return str.substring(str.indexOf(".")+1,str.length());
    }
}

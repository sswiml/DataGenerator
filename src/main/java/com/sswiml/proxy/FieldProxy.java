package com.sswiml.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class FieldProxy implements InvocationHandler {

    private Object obj;

    public FieldProxy(Object obj){
        this.obj=obj;
    }

    /**
     * args[0] 字段类型 args[1] 限制条件
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Object invoke=method.invoke(obj,args[1]);
        if (args[0].toString().equals("VARCHAR")||args[0].toString().equals("DATETIME"))
            invoke="'"+invoke+"'";
        return invoke;
    }

}

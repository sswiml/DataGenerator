package com.sswiml.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class FieldProxy implements InvocationHandler {

    private Object obj;

    public FieldProxy(Object obj){
        this.obj=obj;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object invoke=method.invoke(obj,args);
        return invoke;
    }

}

package com.serendipity.chengzhengqian.jsos;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class JsInvocativeHandler implements InvocationHandler {
    String jsObjectName;
    CommandLock c;
    JsInvocativeHandler(String funcCode, CommandLock c){
        jsObjectName=funcCode;
        this.c=c;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        c.callJsObjectInCurrentThread(jsObjectName,method.getName(),objects);
        return null;
    }
}

package com.serendipity.chengzhengqian.jsos;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class JsInvocativeHandler implements InvocationHandler {
    String code;
    JsInvocativeHandler(String funcCode){
        code=funcCode;
    }
    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        //notice to handle toString method!
        GlobalState.printToLog(
               code,GlobalState.info
                        );
        return null;
    }
}

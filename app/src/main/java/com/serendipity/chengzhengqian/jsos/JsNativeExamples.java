package com.serendipity.chengzhengqian.jsos;

public class JsNativeExamples {
    public static long ctx;
    public static void init(){
        ctx=JsNative.createHeapDefault();
        JsNative.registerJavaHandle(ctx);
        JsNative.registerProxyHandleGet(ctx);
        JsNative.registerJsObjectFinalizer(ctx);
    }
    public static String tutorial1(){
        String expr="123+123";
        JsNative.evalString(ctx,expr);
        int result=JsNative.getInt(ctx,-1);
        return expr+"="+result+"\n";
    }
    public static String tutorial2(){
        String expr="\"123\"+\"123\"";
        JsNative.evalString(ctx,expr);
        String result=JsNative.getString(ctx,-1);
        return expr+"="+result+"\n";
    }
    public static String tutorial3(){
        StringBuilder sb=new StringBuilder();
        int a=1;
        double b=1.5;
        String c="very good!";

        sb.append(String.format("Push %d in stack\n",a));
        JsNative.pushInt(ctx,a);
        sb.append(String.format("Push %f in stack\n",b));
        JsNative.pushNumber(ctx,b);
        sb.append(String.format("Push \"%s\" in stack\n",c));
        JsNative.pushString(ctx,c);
        sb.append(String.format("now the stack is [-3]%d,[-2]%f, [-1]\"%s\"\n",
                JsNative.getInt(ctx, -3), JsNative.getNumber(ctx, -2),
                JsNative.getString(ctx,-1)
        ));

        return sb.toString();
    }
    public static String tutorial4(){
        StringBuilder sb=new StringBuilder();

        String a="key";
        double b=1.5;

        int index=JsNative.pushObject(ctx);
        sb.append(String.format("Push a new empty ojbect in stack at [%d]\n",index));
        sb.append(String.format("Push key \"%s\" in stack\n",a));
        JsNative.pushString(ctx,a);
        sb.append(String.format("Push %f in stack\n",b));
        JsNative.pushNumber(ctx,b);
        sb.append(String.format("Put property for [%d] in stack\n",index));
        JsNative.putProp(ctx,index);
        JsNative.pushString(ctx,a);
        JsNative.getProp(ctx,index);
        double b_new=JsNative.getNumber(ctx,-1);
        sb.append(String.format("Push key and get property %f for [%d] in stack\n",b_new,index));

        return sb.toString();
    }
    public static String tutorial5(){
        StringBuilder sb=new StringBuilder();

        sb.append(String.format("getTopIndex %d\n",JsNative.getTopIndex(ctx)));
        sb.append(String.format("getTop %d\n",JsNative.getTop(ctx)));
        sb.append("pop\n");
        JsNative.pop(ctx);
        sb.append("popN(2)\n");
        JsNative.popN(ctx,2);
        sb.append(String.format("getTopIndex %d\n",JsNative.getTopIndex(ctx)));
        sb.append(String.format("getTop %d\n",JsNative.getTop(ctx)));

        return sb.toString();
    }

    public static String tutorial6(){
        StringBuilder sb=new StringBuilder();
        int a=JsNative.PROXYGETHANDLE;
        String b="arg2";
        int index=JsNative.pushJavaHandle(ctx);
        sb.append(String.format("Push JavaHandle in stack at[%d]\n",index));
        sb.append(String.format("Push %s in stack\n",a));
        JsNative.pushInt(ctx,a);
        sb.append(String.format("Push %s in stack\n",b));
        JsNative.pushObject(ctx,b);
        JsNative.pushObject(ctx,b);
        sb.append("call(3)\n");
        JsNative.call(ctx,3);
        return sb.toString();
    }
    public static String tutorial7(){
        StringBuilder sb=new StringBuilder();
        String a="arg1";
        String value="symbol";
        int index=JsNative.pushObject(ctx);
        sb.append(String.format("Push new object in stack at[%d]\n",index));

        sb.append(String.format("Push symbol %s in stack\n",a));
        JsNative.pushSymbol(ctx,a);
        sb.append(String.format("Push %s in stack\n",value));
        JsNative.pushString(ctx,value);
        sb.append(String.format("set property for [%d] in stack\n",index));
        JsNative.putProp(ctx,index);
        sb.append(String.format("Push string %s in stack\n",a));
        JsNative.pushString(ctx,a);
        sb.append(String.format("get property for [%d] in stack\n",index));
        JsNative.getProp(ctx,index);
        sb.append("the result is "+JsNative.getString(ctx,-1)+"\n");
        sb.append(String.format("Push symbol %s in stack\n",a));
        JsNative.pushSymbol(ctx,a);
        sb.append(String.format("get property for [%d] in stack\n",index));
        JsNative.getProp(ctx,index);
        sb.append("the result is "+JsNative.getString(ctx,-1)+"\n");


        return sb.toString();
    }
    public static String tutorial8() {
        StringBuilder sb = new StringBuilder();
        int a = 123;
        sb.append(String.format("Push %d in stack\n",a));
        JsNative.pushInt(ctx,a);
        String name="a";
        sb.append(String.format("Put it to %s as a property of the global object\n",name));
        JsNative.putGlobalString(ctx,name);
        JsNative.getGlobalString(ctx,name);
        sb.append(String.format("Get %s as a property of the global object as %d\n",name,
                JsNative.getInt(ctx,-1)));

        return sb.toString();

    }
    public static String tutorial9() {
        StringBuilder sb = new StringBuilder();
        String code = "function(a){return function(){return a+1};}";
        sb.append(String.format("Push code \"%s\" in stack\n",code));
        JsNative.pushString(ctx,code);
        String name="closure_test";
        sb.append(String.format("Push name \"%s\" in stack\n",name));
        JsNative.pushString(ctx,name);
        JsNative.compile(ctx,JsNative.DUK_COMPILE_FUNCTION);
        JsNative.pushContextDump(ctx);
        sb.append(String.format("After compile, context is %s\n",
                JsNative.getString(ctx,-1)));
        JsNative.pop(ctx);
        int a=2;
        JsNative.pushInt(ctx,a);
        JsNative.call(ctx,1);
        JsNative.pushContextDump(ctx);
        sb.append(String.format("After push %d, call(1), context is %s\n",a,
                JsNative.getString(ctx,-1)));
        JsNative.pop(ctx);

        JsNative.call(ctx,0);
        JsNative.pushContextDump(ctx);
        sb.append(String.format("call(0), a function with clojure a=%d, context is %s\n",a,
                JsNative.getString(ctx,-1)));
        JsNative.pop(ctx);

        return sb.toString();

    }
    public static String tutorial10() {
        StringBuilder sb = new StringBuilder();

        JsNative.pushPointer(ctx,sb);
        JsNative.pushContextDump(ctx);
        sb.append(String.format("After push Java Object pointer,  context is %s\n",
                JsNative.getString(ctx,-1)));
        JsNative.pop(ctx);

        StringBuilder sb_new= (StringBuilder) JsNative.getPointer(ctx,-1);
        sb_new.append("this is added from retrieved object then we del it\n");
        JsNative.delPointer(ctx,-1);
        return sb.toString();
    }
    public static String tutorial11() {
        StringBuilder sb = new StringBuilder();

        int index=JsNative.pushObject(ctx,sb);
        JsNative.pushContextDump(ctx);
        sb.append(String.format("After push Java Object as a bare JsObject,  context is %s, Object [%d]\n",
                JsNative.getString(ctx,-1),index));
        JsNative.pop(ctx);

        StringBuilder sb_new= (StringBuilder) JsNative.getObject(ctx,index);

        sb_new.append("this is added from retrieved object then we del it\n");

        return sb.toString();
    }
    public static String tutorial12() {
        StringBuilder sb = new StringBuilder();
        TestClass a=new TestClass(12);
        int index=JsNative.pushObject(ctx,a);
        JsNative.pushProxyObject(ctx);
        sb.append(String.format("Push a object and proxy it. Then push \"prop\" in stack and get property of [%d]",index));
        JsNative.pushString(ctx,"prop");
        JsNative.getProp(ctx,index);
        JsNative.popN(ctx,4);

        JsNative.pushContextDump(ctx);
        sb.append(String.format("After pop some elements,  context is %s, see garbage collection\n",
                JsNative.getString(ctx,-1)));
        JsNative.pop(ctx);

        return sb.toString();

    }
    public static void close(){
        JsNative.destroyHeap(JsNativeExamples.ctx);
    }
}

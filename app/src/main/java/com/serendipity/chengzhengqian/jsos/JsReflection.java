package com.serendipity.chengzhengqian.jsos;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.HashMap;

public class JsReflection {
    /**
     * return null if failed
     * @param obj
     * @param name
     * @return
     */

    public static int AsObject=1;
    public static int AsClass=2;

    public static void setField(Object obj, String name, Object val){
        if(obj instanceof Class){
            if(setField(obj,name,val,AsClass))  return;
            setField(obj,name,val,AsObject);
        }
        else{
            if(setField(obj,name,val,AsObject))  return;
            setField(obj,name,val,AsClass);
        }
    }
    public static boolean setField(Object obj,String name, Object val, int type){

        Class<?> c=getClass(obj, type);
        if(c!=null){
            try {
                Field f=c.getField(name);
                f.set(obj, val);
                return true;
            } catch (Exception e) {
                GlobalState.printToLog(e.toString(),GlobalState.error);
                return false;
            }
        }
        else{
            if(JsNative.ISDEBUG)
                GlobalState.printToLog("Error in get class\n",GlobalState.infoDebug);
            return false;
        }
    }
    public static Class<?> getClass(Object obj, int type){
        Class<?> c=null;
        if(type==AsObject )
            c=obj.getClass();
        else if(type==AsClass && (obj instanceof Class)){
            c= (Class<?>) obj;
        }
        else {
            if(JsNative.ISDEBUG)
                GlobalState.printToLog(String.format("TRY [obj:%s] as class instance, failed\n"
                        ,obj.getClass().getSimpleName()),GlobalState.infoDebug);
            return null;
        }
        return c;
    }

    public static Object getField(Object obj, String name, int type){
        Class<?> c=null;

        try {
            c=getClass(obj,type);
            if(c==null){
                return null;
            }
            Field f=c.getField(name);
            return f.get(obj);

        } catch (Exception e) {
            if(JsNative.ISDEBUG)
                GlobalState.printToLog(String.format("TRY [obj:%s] %s, %s\n"
                    ,c.getSimpleName(), name,e.toString()),GlobalState.infoDebug);

        }
        return null;
    }
    public static Object getField(Object obj, String name){
        Object result;
        if(obj instanceof Class){
            result=getField(obj,name,AsClass);
            if(result==null){
                return getField(obj,name,AsObject);
            }
        }
        else {
            result=getField(obj,name,AsObject);
            if(result==null){
                return getField(obj,name,AsClass);
            }
        }
        return result;
    }

    public static boolean isMatch(Class<?>[] types, Object[] paras){
        if(types.length==paras.length){
            for(int i=0;i<paras.length;i++){
                if(types[i]==int.class){if (! (paras[i] instanceof Integer)) return false;}
                else if(types[i]==double.class){if(! (paras[i] instanceof Double)) return false;}
                else if(types[i]==boolean.class){if(!(paras[i] instanceof Boolean))return false;}
                else if(!(types[i].isInstance(paras[i])))return false;
            }
            return true;
        }
        return false;
    }
    public static String showMethod(Method m){
        StringBuilder b=new StringBuilder();
        b.append(m.getReturnType().getClass().getSimpleName()+" "+m.getName()+"(");
        int index=0;
        for(Class<?> c: m.getParameterTypes()){
            if(index>0)
                b.append(", ");
            b.append(c.getSimpleName());
        }
        b.append(")");
        return b.toString();
    }
    public static String showMethod(Constructor m){
        StringBuilder b=new StringBuilder();
        b.append("(");
        int index=0;
        for(Class<?> c: m.getParameterTypes()){
            if(index>0)
                b.append(", ");
            b.append(c.getSimpleName());
        }
        b.append(")");
        return b.toString();
    }

    public static String showMethod(Object target,String name, Object[] paras){
        StringBuilder b=new StringBuilder();
        b.append(String.format("[obj:%s].%s",target.getClass().getSimpleName(),name)+"(");
        int index=0;
        for(Object obj: paras){
            if(index>0)
                b.append(", ");
            b.append(obj.getClass().getSimpleName());
            index++;
        }
        b.append(")");
        return b.toString();
    }

    public static boolean isMatch(Method m, String name, Object[] paras){
        if(m.getName().equals(name)){
            Class<?>[] types=m.getParameterTypes();
            if(isMatch(types,paras)){
                return true;
            }
            else {
                if(JsNative.ISDEBUG){
                    GlobalState.printToLog("FIND "+showMethod(m)+"\n",GlobalState.infoDebug);
                }
                return false;
            }
        }
        return false;
    }

    public static boolean isMatch(Constructor m, Object[] paras){

        Class<?>[] types=m.getParameterTypes();
        if(isMatch(types,paras)){
                return true;
        }
        else {
            if(JsNative.ISDEBUG){
                GlobalState.printToLog("FIND constructor "+showMethod(m)+"\n",
                        GlobalState.infoDebug);
            }
            return false;
        }

    }

    public static String showMethods(Object b,String hint) {
        Class<?> c=b.getClass();
        StringBuilder s=new StringBuilder();
        for(Field f:c.getFields()){
            if(hint.equals("")||f.getName().startsWith(hint))
                s.append(","+f.getName());
        }
        for(Method f:c.getMethods()){
            if(hint.equals("")||f.getName().startsWith(hint))
                s.append(","+f.getName());
        }
        return s.toString();
    }

    public static class CallResult{
        boolean sucess=false;
    }
    public static HashMap<Class<?>, Method> quickMethods=new HashMap<>();
    /**
     * call obj 's method, type control wether we interprete obj as an instance or an class.
     * The later will be checked to ensure obj instanceof class
     * CallResult indicates whether the call is sucessful.
     * @param obj
     * @param name
     * @param paras
     * @param type
     * @param r
     * @return
     */
    public static Object call(Object obj, String name, Object[] paras, int type, CallResult r){
        Class<?> c=getClass(obj,type);
        /*handle the case to try wrong method*/
        if(name.equals("toString"))
            c=obj.getClass();
        if(c!=null){
            /*first check whether it is constructor*/
            if(name.equals(CONSTRUCTORTAG)){
                if(JsNative.ISDEBUG)GlobalState.printToLog(
                        "TRY constructor "+c.getSimpleName()+"\n",
                        GlobalState.infoDebug);
                for(Constructor ct:c.getConstructors()){
                    if(isMatch(ct,paras)){
                        try {
                            r.sucess=true;
                            if(JsNative.ISDEBUG){
                                GlobalState.printToLog("INVOKE constructor  "+c.getSimpleName()+"\n",
                                        GlobalState.infoDebug);
                            }
                            return ct.newInstance(paras);
                        } catch (Exception e) {
                            GlobalState.printToLog(e.toString(),GlobalState.error);
                        }
                    }
                }
                if(JsNative.ISDEBUG)
                    GlobalState.printToLog("FAIL to find matched constructor\n",
                        GlobalState.infoDebug);
            }

            /*try quick method, in case this method has been called frequently,
            * improve the algorithm to find*/
            if (quickMethods.get(c)!=null) {
                Method m=quickMethods.get(c);
                if(isMatch(m,name,paras))
                    try{
                        r.sucess=true;
                        return m.invoke(obj,paras);
                    }catch(Exception e){
                        GlobalState.printToLog(e.toString(),GlobalState.error);
                    }
            }
            for(Method m: c.getMethods()){
                if(isMatch(m,name,paras)){
                    try {
                        r.sucess=true;
                        quickMethods.put(c,m);
                        return m.invoke(obj,paras);
                    } catch (Exception e) {
                        GlobalState.printToLog(e.toString(),GlobalState.error);
                    }
                }

            }
            return null;
        }
        else {
            return null;
        }
    }

    /**
     * this handles three types of calls
     * constructor
     * static method
     * object method
     * @param obj
     * @param name
     * @param paras
     * @return
     */
    public static String CONSTRUCTORTAG="new";
    public static Object call(Object obj, String name, Object[] paras){
        CallResult r=new CallResult();

        if(obj instanceof Class<?>) {
            Object result = call(obj, name, paras, AsClass, r);
            if(r.sucess)
                return result;
            if (JsNative.ISDEBUG)
                GlobalState.printToLog("As class:"+showMethod(obj, name, paras) + " sucess:" + String.valueOf(r.sucess) + "\n",
                        GlobalState.infoDebug);
            result = call(obj, name, paras, AsClass, r);
            if(r.sucess)
                return result;
            if (JsNative.ISDEBUG)
                GlobalState.printToLog("As object:"+showMethod(obj, name, paras) + " sucess:" + String.valueOf(r.sucess) + "\n",
                        GlobalState.infoDebug);
            return result;
        }
        else{
            Object result = call(obj, name, paras, AsObject, r);
            if(r.sucess)
                return result;
            if (JsNative.ISDEBUG)
                GlobalState.printToLog("As object:"+showMethod(obj, name, paras) + " sucess:" + String.valueOf(r.sucess) + "\n",
                        GlobalState.infoDebug);
            result = call(obj, name, paras, AsClass, r);
            if(r.sucess)
                return result;
            if (JsNative.ISDEBUG)
                GlobalState.printToLog("As class:"+showMethod(obj, name, paras) + " sucess:" + String.valueOf(r.sucess) + "\n",
                        GlobalState.infoDebug);
            return result;
        }
    }
}

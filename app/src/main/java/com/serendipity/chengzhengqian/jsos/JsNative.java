package com.serendipity.chengzhengqian.jsos;

public class JsNative {
    /**
     * (No effect on value stack.)
     * Create a new Duktape heap and return an initial context (thread). If heap initialization fails, a NULL is returned. There is currently no way to obtain more detailed error information.
     * The created heap will use default memory management and fatal error handler functions. This API call is equivalent to:
     * ctx = duk_create_heap(NULL, NULL, NULL, NULL, NULL);
     *
     * @return check whether it is zero (indicates the heap fails to be created
     */
    public static native long createHeapDefault();

    /**
     * . . .  → . . . result
     * Like duk_eval(), but the eval input is given as a C string. The filename associated with the temporary eval function is "eval".
     *
     * @param context
     * @param source
     */
    public static native void evalString(long context, String source);

    /**
     * . . . val . . .
     * Get the number at idx and convert it to a C duk_int_t by first clamping the value between [DUK_INT_MIN, DUK_INT_MAX] and then truncating towards zero. The value on the stack is not modified. If the value is a NaN, is not a number, or the index is invalid, returns 0.
     * <p>
     * Conversion examples:
     * <p>
     * Input	Output
     * -Infinity	DUK_INT_MIN;
     * DUK_INT_MIN - 1	DUK_INT_MIN;
     * -3.9	-3;
     * 3.9	3;
     * DUK_INT_MAX + 1	DUK_INT_MAX;
     * +Infinity	DUK_INT_MAX;
     * NaN	0;
     * "123"	0 (non-number);
     *
     * @param context
     * @param index
     * @return
     */
    public static native int getInt(long context, int index);

    /**
     * (No effect on value stack.)
     * Destroy a Duktape heap. The argument context can be any context linked to the heap. All resources related to the heap are freed and must not be referenced after the call completes. These resources include all contexts linked to the heap, and also all string and buffer pointers within the heap.
     *
     * @param context If ctx is NULL, the call is a no-op.
     */
    public static native void destroyHeap(long context);

    /**
     * . . . val . . .
     * Get character data pointer for a string at idx without modifying or coercing the value. Returns a non-NULL pointer to the read-only, NUL-terminated string data. Returns NULL if the value is not a string or the index is invalid.
     * <p>
     * To get the string byte length explicitly (which is useful if the string contains embedded NUL characters), use duk_get_lstring().
     * <p>
     * A non-NULL return value is guaranteed even for zero length strings; this differs from how buffer data pointers are handled (for technical reasons).
     * Symbol values are visible in the C API as strings so that both duk_is_symbol() and duk_is_string() are true. This behavior is similar to Duktape 1.x internal strings. Symbols are still an experimental feature; for now, you can distinguish Symbols from ordinary strings using duk_is_symbol(). For the internal representation, see symbols.rst.
     *
     * @param context
     * @param index
     * @return
     */
    public static native String getString(long context, int index);

    /**
     * . . .  → . . . val  (if key exists)
     * . . .  → . . . undefined  (if key doesn't exist)
     * Summary §
     * Get property named key from the global object. Returns non-zero if the property exists and zero otherwise. This is a convenience function which does the equivalent of:
     * <p>
     * duk_bool_t ret;
     * <p>
     * duk_push_global_object(ctx);
     * ret = duk_get_prop_string(ctx, -1, key);
     * duk_remove(ctx, -2);
     *
     * @param context
     * @param name
     * @return
     */
    public static native boolean getGlobalString(long context, String name);

    /**
     * . . .  → . . . val
     * Summary §
     * Convert val to an IEEE double and push it to the stack.
     * <p>
     * This is a shorthand for calling duk_push_number(ctx, (duk_double_t) val).
     *
     * @param context
     * @param value
     */
    public static native void pushInt(long context, int value);

    /**
     * . . .  → . . . val
     * Summary §
     * Push number (IEEE double) val to the stack.
     * <p>
     * If val is a NaN it may be normalized into another NaN form.
     *
     * @param context
     * @param value
     */
    public static native void pushNumber(long context, double value);

    /**
     * void duk_push_null(duk_context *ctx);
     * Stack §
     * . . .  → . . . null
     * Summary §
     * Push null to the stack.
     *
     * @param context
     */
    public static native void pushNull(long context);

    /**
     * . . . target handler  → . . . proxy
     * Summary §
     * return duk_push_proxy(ctx, 0);
     * Push a new Proxy object for target and handler table given on the value stack, equivalent to new Proxy(target, handler). The proxy_flags argument is currently (Duktape 2.2) unused, calling code must pass in a zero.
     *
     * @param context
     * @return
     */
    public static native int pushProxy(long context);

    /**
     * . . .  → . . . ptr
     * NOTICE !!!
     * Internally, it use
     * duk_push_pointer(ctx,env->NewGlobalRef(p));
     * to create a global reference to pointer so that object pointed won't be colletecd
     * One need to release it when done
     * Avoid use it, as be careful to use delPointer to remove global reference
     * Use pushObject(long ctx, Object obj) instead. It will wrap it in a barebone object and register a finalizer
     * <p>
     * Summary §
     * Push p into the stack as a pointer value. Duktape won't interpret the pointer in any manner.
     *
     * @param context
     */
    public static native void pushPointer(long context, Object object);

    /**
     * . . . val . . .
     * Summary §
     * Get the pointer value at idx as void * without modifying or coercing the value. Returns NULL if the value is not a pointer or the index is invalid.
     */

    public static native Object getPointer(long context, int index);

    /**
     * Delete the globalRefence for the pointer, the value of it won't change
     * deleteGlobalRef(env, duk_get_pointer(ctx, idx));
     *
     * @param context
     */
    public static native void delPointer(long context, int index);

    /**
     * . . . val . . .
     * Summary §
     * Get the number value at idx without modifying or coercing the value. Returns NaN if the value is not a number or the index is invalid.
     *
     * @return
     */
    public static native double getNumber(long context, int index);

    /**
     * . . .  → . . . str  (if str != NULL)
     . . .  → . . . null  (if str == NULL)
     Summary §
     Push a C string into the stack. String length is automatically detected with a strlen() equivalent (i.e. looking for the first NUL character). A pointer to the interned string data is returned. If the operation fails, throws an error.

     If str is NULL, an Ecmascript null is pushed to the stack and NULL is returned. This behavior differs from duk_push_lstring on purpose.

     C code should normally only push valid CESU-8 strings to the stack. Some invalid CESU-8/UTF-8 byte sequences are reserved for special uses such as representing Symbol values. When you push such an invalid byte sequence, the value on the value stack will behave like a string for C code but will appear as a Symbol for Ecmascript code. See Symbols for more discussion.
     If input string might contain internal NUL characters, use duk_push_lstring() instead.
     * @param context
     */
    /**
     * . . .  → . . . str
     * Summary §
     * Push a one-line string summarizing the state of the current activation of context ctx. This is useful for debugging Duktape/C code and is not intended for production use.
     * <p>
     * The exact dump contents are version specific. The current format includes the stack top (i.e. number of elements on the stack) and prints out the current elements as an array of JX-formatted (Duktape's custom extended JSON format) values. The example below would print something like:
     * <p>
     * ctx: top=2, stack=[123,"foo"]
     * You should not leave dump calls in production code.
     *
     * @param context
     */
    public static native void pushContextDump(long context);

    /**
     * . . .  → . . . str  (if str != NULL)
     * . . .  → . . . null  (if str == NULL)
     * Summary §
     * Push a C string into the stack. String length is automatically detected with a strlen() equivalent (i.e. looking for the first NUL character). A pointer to the interned string data is returned. If the operation fails, throws an error.
     * <p>
     * If str is NULL, an Ecmascript null is pushed to the stack and NULL is returned. This behavior differs from duk_push_lstring on purpose.
     * <p>
     * C code should normally only push valid CESU-8 strings to the stack. Some invalid CESU-8/UTF-8 byte sequences are reserved for special uses such as representing Symbol values. When you push such an invalid byte sequence, the value on the value stack will behave like a string for C code but will appear as a Symbol for Ecmascript code. See Symbols for more discussion.
     * If input string might contain internal NUL characters, use duk_push_lstring() instead.
     *
     * @param context
     * @param value
     */
    public static native void pushString(long context, String value);

    /**
     * void duk_push_global_object(duk_context *ctx);
     * Stack §
     * . . .  → . . . global
     * Summary §
     * Push the global object to the stack.
     * <p>
     * Example §
     * duk_push_global_object(ctx);
     *
     * @param context
     */
    public static native void pushGlobalObject(long context);

    /**
     * Duktape supports ES2015 Symbols and also provides a Duktape specific hidden Symbol variant similar to internal strings in Duktape 1.x. Hidden Symbols differ from ES2015 Symbols in that they're hidden from ordinary Ecmascript code: they can't be created from Ecmascript code, won't be enumerated or JSON-serialized, and won't be returned from Object.getOwnPropertyNames() or even Object.getOwnPropertySymbols(). Properties with hidden Symbol keys can only be accessed by a direct property read/write when holding a reference to the hidden Symbol.
     * <p>
     * Symbols of all kinds are represented internally using invalid UTF-8 byte sequences, see symbols.rst for the current formats in use. Application hidden Symbols begin with a 0xFF byte prefix and are followed by an arbitrary, application selected string. When C code pushes a string using e.g. duk_push_string() and the byte sequence matches an internal Symbol format, the string value is automatically interpreted as a Symbol.
     * <p>
     * Duktape also uses hidden Symbols for various implementation specific purposes, such as storing an object's finalizer reference. As of Duktape 2.2 a different byte prefix is used for Duktape's hidden Symbols, so the 0xFF prefix is now reserved entirely for application use. Application code should never try to access Duktape's hidden Symbol keyed properties: the set of such properties can change arbitrarily between versions.
     * <p>
     * Note that the internal UTF-8 byte sequences cannot be created from Ecmascript code as a valid Ecmascript string. For example, a hidden Symbol might be represented using \xFFxyz, i.e. the byte sequence ff 78 79 7a, while the Ecmascript string "\u00ffxyz" would be represented as the CESU-8 bytes c3 bf 78 79 7a in memory.
     * Creating a Symbol is straightforward from C code:
     * <p>
     * duk_push_string(ctx, DUK_HIDDEN_SYMBOL("mySymbol"));
     *
     * @param context
     * @param symbol
     */
    public static native void pushSymbol(long context, String symbol);

    /**
     * . . .  → . . . obj
     * Summary §
     * Push an empty object to the stack. Returns non-negative index (relative to stack bottom) of the pushed object.
     * <p>
     * The internal prototype of the created object is Object.prototype. Use duk_set_prototype() to change it.
     *
     * @return
     */
    public static native int pushObject(long context);

    /**
     * . . . obj . . . key val  → . . . obj . . .
     * Summary §
     * Write val to the property key of a value at obj_idx. key and val are removed from the stack. Return code and error throwing behavior:
     * <p>
     * If the property write succeeds, returns 1.
     * If the property write fails, throws an error (strict mode semantics). An error may also be thrown by the "setter" function of an accessor property.
     * If the value at obj_idx is not object coercible, throws an error.
     * If obj_idx is invalid, throws an error.
     * The property write is equivalent to the Ecmascript expression obj[key] = val. The exact rules of when a property write succeeds or fails are the same as for Ecmascript code making the equivalent assignment. For precise semantics, see Property Accessors, PutValue (V, W), and [[Put]] (P, V, Throw). Both the target value and the key are coerced:
     * <p>
     * The target value is automatically coerced to an object. However, the object is transitory so writing its properties is not very useful. Moreover, Ecmascript semantics prevent new properties from being created for such transitory objects (see PutValue (V, W), step 7 of the special [[Put]] variant).
     * The key argument is internally coerced using ToPropertyKey() coercion which results in a string or a Symbol. There is an internal fast path for arrays and numeric indices which avoids an explicit string coercion, so use a numeric key when applicable.
     * If the target is a Proxy object which implements the set trap, the trap is invoked and the API call return value matches the trap return value.
     * <p>
     * In Ecmascript an assignment expression has the value of the right-hand-side expression, regardless of whether or not the assignment succeeds. The return value for this API call is not specified by Ecmascript or available to Ecmascript code: the API call returns 0 or 1 depending on whether the assignment succeeded or not (with the 0 return value promoted to an error in strict code).
     * If the key is a fixed string you can avoid one API call and use the duk_put_prop_string() variant. Similarly, if the key is an array index, you can use the duk_put_prop_index() variant.
     * <p>
     * Although the base value for property accesses is usually an object, it can technically be an arbitrary value. Plain string and buffer values have virtual index properties so you can access "foo"[2], for instance. Most primitive values also inherit from some prototype object so that you can e.g. call methods on them: (12345).toString(16).
     *
     * @param context
     * @param objectIndex
     * @return
     */
    public static native boolean putProp(long context, int objectIndex);

    /**
     * . . . val  → . . .
     * Summary §
     * Put property named key to the global object. Return value behaves similarly to duk_put_prop(). This is a convenience function which does the equivalent of:
     * <p>
     * duk_bool_t ret;
     * <p>
     * duk_push_global_object(ctx);
     * duk_insert(ctx, -2);
     * ret = duk_put_prop_string(ctx, -2, key);
     * duk_pop(ctx);
     *
     * @param context
     * @param name
     * @return
     */
    public static native boolean putGlobalString(long context, String name);

    /**
     * . . . obj . . . key  → . . . obj . . . val  (if key exists)
     * . . . obj . . . key  → . . . obj . . . undefined  (if key doesn't exist)
     * Summary §
     * Get the property key of a value at obj_idx. Return code and error throwing behavior:
     * <p>
     * If the property exists, 1 is returned and key is replaced by the property value on the value stack. However, if the property is an accessor, the "getter" function may throw an error.
     * If the property does not exist, 0 is returned and key is replaced by undefined on the value stack.
     * If the value at obj_idx is not object coercible, throws an error.
     * If obj_idx is invalid, throws an error.
     * The property read is equivalent to the Ecmascript expression res = obj[key] with the exception that the presence or absence of the property is indicated by the call return value. For precise semantics, see Property Accessors, GetValue (V), and [[Get]] (P). Both the target value and the key are coerced:
     * <p>
     * The target value is automatically coerced to an object. For instance, a string is converted to a String and you can access its "length" property.
     * The key argument is internally coerced using ToPropertyKey() coercion which results in a string or a Symbol. There is an internal fast path for arrays and numeric indices which avoids an explicit string coercion, so use a numeric key when applicable.
     * If the target is a Proxy object which implements the get trap, the trap is invoked and the API call always returns 1 (i.e. property present): the absence/presence of properties is not indicated by the get Proxy trap. Thus, the API call return value may be of limited use if the target object is potentially a Proxy.
     * <p>
     * If the key is a fixed string you can avoid one API call and use the duk_get_prop_string() variant. Similarly, if the key is an array index, you can use the duk_get_prop_index() variant.
     * <p>
     * Although the base value for property accesses is usually an object, it can technically be an arbitrary value. Plain string and buffer values have virtual index properties so you can access "foo"[2], for instance. Most primitive values also inherit from some prototype object so that you can e.g. call methods on them: (12345).toString(16).
     *
     * @return
     */
    public static native boolean getProp(long context, int objectIndex);

    /**
     * . . . val . . .  → . . . val . . . proto
     * Summary §
     * Get the internal prototype of the value at idx. If the value is not an object, an error is thrown. If the object has no prototype (which is possible for "naked objects") undefined is pushed to the stack instead.
     *
     * @param context
     * @param objectIndex
     */
    public static native void getPrototype(long context, int objectIndex);


    /**
     * . . . val . . .
     * Summary §
     * Returns type of value at idx. The return value is one of DUK_TYPE_xxx or DUK_TYPE_NONE if idx is invalid.
     * <p>
     * Symbol values are visible in the C API as strings so that both duk_is_symbol() and duk_is_string() are true. This behavior is similar to Duktape 1.x internal strings. Symbols are still an experimental feature; for now, you can distinguish Symbols from ordinary strings using duk_is_symbol(). For the internal representation, see symbols.rst.
     * <p>
     * DUK_TYPE_MIN                      0U
     * DUK_TYPE_NONE                     0U
     * DUK_TYPE_UNDEFINED                1U
     * DUK_TYPE_NULL                     2U
     * DUK_TYPE_BOOLEAN                  3U
     * DUK_TYPE_NUMBER                   4U
     * DUK_TYPE_STRING                   5U
     * DUK_TYPE_OBJECT                   6U
     * DUK_TYPE_BUFFER                   7U
     * DUK_TYPE_POINTER                  8U
     * DUK_TYPE_LIGHTFUNC                9U
     * DUK_TYPE_MAX                      9U
     *
     * @param context
     * @return
     */
    public static native int getType(long context, int objectIndex);

    /**
     * Returns type mask of value at idx. The return value is one of DUK_TYPE_MASK_xxx or DUK_TYPE_MASK_NONE if idx is invalid.
     * <p>
     * Type masks allow e.g. for a convenient comparison for multiple types at once (the duk_check_type_mask() call is even more convenient for this purpose).
     * <p>
     * Symbol values are visible in the C API as strings so that both duk_is_symbol() and duk_is_string() are true. This behavior is similar to Duktape 1.x internal strings. Symbols are still an experimental feature; for now, you can distinguish Symbols from ordinary strings using duk_is_symbol(). For the internal representation, see symbols.rst.
     * Example §
     * if (duk_get_type_mask(ctx, -3) & (DUK_TYPE_MASK_STRING |
     * DUK_TYPE_MASK_NUMBER)) {
     * printf("value is a string or a number\n");
     * }
     *
     * @param context
     * @return
     */
    public static native int getTypeMask(long context);

    public static int DUK_TYPE_MIN = 0;
    public static int DUK_TYPE_NONE = 0;
    public static int DUK_TYPE_UNDEFINED = 1;
    public static int DUK_TYPE_NULL = 2;
    public static int DUK_TYPE_BOOLEAN = 3;
    public static int DUK_TYPE_NUMBER = 4;
    public static int DUK_TYPE_STRING = 5;
    public static int DUK_TYPE_OBJECT = 6;
    public static int DUK_TYPE_BUFFER = 7;
    public static int DUK_TYPE_POINTER = 8;
    public static int DUK_TYPE_LIGHTFUNC = 9;
    public static int DUK_TYPE_MAX = 9;
    public static int DUK_TYPE_MASK_NONE = (1 << DUK_TYPE_NONE);
    public static int DUK_TYPE_MASK_UNDEFINED = (1 << DUK_TYPE_UNDEFINED);
    public static int DUK_TYPE_MASK_NULL = (1 << DUK_TYPE_NULL);
    public static int DUK_TYPE_MASK_BOOLEAN = (1 << DUK_TYPE_BOOLEAN);
    public static int DUK_TYPE_MASK_NUMBER = (1 << DUK_TYPE_NUMBER);
    public static int DUK_TYPE_MASK_STRING = (1 << DUK_TYPE_STRING);
    public static int DUK_TYPE_MASK_OBJECT = (1 << DUK_TYPE_OBJECT);
    public static int DUK_TYPE_MASK_BUFFER = (1 << DUK_TYPE_BUFFER);
    public static int DUK_TYPE_MASK_POINTER = (1 << DUK_TYPE_POINTER);
    public static int DUK_TYPE_MASK_LIGHTFUNC = (1 << DUK_TYPE_LIGHTFUNC);
    public static int DUK_TYPE_MASK_THROW = (1 << 10);
    public static int DUK_TYPE_MASK_PROMOTE = (1 << 11);

    /**
     * . . . val  → . . .
     * Summary §
     * Pop one element off the stack. If the stack is empty, throws an error.
     * <p>
     * To pop multiple elements, use duk_pop_n() or the shortcuts for common cases: duk_pop_2() and duk_pop_3().
     */
    public static native void pop(long context);

    /**
     * . . . val1 . . . valN  → . . .
     * Summary §
     * Pop count elements off the stack. If the stack has fewer than count elements, throws an error. If count is zero, the call is a no-op. Negative counts cause an error to be thrown.
     *
     * @param context
     * @param count
     */
    public static native void popN(long context, int count);

    /**
     * (No effect on value stack.)
     * <p>
     * Summary §
     * Get the absolute index (>= 0) of the topmost value on the stack. If the stack is empty, returns DUK_INVALID_INDEX.
     *
     * @return
     */
    public static native int getTopIndex(long context);

    /**
     * (No effect on value stack.)
     * top=top_index+1
     * Summary §
     * Get current stack top (>= 0), indicating the number of values currently on the value stack (of the current activation).
     *
     * @return
     */
    public static native int getTop(long context);

    /**
     * this is a key function that javascript want to call java api.
     * in js, whether the code try to call to jvm, they will call the corresponding c function
     * which has wrap this function.
     * The general way to call is is __java__handle(id, arg1, arg2, ..)
     * the id indicates that how jvm should interprete this call
     * proxy get, set
     * release a object referece
     * call a method associated with a object (either static or object method)
     */
    public static void __java_handle__(long context) {
        int id = JsNative.getInt(context, 0);
        if (id == PROXYGETHANDLE) {
            __handle_proxy_get_call__(context);
        } else if (id == JSOBJECTFINALIZERHANDLE) {
            __handle_object_finalization_call__(context);
        } else if (id == FUNCTIONCALL) {
            __handle_function_call__(context);
        } else if (id == PROXYSETHANDLE) {
            __handle_proxy_set_call__(context);
        } else {
            if (JsNative.ISDEBUG) {
                GlobalState.printToLog(String.format("Unknown code %d in %s", id, JAVAHANLEFUNCNAME),
                        GlobalState.error);
                printCurrentStack(context);
            }
        }

    }


    private static void printCurrentStack(long context) {
        pushContextDump(context);
        GlobalState.printToLog("stack:" + getString(context, -1) + "\n", GlobalState.info);
        pop(context);
    }

    /**
     * id, obj, name, arg1 (3) arg2 (4), .. (notice only obj are bare js object, arg1, arg2, are either primitive type or proxy
     * use __id__ to retieve the object.
     *
     * @param context
     */
    private static void __handle_function_call__(long context) {
        Object obj = getObject(context, 1);
        if (obj != null) {
            String key = getString(context, 2);
            if (JsNative.ISDEBUG)
                GlobalState.printToLog(String.format("CALL [obj:%s].%s\n", obj.getClass().getSimpleName().toString(), key),
                        GlobalState.infoDebug);
            /* three additional parameters, id, obj, name*/
            int size = getTop(context) - 3;
            Object[] paras = new Object[size];
            for (int i = 0; i < size; i++) {
                paras[i] = getJavaObject(context, 3 + i);
            }
            Object result = JsReflection.call(obj, key, paras);
            if (result == null) {
                pushNull(context);
            } else {
                pushJavaObject(context, result);
            }

        } else {
            if (JsNative.ISDEBUG) {
                GlobalState.printToLog("call: id, obj, key, arg1,.., obj is not valid!\n",
                        GlobalState.infoDebug);
                printCurrentStack(context);
            }
        }
    }

    public static int CALLSUCCESS = 0;
    /**
     * indicates that the handle has not yet push any result on stack
     */
    public static int CALLFAILED = 1;

    /**
     * id obj name val (3)
     */

    private static void __handle_proxy_set_call__(long context) {
        Object obj = getObject(context, 1);
        if (obj != null) {
            String key = getString(context, 2);
            Object val = getJavaObject(context, 3);
            if (JsNative.ISDEBUG)
                GlobalState.printToLog(String.format("SET [obj:%s].%s\n", obj.getClass().getSimpleName().toString(), key),
                        GlobalState.infoDebug);
            JsReflection.setField(obj, key, val);
        }
    }

    /**
     * id obj name arg1 arg2 ..
     *
     * @param context
     * @return
     */
    private static int __handle_proxy_get_call__(long context) {
        Object obj = getObject(context, 1);
        if (obj != null) {
            String key = getString(context, 2);
            if (JsNative.ISDEBUG)
                GlobalState.printToLog(String.format("GET [obj:%s].%s\n", obj.getClass().getSimpleName().toString(), key),
                        GlobalState.infoDebug);
            Object result = null;
            /*first try field, now we reserver new for type constructor*/
            if (!key.equals(JsReflection.CONSTRUCTORTAG))
                result = JsReflection.getField(obj, key);
            if (result == null) {
                if (JsNative.ISDEBUG) {
                    GlobalState.printToLog(String.format("PUSH [obj:%s].%s as method\n", obj.getClass().getSimpleName().toString(),
                            key), GlobalState.infoDebug);
                }
                /* push it as function*/
                pushObjectMethod(context);
            } else {
                if (JsNative.ISDEBUG) {
                    GlobalState.printToLog(String.format("PUSH [obj:%s].%s as field\n", obj.getClass().getSimpleName().toString(),
                            key), GlobalState.infoDebug);
                }
                pushJavaObject(context, result);
            }

        } else {
            GlobalState.printToLog("get property from non java object" + "\n",
                    GlobalState.error);
            return CALLFAILED;
        }
        return CALLSUCCESS;
    }

    private static int __handle_object_finalization_call__(long context) {
        Object obj = getObject(context, 1);
        if (obj != null) {
            delObject(context, 1);
            if (JsNative.ISDEBUG)
                GlobalState.printToLog(String.format("release [obj:%s]\n", obj.getClass().getSimpleName().toString()),
                        GlobalState.infoDebug);
        } else {
            GlobalState.printToLog("release non java object" + "\n",
                    GlobalState.error);
        }
        return CALLSUCCESS;
    }

    /**
     * push a C function that will call __java_handle__ (defined as static method in this class)
     */
    public static native int pushJavaHandle(long context);

    /**
     * . . . func arg1 . . . argN  → . . . retval
     * Summary §
     * Call target function func with nargs arguments (not counting the function itself). The function and its arguments are replaced by a single return value. An error thrown during the function call is not automatically caught.
     * <p>
     * The target function this binding is initially set to undefined. If the target function is not strict, the binding is replaced by the global object before the function is invoked; see Entering Function Code. If you want to control the this binding, you can use duk_call_method() or duk_call_prop() instead.
     * <p>
     * This API call is equivalent to:
     * <p>
     * var retval = func(arg1, ..., argN);
     * or:
     * <p>
     * var retval = func.call(undefined, arg1, ..., argN);
     *
     * @param context
     */
    public static native void call(long context, int nArgs);

    /**
     * . . . source filename  → . . . function
     * Compile Ecmascript source code and replace it with a compiled function object (the code is not executed). The filename argument is stored as the fileName property of the resulting function, and is the name used in e.g. tracebacks to identify the function. May throw a SyntaxError for any compile-time errors (in addition to the usual internal errors like out-of-memory, internal limit errors, etc).
     * <p>
     * The following flags may be given:
     * <p>
     * DUK_COMPILE_EVAL	Compile the input as eval code instead of as an Ecmascript program
     * DUK_COMPILE_FUNCTION	Compile the input as a function instead of as an Ecmascript program
     * DUK_COMPILE_STRICT	Force the input to be compiled in strict mode
     * DUK_COMPILE_SHEBANG	Allow non-standard shebang comment (#! ...) on first line of the input
     * The source code being compiled may be:
     * <p>
     * Global code: compiles into a function with zero arguments, which executes like a top level Ecmascript program (default)
     * Eval code: compiles into a function with zero arguments, which executes like an Ecmascript eval call (flag DUK_COMPILE_EVAL)
     * Function code: compiles into a function with zero or more arguments (flag DUK_COMPILE_FUNCTION)
     * All of these have slightly different semantics in Ecmascript. See Establishing an Execution Context for a detailed discussion. One major difference is that global and eval contexts have an implicit return value: the last non-empty statement value is an automatic return value for the program or eval code, whereas functions don't have an automatic return value.
     * <p>
     * Global and eval code don't have an explicit function syntax. For instance, the following can be compiled both as a global and as an eval expression:
     * <p>
     * All of these have slightly different semantics in Ecmascript. See Establishing an Execution Context for a detailed discussion. One major difference is that global and eval contexts have an implicit return value: the last non-empty statement value is an automatic return value for the program or eval code, whereas functions don't have an automatic return value.
     * <p>
     * Global and eval code don't have an explicit function syntax. For instance, the following can be compiled both as a global and as an eval expression:
     * <p>
     * print("Hello world!");
     * 123;  // implicit return value
     * Function code follows the Ecmascript function syntax (the function name is optional):
     * <p>
     * function adder(x,y) {
     * return x+y;
     * }
     * Compiling a function is equivalent to compiling eval code which contains a function expression. Note that the outermost parentheses are required, otherwise the eval code will register a global function named "adder" instead of returning a plain function value:
     * <p>
     * (function adder(x,y) {
     * return x+y;
     * })
     * The bytecode generated for global and eval code is currently slower than that generated for functions: a "slow path" is used for all variable accesses in program and eval code, and the implicit return value handling of program and eval code generates some unnecessary bytecode. From a performance point of view (both memory and execution performance) it is thus preferable to have as much code inside functions as possible.
     * <p>
     * When compiling eval and global expressions, be careful to avoid the usual Ecmascript gotchas, such as:
     *
     * @param context
     * @param flags   DUK_COMPILE_EVAL                  (1U << 3)
     *                DUK_COMPILE_FUNCTION              (1U << 4)
     *                DUK_COMPILE_STRICT                (1U << 5)
     *                DUK_COMPILE_SHEBANG               (1U << 6)
     */

    public static native void compile(long context, int flags);

    public static final int DUK_COMPILE_EVAL = (1 << 3);
    public static final int DUK_COMPILE_FUNCTION = (1 << 4);
    public static final int DUK_COMPILE_STRICT = (1 << 5);
    public static final int DUK_COMPILE_SHEBANG = (1 << 6);

    /**
     * . . . source filename  → . . . function  (if success, return value == 0)
     * . . . source filename  → . . . err  (if failure, return value != 0)
     * Summary §
     * Like duk_compile() but catches errors related to compilation (such as syntax errors in the source). A zero return value indicates success and the compiled function is left on the stack top. A non-zero return value indicates an error, and the error is left on the stack top.
     * <p>
     * If value stack top is too low (smaller than 2), an error is thrown.
     *
     * @param context
     * @return
     */
    public static native int pCompile(long context, int flags);

    /**
     * . . .  → . . . thr
     Summary §
     Push a new Duktape thread (context, coroutine) to the stack. Returns non-negative index (relative to stack bottom) of the pushed thread. The new thread will be associated with the same Duktape heap as the argument ctx, and will share the same global object environment.

     To interact with the new thread with the Duktape API, use duk_get_context() to get a context pointer for API calls.

     Example §
     duk_idx_t thr_idx;
     duk_context *new_ctx;

     thr_idx = duk_push_thread(ctx);
     new_ctx = duk_get_context(ctx, thr_idx);
     * @param context
     * @return
     */
    public static native int pushThread(long context);
    /**
     * . . .  → . . . obj
     * Summary §
     * Similar to duk_push_object() but the pushed object doesn't inherit from any other object, i.e. its internal prototype is null. This call is equivalent to Object.create(null). Returns non-negative index (relative to stack bottom) of the pushed object.
     * <p>
     * Example §
     * duk_idx_t obj_idx;
     * <p>
     * obj_idx = duk_push_bare_object(ctx);
     *
     * @param context
     * @return
     */
    public static native int pushBareObject(long context);

    /**
     * . . .  → . . . true  (if val != 0)
     * . . .  → . . . false  (if val == 0)
     * Summary §
     * Push true (if val != 0) or false (if val == 0) to the stack.
     *
     * @param context
     * @param b
     */
    public static native void pushBoolean(long context, boolean b);

    public static final String JSOBJECTPTR = "ptr";
    public static boolean ISDEBUG = false;

    /**
     * . . . -> . . . obj
     * push a new bare object in state, and assign its symbol property to a pointer of obj
     * We do not add proxy to it yet
     * return the index of new created bare Object. We set a finalizer to remove the reference after we finish it
     * Notice this pushObject is used to get a minimal js object holding a pointer to java obj
     * use pushProxyObject to equip it with get and set properties
     * ( not same as duk pushObject)
     *
     * @param context
     */
    public static int pushObject(long context, Object obj) {
        getGlobalString(context, JSOBJECTFINALIZER);
        int index = pushBareObject(context);
        pushSymbol(context, JSOBJECTPTR);
        pushPointer(context, obj);
        putProp(context, index);
        call(context, 1);
        if (ISDEBUG) {
            GlobalState.printToLog(String.format("Create [obj:%s]\n", obj.getClass().getSimpleName()),
                    GlobalState.infoDebug);
        }
        return getTopIndex(context);
    }

    /**
     * . . . -> . . .
     * push a bare object, apply proxy handle to it, and put it as a global property
     * This is recommended way to wrap a java object to js
     *
     * @param context
     * @param obj
     * @param name
     * @return
     */
    public static int pushObject(long context, Object obj, String name) {
        int index = pushObject(context, obj);
        pushProxyObject(context);
        putGlobalString(context, name);
        return index;
    }

    /**
     * . . .bare_object name -> . . . {function}
     * wrap a bare_object and name to a function so js can call it further
     *
     * @param context
     * @return
     */
    public static void pushObjectMethod(long context) {
        getGlobalString(context, FUNCTIONHANDLE);
        insert(context, -3);
        //printCurrentStack(context);
        call(context, 2);
    }

    /**
     * this provides a overall way to push a java object to stack
     * int, double, boolean will be keep in primitive form. (Integer, ..) boxed form will be unboxed
     * string will be push as c-string
     * while other things just as wrapped js object (i.e, include proxy)
     *
     * @param context
     * @param obj
     */
    public static void pushJavaObject(long context, Object obj) {
        if (obj instanceof Integer) {
            pushInt(context, (Integer) obj);
        } else if (obj instanceof Double) {
            pushNumber(context, (Double) obj);
        } else if (obj instanceof String) {
            pushString(context, (String) obj);
        } else if (obj instanceof Boolean) {
            pushBoolean(context, (Boolean) obj);
        } else if (obj == null) {
            pushNull(context);
        } else {
            pushObject(context, obj);
            pushProxyObject(context);
        }

    }

    /**
     * . . . bare_jsObj -> . . . proxy_jsObj
     *
     * @param context
     * @return
     */
    public static int pushProxyObject(long context) {
        pushProxyHandle(context);
        return pushProxy(context);
    }

    /**
     * . . . -> . . . handle
     * push a object with generic handle to wrap a bare jsObject
     *
     * @param context
     * @return
     */
    public static int pushProxyHandle(long context) {
        int handleIndex = pushObject(context);
        pushString(context, "get");
        getGlobalString(context, PROXYGET);
        putProp(context, handleIndex);
        pushString(context, "set");
        getGlobalString(context, PROXYSET);
        putProp(context, handleIndex);
        return handleIndex;
    }

    public static final String JAVAHANLEFUNCNAME = "__java_handle__";
    public static final String PROXYGET = "__java_proxy_get__";
    public static final String PROXYSET = "__java_proxy_set__";
    public static final String FUNCTIONHANDLE = "__function_handle__";
    public static final String GETJSOBJECTPROPERTIES = "__get_jsobject_properties__";

    /*a key to retrieve the bare object from proxy*/
    public static final String PROXYGETBAREOBJECTKEY = "__id__";
    public static final String JSOBJECTFINALIZER = "__js_object_finalizer__";

    /**
     * register the handle function of proxy get.
     * function(obj,key,recv) , notice obj is the bare object (being proxied)
     *
     * @param context
     */
    public static void registerProxyHandleGet(long context) {
        pushString(context, String.format(
                "function(obj,key,recv){if(key===\"%s\"){return obj;};return %s(%d,obj,key);}",
                PROXYGETBAREOBJECTKEY
                , JAVAHANLEFUNCNAME, PROXYGETHANDLE));
        pushString(context, "proxyGet");
        pCompile(context, DUK_COMPILE_FUNCTION);
        putGlobalString(context, PROXYGET);

    }

    public static void registerProxyHandleSet(long context) {
        pushString(context, String.format(
                "function(obj,key,val,recv){if(key===\"%s\"){return true;};%s(%d,obj,key,val);return true;}",
                PROXYGETBAREOBJECTKEY
                , JAVAHANLEFUNCNAME, PROXYSETHANDLE));
        pushString(context, "proxySet");
        pCompile(context, DUK_COMPILE_FUNCTION);
        putGlobalString(context, PROXYSET);

    }

    /**
     * a function that takes a bare object and string, return a callable object
     *
     * @param context
     */
    public static void registerFunctionHandle(long context) {
        pushString(context, String.format(
                "function(obj,name)" +
                        "{return function(){" +
                        "return %s.apply(null,[%d,obj,name].concat(Array.prototype.slice.call(arguments)));" +
                        "};}"
                , JAVAHANLEFUNCNAME, FUNCTIONCALL));
        pushString(context, "functionHandle");
        pCompile(context, DUK_COMPILE_FUNCTION);
        putGlobalString(context, FUNCTIONHANDLE);

    }

    /**
     * a function that return the js object properties
     *
     * @param context
     */
    public static void registerJsObejctProperties(long context) {
        pushString(context, "function(x){return Object.getOwnPropertyNames(x)}");
        pushString(context, "jsObjectProperties");
        pCompile(context, DUK_COMPILE_FUNCTION);
        putGlobalString(context, GETJSOBJECTPROPERTIES);
    }

    public static void registerJsObjectFinalizer(long context) {
        pushString(context, String.format(
                "function(obj){Duktape.fin(obj,function(x){%s(%d,x)});return obj;}"
                , JAVAHANLEFUNCNAME, JSOBJECTFINALIZERHANDLE));
        pushString(context, "finalizer");
        pCompile(context, DUK_COMPILE_FUNCTION);
        putGlobalString(context, JSOBJECTFINALIZER);
    }

    public static final int PROXYGETHANDLE = 0;
    public static final int JSOBJECTFINALIZERHANDLE = 1;
    public static final int FUNCTIONCALL = 2;
    public static final int PROXYSETHANDLE = 3;

    /**
     * register the key function that js will invoke whenever it want to communicate with java
     * args  int , obj1, obj2, ..
     * the first argument indicate the type of java call
     * public static final int PROXYGETHANDLE=0;
     * public static final int JSOBJECTFINALIZERHANDLE=1;
     *
     * @param context
     */
    public static void registerJavaHandle(long context) {
        pushJavaHandle(context);
        putGlobalString(context, JAVAHANLEFUNCNAME);
    }

    /**
     * bare_obj .. -> bare_obj .. (no effect)
     * return the pointer (jobject) hold by  bare Js object
     *
     * @param context
     * @param jsObjectIndex
     * @return
     */

    public static Object getObject(long context, int jsObjectIndex) {
        /* this is a subtle bug here, where jsObjectIndex is negative index, pushSymbol will change it
         * when jsObjectIndex is negative, pushSymbol will change it effectively.
         * */
        //pushSymbol(context, JSOBJECTPTR);
        //getProp(context, jsObjectIndex);
        getPropSymbol(context, jsObjectIndex, JSOBJECTPTR);
        Object obj = getPointer(context, -1);
        pop(context);
        return obj;
    }

    /**
     * proxy_obj ... -> proxy_obj ...(no effect)
     * return the pointer (jobject) hold by proxy js object
     *
     * @param context
     * @return
     */
    public static Object getProxyObject(long context, int index) {
        //printCurrentStack(context);
        getPropString(context, index, PROXYGETBAREOBJECTKEY);
        //printCurrentStack(context);
        Object result = getObject(context, -1);
        //printCurrentStack(context);
        pop(context);
        //printCurrentStack(context);
        return result;
    }

    /**
     * .. .. val . -> . . val . (no effect)
     * this provides a overall way to get a java object from stack
     * int, double, boolean will be keep in primitive form. (Integer, ..) will be unboxed
     * string will be push as c-string
     * while other things is assumed to be proxy object!!
     *
     * @param context
     * @param index
     * @return
     */
    public static Object getJavaObject(long context, int index) {
        int type = getType(context, index);
        if (type == DUK_TYPE_OBJECT) {
            return getProxyObject(context, index);
        } else if (type == DUK_TYPE_NUMBER) {
            double a = getNumber(context, index);
            if ((a == Math.floor(a)) && !Double.isInfinite(a)) {
                return (int) Math.floor(a);
            } else return a;
        } else if (type == DUK_TYPE_STRING) {
            return getString(context, index);
        } else if (type == DUK_TYPE_BOOLEAN) {
            return getBoolean(context, index);
        }
        return null;
    }
    /**
     (No effect on value stack.)

     Summary §
     Force a mark-and-sweep garbage collection round. If mark-and-sweep is disabled in the Duktape build, the call is a no-op.

     The following flags are defined:

     Define	Description
     DUK_GC_COMPACT	Force object property table compaction
     You may want to call this function twice to ensure even objects with finalizers are collected. Currently it takes two mark-and-sweep rounds to collect such objects. First round marks the object as finalizable and runs the finalizer. Second round ensures the object is still unreachable after finalization and then frees the object.
     */
    public static native void gc(long context, int flags);
    /**
     * . . . val . . .
     * Summary §
     * Get the boolean value at idx without modifying or coercing the value. Returns 1 if the value is true, 0 if the value is false, not a boolean, or the index is invalid.
     * <p>
     * Note that the value is not coerced, so even a "truthy" Ecmascript value (like a non-empty string) will be treated as false. If you want to coerce the value, use duk_to_boolean().
     * <p>
     * Example §
     * if (duk_get_boolean(ctx, -3)) {
     * printf("value is true\n");
     * }
     *
     * @param context
     * @param index
     * @return
     */
    public static native boolean getBoolean(long context, int index);

    /**
     * duk_context *duk_get_context(duk_context *ctx, duk_idx_t idx);
     Stack §
     . . . val . . .
     Summary §
     Get a context pointer for a Duktape thread at idx. If the value at idx is not a Duktape thread or the index is invalid, returns NULL.

     The returned context pointer is only valid while the Duktape thread is reachable from a garbage collection point of view.

     If you prefer an error to be thrown for an invalid value or index, use duk_require_context()
     * @param context
     * @param index
     * @return
     */
    public static native long getContext(long context, int index);

    /**
     * . . . obj . . .  → . . . obj . . . val  (if key exists)
     * . . . obj . . .  → . . . obj . . . undefined  (if key doesn't exist)
     * Summary §
     * Like duk_get_prop(), but the property name is given as a NUL-terminated C string key.
     *
     * @param context
     * @param indexObject
     * @param key
     * @return
     */
    public static native boolean getPropString(long context, int indexObject, String key);

    /**
     * * . . . obj . . .  → . . . obj . . . val  (if key exists)
     * . . . obj . . .  → . . . obj . . . undefined  (if key doesn't exist)
     * Summary §
     * Like duk_get_prop(), but the property name is given as string which will be internaled as symbol
     *
     * @param context
     * @param indexObject
     * @param key
     * @return
     */
    public static native boolean getPropSymbol(long context, int indexObject, String key);

    /**
     * obj .. -> obj .. delete obj.PTR reference
     * delete the refence of PRT hold by bare js object
     *
     * @param context
     * @param jsObjectIndex
     */
    public static void delObject(long context, int jsObjectIndex) {
        pushSymbol(context, JSOBJECTPTR);
        getProp(context, jsObjectIndex);
        delPointer(context, -1);
        pop(context);
    }

    /**
     * . . . val . . .  → . . . ToString(val) . . .
     * Summary §
     * Like duk_to_string() but if the initial string coercion fails, the error value is coerced to a string. If that also fails, a fixed error string is returned.
     * <p>
     * The caller can safely use this function to coerce a value to a string, which is useful in C code to print out a return value safely with printf(). The only uncaught errors possible are out-of-memory and other internal errors which trigger fatal error handling anyway.
     *
     * @param context
     * @return
     */
    public static native String safeToString(long context, int index);

    public static int DUK_EXEC_SUCCESS = 0;
    public static int DUK_EXEC_ERROR = 1;

    /**
     * . . . code -> . . . result (or error)
     * protected evaluation
     *
     * @param context
     */
    public static native int safeEval(long context);

    /**
     * . . . -> . . . result (or error)
     *
     * @param context
     * @param content
     */
    public static native int safeEvalString(long context, String content);

    /**
     * . . . old(to_idx) . . . val  → . . . val(to_idx) old . . .
     * Summary §
     * Insert a value at to_idx with a value popped from the stack top. The previous value at to_idx and any values above it are moved up the stack by a step. If to_idx is an invalid index, throws an error.
     * <p>
     * Negative indices are evaluated prior to popping the value at the stack top. This is also illustrated by the example.
     *
     * @param context
     * @param index
     */
    public static native void insert(long context, int index);

    /**
     * must be called if previous has assciated a pushJavaHandle
     *
     * @param context
     */
    public static native void releaseJavaHandle(long context);

    /**
     * duk_bool_t duk_is_error(duk_context *ctx, duk_idx_t idx);
     * Stack §
     * . . . val . . .
     * Summary §
     * Returns 1 if value at idx inherits from Error, otherwise returns 0. If idx is invalid, also returns 0.
     * <p>
     * Example §
     *
     * @param context
     * @param index
     * @return
     */

    public static native boolean isError(long context, int index);

    /*
    . . . -> (emtpy)
    empty the current stack
     */
    public static void clearStack(long context) {
        popN(context, getTop(context));
    }
}

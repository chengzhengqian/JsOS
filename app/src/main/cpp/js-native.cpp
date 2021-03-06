#include "duktape.h"
#include <jni.h>
#include <string>
#include <map>
/**
Utility functions for JNI

**/
// JavaVM *jvm;
std::map<duk_context*, JNIEnv*> envs;
std::map<duk_context*, jclass> clsMap ;
std::map<duk_context*, jmethodID> jhdMap;

jstring newString(JNIEnv *env, const char *c){
  return env->NewStringUTF(c);
}

void deleteLocalRef(JNIEnv *env, jobject obj){
    env->DeleteLocalRef(obj);
}

void deleteGlobalRef(JNIEnv *env, jobject obj){
  env->DeleteGlobalRef(obj);
}


JNIEnv* getCurrentEnv(duk_context *ctx){
  /* one should cache jvm instead of env. but as ctx corresponding a same thread
   */
  // JNIEnv* env;
  // jvm->AttachCurrentThread(&env, NULL);
  return envs[ctx];
}

void releaseCurrentEnv(duk_context *ctx){
  deleteGlobalRef(envs[ctx],clsMap[ctx]);
  // envs.erase(ctx);
  // clsMap.erase(ctx);
  // jhdMap.erase(ctx);
}

const char *getString(JNIEnv *env, jstring s) {
  return env->GetStringUTFChars(s, 0);
}
void releaseString(JNIEnv *env, jstring s, const char *c) {
  env->ReleaseStringUTFChars(s, c);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_createHeapDefault(
								    JNIEnv *env, jobject /* this */) {
  return (jlong)duk_create_heap_default();
}

extern "C" JNIEXPORT void JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_evalString(JNIEnv *env,
                                                             jobject /* this */,
                                                             long ctx_,
                                                             jstring jstr) {
  duk_context *ctx = (duk_context *)ctx_;
  const char *cstr = getString(env, jstr);
  duk_eval_string((duk_context *)ctx, cstr);
  releaseString(env, jstr, cstr);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_getInt(JNIEnv *env,
                                                         jobject /* this */,
                                                         long ctx_, int idx) {
  duk_context *ctx = (duk_context *)ctx_;
  return duk_get_int(ctx, idx);
}
extern "C" JNIEXPORT jdouble JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_getNumber(JNIEnv* env, jobject /* this */, long ctx_, int idx) {
  duk_context * ctx=(duk_context*)ctx_;
  return duk_get_number(ctx, idx);
}






extern "C" JNIEXPORT void JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_destroyHeap(JNIEnv* env, jobject /* this */, long ctx_) {
  duk_context * ctx=(duk_context*)ctx_;
  duk_destroy_heap(ctx);
}


extern "C" JNIEXPORT jstring JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_getString(JNIEnv* env, jobject /* this */, long ctx_, int idx) {
  duk_context * ctx=(duk_context*)ctx_;
  const char * cstr= duk_get_string(ctx, idx);
  jstring jstr=newString(env, cstr);
  //  we don't need call deleteLocalRef(env,jstr), as we will pass it to JVM,  otherwise JVM gc the string.
  return jstr;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_safeToString(JNIEnv* env, jobject /* this */, long ctx_, int idx) {
  duk_context * ctx=(duk_context*)ctx_;
  const char * cstr= duk_safe_to_string(ctx, idx);
  jstring jstr=newString(env, cstr);
  return jstr;
}

extern "C" JNIEXPORT void JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_gc(JNIEnv* env, jobject /* this */, long ctx_, int flags) {
  duk_context * ctx=(duk_context*)ctx_;
  duk_gc(ctx, flags);
}


extern "C" JNIEXPORT jboolean JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_getBoolean(JNIEnv* env, jobject /* this */, long ctx_, int idx) {
  duk_context * ctx=(duk_context*)ctx_;
  return duk_get_boolean(ctx, idx);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_getContext(JNIEnv* env, jobject /* this */, long ctx_, int idx) {
  duk_context * ctx=(duk_context*)ctx_;
  return (long) duk_get_context(ctx,idx);
}




extern "C" JNIEXPORT jboolean JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_getGlobalString(JNIEnv* env, jobject /* this */, long ctx_, jstring jstr) {
  duk_context * ctx=(duk_context*)ctx_;
  const char* cstr=getString(env, jstr);
  bool result= duk_get_global_string(ctx, cstr);
  /* notice the system will store cstr in heap, (before checking is there same string)*/
  releaseString(env, jstr, cstr);
  return result;

}



extern "C" JNIEXPORT void JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_pushInt(JNIEnv* env, jobject /* this */, long ctx_, int val) {
  duk_context * ctx=(duk_context*)ctx_;
  duk_push_int(ctx, val);
}


extern "C" JNIEXPORT void JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_pushNumber(JNIEnv* env, jobject /* this */, long ctx_, double val) {
  duk_context * ctx=(duk_context*)ctx_;
  duk_push_number(ctx, val);
}

extern "C" JNIEXPORT void JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_pushNull(JNIEnv* env, jobject /* this */, long ctx_) {
  duk_context * ctx=(duk_context*)ctx_;
  duk_push_null(ctx);
}



extern "C" JNIEXPORT void JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_pushString(JNIEnv* env, jobject /* this */, long ctx_, jstring jstr) {
  duk_context * ctx=(duk_context*)ctx_;
  const char* cstr=getString(env, jstr);
  duk_push_string(ctx, cstr);
  /* notice the system will store cstr in heap, (before checking is there same string)*/
  releaseString(env, jstr, cstr);
}

extern "C" JNIEXPORT void JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_pushPointer(JNIEnv* env, jobject /* this */, long ctx_, jobject p) {
  duk_context * ctx=(duk_context*)ctx_;
  /*notice one must release it*/
  duk_push_pointer(ctx,env->NewGlobalRef(p));
  /* it seems we need this?*/
  //deleteLocalRef(env,p);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_pushProxy(JNIEnv* env, jobject /* this */, long ctx_) {
  duk_context * ctx=(duk_context*)ctx_;
  return duk_push_proxy(ctx, 0);
}



extern "C" JNIEXPORT jobject JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_getPointer(JNIEnv* env, jobject /* this */, long ctx_, int idx) {
  duk_context * ctx=(duk_context*)ctx_;
  return (jobject)duk_get_pointer(ctx, idx);
}

extern "C" JNIEXPORT void JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_delPointer(JNIEnv* env, jobject /* this */, long ctx_, int idx) {
  duk_context * ctx=(duk_context*)ctx_;
  deleteGlobalRef(env, (jobject)duk_get_pointer(ctx, idx));
}




#define MAXSYMBOL 100
extern "C" JNIEXPORT void JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_pushSymbol(JNIEnv* env, jobject /* this */, long ctx_, jstring jstr) {
  duk_context * ctx=(duk_context*)ctx_;
  const char* cstr=getString(env, jstr);
  char symbol[MAXSYMBOL];
  strcpy(symbol, "\xff");
  strcat(symbol, cstr);
  duk_push_string(ctx, symbol);
  /* notice the system will store cstr in heap, (before checking is there same string)*/
  releaseString(env, jstr, cstr);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_pushThread(JNIEnv* env, jobject /* this */, long ctx_) {
  duk_context * ctx=(duk_context*)ctx_;
  return duk_push_thread(ctx);
}




extern "C" JNIEXPORT jint JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_pushBareObject(JNIEnv* env, jobject /* this */, long ctx_) {
  duk_context * ctx=(duk_context*)ctx_;
  return duk_push_bare_object(ctx);
}

extern "C" JNIEXPORT void JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_pushBoolean(JNIEnv* env, jobject /* this */, long ctx_, bool value) {
  duk_context * ctx=(duk_context*)ctx_;
  duk_push_boolean(ctx, value);
}


extern "C" JNIEXPORT void JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_pushGlobalObject(JNIEnv* env, jobject /* this */, long ctx_) {
  duk_context * ctx=(duk_context*)ctx_;
  duk_push_global_object(ctx);
}




extern "C" JNIEXPORT jint JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_pushObject(JNIEnv* env, jobject /* this */, long ctx_) {
  duk_context * ctx=(duk_context*)ctx_;
  return duk_push_object(ctx);
}


extern "C" JNIEXPORT jboolean JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_putProp(JNIEnv* env, jobject /* this */, long ctx_, int obj_idx) {
  duk_context * ctx=(duk_context*)ctx_;
  return duk_put_prop(ctx, obj_idx);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_putGlobalString(JNIEnv* env, jobject /* this */, long ctx_, jstring jstr) {
  duk_context * ctx=(duk_context*)ctx_;
  const char* cstr=getString(env, jstr);
  bool result=duk_put_global_string(ctx, cstr);
  /* notice the system will store cstr in heap, (before checking is there same string)*/
  releaseString(env, jstr, cstr);
  return result;
}




extern "C" JNIEXPORT jboolean JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_getProp(JNIEnv* env, jobject /* this */, long ctx_, int obj_idx) {
  duk_context * ctx=(duk_context*)ctx_;
  return duk_get_prop(ctx, obj_idx);
}

extern "C" JNIEXPORT void JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_getPrototype(JNIEnv* env, jobject /* this */, long ctx_, int idx) {
  duk_context * ctx=(duk_context*)ctx_;
  duk_get_prototype(ctx, idx );
}


extern "C" JNIEXPORT jint JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_getType(JNIEnv* env, jobject /* this */, long ctx_, int idx) {
  duk_context * ctx=(duk_context*)ctx_;
  return duk_get_type(ctx, idx);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_getTypeMask(JNIEnv* env, jobject /* this */, long ctx_, int idx) {
  duk_context * ctx=(duk_context*)ctx_;
  return duk_get_type_mask(ctx, idx);
}





extern "C" JNIEXPORT void JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_pop(JNIEnv* env, jobject /* this */, long ctx_) {
  duk_context * ctx=(duk_context*)ctx_;
  duk_pop(ctx);
}


extern "C" JNIEXPORT void JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_popN(JNIEnv* env, jobject /* this */, long ctx_, int count) {
  duk_context * ctx=(duk_context*)ctx_;
  duk_pop_n(ctx, count);
}


extern "C" JNIEXPORT jint JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_getTopIndex(JNIEnv* env, jobject /* this */, long ctx_) {
  duk_context * ctx=(duk_context*)ctx_;
  return duk_get_top_index(ctx);
}


extern "C" JNIEXPORT jint JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_getTop(JNIEnv* env, jobject /* this */, long ctx_) {
  duk_context * ctx=(duk_context*)ctx_;
  return duk_get_top(ctx);
}

/*default handle to let javascript interact with JVM.*/
#define JSNATIVE   "com/serendipity/chengzhengqian/jsos/JsNative"
#define JAVAHANDLE  "__java_handle__"
#define  JAVAHANDLETYPE "(J)V"

duk_ret_t __java_handle__(duk_context *ctx){
  JNIEnv *env=getCurrentEnv(ctx);
  // jclass cls = env->FindClass(JSNATIVE);
  jclass cls=clsMap[ctx];
  //  jmethodID javaHandle = env->GetStaticMethodID(cls, JAVAHANDLE, JAVAHANDLETYPE);
  jmethodID javaHandle=jhdMap[ctx];
  env->CallStaticVoidMethod(cls, javaHandle, (long)ctx);
  //as this will be called a lot,we must release it
  // deleteLocalRef(env,cls);
  //deleteLocalRef(env,(jobject)javaHandle);  
  return 1;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_pushJavaHandle(JNIEnv* env, jobject /* this */, long ctx_) {
  duk_context * ctx=(duk_context*)ctx_;
  // env->GetJavaVM(&jvm);
  envs[ctx]=env;
  clsMap[ctx]=(jclass)env->NewGlobalRef(env->FindClass(JSNATIVE));
  //notice methodid is a permanent variable
  jhdMap[ctx]=env->GetStaticMethodID(clsMap[ctx], JAVAHANDLE, JAVAHANDLETYPE);
  return duk_push_c_function(ctx, __java_handle__,DUK_VARARGS);
  
  //this means __java_handle will receive a variable stack according to caller.
}


extern "C" JNIEXPORT void JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_call(JNIEnv* env, jobject /* this */, long ctx_, int nargs) {
  duk_context * ctx=(duk_context*)ctx_;
  duk_call(ctx, nargs);
}



extern "C" JNIEXPORT void JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_compile(JNIEnv* env, jobject /* this */, long ctx_, int flags) {
  duk_context * ctx=(duk_context*)ctx_;
  duk_compile(ctx, flags);
}


extern "C" JNIEXPORT jint JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_pCompile(JNIEnv* env, jobject /* this */, long ctx_, int flags) {
  duk_context * ctx=(duk_context*)ctx_;
  return duk_pcompile(ctx,flags);
}


extern "C" JNIEXPORT void JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_pushContextDump(JNIEnv* env, jobject /* this */, long ctx_) {
  duk_context * ctx=(duk_context*)ctx_;
  duk_push_context_dump(ctx);
}

duk_ret_t __eval__(duk_context *ctx, void *udata) {
  duk_eval(ctx);
  return 1;
}
extern "C" JNIEXPORT jint JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_safeEvalString(JNIEnv* env, jobject /* this */, long ctx_, jstring jstr) {
  duk_context * ctx=(duk_context*)ctx_;
  const char* cstr=getString(env, jstr);
  duk_push_string(ctx,cstr);
  int result=duk_safe_call(ctx, __eval__, NULL/* udata*/, 1 /*nargs*/, 1 /*nrets*/);
  /* notice the system will store cstr in heap, (before checking is there same string)*/
  releaseString(env, jstr, cstr);
  return result;
}

/* safe eval the string on stack top*/
extern "C" JNIEXPORT jint JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_safeEval(JNIEnv* env, jobject /* this */, long ctx_) {
  duk_context * ctx=(duk_context*)ctx_;
  return  duk_safe_call(ctx, __eval__, NULL/* udata*/, 1 /*nargs*/, 1 /*nrets*/);
}






extern "C" JNIEXPORT void JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_insert(JNIEnv* env, jobject /* this */, long ctx_, int idx) {
  duk_context * ctx=(duk_context*)ctx_;
  duk_insert(ctx, idx);
}


extern "C" JNIEXPORT jboolean JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_getPropString(JNIEnv* env, jobject /* this */, long ctx_,int idx, jstring jstr) {
  duk_context * ctx=(duk_context*)ctx_;
  const char* cstr=getString(env, jstr);
  bool result= duk_get_prop_string(ctx, idx,cstr);
  /* notice the system will store cstr in heap, (before checking is there same string)*/
  releaseString(env, jstr, cstr);
  return result;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_getPropSymbol(JNIEnv* env, jobject /* this */, long ctx_, int idx, jstring jstr) {
  duk_context * ctx=(duk_context*)ctx_;
  const char* cstr=getString(env, jstr);
  char symbol[MAXSYMBOL];
  strcpy(symbol, "\xff");
  strcat(symbol, cstr);
  bool result= duk_get_prop_string(ctx, idx, symbol);
  /* notice the system will store cstr in heap, (before checking is there same string)*/
  releaseString(env, jstr, cstr);
  return result;
}


extern "C" JNIEXPORT void JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_releaseJavaHandle(JNIEnv* env, jobject /* this */, long ctx_) {
  duk_context * ctx=(duk_context*)ctx_;
  releaseCurrentEnv(ctx);  
}


extern "C" JNIEXPORT jboolean JNICALL
Java_com_serendipity_chengzhengqian_jsos_JsNative_isError(JNIEnv* env, jobject /* this */, long ctx_, int idx) {
  duk_context * ctx=(duk_context*)ctx_;
  return duk_is_error(ctx, idx);
}


//
// Created by 60546 on 4/23/2016.
//

#include "com_example_groovemax_ACR_JniUtils.h"

//public native void ACRLoadModelBuildNet(String modelPath, String cmdPath);
JNIEXPORT void JNICALL Java_com_example_groovemax_ACR_JniUtils_ACRLoadModelBuildNet
        (JNIEnv *env, jobject cls, jstring modelString, jstring commandString){
    const char* modelPath = env->GetStringUTFChars(modelString, false);
    const char* commandPath = env->GetStringUTFChars(commandString, false);
    LOGV("%s", modelPath);
    LOGV("%s", commandPath);
    ACRLoadModelBuildNet(modelPath, commandPath);
}

JNIEXPORT jint JNICALL Java_com_example_groovemax_ACR_JniUtils_ACRWavDecode
        (JNIEnv *env, jobject cls, jstring wavString){
    const char* wavPath = env->GetStringUTFChars(wavString, false);
    LOGV("%s", wavPath);
    jint j = WavDecode(wavPath);
    return j;
}

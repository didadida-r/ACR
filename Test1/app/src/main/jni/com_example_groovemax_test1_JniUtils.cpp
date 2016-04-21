//
// Created by 60546 on 4/15/2016.
//

#include <com_example_groovemax_test1_JniUtils.h>


JNIEXPORT jint JNICALL Java_com_example_groovemax_test1_JniUtils_ACRWavDecode
        (JNIEnv *env, jobject cls, jstring string) {

    float * wavData=NULL;
    Vector mfccData=NULL;
    Feature * pFeature=NULL;
    int samplePointNum;
    int frameNum;
    int mfccDimension;

    const char* wavPath = env->GetStringUTFChars(string, false);
    openWavFile(wavPath,&wavData,&samplePointNum);
    wav2mfc(wavData,samplePointNum,&mfccData,&frameNum,&mfccDimension);
    pFeature=(Feature *)malloc(sizeof(Feature)*frameNum);
    mfccToFeature(pFeature,mfccData,frameNum,mfccDimension);
    LOGV("%s",wavPath);
    jint j = mfccDimension;
    return j;
}

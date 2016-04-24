#ifndef _COMMON_H_
#define _COMMON_H_

#define NAMESIZE 20
#define DIMENSION 39
#define NAME 200
#define SIZEA 30

#include <stdio.h>
#include <stdlib.h>
#include <malloc.h>
#include <math.h>
#include <string.h>
#include <iostream>
#include <fstream>
#include <string>
#include <./utils/android_log_print.h>
using namespace std;

//#include <android/log.h>
//#define LOG_TAG "JNI"
//#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##args)
//#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, fmt, ##args)
//#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##args)

struct Gaussian
{
	double weight;
	double mean[39];
	double variance[39];
	double gconst;
};
struct State
{
	int numMixes;
	Gaussian *arr;
};
struct Hmm
{
	string name;
	int stateCount;
	int numMixes;
	State *state;
	double **transfer;
};

struct Link 
{
	int stateSum;//有效的状态数，不包含首尾状态
	struct Node * nodeList;
};
struct Node
{
	struct State *pState;
	double transferNow;
	double transferNext;
};

typedef struct Feature
{
	float *data;
}Feature;


#endif

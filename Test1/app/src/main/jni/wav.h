/**
***************************************************
Function: define features
Author  : Weibin Zhang
Version:
1.0.0     created         zhangweibin            2014.4.2      

***************************************************
*/
#ifndef _WAV_H_
#define _WAV_H_


typedef struct{
    char rID[4];      // 'RIFF'
    int rLen;

    char wID[4];      // 'WAVE'

    char fId[4];      // 'fmt'
    int pcmHeaderLength;
    short int wFormatTag;
    short int numChannels;
    int nSamplesPerSec;
    int nAvgBytesPerSec;
    short int numBlockAlingn;
    short int numBitsPerSample;
} WAV_HDR;

/* header of wave file */
typedef struct
{
    char dId[4];  // 'data' or 'fact'
    int dLen;
} CHUNK_HDR;

int openWavFile(const char* fileName, float **gWavDataIn, int *maxInSamples);



#endif


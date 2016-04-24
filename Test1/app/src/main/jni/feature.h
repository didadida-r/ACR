/**
***************************************************
Function: define features
Author  : Weibin Zhang
Version:
1.0.0     created         zhangweibin            2014.4.2      

***************************************************
*/
#ifndef _FEATURE_H_
#define _FEATURE_H_

#include "common.h";



typedef float *Vector;     /* vector[1..size]   */
typedef short *ShortVec;   /* short vector[1..size] */
/* Boolean type definition */
typedef enum {FALSE=0, TRUE=1} Boolean;
//typedef enum {false=0,true=1} bool;
/* -------------------- MFCC Related Operations -------------------- */
typedef struct{
   int frameSize;       /* speech frameSize */
   int numChans;        /* number of channels */
   long sampPeriod;     /* sample period */
   int fftN;            /* fft size */
   int klo,khi;         /* lopass to hipass cut-off fft indices */
   Boolean usePower;    /* use power rather than magnitude */
   Boolean takeLogs;    /* log filterbank channels */
   float fres;          /* scaled fft resolution */
   Vector cf;           /* array[1..pOrder+1] of centre freqs */
   ShortVec loChan;     /* array[1..fftN/2] of loChan index */
   Vector loWt;         /* array[1..fftN/2] of loChan weighting */
   Vector x;            /* array[1..fftN] of fftchans */
}FBankInfo;

void PreEmphasise (Vector s, float k);

Vector CreateVector(int size);

ShortVec CreateShortVec(int size);


int VectorSize(Vector v);

void ZeroVector(Vector v);


void FFT(Vector s, int invert);

void Realft (Vector s);


void Wave2FBank(Vector s, Vector fbank, float *te, FBankInfo info);

void FBank2MFCC(Vector fbank, Vector c, int n);

void FBank2MFCC(Vector fbank, Vector c, int n);

float Mel(int k,float fres);

float WarpFreq (float fcl, float fcu, float freq, float minFreq, float maxFreq , float alpha);

void Ham (Vector s);

/* EXPORT->InitFBank: Initialise an FBankInfo record */
FBankInfo InitFBank(int frameSize, long sampPeriod, int numChans,
                    float lopass, float hipass, Boolean usePower, Boolean takeLogs,
                    Boolean doubleFFT,
                    float alpha, float warpLowCut, float warpUpCut);


/* EXPORT->WeightCepstrum: Apply cepstral weighting to c */
void WeightCepstrum (Vector c, int start, int count, int cepLiftering);


/* EXPORT->FBank2C0: return zero'th cepstral coefficient */
float FBank2C0(Vector fbank);

/* Regression: add regression vector at +offset from source vector.  If head
   or tail is less than delwin then duplicate first/last vector to compensate */
void Regress(float *data, int vSize, int n, int step, int offset,
                    int delwin, int head, int tail);


void AddDiffs(float *pbuf, int nFrames, int lenOfMfcc, int step, int diffWin, int nhead, 
                     int ntail);

void FZeroMean(float *data, int vSize, int n, int step);


bool wav2mfc(float *pwav, int nSamples, Vector *pmfccData, int *nFrames, int *lOfMfcc);

void mfccToFeature(Feature* pFeature,Vector mfccData,int frameNum,int mfccDimension);

#endif


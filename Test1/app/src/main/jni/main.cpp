#include "common.h"
#include "hmm.h"
#include "feature.h"
#include "wav.h"
#include "gaussian.h"
#include "net.h"
#include "decodeNet.h"
#include <./utils/android_log_print.h>

Hmm ***hmmHead = NULL;//the head of Hmm NetWork
Link *netHead = NULL;//the head of net used to decode
Feature * pFeature=NULL;
int frameNum;
int mfccDimension;
float * wavData=NULL;
Vector mfccData=NULL;
int samplePointNum;
FILE * pCommand = NULL;
int commandSum;//the number of command
int *hmmSum = new int();//the number of triPhone of each command

void ACRLoadModelBuildNet(const char * modelPath, const char * cmdPath){
	pCommand = fopen(cmdPath, "r");
	//readhmm
	hmmHead = hmmRead(pCommand, modelPath, &commandSum, hmmSum);
	LOGV("%s", "readhmm okay");
	//buildNet
	netHead = buildNet(hmmHead, hmmSum, commandSum);
	LOGV("%s", "buildNet okay");
	fclose(pCommand);
	pCommand = NULL;

}

int WavDecode(const char * wavPath){
	//load MFCC feature
	openWavFile(wavPath, &wavData, &samplePointNum);
	LOGV("%s", "openWavFile okay");
	wav2mfc(wavData, samplePointNum, &mfccData, &frameNum, &mfccDimension);
	LOGV("%s", "wav2mfc okay");
	pFeature = (Feature *)malloc(sizeof(Feature)*frameNum);
	LOGV("%s", "pFeature okay");
	mfccToFeature(pFeature, mfccData, frameNum, mfccDimension);
	LOGV("%s", "mfccToFeature okay");
	//preforward algorithem decode
	return preDecode(netHead, commandSum, frameNum, pFeature);
}





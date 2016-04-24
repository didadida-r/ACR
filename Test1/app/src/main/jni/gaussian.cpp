#include "gaussian.h"

float GmmProb(State* pState,Feature * pFeature)
{
	float gmmProb=LZERO;
	float logGaussLike;
	Gaussian * pGaussian;

	int i=0;
	for(i=0;i<pState->numMixes;i++)	
	{
		pGaussian=&pState->arr[i];
		logGaussLike = LogGaussProb(pFeature->data,pGaussian,39);//默认39维高斯，numMixes是混合数
		gmmProb=LogAdd(gmmProb, pGaussian->weight+logGaussLike);	//g1->weight
	}
	
	return gmmProb;
}



/*
Function : LogGaussProb
Action : calculate the log value of a gauss
Input : a pointer point to the data of feature,a pointer point to Gaussian struct,the dimension of gauss
Output: log value of the gauss
*/
float LogGaussProb(float *mfcc,Gaussian *guassian, int dimension)
{
	float prob, factor = 0;
	int a;

	for(a=0; a<dimension; a++)
		factor = factor + (mfcc[a] - guassian->mean[a])*(mfcc[a] - guassian->mean[a])/guassian->variance[a];

	prob = guassian->gconst + factor;

	//return the log Gaussian Prob
	return -0.5*prob;
}


/*
Function : LogAdd
input log(x)+log(y),return log(x+y);
Action : return sum x+y on log scale
Input : two log like
Output: sum x+y on log scale
*/
float LogAdd(float x, float y)
{
	float temp,diff,z;

	if (x<y) {
		temp = x; x = y; y = temp;
	}
	diff = y-x;
	if (diff<(-log(-LZERO))) 
		return  (x<LSMALL)?LZERO:x;
	else {
		z = exp(diff);
		return x+log(1.0+z);
	}
}
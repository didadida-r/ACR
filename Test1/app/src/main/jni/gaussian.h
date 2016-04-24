#include "common.h"
#define LZERO  (-1.0E10)		      /* ~log(0)*/
#define LSMALL (-0.5E10)			  /* log values < LSMALL are set to LZERO */
#define MINMIX  1.0E-5				  /* Min usable mixture weight */
#define LMINMIX -11.5129254649702     /* log(MINMIX) */


float LogGaussProb(float *mfcc,Gaussian *guassian, int dimension);
float LogAdd(float x, float y);

float GmmProb(State* pState,Feature * pFeature);
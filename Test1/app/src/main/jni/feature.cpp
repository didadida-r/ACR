/*
 *--------------------------------------------------------------------------------------
 *       Class:  Feature
 *      Method:  extract MFCC feature from wav data
 * Description:  this is another constructor which reads the file.
 *--------------------------------------------------------------------------------------
 */

#include "feature.h"

#define PI   3.14159265358979
#define TPI  6.28318530717959     /* PI*2 */


static int hamWinSize = 0;          /* Size of current Hamming window */
static Vector hamWin = NULL;        /* Current Hamming window */

static int cepWinSize=0;            /* Size of current cepstral weight window */
static int cepWinL=0;               /* Current liftering coeff */
static Vector cepWin = NULL;        /* Current cepstral weight window */

/* configures to convert wave to mfcc */
static double winDur=250000.0;
static double tgtSampRate=100000.0;
static double preEmph=0.97;
static int numChans=26;
static int cepLifter=22;
static int numCepCoef=12;
static float loFBankFreq=64;
static float hiFBankFreq=8000;
static Boolean useHam=TRUE;
static Boolean usePower=FALSE;
static Boolean doubleFFT=FALSE;
static int frameSize=400;
static int skip=160;
static long srcSampRate=625;

static int step=13;
static int lenOfMfcc=39;

static int diffWin=2;
/* configures to convert wave to mfcc (end)*/



size_t VectorElemSize(int size) { return (size+1)*sizeof(float); }


/* EXPORT->CreateVector:  Allocate space for vector v[1..size] */
Vector CreateVector(int size)
{
   Vector v;
   int *i;
   
   v = (Vector)malloc(sizeof(float)*(size+1));
   //v = (Vector)new float[size+1];
   i = (int *) v; *i = size;
   return v;
}

/* EXPORT->CreateShortVec:  Allocate space for short array v[1..size] */
ShortVec CreateShortVec(int size)
{
   short *v;
   
   v = (short *)malloc(sizeof(short)*(size+1));
   //v = (short *)new short[size+1];
   *v = size;
   return (ShortVec)v;
}


/* EXPORT->VectorSize: returns number of components in v */
int VectorSize(Vector v)
{
   int *i;
   
   i = (int *) v;
   return *i;
}

/* EXPORT->ZeroVector: Zero the elements of v */
void ZeroVector(Vector v)
{
   int i,n;
   
   n=VectorSize(v);
   for (i=1;i<=n;i++) v[i]=0.0;
}




/* GenHamWindow: generate precomputed Hamming window function */
static void GenHamWindow (int frameSize)
{
   int i;
   float a;
   
   if (hamWin==NULL || VectorSize(hamWin) < frameSize)
      hamWin = CreateVector(frameSize);
   a = TPI / (frameSize - 1);
   for (i=1;i<=frameSize;i++)
      hamWin[i] = 0.54 - 0.46 * cos(a*(i-1));
   hamWinSize = frameSize;
}



/* EXPORT->PreEmphasise: pre-emphasise signal in s */
void PreEmphasise (Vector s, float k)
{
   int i;
   float preE;
   
   preE = k;
   for (i=VectorSize(s);i>=2;i--)
      s[i] -= s[i-1]*preE;
   s[1] *= 1.0-preE;
}

/* EXPORT-> FFT: apply fft/invfft to complex s */
void FFT(Vector s, int invert)
{
   int ii,jj,n,nn,limit,m,j,inc,i;
   double wx,wr,wpr,wpi,wi,theta;
   double xre,xri,x;
   
   n=VectorSize(s);
   nn=n / 2; j = 1;
   for (ii=1;ii<=nn;ii++) {
      i = 2 * ii - 1;
      if (j>i) {
         xre = s[j]; xri = s[j + 1];
         s[j] = s[i];  s[j + 1] = s[i + 1];
         s[i] = xre; s[i + 1] = xri;
      }
      m = n / 2;
      while (m >= 2  && j > m) {
         j -= m; m /= 2;
      }
      j += m;
   };
   limit = 2;
   while (limit < n) {
      inc = 2 * limit; theta = TPI / limit;
      if (invert) theta = -theta;
      x = sin(0.5 * theta);
      wpr = -2.0 * x * x; wpi = sin(theta); 
      wr = 1.0; wi = 0.0;
      for (ii=1; ii<=limit/2; ii++) {
         m = 2 * ii - 1;
         for (jj = 0; jj<=(n - m) / inc;jj++) {
            i = m + jj * inc;
            j = i + limit;
            xre = wr * s[j] - wi * s[j + 1];
            xri = wr * s[j + 1] + wi * s[j];
            s[j] = s[i] - xre; s[j + 1] = s[i + 1] - xri;
            s[i] = s[i] + xre; s[i + 1] = s[i + 1] + xri;
         }
         wx = wr;
         wr = wr * wpr - wi * wpi + wr;
         wi = wi * wpr + wx * wpi + wi;
      }
      limit = inc;
   }
   if (invert)
      for (i = 1;i<=n;i++) 
         s[i] = s[i] / nn;
   
}

/* EXPORT-> Realft: apply fft to real s */
void Realft (Vector s)
{
   int n, n2, i, i1, i2, i3, i4;
   double xr1, xi1, xr2, xi2, wrs, wis;
   double yr, yi, yr2, yi2, yr0, theta, x;

   n=VectorSize(s) / 2; n2 = n/2;
   theta = PI / n;
   FFT(s, FALSE);
   x = sin(0.5 * theta);
   yr2 = -2.0 * x * x;
   yi2 = sin(theta); yr = 1.0 + yr2; yi = yi2;
   for (i=2; i<=n2; i++) {
      i1 = i + i - 1;      i2 = i1 + 1;
      i3 = n + n + 3 - i2; i4 = i3 + 1;
      wrs = yr; wis = yi;
      xr1 = (s[i1] + s[i3])/2.0; xi1 = (s[i2] - s[i4])/2.0;
      xr2 = (s[i2] + s[i4])/2.0; xi2 = (s[i3] - s[i1])/2.0;
      s[i1] = xr1 + wrs * xr2 - wis * xi2;
      s[i2] = xi1 + wrs * xi2 + wis * xr2;
      s[i3] = xr1 - wrs * xr2 + wis * xi2;
      s[i4] = -xi1 + wrs * xi2 + wis * xr2;
      yr0 = yr;
      yr = yr * yr2 - yi  * yi2 + yr;
      yi = yi * yr2 + yr0 * yi2 + yi;
   }
   xr1 = s[1];
   s[1] = xr1 + s[2];
   s[2] = 0.0;
}

/* EXPORT->Mel: return mel-frequency corresponding to given FFT index */
float Mel(int k,float fres)
{
   return 1127 * log(1 + (k-1)*fres);
}

/* EXPORT->WarpFreq: return warped frequency */
float WarpFreq (float fcl, float fcu, float freq, float minFreq, float maxFreq , float alpha)
{
   if (alpha == 1.0)
      return freq;
   else {
      float scale = 1.0 / alpha;
      float cu = fcu * 2 / (1 + scale);
      float cl = fcl * 2 / (1 + scale);

      float au = (maxFreq - cu * scale) / (maxFreq - cu);
      float al = (cl * scale - minFreq) / (cl - minFreq);
      
      if (freq > cu)
         return  au * (freq - cu) + scale * cu ;
      else if (freq < cl)
         return al * (freq - minFreq) + minFreq ;
      else
         return scale * freq ;
   }
}



/* EXPORT->InitFBank: Initialise an FBankInfo record */
FBankInfo InitFBank(int frameSize, long sampPeriod, int numChans,
                    float lopass, float hipass, Boolean usePower, Boolean takeLogs,
                    Boolean doubleFFT,
                    float alpha, float warpLowCut, float warpUpCut)
{
   FBankInfo fb;
   float mlo,mhi,ms,melk;
   int k,chan,maxChan,Nby2;

   /* Save sizes to cross-check subsequent usage */
   fb.frameSize = frameSize; fb.numChans = numChans;
   fb.sampPeriod = sampPeriod; 
   fb.usePower = usePower; fb.takeLogs = takeLogs;
   /* Calculate required FFT size */
   fb.fftN = 2;   
   while (frameSize>fb.fftN) fb.fftN *= 2;
   if (doubleFFT) 
      fb.fftN *= 2;
   Nby2 = fb.fftN / 2;
   fb.fres = 1.0E7/(sampPeriod * fb.fftN * 700.0);
   maxChan = numChans+1;
   /* set lo and hi pass cut offs if any */
   fb.klo = 2; fb.khi = Nby2;       /* apply lo/hi pass filtering */
   mlo = 0; mhi = Mel(Nby2+1,fb.fres);
   if (lopass>=0.0) {
      mlo = 1127*log(1+lopass/700.0);
      fb.klo = (int) ((lopass * sampPeriod * 1.0e-7 * fb.fftN) + 2.5);
      if (fb.klo<2) fb.klo = 2;
   }
   if (hipass>=0.0) {
      mhi = 1127*log(1+hipass/700.0);
      fb.khi = (int) ((hipass * sampPeriod * 1.0e-7 * fb.fftN) + 0.5);
      if (fb.khi>Nby2) fb.khi = Nby2;
   }
   /* Create vector of fbank centre frequencies */
   fb.cf = CreateVector(maxChan);
   ms = mhi - mlo;
   for (chan=1; chan <= maxChan; chan++) {
      if (alpha == 1.0) {
         fb.cf[chan] = ((float)chan/(float)maxChan)*ms + mlo;
      }
      else {
         /* scale assuming scaling starts at lopass */
         float minFreq = 700.0 * (exp (mlo / 1127.0) - 1.0 );
         float maxFreq = 700.0 * (exp (mhi / 1127.0) - 1.0 );
         float cf = ((float)chan / (float) maxChan) * ms + mlo;
         
         cf = 700 * (exp (cf / 1127.0) - 1.0);
         
         fb.cf[chan] = 1127.0 * log (1.0 + WarpFreq (warpLowCut, warpUpCut, cf, minFreq, maxFreq, alpha) / 700.0);
      }
   }
   
   /* Create loChan map, loChan[fftindex] -> lower channel index */
   fb.loChan = CreateShortVec(Nby2);
   for (k=1,chan=1; k<=Nby2; k++){
      melk = Mel(k,fb.fres);
      if (k<fb.klo || k>fb.khi) fb.loChan[k]=-1;
      else {
         while (fb.cf[chan] < melk  && chan<=maxChan) ++chan;
         fb.loChan[k] = chan-1;
      }
   }

   /* Create vector of lower channel weights */   
   fb.loWt = CreateVector(Nby2);
   for (k=1; k<=Nby2; k++) {
      chan = fb.loChan[k];
      if (k<fb.klo || k>fb.khi) fb.loWt[k]=0.0;
      else {
         if (chan>0) 
            fb.loWt[k] = ((fb.cf[chan+1] - Mel(k,fb.fres)) / 
                          (fb.cf[chan+1] - fb.cf[chan]));
         else
            fb.loWt[k] = (fb.cf[1]-Mel(k,fb.fres))/(fb.cf[1] - mlo);
      }
   }
   /* Create workspace for fft */
   fb.x = CreateVector(fb.fftN);
   return fb;
}



/* EXPORT->Wave2FBank:  Perform filterbank analysis on speech s */
void Wave2FBank(Vector s, Vector fbank, float *te, FBankInfo info)
{
   const float melfloor = 1.0;
   int k, bin;
   float t1,t2;   /* real and imag parts */
   float ek;      /* energy of k'th fft channel */
   
   /* Check that info record is compatible */
   if (info.frameSize != VectorSize(s))
      {printf("Wave2FBank: frame size mismatch"); return;}
   if (info.numChans != VectorSize(fbank))
      {printf("Wave2FBank: num channels mismatch");return;}
   /* Compute frame energy if needed */
   if (te != NULL){
      *te = 0.0;  
      for (k=1; k<=info.frameSize; k++) 
         *te += (s[k]*s[k]);
   }
   /* Apply FFT */
   for (k=1; k<=info.frameSize; k++) 
      info.x[k] = s[k];    /* copy to workspace */
   for (k=info.frameSize+1; k<=info.fftN; k++) 
      info.x[k] = 0.0;   /* pad with zeroes */
   Realft(info.x);                            /* take fft */

   /* Fill filterbank channels */
   ZeroVector(fbank); 
   for (k = info.klo; k <= info.khi; k++) {             /* fill bins */
      t1 = info.x[2*k-1]; t2 = info.x[2*k];
      if (info.usePower)
         ek = t1*t1 + t2*t2;
      else
         ek = sqrt(t1*t1 + t2*t2);
      bin = info.loChan[k];
      t1 = info.loWt[k]*ek;
      if (bin>0) fbank[bin] += t1;
      if (bin<info.numChans) fbank[bin+1] += ek - t1;
   }

   /* Take logs */
   if (info.takeLogs)
      for (bin=1; bin<=info.numChans; bin++) { 
         t1 = fbank[bin];
         if (t1<melfloor) t1 = melfloor;
         fbank[bin] = log(t1);
      }
}

/* EXPORT->FBank2MFCC: compute first n cepstral coeff */
void FBank2MFCC(Vector fbank, Vector c, int n)
{
   int j,k,numChan;
   float mfnorm,pi_factor,x;
   
   numChan = VectorSize(fbank);
   mfnorm = sqrt(2.0/(float)numChan);
   pi_factor = PI/(float)numChan;
   for (j=1; j<=n; j++)  {
      c[j] = 0.0; x = (float)j * pi_factor;
      for (k=1; k<=numChan; k++)
         c[j] += fbank[k] * cos(x*(k-0.5));
      c[j] *= mfnorm;
   }        
}

/* GenCepWin: generate a new cep liftering vector */
static void GenCepWin (int cepLiftering, int count)
{
   int i;
   float a, Lby2;
   
   if (cepWin==NULL || VectorSize(cepWin) < count)
      cepWin = CreateVector(count);
   a = PI/cepLiftering;
   Lby2 = cepLiftering/2.0;
   for (i=1;i<=count;i++)
      cepWin[i] = 1.0 + Lby2*sin(i * a);
   cepWinL = cepLiftering;
   cepWinSize = count;
} 

/* EXPORT->Ham: Apply Hamming Window to Speech frame s */
void Ham (Vector s)
{
   int i,frameSize;
   
   frameSize=VectorSize(s);
   if (hamWinSize != frameSize)
      GenHamWindow(frameSize);
   for (i=1;i<=frameSize;i++)
      s[i] *= hamWin[i];
}



/* EXPORT->WeightCepstrum: Apply cepstral weighting to c */
void WeightCepstrum (Vector c, int start, int count, int cepLiftering)
{
   int i,j;
   
   if (cepWinL != cepLiftering || count > cepWinSize)
      GenCepWin(cepLiftering,count);
   j = start;
   for (i=1;i<=count;i++)
      c[j++] *= cepWin[i];
}


/* EXPORT->FBank2C0: return zero'th cepstral coefficient */
float FBank2C0(Vector fbank)
{
   int k,numChan;
   float mfnorm,sum;
   
   numChan = VectorSize(fbank);
   mfnorm = sqrt(2.0/(float)numChan);
   sum = 0.0; 
   for (k=1; k<=numChan; k++)
      sum += fbank[k];
   return sum * mfnorm;
}


/* Regression: add regression vector at +offset from source vector.  If head
   or tail is less than delwin then duplicate first/last vector to compensate */
void Regress(float *data, int vSize, int n, int step, int offset,
                    int delwin, int head, int tail)
{
   float *fp,*fp1,*fp2, *back, *forw;
   float sum, sigmaT2;
   int i,t,j;
   
   sigmaT2 = 0.0;
   for (t=1;t<=delwin;t++)
      sigmaT2 += t*t;
   sigmaT2 *= 2.0;
   fp = data;
   for (i=1;i<=n;i++){
      fp1 = fp; fp2 = fp+offset;
      for (j=1;j<=vSize;j++){
         back = forw = fp1; sum = 0.0;
         for (t=1;t<=delwin;t++) {
            if (head+i-t > 0)     back -= step;
            if (tail+n-i+1-t > 0) forw += step;
            sum += t * (*forw - *back);
         }
         *fp2 = sum / sigmaT2;
         ++fp1; ++fp2;
      }
      fp += step;
   }
}


void AddDiffs(float *pbuf, int nFrames, int lenOfMfcc, int step, int diffWin, int nhead, 
                     int ntail)
{
    int n=nFrames-nhead-ntail;

    Regress(pbuf,step,nhead,lenOfMfcc,step,diffWin,0,2);
    pbuf += nhead*lenOfMfcc;

    
    Regress(pbuf,step,n,lenOfMfcc,step,diffWin,diffWin,diffWin);
    pbuf += n*lenOfMfcc;

    Regress(pbuf,step,ntail,lenOfMfcc,step,diffWin,
                       diffWin,0);
}


/* EXPORT->FZeroMean: Zero mean the given data sequence */
void FZeroMean(float *data, int vSize, int n, int step)
{
   double sum;
   float *fp,mean;
   int i,j;


   for (i=0; i<vSize; i++){
      /* find mean over i'th component */
      sum = 0.0;
      fp = data+i;
      for (j=0;j<n;j++){
         sum += *fp; fp += step;
      }
      mean = sum / (double)n;
      /* subtract mean from i'th components */
      fp = data+i;
      for (j=0;j<n;j++){
         *fp -= mean; fp += step;
      }
   }

}

bool wav2mfc(float *pwav, int nSamples, Vector *pmfccData, int *nFrames, int *lOfMfcc)
{
  int nhead, ntail;
  
  float *pbuf;
  FBankInfo fbInfo;
  Vector frameData=CreateVector(frameSize);
  Vector fbank=CreateVector(numChans);
  Vector mfccData;
  int i,j;

  nhead=ntail=diffWin;
  fbInfo.cf=NULL;
  fbInfo.loChan=NULL;
  fbInfo.loWt=NULL;
  fbInfo.x=NULL;


  //LOGE("wav2 A");
  *lOfMfcc=DIMENSION;//原为 *lOfMfcc=lenOfMfcc;
    
  *nFrames=(int) floor(((float) (nSamples-frameSize)/skip)) +1;
    
  if(*nFrames-nhead-ntail <= 0)
  {printf("ERROR: not enough frames!"); return false;}
    
  mfccData=CreateVector((*nFrames)*lenOfMfcc);
  *pmfccData=mfccData;
  //LOGE("wav2 b");
  /*static MFCCs*/
  pbuf=mfccData;
  for(i=0;i<*nFrames;i++)
  {
      //copy for the workplace
      for(j=0;j<frameSize;j++)
          frameData[j+1]=pwav[j];
    
      pwav += skip;
    
	  fbInfo=InitFBank(frameSize,srcSampRate,numChans,loFBankFreq,hiFBankFreq,usePower,TRUE,doubleFFT,1.0,0.0,0.0);
	  PreEmphasise(frameData,preEmph);
	  Ham(frameData);
	  Wave2FBank(frameData,fbank, NULL, fbInfo);
	  FBank2MFCC(fbank, pbuf, numCepCoef);
	  WeightCepstrum(pbuf, 1, numCepCoef, cepLifter);
	  pbuf[step]=FBank2C0(fbank);

	  pbuf += lenOfMfcc; 


	  free(fbInfo.cf);
	  free(fbInfo.loWt);
	  free(fbInfo.x);
	  free(fbInfo.loChan);

  }
  //LOGE("wav2 c");
  pbuf=&mfccData[1];
  AddDiffs(pbuf, *nFrames, lenOfMfcc, step, diffWin, nhead, 
	  ntail);

  AddDiffs(pbuf+13, *nFrames, lenOfMfcc, step, diffWin, nhead, 
	  ntail);


  FZeroMean(&mfccData[1], step, *nFrames, lenOfMfcc);
  //LOGE("wav2 d");
  free(hamWin);hamWin=NULL;
  free(cepWin);cepWin=NULL;
  free(frameData);frameData=NULL;
  free(fbank);fbank=NULL;



hamWinSize = 0;
hamWin = NULL;

cepWinSize=0;
cepWinL=0;
cepWin = NULL;

  /* configures to convert wave to mfcc */
winDur=250000.0;
tgtSampRate=100000.0;
preEmph=0.97;
numChans=26;
cepLifter=22;
numCepCoef=12;
loFBankFreq=64;
hiFBankFreq=8000;
useHam=TRUE;
usePower=FALSE;
doubleFFT=FALSE;
frameSize=400;
skip=160;
srcSampRate=625;

step=13;
lenOfMfcc=39;

diffWin=2;
  //LOGE("wav2 e");
}



void mfccToFeature(Feature* pFeature,Vector mfccData,int frameNum,int mfccDimension)
{
	int i,j;

	for (i=0;i<frameNum;i++)
	{
		pFeature[i].data=(float *)malloc(sizeof(float)*mfccDimension);
		for (j=0;j<mfccDimension;j++)
		{
			mfccData++;
			pFeature[i].data[j]=*mfccData;		
		}
	}
}


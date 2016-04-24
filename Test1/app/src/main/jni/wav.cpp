#include "wav.h"


/*
 *--------------------------------------------------------------------------------------
 *       Class:  WavFile
 *      Method:  WavFile
 * Description:  this is another constructor which reads the file.
 *--------------------------------------------------------------------------------------
 */

static int debug=0;

int openWavFile(const char* fileName, float **gWavDataIn, int *maxInSamples)
{

#ifdef  DEBUG
    LOGI("\nopenWavFile function");
#endif     /* -----  not DEBUG  ----- */
    int i;
    //LOGI("Inside function.");
    FILE *pFile;
    unsigned int stat;
    char outBuffer[80];

    WAV_HDR* pWavHeader;
    CHUNK_HDR* pChunkHeader;

    short int* pU;
    unsigned char* pC;
    int sFlag;
    long int rMore;

    char* wBuffer;
    int wBufferLength;

    /* set the defaults values. */
    *maxInSamples = 0;
    *maxInSamples = 0;


    
    /* allocate wav header */
	pWavHeader =(WAV_HDR *)malloc(sizeof(WAV_HDR));
	pChunkHeader =(CHUNK_HDR *)malloc(sizeof(CHUNK_HDR));

   // pWavHeader = new WAV_HDR;
   // pChunkHeader = new CHUNK_HDR;
    
    if( NULL == pWavHeader )
    {
        printf("can't new headers\n");
        return 0;
    }

    if( NULL == pChunkHeader )
    {
        printf("can't new headers\n");
        return 0;
    }

    /* 
     * open the wav file 
     */
    pFile = fopen( fileName, "rb");
    if(pFile == NULL)
    {
        printf("Can't open wav file.\n");
        return 0;
    }

    if(debug)
        printf("success in opening the wav file:%s.\n",fileName);


    /*-----------------------------------------------------------------------------
     *  Now, we have load the file. Start reading data.
     *-----------------------------------------------------------------------------*/

    /* read riff/wav header */ 
    stat = fread((void*) pWavHeader, sizeof(WAV_HDR), (size_t)1, pFile);
    if(stat != 1)
    {
        printf("Header missing. May be format is not OK!\n"); // This is tested.
        return 0;
    }

    /* check format of header */
    for(i = 0; i < 4; i++)
    {
        outBuffer[i] = pWavHeader->rID[i];
    }
    outBuffer[4] = 0;
    if(strcmp(outBuffer, "RIFF") != 0) // tested.
    {
        printf("\nBad RIFF format. I am not cool enough to support everything");
        printf("\nyou provide us with! Give me a good file.");
        return 0;
    }

    for(i = 0; i < 4; i++)
    {
        outBuffer[i] = pWavHeader->wID[i];
    }
    outBuffer[4] = 0;

    if(strcmp(outBuffer, "WAVE") != 0) // tested.
    {
        printf("\nBad WAVE format");
        return 0;
    }

    for(i = 0; i < 4; i++)
    {
        outBuffer[i] = pWavHeader->fId[i];
    }
    outBuffer[4] = 0;

    if(strcmp(outBuffer, "fmt ") != 0) // not with "fmt" since 4th pos is blank
    {
        printf("\nBad fmt format");
        return 0;
    }

    if(pWavHeader->wFormatTag != 1)
    {
        printf("\n Bad wav wFormatTag");
        return 0;
    }

    if( (pWavHeader->numBitsPerSample != 16) && (pWavHeader->numBitsPerSample != 8))
    {
        printf("\nBad wav bits per sample");
        return 0;
    }

    /* 
     * Skip over any remaining portion of wav header.
     */
    rMore = pWavHeader->pcmHeaderLength - (sizeof(WAV_HDR) - 20);
    if( 0 != fseek(pFile, rMore, SEEK_CUR))
    {
        printf("Can't seek.");
        return 0;
    }

    /* 
     * read chunk untill a data chunk is found.
     */
    sFlag = 1;
    while(sFlag != 0)
    {
        // check attempts.
        if(sFlag > 10) { printf("\nToo manu chunks"); return 0;}

        // read chunk header
        stat = fread((void*)pChunkHeader, sizeof(CHUNK_HDR), (size_t)1, pFile);
        if( 1 != stat)
        {
            printf("\n I just can't read data. Sorry!");
            return 0;
        }

        // check chunk type.
        for(i =0; i < 4; i++)
        {
            outBuffer[i] = pChunkHeader->dId[i];
        }
        outBuffer[4] = 0;
        if(strcmp(outBuffer, "data") == 0) { break;}

        // skip over chunk.
        sFlag++;
        stat = fseek(pFile, pChunkHeader->dLen, SEEK_CUR);
        if(stat != 0)
        {
            printf("Can't seek.");
            return 0;
        }

    }

    /* find length of remaining data. */
    wBufferLength = pChunkHeader->dLen;


    /* find number of samples. */
    *maxInSamples = pChunkHeader->dLen;
    *maxInSamples /= pWavHeader->numBitsPerSample/8;

    /* allocate new buffers */
	wBuffer=(char *)malloc(sizeof(char)*wBufferLength);
    //wBuffer = new char[wBufferLength];
    if( wBuffer == NULL)
    {
        printf("\nCan't allocate."); return 0;
    }

	*gWavDataIn =(float *)malloc(sizeof(float)*(*maxInSamples));
    //*gWavDataIn = new float[*maxInSamples];
    if(*gWavDataIn == NULL)
    {
        printf("Can't allocate\n"); return 0;
    }

    /* read signal data */
    stat = fread((void*)wBuffer, wBufferLength, (size_t)1, pFile);
    if( 1 != stat)
    {
        printf("\nCan't read buffer.");
        return 0;
    }

    /* convert data */
    if(pWavHeader->numBitsPerSample == 16)
    {
        pU = (short*) wBuffer;
        for( i = 0; i < *maxInSamples; i++)
        {
            (*gWavDataIn)[i] = (double) (pU[i]);
        }
    }
    else
    {
        pC = (unsigned char*) wBuffer;
        for( i = 0; i < *maxInSamples; i++)
        {
            (*gWavDataIn)[i] = (double) (pC[i]);
        }
    }


   if(wBuffer != NULL) free(wBuffer);//delete wBuffer;
   if(pWavHeader != NULL) free(pWavHeader); //delete pWavHeader;
   if(pChunkHeader != NULL) free(pChunkHeader); //delete pChunkHeader;
   fclose(pFile);
    
   debug=0;
   return 1;
}



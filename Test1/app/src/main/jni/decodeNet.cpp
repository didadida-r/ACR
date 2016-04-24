#include "decodeNet.h"

/*
 * function: use the preforward algorithem to get the final result
 */
int preDecode(Link* link, int commandSum, int frameNum, Feature* pFeature){
	double *probList;
	double *probListNext;
	int i, j, k;
	double result = LZERO;
	int resultNum;

	for (int a = 0; a<commandSum; a++)
	{
		int stateSum = link[a].stateSum;
		Node *nodeList = link[a].nodeList;

		//��ʼ���������һ��״̬�ĸ���Ϊ1
		probList = new double[stateSum + 1];
		for (i = 0; i<stateSum + 1; i++)
			probList[i] = LZERO;
		probList[0] = 0;


		probListNext = new double[stateSum + 1];
		for (i = 0; i<stateSum + 1; i++)
			probListNext[i] = LZERO;

		double *tmp;
		for (int t = 0; t<frameNum; t++)
		{
			for (j = 0; j<stateSum; j++)
			{
				probListNext[j] = LogAdd(probListNext[j], probList[j] + nodeList[j].transferNow);
				probListNext[j + 1] = LogAdd(probListNext[j + 1], probList[j] + nodeList[j].transferNext);
			}
			for (j = 0; j<stateSum; j++)
			{
				probListNext[j] = probListNext[j] + GmmProb(nodeList[j].pState, &pFeature[t]);
			}
			tmp = probList;
			probList = probListNext;
			probListNext = tmp;
			for (j = 0; j<stateSum + 1; j++)
			{
				probListNext[j] = LZERO;
			}
		}
		double sum = LZERO;
		for (i = 0; i<stateSum; i++)
		{
			sum = LogAdd(sum, probList[i]);
		}
		cout << sum / frameNum << endl;
		if (sum > result){
			resultNum = a + 1;
			result = sum;
		}
			
	}
	LOGV("%s", "WavDeocde okay");
	return resultNum;
}
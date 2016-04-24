#include "net.h"

Hmm*** hmmRead(FILE * pCommand, const char* modelPath, int *commandSum, int*& hmmSum){

	char oneLine[1024];
	char *tmp1 = NULL, *tmp2 = NULL;//tmp1指向当前一个三音素的尾部，tmp2指向当前一个的头部
	int cmdNum = 0, triNum = 1;

	string before = modelPath;
	string after = ".hmm";
	LOGV("%s", "hmmRead1");
	while (!feof(pCommand)){
		fgets(oneLine, 1024, pCommand);

		if (feof(pCommand)){
			break;
		}
		cmdNum++;
	}

	LOGV("%s", "hmmRead2");

	*commandSum = cmdNum;
	string** name = (string**)calloc(cmdNum, sizeof(string*));
	Hmm*** hmm = (Hmm***)calloc(cmdNum, sizeof(Hmm**));
	hmmSum = (int*)calloc(cmdNum, sizeof(int));
	cmdNum = 0;

	LOGV("%s", "hmmRead3");

	//申请内存并加载指令
	fseek(pCommand, 0, 0);
	while (!feof(pCommand)){
		memset(oneLine, '\0', 1024);
		fgets(oneLine, 1024, pCommand);
		if (feof(pCommand)){
			break;
		}
		tmp1 = tmp2 = oneLine;
		//找出当前指令的triNum,并申请内存
		triNum = 1;
		while (*tmp1 != '\n'){
			if (*tmp1 == ' '){
				triNum++;
			}
			tmp1++;
		}
		tmp1 = oneLine;
		*(hmmSum + cmdNum) = triNum;
		name[cmdNum] = new string[triNum];
		*(hmm + cmdNum) = new Hmm*[triNum];
		triNum = 1;

		LOGV("%s", "hmmRead4");

		//遍历当前指令，当找到三音素分界时，将三音素提出并读取hmm模型
		while (1){
			tmp1++;
			if (*tmp1 == ' '){
				*tmp1 = '\0';
				name[cmdNum][triNum-1] = tmp2;
				hmm[cmdNum][triNum-1] = readHmm(before + tmp2 + after);
				//cout << tmp2 << " " << triNum << endl;
				tmp2 = tmp1 + 1;//tmp2指向下一个名字开头
				triNum++;
			}
			if (*tmp1 == '\n'){
				*tmp1 = '\0';
				name[cmdNum][triNum-1] = tmp2;
				hmm[cmdNum][triNum-1] = readHmm(before + tmp2 + after);
				//cout << tmp2 <<" "<<triNum<< endl;
				tmp2 = tmp1 = NULL;
				triNum++;
				break;
			}
		}
		cmdNum++;//下一条指令开始
	}

	return hmm;

}

/*
 * function:build Hmm netWork 
 */
Link* buildNet(Hmm ***hmm, int* hmmSum, int commandSum){
	Link *link = new Link[commandSum];
	int i, j, k;

	for (i = 0; i<commandSum; i++)
	{
		int stateSum = 0;
		for (j = 0; j<hmmSum[i]; j++)
		{
			stateSum += hmm[i][j]->stateCount - 2;
		}
		link[i].stateSum = stateSum;//为了合并所有状态，求总状态数以便生成状态指针数组
	}
	for (i = 0; i<commandSum; i++)
	{
		link[i].nodeList = new Node[link[i].stateSum];
	}
	for (i = 0; i<commandSum; i++)
	{
		int n = 0;
		for (j = 0; j<hmmSum[i]; j++)//构建网络，将状态指针指向状态，给每个节点的转移概率（当前与下一个）赋值
		{
			for (k = 0; k<hmm[i][j]->stateCount - 2; k++)
			{
				link[i].nodeList[n].pState = &hmm[i][j]->state[k];
				link[i].nodeList[n].transferNow = hmm[i][j]->transfer[k][k];
				link[i].nodeList[n].transferNext = hmm[i][j]->transfer[k][k + 1];
				//cout<<n<<' '<<link[i].nodeList[n].transferNow<<' '<<link[i].nodeList[n].transferNext<<endl;
				n++;
			}
		}
	}
	return link;
}
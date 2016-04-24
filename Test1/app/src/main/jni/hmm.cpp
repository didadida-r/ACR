#include "hmm.h";
#include "common.h"

int readInt(fstream &hmmFile)//����һ����������
{
	int a;
	hmmFile.read((char*)&a,sizeof(int));
	return a;
}
double readDouble(fstream &hmmFile)//����һ����������
{
	double a;
	hmmFile.read((char*)&a,sizeof(double));
	return a;
} 

//����·����ȡhmmģ��
Hmm* readHmm(string name)//��ȡHMM
{
	fstream hmmFile(name.data(),ios::in|ios::binary);
	
	Gaussian *pGaussian;
	Hmm *hmm=new Hmm;
	hmm->name=name;//Hmm����
	int count=readInt(hmmFile);
	hmm->stateCount=count;//״̬��
	hmm->numMixes=readInt(hmmFile);//��˹�����,������Ϊÿ��HMMģ�͵ĸ���״̬�ĸ�˹�����һ����
	hmm->state=new State[hmm->stateCount-2];
	int i,j,k;
	for(i=0;i<count-2;i++)//ÿһ��״̬ �۳���β״̬
	{
		hmm->state[i].arr=new Gaussian[hmm->numMixes];
		hmm->state[i].numMixes=hmm->numMixes;
		for(j=0;j<hmm->numMixes;j++)//ÿһ��״̬�µ�ÿһ����ϸ�˹
		{
			pGaussian=&(hmm->state[i].arr[j]);
			pGaussian->weight=readDouble(hmmFile);//Ȩ��
			for(k=0;k<39;k++)
			{
				pGaussian->mean[k]=readDouble(hmmFile);//��ֵ
			}
			for(k=0;k<39;k++)
			{
				pGaussian->variance[k]=readDouble(hmmFile);//����
			}
			pGaussian->gconst=readDouble(hmmFile);//��˹����
		}
	}
	hmm->transfer=new double*[count];
	for(i=0;i<count;i++)
	{
		hmm->transfer[i]=new double[count];
		for(j=0;j<count;j++)
		{
			hmm->transfer[i][j]=readDouble(hmmFile);
		}
	}
	return hmm;
}
void printHmm(Hmm *hmm)//���HMM����
{
	Gaussian *pGaussian;
	State *pState;
	int count=hmm->stateCount;
	cout<<"HMM name:"<<hmm->name<<endl;
	cout<<"stateCount:"<<hmm->stateCount<<endl;
	cout<<"numMixes:"<<hmm->numMixes<<endl;
	int i,j,k;
	for(i=0;i<count-2;i++)
	{
		cout<<"  state "<<i<<endl;
		pState=&hmm->state[i];
		for(j=0;j<hmm->numMixes;j++)
		{
			cout<<"    gaussian "<<j<<endl;
			pGaussian=&pState->arr[j];
			cout<<"      weight:"<<pGaussian->weight<<endl;
			cout<<"      mean:";
			for(k=0;k<39;k++)
			{
				cout<<pGaussian->mean[k]<<' ';
			}
			cout<<endl<<"      variance:";
			for(k=0;k<39;k++)
			{
				cout<<pGaussian->variance[k]<<' ';
			}
			cout<<endl<<"      gconst:"<<pGaussian->gconst<<endl;
		}
	}
	cout<<"transfer:"<<endl;
	for(i=0;i<count;i++)
	{
		for(j=0;j<count;j++)
		{
			cout<<hmm->transfer[i][j]<<'\t';
		}
		cout<<endl;
	}
}
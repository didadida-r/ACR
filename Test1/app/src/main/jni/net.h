#ifndef _NET_H_
#define _NET_H_

#include "common.h"
#include "hmm.h"


Hmm*** hmmRead(FILE * pCommand, const char* modelPath, int *cmdNum, int*& triNUm);

Link* buildNet(Hmm ***hmm, int* hmmSum, int commandSum);
#endif
# DSI

## Running DSI and reproducing results

1. The script `run-small-project.sh` will run DSI on the shortest running project (kamranzafar.jtar). Running this script takes an estimated 5 minutes to run on a server with 32 cores.
2. The script `bash run-all-projects.sh ${MINING_OP}` will run DSI on the set of 6 projects we used for ground truth inspections. Here, `${MINING_OP}` should be set to `-noMining` to avoid mining.

The set of likely valid specifications from DSI can be found in `data/generated-data/<PROJECT_NAME>/dsiPlus-dsiAllGranularities/results/total-true-specs.txt`.

The files for likely spurious and unknown specs are in the similar paths but with (hopefully) intuitive names.

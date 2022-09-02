# Temari vs. DSI Experiments

This directory contains code and scripts to reproduce the experiments ran for RQ2 (What is the accuracy of Temari, compared to the accuracy of DSI?) and RQ3 (What are the runtime costs of Temari, and how do they compare to those of DSI?).

## Running Temari vs. DSI Experiments

0. Set up the docker environment as specified in `README.md` in the root directory of this repository.

1. To reproduce the experiments on the smallest project (kamranzafar.jtar), run:

```
bash run-smallest-project.sh ${NUM_THREADS}
```
where ${NUM_THREADS} is the number of cores to use.

2. To reproduce the experiments on all projects, run:

```
bash run-all-projects.sh ${NUM_THREADS}
```
where ${NUM_THREADS} is the number of cores to use.

The results of the experiments will be stored in `data/generated-data/${PROJECT}/results` where `${PROJECT}` is the project evaluated.

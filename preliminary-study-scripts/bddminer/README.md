# BDDMiner

## Running BDDMiner and reproducing results

0. Set up the docker environment as specified in `README.md` in the root directory of this repository.

1. To mine FSM specifications using Javert from the smallest project (kamranzafar.jtar), run:

```
bash run-smallest-project.sh ${NUM_THREADS}
```
where ${NUM_THREADS} is the number of cores to use.

2. To mine FSM specifications using Javert from all projects, run:

```
bash run-all-projects.sh ${NUM_THREADS}
```

The final FSM specifications will be stored in:
```
bdd-2-fsms/${PROJECT}.tgz
bdd-3-fsms/${PROJECT}.tgz
```
where ${PROJECT} is the project being mined from.

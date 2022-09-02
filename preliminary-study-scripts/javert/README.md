# Javert

## Running Javert

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

The mined FSM specifications from Javert will be stored in `data/javert-fsms/${PROJECT}` where ${PROJECT} is the project from which specifications are mined.
# DICE

## Docker environment

To run DICE mining, run the following command within this directory:

```
docker run -v ${PWD}/..:/workspace  -it --name DICE adoptopenjdk/openjdk8 /bin/bash
```

## Running DICE

1. Once in the docker environment, finish setting up the environment via:
```
cd /workspace/dice
bash install-packages-to-docker.sh
```

2. To mine from DICE on the smallest project (kamranzafar.jtar), run:

```
bash run-smallest-project.sh ${NUM_THREADS}
```
where ${NUM_THREADS} is the number of cores to use.

2. To mine from DICE from all 6 projects, run:

```
bash run-all-projects.sh ${NUM_THREADS}
```

For each project, the mined specifications from DICE should be located in `data/generated-data/${PROJECT}/generated-specs`.

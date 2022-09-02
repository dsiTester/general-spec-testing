# Texada

## Setting up Texada

0. Set up the docker environment as specified in `docker-setup/README.md`.

1. To further set up Texada, run:

```
bash setup-texada.sh
```

## Running Texada

1. To obtain FSM specs from the project with the least number of Texada specs (commons-codec), run:

```
bash run-smallest-project.sh ${NUM_THREADS}
```
where ${NUM_THREADS} is the number of cores to use. Know that this process took 5 hours on a server with 96 cores.

2. To mine FSM specifications using Texada for all 6 projects, run:

```
bash run-all-projects.sh ${NUM_THREADS}
```
Know that Texada mined the most specifications (45.1 million), and the process of converting these mined specifications to FSMs took about 2 weeks for a server with 96 cores and necessitates ~100G of disk space.

The mined FSM specifications from Texada will be stored in `texada-spec-fsms/${PROJECT}` where ${PROJECT} is the project from which specifications are mined.

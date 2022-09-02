# DSM

## Running DSM

0. Set up the docker environment as specified in `README.md` in the root directory of this repository. Then, to further set up the DSM conda environment, run:

```
bash setup-dsm.sh
```

1. To run both DSM-Manual (DSM with developer-written unit tests) and DSM-Randoop (DSM with randoop-generated unit tests) on the smallest project (kamranzafar.jtar), run:

```
bash run-smallest-project.sh ${NUM_THREADS}
```
where NUM_THREADS is the number of cores.

The mined specifications for DSM-Manual will be in `dsm-manual-kamranzafar.jtar-specs/*.txt`, and the mined specifications for DSM-Randoop will be in `dsm-randoop-kamranzafar.jtar-specs/*.txt`.

2. To run both DSM-Manual (DSM with developer-written unit tests) and DSM-Randoop (DSM with randoop-generated unit tests) on all 6 projects, run:

```
bash run-all-projects.sh ${NUM_THREADS}
```
where NUM_THREADS is the number of cores.

The mined specifications for DSM-Manual will be in `dsm-manual-${PROJECT}-specs/*.txt`, and the mined specifications for DSM-Randoop will be in `dsm-randoop-${PROJECT}-specs/*.txt`.
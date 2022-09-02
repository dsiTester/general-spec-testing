# Temari

## Running Temari

0. Set up the docker environment as specified in `README.md` in the root directory of this repository.

1. To reproduce the limit study described in RQ1 (How generalizable is Temari?), run:

```
bash run-limit-study.sh ${NUM_THREADS}
```
where ${NUM_THREADS} is the number of cores to use.

The results of the limit study will be stored in `data/limit-study-wrapper`, and a high-level summary of the outcomes of running Temari will be stored in `data/limit-study-wrapper/generated-data/summary-results.csv`.

2. To reproduce the experiments described in RQ2 and RQ3, navigate to `state-comparison/scripts` and follow the directions within the `README.md`.

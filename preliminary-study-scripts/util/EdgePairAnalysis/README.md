# Edge Pair Analysis

This directory contains the tool used for analysis done in the preliminary study (Section III-A).

## Running Edge Pair Analysis

0. For all miners besides DSM, place the FSM output into the directory

```
spec-exploration-master/data/${MINER_DIRNAME}/${PROJECT}
```
where `MINER_DIRNAME` is one of bddtwo, bddthree, dice, texada, or javert-fsms, and `PROJECT` is the name of the project from which specifications were mined.

For DSM-Manual, place the FSM output into the directory

```
spec-exploration-master/data/dsm/${PROJECT}-unit-tests
```

For DSM-Randoop, place the FSM output into the directory

```
spec-exploration-master/data/dsm/${PROJECT}-only-randoop
```


1. To run the edge pair analysis, run

```
java -jar EdgePairAnalysis.jar `pwd` ${MINER}
```
where `MINER` is the miner used (one of bddtwo, bddthree, dice, dsm, javert, texada).

Outputs will be stored in `automated-results/${MINER}`.
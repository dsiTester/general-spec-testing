# Data Availability

Below are the files and directories for Data Availability. Each directory contains a `README.md` with information about the data.
- **appendix.pdf**: contains a description of DSI's algorithm and how we use DSI in our preliminary study and in Temari.
- **preliminary-study**: raw data for the preliminary study discussed in Section III. Additional intermediate data for the preliminary study are available at https://zenodo.org/record/7058165/files/edge-pair-analysis.tgz and https://zenodo.org/record/7058165/files/preliminary-study-mined-fsms.tgz.
- **preliminary-study-scripts**: artifacts and scripts that are needed to reproduce our data for the preliminary study discussed in Section III.
- **ground-truth-inspections**: contains manual inspection results (notes, snippets, counter-example tests, etc) in JSON files, schemas, and scripts that are needed to process the manual inspection data.
- **temari_code_and_scripts**: contains artifacts and scripts that are needed to reproduce our data for Temari's evaluation.
- **RQ1**: base data for the limit study described in RQ1 (How generalizable is Temari?).
- **RQ2**: base data for Table VI (Accuracy comparison of Temari and DSI).
- **RQ3**: base data for Table VII (Runtime costs of Temari vs. DSI in minutes).
- All Temari intermediate data are available at https://zenodo.org/record/7058165/files/temari-study-data.tgz.

## Setting up a Docker environment

All scripts and code in this repository should be run in docker using the provided instructions.

Note that scripts for Texada and DICE should be run through different docker configurations. These are specified within `preliminary-study-scripts/texada/README.md` and `preliminary-study-scripts/dice/README.md` respectively. The instructions below are for running all other miners in `preliminary-study-scripts`, and for running experiments in `temari-code-and-scripts`.

0. Build a docker container using `dockerFile` and run.
```
docker build -t temari:test .
docker run --name temaritest -it temari:test
```

1. Run `bash setup.sh` to set up the environment for DSI+ tool implementation to run. This script will move the local `~/.m2` file to `~/.m2-backup`, so this should be done in a docker container to be safe. The first time this script is invoked in the Docker, `mv` will show an error because there is no current `~/.m2` directory.

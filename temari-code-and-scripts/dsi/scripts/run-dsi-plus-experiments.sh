# This script runs all granularities of DSI on the list of projects given.
if [ $# -lt 4 -o $# -gt 5 ]; then
    echo "This script is a wrapper around run-dsi-plus.sh and run-optimized-dsi-plus.sh"
    echo "Usage: $0 PROJECT_LIST OPTION BUILD_OP BACKUP_OP [MINING-OP]"
    echo "where OPTION is one of dsiPlus-dsiAllGranularities dsiPlus-allGranularities dsiPlus-allTests dsiPlus-testClasses dsiPlus-testMethods optimizedDsiPlus-1 optimizedDsiPlus-2 optimizedDsiPlus-3 optimizedDsiPlus-4 optimizedDsiPlus-5 optimizedDsiPlus-6"
    echo "if BUILD_OP is -build, DSI will be built and if BUILD_OP is -noBuild, then DSI will not be built"
    echo "if BACKUP_OP is -backup, generated-data will be backed up and if BACKUP_OP is -noBackup, then no backup dir will be made"
    echo "if MINING-OP is -noMining, DSI++ will read from a preexisting master-spec-file (this option will do nothing for DSI+)"
    exit
fi

PROJ_LIST=$1
OPTION=$2
BUILD_OP=$3
BACKUP_OP=$4
MINING_OP=$5
SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )

SUBOP=$( echo ${OPTION} | cut -d- -f2 )

if [[ "${OPTION}" == dsiPlus* ]]; then
    echo "Running bash run-dsi-plus.sh ${PROJ_LIST} ${SUBOP} ${BUILD_OP} ${BACKUP_OP} ${MINING_OP}"
    bash run-dsi-plus.sh ${PROJ_LIST} ${SUBOP} ${BUILD_OP} ${BACKUP_OP} ${MINING_OP}
elif [[ "${OPTION}" == optimizedDsiPlus* ]]; then
    echo "Running bash run-optimized-dsi-plus.sh ${PROJ_LIST} ${SUBOP} ${BUILD_OP} ${BACKUP_OP} ${MINING_OP}"
    bash run-optimized-dsi-plus.sh ${PROJ_LIST} ${SUBOP} ${BUILD_OP} ${BACKUP_OP} ${MINING_OP}
fi

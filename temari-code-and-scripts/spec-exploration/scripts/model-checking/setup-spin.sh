SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
SPIN_DIR=${SCRIPT_DIR}/spin-tool
LOGS_DIR=${SCRIPT_DIR}/setup-logs
mkdir -p ${LOGS_DIR}

function setup_spin() {
    echo "[ LOG ] Setting up spin"
    if [ ! -d ${SPIN_DIR} ]; then
        (
            cd ${SCRIPT_DIR}
            git clone git@github.com:nimble-code/Spin.git spin-tool
        )
    fi
    local log_file=${LOGS_DIR}/gol-build-spin
    (
        cd ${SPIN_DIR}
        ( make ) &> ${log_file}
    )
    if grep -q "yacc: Command not found" ${log_file} ; then
        echo -e "[ERROR] yacc is not installed. Please install yacc by running:\n sudo apt-get install bison -y"
        exit
    fi
}

setup_spin

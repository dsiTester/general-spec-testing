if [ $# -ne 2 ]; then
    echo "USAGE: bash $0 PML_FILE SPIN_DIR"
    exit
fi

PML_FILE=$1
SPIN_DIR=$2

function run_spin() {
    cd ${SPIN_DIR}
    ./Src/spin -a ${PML_FILE}
    gcc -o pan pan.c
    ./pan -a
}

run_spin

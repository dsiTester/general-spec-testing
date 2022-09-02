if [ $# -ne 3 ]; then
    echo "USAGE: bash $0 PML_FILE SPIN_DIR IDENT"
    exit
fi

PML_FILE=$1
SPIN_DIR=$2
IDENT=$3

SPIN_PARENT=$( dirname ${SPIN_DIR} )

NEW_SPIN_DIR=${SPIN_PARENT}/${IDENT}
cp -r ${SPIN_DIR} ${NEW_SPIN_DIR}

function run_spin() {
    cd ${NEW_SPIN_DIR}
    ./Src/spin -a ${PML_FILE}
    gcc -o pan pan.c
    ./pan -a
}

run_spin

rm -rf ${NEW_SPIN_DIR}

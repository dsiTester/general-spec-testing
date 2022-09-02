if [ $# -ne 1 ]; then
    echo "USAGE: bash $0 NUM_THREADS"
fi

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
NUM_THREADS=$1

for project in $( cut -d' ' -f3 ); do
    echo "Running DICE on ${project}..."
    bash ${SCRIPT_DIR}/run-dice-on-proj-in-parallel.sh ${project} ${NUM_THREADS}
done

# This script runs spec-to-automata program and displays a png representation of the resulting dot file.

if [ $# -lt 3 -o $# -gt 4 ]; then
    echo "Usage: $0 EVENTS_FILE FORMULA_FILE LANGUAGE"
    echo "where LANGUAGE is the formal language the formula is written in (either ltl, ere, fsm)"
    echo "ex) bash $0 sample-inputs-new/new-hasNext-events.txt sample-inputs-new/new-hasNext-formula.txt ltl"
    echo "ex) bash $0 sample-inputs-premade/premade-hasNext-events.txt sample-inputs-premade/premade-hasNext-formula.txt fsm"
    exit
fi

EVENTS_FILE=$1
FORMULA_FILE=$2
OP=$3

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
LOGICREPO_PATH=${SCRIPT_DIR}/logicrepository
# need to touch so that we can figure out which dot file we wrote to
touch ${SCRIPT_DIR}/SCRIPT_NOW
cd ${SCRIPT_DIR}

function run_program() {
    # prepare the program
    mvn package
    # run the program
    java -cp logicrepository/target/logicrepository-1.0-SNAPSHOT-jar-with-dependencies.jar com.runtimeverification.rvmonitor.logicrepository.Main ${LOGICREPO_PATH} ${EVENTS_FILE} ${FORMULA_FILE} ${OP}
}

function post_process() {
    dotfile=`find -newer ${SCRIPT_DIR}/SCRIPT_NOW -name '*.dot'`
    pngfile=`echo ${dotfile:2} | cut -d'.' -f1`
    pngfile="${pngfile}.png"

    rm ${SCRIPT_DIR}/SCRIPT_NOW

    dot -Tpng ${dotfile} > ${pngfile}
    # xdg-open ${pngfile}
}

run_program
post_process

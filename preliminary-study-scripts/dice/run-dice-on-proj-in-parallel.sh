if [ $# -ne 2 ]; then
    echo "USAGE: bash $0 PROJECT_NAME NUM_THREADS"
    exit
fi

PROJECT=$1
NUM_THREADS=$2

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname ${SCRIPT_DIR} )
GENERATED_DATA_DIR=${SCRIPT_DIR}/data/generated-data
PROJECT_DATA_DIR=${GENERATED_DATA_DIR}/${PROJECT}
LOGS_DIR=${PROJECT_DATA_DIR}/logs
WS=${PROJECT_DATA_DIR}/ws
DICE_TESTER_DIR=${SCRIPT_DIR}/dice-tester
DICE_TESTER_JAR=${DICE_TESTER_DIR}/master/target/evosuite-master-1.0.7-DICE.jar
OG_DICE_MINER_DIR=${SCRIPT_DIR}/dice-miner

if [ -d ${PROJECT_DATA_DIR} ]; then
    mv ${GENERATED_DATA_DIR} ${GENERATED_DATA_DIR}-`date +%Y-%m-%d-%H-%M-%S`
fi
mkdir -p ${LOGS_DIR}
mkdir -p ${WS}

if [ ! -d ${BASE_DIR}/dice-dsm-manual-traces ]; then
    (
        cd ${BASE_DIR}
        tar xf dice-dsm-manual-traces.tgz
    )
fi

OG_TRACES_DIR=${BASE_DIR}/dice-dsm-manual-traces/${PROJECT}

if [ ! -d ${OG_TRACES_DIR} ]; then
    (
        cd ${BASE_DIR}/dice-dsm-manual-traces
        tar xf ${PROJECT}.tgz
    )
fi

function setup_og_traces() {
    if [ ! -d ${OG_TRACES_DIR} ]; then
	echo "unpacking traces dir!"
	(
	    cd ${SCRIPT_DIR}/dev-unit-tests-dsm-traces
	    tar xf ${PROJECT}.tgz
	)
    fi
}

function setup_dice_tester_jar() {
    (
        cd ${DICE_TESTER_DIR}
        git config --global --add safe.directory ${DICE_TESTER_DIR}
        git pull
        mvn clean package -DskipTests
    ) &> ${LOGS_DIR}/gol-setup-dice-tester-jar
}

PROJECT_CLASSPATH=${SCRIPT_DIR}/${PROJECT}-code/target/classes/
SUBJECTS_LIST=${SCRIPT_DIR}/subjects.txt

function setup_project_classpath() {
    if [ ! -d ${PROJECT_CLASSPATH} ]; then
	echo "Setting up project classpath!"
	(
	    url=$( grep "${PROJECT}" ${SUBJECTS_LIST} | cut -d' ' -f1 )
	    sha=$( grep "${PROJECT}" ${SUBJECTS_LIST} | cut -d' ' -f2 )
	    git clone ${url} ${PROJECT}-code
	    cd ${PROJECT}-code
	    git checkout ${sha}
	    mvn clean compile
	) &> ${LOGS_DIR}/gol-setup-project-classpath
    fi
}

function main() {

    (
        export DICE_TESTER_JAR
        export PROJECT_CLASSPATH
        export LOGS_DIR
        export OG_TRACES_DIR
	export OG_DICE_MINER_DIR
        export WS
	export PROJECT
	export PROJECT_DATA_DIR
	export GENERATED_DATA_DIR
        ls ${OG_TRACES_DIR} | rev | cut -d. -f2- | rev | parallel -j ${NUM_THREADS} bash ${SCRIPT_DIR}/run-dice-on-proj-atomic.sh
    )
}

function postprocess() {
    generated_specs_dir=${PROJECT_DATA_DIR}/generated-specs
    mkdir -p ${generated_specs_dir}
    for f in $( find ${PROJECT_DATA_DIR}/results -name fsm.txt ); do
	f_dir=$( dirname ${f} )
	class_name=$( echo ${f} | rev | cut -d/ -f3 | rev )
	cp ${f} ${generated_specs_dir}/${class_name}.txt
	cp ${f_dir}/fsm.dot ${generated_specs_dir}/${class_name}.dot
	cp ${f_dir}/fsm.json ${generated_specs_dir}/${class_name}.json
    done
}

setup_og_traces
setup_dice_tester_jar
setup_project_classpath
main
postprocess

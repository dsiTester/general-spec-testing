if [ $# -ne 2 ]; then
    echo "USAGE: bash $0 IN_FILE FILES_DIR"
    exit
fi

IN_FILE=$1
FILES_DIR=$2
mkdir -p ${FILES_DIR}
IN_NAME=$( echo "${IN_FILE}" | rev | cut -d/ -f1 | cut -d. -f2- | rev )
SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )

function create_constraints() {
    echo "[ LOG ] Creating constraints started at "`date +%Y-%m-%d-%H-%M-%S`
    local methods_file=${FILES_DIR}/${IN_NAME}-methods.txt
    head -1 ${IN_FILE} | cut -d\{ -f2 | cut -d\} -f1 | tr -d " " | sed 's/,/\n/g' | grep -v EPSILON > ${methods_file}
    (
        cd ${SCRIPT_DIR}
        javac -cp guava-31.1-jre.jar CreateLTLConstraints.java && java -cp .:guava-31.1-jre.jar CreateLTLConstraints ${methods_file} &> ${FILES_DIR}/constraints.txt
    )
    echo "[ LOG ] Creating constraints ended at "`date +%Y-%m-%d-%H-%M-%S`
}

create_constraints


id=$1
SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname ${SCRIPT_DIR} )
DATA_DIR=${BASE_DIR}/data
CONVERSION_DIR=${BASE_DIR}/spec-to-aspect
CONVERSION_JAR=${CONVERSION_DIR}/target/spec-to-aspect-1.0-SNAPSHOT-jar-with-dependencies.jar
TEMPLATE=${SCRIPT_DIR}/StateComparisonAspect.template

SKIPS=" -Dcheckstyle.skip -Drat.skip -Denforcer.skip -Danimal.sniffer.skip -Dmaven.javadoc.skip -Dfindbugs.skip -Dwarbucks.skip -Dmodernizer.skip -Dimpsort.skip -DfailIfNoTests=false -Dpmd.skip=true -Dcpd.skip=true -Dlicense.skip"

WS=experiment-${PROJECT}-${id}
JDK_WS=experiment-${PROJECT}-${id}-jdk-weaving

cd ${SCRIPT_DIR}
url=$( grep ${PROJECT} ${SETUP_FILE} | cut -d' ' -f1 )
sha=$( grep ${PROJECT} ${SETUP_FILE} | cut -d' ' -f2 )
git clone ${url} ${WS} &> /dev/null
cd ${WS}
git checkout ${sha} &> /dev/null
# FIXME: write an actual extension for this
cp ${SCRIPT_DIR}/temp-poms/${PROJECT}-pom.xml pom.xml
# treat-special for project
bash ${SCRIPT_DIR}/project_treat_special.sh ${PROJECT}

PACKAGE=$( find src/main/java -name *.java | cut -d/ -f4- | rev | cut -d/ -f2- | rev | sed 's:/:.:g' | sort -u | head -1 )

methods=$( grep ^${id} ${SMAPS_FILE} | cut -d' ' -f2- | cut -d' ' -f-2 | sed 's/ /,/g' )
echo "===running spec ${id}"
#methods=$( echo "${line}" | cut -d, -f2-3 )

tst=$( grep ^${id} ${SMAPS_FILE} | cut -d' ' -f4 | rev | cut -d, -f1 | rev )
if [ "${tst}" != "all-tests" ]; then
    tstOpt="-Dtest=${tst}"
else
    tstOpt=""
fi

name=${NAME}${id}
path=$( echo ${PACKAGE} | sed 's:.:/:g' )
ASPECT_FILE=src/main/java/${path}/${name}.aj

ORIGINAL_OUTPUT_FILE=${LOGS_DIR}/gol-state-check-${id}

source ${SCRIPT_DIR}/state_helper.sh

function prepare_aspect_file() {
    local set_get_str=$1
    local aspect_file=$2
    local object_type_mode=$3
    set_str=$( echo "${set_get_str}" | sed 's/MODE/set/g' | sed 's/\&/\\&/g')
    get_str=$( echo "${set_get_str}" | sed 's/MODE/get/g' | sed 's/\&/\\&/g' )
    cp ${TEMPLATE} ${aspect_file}
    converted_methods=$( java -jar ${CONVERSION_JAR} DUMP "${methods}" )
    a=$( echo "${converted_methods}" | cut -d, -f1 | sed 's/;/,/g' )
    b=$( echo "${converted_methods}" | cut -d, -f2 | sed 's/;/,/g' )
    sed -i "s/METHOD_A_SIG/${a}/g" ${aspect_file}
    sed -i "s/METHOD_B_SIG/${b}/g" ${aspect_file}
    sed -i "s/PACKAGE/${PACKAGE}/g" ${aspect_file}
    sed -i "s/NAME/${name}/g" ${aspect_file}
    sed -i "s/GETS/${get_str}/g" ${aspect_file}
    sed -i "s/SETS/${set_str}/g" ${aspect_file}
    sed -i "s/OBJECT_TYPE_MODE/${object_type_mode}/g" ${aspect_file}
    sed -i 's:\$:.:g' ${aspect_file}
    # treat_special for certain aspects
    bash ${SCRIPT_DIR}/specs_treat_special.sh ${PROJECT} ${id} ${aspect_file}
}

function run_first_pass() {
    prepare_aspect_file "MODE(* *)" ${ASPECT_FILE} true
    ( set -o xtrace ; time mvn test-compile ${tstOpt} ${SKIPS} ; set +o xtrace ) &> ${LOGS_DIR}/gol-test-compile-${id}
    ( set -o xtrace ; time timeout 15m mvn surefire:test ${tstOpt} ${SKIPS} ; set +o xtrace ) &> ${ORIGINAL_OUTPUT_FILE}
    mv ${ASPECT_FILE} ${ASPECTS_DIR}
}

# function make_set_get_string() {
#     local typ=$1
#     local treat_special_file=${SCRIPT_DIR}/aspect_excludes.csv
#     local treat_special_log=${LOGS_DIR}/gol-treat-special-${id}
#     if grep -q ^"${typ}," ${treat_special_file} ; then
#         grep ^"${typ}," ${treat_special_file} | cut -d, -f2
#         echo "${typ}" >> ${treat_special_log}
#     else
#         typ=$( echo "${typ}" | sed 's/\$/./g' )
#         typ="MODE(* ${typ}.*)"
#         echo "${typ}"
#     fi
# }

# function create_sets_and_gets() {
#     acc_str=""
#     common_types=$( comm -12 <( grep "METHOD_A,Specific type: " ${ORIGINAL_OUTPUT_FILE} | cut -d' ' -f3 | sort -u ) <( grep "METHOD_B,Specific type: " ${ORIGINAL_OUTPUT_FILE} | cut -d' ' -f3 | sort -u ) | cut -d, -f1 | sort -u )
#     for typ in $( echo "${common_types}" | grep -v "\[L" | grep "java." ); do # excluding all arrays since we can't track them anyways.
#         new_pointcut=$( make_set_get_string "${typ}" )
#         if [ "${new_pointcut}" == "" ]; then
#             continue
#         fi
#         if [ "${acc_str}" == "" ]; then
#             acc_str="${new_pointcut}"
#         else
#             acc_str="${acc_str} || ${new_pointcut}"
#         fi
#     done
#     echo "${acc_str}"
# }

function run_additional_pass() {
    common_types=$( comm -12 <( grep "METHOD_A,Specific type: " ${ORIGINAL_OUTPUT_FILE} | cut -d' ' -f3 | sort -u ) <( grep "METHOD_B,Specific type: " ${ORIGINAL_OUTPUT_FILE} | cut -d' ' -f3 | sort -u ) | cut -d, -f1 | sort -u )
    new_sets_and_gets=$( create_sets_and_gets ${common_types} ${LOGS_DIR}/gol-treat-special-${id} )
    state_status=$( compute_share_state ${ORIGINAL_OUTPUT_FILE} | cut -d, -f1 )
    jdk_weave_log=${LOGS_DIR}/gol-weave-jdk-${id}
    run_w_jdk_log=${LOGS_DIR}/gol-jdk-run-${id}
    run_w_jdk_compile_log=${LOGS_DIR}/gol-jdk-run-test-compile-${id}
    if [[ "${new_sets_and_gets}" == "" || "${state_status}" == "yes" ]]; then # if we already share state, then 
        echo "NO ADDITIONAL JDK CHECKS TO BE MADE!"
        # just in case we want to know which reason for debugging purposes
        echo "Pointcuts: ${new_sets_and_gets}"
        echo "Status: ${state_status}"
    else
        echo "RUNNING WITH JDK WEAVING..."
        echo "Pointcuts to be added to JDK run: ${new_sets_and_gets}"
        # (1) weave the JDK wrt the new sets and gets we want to check
        cp -r ${SCRIPT_DIR}/weave-jdk-template ${SCRIPT_DIR}/${JDK_WS}
        cd ${SCRIPT_DIR}/${JDK_WS}
        prepare_aspect_file "${new_sets_and_gets}" src/main/java/edu/cornell/${name}.aj false
        ( set -o xtrace ; time mvn clean package ${SKIPS} ; set +o xtrace ) &> ${jdk_weave_log}
        if [ ! -f target/weave-jdk-1.0-SNAPSHOT-jar-with-dependencies.jar ]; then
            echo "JDK WEAVING ERROR"
            return
        fi
        # (2) run mvn test with the new weaved JDK
        cd ${SCRIPT_DIR}/${WS}
        prepare_aspect_file "${new_sets_and_gets}" ${ASPECT_FILE} false
        ( set -o xtrace ; time mvn clean test-compile ${tstOpt} -DargLine="-Xbootclasspath/p:${SCRIPT_DIR}/${JDK_WS}/target/weave-jdk-1.0-SNAPSHOT-jar-with-dependencies.jar" ${SKIPS} ; set +o xtrace ) &> ${run_w_jdk_compile_log}
        ( set -o xtrace ; time mvn surefire:test ${tstOpt} -DargLine="-Xbootclasspath/p:${SCRIPT_DIR}/${JDK_WS}/target/weave-jdk-1.0-SNAPSHOT-jar-with-dependencies.jar" ${SKIPS} ; set +o xtrace ) &> ${run_w_jdk_log}
        mv ${ASPECT_FILE} ${ASPECTS_DIR}/${name}JDK.aj
        echo "Done running additional JDK run"
    fi
}

( run_first_pass
run_additional_pass ) &> ${LOGS_DIR}/gol-wrapper-${id}

rm -rf ${SCRIPT_DIR}/${WS}
rm -rf ${SCRIPT_DIR}/${JDK_WS}

# This script runs all granularities of DSI on the list of projects given.
if [ $# -lt 3 -o $# -gt 5 ]; then
    echo "Usage: $0 PROJECT_LIST OPTION BUILD_OP BACKUP_OP [PART_FILE]"
    echo "where OPTION is one of -allTests -allTestsNoDSI -testClasses
    -testClassesNoDSI -testMethods -testMethodsNoDSI
    -allTests_testClasses -allTests_testMethods -testClasses_testMethods
    -allTests@testClasses -allTests@testMethods -testClasses@allTests
    -testClasses@testMethods -testMethods@testClasses -testMethods@allTests
    -testClasses@allTests_testClasses -testClasses@allTests_testMethods
    -testMethods@allTests_testClasses -testMethods@allTests_testMethods
    -testMethods@testClasses_testMethods"
    echo "where options with @ in them are denoted -testSpec@testValidate"
    echo "where options with _ in them are for selection"
    echo "i.e. -testClasses@allTests_testClasses is, mine specs from testClasses, validate with allTests with test class selection"
    echo "if BUILD_OP is -build, DSI will be built and if BUILD_OP is -noBuild, then DSI will not be built"
    echo "if BACKUP_OP is -backup, generated-data will be backed up and if BACKUP_OP is -noBackup, then no backup dir will be made"
    echo "and PART_FILE is the file containing the specific testClasses/testMethods for a partial run (NEEDS TO BE FULL PATH)"
    echo "ex) $0 project-list.txt -testClasses -noBuild"
    exit
fi

valid_options="-allTests|-allTestsNoDSI|-testClasses|-testClassesNoDSI|-testMethods|-testMethodsNoDSI|-allTests@testClasses|-allTests@testMethods|-testClasses@allTests|-testClasses@testMethods|-testMethods@testClasses|-testMethods@allTests|-allTests_testClasses|-allTests_testMethods|-testClasses_testMethods|-testClasses@allTests_testClasses|-testClasses@allTests_testMethods|-testMethods@allTests_testClasses|-testMethods@allTests_testMethods|-testMethods@testClasses_testMethods"

if [[ ! $2 =~ ^(${valid_options}) ]]; then
    echo "invalid option!"
    exit
fi

PROJ_LIST=$1
OPTION=$2
BUILD_OP=$3
BACKUP_OP=$4
PART_FILE=$5

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
DSI_DIR=$( cd $( dirname ${SCRIPT_DIR} ) && pwd )
DATA_DIR=${SCRIPT_DIR}/data
SUBJECTS_DIR=${SCRIPT_DIR}/data/subjects
WORKSPACES_DIR=${SCRIPT_DIR}/data/workspaces
RESULT_DIR=${SCRIPT_DIR}/data/generated-data
RUN_INFO_DIR=${RESULT_DIR}/run-info
MOP_CHANGER=${SCRIPT_DIR}/pom-modify/modify-project.sh
BUCKETING_SCRIPT=${SCRIPT_DIR}/bucket-results.sh
BREAKDOWN_GENERATOR=${SCRIPT_DIR}/generate-bucket-breakdown.sh
PROCESS_AGGREGATE=${SCRIPT_DIR}/process-aggregate-results.sh
DEFAULT_BUILD_DIR_NAME=target
COMP_SKIPS=" -Dcheckstyle.skip -Drat.skip -Denforcer.skip -Danimal.sniffer.skip -Dmaven.javadoc.skip -Dfindbugs.skip -Dwarbucks.skip -Dmodernizer.skip -Dimpsort.skip -DfailIfNoTests=false -Dpmd.skip=true -Dcpd.skip=true -Dlicense.skip"
SKIPS=" ${COMP_SKIPS} -Dmaven.main.skip" # take out maven.main.skip?
EXCLUDES_DIR=${SCRIPT_DIR}/excludes

TIMESTAMP=`date +%Y-%m-%d-%H-%M-%S`
proj_list_name=$( echo "${PROJ_LIST}" | rev | cut -d'/' -f1 | cut -d'.' -f2- | rev ) 
RUN_INFO_FILE=${RUN_INFO_DIR}/${proj_list_name}${OPTION}-${TIMESTAMP}-run.info


# How many parallel instances to run
PARALLEL_COUNT=16


function check_project_clone() {
    local url=$1
    local proj_name=$2
    local proj_dir=${SUBJECTS_DIR}/${proj_name}
    if [ ! -d  ${proj_dir} ]; then
        echo "****Cloning ${proj_name} to ${proj_dir}"
        git clone ${url} ${proj_dir}
    fi
}

function prepare_workspaces() {
    local project=$1
    local dest_dir=${WORKSPACES_DIR}/${project}
    if [ -d ${dest_dir} ]; then
        echo "${dest_dir} exists... not cloning"
    else
        cp -r ${SUBJECTS_DIR}/${project} ${WORKSPACES_DIR}/${project}
    fi
}

function write_meta_info() {
    mkdir -p ${WORKSPACES_DIR}
    # local info_file=
    >${RUN_INFO_FILE}
    echo "Experiment Started at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
    echo "Script: $0" >> ${RUN_INFO_FILE}
    echo "REPO-VERSION: "$(git rev-parse HEAD) >> ${RUN_INFO_FILE}
    echo "PROJECTS-RUN: " >> ${RUN_INFO_FILE}
    echo >> ${RUN_INFO_FILE}
    projects=$(cat ${PROJ_LIST} | cut -d' ' -f3)
    printf "%s\n" "${projects[@]}" >> ${RUN_INFO_FILE}
}

function write_end_time() {
    echo "Experiment ended at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
}

function make_global_dirs() {
    mkdir -p ${RESULT_DIR}
    mkdir -p ${RUN_INFO_DIR}
    mkdir -p ${SUBJECTS_DIR}
}

function backup_old_results() {
    if [ -d ${RESULT_DIR} ]; then
        echo "****Moving old results"
        mv ${RESULT_DIR} ${RESULT_DIR}-`date +%Y-%m-%d-%H-%M-%S`
    fi
}

function make_project_dirs() {
    local project=$1
    local option=$2
    # TODO: make this uniform (Done) and test
    mkdir -p ${RESULT_DIR}/${project}/${option}/logs
    mkdir -p ${RESULT_DIR}/${project}/${option}/ws
}

function prepare_project_workspace() {
    local project=$1
    local option=$2
    local project_version=$3
    git clone ${SUBJECTS_DIR}/${project} ${RESULT_DIR}/${project}/${option}/ws
    (
        cd ${RESULT_DIR}/${project}/${option}/ws
        git checkout ${project_version}
        bash ${SCRIPT_DIR}/treat_special.sh ${project}
    ) &> ${RESULT_DIR}/${project}/${option}/logs/gol-checkout-${project_version}
}

function prepare_dsi() {
    bash ${SCRIPT_DIR}/install_dsi_plugin.sh
}

function change_pom_no_listener() {
    # TODO: change to the extension
    if [[ ! ${OPTION} == *"NoDSI"* ]]; then
        echo "change_pom_no_listener: Adding DSI Plugin to the pom.xml files..."
        ${MOP_CHANGER} `pwd` ${DSI_DIR}/dsi-base/dist/ruleverify.jar
    else
        echo "change_pom_no_listener: NoDSI option, not adding dsi to pom..."
    fi
}

function change_pom_with_listener() {
    ${MOP_CHANGER} `pwd` ${SCRIPT_DIR}/dsi-base/dist/ruleverify.jar -AL
}

function test_compile() {
    local gol_dir=$1
    buildDir=""
    if [[ ${OPTION} == *"NoDSI"* ]]; then
	echo "test_compile: NoDSI option/testMethods option, not adding buildDirectory to args..."
    else
        echo "test_compile: Adding -DbuildDirectory=${DEFAULT_BUILD_DIR_NAME} to args..."
        buildDir="-DbuildDirectory=${DEFAULT_BUILD_DIR_NAME}"
    fi
    (time mvn clean test-compile ${COMP_SKIPS} ${buildDir} ) &> ${gol_dir}/compile-log
}

function process_output() {
    local project_location=$1
    local gran=$2
    local bucket_loc=$3
    local surefire_rep_loc=$4
    local traces_loc=traces

    echo "process_output bucketing START TIME: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
    # java implements most of the bucketing, and outputs it to gol/bucket-results
    mv gol/bucket-results ${project_location}/${bucket_loc}
    # This ASM error should be bucketed to unknown, but the Mojo does not currently handle this feature.
    for file in $( grep -ilr "Exception while transforming:java.lang.StringIndexOutOfBoundsException: String index out of range: -1" gol/bucket-results/ ); do name=$( echo ${file} | rev | cut -d'/' -f1 | rev ); path=$( echo ${file} | rev | cut -d'/' -f3- | rev ); mv ${file} ${path}/unknown/${name}; done
    echo "process_output bucketing END TIME: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
    echo "process_output generating breakdown START TIME: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
    bash ${BREAKDOWN_GENERATOR} ${project_location}/${bucket_loc} ${project_location}/logs ${gran}
    echo "process_output generating breakdown END TIME: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}

    # FIXME: uncomment below to compress dsi-outputs
    # (
    #     cd ${project_location}
    # # postprocessing... compress .log files and remove the original
    # tar -cvzf dsi-outputs.tgz ${bucket_loc}
    # rm -rf ${bucket_loc}
    # )

    tar -cvzf ${project_location}/traces.tgz ${traces_loc}

    (
	cd ${surefire_rep_loc}/..
	mv surefire-reports ${project_location}/surefire-reports
	# FIXME: uncomment below to compress surefire-reports
        # tar -cvzf ${project_location}/surefire-reports.tgz ${project_location}/surefire-reports 
        # rm -rf ${project_location}/surefire-reports
    )

}

function run_all_tests() {
    local project_location=$1
    local test_validate_option=$2
    local selection_option=$3
    echo "running all tests, validating with ${test_validate_option}..."
    local log_file=${project_location}/logs/gol-all-tests # our log file
    local mining_log_file=${project_location}/logs/gol-mine-all-tests
    local surefire_rep_loc=${project_location}/ws/gol/surefire-reports
    local bucket_loc=dsi-outputs
    local methodnames="dsi-target/testnames.txt"
    local SPEC_FILE=${project_location}/ws/gol/surefire-reports/all-tests/spec-order.txt
    local ORIGINAL_SPEC_FILE=${project_location}/../surefire-reports/all-tests/spec-order.txt
        
    if [ ${selection_option} != "NO_SELECTION" ]; then
        echo "Will run selection, first getting method names..."
        echo "Getting method traces for selection START TIME: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
	get_method_traces ${project_location}
        echo "changing back pom..."
	change_pom_no_listener # putting back dsi into pom
	echo "Getting method traces for selection END TIME: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
    fi

    # check if test method spec order file exists
    if [ -f "${TESTCLASS_SPEC_FILE}" ]; then
        echo "test class spec order exists!! not mining." | tee -a ${mining_log_file}
        cp ${ORIGINAL_SPEC_FILE} ${SPEC_FILE}
    else
        echo "no spec order exists... mining." | tee -a ${mining_log_file}
        echo "MINING START TIME: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
        ( time mvn dsi:mine ${COMP_SKIPS} -DbuildDirectory=${DEFAULT_BUILD_DIR_NAME} ) &> ${mining_log_file}
        echo "MINING END TIME: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
    fi

    if [ ${test_validate_option} == "allTests" ]; then
	echo "RUNNING DSI START TIME: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
	( time mvn dsi:run-dsi ${COMP_SKIPS} -DbuildDirectory=${DEFAULT_BUILD_DIR_NAME} -DspecFile=${SPEC_FILE} -Dselection=${selection_option} ) &> ${log_file}
	echo "RUNNING DSI END TIME: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
        breakdown_level="allTests"
    elif [ ${test_validate_option} == "testClasses" ]; then
        ( time mvn dsi:collect -DbuildDirectory=${DEFAULT_BUILD_DIR_NAME} ${COMP_SKIPS} ) &> ${log_file}
        echo "running test class verification in parallel..."
        find -name *Test*.java | xargs -n 1 basename | cut -d. -f1 | sed "s/$/,${selection_option}/g" | parallel -j ${PARALLEL_COUNT} bash ${SCRIPT_DIR}/run-all-tests.sh
        breakdown_level="allTests@testClassLevel"
    elif [ ${test_validate_option} == "testMethods" ]; then
        if [ ${selection_option} == "NO_SELECTION" ]; then
	    echo "getting method traces..."
	    get_method_traces ${project_location}
            echo "changing back pom..."
	    change_pom_no_listener # putting back dsi into pom
        fi
        echo "running test method verification in parallel..."
	cat ${methodnames} | sed "s/$/,${selection_option}/g" | parallel -j ${PARALLEL_COUNT} bash ${SCRIPT_DIR}/run-all-tests.sh
        breakdown_level="allTests@testMethodLevel"
    fi
    process_output ${project_location} ${breakdown_level} ${bucket_loc} ${surefire_rep_loc}
}

function run_all_tests_no_dsi() {
    local project_location=$1
    local log_file=${project_location}/logs/gol-all-tests-no-dsi

    echo "Running All Tests Granularity with no DSI..." > ${log_file}
    (time mvn surefire:test -fn ${COMP_SKIPS}) &> ${log_file}
}

function run_test_classes() {
    local project_location=$1
    local test_validate_option=$2
    local selection_option=$3
    local part_file=$4 # (optional) file specifying which classes to run for a partial run, if running partially
    local bucket_loc=dsi-outputs
    local surefire_rep_loc=${project_location}/ws/gol/surefire-reports # surefire_rep_loc is the (post-dsi) location of the surefire reports.
    # run DSI on all test classes
    mvn dsi:clean
    echo "starting to run test classes in parallel..."
    if [ ${test_validate_option} == "testClasses" ]; then
        breakdown_level="testClassLevel"
    elif [ ${test_validate_option} == "allTests" ]; then
        breakdown_level="testClassLevel@allTests"
    elif [ ${test_validate_option} == "testMethods" ]; then
	echo "getting method traces..."
	get_method_traces ${project_location}
        echo "changing back pom..."
	change_pom_no_listener # putting back dsi into pom
	breakdown_level="testClassLevel@testMethodLevel"
    fi
    # FIXME: need to make sure that we won't be running this routine twice, check above...
    if [ ${selection_option} != "NO_SELECTION" ]; then
        echo "Will run selection, first getting method names..."
        echo "getting method traces..."
	get_method_traces ${project_location}
        echo "changing back pom..."
	change_pom_no_listener # putting back dsi into pom
    fi
    if [ ! -z "${part_file}" ]; then
	echo "partial run file: ${part_file}"
        cat ${part_file} | sed "s/$/,${test_validate_option},${selection_option}/g" |  parallel -j ${PARALLEL_COUNT} bash ${SCRIPT_DIR}/run-class.sh
    else
        # TODO: this will break for multi-module project
        # TODO: look into surefire's excludes so that we don't run tests that developers want to skip
        find -name *Test*.java | xargs -n 1 basename | cut -d. -f1 | sed "s/$/,${test_validate_option},${selection_option}/g" | parallel -j ${PARALLEL_COUNT} bash ${SCRIPT_DIR}/run-class.sh
    fi
    echo "ending run of test classes in parallel..."
    process_output ${project_location} ${breakdown_level} ${bucket_loc} ${surefire_rep_loc}
}

function run_test_classes_no_dsi() {
    local project_location=$1

    echo "starting to run test classes (no dsi) in parallel..."
    find -name *Test*.java | xargs -n 1 basename | cut -d. -f1 | parallel -j ${PARALLEL_COUNT} bash ${SCRIPT_DIR}/run-class-no-dsi.sh
    echo "ending run of test classes (no dsi) in parallel..."
}

function get_method_traces() {
    local project_location=$1
    local log_file=${project_location}/logs/gol-collect-test-method-names
    # modify the pom to add listener...
    change_pom_with_listener

    # dsi:collect puts traces in directory called "traces"
    # mvn dsi:collect ${SKIPS} > ${log_file} 2>&1
    mvn test ${SKIPS} > ${log_file} 2>&1

    echo "done with collecting traces, restoring previous pom..." >> ${log_file}
    git checkout pom.xml # restoring original pom...
}

function run_test_methods() {
    local project_location=$1
    local test_validate_option=$2
    local selection_option=$3
    local part_file=$4 # file specifying which methods to run for a partial run, if running partially
    local bucket_loc=dsi-outputs
    local surefire_rep_loc=${project_location}/ws/gol/surefire-reports
    local traces="traces"
    local methodnames="dsi-target/testnames.txt"

    if [ ! -z "${part_file}" ]; then
	echo "partial run file: ${part_file}"
	echo "starting to run test methods in parallel..."
        cat ${part_file} | sed "s/$/,${test_validate_option},${selection_option}/g" | parallel -j ${PARALLEL_COUNT} bash ${SCRIPT_DIR}/run-method.sh
    else
        echo "getting method traces..."
	get_method_traces ${project_location}

        echo "changing back pom..."
	change_pom_no_listener # putting back dsi into pom

	echo "starting to run test methods in parallel..."
	cat ${methodnames} | sed "s/$/,${test_validate_option},${selection_option}/g" | parallel -j ${PARALLEL_COUNT} bash ${SCRIPT_DIR}/run-method.sh
    fi

    echo "ending run of test methods in parallel..."
    if [ ${test_validate_option} == "testMethods" ]; then
        breakdown_level="testMethodLevel"
    elif [ ${test_validate_option} == "testClasses" ]; then
        breakdown_level="testMethodLevel@testClassLevel"
    elif [ ${test_validate_option} == "allTests" ]; then
        breakdown_level="testMethodLevel@allTests"
    fi
    process_output ${project_location} ${breakdown_level} ${bucket_loc} ${surefire_rep_loc}
}

function run_test_methods_no_dsi() {
    local project_location=$1
    local traces="traces"
    local methodnames="dsi-target/testnames.txt"

    get_method_traces ${project_location} # original pom will be restored by this point
    echo "starting to run test methods in parallel..."
    test_compile ${project_location}/logs # but we need to do test compile again
    cat ${methodnames} | parallel -j ${PARALLEL_COUNT} bash ${SCRIPT_DIR}/run-method-no-dsi.sh
    echo "ending run of test methods (no dsi) in parallel..."
}

function main() {
    local option=$(echo ${OPTION} | cut -d- -f2)
    while read url sha name
    do
        [[ $url =~ ^#.* ]] && continue # skips projects that have hash in front
        [[ -z "$url" ]] && continue # skip newline
        echo ===============${name}
        check_project_clone ${url} ${name}
        make_project_dirs ${name} ${option}
        prepare_project_workspace ${name} ${option} ${sha}
        # go to workspace...
        (
            export COMP_SKIPS
            export SKIPS
            cd ${RESULT_DIR}/${name}/${option}/ws
            change_pom_no_listener # only changes pom when option doesn't contain NoDSI
            test_compile ${RESULT_DIR}/${name}/${option}/logs
            [[ "${OPTION}" == "-allTests" ]] && run_all_tests ${RESULT_DIR}/${name}/${option} "allTests" "NO_SELECTION" && continue
	    [[ "${OPTION}" == "-allTests@testClasses" ]] && run_all_tests ${RESULT_DIR}/${name}/${option} "testClasses" "NO_SELECTION" && continue
	    [[ "${OPTION}" == "-allTests@testMethods" ]] && run_all_tests ${RESULT_DIR}/${name}/${option} "testMethods" "NO_SELECTION" && continue
            #-allTests%testClasses|-allTests%testMethods|-testClasses%testMethods
            [[ "${OPTION}" == "-allTests_testClasses" ]] && run_all_tests ${RESULT_DIR}/${name}/${option} "allTests" "ALL_TESTS_TEST_CLASSES" && continue
            [[ "${OPTION}" == "-allTests_testMethods" ]] && run_all_tests ${RESULT_DIR}/${name}/${option} "allTests" "ALL_TESTS_TEST_METHODS" && continue
            [[ "${OPTION}" == "-allTestsNoDSI" ]] && run_all_tests_no_dsi ${RESULT_DIR}/${name}/${option} && continue
            [[ "${OPTION}" == "-testClasses" ]] && run_test_classes ${RESULT_DIR}/${name}/${option} "testClasses" "NO_SELECTION" ${PART_FILE} && continue
            [[ "${OPTION}" == "-testClasses@allTests" ]] && run_test_classes ${RESULT_DIR}/${name}/${option} "allTests" "NO_SELECTION" ${PART_FILE} && continue
            [[ "${OPTION}" == "-testClasses@allTests_testClasses" ]] && run_test_classes ${RESULT_DIR}/${name}/${option} "allTests" "CROSS_ALL_TESTS_TEST_CLASSES" ${PART_FILE} && continue
            [[ "${OPTION}" == "-testClasses@allTests_testMethods" ]] && run_test_classes ${RESULT_DIR}/${name}/${option} "allTests" "CROSS_ALL_TESTS_TEST_METHODS" ${PART_FILE} && continue
	    [[ "${OPTION}" == "-testClasses@testMethods" ]] && run_test_classes ${RESULT_DIR}/${name}/${option} "testMethods" "NO_SELECTION" ${PART_FILE} && continue
	    [[ "${OPTION}" == "-testClasses_testMethods" ]] && run_test_classes ${RESULT_DIR}/${name}/${option} "testClasses" "TEST_CLASSES_TEST_METHODS" ${PART_FILE} && continue
            [[ "${OPTION}" == "-testClassesNoDSI" ]] && run_test_classes_no_dsi ${RESULT_DIR}/${name}/${option} && continue
            [[ "${OPTION}" == "-testMethods" ]] && run_test_methods ${RESULT_DIR}/${name}/${option} "testMethods" "NO_SELECTION" ${PART_FILE} && continue
            [[ "${OPTION}" == "-testMethods@testClasses" ]] && run_test_methods ${RESULT_DIR}/${name}/${option} "testClasses" "NO_SELECTION" ${PART_FILE} && continue
            [[ "${OPTION}" == "-testMethods@allTests" ]] && run_test_methods ${RESULT_DIR}/${name}/${option} "allTests" "NO_SELECTION" ${PART_FILE} && continue
            [[ "${OPTION}" == "-testMethods@allTests_testClasses" ]] && run_test_methods ${RESULT_DIR}/${name}/${option} "allTests" "CROSS_ALL_TESTS_TEST_CLASSES" ${PART_FILE} && continue
            [[ "${OPTION}" == "-testMethods@allTests_testMethods" ]] && run_test_methods ${RESULT_DIR}/${name}/${option} "allTests" "CROSS_ALL_TESTS_TEST_METHODS" ${PART_FILE} && continue
            [[ "${OPTION}" == "-testMethods@testClasses_testMethods" ]] && run_test_methods ${RESULT_DIR}/${name}/${option} "testClasses" "CROSS_TEST_CLASSES_TEST_METHODS" ${PART_FILE} && continue
            [[ "${OPTION}" == "-testMethodsNoDSI" ]] && run_test_methods_no_dsi ${RESULT_DIR}/${name}/${option} && continue
        )
        # NOTE: uncomment below to remove the workspace once we're done using it, if need to save on space
        # rm -rf ${RESULT_DIR}/${name}/${option}/ws
    done < ${PROJ_LIST}
}

if [ "${BACKUP_OP}" == "-backup" ]; then
    backup_old_results
fi
make_global_dirs
write_meta_info
if [ "${BUILD_OP}" != "-noBuild" ]; then
    prepare_dsi
fi
main
write_end_time

# NOTE: OPTION starts with a dash
# cp ${WORKSPACES_DIR}/${TIMESTAMP}-run.info ${RUN_INFO_DIR}/${proj_list_name}${OPTION}-run.info

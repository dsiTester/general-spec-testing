# This script runs all granularities of DSI on the list of projects given.
if [ $# -lt 4 -o $# -gt 6 ]; then
    echo "Usage: $0 IDENT PROJECT_FILE SMAP NUM_THREADS"
    exit
fi


IDENT=$1
PROJECT_FILE=$2
SMAP=$3
PARALLEL_COUNT=$4

OPTION=dsiAllGranularities

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
DSI_DIR=$( cd $( dirname ${SCRIPT_DIR} ) && pwd )
DATA_DIR=${SCRIPT_DIR}/data
SUBJECTS_DIR=${SCRIPT_DIR}/data/subjects
WORKSPACES_DIR=${SCRIPT_DIR}/data/workspaces
RESULT_DIR=${SCRIPT_DIR}/data/generated-data
RUN_INFO_DIR=${RESULT_DIR}/run-info
MOP_CHANGER=${SCRIPT_DIR}/pom-modify/modify-project.sh
DEFAULT_BUILD_DIR_NAME=target
COMP_SKIPS=" -Dcheckstyle.skip -Drat.skip -Denforcer.skip -Danimal.sniffer.skip -Dmaven.javadoc.skip -Dfindbugs.skip -Dwarbucks.skip -Dmodernizer.skip -Dimpsort.skip -DfailIfNoTests=false -Dpmd.skip=true -Dcpd.skip=true -Dlicense.skip"
SKIPS=" ${COMP_SKIPS} -Dmaven.main.skip" # take out maven.main.skip?
EXCLUDES_DIR=${SCRIPT_DIR}/excludes

TIMESTAMP=`date +%Y-%m-%d-%H-%M-%S`
proj_list_name=$( echo "${PROJ_LIST}" | rev | cut -d'/' -f1 | cut -d'.' -f2- | rev )
RUN_INFO_FILE=${RUN_INFO_DIR}/${IDENT}-dsiPlus-${OPTION}-${TIMESTAMP}-run.info
MINING_OP="-noMining"

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
    echo "Script: $0 $@" >> ${RUN_INFO_FILE}
    echo "REPO-VERSION: "$(git rev-parse HEAD) >> ${RUN_INFO_FILE}
    echo "RUNNING ADDITIONAL DSI FOR COMPLEX AUTOMATON: ${IDENT}" >> ${RUN_INFO_FILE}
}

function write_end_time() {
    echo "Experiment ended at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
}

function make_global_dirs() {
    mkdir -p ${RESULT_DIR}
    mkdir -p ${RUN_INFO_DIR}
    mkdir -p ${SUBJECTS_DIR}
}

function backup_old_results {

    if [ -d ${RESULT_DIR}/${IDENT} ]; then # if this automaton already exists, then we're going to move the old results
        echo "****Moving old results"
        mv ${RESULT_DIR} ${RESULT_DIR}-`date +%Y-%m-%d-%H-%M-%S`
    fi
}

function make_project_dirs() {
    local project=$1
    local option=$2
    # TODO: make this uniform (Done) and test
    mkdir -p ${RESULT_DIR}/${IDENT}/${option}/logs
    mkdir -p ${RESULT_DIR}/${IDENT}/${option}/ws
}

function prepare_project_workspace() {
    local project=$1
    local option=$2
    local project_version=$3
    git clone ${SUBJECTS_DIR}/${project} ${RESULT_DIR}/${IDENT}/${option}/ws
    (
        cd ${RESULT_DIR}/${IDENT}/${option}/ws
        git checkout ${project_version}
        bash ${SCRIPT_DIR}/treat_special.sh ${project}
    ) &> ${RESULT_DIR}/${IDENT}/${option}/logs/gol-checkout-${project_version}
}

function prepare_dsi() {
    bash ${SCRIPT_DIR}/install_dsi_plugin.sh
}

function change_pom_no_listener() {
    # TODO: change to the extension
    echo "change_pom_no_listener: Adding DSI Plugin to the pom.xml files..."
    ${MOP_CHANGER} `pwd` ${DSI_DIR}/dsi-base/dist/ruleverify.jar
}

function change_pom_with_listener() {
    ${MOP_CHANGER} `pwd` ${SCRIPT_DIR}/dsi-base/dist/ruleverify.jar -AL
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

function test_compile() {
    local gol_dir=$1
    buildDir=""
    echo "test_compile: Adding -DbuildDirectory=${DEFAULT_BUILD_DIR_NAME} to args..."
    buildDir="-DbuildDirectory=${DEFAULT_BUILD_DIR_NAME}"
    echo "test_compile started at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
    (time mvn clean test-compile ${COMP_SKIPS} ${buildDir} ) &> ${gol_dir}/compile-log
    echo "test_compile started at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
}

function surefire_test() {
    local gol_dir=$1
    buildDir=""
    echo "surefire_test: Adding -DbuildDirectory=${DEFAULT_BUILD_DIR_NAME} to args..."
    buildDir="-DbuildDirectory=${DEFAULT_BUILD_DIR_NAME}"
    for i in $( seq 1 5 ); do
        echo "surefire:test run ${i} started at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
        ( time mvn surefire:test ${SKIPS} ${buildDir} ) &> ${gol_dir}/gol-baseline-run-${i}
        echo "surefire:test run ${i} ended at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
    done
}

function process_output() {
    local project_location=$1
    local surefire_rep_dir=${project_location}/ws/gol/surefire-reports
    local bucket_dir=${project_location}/ws/gol/bucket-results
    local dsi_results_dir=${project_location}/ws/gol/dsi-results
    local results_dir=${project_location}/ws/gol/results
    local traces_dir=traces
    local summary_dir=${results_dir}/summaries
    local total_true_specs_file=${results_dir}/total-true-specs.txt
    local total_spurious_specs_file=${results_dir}/total-spurious-specs.txt
    local total_unknown_specs_file=${results_dir}/total-unknown-specs.txt
    local total_error_specs_file=${results_dir}/total-error-specs.txt
    local intersection_file=${results_dir}/category-intersection.txt
    local tMap_dir=test-to-spec-maps

    # remove tmp files created by dsi
    rm ${project_location}/ws/pt-*.tmp

    echo "Retrieving tMap for better access..."
    mv ${tMap_dir} ${project_location}/test-to-spec-maps

    echo "Intersection between true and spurious/unknown specs: " > ${intersection_file}
    comm -12 <( cat ${results_dir}/true-specs/* | cut -d' ' -f2- | sort -u ) <( cat ${results_dir}/spurious-specs/* ${results_dir}/unknown-specs/* ${results_dir}/error-specs/* | cut -d' ' -f2- | sort -u ) >> ${intersection_file}
    # get total true specs
    comm -23 <( cat ${results_dir}/true-specs/* | cut -d' ' -f2- | sort -u ) <( cat ${results_dir}/spurious-specs/* ${results_dir}/unknown-specs/* ${results_dir}/error-specs/* | cut -d' ' -f2- | sort -u ) > ${total_true_specs_file}
    # if it has been deemed error at least once, we haven't been able to fully evaluate the spec, so we need to reevaluate it...
    cat ${results_dir}/error-specs/* | cut -d' ' -f2- | sort -u > ${total_error_specs_file}
    # if it has been deemed spurious at least once and never error, then it is spurious
    comm -23 <( cat ${results_dir}/spurious-specs/* | cut -d' ' -f2- | sort -u ) <( cat ${results_dir}/error-specs/* | cut -d' ' -f2- | sort -u ) > ${total_spurious_specs_file}
    # unknown is only the set that got just unknown (no spurious)
    comm -23 <( cat ${results_dir}/unknown-specs/* | cut -d' ' -f2- | sort -u ) <( cat ${results_dir}/spurious-specs/* ${results_dir}/error-specs/* | cut -d' ' -f2- | sort -u ) > ${total_unknown_specs_file}
    # get total summaries
    echo "testName,Error,Unknown,SpuriousSpec,TrueSpec,Total" > ${results_dir}/summary.csv
    cat ${summary_dir}/* | grep -v "^testName" | sort -u >> ${results_dir}/summary.csv
    error_spec_num=$( cat ${total_error_specs_file} | wc -l )
    true_spec_num=$( cat ${total_true_specs_file} | wc -l )
    spurious_spec_num=$( cat ${total_spurious_specs_file} | wc -l )
    unknown_spec_num=$( cat ${total_unknown_specs_file} | wc -l )
    total_spec_num=$( cat ${total_true_specs_file} ${total_spurious_specs_file} ${total_unknown_specs_file} | sort -u | wc -l )
    echo "TOTAL-UNIQUE,${error_spec_num},${unknown_spec_num},${spurious_spec_num},${true_spec_num},${total_spec_num}" >> ${results_dir}/summary.csv

    mv ${results_dir} ${project_location}/results
    mv ${dsi_results_dir} ${project_location}/dsi-results
    mv ${bucket_dir} ${project_location}/bucket-results
    mv ${surefire_rep_dir} ${project_location}/surefire-reports

}

function build_configs_file() {
    local all_configs_file=${project_location}/logs/dsi-plus-all-configs.txt

    # allGranularities - retrieve all possible tests that we can run via DSI+
    if [[ ${OPTION} == "allGranularities" || ${OPTION} == "dsiAllGranularities" || ${OPTION} == "allTests" ]]; then
	echo "all-tests" > ${all_configs_file} # run all-tests
    fi
    # need to make unique since there can be multiple test methods from the same test class, and we don't want to duplicate work/enter a race condition
    if [[ ${OPTION} == "allGranularities" || ${OPTION} == "dsiAllGranularities" || ${OPTION} == "testClasses" ]]; then
	cat ${method_names_file} | cut -d# -f1 | sort -u >> ${all_configs_file} # run test classes
    fi
    if [[ ${OPTION} == "allGranularities" || ${OPTION} == "dsiAllGranularities" || ${OPTION} == "testMethods" ]]; then
	cat ${method_names_file} >> ${all_configs_file} # run test methods
    fi
}

function run_dsi_plus() {
    local project_location=$1
    local project_name=$2
    local all_configs_file=${project_location}/logs/dsi-plus-all-configs.txt
    local method_names_file=dsi-target/testnames.txt
    local tMap_dir=test-to-spec-maps

    # echo "Getting method names started at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
    # # getting the list of test method names...
    test_compile ${project_location}/logs
    # echo "getting method traces..."
    get_method_traces ${project_location}
    echo "changing back pom..."
    change_pom_no_listener # putting back dsi into pom
    # echo "Getting method names ended at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}

    # # build all_configs_file according to what we need to run
    # if [ ! -z "${PART_FILE}" ]; then
    #     echo "porting partial run file: ${PART_FILE} to ${all_configs_file}..."
    #     cp ${SCRIPT_DIR}/${PART_FILE} ${all_configs_file}
    # else
    #     build_configs_file
    # fi

    echo "NO MINING: Creating tMap started at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
    # make tMap_dir
    mkdir -p ${tMap_dir}
    # make a blank tMap file for all configs --> possible optimization: only run configs that have entries in tMap
    # for test_config in $( cat ${all_configs_file} ); do
    #     >${tMap_dir}/${test_config}-specs.txt
    # done
    # we take sMap from the master-spec-file list and then create our tMap

    while read id a b tests formulae; do
        for test_name in $( echo "${tests}" | sed 's/,/ /g' ); do
            echo ${id} ${a} ${b} >> ${tMap_dir}/${test_name}-specs.txt
        done
    done < ${SMAP}
    echo "NO MINING: Creating tMap ended at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}

    echo "populating all_configs_file using contents of tMap"
    cat ${SMAP} | cut -d' ' -f4 | sort -u >> ${all_configs_file}

    local mode="DSI"

    echo "Running DSI started at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
    cat ${all_configs_file} | sed "s/$/@${mode}@${MINING_OP}/g" | parallel -j ${PARALLEL_COUNT} bash ${SCRIPT_DIR}/run-dsi-plus-parallel.sh
    echo "Running DSI ended at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}

    echo "process_output started at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
    process_output ${project_location}
    echo "process_output ended at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
}

function run_baseline() {
    local logs_dir=$1
    test_compile ${logs_dir}
    echo "surefire tests started at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
    surefire_test ${logs_dir}
    echo "surefire tests ended at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
}

function main() {
    option=dsiPlus-${OPTION}
    url=$( head -1 ${PROJECT_FILE} | cut -d' ' -f1 )
    sha=$( head -1 ${PROJECT_FILE} | cut -d' ' -f2 )
    name=$( head -1 ${PROJECT_FILE} | cut -d' ' -f3 )
    [[ $url =~ ^#.* ]] && continue # skips projects that have hash in front
    [[ -z "$url" ]] && continue # skip newline
    echo ===============${IDENT}
    echo "Setup started at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
    check_project_clone ${url} ${name}
    make_project_dirs ${IDENT} ${option}
    prepare_project_workspace ${name} ${option} ${sha}
    echo "Setup ended at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
    # go to workspace...
    (
        export COMP_SKIPS
        export SKIPS
        cd ${RESULT_DIR}/${IDENT}/${option}/ws

        run_dsi_plus ${RESULT_DIR}/${IDENT}/${option} ${IDENT}
    )
}

backup_old_results
make_global_dirs
write_meta_info
prepare_dsi
main
write_end_time

# NOTE: OPTION starts with a dash
# cp ${WORKSPACES_DIR}/${TIMESTAMP}-run.info ${RUN_INFO_DIR}/${proj_list_name}${OPTION}-run.info

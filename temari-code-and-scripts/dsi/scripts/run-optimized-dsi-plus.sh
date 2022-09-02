# This script runs all granularities of DSI on the list of projects given.
if [ $# -lt 4 -o $# -gt 5 ]; then
    echo "Usage: $0 PROJECT_LIST OPTIMIZATION_LEVEL BUILD_OP BACKUP_OP [MINING-OP]"
    echo "if OPTIMIZATION_LEVEL is 1, then Spec-based DSI will run all tests that mined the spec without selection"
    echo "if OPTIMIZATION_LEVEL is 2, then Spec-based DSI will halt as soon as a spec is not classified as a true spec by DSI"
    echo "if OPTIMIZATION_LEVEL is 3, then Spec-based DSI will halt as soon as a spec is not classified as a true spec by DSI and perform selection"
    echo "if OPTIMIZATION_LEVEL is 4, then Spec-based DSI will run all tests that mined the spec with selection"
    echo "if OPTIMIZATION_LEVEL is 5, then Spec-based DSI will halt as soon as a spec is classified as a spurious spec without selection"
    echo "if OPTIMIZATION_LEVEL is 6, then Spec-based DSI will halt as soon as a spec is classified as a spurious spec with selection"
    echo "if BUILD_OP is -build, DSI will be built and if BUILD_OP is -noBuild, then DSI will not be built"
    echo "if BACKUP_OP is -backup, generated-data will be backed up and if BACKUP_OP is -noBackup, then no backup dir will be made"
    echo "if MINING-OP is -noMining, DSI++ will read from a preexisting master-spec-file (will halt if there is no preexisting master-spec-file)"
    exit
fi

if [ $2 -lt 0 -o $2 -gt 6 ]; then
    echo "OPTIMIZATION_LEVEL: invalid value! Valid values are 1 <= n <=6"
    exit
fi

PROJ_LIST=$1
OPTIMIZATION_LEVEL=$2
BUILD_OP=$3
BACKUP_OP=$4
MINING_OP=$5

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
RUN_INFO_FILE=${RUN_INFO_DIR}/${proj_list_name}-optimizedDsiPlus-${OPTIMIZATION_LEVEL}-${TIMESTAMP}-run.info

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
    echo "Script: $0 $@" >> ${RUN_INFO_FILE}
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

function get_method_names() {
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

function process_output() {
    local project_location=$1
    local gran=$2
    local surefire_rep_loc=${project_location}/ws/gol/surefire-reports
    local master_spec_file=${project_location}/logs/master-spec-file.txt
    local bucket_loc=${project_location}/ws/gol/bucket-results
    local raw_results=${project_location}/ws/gol/dsi-results
    local results_dir=${project_location}/ws/gol/results
    local traces_dir=traces
    local total_bucket_breakdown_file=${results_dir}/spec-breakdown-total.csv
    local summary_csv=${results_dir}/summary.csv
    local total_true_specs_file=${results_dir}/total-true-specs.txt
    local total_spurious_specs_file=${results_dir}/total-spurious-specs.txt
    local total_unknown_specs_file=${results_dir}/total-unknown-specs.txt
    local total_error_specs_file=${results_dir}/total-error-specs.txt

    true_spec=0
    spurius_spec=0
    unknown=0
    error=0
    echo "SpecID,RunType,Config,Bucket,Status" > ${total_bucket_breakdown_file}
    # creating the total category files even if there are no specs in those categories
    > ${total_true_specs_file}
    > ${total_spurious_specs_file}
    > ${total_unknown_specs_file}
    > ${total_error_specs_file}
    for id in $( cat ${master_spec_file} | cut -d' ' -f1 ); do
        breakdown_file=${results_dir}/spec-breakdown-${id}.csv
        if [ ! -f ${breakdown_file} ]; then # if the breakdown file doesn't even exist, then we can't do anything about it...
            error=$((${error} + 1))
            cat ${master_spec_file} | grep ${id} | cut -d' ' -f2- | rev | cut -d' ' -f2- | rev >> ${total_error_specs_file}
            continue
        fi
        ( cat ${breakdown_file} | grep -v ^S | grep -v "decision," | sort ) >> ${total_bucket_breakdown_file}
        res=$( cat ${breakdown_file} | grep "decision," | rev | cut -d, -f1 | rev )
        if [ "${res}" == "true-spec" ]; then
            true_spec=$((${true_spec} + 1))
            cat ${master_spec_file} | grep ${id} | cut -d' ' -f2- | rev | cut -d' ' -f2- | rev >> ${total_true_specs_file}
        elif [ "${res}" == "spurius-spec" ]; then
            spurius_spec=$((${spurius_spec} + 1))
            cat ${master_spec_file} | grep ${id} | cut -d' ' -f2- | rev | cut -d' ' -f2- | rev >> ${total_spurious_specs_file}
        elif [ "${res}" == "error" ]; then
            error=$((${error} + 1))
            cat ${master_spec_file} | grep ${id} | cut -d' ' -f2- | rev | cut -d' ' -f2- | rev >> ${total_error_specs_file}
        else
            unknown=$((${unknown} + 1))
            cat ${master_spec_file} | grep ${id} | cut -d' ' -f2- | rev | cut -d' ' -f2- | rev >> ${total_unknown_specs_file}
        fi
    done

    total_num=$( cat ${total_unknown_specs_file} ${total_spurious_specs_file} ${total_true_specs_file} | sort -u | wc -l )
    echo "Granularity,Error,Unknown,SpuriusSpec,TrueSpec,Total" > ${summary_csv}
    echo "TOTAL-UNIQUE,${error},${unknown},${spurius_spec},${true_spec},${total_num}" >> ${summary_csv}

    # uncomment below to get zipped traces
    if [ "${MINING_OP}" != "-noMining" ]; then
	echo "Zipping started at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
        tar -czf ${project_location}/traces.tgz ${traces_dir}
        echo "Zipping ended at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
    fi

    (
        mv ${results_dir} ${project_location}/results
        mv ${raw_results} ${project_location}/dsi-results
        mv ${bucket_loc} ${project_location}/bucket-results
	mv ${surefire_rep_loc} ${project_location}/surefire-reports
    )

}

function setup_optimized_dsi_plus() {
    local project_location=$1
    local project_name=$2
    local all_configs_file=${project_location}/logs/dsi-plus-all-configs.txt
    local method_names_file=dsi-target/testnames.txt
    local master_spec_file=${project_location}/logs/master-spec-file.txt

    echo "Setup for DSI++ started at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}

    # need to get method names for mining/selection --> do we want to carry around the testnames/all-configs files as well?
    echo "Getting method names started at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
    # getting the list of test method names...
    test_compile ${RESULT_DIR}/${name}/${option}/logs
    echo "getting method traces..."
    get_method_names ${RESULT_DIR}/${name}/${option}
    echo "changing back pom..."
    change_pom_no_listener # putting back dsi into pom
    echo "Getting method names ended at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}

    # setup all configs file
    echo "all-tests" > ${all_configs_file}
    # using sort to make sure that the same test class doesn't get mined multiple times
    cat ${method_names_file} | cut -d# -f1 | sort -u >> ${all_configs_file}
    cat ${method_names_file}  >> ${all_configs_file}

    if [ "${MINING_OP}" == "-noMining" ]; then
        echo "NO MINING - moving necessary files started at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
        # locate the master-spec-file for this project
        premade_master_spec_file=${SCRIPT_DIR}/master-spec-files/${project_name}-master-spec-file.txt
        tar zxf ${SCRIPT_DIR}/selection-trace-files/${project_name}.tgz
        premade_traces_dir=${project_name} # tar zxf outputs to current directory with the name of the .tgz
        cp ${premade_master_spec_file} ${master_spec_file}
        cp -r ${premade_traces_dir} ${project_location}/ws/traces
        echo "NO MINING - moving necessary files ended at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
	average_mining_time=$( grep ${project_name} ${SCRIPT_DIR}/mining-average-times.csv | rev | cut -d, -f1 | rev )
	echo "NO MINING - mining average time in seconds was: ${average_mining_time}" | tee -a ${RUN_INFO_FILE}
    else
        # get all the specs that we want
        if [ ! -z "${PART_FILE}" ]; then
	    echo "partial run file: ${PART_FILE}"
            cat ${SCRIPT_DIR}/${PART_FILE} | parallel -j ${PARALLEL_COUNT}  bash ${SCRIPT_DIR}/run-mining.sh
        else
            # mine all test configs
	    echo "Mining all configs started at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
            cat ${all_configs_file} | parallel -j ${PARALLEL_COUNT} bash ${SCRIPT_DIR}/run-mining.sh
	    echo "Mining all configs ended at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
        fi

        echo "FINISHED GETTING SPECS... NOW COMPUTING OUR GLOBAL SPECS"

        echo "Building sMap started at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
        acc=1
        # generate our master spec file
        while read spec; do
            spec_for_grep=$( echo "${spec}" | sed 's/\[/\\[/g' | sed 's/\$/\\$/g' )
            formatted=$( printf "%05d" ${acc} )
            tests=$(grep -ilr "${spec_for_grep}" dsi-target | rev | cut -d/ -f1 | cut -d. -f2- | rev | cut -d'-' -f3- )
            tests=$( echo ${tests} | sed 's/ /,/g' )
            echo "${formatted} ${spec} ${tests}" >> ${master_spec_file}
            acc=$((${acc} + 1))
        done < <( cat dsi-target/mined-specs-* | cut -d' ' -f2- | sort -u )
        echo "Building sMap ended at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
    fi
    echo "Setup for DSI++ ended at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
}

function run_optimized_dsi_plus() {
    local project_location=$1
    local project_name=$2
    local all_configs_file=${project_location}/logs/dsi-plus-all-configs.txt
    local method_names_file=dsi-target/testnames.txt
    local master_spec_file=${project_location}/logs/master-spec-file.txt

    setup_optimized_dsi_plus ${project_location} ${project_name}

    # run spec-based dsi+ on all specs
    echo "COMPUTED OUR GLOBAL SPECS, NOW RUNNING..."
    echo "Running DSI+ started at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
    cat ${master_spec_file} | cut -d' ' -f1 | sed "s:$: ${master_spec_file} ${OPTIMIZATION_LEVEL}:g" | sed 's/ /@/g' | parallel -j ${PARALLEL_COUNT} bash ${SCRIPT_DIR}/run-parallel-optimized-dsi-plus.sh
    echo "Running DSI+ ended at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
    echo "process_output started at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
    process_output ${project_location}
    echo "process_output ended at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
}


function main() {
    local option=optimizedDsiPlus-${OPTIMIZATION_LEVEL}
    while read url sha name
    do
        [[ $url =~ ^#.* ]] && continue # skips projects that have hash in front
        [[ -z "$url" ]] && continue # skip newline
        echo ===============${name}
	echo "Setup started at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
        check_project_clone ${url} ${name}
        make_project_dirs ${name} ${option}
        prepare_project_workspace ${name} ${option} ${sha}
	echo "Setup ended at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
        # go to workspace...
        (
            export COMP_SKIPS
            export SKIPS
            cd ${RESULT_DIR}/${name}/${option}/ws
            run_optimized_dsi_plus ${RESULT_DIR}/${name}/${option} ${name}
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

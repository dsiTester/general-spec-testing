# This script runs all granularities of DSI on the list of projects given.
if [ $# -ne 4 ]; then
    echo "Usage: $0 PROJECT_LIST BUILD_OP BACKUP_OP MOVE_OP"
    exit
fi

PROJ_LIST=$1
BUILD_OP=$2
BACKUP_OP=$3
MOVE_OP=$4

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
RUN_INFO_FILE=${RUN_INFO_DIR}/${proj_list_name}-mining-${TIMESTAMP}-run.info

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

function run_optimized_dsi_plus() {
    local project_location=$1
    local all_configs_file=${project_location}/logs/dsi-plus-all-configs.txt
    local method_names_file=dsi-target/testnames.txt
    local master_spec_file=${project_location}/logs/master-spec-file.txt

    echo "Getting method names started at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
    # getting the list of test method names...
    test_compile ${RESULT_DIR}/${name}/${option}/logs
    echo "getting method traces..."
    get_method_names ${RESULT_DIR}/${name}/${option}
    echo "changing back pom..."
    change_pom_no_listener # putting back dsi into pom
    echo "Getting method names ended at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}

    # get all the specs that we want
    if [ ! -z "${PART_FILE}" ]; then
	echo "partial run file: ${PART_FILE}"
        cat ${SCRIPT_DIR}/${PART_FILE} | parallel -j ${PARALLEL_COUNT}  bash ${SCRIPT_DIR}/run-mining.sh
    else
	echo "Mining all configs started at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${RUN_INFO_FILE}
        # run mining of all tests, test classes, test methods...
        echo "all-tests" > ${all_configs_file}
        cat ${method_names_file} | cut -d# -f1 | sort -u >> ${all_configs_file}
        cat ${method_names_file} | sort -u >> ${all_configs_file}
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

    if [ "${MOVE_OP}" == "-move" ]; then
	mkdir -p ${SCRIPT_DIR}/master-spec-files
	mkdir -p ${SCRIPT_DIR}/dsi-plus-all-configs-files
	cp ${master_spec_file} ${SCRIPT_DIR}/master-spec-files/${name}-master-spec-file.txt
	cp ${project_location}/logs/dsi-plus-all-configs.txt ${SCRIPT_DIR}/dsi-plus-all-configs-files/${name}-all-configs.txt
	mkdir -p ${SCRIPT_DIR}/selection-trace-files/${name}
	cp ${project_location}/ws/traces/*-method-names.txt.gz ${SCRIPT_DIR}/selection-trace-files/${name}
	(
	    cd ${SCRIPT_DIR}/selection-trace-files
	    tar -czf ${name}.tgz ${name}
	    rm -rf ${name}
	)
    fi

}


function main() {
    local option=mining
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
            run_optimized_dsi_plus ${RESULT_DIR}/${name}/${option}
        )
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

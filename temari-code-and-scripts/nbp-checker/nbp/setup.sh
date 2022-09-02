if [ $# -lt 2 ]; then
    echo "Usage: $0 edge-finder-jar project-list [inspection-dir-with-projects]"
    echo "Example: $0 lib/edge-finder.jar projects.txt ~/projects/dsi-inspections/inspections"
    exit
fi

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )

jar_file=$1
project_list=$2
inspection_dir=$3

gol_file=/tmp/gol-edge-finder-install.txt

function check_status() {
    local myvar=$1
    local suffix=$2

    if [[ -n ${myvar} ]]; then
        echo "PASSED "${suffix}
    else
        echo "FAILED "${suffix}
        exit 1
    fi
}

function install_edge_finder() {
    (
        mvn install:install-file -Dfile=${jar_file} -DgroupId="edu.cornell" \
            -DartifactId="edge-finder" -Dversion="1.0" -Dpackaging="jar"
    ) &> ${gol_file}

    install_status=$(grep "BUILD SUCCESS" ${gol_file})
    check_status "${install_status}" " on installing edge-finder jar"
}

function install_yasgl() {
    local yasgl_dir=${SCRIPT_DIR}/yasgl
    local yasgl_gol=${SCRIPT_DIR}/gol-yasgl
    (
        if [ ! -d ${yasgl_dir} ]; then
            git clone https://github.com/TestingResearchIllinois/yasgl.git ${yasgl_dir} &> /tmp/install-yasgl
        fi
        cd ${yasgl_dir}
        mvn install
    ) &> ${yasgl_gol}

    yasgl_status=$(grep "BUILD SUCCESS" ${yasgl_gol})
    check_status "${yasgl_status}" " on installing yasgl"
}

function install_nbp_spotter() {
    local nbp_gol=${SCRIPT_DIR}/gol-nbp-spotter
    (
        cd ${SCRIPT_DIR}
        mvn clean install
        mvn dependency:build-classpath -Dmdep.outputFile=${SCRIPT_DIR}/cp-nbp.txt
    ) &> ${nbp_gol}

    nbp_status=$(grep "BUILD SUCCESS" ${nbp_gol})
    check_status "${nbp_status}" " on installing nbp spotter"
}

function prepare_project() {
    local proj_url=$1
    local proj_sha=$2
    local proj_name=$3
    local nbp_file=$4
    local proj_dir=/tmp/${proj_name}
    if [ ! -d ${proj_dir}  ]; then
        git clone ${proj_url} ${proj_dir} &> /dev/null
    fi
    (
        cd ${proj_dir}
        git checkout -f ${proj_sha}
        git clean -ffxd
        mvn test-compile
    ) &> /dev/null

    if [ "${nbp_file}" != "" ]; then
        echo "COLLECTING INSPECTION DATA FROM ${inspection_dir}"
        > ${nbp_file}
        for json_file in $(ls ${inspection_dir}/${proj_name}/*.json)
        do
            python3 ${SCRIPT_DIR}/fetch-nbp.py ${json_file} /tmp/temp-nbp.txt
            cat /tmp/temp-nbp.txt >> ${nbp_file}
            > /tmp/temp-nbp.txt
        done
    else
        echo "SKIPPING CHECKING AGAINST INSPECTIONS"
    fi
}

function run_project() {
    local proj_url=$1
    local proj_sha=$2
    local proj_name=$3
    local nbp_file=$4
    local proj_dir=/tmp/${proj_name}
    (time java -jar ${SCRIPT_DIR}/lib/edge-finder.jar ${proj_dir}) &> ${SCRIPT_DIR}/gol-edge-finder-${proj_name}.txt
    xts_dir=${SCRIPT_DIR}/.ekstazi-${proj_name}
    mv .ekstazi ${xts_dir}
    nbp_cp=$(cat ${SCRIPT_DIR}/cp-nbp.txt)
    (
        time java -cp ${nbp_cp}:${SCRIPT_DIR}/target/dsi-1.0-SNAPSHOT.jar edu.cornell.GraphUtil \
             ${xts_dir}/method2MethodsClosure.txt \
             ${SCRIPT_DIR}/data/${proj_name}-master-spec-file.txt \
             ${SCRIPT_DIR}/.proj-${proj_name} \
             ${xts_dir}/hierarchy_parents.txt \
             ${xts_dir}/hierarchy_children.txt\
             ${nbp_file}
    ) &> ${SCRIPT_DIR}/gol-nbp-spotter-${proj_name}.txt

    echo "Static Analysis Time: "$(grep ^real ${SCRIPT_DIR}/gol-edge-finder-${proj_name}.txt | cut -f2)
    echo "NBP Finder Time: "$(grep ^real ${SCRIPT_DIR}/gol-nbp-spotter-${proj_name}.txt | cut -f2)
    echo "RESULTS:"
    grep -A4 ^"Manual:" ${SCRIPT_DIR}/gol-nbp-spotter-${proj_name}.txt
}

function cleanup() {
    rm -rf ${SCRIPT_DIR}/.ekstazi*
    rm -rf ${SCRIPT_DIR}/.proj*
    rm -rf ${SCRIPT_DIR}/gol-edge-*
    rm -rf ${SCRIPT_DIR}/gol-nbp-*
    rm -rf ${SCRIPT_DIR}/nbp-*
    rm -rf ${SCRIPT_DIR}/cp-nbp.txt
    rm -rf ${SCRIPT_DIR}/gol-yasgl    
}

function main() {
    cleanup # remove any old artifacts
    install_edge_finder
    install_yasgl
    install_nbp_spotter
    while read url sha name
    do
        echo "======RUNNING: ${name}"
        if [ "${inspection_dir}" != "" ]; then
            nbp_spec_file=${SCRIPT_DIR}/nbp-${name}
        else
            nbp_spec_file=""
        fi
        prepare_project ${url} ${sha} ${name} ${nbp_spec_file}
        run_project ${url} ${sha} ${name} ${nbp_spec_file}

    done < ${project_list}
}

main

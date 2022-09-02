SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
OUT_DIR=${SCRIPT_DIR}/setup-nbp-logs
mkdir -p ${OUT_DIR}
jar_file=${SCRIPT_DIR}/lib/edge-finder.jar
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
    local yasgl_gol=${OUT_DIR}/gol-yasgl
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
    local nbp_gol=${OUT_DIR}/gol-nbp-spotter
    (
        cd ${SCRIPT_DIR}
        mvn clean install
        mvn dependency:build-classpath -Dmdep.outputFile=${SCRIPT_DIR}/cp-nbp.txt
    ) &> ${nbp_gol}

    nbp_status=$(grep "BUILD SUCCESS" ${nbp_gol})
    check_status "${nbp_status}" " on installing nbp spotter"
}

function cleanup() {
    rm -rf ${SCRIPT_DIR}/.ekstazi*
    rm -rf ${SCRIPT_DIR}/.proj*
    # rm -rf ${SCRIPT_DIR}/gol-edge-*
    # rm -rf ${SCRIPT_DIR}/gol-nbp-*
    # rm -rf ${SCRIPT_DIR}/nbp-*
    rm -rf ${SCRIPT_DIR}/cp-nbp.txt
    rm -rf ${SCRIPT_DIR}/gol-yasgl
}


cleanup # remove any old artifacts
install_edge_finder
install_yasgl
install_nbp_spotter

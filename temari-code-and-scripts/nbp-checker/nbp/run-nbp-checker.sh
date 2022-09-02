if [ $# -lt 2 ]; then
    echo "Usage: $0 ident project-file smap"
    echo "Example: $0 ident commons-validator.txt commons-validator-master-spec-file.txt"
    exit
fi

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )

ident=$1
project_list=$2
smap=$3

jar_file=${SCRIPT_DIR}/lib/edge-finder.jar
gol_file=/tmp/gol-edge-finder-install.txt
OUT_DIR=${SCRIPT_DIR}/${ident}
rm -rf ${OUT_DIR}
mkdir -p ${OUT_DIR}
NBP_OUT=${OUT_DIR}/gol-nbp-spotter-${ident}.txt
RESULTS=${OUT_DIR}/results

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
        mvn test-compile -Dcheckstyle.skip -Drat.skip -Denforcer.skip -Danimal.sniffer.skip -Dmaven.javadoc.skip -Dfindbugs.skip -Dwarbucks.skip -Dmodernizer.skip -Dimpsort.skip -DfailIfNoTests=false -Dpmd.skip=true -Dcpd.skip=true -Dlicense.skip
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
    ( set -o xtrace ; time java -jar ${SCRIPT_DIR}/lib/edge-finder.jar ${proj_dir} ; set +o xtrace ) &> ${OUT_DIR}/gol-edge-finder-${proj_name}.txt
    xts_dir=${SCRIPT_DIR}/.ekstazi-${proj_name}
    mv .ekstazi ${xts_dir}
    nbp_cp=$(cat ${SCRIPT_DIR}/cp-nbp.txt)
    (
        set -o xtrace
        time java -cp ${nbp_cp}:${SCRIPT_DIR}/target/dsi-1.0-SNAPSHOT.jar edu.cornell.GraphUtil \
             ${xts_dir}/method2MethodsClosure.txt \
             ${smap} \
             ${RESULTS} \
             ${xts_dir}/hierarchy_parents.txt \
             ${xts_dir}/hierarchy_children.txt
        set +o xtrace
    ) &> ${NBP_OUT}

    echo "Static Analysis Time: "$(grep ^real ${OUT_DIR}/gol-edge-finder-${proj_name}.txt | cut -f2)
    echo "NBP Finder Time: "$(grep ^real ${NBP_OUT} | cut -f2)
    echo "RESULTS:"
    grep -A4 ^"Manual:" ${NBP_OUT}
}

function filter_smap() {
    out=${OUT_DIR}/nbp-filtered-smap-${ident}.txt
    >${out}
    for id in $( comm -23 <( cut -d' ' -f1 ${smap} | sort -u ) <( cat ${RESULTS}/nbp-specs.txt | cut -d' ' -f1 | sort -u ) ); do
        grep ^${id} ${smap} >> ${out}
    done
}

function main() {
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
        filter_smap
    done < ${project_list}
}

main

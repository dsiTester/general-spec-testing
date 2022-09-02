#!/bin/bash

if [ $# -lt 1 -o $# -gt 2 ]; then # TODO: do we want an option where
                                  # the user can pass in a preexisting
                                  # dashboard to be edited?
    echo "usage: bash $0 TARBALL [noRM]"
    exit
fi


SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
TARBALL=$1
TARBALL_DIR_NAME=$( echo ${TARBALL} | rev | cut -d'/' -f1 | rev | cut -d'.' -f1 )
RM_OP=$2
WORKSPACE=ws-`date +%Y-%m-%d-%H-%M-%S`
OUT_FILE=${SCRIPT_DIR}/dashboards/dashboard-`date +%Y-%m-%d-%H-%M-%S`.csv
DETAILED_OUT_FILE=${SCRIPT_DIR}/dashboards/detailed-dashboard-`date +%Y-%m-%d-%H-%M-%S`.csv

OPTIONS=(allTests allTestsNoDSI testClasses testClassesNoDSI testMethods testMethodsNoDSI)

function extract() {
    tar xf $1
}

function process_nodsi() { # current oracle: whether we have any no-dsi logs in the logs directory
    local option=$1
    local project_name=$2
    extract ${option}-${project_name}.tgz
    num_logs=$(ls ${option}-${project_name}/${project_name}/${option}/logs | grep "no-dsi" | wc -l)
    if [ ${num_logs} -ne 0 ]; then
        echo "Y"
    else
        echo "N (no log files)"
    fi
}

function process_dsi() { # current oracle: whether the bucket breakdown csv exists, and it contains more than 1 line (1 line gets added by default)
    local option=$1
    local project_name=$2
    extract ${option}-${project_name}.tgz
    breakdown_file=$( find ${option}-${project_name}/${project_name}/${option}/logs -name *bucket-breakdown.csv )
    if [ -f "${breakdown_file}" ]; then
        breakdown_num=$( cat ${breakdown_file} | wc -l )
        if [ ${breakdown_num} -gt 1 ]; then
            echo "Y"
        else
            echo "N (blank breakdown)"
        fi
    else
        echo "N (no breakdown)"
    fi
}

function produce_out_files() {
    echo "Project Name,All Tests,All Tests (No DSI),Test Classes,Test Classes (No DSI),Test Methods,Test Methods (No DSI)" | tee ${OUT_FILE} ${DETAILED_OUT_FILE}
    for project_name in $( ls | rev | cut -d'.' -f2- | rev | cut -d'-' -f2- | sort -u ); do
        out_string="${project_name}"
        detailed_out_string="${project_name}"
        for option in ${OPTIONS[@]}; do
            if [ -f "${option}-${project_name}.tgz" ]; then
                if [[ "${option}" == *"NoDSI" ]]; then
                    det_res=$(process_nodsi ${option} ${project_name})
                else
                    det_res=$(process_dsi ${option} ${project_name})
                fi
            else
                det_res="N (vm crash)" # if the tarball does not exist, it is an automatic "NO"
            fi
            res=$( echo ${det_res} | cut -d' ' -f1 )
            detailed_out_string="${detailed_out_string},${det_res}"
            out_string="${out_string},${res}"
        done
        echo "${detailed_out_string}" >> ${DETAILED_OUT_FILE}
        echo "${out_string}" >> ${OUT_FILE}
    done
}

function process_tarball() {
    mkdir ${WORKSPACE} # making a workspace directory for processing the tarball that will get deleted once results are produced.
    cp ${TARBALL} ${WORKSPACE}
    (
        cd ${WORKSPACE}
        extract ${TARBALL}
        cd ${TARBALL_DIR_NAME}

        produce_out_files
    )
    if [ "${RM_OP}" != "noRM" ]; then
	rm -rf ${WORKSPACE} # cleanup workspace
    fi
}

process_tarball

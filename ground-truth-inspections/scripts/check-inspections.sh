#!/bin/bash

if [ $# -ne 1 ]; then
    echo "USAGE: $0 PROJECT"
    echo "Where PROJECT is either the name of the project, or ALL to check all current projects in the repo"
    exit
fi

categories=( lv ls u e lv-ls lv-u lv-e ls-u ls-e u-e lv-ls-u lv-ls-e ls-u-e lv-ls-u-e )

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
REPO_DIR=$( dirname "${SCRIPT_DIR}" )
INSPECTIONS_DIR=${REPO_DIR}/inspection-files
SCHEMAS_DIR=${REPO_DIR}/schemas
PROJECT=$1

function run_validator() {
    echo -e "\nRUNNING VALIDATOR..."
    project=$1
    for category in ${categories[@]}; do
        json_file=${INSPECTIONS_DIR}/${project}/${category}.json
        if [ -f ${json_file} ]; then
            python3 ${SCRIPT_DIR}/validate-inspection-file.py ${SCHEMAS_DIR}/${category}-schema.json ${json_file}
            if [ $? -ne 0 ]; then
                echo "[ERROR] Validation of ${json_file} failed!"
                exit 1
            fi
        else
            echo "JSON file for ${category} does not exist, skipping."
        fi
    done
}

function check_snippet_files() {
    project=$1
    echo -e "\nCHECKING FOR SNIPPET FILES..."
    missing_count=0
    for snippet_file in $( grep \"code-snippets-file\" ${INSPECTIONS_DIR}/${project}/*.json | rev | cut -d' ' -f1  | rev | sed 's/\"//g' | sed 's/,//g' ); do
        if [ ! -f ${INSPECTIONS_DIR}/${project}/${snippet_file} ]; then
            missing_count=$((missing_count + 1))
            echo "[ERROR] ${INSPECTIONS_DIR}/${project}/${snippet_file} missing!"
        fi
    done
    if [ ${missing_count} -ne 0 ]; then
        echo "Missing ${missing_count} snippet files in ${project}..."
        exit 1
    else
        echo "No missing snippet files! :)"
    fi
}

function get_stats() {
    project=$1
    total_inspected=$( grep \"spec-id\" ${INSPECTIONS_DIR}/${project}/*.json | wc -l )
    total_specs=$( cat ${REPO_DIR}/data/spec-to-test-maps/${project}-master-spec-file.txt | wc -l )
    time_in_minutes=$( grep "inspection-time-in-minutes" ${INSPECTIONS_DIR}/${project}/*.json  | rev | cut -d' ' -f1 | rev | paste -sd+ | bc -l )
    breakdown=""
    verdicts=( "true-spec" "spurious-spec" "unknown" "no-break-pass (direct)" "no-break-pass (indirect)"  )
    for verdict in "${verdicts[@]}"; do
        breakdown=${breakdown},$( grep \"verdict\" ${INSPECTIONS_DIR}/${project}/*.json | grep "${verdict}" | wc -l )
    done
    echo ${project},${total_inspected},${total_specs},${time_in_minutes}${breakdown}
}

function output_status_message() {
    stats=$1
    project=$( echo ${stats} | cut -d, -f1 )
    total_inspected=$( echo ${stats} | cut -d, -f2 )
    total_specs=$( echo ${stats} | cut -d, -f3 )
    time_in_minutes=$( echo ${stats} | cut -d, -f4 )
    true_specs=$( echo ${stats} | cut -d, -f5)
    spurious_specs=$( echo ${stats} | cut -d, -f6)
    unknown=$( echo ${stats} | cut -d, -f7)
    nbpd=$( echo ${stats} | cut -d, -f8)
    nbpi=$( echo ${stats} | cut -d, -f9)
    avg_time_in_minutes=$( echo "scale=2; ${time_in_minutes}/${total_inspected}" | bc -l )
    echo -e "\nIn ${project}, you inspected ${total_inspected} specs out of a total of ${total_specs} specs in ${time_in_minutes} minutes (avg. ${avg_time_in_minutes} min/spec)! Breakdown below:"
    printf "%-30s ${true_specs}\n" "true-spec:"
    printf "%-30s ${spurious_specs}\n" "spurious-spec:"
    printf "%-30s ${unknown}\n" "unknown:"
    printf "%-30s ${nbpd}\n" "no-break-pass (direct):"
    printf "%-30s ${nbpi}\n" "no-break-pass (indirect):"
}

function check_project() {
    project=$1
    echo -e "\n==================CHECKING PROJECT ${project}"
    inspection_file_count=$( ls ${INSPECTIONS_DIR}/${project}/*.json | wc -l )
    if [ ${inspection_file_count} -eq 0 ]; then
        echo "No inspection files yet in ${project}, skipping."
        return
    fi
    run_validator ${project}
    check_snippet_files ${project}
    project_stat=$( get_stats ${project} )
    output_status_message ${project_stat}
}

function output_global_status_message() {
    total_inspected=0
    total_specs=0
    time_in_minutes=0
    true_specs=0
    spurious_specs=0
    unknown=0
    nbpd=0
    nbpi=0
    for project in $( ls ${INSPECTIONS_DIR} ); do
        if [ "${project}" == "README.md" ]; then
            continue
        fi
        inspection_file_count=$( ls ${INSPECTIONS_DIR}/${project}/*.json | wc -l )
        if [ ${inspection_file_count} -eq 0 ]; then
            continue
        fi
        stat=$( get_stats ${project} )
        total_inspected=$( echo "${total_inspected} + $( echo ${stat} | cut -d, -f2 )" | bc -l )
        total_specs=$( echo "${total_specs} + $( echo ${stat} | cut -d, -f3 )" | bc -l)
        time_in_minutes=$( echo "${time_in_minutes} + $( echo ${stat} | cut -d, -f4 )" | bc -l)
        true_specs=$( echo "${true_specs} + $( echo ${stat} | cut -d, -f5 )" | bc -l )
        spurious_specs=$( echo "${spurious_specs} + $( echo ${stat} | cut -d, -f6 )" | bc -l )
        unknown=$( echo "${unknown} + $( echo ${stat} | cut -d, -f7 )" | bc -l)
        nbpd=$( echo "${nbpd} + $( echo ${stat} | cut -d, -f8 )" | bc -l )
        nbpi=$( echo "${nbpi} + $( echo ${stat} | cut -d, -f9 )" | bc -l )
    done
    total_stats=TOTAL,${total_inspected},${total_specs},${time_in_minutes},${true_specs},${spurious_specs},${unknown},${nbpd},${nbpi}
    output_status_message ${total_stats}
}

function main() {
    if [ ${PROJECT} == ALL ]; then
        for project in $( ls ${INSPECTIONS_DIR} ); do
            if [ "${project}" == "README.md" ]; then
                continue
            fi
            check_project ${project}
        done
        echo -e "\n\n==================DASHBOARD FOR ALL CURRENT PROJECTS"
        output_global_status_message
    elif [ ! -d ${INSPECTIONS_DIR}/${PROJECT} ]; then
        echo "Project ${PROJECT} does not exist! Try again with a different input."
        exit 1
    else
        check_project ${PROJECT}
    fi
}

main

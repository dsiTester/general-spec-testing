#!/bin/bash
if [ $# -ne 2 ]; then
    echo "Usage: $0 WORKSPACE MASTER_SPEC_FILE_DIR"
    exit
fi

# this script constructs fail-on-spurious results based on DSI+ results.
# (DSI+ does not fail early)

WORKSPACE=$1
MASTER_SPEC_FILE_DIR=$2
RESULTS=${WORKSPACE}/dsi-6-levels #hardcoding the name
SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
OUT=${SCRIPT_DIR}/dsiPlus-allGranFailOnSpurious
rm -rf ${OUT} && mkdir -p ${OUT} # for debugging

function place_in_bucket() {
    #	place_in_bucket ${a} ${b} ${result} ${out_dir}
    local a=$1
    local b=$2
    local result=$3
    local out_dir=$4
    echo "${a} ${b}" >> ${out_dir}/total-${result}-specs.txt
}

function find_bucket() {
    local results_dir=$1
    local id=$2
    result=""
    if grep -q "unknown" <( grep -r ${id} ${results_dir}/total-results | cut -d: -f2 | grep -e "testMethods@testMethods" -e "allTests@allTests" -e "testClasses@testClasses" | rev | cut -d, -f-2 | rev ) ; then
	result=unknown
    elif grep -q "error" <( grep -r ${id} ${results_dir}/total-results | cut -d: -f2 | grep -e "testMethods@testMethods" -e "allTests@allTests" -e "testClasses@testClasses" | rev | cut -d, -f-2 | rev ) ; then
	result=error
    elif grep -q "true-spec" <( grep -r ${id} ${results_dir}/total-results | cut -d: -f2 | grep -e "testMethods@testMethods" -e "allTests@allTests" -e "testClasses@testClasses" | rev | cut -d, -f-2 | rev ) ; then
	result=true
    fi
    echo ${result}
}

function make_summary() {
    local out_dir=$1
    local master_spec_file=$2
    local summary_file=${out_dir}/summary.csv
    echo "testName,Error,Unknown,SpuriousSpec,TrueSpec,Total" > ${summary_file}
    echo "TOTAL-UNIQUE,$( cat ${out_dir}/total-error-specs.txt | wc -l ),$( cat ${out_dir}/total-unknown-specs.txt | wc -l),$( cat ${out_dir}/total-spurious-specs.txt | wc -l),$( cat ${out_dir}/total-true-specs.txt | wc -l),$( cat ${master_spec_file} | wc -l)" >> ${summary_file}
}

function make_per_spec_results() {
    local results_dir=$1
    local out_dir=$2
    local master_spec_file=$3
    result=""
    while read id a b tests; do
	if grep -q "spurius-spec" <( grep -r ${id} ${results_dir}/total-results | cut -d: -f2- ) ; then
	    result=spurious
	else
	    # now we need to find the outcome from intra granularity...
	    result=$( find_bucket ${results_dir} ${id} )
	fi
	place_in_bucket ${a} ${b} ${result} ${out_dir}
    done < ${master_spec_file}
}

function main() {
    for project_name in $( ls ${RESULTS} | grep dsiPlus-allGranularities | cut -d- -f3- ); do
	echo "project: ${project_name}"
	all_gran_results_dir=${RESULTS}/dsiPlus-allGranularities-${project_name}/${project_name}/dsiPlus-allGranularities/results
	project_out_dir=${OUT}/${project_name} # check with if we want to produce the results in the same location as dsiPlus-allGranularities?
	mkdir -p ${project_out_dir}
	>${project_out_dir}/total-error-specs.txt
	>${project_out_dir}/total-spurious-specs.txt
	>${project_out_dir}/total-true-specs.txt
	>${project_out_dir}/total-unknown-specs.txt
	master_spec_file=${MASTER_SPEC_FILE_DIR}/${project_name}-master-spec-file.txt
	echo "making per_spec results..."
	make_per_spec_results ${all_gran_results_dir} ${project_out_dir} ${master_spec_file}    
	echo "making summary..."
	make_summary ${project_out_dir} ${master_spec_file}
    done
}

main

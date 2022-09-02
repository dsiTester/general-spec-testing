#!/bin/bash

# This bash script counts how many .log files are in each bucket and creates a csv file
# containing breakdown for each DaCapo Benchmark project.

# Can provide directory (containing all of the buckets)
if [ $# != 3 ] ; then
  echo "usage: bash $0 DIRECTORY LOGS_DIR GRANULARITY"
  echo "where DIRECTORY is the directory where all of the buckets are in"
  echo "and LOGS_DIR is the directory where the output logs are in (breakdown csv will be stored here)"
  echo "and GRANULARITY is one of allTests, testClassLevel, or testMethodLevel (following csv file name conventions) "
  exit
fi

directory=$1
LOGS_DIR=$2
GRAN=$3
OUT=${LOGS_DIR}/${GRAN}-bucket-breakdown.csv

bucket_names=(property-not-satisfied sanity-checks-failed no-break-pass fail-before-stage-0 stage-0-failed invalidated stage-1-f2-crash-precond stage-2-f1-crash-postcond stage-3-complete unknown)

echo "project,not-sat,sanity-fail,nbp,before-s0,s0-fail,invalid,s1-crash,s2-crash,s3-comp,unknown,total" > ${OUT}

comma=","


function check_logs() {
    local ben=$1
    gol_file=$( ls ${LOGS_DIR} | grep "gol-" | grep -- "-${ben}$" | grep -v "compile" )
    num_asgn=$( cat ${LOGS_DIR}/${gol_file} | grep "# assignments" | cut -d' ' -f3 )
    if [ ! -z "${num_asgn}" ]; then
	if [ ${num_asgn} -eq 0 ]; then
	    echo "${ben},0,0,0,0,0,0,0,0,0,0,0" >> ${OUT}
	fi
    else
        echo "${ben},-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1" >> ${OUT}
        # echo
        # echo "${ben}: logs does not have \"# assignments\"!"
        # uncomment below to print out the specific exception
        # l=$( cat ${LOGS_DIR}/${gol_file} | grep "Exception" | grep -v "^Exception" )
        # echo ${l}
    fi
}

function main() {
    # iterate over all subdirectories of base bucket directory
    for ben in $(ls ${directory}); do
	curr_breakdown=${ben}
	curr_total=0
	for bucket in ${bucket_names[@]}; do
	    curr_bucket_num=`ls ${directory}/${ben}/${bucket}/ | wc -l`
	    curr_total=$((${curr_total} + ${curr_bucket_num}))
	    curr_breakdown=${curr_breakdown}${comma}${curr_bucket_num}
	done
	echo "${curr_breakdown}${comma}${curr_total}" >> ${OUT}
    done
    if [ "${GRAN}" == "allTests" ]; then
	( check_logs "all-tests" ) # if there's no 0 assignment printout, then nothing happens
    else
    	# we want to check for tests that didn't end up on the breakdown because input to DSI was 0
    	remainder=$( comm -13 <( tail -n +2 ${OUT} | cut -d',' -f1 | sort -u ) <( ls ${LOGS_DIR} | grep "gol-" | grep -v "collect-" | grep -v "mine-" | grep -v "checkout" | grep -v "compile" | rev | cut -d'-' -f1 | rev | sort -u ) )
    	for r in ${remainder}; do
    	    ( check_logs "${r}" ) # the tests that don't show up after this are ones that errored out
    	done
    fi
}

main

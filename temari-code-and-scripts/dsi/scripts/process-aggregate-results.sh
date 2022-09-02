#!/bin/bash

# This bash script creates an aggregate spec statistc
if [ $# != 3 ] ; then
  echo "usage: bash $0 ALL_TESTS_DIR TEST_CLASS_DIR TEST_METHOD_DIR"
  echo "where ALL_TESTS_DIR contains results for all tests (together)"
  echo "and TEST_CLASS_DIR contains results for the test class level"
  echo "and TEST_METHOD_DIR contains results for the test method level"
  exit
fi

# SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
OG_DIR=$(pwd)
ALL_TESTS_DIR=$( cd ${OG_DIR} && cd $1 && pwd )
TEST_CLASS_DIR=$( cd ${OG_DIR} && cd $2 && pwd )
TEST_METHOD_DIR=$( cd ${OG_DIR} && cd $3 && pwd )

# There is DEFINITELY a smarter way to do this
function process_gran_with_excludes() {
    local gran_name=$1
    local gran_dir=$2
    local excludes=$3
    cd ${gran_dir}
    # get total
    total_specs=`find -name *.log | grep -vE "${excludes}" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u | wc -l`
    # get ctf (collect-traces-failure)
    ctf_specs=`find -name *.log | grep -vE "${excludes}" | grep -E "collect-traces-failure" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u | wc -l`
    # get non specs
    non_specs=`find -name *.log | grep -vE "${excludes}" | grep -E "invalidated|no-break-pass|stage-0-failed" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u | wc -l`
    # get true specs
    true_specs=`find -name *.log | grep -vE "${excludes}" | grep -E "stage-1-f2-crash-precond|stage-2-f1-crash-postcond|stage-3-complete" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u | wc -l`
    cd ${OG_DIR}
    echo "${gran_name},${total_specs},${ctf_specs},${non_specs},${true_specs}" >> granularity-aggregate-results.csv
}

function nonunique_process_gran_with_excludes() {
    local gran_name=$1
    local gran_dir=$2
    local excludes=$3
    cd ${gran_dir}
    # get total
    total_specs=`find -name *.log | grep -vE "${excludes}" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | wc -l`
    # get ctf (collect-traces-failure)
    ctf_specs=`find -name *.log | grep -vE "${excludes}" | grep -E "collect-traces-failure" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | wc -l`
    # get non specs
    non_specs=`find -name *.log | grep -vE "${excludes}" | grep -E "invalidated|no-break-pass|stage-0-failed" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | wc -l`
    # get true specs
    true_specs=`find -name *.log | grep -vE "${excludes}" | grep -E "stage-1-f2-crash-precond|stage-2-f1-crash-postcond|stage-3-complete" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | wc -l`
    cd ${OG_DIR}
    echo "${gran_name} (Non-Unique),${total_specs},${ctf_specs},${non_specs},${true_specs}" >> granularity-aggregate-results.csv
}
# END HERE

function process_gran() {
    local gran_name=$1
    local gran_dir=$2
    cd ${gran_dir}
    # get total
    total_specs=`find -name *.log | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u | wc -l`
    if [ ${total_specs} -eq 1 ]; then
        ${total_specs}=0
    fi
    # get ctf (collect-traces-failure)
    ctf_specs=`find -name *.log | grep -E "collect-traces-failure" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u | wc -l`
    # get non specs
    non_specs=`find -name *.log | grep -E "invalidated|no-break-pass|stage-0-failed" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u | wc -l`
    # get true specs
    true_specs=`find -name *.log | grep -E "stage-1-f2-crash-precond|stage-2-f1-crash-postcond|stage-3-complete" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u | wc -l`
    cd ${OG_DIR}
    echo "${gran_name},${total_specs},${ctf_specs},${non_specs},${true_specs}" >> granularity-aggregate-results.csv
}

function nonunique_process_gran() {
    local gran_name=$1
    local gran_dir=$2
    cd ${gran_dir}
    # get total
    total_specs=`find -name *.log | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | wc -l`
    # get ctf (collect-traces-failure)
    ctf_specs=`find -name *.log | grep -E "collect-traces-failure" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | wc -l`
    # get non specs
    non_specs=`find -name *.log | grep -E "invalidated|no-break-pass|stage-0-failed" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | wc -l`
    # get true specs
    true_specs=`find -name *.log | grep -E "stage-1-f2-crash-precond|stage-2-f1-crash-postcond|stage-3-complete" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | wc -l`
    cd ${OG_DIR}
    echo "${gran_name} (Non-Unique),${total_specs},${ctf_specs},${non_specs},${true_specs}" >> granularity-aggregate-results.csv
}

function venn_helper() {
    local gran_1_name=$1
    local gran_1_dir=$2
    local gran_1_excludes=$3
    local gran_2_name=$4
    local gran_2_dir=$5
    local gran_2_excludes=$6

    if [[ ${gran_1_excludes} == "NA" && ${gran_2_excludes} == "NA" ]]; then
        # echo "both $gran_1_name and $gran_2_name have no excludes" # debug log
        total_specs=`comm -23 <(cd ${gran_1_dir} && find -name *.log | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) <(cd ${gran_2_dir} && find -name *.log | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) | wc -l`
        ctf_specs=`comm -23 <(cd ${gran_1_dir} && find -name *.log | grep -E "collect-traces-failure" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) <(cd ${gran_2_dir} && find -name *.log | grep -E "collect-traces-failure" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) | wc -l`
        non_specs=`comm -23 <(cd ${gran_1_dir} && find -name *.log | grep -E "invalidated|no-break-pass|stage-0-failed" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) <(cd ${gran_2_dir} && find -name *.log | grep -E "invalidated|no-break-pass|stage-0-failed" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) | wc -l`
        true_specs=`comm -23 <(cd ${gran_1_dir} && find -name *.log | grep -E "stage-1-f2-crash-precond|stage-2-f1-crash-postcond|stage-3-complete" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) <(cd ${gran_2_dir} && find -name *.log | grep -E "stage-1-f2-crash-precond|stage-2-f1-crash-postcond|stage-3-complete" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) | wc -l`
    elif [[ ${gran_1_excludes} == "NA" ]]; then # gran 1 has no excludes but gran 2 does
        # echo "$gran_1_name has no excludes" # debug log
        total_specs=`comm -23 <(cd ${gran_1_dir} && find -name *.log | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) <(cd ${gran_2_dir} && find -name *.log | grep -vE "${gran_2_excludes}" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) | wc -l`
        ctf_specs=`comm -23 <(cd ${gran_1_dir} && find -name *.log | grep -E "collect-traces-failure" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) <(cd ${gran_2_dir} && find -name *.log | grep -vE "${gran_2_excludes}" | grep -E "collect-traces-failure" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) | wc -l`
        non_specs=`comm -23 <(cd ${gran_1_dir} && find -name *.log | grep -E "invalidated|no-break-pass|stage-0-failed" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) <(cd ${gran_2_dir} && find -name *.log | grep -vE "${gran_2_excludes}" | grep -E "invalidated|no-break-pass|stage-0-failed" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) | wc -l`
        true_specs=`comm -23 <(cd ${gran_1_dir} && find -name *.log | grep -E "stage-1-f2-crash-precond|stage-2-f1-crash-postcond|stage-3-complete" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) <(cd ${gran_2_dir} && find -name *.log | grep -vE "${gran_2_excludes}" | grep -E "stage-1-f2-crash-precond|stage-2-f1-crash-postcond|stage-3-complete" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) | wc -l`
    elif [[ ${gran_2_excludes} == "NA" ]]; then # gran 2 has no excludes but gran 1 does
        # echo "$gran_2_name has no excludes" # debug log
        total_specs=`comm -23 <(cd ${gran_1_dir} && find -name *.log | grep -vE "${gran_1_excludes}" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) <(cd ${gran_2_dir} && find -name *.log | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) | wc -l`
        ctf_specs=`comm -23 <(cd ${gran_1_dir} && find -name *.log | grep -vE "${gran_1_excludes}" | grep -E "collect-traces-failure" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) <(cd ${gran_2_dir} && find -name *.log | grep -E "collect-traces-failure" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) | wc -l`
        non_specs=`comm -23 <(cd ${gran_1_dir} && find -name *.log | grep -vE "${gran_1_excludes}" | grep -E "invalidated|no-break-pass|stage-0-failed" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) <(cd ${gran_2_dir} && find -name *.log | grep -E "invalidated|no-break-pass|stage-0-failed" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) | wc -l`
        true_specs=`comm -23 <(cd ${gran_1_dir} && find -name *.log | grep -vE "${gran_1_excludes}" | grep -E "stage-1-f2-crash-precond|stage-2-f1-crash-postcond|stage-3-complete" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) <(cd ${gran_2_dir} && find -name *.log | grep -E "stage-1-f2-crash-precond|stage-2-f1-crash-postcond|stage-3-complete" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) | wc -l`
    else # we can use both excludes
        # echo "both $gran_1_name and $gran_2_name have excludes" # debug log
        total_specs=`comm -23 <(cd ${gran_1_dir} && find -name *.log | grep -vE "${gran_1_excludes}" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) <(cd ${gran_2_dir} && find -name *.log | grep -vE "${gran_2_excludes}" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) | wc -l`
        ctf_specs=`comm -23 <(cd ${gran_1_dir} && find -name *.log | grep -vE "${gran_1_excludes}" | grep -E "collect-traces-failure" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) <(cd ${gran_2_dir} && find -name *.log | grep -vE "${gran_2_excludes}" | grep -E "collect-traces-failure" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) | wc -l`
        non_specs=`comm -23 <(cd ${gran_1_dir} && find -name *.log | grep -vE "${gran_1_excludes}" | grep -E "invalidated|no-break-pass|stage-0-failed" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) <(cd ${gran_2_dir} && find -name *.log | grep -vE "${gran_2_excludes}" | grep -E "invalidated|no-break-pass|stage-0-failed" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) | wc -l`
        true_specs=`comm -23 <(cd ${gran_1_dir} && find -name *.log | grep -vE "${gran_1_excludes}" | grep -E "stage-1-f2-crash-precond|stage-2-f1-crash-postcond|stage-3-complete" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) <(cd ${gran_2_dir} && find -name *.log | grep -vE "${gran_2_excludes}" | grep -E "stage-1-f2-crash-precond|stage-2-f1-crash-postcond|stage-3-complete" | xargs grep -A1 ^"a=" | cut -d'=' -f2 | grep -v ^- | paste -s -d' \n' | sed '/^\s*$/d' | sort -u) | wc -l`
    fi
    cd ${OG_DIR}
    echo "${gran_1_name}-${gran_2_name},${total_specs},${ctf_specs},${non_specs},${true_specs}" >> granularity-aggregate-results.csv
}

# produces venn diagram info, here we only care about unique specs
function venn() {
    # take in the names and directories for both
    local gran_1_name=$1
    local gran_1_dir=$2
    local gran_1_excludes=$3
    local gran_2_name=$4
    local gran_2_dir=$5
    local gran_2_excludes=$6


    # we want to do both ends... call a fun that does the same thing for both
    # apparently you need quotes here so that string with whitespace will be taken in as one thing
    venn_helper "${gran_1_name}" ${gran_1_dir} ${gran_1_excludes} "${gran_2_name}" ${gran_2_dir} ${gran_2_excludes}
    # venn_helper "${gran_2_name}" ${gran_2_dir} ${gran_2_excludes} "${gran_1_name}" ${gran_1_dir} ${gran_1_excludes}
}

function main() {
    # preprocessing...

    all_method_classes=`cd ${TEST_METHOD_DIR} && find -name *.log | cut -d'/' -f2 | cut -d# -f1 | sort | uniq `
    classes_with_specs=`ls ${TEST_CLASS_DIR} | sort `
    cd ${TEST_METHOD_DIR}

    # Classes that were in the Test Class level, but weren't in the Test Method Level
    classes_to_exclude=`comm -13 <(find -name *.log | cut -d'/' -f2 | cut -d# -f1 | sort | uniq ) <(ls ${TEST_CLASS_DIR} | sort )` 
    classes_to_exclude=`echo ${classes_to_exclude} | tr ' ' '|' `

    # Classes that were in the Test Method level, but weren't in the Test Class Level
    m_classes_to_exclude=`comm -23 <(find -name *.log | cut -d'/' -f2 | cut -d# -f1 | sort | uniq ) <(ls ${TEST_CLASS_DIR} | sort )` 
    m_classes_to_exclude=`echo ${m_classes_to_exclude} | tr ' ' '|' `

    cd ${OG_DIR}
    echo "granularity,total,ctf,non-specs,true-specs" > granularity-aggregate-results.csv
    if [ ${ALL_TESTS_DIR} != "N" ]; then
        process_gran "All Tests" ${ALL_TESTS_DIR}
        nonunique_process_gran "All Tests" ${ALL_TESTS_DIR}
    fi
    if [ -z ${classes_to_exclude} ] ; then
        echo "Class Level: no classes to exclude!"
        process_gran "Test Class" ${TEST_CLASS_DIR}
        nonunique_process_gran "Test Class" ${TEST_CLASS_DIR}
        classes_to_exclude="NA"
    else
        echo "Class Level: excluding classes ${classes_to_exclude}"
        process_gran_with_excludes "Test Class" ${TEST_CLASS_DIR} ${classes_to_exclude}
        nonunique_process_gran_with_excludes "Test Class" ${TEST_CLASS_DIR} ${classes_to_exclude}
    fi
    if [ -z ${m_classes_to_exclude} ] ; then
        echo "Method Level: no classes to exclude!"
        process_gran "Test Method" ${TEST_METHOD_DIR}
        nonunique_process_gran "Test Method" ${TEST_METHOD_DIR}
        m_classes_to_exclude="NA"
    else
        echo "Method Level: excluding classes ${m_classes_to_exclude}"
        process_gran_with_excludes "Test Method" ${TEST_METHOD_DIR} ${m_classes_to_exclude}
        nonunique_process_gran_with_excludes "Test Method" ${TEST_METHOD_DIR} ${m_classes_to_exclude}
    fi

    venn "All Tests" ${ALL_TESTS_DIR} "NA" "Test Class" ${TEST_CLASS_DIR} "NA" # when comparing against all tests, no excludes
    venn "All Tests" ${ALL_TESTS_DIR} "NA" "Test Method" ${TEST_METHOD_DIR} "NA"
    venn "Test Class" ${TEST_CLASS_DIR} "NA" "All Tests" ${ALL_TESTS_DIR} "NA"
    venn "Test Class" ${TEST_CLASS_DIR} ${classes_to_exclude} "Test Method" ${TEST_METHOD_DIR} ${m_classes_to_exclude}
    venn "Test Method" ${TEST_METHOD_DIR} "NA" "All Tests" ${ALL_TESTS_DIR} "NA"
    venn "Test Method" ${TEST_METHOD_DIR} ${m_classes_to_exclude} "Test Class" ${TEST_CLASS_DIR} ${classes_to_exclude}


}

main

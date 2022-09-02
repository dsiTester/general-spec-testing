#!/bin/sh

# partition the provided directory/current directory's log files into
# the main experimental results categories

if [ $# != 2 ]; then
    echo "usage: bash bucket-results.sh INPUT_DIRECTORY OUTPUT_DIRECTORY"
    echo "where INPUT_DIRECTORY is the directory containing the logs to be sorted into buckets"
    echo "and OUTPUT_DIRECTORY is the directory where the buckets should go"
    exit
fi

input_directory=$1
output_directory=$2

for file in $(find ${input_directory} -type f); do

    test_class_name=`grep "TEST NAME:" ${file} | cut -d' ' -f3`

    # this is egregious in the number of times mkdir is going to happen but I think it's ok for now
    mkdir -p ${output_directory}/${test_class_name}/sanity-checks-failed \
          ${output_directory}/${test_class_name}/property-not-satisfied \
          ${output_directory}/${test_class_name}/no-break-pass \
          ${output_directory}/${test_class_name}/invalidated \
          ${output_directory}/${test_class_name}/fail-before-stage-0 \
          ${output_directory}/${test_class_name}/stage-0-failed \
          ${output_directory}/${test_class_name}/stage-1-f2-crash-precond \
          ${output_directory}/${test_class_name}/stage-2-f1-crash-postcond \
          ${output_directory}/${test_class_name}/stage-3-complete \
          ${output_directory}/${test_class_name}/unknown

    # all method level specs with != 1 Tests run will be put into unknown
    incorrect_num=`grep "INCORRECT_NUM" ${file} | wc -l`
    if [[ ${incorrect_num} -gt 0 ]]; then
        cp ${file} ${output_directory}/${test_class_name}/unknown
        continue
    fi

    # NOTE: these ASM bugs will now be moved to unknown to prevent confusion
    buggy_exception=`grep "Exception while transforming:java.lang.StringIndexOutOfBoundsException: String index out of range: -1" ${file} | wc -l`
    if [[ ${buggy_exception} -gt 0 ]]; then
        cp ${file} ${output_directory}/${test_class_name}/unknown
        continue
    fi

    # Checking if property was broken is useful for cross-granularity checks
    property_not_satisfied=`grep -e "Property not satisfied!" -e "Sanity check failed: Trace does not satisfy property!" ${file} | wc -l`
    if [[ ${property_not_satisfied} -gt 0 ]]; then
        cp ${file} ${output_directory}/${test_class_name}/property-not-satisfied
        continue
    fi

    # check if we have sanity check failure
    # there are cases when the sanity check fails for reasons other than 
    sanity_checks_failed=`grep "TEST FAILURE DURING COLLECTING TRACES" ${file} | wc -l`
    if [[ ${sanity_checks_failed} -gt 0 ]]; then
        cp ${file} ${output_directory}/${test_class_name}/sanity-checks-failed
        continue
    fi

    # we can check for specs we couldn't break
    no_break_pass=`grep "***Property SATISFIED***" ${file} | wc -l`
    if [[ ${no_break_pass} -gt 0 ]]; then
        cp ${file} ${output_directory}/${test_class_name}/no-break-pass
        continue
    fi

    # we can also check for invalidated (stage-3-passed) from the test results
    invalidated=`grep "TEST PASSED - likely false specification" ${file} | wc -l`
    if [[ ${invalidated} -gt 0 ]]; then
        cp ${file} ${output_directory}/${test_class_name}/invalidated
        continue
    fi

    # FIXME: this really isn't the best fix, but I want to avoid cases where there's a complete but also a FIRST_DELAYED
    # FIXME: assuming that all prints appear in the log file
    # now we need to search for the occurences of all of the others
    complete=`grep "***COMPLETE***" ${file} | wc -l`
    second_ex=`grep "***SECOND_EXECUTED***" ${file} | wc -l`
    before_second=`grep "***BEFORE_SECOND***" ${file} | wc -l`
    first_delayed=`grep "***FIRST_DELAYED***" ${file} | wc -l`
    # if FIRST_DELAYED is the highest, then we will place this test into the stage0 dir
    if [[ ${first_delayed} -gt ${before_second} ]]; then
        cp ${file} ${output_directory}/${test_class_name}/stage-0-failed
    # if BEFORE_SECOND is higher than the others but equal to FIRST_DELAYED, then stage1
    elif [[ ${before_second} -gt ${second_ex} ]]; then
        cp ${file} ${output_directory}/${test_class_name}/stage-1-f2-crash-precond
    # if SECOND_EXECUTED is higher than COMPLETE but equal to the others, then stage2
    elif [[ ${second_ex} -gt ${complete} ]]; then
        cp ${file} ${output_directory}/${test_class_name}/stage-2-f1-crash-postcond
    # if all of them are zero... none of the preexisting cases
    elif [[ ${complete} -eq 0 ]]; then
    	cp ${file} ${output_directory}/${test_class_name}/fail-before-stage-0
    # if COMPLETE is equal to all the others then it's stage3complete
    else
        cp ${file} ${output_directory}/${test_class_name}/stage-3-complete
    fi
done

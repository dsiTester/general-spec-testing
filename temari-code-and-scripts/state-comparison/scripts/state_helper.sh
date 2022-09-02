SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )

# returns share-state-status,a-set-b-set,a-set-b-get,a-get-b-set,a-get-b-get(array)
function compute_share_state() {
    local in_file=$1
    method_a_set_method_b_set=$( comm -12 <( grep "SET,METHOD_A" ${in_file} | cut -d, -f3-4 | sed 's/ /-/g' | sort -u ) <( grep "SET,METHOD_B" ${in_file} | cut -d, -f3-4  | sed 's/ /-/g' | sort -u ) )
    method_a_set_method_b_get=$( comm -12 <( grep "SET,METHOD_A" ${in_file} | cut -d, -f3-4 | sed 's/ /-/g' | sort -u ) <( grep "GET,METHOD_B" ${in_file} | cut -d, -f3-4  | sed 's/ /-/g' | sort -u ) )
    method_a_get_method_b_set=$( comm -12 <( grep "GET,METHOD_A" ${in_file} | cut -d, -f3-4 | sed 's/ /-/g' | sort -u ) <( grep "SET,METHOD_B" ${in_file} | cut -d, -f3-4  | sed 's/ /-/g' | sort -u ) )
    method_a_get_method_b_get=$( comm -12 <( grep "GET,METHOD_A" ${in_file} | cut -d, -f3-4 | sed 's/ /-/g' | sort -u ) <( grep "GET,METHOD_B" ${in_file} | cut -d, -f3-4  | sed 's/ /-/g' | sort -u ) | grep "\[\]" ) # count mutual gets of arrays as sharing state

    a_set_b_set_num=$( echo "${method_a_set_method_b_set}" | wc -w )
    a_set_b_get_num=$( echo "${method_a_set_method_b_get}" | wc -w )
    a_get_b_set_num=$( echo "${method_a_get_method_b_set}" | wc -w )
    a_get_b_get_num=$( echo "${method_a_get_method_b_get}" | wc -w )

    num_share_state=$(( a_set_b_set_num + a_set_b_get_num + a_get_b_set_num + a_get_b_get_num ))


    if [ "${num_share_state}" -gt 0 ]; then
        echo "yes,${a_set_b_set_num},${a_set_b_get_num},${a_get_b_set_num},${a_get_b_get_num}"
    else
        echo "no,${a_set_b_set_num},${a_set_b_get_num},${a_get_b_set_num},${a_get_b_get_num}"
    fi
}

# function for obtaining a 
function make_set_get_string() {
    local typ=$1
    local treat_special_log=$2
    local treat_special_file=${SCRIPT_DIR}/aspect_excludes.csv
    if grep -q ^"${typ}," ${treat_special_file} ; then
        grep ^"${typ}," ${treat_special_file} | cut -d, -f2
        echo "${typ}" >> ${treat_special_log}
    else
        typ=$( echo "${typ}" | sed 's/\$/./g' )
        typ="MODE(* ${typ}.*)"
        echo "${typ}"
    fi
}

function create_sets_and_gets() {
    local common_types=$1
    local treat_special_log=$2
    acc_str=""
    # common_types=$( comm -12 <( grep "METHOD_A,Specific type: " ${ORIGINAL_OUTPUT_FILE} | cut -d' ' -f3 | sort -u ) <( grep "METHOD_B,Specific type: " ${ORIGINAL_OUTPUT_FILE} | cut -d' ' -f3 | sort -u ) | cut -d, -f1 | sort -u )
    for typ in $( echo "${common_types}" | grep -v "\[L" | grep "java." ); do # excluding all arrays since we can't track them anyways.
        new_pointcut=$( make_set_get_string "${typ}" ${treat_special_log} )
        if [ "${new_pointcut}" == "" ]; then
            continue
        fi
        if [ "${acc_str}" == "" ]; then
            acc_str="${new_pointcut}"
        else
            acc_str="${acc_str} || ${new_pointcut}"
        fi
    done
    echo "${acc_str}"
}

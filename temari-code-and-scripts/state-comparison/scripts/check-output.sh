if [ $# -ne 1 ]; then
    echo "USAGE: $0 out-file"
    exit
fi

OUT_FILE=$1

acc_num=0

echo "id,state-result,accuracy,tags" > ${OUT_FILE}
for line in $( tail -n +2 fileupload.csv | sort ); do
    spec=$( echo ${line} | cut -d, -f1 )
    verdict=$( echo ${line} | cut -d, -f5 )
    tags=$( echo ${line} | cut -d, -f6 )
    if grep -q "method-a and method-b share state!" ~/sandbox/commons-fileupload/state-logs/gol-$spec ; then
        share="yes" ; else share="no"
    fi
    if grep -q "method-a and method-b do NOT share state!" ~/sandbox/commons-fileupload/state-logs/gol-$spec ; then
        noshare="yes"
    else
        noshare="no"
    fi
    out="?"
    if [[ "${share}" == "yes" && "${noshare}" == "yes" ]]; then
        out="mixed"
    elif [ "${share}" == "yes" ] ; then
        out="yes"
    elif [ "${noshare}" == "yes" ]; then
        out="no"
    fi
    # echo $spec,$out

    # {"UNRELATED_STATEFUL_METHODS", "ONE_STATELESS_METHOD", "ONE_PURE_SETTER", "CONNECTION_DOES_NOT_NECESSITATE_ORDERING", "UNRELATED_STATELESS_METHODS", "UNRELATED_PURE_SETTERS", "TRUE_SPEC_WITH_CALLER", "MODIFIED_STATE_DOES_NOT_INTERSECT"}
    accurate="?"

    if [[ "${tags}" == *"ONE_STATELESS_METHOD"* || "${tags}" == *"UNRELATED_STATEFUL_METHODS"* || "${tags}" == *"UNRELATED_STATELESS_METHODS"* || "${tags}" == *"UNRELATED_PURE_SETTERS"* || "${tags}" == *"MODIFIED_STATE_DOES_NOT_INTERSECT"* ]]; then
        if [ "${out}" == no ]; then
            accurate="yes"
        elif [ "${out}" == yes ]; then
            accurate="no"
        fi
    elif [[ "${tags}" == *"CONNECTION_DOES_NOT_NECESSITATE_ORDERING"* ]]; then
        if [ "${out}" == no ]; then
            accurate="no"
        elif [ "${out}" == yes ]; then
            accurate="yes"
        fi
    fi
    echo $spec,$out,$accurate,$tags  >> ${OUT_FILE}
done

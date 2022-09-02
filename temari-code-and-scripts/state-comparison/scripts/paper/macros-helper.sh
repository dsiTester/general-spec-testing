function project_to_macro_name() {
    project=$1
    if [ ${project} == commons-codec ]; then
        echo codec
    elif [ ${project} == commons-validator ]; then
        echo validator
    elif [ ${project} == commons-exec ]; then
        echo exec
    elif [ ${project} == commons-fileupload ]; then
        echo fileupload
    elif [ ${project} == joda-convert ]; then
        echo convert
    elif [ ${project} == kamranzafar.jtar ]; then
        echo jtar
    elif [[ ${project} == TOTAL ]]; then
        echo total
    elif [[ ${project} == AVG ]]; then
        echo avg
    fi
}

function project_to_project_name_macro() {
    local macro_header=$1
    if [[ "${macro_header}" == total ]]; then
        echo allStat
    elif [[ "${macro_header}" == avg ]]; then
        echo avgStat
    else
        echo ${macro_header}IPID
    fi
}

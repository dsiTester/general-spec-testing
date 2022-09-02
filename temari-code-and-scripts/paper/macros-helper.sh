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
    elif [[ ${project} == MIN ]]; then
        echo min
    elif [[ ${project} == MAX ]]; then
        echo max
    fi
}

function project_to_project_name_macro() {
    local macro_header=$1
    if [[ "${macro_header}" == total ]]; then
        echo allStat
    elif [[ "${macro_header}" == avg ]]; then
        echo avgStat
    elif [[ "${macro_header}" == min ]]; then
        echo minStat
    elif [[ "${macro_header}" == max ]]; then
        echo maxStat
    else
        echo ${macro_header}IPID
    fi
}

function miner_to_macro_name() {
    miner=$1
    if [[ ${miner} == bdd-three || "${miner}" == bdd-3 || "${miner}" == BDD-3 ]]; then
        echo bddThree
    elif [[ ${miner} == bdd-two || "${miner}" == bdd-2 || "${miner}" == BDD-2 ]]; then
        echo bddTwo
    elif [[ ${miner} == dice || "${miner}" == DICE ]]; then
        echo dice
    elif [[ ${miner} == dsm-randoop || "${miner}" == DSM-Randoop ]]; then
        echo dsmRandoop
    elif [[ ${miner} == dsm-unit-test || "${miner}" == dsm-manual || "${miner}" == DSM-Manual ]]; then
        echo dsmUnitTest
    elif [[ ${miner} == javert || "${miner}" == Javert ]]; then
        echo javert
    elif [[ ${miner} == texada* || "${miner}" == Texada ]]; then
        echo texada
    elif [[ ${miner} == TOTAL ]]; then
        echo total
    elif [[ ${miner} == AVG ]]; then
        echo avgStat
    fi
}

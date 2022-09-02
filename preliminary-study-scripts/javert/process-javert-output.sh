if [ $# -ne 3 ]; then
    echo "USAGE: bash $0 PROJECT_NAME JAVERT_OUTPUT_DIR SPEC_TO_AUTOMATA_DIR"
    exit
fi

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )

DATA_DIR=${SCRIPT_DIR}/data
PROJECT=$1
JAVERT_OUTPUT_DIR=$2
IN_DIR=${JAVERT_OUTPUT_DIR}/${PROJECT}
SPEC_TO_AUTOMATA_DIR=$3
LOGICREPO_PATH=${SPEC_TO_AUTOMATA_DIR}/logicrepository
WS=${SCRIPT_DIR}/${PROJECT}-ws
OUT=${DATA_DIR}/javert-fsms/${PROJECT}
LOGS_OUT=${SCRIPT_DIR}/logs/${PROJECT}
mkdir -p ${OUT}
mkdir -p ${LOGS_OUT}

function preprocess() {
    ( time python3 ${SCRIPT_DIR}/preprocess-javert-output.py ${IN_DIR} ${WS} ${PROJECT} javert ) &> ${LOGS_OUT}/gol-preprocess
}

function create_automata() {
    # package only once
    cd ${SPEC_TO_AUTOMATA_DIR}
    mvn package &> /dev/null

    for jSpec in $( ls ${WS} | grep -v spec-id-to-mined-tests.csv | rev | cut -d- -f2 | rev | sort -u ); do
        echo "converting spec: ${jSpec}"
        touch SCRIPT_NOW
        formula_file=${WS}/${PROJECT}-${jSpec}-formula.txt
        events_file=${WS}/${PROJECT}-${jSpec}-events.txt
        alias_file=${WS}/${PROJECT}-${jSpec}-aliasmap.csv
        ( time java -cp logicrepository/target/logicrepository-1.0-SNAPSHOT-jar-with-dependencies.jar com.runtimeverification.rvmonitor.logicrepository.Main ${LOGICREPO_PATH} ${events_file} ${formula_file} ere ) &> ${LOGS_OUT}/gol-convert-${jSpec}

        txtfile=`find ${SPEC_TO_AUTOMATA_DIR} -newer SCRIPT_NOW -name 'txtRepresentation*.txt'`
        dotfile=`find ${SPEC_TO_AUTOMATA_DIR} -newer SCRIPT_NOW -name '*.dot'`
        transitionfile=`find ${SPEC_TO_AUTOMATA_DIR} -newer SCRIPT_NOW -name 'transitionKey*.txt'`
        pngfile=`echo ${dotfile:2} | cut -d'.' -f1`
        pngfile="${pngfile}.png"

        rm SCRIPT_NOW

        out_txt=${OUT}/${PROJECT}-${jSpec}.txt
        out_dot=${OUT}/${PROJECT}-${jSpec}.dot
        cp ${txtfile} ${out_txt}
        cp ${dotfile} ${out_dot}

        # read from the transitions file and first replace the keys in the dot files
        while read line ; do
            letter=$( echo ${line} | cut -d: -f1 )
            alias=$( echo ${line} | cut -d' ' -f2 )
            sed -i "s:\"${letter}\":\"${alias}\":g" ${out_dot}
        done < ${transitionfile}

        while read aliasline; do
            alias=$( echo ${aliasline} | cut -d, -f1 )
            spec=$( echo ${aliasline} | cut -d, -f2 )
            # echo "command:  sed -i \"s:${alias}:${spec}:g\" ${out_dot}"
            sed -i "s:${alias}:${spec}:g" ${out_dot}
            sed -i "s:${alias}:${spec}:g" ${out_txt}
        done < <( tail -n +2 ${alias_file} )

        dot -Tpng ${out_dot} > ${OUT}/${PROJECT}-${jSpec}.png
    done
}

preprocess
create_automata
cp ${WS}/spec-id-to-mined-tests.csv ${OUT}/

jSpec=$1

log_file=${LOGS_OUT}/gol-convert-${jSpec}
echo "converting spec: ${jSpec}"
formula_file=${WS}/${PROJECT}-${jSpec}-formula.txt
events_file=${WS}/${PROJECT}-${jSpec}-events.txt
alias_file=${WS}/${PROJECT}-${jSpec}-aliasmap.csv
local_spec_to_automata=${SCRIPT_DIR}/${jSpec}-spec-to-automata
cp -r ${SPEC_TO_AUTOMATA_DIR} ${local_spec_to_automata}
cd ${local_spec_to_automata}
NOW_FILE=${local_spec_to_automata}/SCRIPT_NOW_${jSpec}
touch ${NOW_FILE}
( time java -cp logicrepository/target/logicrepository-1.0-SNAPSHOT-jar-with-dependencies.jar com.runtimeverification.rvmonitor.logicrepository.Main ${LOGICREPO_PATH} ${events_file} ${formula_file} ere ) &> ${log_file}

txtfile=`find ${local_spec_to_automata} -newer ${NOW_FILE} -name 'txtRepresentation*.txt'`
dotfile=`find ${local_spec_to_automata} -newer ${NOW_FILE} -name '*.dot'`
transitionfile=`find ${local_spec_to_automata} -newer ${NOW_FILE} -name 'transitionKey*.txt'`
pngfile=`echo ${dotfile:2} | cut -d'.' -f1`
pngfile="${pngfile}.png"

rm ${NOW_FILE}

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
    sed -i "s:${alias}\":${spec}\":g" ${out_dot}
    sed -i "s: ${alias}$: ${spec}:g" ${out_txt}
done < <( tail -n +2 ${alias_file} )

# dot -Tpng ${out_dot} > ${OUT}/${PROJECT}-${jSpec}.png

cd ${SCRIPT_DIR}
rm -rf ${local_spec_to_automata}

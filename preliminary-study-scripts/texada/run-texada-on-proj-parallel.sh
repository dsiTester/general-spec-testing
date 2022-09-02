pattern_args_file=$1

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )

echo "[BEGIN] ${pattern_args_file} " | tee -a ${SUMMARY_LOG}
pat=$( head -1 ${pattern_args_file} | cut -d' ' -f2- | cut -d\' -f2 )
out_name=$( echo ${pattern_args_file} | cut -d/ -f3- | rev | cut -d/ -f2- | rev | sed 's:\/:-:g' )
out_path=${LOGS_DIR}/gol-${out_name}
( set -o xtrace ; time timeout 130m ${SCRIPT_DIR}/texada -m -f "${pat}" --log-file ${TEXADA_TRACES_DIR}/${PROJECT}-texada-traces.txt ; set -o xtrace ) &> ${out_path}
echo "[END] ${pattern_args_file} TIME: " $( grep ^real ${out_path} )

clz=$1

echo ===${clz}
cdir=${WS}/${clz}
source activate dsm
( time timeout 2h python3 ${DSM}/DSM.py --data_dir ${cdir}/input_traces --save_dir ${cdir}/saved_model --work_dir ${cdir}/work_dir ) &> ${LOGS_DIR}/gol-dsm-${clz}
if [ -f ${cdir}/work_dir/FINAL_mindfa.txt ]; then
    cp ${cdir}/work_dir/FINAL_mindfa.txt ${OUT}/${clz}_model.txt
    cp ${cdir}/work_dir/FINAL_mindfa.eps ${OUT}/${clz}_model.eps
else
    echo "DSM did not produce a model for class ${clz}!"
fi

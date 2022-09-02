SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
GENERATED_DATA_DIR=${SCRIPT_DIR}/data/generated-data

echo "project,total-classes,classes-with-ltl,classes-with-enhanced,classes-with-fsm"
for proj in $( ls ${GENERATED_DATA_DIR} ); do
    proj_dir=${GENERATED_DATA_DIR}/${proj}
    total_classes=$( ls ${proj_dir}/logs | grep gol-summary | wc -l )
    total_classes_wo_ltl=$( grep -rl "No LTL formulae!" ${proj_dir}/logs | wc -l )
    total_classes_w_ltl=$( echo "${total_classes} - ${total_classes_wo_ltl}" | bc -l )
    # total_classes_wo_enhanced=$( grep -rl "Patching" ${proj_dir}/logs/gol-run-evosuite-2-* | wc -l )
    # total_classes_w_enhanced=$( echo "${total_classes} - ${total_classes_w_enhanced}" | bc -l )
    total_classes_w_enhanced=$( ls ${proj_dir}/results/*/*_enhanced.traces  | xargs wc -l | grep -v total | grep -v " 0" | wc -l )
    classes_with_fsm=$( find ${proj_dir}/results -name fsm.txt | wc -l )
    echo "${proj},${total_classes},${total_classes_w_ltl},${total_classes_w_enhanced},${classes_with_fsm}"
done

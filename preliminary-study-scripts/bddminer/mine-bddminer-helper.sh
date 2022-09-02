tst=$1

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )

echo "Running BDDMiner on $tst"

if [ "${NUM_LETTERS}" -eq 2 ]; then
    timeout 100m java -jar ${SCRIPT_DIR}/bddminer.jar -mine "(ab)*" ${tst}-processed.txt &> ${bdd_dir}/ab@s/gol-bdd-ab@s-${tst}
    timeout 100m java -jar ${SCRIPT_DIR}/bddminer.jar -mine "ab+" ${tst}-processed.txt &> ${bdd_dir}/abp/gol-bdd-abp-${tst}
    timeout 100m java -jar ${SCRIPT_DIR}/bddminer.jar -mine "a+b" ${tst}-processed.txt &> ${bdd_dir}/apb/gol-bdd-apb-${tst}
    timeout 100m java -jar ${SCRIPT_DIR}/bddminer.jar -mine "a*b" ${tst}-processed.txt &> ${bdd_dir}/abs/gol-bdd-abs-${tst}
    timeout 100m java -jar ${SCRIPT_DIR}/bddminer.jar -mine "ab*" ${tst}-processed.txt &> ${bdd_dir}/asb/gol-bdd-asb-${tst}
    timeout 100m java -jar ${SCRIPT_DIR}/bddminer.jar -mine "(ab?)?" ${tst}-processed.txt &> ${bdd_dir}/abq@q/gol-bdd-abq@q-${tst}
    timeout 100m java -jar ${SCRIPT_DIR}/bddminer.jar -mine "(a?b)?" ${tst}-processed.txt &> ${bdd_dir}/aqb@q/gol-bdd-aqb@q-${tst}
    timeout 100m java -jar ${SCRIPT_DIR}/bddminer.jar -mine "(ab?)*" ${tst}-processed.txt &> ${bdd_dir}/abq@s/gol-bdd-abq@s-${tst}
    timeout 100m java -jar ${SCRIPT_DIR}/bddminer.jar -mine "(a?b)*" ${tst}-processed.txt &> ${bdd_dir}/aqb@s/gol-bdd-aqb@s-${tst}
    timeout 100m java -jar ${SCRIPT_DIR}/bddminer.jar -mine "(a+b+)?" ${tst}-processed.txt &> ${bdd_dir}/apbp@q/gol-bdd-apbp@q-${tst}
    timeout 100m java -jar ${SCRIPT_DIR}/bddminer.jar -mine "(a+b*)?" ${tst}-processed.txt &> ${bdd_dir}/apbs@q/gol-bdd-apbs@q-${tst}
    timeout 100m java -jar ${SCRIPT_DIR}/bddminer.jar -mine "(a*b+)?" ${tst}-processed.txt &> ${bdd_dir}/asbp@q/gol-bdd-asbp@q-${tst}
    timeout 100m java -jar ${SCRIPT_DIR}/bddminer.jar -mine "(a+b+)*" ${tst}-processed.txt &> ${bdd_dir}/apbp@s/gol-bdd-apbp@s-${tst}
    timeout 100m java -jar ${SCRIPT_DIR}/bddminer.jar -mine "(a+b*)*" ${tst}-processed.txt &> ${bdd_dir}/apbs@s/gol-bdd-apbs@s-${tst}
    timeout 100m java -jar ${SCRIPT_DIR}/bddminer.jar -mine "(a*b+)*" ${tst}-processed.txt &> ${bdd_dir}/asbp@s/gol-bdd-asbp@s-${tst}
    ( time timeout 100m java -jar ${SCRIPT_DIR}/bddminer.jar -mine "(ab)?" ${tst}-processed.txt ) &> ${bdd_dir}/ab@q/gol-bdd-ab@q-${tst}
else
    # BDD3 specs
    ( time timeout 100m java -jar ${SCRIPT_DIR}/bddminer.jar -mine "(ab*c)*" ${tst}-processed.txt ) &> ${bdd_dir}/resource-usages-closed/gol-bdd-resource-usages-closed-${tst}
    ( time timeout 100m java -jar ${SCRIPT_DIR}/bddminer.jar -mine "(a+b*c+)?" ${tst}-processed.txt ) &> ${bdd_dir}/resource-usages-general/gol-bdd-resource-usages-general-${tst}
    ( time timeout 100m java -jar ${SCRIPT_DIR}/bddminer.jar -mine "(a+b*c+)*" ${tst}-processed.txt ) &> ${bdd_dir}/resource-usages-general-closed/gol-bdd-resource-usages-general-closed-${tst}
fi

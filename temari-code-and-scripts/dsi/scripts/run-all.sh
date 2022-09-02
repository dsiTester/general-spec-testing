proj=$1
partial_type=$2 # none, class, method
if [ ${partial_type} == "class" ]; then
    partial_class=$3
elif [ ${partial_type} == "method" ]; then
    partial_method=$3
fi

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )

bash ${SCRIPT_DIR}/install_dsi_plugin.sh

# noDSI run for baseline timing data
bash ${SCRIPT_DIR}/run-dsi.sh ${proj} -allTestsNoDSI -noBuild -noBackup
bash ${SCRIPT_DIR}/run-dsi.sh ${proj} -testClassesNoDSI -noBuild -noBackup
bash ${SCRIPT_DIR}/run-dsi.sh ${proj} -testMethodsNoDSI -noBuild -noBackup

# intra-granularity
bash ${SCRIPT_DIR}/run-dsi.sh ${proj} -allTests -noBuild -noBackup
bash ${SCRIPT_DIR}/run-dsi.sh ${proj} -testClasses -noBuild -noBackup ${partial_class} # ${partial_class} may be empty
bash ${SCRIPT_DIR}/run-dsi.sh ${proj} -testMethods -noBuild -noBackup ${partial_method} # ${partial_method} may be empty

# intra-granularity with selection (what we have so far)
bash ${SCRIPT_DIR}/run-dsi.sh ${proj} -allTests#testClasses -noBuild -noBackup
bash ${SCRIPT_DIR}/run-dsi.sh ${proj} -allTests#testMethods -noBuild -noBackup
bash ${SCRIPT_DIR}/run-dsi.sh ${proj} -testClasses#testMethods -noBuild -noBackup

# upwards inter-granularity
bash ${SCRIPT_DIR}/run-dsi.sh ${proj} -testClasses@allTests -noBuild -noBackup
bash ${SCRIPT_DIR}/run-dsi.sh ${proj} -testMethods@testClasses -noBuild -noBackup
bash ${SCRIPT_DIR}/run-dsi.sh ${proj} -testMethods@allTests -noBuild -noBackup

# downwards inter-granularity
bash ${SCRIPT_DIR}/run-dsi.sh ${proj} -allTests@testClasses -noBuild -noBackup
bash ${SCRIPT_DIR}/run-dsi.sh ${proj} -allTests@testMethods -noBuild -noBackup
bash ${SCRIPT_DIR}/run-dsi.sh ${proj} -testClasses@testMethods -noBuild -noBackup

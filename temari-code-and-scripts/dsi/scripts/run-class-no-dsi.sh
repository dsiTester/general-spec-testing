if [ $# -lt 1 ]; then
    echo "Usage: $0 TEST_CLASS_NAME"
    exit
fi

tst=$1
project_location=`dirname ${PWD}`
log_file=${project_location}/logs/gol-no-dsi-test-classes-${tst}
mkdir -p ${project_location}/logs/
SKIPS=" -Dcheckstyle.skip -Drat.skip -Denforcer.skip -Danimal.sniffer.skip -Dmaven.javadoc.skip -Dfindbugs.skip -Dwarbucks.skip -Dmodernizer.skip -Dimpsort.skip -DfailIfNoTests=false -Dmaven.main.skip"

echo "starting run... ${tst}"
echo "######################################################################" > ${log_file}
echo "RUNNING ${tst}..." >> ${log_file}
echo "######################################################################" >> ${log_file}
export MAVEN_OPTS="-Xmx30G -Xss16G -XX:MaxPermSize=28GM"

echo "Running the following command... mvn surefire:test ${SKIPS} -DtempDir=${tst} -Dtest=${tst} >> ${log_file} 2>&1"
( time mvn surefire:test ${SKIPS} -DtempDir=${tst} -Dtest=${tst} ) >> ${log_file} 2>&1

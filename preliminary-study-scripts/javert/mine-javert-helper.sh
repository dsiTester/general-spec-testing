tst=$1

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
REPO_DIR=$( dirname "${SCRIPT_DIR}" )

echo "Running Javert on $tst"

if [ ! -f ${tst}.txt ]; then
    gunzip ${tst}.txt.gz
fi

timeout 100m java -jar ${SCRIPT_DIR}/javert.jar -flat ${tst}.txt &> ${javert_dir}/gol-javert-${tst}

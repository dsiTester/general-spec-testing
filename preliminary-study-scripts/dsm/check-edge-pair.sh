# checks whether the passed-in edge-pair was one of the 2001 that we inspected.
if [ ! $# -eq 3 ]; then
    echo "USAGE: $0 method-a method-b project"
    exit
fi

METHOD_A=$1
METHOD_B=$2
PROJECT=$3

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname $( dirname ${SCRIPT_DIR} ) ) # location of spec-exploration repo root
DSI_SPECS_DIR=${BASE_DIR}/data/dsi-spec-verdicts

# our search string is method-a,method-b
# for grep to work, we need to escape some characters
# echo is print, sed 's/STR_TO_REPLACE/REPLACEMENT_STR/g' replaces all occurences of STR_TO_REPLACE with REPLACEMENT_STR
search_string=$( echo "${METHOD_A},${METHOD_B}" | sed 's/\[/\\[/g' | sed 's/\$/\\$/g' )

# grep searches through a file for a matching occurence. We want to check the corresponding file for the project
# grep "${search_string}" ${DSI_SPECS_DIR}/${PROJECT}.csv

# output if we see the spec or not
if grep -q "${search_string}" ${DSI_SPECS_DIR}/${PROJECT}.csv ; then
    echo "Spec was inspected"
else
    echo "Spec was NOT inspected"
fi

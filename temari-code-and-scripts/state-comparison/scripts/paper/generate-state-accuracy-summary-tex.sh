#!/bin/sh

if [ $# -lt 1 ]; then
    echo "USAGE: $0 PAPER_DIR"
    exit
fi

PAPER_DIR=$1
SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname $( dirname ${SCRIPT_DIR} ) )
IN_FILE=${BASE_DIR}/data/initial-experiments/results/state-summary-for-paper.csv
MACROS_IDENT=stateAwareAccuracySummary # script-specific macro header to prevent crashes

REORGANIZED_IN_FILE=${BASE_DIR}/data/analysis-data/reorganized-state-summary.csv

head -1 ${IN_FILE} > ${REORGANIZED_IN_FILE}
for entry in $( cat ${PAPER_DIR}/ws/summary_order.txt ); do
    grep ^${entry} ${IN_FILE} >> ${REORGANIZED_IN_FILE}
done
grep ^TOTAL ${IN_FILE} >> ${REORGANIZED_IN_FILE}
# grep ^AVG ${IN_FILE} >> ${REORGANIZED_IN_FILE}

MACROS_DIR=${PAPER_DIR}/generated-macros
TABLES_DIR=${PAPER_DIR}/tables

MACROS_FILE_IDENT=dsi_accuracy_summary_table_macros
MACROS_OUT=${MACROS_DIR}/state_accuracy_summary_table_macros.tex
MACROS_STARTER=${MACROS_DIR}/state_accuracy_summary_table_macros_starter.tex
TABLES_OUT=${TABLES_DIR}/state_accuracy_summary_table.tex

( cat ${MACROS_STARTER} ) >${MACROS_OUT}
>${TABLES_OUT}

source ${SCRIPT_DIR}/macros-helper.sh
source ${BASE_DIR}/scripts/helper.sh

# place our macros file in, if it doesn't exist
if ! grep -q ${MACROS_FILE_IDENT} ${MACROS_DIR}/all_macro_files.tex; then
    echo "\input{generated-macros/${MACROS_FILE_IDENT}}" >> ${MACROS_DIR}/all_macro_files.tex
fi

#         while IFS=, read project dsi_accurate sdsi_accurate total dsi_per_accurate sdsi_per_accurate ; do
function make_header() {
    echo "
\begin{table}[t!]
\small
\caption{Accuracy Comparison of \dsi and State-Aware \dsi.}
  \vspace{-13pt}
\begin{center}
\begin{tabular}{|c|r|r|r|r|r|}
\hline
\multirow{2}{*}{\pid} & \multirow{2}{*}{\total} & \multicolumn{2}{|c|}{\\${MACROS_IDENT}numCorrect} & \multicolumn{2}{|c|}{\\${MACROS_IDENT}percentCorrectForTable} \\\\
"
echo "
\cline{3-6}
& & \dsi & \sdsi & \dsi & \sdsi \\\\
\hline
\hline"

}

function make_footer() {
    echo "\hline
\end{tabular}
\label{tabular:output:state_aware_accuracy}
\end{center}
\vspace{-10pt}
\end{table}
"
}

function make_body() {
    {
        read
        while IFS=, read project dsi_accurate sdsi_accurate total dsi_per_accurate sdsi_per_accurate ; do
            # [[ ${miner} == AVG ]] && continue
            macro_header=$( project_to_macro_name ${project} )
            project_name_macro=$( project_to_project_name_macro ${macro_header} )
            macro_header="${MACROS_IDENT}${macro_header}" # making the macro headers unique
            if [ "${project}" == "TOTAL" ]; then
                echo "\hline"
                echo "\hline"
            fi
            echo "\\${project_name_macro} & \\${macro_header}StateTotalSpecs & \\${macro_header}DsiAccurateNum & \\${macro_header}SdsiAccurateNum & \\${macro_header}DsiAccuratePercent & \\${macro_header}SdsiAccuratePercent \\\\"
            echo "\newcommand{\\${macro_header}DsiAccurateNum}{${dsi_accurate}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}SdsiAccurateNum}{${sdsi_accurate}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}StateTotalSpecs}{${total}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}DsiAccuratePercent}{$( round ${dsi_per_accurate} 1)}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}SdsiAccuratePercent}{$( round ${sdsi_per_accurate} 1)}" >> ${MACROS_OUT}
        done
    } <${REORGANIZED_IN_FILE}

}

(
make_header
make_body
make_footer
) > ${TABLES_OUT}

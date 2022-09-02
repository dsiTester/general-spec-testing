#!/bin/sh

if [ $# -lt 1 ]; then
    echo "USAGE: $0 PAPER_DIR"
    exit
fi

PAPER_DIR=$1
SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname $( dirname ${SCRIPT_DIR} ) )
IN_FILE=${BASE_DIR}/data/initial-experiments/results/original-dsi-accuracy-summary-for-paper.csv
MACROS_IDENT=originalDsiAccuracySummary # script-specific macro header to prevent crashes

MACROS_DIR=${PAPER_DIR}/generated-macros
TABLES_DIR=${PAPER_DIR}/tables

MACROS_FILE_IDENT=dsi_accuracy_summary_table_macros
MACROS_OUT=${MACROS_DIR}/dsi_accuracy_summary_table_macros.tex
MACROS_STARTER=${MACROS_DIR}/dsi_accuracy_summary_table_macros_starter.tex
TABLES_OUT=${TABLES_DIR}/dsi_accuracy_summary_table.tex

( cat ${MACROS_STARTER} ) > ${MACROS_OUT}
>${TABLES_OUT}

source ${SCRIPT_DIR}/macros-helper.sh

# place our macros file in, if it doesn't exist
if ! grep -q ${MACROS_FILE_IDENT} ${MACROS_DIR}/all_macro_files.tex; then
    echo "\input{generated-macros/${MACROS_FILE_IDENT}}" >> ${MACROS_DIR}/all_macro_files.tex
fi

function make_header() {
    echo "
\begin{table}[t!]
\scriptsize
\caption{Accuracy of \dsi.}
  \vspace{-13pt}
\begin{center}
\begin{tabular}{|c|r|r|r|r|r|}
\hline
\multirow{2}{*}{\pid} & \multirow{2}{*}{\total} & \multirow{2}{*}{\numCorrect} & \multirow{2}{*}{\percentCorrectForTable} & \multicolumn{2}{|c|}{\hitrate} \\\\
"
echo "
\cline{5-6}
& & & & \manualT & \manualN \\\\
\hline
\hline"

}

function make_footer() {
    echo "\hline
\end{tabular}
\label{tabular:output:dsi_accuracy}
\end{center}
\vspace{-10pt}
\end{table}
"
}

function make_body() {
    {
        read
        while IFS=, read project total num_accurate percent_accurate hit_ts hit_ns ; do
            # [[ ${miner} == AVG ]] && continue
            macro_header=$( project_to_macro_name ${project} )
            project_name_macro=$( project_to_project_name_macro ${macro_header} ) # need to use original macro header to find the project name
            macro_header="${MACROS_IDENT}${macro_header}" # making the macro headers unique
            if [ "${project}" == "TOTAL" ]; then
                echo "\hline"
                echo "\hline"
                echo "\\${project_name_macro} & \\${macro_header}TotalSpecs & \\${macro_header}DsiAccurateNum & \cellcolor{gray} \\${macro_header}DsiAccuratePercent & \\${macro_header}DsiTrueSpecHit & \cellcolor{gray} \\${macro_header}DsiSpuriousSpecHit \\\\"
            elif [ "${project}" == "AVG" ]; then
                echo "\\${project_name_macro} & \\${macro_header}TotalSpecs & \\${macro_header}DsiAccurateNum & - & - & - \\\\"
            else
                echo "\\${project_name_macro} & \\${macro_header}TotalSpecs & \\${macro_header}DsiAccurateNum & \\${macro_header}DsiAccuratePercent & \\${macro_header}DsiTrueSpecHit & \\${macro_header}DsiSpuriousSpecHit \\\\"
            fi
            echo "\newcommand{\\${macro_header}DsiAccurateNum}{${num_accurate}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}DsiAccuratePercent}{${percent_accurate}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}TotalSpecs}{${total}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}DsiTrueSpecHit}{${hit_ts}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}DsiSpuriousSpecHit}{${hit_ns}}" >> ${MACROS_OUT}
        done
    } <${IN_FILE}

}

(
make_header
make_body
make_footer
) > ${TABLES_OUT}

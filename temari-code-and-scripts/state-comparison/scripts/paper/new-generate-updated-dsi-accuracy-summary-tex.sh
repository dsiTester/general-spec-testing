#!/bin/sh

if [ $# -lt 1 ]; then
    echo "USAGE: $0 PAPER_DIR"
    exit
fi

PAPER_DIR=$1
SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname $( dirname ${SCRIPT_DIR} ) )
IN_FILE=${BASE_DIR}/data/analysis-data/new-state-and-dsi-accuracies.csv
# hack. should I do this here or elsewhere?
REORGANIZED_IN_FILE=${BASE_DIR}/data/analysis-data/reorganized-new-state-and-dsi-accuracies.csv
REORGANIZED_IN_FILE_TEMP=${BASE_DIR}/data/analysis-data/tmp-reorganized-new-state-and-dsi-accuracies.csv

source ${BASE_DIR}/scripts/helper.sh

head -1 ${IN_FILE} > ${REORGANIZED_IN_FILE}
for entry in $( cat ${PAPER_DIR}/ws/summary_order.txt ); do
    grep ^${entry} ${IN_FILE} >> ${REORGANIZED_IN_FILE}
done
cp ${REORGANIZED_IN_FILE} ${REORGANIZED_IN_FILE_TEMP}
dos2unix ${REORGANIZED_IN_FILE_TEMP}
compute_all_stats ${REORGANIZED_IN_FILE_TEMP}
grep ^TOTAL ${IN_FILE} >> ${REORGANIZED_IN_FILE}
dos2unix ${REORGANIZED_IN_FILE}
grep ^AVG ${REORGANIZED_IN_FILE_TEMP} >> ${REORGANIZED_IN_FILE}
rm ${REORGANIZED_IN_FILE_TEMP}


MACROS_IDENT=newUpdatedDsiAccuracySummary # script-specific macro header to prevent crashes

MACROS_DIR=${PAPER_DIR}/generated-macros
TABLES_DIR=${PAPER_DIR}/tables

MACROS_FILE_IDENT=new_updated_dsi_accuracy_summary_table_macros
MACROS_OUT=${MACROS_DIR}/new_updated_dsi_accuracy_summary_table_macros.tex
MACROS_STARTER=${MACROS_DIR}/new_updated_dsi_accuracy_summary_table_macros_starter.tex
TABLES_OUT=${TABLES_DIR}/new_updated_dsi_accuracy_summary_table.tex

( cat ${MACROS_STARTER} ) > ${MACROS_OUT}
 # > ${MACROS_OUT}
>${TABLES_OUT}

source ${SCRIPT_DIR}/macros-helper.sh

# place our macros file in, if it doesn't exist
if ! grep -q ${MACROS_FILE_IDENT} ${MACROS_DIR}/all_macro_files.tex; then
    echo "\input{generated-macros/${MACROS_FILE_IDENT}}" >> ${MACROS_DIR}/all_macro_files.tex
fi

function make_header_v1() {
# \begin{table}[t!]
# \small
# \caption{Accuracy of \dsi.}
#   \vspace{-13pt}
# \begin{center}

    echo "
\begin{tabular}{|c|r|r|r|r|r|r|r|}
\hline
\multirow{2}{*}{\pid} & \multirow{2}{*}{\total} & \multirow{2}{*}{\percentCorrectForTable} & \multicolumn{2}{|c|}{\percentPrecision} & \multicolumn{2}{|c|}{\percentRecall} \\\\
"
echo "
\cline{4-7}
& & & \\${MACROS_IDENT}True & \\${MACROS_IDENT}Spurious & \\${MACROS_IDENT}True & \\${MACROS_IDENT}Spurious  \\\\
\hline
\hline"

}

function make_header() {
    echo "
\begin{table}[t!]
\footnotesize
\caption{Accuracy of \dsiplus.}
  \vspace{-13pt}
\begin{center}
\begin{tabular}{|c|r|r|r|r|r|r|r|}
\hline
\multirow{2}{*}{\pid} & \multirow{2}{*}{\total} & \multirow{2}{*}{\percentCorrectForTable} & \multicolumn{2}{|c|}{\\${MACROS_IDENT}True} & \multicolumn{2}{|c|}{\\${MACROS_IDENT}Spurious} \\\\
"
echo "
\cline{4-7}
& & & \percentPrecision & \percentRecall & \percentPrecision & \percentRecall \\\\
\hline
\hline"

}

function make_footer() {
    echo "\hline
\end{tabular}
\label{tabular:output:new_updated_dsi_accuracy}
\end{center}
\vspace{-10pt}
\end{table}
"
#  \caption{\label{tabular:output:new_dsi_accuracy}(In)accuracy of \dsi.}

}

function make_body() {
    {
        read
        # while IFS=, read project total num_accurate percent_accurate hit_ts hit_ns ; do
        while IFS=, read project total_specs accurate_true_specs accurate_spurious_specs total_accurate acc_percent true_spec_precision spurious_spec_precision true_spec_recall spurious_spec_recall dsi_lv dsi_ls man_ts man_ss; do
            # [[ ${miner} == AVG ]] && continue
            macro_header=$( project_to_macro_name ${project} )
            project_name_macro=$( project_to_project_name_macro ${macro_header} ) # need to use original macro header to find the project name
            macro_header="${MACROS_IDENT}${macro_header}" # making the macro headers unique
            if [ "${project}" == "TOTAL" ]; then
                echo "\hline"
                echo "\hline"
                # echo "\\${project_name_macro} & \\${macro_header}TotalSpecs & - & - & - & - & - \\\\"
                # echo "\\${project_name_macro} & \\${macro_header}TotalSpecs & \\${macro_header}DsiAccuratePercent & \\${macro_header}DsiTrueSpecPrecision & \\${macro_header}DsiSpuriousSpecPrecision & \\${macro_header}DsiTrueSpecRecall & \\${macro_header}DsiSpuriousSpecRecall \\\\"
                echo "\\${project_name_macro} & \\${macro_header}TotalSpecs & \colorbox{gray} \\${macro_header}DsiAccuratePercent & \\${macro_header}DsiTrueSpecPrecision & \\${macro_header}DsiTrueSpecRecall & \colorbox{gray}\\${macro_header}DsiSpuriousSpecPrecision & \\${macro_header}DsiSpuriousSpecRecall \\\\"
            # elif [ "${project}" == "AVG" ]; then
            #     echo "\\${project_name_macro} & \\${macro_header}TotalSpecs & - & - & - & - & - \\\\"
            else
                # echo "\\${project_name_macro} & \\${macro_header}TotalSpecs & \\${macro_header}DsiAccuratePercent & \\${macro_header}DsiTrueSpecPrecision & \\${macro_header}DsiSpuriousSpecPrecision & \\${macro_header}DsiTrueSpecRecall & \\${macro_header}DsiSpuriousSpecRecall \\\\"
                echo "\\${project_name_macro} & \\${macro_header}TotalSpecs & \\${macro_header}DsiAccuratePercent & \\${macro_header}DsiTrueSpecPrecision & \\${macro_header}DsiTrueSpecRecall & \\${macro_header}DsiSpuriousSpecPrecision & \\${macro_header}DsiSpuriousSpecRecall \\\\"
            fi
            echo "\newcommand{\\${macro_header}DsiAccurateNum}{${total_accurate}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}DsiAccuratePercent}{${acc_percent}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}TotalSpecs}{${total_specs}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}DsiTrueSpecPrecision}{${true_spec_precision}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}DsiSpuriousSpecPrecision}{${spurious_spec_precision}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}DsiTrueSpecRecall}{${true_spec_recall}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}DsiSpuriousSpecRecall}{${spurious_spec_recall}}" >> ${MACROS_OUT}
        done
    } <${REORGANIZED_IN_FILE}

}

(
make_header
make_body
make_footer
) > ${TABLES_OUT}

rm ${REORGANIZED_IN_FILE}

#!/bin/sh

if [ $# -lt 1 ]; then
    echo "USAGE: $0 PAPER_DIR"
    exit
fi

PAPER_DIR=$1
SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname $( dirname ${SCRIPT_DIR} ) )
IN_FILE=${BASE_DIR}/data/analysis-data/static-vs-dynamic.csv
# hack. should I do this here or elsewhere?
REORGANIZED_IN_FILE=${BASE_DIR}/data/analysis-data/reorganized-static-vs-dynamic.csv

source ${BASE_DIR}/scripts/helper.sh

head -1 ${IN_FILE} > ${REORGANIZED_IN_FILE}
for entry in $( cat ${PAPER_DIR}/ws/summary_order.txt ); do
    grep ^${entry} ${IN_FILE} >> ${REORGANIZED_IN_FILE}
done
compute_all_stats ${REORGANIZED_IN_FILE}

MACROS_IDENT=staticVsDynamic # script-specific macro header to prevent crashes

MACROS_DIR=${PAPER_DIR}/generated-macros
TABLES_DIR=${PAPER_DIR}/tables

FILE_IDENT=static_vs_dynamic
MACROS_FILE_IDENT=${FILE_IDENT}_macros
MACROS_OUT=${MACROS_DIR}/${MACROS_FILE_IDENT}.tex
MACROS_STARTER=${MACROS_DIR}/${MACROS_FILE_IDENT}_starter.tex
TABLES_OUT=${TABLES_DIR}/${FILE_IDENT}_table.tex

( cat ${MACROS_STARTER} ) > ${MACROS_OUT}
>${TABLES_OUT}

source ${SCRIPT_DIR}/macros-helper.sh

# place our macros file in, if it doesn't exist
if ! grep -q ${MACROS_FILE_IDENT} ${MACROS_DIR}/all_macro_files.tex; then
    echo "\input{generated-macros/${MACROS_FILE_IDENT}}" >> ${MACROS_DIR}/all_macro_files.tex
fi

function make_header() {
    echo "
\begin{table}[h!]
\small
\caption{Static (S) vs. dynamic (D) state checking in \dsiplus.}
  \vspace{-13pt}
\begin{center}
\begin{tabular}{|c|r|r|r|r|r|}
\hline
\pid & \\${MACROS_IDENT}StaticNotDynamic & \\${MACROS_IDENT}NotStaticDynamic & \\${MACROS_IDENT}StaticAndDynamic & \\${MACROS_IDENT}NeitherStaticDynamic & \\${MACROS_IDENT}Total \\\\
"
# \multirow{2}{*}{\pid} & \multirow{2}{*}{\} & \multirow{2}{*}{\percentCorrectForTable} & \multicolumn{2}{|c|}{\percentPrecision} & \multicolumn{2}{|c|}{\percentRecall} \\\\
# \cline{4-7}
# & & & \\${MACROS_IDENT}True & \\${MACROS_IDENT}Spurious & \\${MACROS_IDENT}True & \\${MACROS_IDENT}Spurious  \\\\
echo "
\hline
\hline"

}

function make_footer() {
    echo "\hline
\end{tabular}
\label{tabular:output:static_vs_dynamic}
\end{center}
\vspace{-10pt}
\end{table}
"
}

function make_body() {
    {
        read
        # while IFS=, read project total num_accurate percent_accurate hit_ts hit_ns ; do
        # id,state-compile-time,state-run-time,jdk-weave-time,jdk-run-compile,jdk-run-time,total-state-time(wo/JDK),total-state-time(w/JDK),total-state-time(w/JDK+DSI),only-dsi-time(seq)
        while IFS=, read project static_not_dynamic not_static_dynamic static_and_dynamic neither_static_dynamic total; do
            # [[ ${miner} == AVG ]] && continuee
            macro_header=$( project_to_macro_name ${project} )
            project_name_macro=$( project_to_project_name_macro ${macro_header} ) # need to use original macro header to find the project name
            macro_header="${MACROS_IDENT}${macro_header}" # making the macro headers unique
            if [ "${project}" == "TOTAL" ]; then
                echo "\hline"
                echo "\hline"
            fi
            echo "\\${project_name_macro} & \\${macro_header}StaticNotDynamic & \\${macro_header}NotStaticDynamic & \\${macro_header}StaticAndDynamic & \\${macro_header}NeitherStaticDynamic & \\${macro_header}Total \\\\"
            echo "\newcommand{\\${macro_header}StaticNotDynamic}{${static_not_dynamic}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}NotStaticDynamic}{${not_static_dynamic}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}StaticAndDynamic}{${static_and_dynamic}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}NeitherStaticDynamic}{${neither_static_dynamic}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}Total}{${total}}" >> ${MACROS_OUT}
        done
    } <${REORGANIZED_IN_FILE}

}

(
make_header
make_body
make_footer
) > ${TABLES_OUT}

rm ${REORGANIZED_IN_FILE}

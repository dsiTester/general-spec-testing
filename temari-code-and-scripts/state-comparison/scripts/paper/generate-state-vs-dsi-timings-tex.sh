#!/bin/sh

if [ $# -lt 1 ]; then
    echo "USAGE: $0 PAPER_DIR"
    exit
fi

PAPER_DIR=$1
SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname $( dirname ${SCRIPT_DIR} ) )
IN_FILE=${BASE_DIR}/data/paz-timings/all-projects-timings.csv
# hack. should I do this here or elsewhere?
REORGANIZED_IN_FILE=${BASE_DIR}/data/analysis-data/reorganized-timings.csv

source ${BASE_DIR}/scripts/helper.sh

head -1 ${IN_FILE} > ${REORGANIZED_IN_FILE}
for entry in $( cat ${PAPER_DIR}/ws/summary_order.txt ); do
    grep ^${entry} ${IN_FILE} >> ${REORGANIZED_IN_FILE}
done
grep ^TOTAL ${IN_FILE} >> ${REORGANIZED_IN_FILE}
grep ^AVG ${IN_FILE} >> ${REORGANIZED_IN_FILE}

# dos2unix ${REORGANIZED_IN_FILE}

MACROS_IDENT=dsiStateTimings # script-specific macro header to prevent crashes

MACROS_DIR=${PAPER_DIR}/generated-macros
TABLES_DIR=${PAPER_DIR}/tables

MACROS_FILE_IDENT=dsi_state_timings_macros
MACROS_OUT=${MACROS_DIR}/dsi_state_timings_macros.tex
MACROS_STARTER=${MACROS_DIR}/dsi_state_timings_macros_starter.tex
TABLES_OUT=${TABLES_DIR}/dsi_state_timings_table.tex

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
\small
\caption{Timings of \dsi and State-Aware \dsi in seconds.}
  \vspace{-13pt}
\begin{center}
\begin{tabular}{|c|r|r|r|r|}
\hline
\pid & \dsiStateTimingsNumSpecs & \dsiStateTimingsStateTimeWoDSI & \dsiStateTimingsStateTimesWDSI & \dsiStateTimingsDSITime \\\\
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
\label{tabular:output:dsi_state_timings}
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
        while IFS=, read project state_compile_time state_run_time jdk_weave_time jdk_run_compile jdk_run_time total_state_time_wo_JDK total_state_time_w_JDK total_state_time_w_JDK_DSI only_dsi_time_seq num_specs; do
            # [[ ${miner} == AVG ]] && continue
            macro_header=$( project_to_macro_name ${project} )
            project_name_macro=$( project_to_project_name_macro ${macro_header} ) # need to use original macro header to find the project name
            macro_header="${MACROS_IDENT}${macro_header}" # making the macro headers unique
            if [ "${project}" == "TOTAL" ]; then
                echo "\hline"
                echo "\hline"
            fi
            echo "\\${project_name_macro} & \\${macro_header}NumSpecs & \\${macro_header}StateTimeWoDSI & \\${macro_header}StateTimesWDSI & \\${macro_header}DSITime \\\\"
            echo "\newcommand{\\${macro_header}NumSpecs}{${num_specs}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}StateTimeWoDSI}{$( round ${total_state_time_w_JDK} 1)}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}StateTimesWDSI}{$( round ${total_state_time_w_JDK_DSI} 1)}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}DSITime}{$( round ${only_dsi_time_seq} 1)}" >> ${MACROS_OUT}
        done
    } <${REORGANIZED_IN_FILE}

}

(
make_header
make_body
make_footer
) > ${TABLES_OUT}

rm ${REORGANIZED_IN_FILE}

#!/bin/bash

if [ $# -ne 1 ]; then
    echo "USAGE: $0 OUT_DIR"
    exit
fi

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname $( dirname ${SCRIPT_DIR} ) )
IN=${BASE_DIR}/data/initial-experiments/results/state-summary-for-paper.csv
OUT_DIR=$1

#  Rows are \dsi outcomes. Columns are outcomes from manual inspection.
function make_header() {
    mode=$1
    caption="Accuracy of \dsi and state-aware \dsi"
    table_op="t!"
    echo "
\begin{table}[${table_op}]
\caption{${caption}}
  \vspace{-13pt}
\begin{center}
\begin{tabular}{|c|r|r|r|r|r|}
\hline
Project & \dsi & S\dsi & \$\Sigma\$ & \dsi(\$\%\$) & S\dsi(\$\%\$) \\\\
"
# \multirow{2}{*}{Project} & \multicolumn{2}{|c|}{\manualT} & \multirow{2}{*}{\manualN} & \multirow{2}{*}{\nbp} & \multirow{2}{*}{\manualU} & \multirow{2}{*}{\$\Sigma\$} \\\\
# \cline{2-3}
# & \alwaysT & \sometimesT & & & & \\\\
echo "
\hline
\hline"

}

function make_footer() {
    local mode=$1
    label_name="state_accuracy"
    echo "\hline
\end{tabular}
\label{tabular:output:${label_name}}
\end{center}
\vspace{-10pt}
\end{table}
"
}

function make_body() {
    in_file=$1 # ${PAPER_DATA}/dsi-dsi-plus-outcomes.csv
    {
        read
        while IFS=, read proj dsi_acc sdsi_acc total dsi_percent sdsi_percent; do
            echo "${proj} & ${dsi_acc} & ${sdsi_acc} & ${total} & ${dsi_percent} & ${sdsi_percent} \\\\"
            # if [ ${proj} == "TOTAL" ]; then
            #     echo "\hline"
            #     echo "\hline"

            # elif [[ "${dsi_outcome}" == "likely-valid" || "${dsi_outcome}" == "LV" ]]; then
            #     echo "${macro} & \cellcolor{VeryLightGray} ${ts} & \cellcolor{VeryLightGray} ${sts} & ${ss} & ${nbp} & ${u} & ${total} \\\\"
            # elif [[ "${dsi_outcome}" == "likely-spurious" ||  "${dsi_outcome}" == "LS" ]]; then
            #     echo "${macro} & ${ts} & ${sts} &\cellcolor{VeryLightGray} ${ss} & ${nbp} & ${u} & ${total} \\\\"
            # elif [[ "${dsi_outcome}" == "unknown" ||  "${dsi_outcome}" == "U" ]]; then
            #     echo "${macro} & ${ts} & ${sts} & ${ss} &\cellcolor{VeryLightGray} ${nbp} & ${u} & ${total} \\\\"
            # else
	    #     echo "${macro} & ${ts} & ${sts} & ${ss} & ${nbp} & ${u} & ${total} \\\\"
            # fi
        done
    } <${in_file}
}

function make_paper_tex() {
    local file=$1
    # dos2unix $file
    make_header
    make_body $file
    make_footer
}

make_paper_tex ${IN} > ${OUT_DIR}/state-summary.tex
# make_paper_tex ${ANALYSIS_DIR}/global-outcomes.csv "" "" > ${OUT_DIR}/global-outcomes.tex
# make_paper_tex ${ANALYSIS_DIR}/global-outcomes-dsiPlus.csv "dsi++" "_dsi_with_cross_gran" > ${OUT_DIR}/global-outcomes-dsiPlus.tex

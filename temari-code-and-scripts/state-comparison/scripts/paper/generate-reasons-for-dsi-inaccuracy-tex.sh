#!/bin/sh

if [ $# -lt 1 ]; then
    echo "USAGE: $0 PAPER_DIR"
    exit
fi

PAPER_DIR=$1
SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname $( dirname ${SCRIPT_DIR} ) )
IN_FILE=${BASE_DIR}/data/analysis-data/reasons-for-dsi-inaccuracy.csv

MACROS_IDENT=dsiInaccuracyReasons # script-specific macro header to prevent crashes

MACROS_DIR=${PAPER_DIR}/generated-macros
TABLES_DIR=${PAPER_DIR}/tables

MACROS_FILE_IDENT=dsi-inaccuracy-reasons-macros
MACROS_OUT=${MACROS_DIR}/dsi-inaccuracy-reasons-macros.tex
MACROS_STARTER=${MACROS_DIR}/dsi-inaccuracy-reasons-macros-starter.tex
TABLES_OUT=${TABLES_DIR}/dsi-inaccuracy-reasons.tex

( cat ${MACROS_STARTER} ) > ${MACROS_OUT}
>${TABLES_OUT}

source ${SCRIPT_DIR}/macros-helper.sh
source ${BASE_DIR}/scripts/helper.sh

echo hi
# place our macros file in, if it doesn't exist
if ! grep -q ${MACROS_FILE_IDENT} ${MACROS_DIR}/all_macro_files.tex; then
    echo "\input{generated-macros/${MACROS_FILE_IDENT}}" >> ${MACROS_DIR}/all_macro_files.tex
fi

function make_header() {
echo "\begin{table}[h!]
\footnotesize
\caption{Reasons for \dsi inaccuracy.} %  [majority of mixed; SOMETIMES_TRUE_SPEC is true spec]
\vspace{-13pt}
\begin{center}
\begin{tabular}{|l|l|r|r|r|r|}
\hline
Reason & \total & \perSpurious & \percentNoIntersect & \percentToolNoIntersect \\\\
\hline
\hline"
}

function make_footer() {
    echo "\hline
\end{tabular}
\label{table:dsi:inaccuracy}
\end{center}
\end{table}
"
}

function make_body() {
    {
        read
        # while IFS=, read project total num_accurate percent_accurate hit_ts hit_ns ; do
        while IFS=, read reason total spurious noshare tool_noshare; do
            macro_header=${MACROS_IDENT}${reason}
            echo "\\${macro_header} & \\${macro_header}Total & \\${macro_header}Spurious & \\${macro_header}NoShare & \\${macro_header}ToolNoShare \\\\"
            echo "\newcommand{\\${macro_header}Total}{${total}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}Spurious}{$( round ${spurious} 1 )}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}NoShare}{$( round ${noshare} 1 )}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}ToolNoShare}{$( round ${tool_noshare} 1)}" >> ${MACROS_OUT}
        done
    } <${IN_FILE}

}

(
make_header
make_body
make_footer
) > ${TABLES_OUT}

#!/bin/sh

if [ $# -lt 1 ]; then
    echo "USAGE: $0 PAPER_DIR"
    exit
fi

PAPER_DIR=$1
SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname $( dirname ${SCRIPT_DIR} ) )
IN_FILE=${BASE_DIR}/data/limit-study/limit-study-times.csv
FILE_WITH_ORDERINGS=${BASE_DIR}/data/limit-study/ordered-limit-study-outcomes.csv
REORGANIZED_IN_FILE=${BASE_DIR}/data/limit-study/reorganized-timings.csv

source ${SCRIPT_DIR}/macros-helper.sh
source ${SCRIPT_DIR}/helper.sh

head -1 ${IN_FILE} > ${REORGANIZED_IN_FILE}
for entry in $( cat ${FILE_WITH_ORDERINGS} | cut -d, -f1 ); do
    grep ^${entry}, ${IN_FILE} >> ${REORGANIZED_IN_FILE}
done
if ! grep -q ^TOTAL ${REORGANIZED_IN_FILE} ; then
    grep ^TOTAL ${IN_FILE} >> ${REORGANIZED_IN_FILE}
fi
if ! grep -q ^AVG ${REORGANIZED_IN_FILE} ; then
    grep ^AVG ${IN_FILE} >> ${REORGANIZED_IN_FILE}
fi
compute_min ${REORGANIZED_IN_FILE}
compute_max ${REORGANIZED_IN_FILE}

# dos2unix ${REORGANIZED_IN_FILE}

MACROS_IDENT=limitStudyTimings # script-specific macro header to prevent crashes

MACROS_DIR=${PAPER_DIR}/generated-macros
TABLES_DIR=${PAPER_DIR}/tables

MACROS_FILE_IDENT=limit_study_timings_macros
MACROS_OUT=${MACROS_DIR}/limit_study_timings_macros.tex
MACROS_STARTER=${MACROS_DIR}/limit_study_timings_macros_starter.tex
TABLES_OUT=${TABLES_DIR}/limit_study_timings_table.tex

( cat ${MACROS_STARTER} ) > ${MACROS_OUT}
>${TABLES_OUT}


# place our macros file in, if it doesn't exist
if ! grep -q ${MACROS_FILE_IDENT} ${MACROS_DIR}/all_macro_files.tex; then
    echo "\input{generated-macros/${MACROS_FILE_IDENT}}" >> ${MACROS_DIR}/all_macro_files.tex
fi

function make_header() {
    echo "
\begin{table}[t!]
\tiny
\caption{Timings in the limit study in seconds.}
  \vspace{-13pt}
\begin{center}
\begin{tabular}{|c|c|l|r|r|r|}
\hline
\\${MACROS_IDENT}Miner & \\${MACROS_IDENT}Project & \\${MACROS_IDENT}Ident & \\${MACROS_IDENT}ConstraintGen & \\${MACROS_IDENT}ConstraintFiltering & \\${MACROS_IDENT}DsiPlus \\\\
"
echo "
\hline
\hline"

}

function make_footer() {
    echo "\hline
\end{tabular}
\label{tabular:output:limit_study_timings}
\end{center}
\vspace{-10pt}
\end{table}
"
}

function convert_nums_to_words() { # macros don't take numbers :(
    # echo $1 | sed 's/0/zero/g' | sed 's/1/one/g' | sed 's/2/two/g' | sed 's/3/three/g' | sed 's/4/four/g' | sed 's/5/five/g' | sed 's/6/six/g' | sed 's/7/seven/g' | sed 's/8/eight/g' | sed 's/9/nine/g'
    echo $1 | sed 's/0/z/g' | sed 's/1/on/g' | sed 's/2/tw/g' | sed 's/3/th/g' | sed 's/4/fo/g' | sed 's/5/fi/g' | sed 's/6/six/g' | sed 's/7/sev/g' | sed 's/8/e/g' | sed 's/9/n/g'

}

function make_body() {
    acc=0
    {
        read
        while IFS=, read spec_ident constraint_gen constraint_filter dsiplus; do
            miner=$( echo "${spec_ident}" | cut -d@ -f1 )
            miner_macro=$( echo "${miner}" | sed 's/-//g' )
            miner_macro=$( convert_nums_to_words ${miner_macro} )
            project=$( echo "${spec_ident}" | cut -d@ -f2 )
            name=$( echo "${spec_ident}" | cut -d@ -f3 | rev | cut -d. -f1 | cut -d- -f1 | sed 's/\$//g' | rev )
            converted_name=$( convert_nums_to_words ${name} )
            macro_header=$( project_to_macro_name ${project} )
            project_name_macro=$( project_to_project_name_macro ${macro_header} ) # need to use original macro header to find the project name
            macro_header="${MACROS_IDENT}${miner_macro}${macro_header}${converted_name}" # making the macro headers unique
            if [ "${project}" == "TOTAL" ]; then
                echo "\hline"
                echo "\hline"
                total_time=$( echo "${constraint_gen} + ${constraint_filter} + ${dsiplus}" | bc -l )
                avg_time=$( echo "${total_time} / ${acc}" | bc -l )
                echo "\newcommand{\totalLimitStudyTimeInsecs}{$( round ${total_time} 1)}" >> ${MACROS_OUT}
                echo "\newcommand{\avgTimeLimitStudyInSecs}{$( round ${avg_time} 1)}" >> ${MACROS_OUT}
                # echo "\total
            fi
            acc=$(( acc + 1))
            whole_number_constraint_filter=$( echo "${constraint_filter}" | cut -d. -f1 )
            # if [[ "${dsiplus}" == 0 && "${whole_number_constraint_filter}" -ge 7200 ]]; then # hit the timeout
            #     constraint_filter="\\limitStudyTimingsTimeout"
            # else
                constraint_filter="$( round ${constraint_filter} 1)"
            # fi
            echo "\\${macro_header}Miner & \\${project_name_macro} & \\${macro_header}SpecName & \\${macro_header}ConstraintGen & \\${macro_header}ConstraintFilter & \\${macro_header}DsiPlus \\\\"
            echo "\newcommand{\\${macro_header}Miner}{${miner}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}SpecName}{${name}\\xspace}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}ConstraintGen}{$( round ${constraint_gen} 1)}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}ConstraintFilter}{${constraint_filter}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}DsiPlus}{$( round ${dsiplus} 1)}" >> ${MACROS_OUT}
        done
    } <${REORGANIZED_IN_FILE}

}

(
make_header
make_body
make_footer
) > ${TABLES_OUT}

# echo "\newcommand{\limitStudyTotalSpecs}{$( tail -n +2 ${REORGANIZED_IN_FILE} | grep -v TOTAL | grep -v AVG | grep -v MIN | grep -v MAX | wc -l )}" >> ${MACROS_OUT}

# rm ${REORGANIZED_IN_FILE}

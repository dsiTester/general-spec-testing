#!/bin/sh

if [ $# -lt 1 ]; then
    echo "USAGE: $0 PAPER_DIR"
    exit
fi

PAPER_DIR=$1
SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname $( dirname ${SCRIPT_DIR} ) )
IN_FILE=${BASE_DIR}/data/limit-study/ordered-limit-study-outcomes.csv
REORGANIZED_IN_FILE=${IN_FILE}

# head -1 ${IN_FILE} > ${REORGANIZED_IN_FILE}
# for entry in $( cat ${FILE_WITH_ORDERINGS} | cut -d, -f1 ); do
#     grep ^${entry}, ${IN_FILE} >> ${REORGANIZED_IN_FILE}
# done
# grep ^TOTAL ${IN_FILE} >> ${REORGANIZED_IN_FILE}
# grep ^AVG ${IN_FILE} >> ${REORGANIZED_IN_FILE}

# dos2unix ${REORGANIZED_IN_FILE}

MACROS_IDENT=limitStudyOutcomes # script-specific macro header to prevent crashes

MACROS_DIR=${PAPER_DIR}/generated-macros
TABLES_DIR=${PAPER_DIR}/tables

MACROS_FILE_IDENT=limit_study_outcomes_macros
MACROS_OUT=${MACROS_DIR}/limit_study_outcomes_macros.tex
MACROS_STARTER=${MACROS_DIR}/limit_study_outcomes_macros_starter.tex
TABLES_OUT=${TABLES_DIR}/limit_study_outcomes_table.tex

( cat ${MACROS_STARTER} ) > ${MACROS_OUT}
>${TABLES_OUT}

source ${SCRIPT_DIR}/macros-helper.sh
source ${SCRIPT_DIR}/helper.sh

compute_all_stats ${IN_FILE}
compute_min ${IN_FILE}
compute_max ${IN_FILE}

# place our macros file in, if it doesn't exist
if ! grep -q ${MACROS_FILE_IDENT} ${MACROS_DIR}/all_macro_files.tex; then
    echo "\input{generated-macros/${MACROS_FILE_IDENT}}" >> ${MACROS_DIR}/all_macro_files.tex
fi

function make_header() {
    echo "
\begin{table}[t!]
\tiny
\caption{Outcomes of the limit study.}
  \vspace{-13pt}
\begin{center}
\begin{tabular}{|c|c|l|r|r|r|r|r|r|r|r|}
\hline
\\${MACROS_IDENT}Miner & \\${MACROS_IDENT}Project & \\${MACROS_IDENT}Ident & \\${MACROS_IDENT}State & \\${MACROS_IDENT}Transitions & \\${MACROS_IDENT}Letters & \\${MACROS_IDENT}Constraints & \\${MACROS_IDENT}SpuConstraints & \\${MACROS_IDENT}NbpConstraints & \\${MACROS_IDENT}TrueConstraints & \\${MACROS_IDENT}UnknownConstraints \\\\
"
echo "
\hline
\hline"

}

function make_footer() {
    echo "\hline
\end{tabular}
\label{tabular:output:limit_study_outcomes}
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
    {
        read
        while IFS=, read spec_ident states transitions letters num_constraints spu_constraints nbp_constraints true_constraints unknown_constraints; do
            miner=$( echo "${spec_ident}" | cut -d@ -f1 )
            miner_official_macro=$( miner_to_macro_name "${miner}" )
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
                # continue
                echo "TOTAL & \\${project_name_macro} & \\${macro_header}SpecName & \\${macro_header}States & \\${macro_header}Transitions & \\${macro_header}Letters & \\${macro_header}Constraints & \\${macro_header}SpuConstraints & \\${macro_header}NbpConstraints & \\${macro_header}TrueConstraints & \\${macro_header}UnknownConstraints \\\\"
            elif [ "${project}" == "AVG" ]; then
                echo "AVG & \\${project_name_macro} & \\${macro_header}SpecName & \\${macro_header}States & \\${macro_header}Transitions & \\${macro_header}Letters & \\${macro_header}Constraints & \\${macro_header}SpuConstraints & \\${macro_header}NbpConstraints & \\${macro_header}TrueConstraints & \\${macro_header}UnknownConstraints \\\\"
                states=$( round ${states} 1 )
                transitions=$( round ${transitions} 1 )
                letters=$( round ${letters} 1 )
            else
            echo "\\${miner_official_macro} & \\${project_name_macro} & \\${macro_header}SpecName & \\${macro_header}States & \\${macro_header}Transitions & \\${macro_header}Letters & \\${macro_header}Constraints & \\${macro_header}SpuConstraints & \\${macro_header}NbpConstraints & \\${macro_header}TrueConstraints & \\${macro_header}UnknownConstraints \\\\"
            fi
            # echo "\\${macro_header}Miner & \\${project_name_macro} & \\${macro_header}SpecName & \\${macro_header}States & \\${macro_header}Transitions & \\${macro_header}Letters & \\${macro_header}Constraints & \\${macro_header}SpuConstraints & \\${macro_header}NbpConstraints & \\${macro_header}TrueConstraints & \\${macro_header}UnknownConstraints \\\\"
            # echo "\newcommand{\\${macro_header}Miner}{}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}SpecName}{${name}\\xspace}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}States}{${states}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}Transitions}{${transitions}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}Letters}{${letters}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}Constraints}{${num_constraints}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}SpuConstraints}{${spu_constraints}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}NbpConstraints}{${nbp_constraints}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}TrueConstraints}{${true_constraints}}" >> ${MACROS_OUT}
            echo "\newcommand{\\${macro_header}UnknownConstraints}{${unknown_constraints}}" >> ${MACROS_OUT}
        done
    } <${REORGANIZED_IN_FILE}

}

(
make_header
make_body
make_footer
) > ${TABLES_OUT}

# count the number of complex specs with at least one spurious constraint
at_least_one_spu=0
limit_study_total=$( tail -n +2 ${REORGANIZED_IN_FILE} | grep -v TOTAL | grep -v AVG | grep -v MAX | grep -v MIN | wc -l )
for line in $( tail -n +2 ${REORGANIZED_IN_FILE} | grep -v TOTAL | grep -v AVG | grep -v MAX | grep -v MIN ); do
    spu=$( echo "${line}" | cut -d, -f5 )
    # echo ${spu}
    if [ "${spu}" -gt 0 ]; then
        at_least_one_spu=$(( at_least_one_spu + 1 ))
    fi
done

echo "\newcommand{\limitStudyAtLeastOneSpu}{${at_least_one_spu}}" >> ${MACROS_OUT}
echo "\newcommand{\limitStudyTotalSpecs}{${limit_study_total}}" >> ${MACROS_OUT}

# computing candidate info here
total_candidate_constraints=$( cut -d, -f2 ${BASE_DIR}/data/num-constraints-per-spec-limit-study.csv | paste -sd+ | bc -l )
avg_candidate_constraints=$( echo "${total_candidate_constraints} / ${limit_study_total}" | bc -l )
echo "\newcommand{\limitStudyTotalCandidateConstraints}{${total_candidate_constraints}}" >> ${MACROS_OUT}
echo "\newcommand{\limitStudyAvgCandidateConstraints}{$( round ${avg_candidate_constraints} 1 )}" >> ${MACROS_OUT}

# rm ${REORGANIZED_IN_FILE}

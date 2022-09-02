if [ $# -ne 1 ]; then
    echo "Usage: $0 DIRECTORY"
    exit
fi

DIR=$1

function compare_helper() {
    local file_1=$1
    local file_2=$2

    if [ ! -f ${file_1} ]; then
	>${file_1}
    fi
    if [ ! -f ${file_2} ]; then
	>${file_2}
    fi

    one_but_not_two=$( comm -23 <( cat ${file_1} | sort -u ) <( cat ${file_2} | sort -u ) | wc -l )
    two_but_not_one=$( comm -13 <( cat ${file_1} | sort -u ) <( cat ${file_2} | sort -u ) | wc -l )
    both=$( comm -12 <( cat ${file_1} | sort -u ) <( cat ${file_2} | sort -u ) | wc -l )

    echo "${one_but_not_two},${two_but_not_one},${both}"
}



function compare() {
    local opt_1=$1
    local opt_2=$2

    if [[ ! -d ${opt_1}/results && ! -d ${opt_2}/results ]]; then
	true_res="-1,-1,-1"
	spurious_res="-1,-1,-1"
	unknown_res="-1,-1,-1"
    elif [[ ! -d ${opt_1}/results && -d ${opt_2}/results ]]; then
	true=$( cat ${opt_2}/results/total-true-specs.txt | wc -l );
	true_res="-1,${true},-1"
	spurious=$( cat ${opt_2}/results/total-spurious-specs.txt | wc -l );
        spurious_res="-1,${spurious},-1"
	unknown=$( cat ${opt_2}/results/total-unknown-specs.txt | wc -l );
        unknown_res="-1,${unknown},-1"
    elif [[ -d ${opt_1}/results && ! -d ${opt_2}/results ]]; then
	true=$( cat ${opt_1}/results/total-true-specs.txt | wc -l );
	true_res="${true},-1,-1"
	spurious=$( cat ${opt_1}/results/total-spurious-specs.txt | wc -l );
        spurious_res="${spurious},-1,-1"
	unknown=$( cat ${opt_1}/results/total-unknown-specs.txt | wc -l );
        unknown_res="${unknown},-1,-1"
    else
	true_res=$( compare_helper ${opt_1}/results/total-true-specs.txt ${opt_2}/results/total-true-specs.txt )
	spurious_res=$( compare_helper ${opt_1}/results/total-spurious-specs.txt ${opt_2}/results/total-spurious-specs.txt )
	unknown_res=$( compare_helper ${opt_1}/results/total-unknown-specs.txt ${opt_2}/results/total-unknown-specs.txt )	
    fi

    base="${true_res},${spurious_res},${unknown_res}"
    total=$( echo "${base}" | sed 's/,/+/g' | bc )
    echo "${base},${total}"
}

function main() {
    echo "Config,True-L,True-R,True-B,Spu-L,Spu-R,Spu-B,Unk-L,Unk-R,Unk-B,Total"
    for proj in $( ls ${DIR}/run-info | rev | cut -d'-' -f10- | rev | sort -u ); do
	[[ $( ls ${DIR}/run-info/${proj}* | wc -l ) -lt 3 ]] && continue
	base_l1=$( compare ${DIR}/${proj}/dsiPlus-allGranularities ${DIR}/${proj}/optimizedDsiPlus-1 )
	echo "${proj}-base-l1,${base_l1}"
	base_l2=$( compare ${DIR}/${proj}/dsiPlus-allGranularities ${DIR}/${proj}/optimizedDsiPlus-2 )
	echo "${proj}-base-l2,${base_l2}"
	base_l3=$( compare ${DIR}/${proj}/dsiPlus-allGranularities ${DIR}/${proj}/optimizedDsiPlus-3 )
	echo "${proj}-base-l3,${base_l3}"

	l1_l2=$( compare ${DIR}/${proj}/optimizedDsiPlus-1 ${DIR}/${proj}/optimizedDsiPlus-2 )
	echo "${proj}-l1-l2,${l1_l2}"
	l1_l3=$( compare ${DIR}/${proj}/optimizedDsiPlus-1 ${DIR}/${proj}/optimizedDsiPlus-3 )
	echo "${proj}-l1-l3,${l1_l3}"
	l2_l3=$( compare ${DIR}/${proj}/optimizedDsiPlus-2 ${DIR}/${proj}/optimizedDsiPlus-3 )
	echo "${proj}-l2-l3,${l2_l3}"
	
    done

}

main

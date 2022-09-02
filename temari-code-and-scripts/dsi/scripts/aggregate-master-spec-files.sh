# this is a prototype for a script that takes in three aggregate runs of mining and computes an aggregate sMap
for proj in $( cat a.txt | grep -v jpatterns | cut -d- -f2- | rev | cut -d. -f2- | rev ); do
    master_1=mining-results/mining-results-1/mining-${proj}/${proj}/mining/logs/master-spec-file.txt
    master_2=mining-results/mining-results-2/mining-${proj}/${proj}/mining/logs/master-spec-file.txt
    master_3=mining-results/mining-results-3/mining-${proj}/${proj}/mining/logs/master-spec-file.txt
    echo PROJ: ${proj}
    acc=1
    out=master-spec-files/${proj}-master-spec-file.txt
    >${out}
    # aggregate the list of specs from project
    while read a b; do
        formatted=$( printf "%05d" ${acc} )
	grep_a=$( echo "${a}" | sed 's/\[/\\[/g' | sed 's/\$/\\$/g' )
	grep_b=$( echo "${b}" | sed 's/\[/\\[/g' | sed 's/\$/\\$/g' )
	tests_1=$( grep "${grep_a} ${grep_b}" ${master_1} | cut -d' ' -f4 | sed 's/,/ /g' )
	tests_2=$( grep "${grep_a} ${grep_b}" ${master_1} | cut -d' ' -f4 | sed 's/,/ /g' )
	tests_3=$( grep "${grep_a} ${grep_b}" ${master_3} | cut -d' ' -f4 | sed 's/,/ /g' )
        test_list=$( echo $tests_1 $tests_2 $tests_3 | sed 's/ /\n/g' | sort -u )
	test_list=$( echo $test_list | sed 's/ /,/g' )
	echo $formatted $a $b $test_list >> ${out}
	acc=$((${acc} + 1))
    done < <( cat ${master_1} ${master_2} ${master_3}  | cut -d' ' -f-3 | cut -d' ' -f2- | sort -u )
done

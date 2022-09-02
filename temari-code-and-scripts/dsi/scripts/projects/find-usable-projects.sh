out_file=`pwd`/usable.csv
stats_file=`pwd`/stats.csv
# echo "#name,last-changed,#test-classes,LOC,#modules" > ${stats_file}
>${stats_file}
>${out_file}

while read url sha name
do
    # echo ${url} ${sha} ${name}
    git clone ${url} ${name} &> /dev/null
    (
        cd ${name}
        latest_sha=$( git log -1 | head -1 | cut -d' ' -f2 )
        latest_change=$(git log -1 --format=%aD | tr ',' ' '| cut -d' ' -f5)
        test_count_4=$(find -name "*Test.java" | wc -l)
        test_count_3=$(find -name "Test*.java" | wc -l)
        test_count=$((test_count_3 + test_count_4))
        code_size=$(sloccount . |& grep java: | cut -d\( -f1 | cut -d: -f2 | tr -d ' ')
        module_count=$(find -name pom.xml | wc -l)
        if [ ${module_count} -eq 1 ] && [ ${test_count} -gt 5 ] && [ ${latest_change} -ge 2018 ]; then
            echo ${name},${latest_change},${test_count},${code_size},${module_count} >> ${stats_file}
            echo ${url} ${latest_sha} ${name} >> ${out_file}
        fi
    )
    rm -rf ${name}
done < 200-projects.txt


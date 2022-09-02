#!/bin/bash

if [ $# -ne 2 ]; then
    echo "USAGE: bash $0 PROJECT THREAD_COUNT"
    exit
fi

PROJECT=$1
THREAD_COUNT=$2

echo "RUNNING CONVERSION FOR PROJECT ${PROJECT}"

prefix="gol-"
script_dir=$( cd $( dirname $0 ) && pwd )

# loop though the project directory specified to create spec FSMs
# project_path="${script_dir}/texada-spec-fsms/data/texada/${PROJECT}"
project_path="${script_dir}/texada/logs/${PROJECT}"

result_dir="$script_dir/texada-spec-fsms/${PROJECT}"
logs_dir="${result_dir}/logs"
script_log=${logs_dir}/gol-spec-to-fsm
finished_runs="$script_dir/texada-spec-fsms/${PROJECT}-finished"
mkdir -p "$result_dir"
mkdir -p "$finished_runs"
mkdir -p ${logs_dir}
>${script_log}

echo "Conversion for ${PROJECT} started at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${script_log}

FILE_LIMIT=1000000

num_all_files=$( ls "${project_path}" | wc -l )

acc=0 # for testing
errored_templates_file=$script_dir/texada-spec-fsms/error-${PROJECT}.txt
grep -rli segmentation ${project_path} | rev | cut -d/ -f1 | rev | cut -d- -f2- | sort -u > ${errored_templates_file}

num_all_specs=$( comm -23 <( ls ${project_path} | cut -d- -f2- | sort -u ) <( cat ${errored_templates_file} | sort -u ) | grep -v ^summary$ | xargs -I {} cat ${project_path}/gol-{} | grep -v ^$ | grep -v ^+ | grep -v ^- | grep -v ^real | grep -v ^user | grep -v ^sys | grep -v ^$ | wc -l )
echo  -e "number of specs to convert within ${PROJECT}: ${num_all_specs}\n" | tee -a ${script_log}

num_specs_done=0

for file in "${project_path}"/* ; do
    template_methods=$(basename $file)
    base_name=${template_methods#$prefix}
    fsm_template="$script_dir/translated-texada-spec-templates/txtRepresentation_$base_name.txt"
    if [ ! -f ${fsm_template} ]; then
	echo "===SKIP: template file ${fsm_template} not found!"  | tee -a ${script_log}
	continue
    fi
    if grep -Fxq "$base_name" "${errored_templates_file}" ; then
	echo "===SKIP: ${base_name} in errored templates file"  | tee -a ${script_log}
	continue
    fi
    num_specs_in_template=$( cat "${file}" | grep -v ^$ | grep -v ^+ | grep -v ^- | grep -v ^real | grep -v ^user | grep -v ^sys | wc -l )
    [[ "${num_specs_in_template}" -eq 0 ]] && continue

    echo "file start time (${base_name}): `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${script_log}

    loop_acc=0
    start=1
    end=${FILE_LIMIT}
    # echo "almost while loop. loop_acc: ${loop_acc} ; end: ${end} ; num_specs_in_template: ${num_specs_in_template}"
    while [[ "${loop_acc}" -eq "0" || "${start}" -lt "${num_specs_in_template}" ]]; do
	loop_acc=$(( loop_acc + 1 ))
	echo "===ANALYZING SPECS ${start} TO ${end} out of ${num_specs_in_template} from template file ${file} in project ${PROJECT}"  | tee -a ${script_log}
	(
	    export script_dir
            export PROJECT
            export result_dir
            export finished_runs
            export prefix
	    export template_methods
	    export base_name
	    export fsm_template
	    export file
	    cat "${file}" | grep -v ^$ | grep -v ^+ | grep -v ^- | grep -v ^real | grep -v ^user | grep -v ^sys | head -${end} | tail -n +${start} | parallel -j ${THREAD_COUNT} bash ${script_dir}/convert-texada-spec-to-fsm-helper.sh
	) &> ${logs_dir}/gol-convert-${base_name}
	start=$( echo "${end} + 1" | bc -l )
	end=$( echo "${end} + ${FILE_LIMIT}" | bc -l )
	# package up the current set of specs
	(
	    cd ${result_dir}
	    mv ${base_name} ${base_name}-${loop_acc}
	    tar -czf ${base_name}-${loop_acc}.tgz ${base_name}-${loop_acc}
	    rm -rf ${base_name}-${loop_acc}
	)
	# message about how many specs are left
	num_specs_done=$((num_specs_done + num_specs_in_template))
	percent_done=$( echo "scale=4; ( ${num_specs_done} / ${num_all_specs} ) * 100" | bc -l )
	echo -e "${num_specs_done} SPECS PROCESSED OUT OF ${num_all_specs}. Approx ${percent_done}% done\n"  | tee -a ${script_log}
    done
    echo "file end time (${base_name}): `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${script_log}
done

echo "Conversion for ${PROJECT} ended at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${script_log}

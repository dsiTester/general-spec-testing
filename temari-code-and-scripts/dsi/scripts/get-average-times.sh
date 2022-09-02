#!/bin/bash

# This bash script collects timing information from all log files from projects within
# the overall bucketing directory (needs to be organized in a particular way)

# Need to provide directory (containing all of the buckets)
if [ $# != 1 ] ; then
  echo "usage: bash $0 DIRECTORY"
  echo "where DIRECTORY is the directory where all of the buckets are in"
  exit
fi

directory=$1

# for ben in $(grep -v ^# ${in_file}); do
for ben in $(ls ${directory}); do # iterate over all subdirectories of base bucket directory
  # echo ${ben}
  curr_breakdown=${ben}
  curr_sum=0
  files=`find ${directory}/${ben}/ -type f`
  for f in ${files}; do
      # FIXME: refactoring would be good here
      start_string=`grep -r "START TIME: " ${f} | cut -d' ' -f3`
      start_hour_raw=`echo ${start_string} | cut -d':' -f1`
      # start_hour=$(($((`echo ${start_string} | cut -d':' -f1`)) * 3600))
      start_hour=`awk "BEGIN {print ${start_hour_raw}*3600}"`
      a=`echo ${start_string} | cut -d':' -f2`
      # start_min=$(($((`echo ${start_string} | cut -d':' -f2`)) * 60))
      start_min=`awk "BEGIN {print ${a}*60}"`
      start_secs=`echo ${start_string} | cut -d':' -f3`
      # start_time=$((${start_hour} + ${start_min} + ${start_secs}))
      start_time=`awk "BEGIN {x=${start_hour}+${start_min}+${start_secs};printf \"%.3f\n\", x}"`

      end_string=`grep -r "END TIME: " ${f} | cut -d' ' -f3`
      end_hour_raw=`echo ${end_string} | cut -d':' -f1`
      end_hour=`awk "BEGIN {print ${end_hour_raw}*3600}"`
      # end_hour=$(($((`echo ${end_string} | cut -d':' -f1`)) * 3600))
      b=`echo ${end_string} | cut -d':' -f2`
      # end_min=$(($((`echo ${end_string} | cut -d':' -f2`)) * 60))
      end_min=`awk "BEGIN {print ${b}*60}"`
      end_secs=`echo ${end_string} | cut -d':' -f3`
      end_time=`awk "BEGIN {x=${end_hour}+${end_min}+${end_secs};printf \"%.3f\n\", x}"`

      # while end time is less than start time...
      while [[ 1 -eq "$(echo "${end_time} < ${start_time}" | bc)" ]]; do
          # end_time=$((${end_time} + 86400))
          end_time=`awk "BEGIN {print ${end_time}+86400}"`
      done
      # diff=$((${end_time} - ${start_time}))
      diff=`awk "BEGIN {print ${end_time}-${start_time}}"`
      # curr_sum=$((${curr_sum} + ${diff}))
      curr_sum=`awk "BEGIN {print ${curr_sum}+${diff}}"`
  done
  # collect total # of log files that belong to this project
  num_files=`find ${directory}/${ben}/ -type f | wc -l`
  # avg=$((${curr_sum}/${num_files}))
  avg=`awk "BEGIN {print ${curr_sum}/${num_files}}"`
  echo "Average time for ${ben}: ${avg} secs"
done

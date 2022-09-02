#!/bin/bash

if [ $# -lt 2 -o $# -gt 3 ]; then
    echo "arg1 - the path to the project, where high-level pom.xml is"
    echo "arg2 - the path to the DSI jar"
    echo "arg3 [optional] - pass string \"-AL\" to add listener"
    exit
fi

project_path=$1
dsi_jar_path=$2
option=""

if [ -n $3 ]; then
    option=$3
fi

crnt=`pwd`
working_dir=`dirname $0`

cd ${project_path}
find -name pom.xml | xargs -I {} cp {} {}.pombak
cd - > /dev/null

cd ${working_dir}

javac PomFile.java
find ${project_path} -name pom.xml | java -cp . PomFile ${dsi_jar_path} ${option}

cd ${crnt}

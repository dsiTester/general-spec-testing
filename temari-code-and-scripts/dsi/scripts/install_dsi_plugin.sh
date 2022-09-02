#!/bin/sh

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
DSI_DIR=$( cd $( dirname ${SCRIPT_DIR} ) && pwd )
echo ${DSI_DIR}

cd ${DSI_DIR}

cd ${DSI_DIR}/dsi-base

ant

cd ${DSI_DIR}

echo "installing methodtracer..."

mvn install:install-file -Dfile=${DSI_DIR}/dsi-base/mining/bin/methodtracer.jar -DgroupId="edu.ucdavis" -DartifactId="methodtracer" -Dversion="1.0" -Dpackaging="jar"

echo "installing miner..."
mvn install:install-file -Dfile=${DSI_DIR}/dsi-base/mining/bin/mine.jar -DgroupId="edu.ucdavis" -DartifactId="mine" -Dversion="1.0" -Dpackaging="jar"

echo "installing ruleverify..."

mvn install:install-file -Dfile=${DSI_DIR}/dsi-base/dist/ruleverify.jar -DgroupId="edu.ucdavis" -DartifactId="ruleverify" -Dversion="1.0" -Dpackaging="jar"


mvn install -Dcheckstyle.skip # install the root pom.xml

echo "installed root pom..."

cd ${DSI_DIR}/dsi-plugin-core

mvn package -Dcheckstyle.skip

mvn install:install-file -Dfile=${DSI_DIR}/dsi-plugin-core/target/dsi-plugin-core-1.0-SNAPSHOT-jar-with-dependencies.jar -DgroupId="edu.cornell" -DartifactId="dsi-plugin-core" -Dversion="1.0-SNAPSHOT" -Dpackaging="jar"

cd ${DSI_DIR}/dsi-plugin

mvn install -Dcheckstyle.skip

cd ${DSI_DIR}

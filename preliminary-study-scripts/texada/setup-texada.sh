#!/bin/bash

#  run as root as we need to add libraries and install packages
if [ "$EUID" -ne 0 ]
  then echo "Please run as root"
  exit
fi

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

# install dependencies:
apt-get update
apt-get install -y cmake libgtest-dev g++

# compile and setup google test:
cd /usr/src/gtest
cmake CMakeLists.txt
make
cp *.a /usr/lib # cp lib/*.a /usr/lib

# install boost:
sudo apt-get install -y libboost-all-dev

LIBDIR=/etc/ld.so.conf
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/lib:/usr/lib:$LIBDIR
export LD_RUN_PATH=$LIBDIR:$LD_RUN_PATH

# build/install spot:
cd ${SCRIPT_DIR}
SPOT_VERSION="spot-1.2.6" # to avoid inconsistencies

wget -c https://www.lrde.epita.fr/dload/spot/${SPOT_VERSION}.tar.gz
tar -zxvf ${SPOT_VERSION}.tar.gz
cd ${SPOT_VERSION}/
./configure --disable-python # <-- disable python to avoid version mismatch
make

make check
sudo make install

# grab texada:
#  it was cloning from mercurial repo in the original script
#hg clone https://bestchai@bitbucket.org/bestchai/texada
cd ${SCRIPT_DIR}
git clone https://github.com/ModelInference/texada.git
cd texada

# setup environment to run texada (you'll want to add this line to your .bashrc or similar):
export TEXADA_HOME=${SCRIPT_DIR}/texada

# set up the uservars.mk file based on the example that's there:
cp uservars.mk.example uservars.mk
# only change the SPOT_INCL location since all other location
# vars are available system-wide and need not be changed:
sed -i "/^SPOT_INCL:=/c\SPOT_INCL:=${PWD%/*}/${SPOT_VERSION}/src/" uservars.mk

spot_location=$( sudo find / -name "libspot.so.0" | grep -v texada | tail -1 | rev | cut -d/ -f2- | rev )
export LD_LIBRARY_PATH=${spot_location}

# build texada and texadatest
make

# run texadatest to make sure all the tests pass
./texadatest

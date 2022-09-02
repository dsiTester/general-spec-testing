SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )

if [ ! -d ~/.ssh ]; then
    mkdir ~/.ssh
fi

echo "Host github.com
    StrictHostKeyChecking no" >> ~/.ssh/config


mv ~/.m2 ~/.m2-backup
cp -r ${SCRIPT_DIR}/dot_m2 ~/.m2

project=$1

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
EXCLUDES_DIR=${SCRIPT_DIR}/excludes

function treat_special() {
    local project=$1
    if [ "${project}" == "commons-fileupload" ]; then
	echo "treat_special: dealing with commons-fileupload"
	find -name DiskFileItemSerializeTest.java | xargs sed -i 's/System.getProperty(\"java.io.tmpdir\")/"target"/g'
	find -name DefaultFileItemTest.java | xargs sed -i 's/System.getProperty(\"java.io.tmpdir\")/"target"/g'
        find -name DiskFileItem.java | xargs sed -i 's/System.getProperty(\"java.io.tmpdir\")/"target"/g'
        # find -name DiskFileUploadTest.java | xargs sed -s "s/final File out = File.createTempFile(\"install\", \".tmp\");
    fi

    if [ "${project}" == "joda-convert" ]; then
        rm src/main/java/module-info.java
    fi

}

treat_special ${project}

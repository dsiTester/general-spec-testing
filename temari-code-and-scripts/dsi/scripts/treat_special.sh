project=$1

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
EXCLUDES_DIR=${SCRIPT_DIR}/excludes

function treat_special() {
    local project=$1
    local excludes_file=${EXCLUDES_DIR}/${project}.txt
    echo "treat_special: excludes_file = ${excludes_file}"
    if [ -f "${excludes_file}" ]; then
    	for tst in $( cat ${excludes_file} | cut -d'#' -f1 | cut -d' ' -f1) ; do
    	    echo "treat_special: removing ${tst}..."; find -name ${tst}.java | xargs -r rm
    	done
    else
	echo "treat_special: no excludes!"
    fi
    if [ "${project}" == "commons-fileupload" ]; then
	echo "treat_special: dealing with commons-fileupload"
	find -name DiskFileItemSerializeTest.java | xargs sed -i 's/java.io.tmpdir/buildDirectory/g'
	find -name DefaultFileItemTest.java | xargs sed -i 's/java.io.tmpdir/buildDirectory/g'
        find -name DiskFileItem.java | xargs sed -i 's/java.io.tmpdir/buildDirectory/g'
        # find -name DiskFileUploadTest.java | xargs sed -s "s/final File out = File.createTempFile(\"install\", \".tmp\");
    fi
    if [ "${project}" == "Thomas-S-B.visualee" ]; then # race condition on resource
	rm -rf src/main/resources/
    fi
    if [ "${project}" == "IvanTrendafilov.Confucius" ]; then
	echo "treat_special: dealing with IvanTrendafilov.Confucius"
	find -name FileConfigurationDataProviderTest.java | xargs sed -i 's/java.io.tmpdir/buildDirectory/g'
	find -name ParserTest.java | xargs sed -i 's/java.io.tmpdir/buildDirectory/g'
    fi
    if [ "${project}" == "jmxtrans.embedded-jmxtrans" ]; then
	find -name CsvWriterTest.java | xargs sed -i 's/("csvWriterTest.txt")/(System.getProperty("buildDirectory"), "csvWriterTest.txt")/g'
    fi

}

treat_special ${project}

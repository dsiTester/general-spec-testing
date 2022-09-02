project=$1
spec_id=$2
aspect_file=$3

if [ "${project}" == joda-convert ]; then
    if [ "${spec_id}" == 00018 ]; then
        sed -i 's/callMethodA() : call(/callMethodA() : execution(/g' ${aspect_file}
        sed -i 's/callMethodB() : call(/callMethodB() : execution(/g' ${aspect_file}
    elif [ "${spec_id}" == 00019 ]; then
        sed -i 's/callMethodA() : call(/callMethodA() : execution(/g' ${aspect_file}
    elif [[ "${spec_id}" == 00025 || "${spec_id}" == 00029 || "${spec_id}" == 00034 ]]; then
        sed -i 's/callMethodB() : call(/callMethodB() : execution(/g' ${aspect_file}
    fi
fi

if [ "${project}" == commons-fileupload ]; then
    if [[ "${spec_id}" == 00315 || "${spec_id}" == 00329 || "${spec_id}" == 00332 ]]; then
        sed -i 's/call(org.apache.commons.fileupload2.FileItemIterator org.apache.commons.fileupload2.FileUpload.getItemIterator(org.apache.commons.fileupload2.RequestContext))/execution(org.apache.commons.fileupload2.FileItemIterator org.apache.commons.fileupload2.FileUploadBase.getItemIterator(org.apache.commons.fileupload2.RequestContext))/g' ${aspect_file}
    fi
fi

if [ "${project}" == commons-validator ]; then
    if [[ "${spec_id}" == 00645 || "${spec_id}" == 00646 || "${spec_id}" == 00647 || "${spec_id}" == 00657 ]]; then
        sed -i 's/callMethodA() : call(/callMethodA() : execution(/g' ${aspect_file}
    fi
fi

if [ "${project}" == commons-exec ]; then
    if [ "${spec_id}" == 00050 ]; then
        sed -i 's/callMethodA() : call(/callMethodA() : execution(/g' ${aspect_file}
    fi
fi

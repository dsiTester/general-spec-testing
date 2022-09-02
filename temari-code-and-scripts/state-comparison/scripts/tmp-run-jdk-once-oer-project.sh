SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname ${SCRIPT_DIR} )
DATA_DIR=${BASE_DIR}/data
CONVERSION_DIR=${BASE_DIR}/spec-to-aspect
CONVERSION_JAR=${CONVERSION_DIR}/target/spec-to-aspect-1.0-SNAPSHOT-jar-with-dependencies.jar

ACC=1


function header() {
    echo "package PACKAGE;

public privileged aspect NAME {
"
}

function create_call_pointcut_for_one_method() {
    method=$1
    converted_method=$( java -jar ${CONVERSION_JAR} DUMPONE "${method}" )
    converted_method=$( echo "${converted_method}" | sed 's/;/,/g' )
    echo
    echo -e "\t boolean calledMethod${ACC} = false;"
    echo -e "\t pointcut callMethod${ACC}() : call(${converted_method});"
    echo -e "\t before() : callMethod${ACC}() { System.out.println(\"ASPECT: before calling ${method}\"); numMethodsWithin++; }"
    echo -e "\t after() : callMethod${ACC}() { System.out.println(\"ASPECT: after calling ${method}\"); numMethodsWithin--; }"
    echo

}

function create_set_and_get_pointcuts_and_advice() {
    echo "    pointcut getSomeField(): if (numMethodsWithin) && GETS && notWithin();

    after() returning (Object field) : getSomeField() {
        System.out.println(\"#####GET,SIG=\" + thisJoinPoint.getSignature().toLongString() + \",TARGET_HASHCODE=\" + System.identityHashCode(thisJoinPoint.getTarget()));
    }

    pointcut setSomeField(): if (numMethodsWithin) && SETS && notWithin();

    after() : setSomeField() {
        System.out.println(\"=====SET,SIG=\" + thisJoinPoint.getSignature().toLongString() + \",TARGET_HASHCODE=\" + System.identityHashCode(thisJoinPoint.getTarget()));
    }
}"
}

header

create_call_pointcut_for_one_method "org.apache.commons.exec.CommandLine.addArgument(Ljava/lang/String;)Lorg/apache/commons/exec/CommandLine;"

create_set_and_get_pointcuts_and_advice

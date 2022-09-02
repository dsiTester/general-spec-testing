DICE_TESTER_DIR=/workspace/toy-dice-tester
LOGS_DIR=/workspace/logs
mkdir -p ${LOGS_DIR}

rm -rf /workspace/sandbox
rm -rf /workspace/sandbox2

cd ${DICE_TESTER_DIR}
git pull
( mvn clean package -DskipTests ) &> ${LOGS_DIR}/gol-build-evosuite

mkdir -p /workspace/sandbox
cd /workspace/sandbox

date=`date +%Y-%m-%d-%H-%M-%S`

if [ -d evosuite-tests ]; then
    mv evosuite-tests evosuite-tests-${date}
fi
mkdir evosuite-tests

touch NOW
echo =====================RUNNING EVOSUITE FOR THE FIRST TIME
echo
( timeout 30s java -jar ${DICE_TESTER_DIR}/master/target/evosuite-master-1.0.7-DICE.jar -class org.kamranzafar.jtar.TarInputStream -projectCP /workspace/jtar-code/target/classes/ -Dsearch_budget=10 ) &> ${LOGS_DIR}/gol-run-evosuite-1

find -newer NOW | tee -a ${LOGS_DIR}/gol-run-evosuite-1



cd /workspace/DICE_Miner

echo =====================RUNNING LTL RULES
echo
( python3 ltl_rules.py /workspace/kamranzafar.jtar/org.kamranzafar.jtar.TarInputStream.txt ../sandbox/evosuite-tests/TarInputStream.pures tarinputstream.vocab.txt | grep "LTL" > ltl_tarinputstream.txt ) &> ${LOGS_DIR}/gol-run-ltl-rules

if [ -d dice_tester_traces ]; then
    mv dice_tester_traces dice_tester_traces-${date}
fi
mkdir dice_tester_traces

mv *.vocab.txt /workspace/sandbox
mv ltl_*.txt /workspace/sandbox

mkdir -p /workspace/sandbox2
cd /workspace/sandbox2

touch NOW

echo =====================RUNNING EVOSUITE FOR THE SECOND TIME
echo
echo "path:/workspace/sandbox/ltl_tarinputstream.txt" > config.txt 
( timeout 1000s java -jar ${DICE_TESTER_DIR}/master/target/evosuite-master-1.0.7-DICE.jar -class org.kamranzafar.jtar.TarInputStream -projectCP /workspace/jtar-code/target/classes/ -Dsearch_budget=10 ) &> ${LOGS_DIR}/gol-run-evosuite-2

find -newer NOW | tee -a ${LOGS_DIR}/gol-run-evosuite-2

# echo =======
# cat null_org.kamranzafar.jtar.TarInputStream.traces

cd /workspace/DICE_Miner
mkdir outputs

echo =====================RUNNING DICE MAIN
echo
sed -i 's/<init>/TarInputStream/g' dice_tester_traces/TarInputStream_enhanced.traces

python3 main.py /workspace/kamranzafar.jtar/org.kamranzafar.jtar.TarInputStream.txt ../sandbox/evosuite-tests/TarInputStream.pures dice_tester_traces/TarInputStream_enhanced.traces outputs/TarInputStream/ 3000

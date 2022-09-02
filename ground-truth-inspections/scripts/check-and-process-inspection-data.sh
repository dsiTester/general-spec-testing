SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )

echo "Validating inspection files..."
bash check-inspections.sh ALL
echo "Processing inspection files into intermediate version for data processing..."
python3 ${SCRIPT_DIR}/create_inspections_with_tag_field.py
echo "Processing inspection data..."
python3 ${SCRIPT_DIR}/analyze-inspection-data.py

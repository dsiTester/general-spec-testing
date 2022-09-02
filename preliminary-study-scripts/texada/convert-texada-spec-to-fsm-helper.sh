line="${1}"

escaped_line=$( echo "${line}" | sed 's:\":\\":g' | sed 's:!:\\!:g' | sed 's:\[:\\[:g' | sed 's:\$:\\$:g' )
spec_num=$( grep -n "${escaped_line}" "${file}" | cut -d: -f1 )

# template_methods=$(basename $file)
# base_name=${template_methods#$prefix}
# fsm_template="$script_dir/results/txtRepresentation_${base_name}.txt"

# if ! grep -Fxq "${base_name}" "$script_dir/texada-spec-fsms/error-templates/error-${PROJECT}.txt"; then
#     # if [ -f "$fsm_template" ] && [ ! -f "$finished_runs/${base_name}" ]; then
#     echo "RUNNING: ${base_name}"
event_vars="$script_dir/events/${base_name}_events.txt"

# reading each line of the file to make a new FSM
# while IFS= read -r line; do
var_list=()
readarray  var_list < "$event_vars"

len=${#var_list[@]}

for (( j = 0; j < len; j++ )); do
    var_list[$j]=$(echo ${var_list[$j]} | tr -d '[:space:]')
done

# obtaining method names in the LTL expression
method_list=()
for (( i=0; i<${#line}; i++ )); do
    char="${line:$i:1}"
    if [[ "$char" = ['"'] ]]; then
	j=$(( $i+1 ))
	quote="${line:$j:1}"

	# go until second quote is hit
	while [[ "$quote" != ['"'] ]]; do
	    quote="${line:$j:1}"
	    j=$(( $j+1 ))
	done

	method="${line:$i+1:j-i-2}"
	method_list+=("$method")
	i="$j"
    fi
done

# remove duplicates
# the array should now line up exactly with the event array
unique_methods=($(echo "${method_list[@]}" | tr ' ' '\n' | sort -u | tr '\n' ' '))

result_file="$result_dir/${base_name}/${PROJECT}-texada-${base_name}-${spec_num}.txt"
mkdir -p ${result_dir}/${base_name}
cp "$fsm_template" "$result_file"

# sed for each variable with the corresponding method name
len=${#var_list[@]}
for (( j = 0; j < len; j++ ));
do
    string1=${var_list[$j]}
    string2=${unique_methods[$j]}
    sed -i "s:$string1:$string2:g" "$result_file"
done


# echo "$base_name: $spec_num"
# echo "$spec_num" > "${finished_runs}/${base_name}"
# fi
# fi

echo "end time: `date +%Y-%m-%d-%H-%M-%S`"

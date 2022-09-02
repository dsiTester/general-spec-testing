in_file=$1

echo =====method_a_set_method_b_set

comm -12 <( grep "SET,METHOD_A" ${in_file} | cut -d, -f3-4 | sed 's/ /-/g' | sort -u ) <( grep "SET,METHOD_B" ${in_file} | cut -d, -f3-4  | sed 's/ /-/g' | sort -u )
echo =====method_a_set_method_b_get

comm -12 <( grep "SET,METHOD_A" ${in_file} | cut -d, -f3-4 | sed 's/ /-/g' | sort -u ) <( grep "GET,METHOD_B" ${in_file} | cut -d, -f3-4  | sed 's/ /-/g' | sort -u )

echo =====method_a_get_method_b_set

comm -12 <( grep "GET,METHOD_A" ${in_file} | cut -d, -f3-4 | sed 's/ /-/g' | sort -u ) <( grep "SET,METHOD_B" ${in_file} | cut -d, -f3-4  | sed 's/ /-/g' | sort -u )

echo =====method_a_get_method_b_get

comm -12 <( grep "GET,METHOD_A" ${in_file} | cut -d, -f3-4 | sed 's/ /-/g' | sort -u ) <( grep "GET,METHOD_B" ${in_file} | cut -d, -f3-4  | sed 's/ /-/g' | sort -u ) | grep "\[\]" # count mutual gets of arrays as sharing state

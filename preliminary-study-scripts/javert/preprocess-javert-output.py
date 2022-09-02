import os
import sys

def get_specs_from_javert(javert_out_dir):
    # jd = os.path.join(miners_out_dir, "javert")
    acc = 1
    specs = {}
    if not os.path.exists(javert_out_dir):
        print("path does not exist: " + javert_out_dir)
        return specs
    for jf in os.listdir(javert_out_dir):
        test_name="".join(jf.split("-")[2:])
        if test_name == "alltests":
            test_name = "all-tests"
        with open(os.path.join(javert_out_dir, jf)) as f:
            in_spec=False
            spec_str_acc=""
            spec_lst_acc=[]
            for line in f:
                if not line.strip():
                    continue
                if line.startswith("Specification "): # found a spec
                    in_spec = True
                elif in_spec and line.startswith("----"): # done reading current spec
                    if not spec_str_acc in specs: # adding in new spec
                        specs[spec_str_acc] = { "id" : "javert" + str(acc).zfill(5) , "spec" : spec_lst_acc , "tests" : [test_name] }
                        acc += 1
                    else:
                        specs[spec_str_acc]["tests"].append(test_name)
                    spec_str_acc=""
                    spec_lst_acc=[]
                    in_spec = False
                    contains_test_class = False
                elif in_spec:
                    if "Test" in line or "test" in line:
                        contains_test_class = True
                    spec_str_acc += line
                    spec_lst_acc.append(line.strip())
    return specs

alias_acc = 0

def create_aliases(spec_as_list):
    global alias_acc
    aliases_map = {}
    rewritten_spec = []
    for entry in spec_as_list:
        revised_entry = ""
        if entry.startswith("(") or entry.startswith(")"):
            revised_entry = entry
        elif "|" in entry:
            method = entry.split()[1]
            revised_method = ""
            if not method in aliases_map:
                aliases_map[method] = "alias" + str(alias_acc)
                alias_acc += 1
            revised_entry = "| " + aliases_map[method]
        elif entry in aliases_map:
            revised_entry = aliases_map[entry]
        else:
            aliases_map[entry] = "alias" + str(alias_acc)
            alias_acc += 1
            revised_entry = aliases_map[entry]
        rewritten_spec.append(revised_entry)

    return rewritten_spec, aliases_map



def produce_files(specs, out_dir, project_name):
    smap_file = open(os.path.join(out_dir, "spec-id-to-mined-tests.csv"), "w")
    smap_file.write("spec-id,mined-tests\n")

    for spec_str in specs:
        spec_id = specs[spec_str]["id"]
        print("producing files for " + spec_id)
        spec_as_list = specs[spec_str]["spec"]
        mined_tests = specs[spec_str]["tests"]

        rewritten_spec, alias_map = create_aliases(spec_as_list)
        smap_file.write(spec_id + "," + ";".join(mined_tests) + "\n")

        out_prefix = project_name + "-" + spec_id
        with open(os.path.join(out_dir, out_prefix + "-originalformula.txt"), "w") as og_formula_file:
            og_formula_file.write("\n".join(spec_as_list))
        with open(os.path.join(out_dir, out_prefix + "-formula.txt"), "w") as formula_file:
            formula_file.write("\n".join(rewritten_spec))
        events_file = open(os.path.join(out_dir, out_prefix + "-events.txt"), "w")
        aliasmap_file = open(os.path.join(out_dir, out_prefix + "-aliasmap.csv"), "w")
        aliasmap_file.write("alias,event\n")
        for event_name in alias_map:
            events_file.write(alias_map[event_name] + "\n")
            aliasmap_file.write(alias_map[event_name] + "," + event_name + "\n")

def wrapper(javert_out_dir, script_out_dir, project_name, spec_miner_name):
    if not os.path.isdir(javert_out_dir):
        print("javert-out-dir does not exist! Exiting...")
    if not os.path.isdir(script_out_dir):
        print("script-out-dir does not exist! Creating new directory...")
        os.mkdir(script_out_dir)
    if spec_miner_name == "javert":
        specs = get_specs_from_javert(javert_out_dir)
    elif spec_miner_name == "bdd":
        specs = get_specs_from_bdd(javert_out_dir)
    else:
        print("spec-miner is invalid! must be either javert or bdd")
        sys.exit(1)
    print(spec_miner_name + " mined " + str(len(specs)) + " specs from project " + project_name)
    produce_files(specs, script_out_dir, project_name)

if __name__ == '__main__':
    if len(sys.argv) != 5:
        print("Usage: " + sys.argv[0] + " out-dir script-out-dir project-name spec-miner")
        sys.exit(1)
    wrapper(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4])

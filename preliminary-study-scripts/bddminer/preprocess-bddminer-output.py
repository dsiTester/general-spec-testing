import os
import sys

def convert_bdd_pattern(original_pattern):
    if original_pattern == "(a?b)*":
        return "((epsilon | a) b)*"
    elif original_pattern == "(ab?)*":
        return "(a (epsilon | b))*"
    elif original_pattern == "(a+b*c+)?":
        return "epsilon | (a+b*c+)"
    else:
        return original_pattern

id_acc = 1

def add_spec(specs, raw_spec_str, spec_str, test_name, alias_map):
    global id_acc
    if raw_spec_str in specs:
        specs[raw_spec_str]["tests"].append(test_name)
    else:
        specs[raw_spec_str] = { "id" : "bddminer" + str(id_acc).zfill(5), "spec" : spec_str, "tests" : [test_name] , "alias_map" : alias_map }
        id_acc += 1

alias_acc = 0

def get_specs_from_bdd(mined_specs_dir):
    global alias_acc
    specs = {} # keys are the original string used to create the spec
    aliases_map = {}
    if not os.path.exists(mined_specs_dir):
        print("path does not exist: " + mined_specs_dir)
        return specs
    for pattern_dir in os.listdir(mined_specs_dir):
        for mine_out_filename in os.listdir(os.path.join(mined_specs_dir, pattern_dir)):
            if "all-tests" in mine_out_filename:
                test_name = "all-tests"
            else:
                test_name="".join(mine_out_filename.split("-")[-1])
            with open(os.path.join(os.path.join(mined_specs_dir, pattern_dir, mine_out_filename))) as f:
                in_spec=False
                spec_str=""
                raw_spec_str=""
                pattern = ""
                for line in f:
                    if not line.strip():
                        continue
                    alias = "alias" + str(alias_acc)
                    if line.startswith("Pattern: "): # found a spec
                        pattern = convert_bdd_pattern(line.split()[1])
                        spec_str = pattern
                        raw_spec_str += line
                    elif "a = " in line:
                        method = line.split()[2]
                        spec_str = spec_str.replace("a", " " + alias + " ") # this only works because a is the first letter...
                        aliases_map[method] = alias
                        alias_acc += 1
                        raw_spec_str += line
                    elif "b = " in line:
                        method = line.split()[2]
                        spec_str = spec_str.replace("b", " " + alias + " ")
                        aliases_map[method] = alias
                        alias_acc += 1
                        raw_spec_str += line
                        if not "c" in pattern: # we are done when we see a b if our spec doesn't contain a c
                            add_spec(specs, raw_spec_str, spec_str, test_name, aliases_map)
                            raw_spec_str = ""
                            spec_str = ""
                            aliases_map = {}
                    elif "c = " in line:
                        method = line.split()[2]
                        spec_str = spec_str.replace("c", " " + alias + " ")
                        aliases_map[method] = alias
                        alias_acc += 1
                        raw_spec_str += line
                        add_spec(specs, raw_spec_str, spec_str, test_name, aliases_map)
                        raw_spec_str = ""
                        spec_str = ""
                        aliases_map = {}

    return specs


# def create_aliases(spec_as_list):
#     global alias_acc
#     aliases_map = {}
#     rewritten_spec = []
#     for entry in spec_as_list:
#         revised_entry = ""
#         if entry.startswith("(") or entry.startswith(")"):
#             revised_entry = entry
#         elif "|" in entry:
#             method = entry.split()[1]
#             revised_method = ""
#             if not method in aliases_map:
#                 aliases_map[method] = "alias" + str(alias_acc)
#                 alias_acc += 1
#             revised_entry = "| " + aliases_map[method]
#         elif entry in aliases_map:
#             revised_entry = aliases_map[entry]
#         else:
#             aliases_map[entry] = "alias" + str(alias_acc)
#             alias_acc += 1
#             revised_entry = aliases_map[entry]
#         rewritten_spec.append(revised_entry)

#     return rewritten_spec, aliases_map



def produce_files(specs, out_dir, project_name):
    smap_file = open(os.path.join(out_dir, "spec-id-to-mined-tests.csv"), "w")
    smap_file.write("spec-id,mined-tests\n")

    for spec_str in specs:
        spec_id = specs[spec_str]["id"]
        print("producing files for " + spec_id)
        rewritten_spec = specs[spec_str]["spec"]
        mined_tests = specs[spec_str]["tests"]
        alias_map = specs[spec_str]["alias_map"]

        # rewritten_spec, alias_map = create_aliases(spec_as_list)
        smap_file.write(spec_id + "," + ";".join(mined_tests) + "\n")

        out_prefix = project_name + "-" + spec_id
        with open(os.path.join(out_dir, out_prefix + "-originalformula.txt"), "w") as og_formula_file:
            og_formula_file.write(spec_str)
        with open(os.path.join(out_dir, out_prefix + "-formula.txt"), "w") as formula_file:
            formula_file.write(rewritten_spec)
        events_file = open(os.path.join(out_dir, out_prefix + "-events.txt"), "w")
        aliasmap_file = open(os.path.join(out_dir, out_prefix + "-aliasmap.csv"), "w")
        aliasmap_file.write("alias,event\n")
        for event_name in alias_map:
            events_file.write(alias_map[event_name] + "\n")
            aliasmap_file.write(alias_map[event_name] + "," + event_name + "\n")

def wrapper(bddminer_out_dir, script_out_dir, project_name):
    if not os.path.isdir(bddminer_out_dir):
        print("bddminer-out-dir does not exist! Exiting...")
    if not os.path.isdir(script_out_dir):
        print("script-out-dir does not exist! Creating new directory...")
        os.mkdir(script_out_dir)
    specs = get_specs_from_bdd(bddminer_out_dir)
    print("BDDMiner mined " + str(len(specs)) + " specs from project " + project_name)
    produce_files(specs, script_out_dir, project_name)

if __name__ == '__main__':
    if len(sys.argv) != 4:
        print("Usage: " + sys.argv[0] + " out-dir script-out-dir project-name")
        sys.exit(1)
    wrapper(sys.argv[1], sys.argv[2], sys.argv[3])

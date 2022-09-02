import csv
import os
import pandas as pd
import sys

scripts_dir=os.path.dirname(os.path.abspath(__file__))
base_dir=os.path.dirname(scripts_dir)
out_dir=os.path.join(base_dir, "data", "rankings-for-model-checking")

miner_name=""

def find_num_states_and_transitions_in_file(dirname, filename, project):
    global miner_name
    states = set()
    num_transitions = 0
    letters = set()
    if "_model.txt" in filename: # dsm has its own naming convention
        ident = filename.split("_model.txt")[0]
    else:
        ident = filename.split(".txt")[0]
    with open(os.path.join(dirname, filename), "r") as f:
        for line in f:
            sline = line.split()
            if len(sline) == 3:
                states.add(sline[0])
                states.add(sline[1])
                letters.add(sline[2])
                num_transitions += 1

    # hack to deal with bdd2 (ab)* bug. we probably want to remove this once we get accurate results for (ab)*
    if (miner_name == "bdd-2" and (len(states) == 0) and (num_transitions == 0) and (len(letters) == 0)):
        return { "project" : project, "spec-id" : ident, "states" : 2, "transitions" : 2, "letters" : 2 }
    else:
        return { "project" : project, "spec-id" : ident, "states" : len(states), "transitions" : num_transitions, "letters" : len(letters) }

def collect_data(miner_name, fsms_dir):
    spec_lst = []
    for project in os.listdir(fsms_dir):
        if (not os.path.isdir(os.path.join(fsms_dir, project))) or (project == "templates"):
            continue
        print("Collecting data from project: " + project)
        project_dir = os.path.join(fsms_dir, project)
        for filename in os.listdir(project_dir):
            if (not filename.endswith(".txt")):
                continue
            spec_dict = find_num_states_and_transitions_in_file(project_dir, filename, project)
            spec_lst.append(spec_dict)

    return spec_lst

def output_to_csv(keys, list_to_output, dirname, filename):
    with open(os.path.join(dirname, filename), "w") as out:
        writer = csv.DictWriter(out, fieldnames = keys)
        writer.writerows(list_to_output)

def wrapper(miner_name, fsms_dir):
    data = collect_data(miner_name, fsms_dir)
    print(data[0])
    filtered_data = data
    # filtered_data = [ entry for entry in data if entry["spurious-spec"] > 0 ]
    # going to get only the ones that ended
    sorted_data = sorted(filtered_data, key = lambda d: d["states"])
    keys = [ "project", "spec-id", "states", "transitions", "letters"]
    lst_for_out = [ {key : key for key in keys } ]
    lst_for_out += sorted_data
    output_to_csv(keys, lst_for_out, out_dir, miner_name + "-rankings-wo-edge-pair.csv")

if __name__ == '__main__':
    if len(sys.argv) != 3:
        print("Usage: " + sys.argv[0] + " miner_name fsms_dir")
        sys.exit(1)
    miner_name=sys.argv[1]
    wrapper(sys.argv[1], sys.argv[2])

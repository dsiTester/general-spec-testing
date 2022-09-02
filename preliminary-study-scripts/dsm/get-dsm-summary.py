import csv
import os

dsm_scripts_dir = os.path.dirname(os.path.abspath(__file__))
scripts_dir = os.path.dirname(dsm_scripts_dir)
base_dir = os.path.dirname(scripts_dir)
data_dir = os.path.join(os.path.join(base_dir, "data"), "dsm")
out_dir = os.path.join(data_dir, "summaries")

def read_proj(proj):
    specs = []
    proj_dir = os.path.join(data_dir, proj)
    acc_classes = 0
    acc_num_initial_states = 0
    acc_num_final_states = 0
    acc_num_transitions = 0
    acc_num_states = 0
    acc_num_selfloops = 0
    for filename in os.listdir(proj_dir):
        if (filename.endswith(".txt")):
            class_name = filename.split("_")[0] # ".".join(filename.split(".")[:-1])
            with open(os.path.join(proj_dir, filename)) as f:
                states = set() # need to count states from the transitions
                # first line specifies the number of initial states
                num_initial_states = int(f.readline())
                # FIXME: is there a more intelligent way to do the below?
                for i in range(num_initial_states):
                    f.readline()
                # after the initial states there is a line specifying the number of final states
                num_final_states = int(f.readline())
                # FIXME: is there a more intelligent way to do the below?
                for i in range(num_final_states):
                    f.readline()
                # then, the number of transitions is specified
                num_transitions = int(f.readline())
                num_selfloops = 0
                for i in range(num_transitions):
                    split = f.readline().split()
                    states.add(split[0])
                    states.add(split[1])
                    if (split[0] == split[1]):
                        num_selfloops += 1
                num_states = len(states)
                specs.append({ "className" : class_name, "numInitialStates" : num_initial_states, "numFinalStates" : num_final_states, "numStates" : num_states, "numTransitions" : num_transitions , "numSelfLoops" : num_selfloops})
                acc_classes += 1
                acc_num_initial_states += num_initial_states
                acc_num_final_states += num_final_states
                acc_num_transitions += num_transitions
                acc_num_states += num_states
                acc_num_selfloops += num_selfloops
    specs.append({ "className" : "total; " + str(acc_classes), "numInitialStates" : acc_num_initial_states, "numFinalStates" : acc_num_final_states, "numStates" : acc_num_states, "numTransitions" : acc_num_transitions, "numSelfLoops" : acc_num_selfloops})
    return specs

def output_data(proj, specs):
    keys = [ "className", "numInitialStates", "numFinalStates", "numStates", "numTransitions", "numSelfLoops" ]
    header = { key : key for key in keys }
    specs.insert(0, header)
    with open(os.path.join(out_dir, proj + ".csv"), "w") as out:
        writer = csv.DictWriter(out, fieldnames = keys)
        writer.writerows(specs)

def main():
    if not (os.path.isdir(data_dir)):
        print("[ERROR] " + data_dir + " does not exist!")
        exit
    if not (os.path.isdir(out_dir)):
        os.mkdir(out_dir)
    for proj in os.listdir(data_dir):
        if (proj == "summaries"): # this is our output directory
            continue
        print("[LOG] processing DSM results from project " + proj)
        specs = read_proj(proj)
        output_data(proj, specs)

if __name__ == '__main__':
    main()


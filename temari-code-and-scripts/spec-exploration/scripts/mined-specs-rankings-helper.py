def find_num_states_and_transitions_in_file(dirname, filename):
    states = set()
    num_transitions = 0
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
                num_transitions += 1

    return { "project" : project, "spec-id" : ident, "states" : len(states), "transitions" : num_transitions }

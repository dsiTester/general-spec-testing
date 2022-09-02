import csv
import os
import pandas as pd
import shutil
import sys

scripts_dir=os.path.dirname(os.path.abspath(__file__))
base_dir=os.path.dirname(scripts_dir)

def setup(fsm_txt, ident, out_dir):
    automaton_dir = os.path.join(out_dir, ident)
    # erasing the previous version
    if os.path.isdir(automaton_dir):
        for f in os.listdir(automaton_dir):
            os.remove(os.path.join(automaton_dir, f))
        os.rmdir(automaton_dir)
    os.mkdir(automaton_dir)
    shutil.copy(fsm_txt, os.path.join(automaton_dir, ident + ".txt"))
    return automaton_dir

def output_label_map(label_map, ident, automaton_out_dir):
    keys = ["transition-id", "method-sig" ]
    label_list = [ { key : key for key in keys } ]
    for lbl in label_map:
        label_list.append(label_map[lbl])
    with open(os.path.join(automaton_out_dir, ident + ".mapping"), "w") as out:
        # for entry in label_list:
        #     out.write(entry["transition-id"] + "," + entry["method-sig"] + "\n")
        writer = csv.DictWriter(out, fieldnames = keys, dialect = "unix", quoting=csv.QUOTE_NONE)
        writer.writerows(label_list)

def convert_state_name(name):
    if name[0].isdigit():
        name = "STATE" + name
    return name.replace('_', '')

def clean_ident_name(ident):
    return ident.replace("$", "");

def convert_label_and_update_label_mapping(label_map, lbl, ext):
    if lbl == "<BEGIN>" or lbl == "<END>" or lbl == "<START>" or lbl == "<IO-LEAK>": # DICE/DSM-specific transitions that we don't care about
        return "EPSILON"
    if ( ext != "" ):
        full_lbl = ext + "." + lbl
    else:
        full_lbl = lbl

    short_lbl = "".join(full_lbl.split(".")[-2:]).replace("(", "").replace(")", "").replace("/", "").replace(";", "").replace("$", "").replace("[", "ARR")
    label_map[lbl] = { "transition-id" : short_lbl , "method-sig" : full_lbl }
    return short_lbl

def parse(filename, ext):
    states = set()
    num_transitions = 0
    letters = set()
    start_states = set()
    final_states = set()
    transition_map = {}
    transition_list = []
    label_map = {}
    with open(filename, "r") as f:
        start_state_count = -1
        final_state_count = -1
        for line in f:
            sline = line.split()
            if len(sline) == 3:
                src = convert_state_name(sline[0])
                tgt = convert_state_name(sline[1])
                lbl = convert_label_and_update_label_mapping(label_map, sline[2], ext)
                letters.add(lbl)
                if (not src in transition_map):
                    transition_map[src] = [(tgt, lbl)]
                else:
                    transition_map[src].append((tgt, lbl))
                transition_list.append({"source": src, "target": tgt, "label": lbl})
                states.add(src)
                states.add(tgt)
            elif start_state_count == -1: # this is effectively the first line in the file
                start_state_count = int(sline[0])
            elif start_state_count > 0: # reading the first couple of lines that specify the start state
                start_states.add(convert_state_name(sline[0]))
                start_state_count -= 1
            elif final_state_count == -1: # previous conditions will be true if we were still reading start states
                final_state_count = int(sline[0])
            elif final_state_count > 0:
                if (sline[0] != "null"):
                    final_states.add(convert_state_name(sline[0]))
                final_state_count -= 1
            else: # this is probably the number of transitions
                # print("extra line: " + line)
                continue

    return start_states, final_states, transition_map, transition_list, label_map, states

def add_new_start_state(start_states, transition_map, transition_list, states):
    states.add("START")
    transition_map["START"] = []
    for state in start_states:
        transition_map["START"].append((state, "EPSILON"))
        transition_list.insert(0, {"source": "START", "target": state, "label": "EPSILON"})

def create_pml_header(states, label_map):
    header_string = ""
    # All of the transitions
    #mtype = { InetAddressValidatorisValidInet4Address, InetAddressValidatorisValid, RegexValidatormatch, UrlValidatorcountToken, UrlValidatorisValidAuthority, UrlValidatorisValidFragment, UrlValidatorisValid, UrlValidatorisValidPath, UrlValidatorisValidQuery, UrlValidatorisValidScheme };
    all_transitions_str = "mtype = { EPSILON" # by default we will have epsilon transition for the start state
    for label in label_map:
        all_transitions_str += ", " + label_map[label]["transition-id"]
    all_transitions_str += " };"
    header_string += all_transitions_str + "\n"

    # mtype = { s0, s1, s2, s3, s4, s5, s6 };
    all_states_str = "mtype = { " + ", ".join(list(states)) + " };"
    header_string += all_states_str + "\n\n"

    #mtype state = s0;
    #mtype event = EPSILON;
    start_state_str = "mtype state = START;\nmtype event = EPSILON;\n"
    header_string += start_state_str
    header_string += "active proctype P() {\n\n"
    header_string += "\tprintf(\"The state is now %e and event is %e\\n\", state, event)\n"
    header_string += "\tdo\n"
    header_string += "\t:: if\n"

    return header_string

def create_src_transition_to_target_map(transition_list): # acccount for nondeterminism
    new_map = {}
    for transition in transition_list:
        key = (transition["source"], transition["label"])
        if key in new_map:
            new_map[key].append(transition["target"])
        else:
            new_map[key] = [transition["target"]]

    return new_map

def get_outwards_transitions(targets, transition_map):
    outwards_transitions = []
    for target in targets:
        if not target in transition_map:
            continue
        for pair in transition_map[target]:
            next_target, next_transition = pair
            outwards_transitions.append((target, next_transition))

    return outwards_transitions

def create_pml_body(transition_map, transition_list, final_states):
    body_string = ""
    src_transition_to_target_map = create_src_transition_to_target_map(transition_list)
    done_transitions = set()
    for transition in transition_list:
        if (transition["source"], transition["label"]) in done_transitions:
            continue
        # what state we are currently on, and what transition we just read
        body_string += "\t   :: state == " + transition["source"] + " && event == " + transition["label"] + " ->"
        # what states are we going to?
        targets = src_transition_to_target_map[(transition["source"], transition["label"])]
        outgoing_edges_from_targets = get_outwards_transitions(targets, transition_map)
        if len(outgoing_edges_from_targets) == 1:
            next_state, next_transition = outgoing_edges_from_targets[0]
            body_string += "\tstate = " + next_state + "; event = " + next_transition
        elif len(outgoing_edges_from_targets) == 0 and len(targets) == 1: # the next state is a final state w no outgoing edges
            body_string += "\tstate = " + targets[0]
        elif len(outgoing_edges_from_targets) == 0 and len(targets) > 1: # the next state is a final state w no outgoing edges
            body_string += "\n\t\t\tif\n"
            for target in targets:
                body_string += "\t\t\t:: state = " + target + "\n"
            body_string += "\t\t\tfi"
        else:
            body_string += "\n\t\t\tif\n"
            for outgoing_edge in outgoing_edges_from_targets:
                next_state, next_transition = outgoing_edge
                body_string += "\t\t\t:: state = " + next_state + "; event = " + next_transition + "\n"
            body_string += "\t\t\tfi"

        done_transitions.add((transition["source"], transition["label"]))
        body_string += "\n"
    # for any final states that don't have a transition, we need to break
    for final_state in final_states:
        if not final_state in transition_map:
            body_string += "\t   :: state == " + final_state + " -> break\n"
    return body_string + "\n"

def create_pml_footer():
    return """\t   fi
\t   printf(\"The state is now %e and event is %e\\n\", state, event)
\tod
}\n"""

def create_pml(states, final_states, transition_map, transition_list, label_map, out_dir, ident):
    with open(os.path.join(out_dir, ident + ".pml"), "w") as f:
        print("Writing PML file to " + os.path.join(out_dir, ident + ".pml"))
        f.write(create_pml_header(states, label_map))
        f.write(create_pml_body(transition_map, transition_list, final_states))
        f.write(create_pml_footer())
    return

def preview_pml(states, final_states, transition_map, transition_list, label_map, out_dir, ident):
    print("=====PML PREVIEW")
    print(create_pml_header(states, label_map))
    print(create_pml_body(transition_map, transition_list, final_states))
    print(create_pml_footer())

def wrapper(fsm_txt, miner_name, project, out_dir):
    if (not os.path.isdir(out_dir)):
        os.mkdir(out_dir)
    filename = os.path.basename(fsm_txt)
    if "_model.txt" in filename: # dsm has its own naming convention
        ident = "@".join([miner_name, project, filename.split("_model.txt")[0]])
        ext = filename.split("_model.txt")[0]
    elif ("dsm" in miner_name or "dice" == miner_name):
        ident = "@".join([miner_name, project, filename.split(".txt")[0]])
        ext = filename.split(".txt")[0]
    else:
        ident = "@".join([miner_name, project, filename.split(".txt")[0]])
        ext = ""
    # clean ident
    ident = clean_ident_name(ident)

    automaton_out_dir = setup(fsm_txt, ident, out_dir)
    original_start_states, final_states, transition_map, transition_list, label_map, states = parse(fsm_txt, ext)
    add_new_start_state(original_start_states, transition_map, transition_list, states)
    # print("Start states: " + str(start_states))
    # print("Final states: " + str(final_states))
    # print("Transition map:")
    # for source in transition_map:
    #     print("from " + source + ": " + str(transition_map[source]))
    # for lbl in label_map:
    #     print("Label map: " + str(label_map[lbl]))
    # output label map to the mapping file
    # preview_pml(states, final_states, transition_map, transition_list, label_map, automaton_out_dir, ident)
    output_label_map(label_map, ident, automaton_out_dir)
    create_pml(states, final_states, transition_map, transition_list, label_map, automaton_out_dir, ident)

if __name__ == '__main__':
    if len(sys.argv) != 5:
        print("Usage: " + sys.argv[0] + " fsm miner_name project out_dir")
        print("where out_dir is the directory where all of the autogenerated automata should be")
        sys.exit(1)
    wrapper(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4])

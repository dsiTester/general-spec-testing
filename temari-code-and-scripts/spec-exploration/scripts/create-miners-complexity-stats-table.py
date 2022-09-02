import csv
import os
import pandas as pd
import statistics
import sys

scripts_dir=os.path.dirname(os.path.abspath(__file__))
base_dir=os.path.dirname(scripts_dir)
data_dir=os.path.join(base_dir, "data")
rankings_dir=os.path.join(data_dir, "rankings-for-model-checking")
out_dir=os.path.join(data_dir, "miners-complexity")

def get_means(complexity_dict):
    mean_dict = {}
    for key in complexity_dict:
        if (key == "miner"):
            mean_dict[key] = complexity_dict[key]
        else:
            mean_dict[key] = sum(complexity_dict[key]) / len(complexity_dict[key])

    return mean_dict

def get_medians(complexity_dict):
    medians_dict = {}
    for key in complexity_dict:
        if (key == "miner"):
            medians_dict[key] = complexity_dict[key]
        else:
            medians_dict[key] = statistics.median(complexity_dict[key])

    return medians_dict

def get_modes(complexity_dict):
    modes_dict = {}
    for key in complexity_dict:
        if (key == "miner"):
            modes_dict[key] = complexity_dict[key]
        else:
            modes_dict[key] = statistics.mode(complexity_dict[key])
    return modes_dict

def get_maxs(complexity_dict):
    max_dict = {}
    for key in complexity_dict:
        if (key == "miner"):
            max_dict[key] = complexity_dict[key]
        else:
            max_dict[key] = max(complexity_dict[key])
    return max_dict

def get_totals(complexity_dict):
    totals_dict = {}
    for key in complexity_dict:
        if (key == "miner"):
            totals_dict[key] = complexity_dict[key]
        else:
            totals_dict[key] = sum(complexity_dict[key])
    return totals_dict

def read_rankings(miner):
    categories = [ "true-spec", "spurious-spec", "no-break-pass", "no-mined-spec" ]

    rankings_file = os.path.join(rankings_dir, miner + "-rankings.csv")
    if not os.path.isfile(rankings_file):
        return

    states_list = []
    transitions_list = []
    letters_list = []
    total_edge_pairs_list = []
    rankings = pd.read_csv(rankings_file)
    for _, row in rankings.iterrows():
        states_list.append(row["states"])
        transitions_list.append(row["transitions"])
        letters_list.append(row["letters"])
        total_edge_pairs_list.append(row["total-edge-pairs"])

    complexity_dict = { "miner" : miner, "states" : states_list, "transitions" : transitions_list, "letters" : letters_list, "total-edge-pairs" : total_edge_pairs_list }
    means = get_means(complexity_dict)
    medians = get_medians(complexity_dict)
    modes = get_modes(complexity_dict)
    maxs = get_maxs(complexity_dict)
    totals = get_totals(complexity_dict)

    return ( means, medians, modes, maxs, totals )

def output_to_csv(keys, list_to_output, dirname, filename):
    with open(os.path.join(dirname, filename), "w") as out:
        writer = csv.DictWriter(out, fieldnames = keys, dialect='unix', quoting=csv.QUOTE_NONE)
        writer.writerows(list_to_output)

def wrapper():
    if (not os.path.isdir(out_dir)):
        os.mkdir(out_dir)
    keys = [ "miner", "states", "transitions", "letters", "total-edge-pairs", "" ]
    header = { key : key for key in keys }
    means_dicts = [header]
    medians_dicts = [header]
    modes_dicts = [header]
    maxs_dicts = [header]
    totals_dicts = [header]
    for f in os.listdir(rankings_dir):
        print("processing file: " + f)
        # if ("bdd-3" in f or "#" in f): # bdd3 is broken rn :(
        #     continue
        if (f.endswith("-rankings.csv")):
            miner_name = f.split("-rankings.csv")[0]
            print("Computing stats for " + miner_name)
            ( means, medians, modes, maxs, totals ) = read_rankings(miner_name)
            means_dicts.append(means)
            medians_dicts.append(medians)
            modes_dicts.append(modes)
            maxs_dicts.append(maxs)
            totals_dicts.append(totals)

    output_to_csv(keys, means_dicts, out_dir, "means.csv")
    output_to_csv(keys, medians_dicts, out_dir, "medians.csv")
    output_to_csv(keys, modes_dicts, out_dir, "modes.csv")
    output_to_csv(keys, maxs_dicts, out_dir, "maxs.csv")
    output_to_csv(keys, totals_dicts, out_dir, "totals.csv")

if __name__ == '__main__':
    wrapper()

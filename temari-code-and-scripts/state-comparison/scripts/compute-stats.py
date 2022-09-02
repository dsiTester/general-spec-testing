from analysis_helper import *
import pandas as pd
import os
import sys
import copy
import csv

scripts_dir=os.path.dirname(os.path.abspath(__file__))
base_dir=os.path.dirname(scripts_dir)
out_dir=os.path.join(base_dir, "data", "analysis-data")


def output_original_dsi_stats(option, project, data):
    # "original-dsi-verdict"
    # precision over true specs
    # precision over spurious specs
    # let's filter out mixed from the precision and recall for now. (that's what we did for FSE)
    total = len(data)
    accurate_set = set()
    manual_spurious_set = set()
    manual_true_set = set()
    dsi_ls_set = set()
    dsi_lv_set = set()

    for entry in data:
        # print(entry.keys())
        # print(entry)
        spec_id = entry["id"]
        dsi = entry[option]
        manual = entry["manual-verdict"]
        if (manual == "unknown"):
            total -= 1
            continue
        # accumulating dsi verdicts for each of the manual verdicts we want to compute recall over
        if (manual == "spurious-spec"):
            manual_spurious_set.add(spec_id)
        elif ("true-spec" in manual):
            manual_true_set.add(spec_id)
        # accumulating manual verdicts for each of the DSI verdicts we want to compute precision over
        if (dsi == "ls"):
            dsi_ls_set.add(spec_id)
        elif (dsi == "lv"):
            dsi_lv_set.add(spec_id)

    # accurate_true_specs = len([man_verdict for man_verdict in dsi_lv_set if "true-spec" in man_verdict ])
    # accurate_spurious_specs = len([man_verdict for man_verdict in dsi_ls_set if man_verdict == "spurious-spec" ])
    accurate_true_specs = len(manual_true_set.intersection(dsi_lv_set))
    accurate_spurious_specs = len(manual_spurious_set.intersection(dsi_ls_set))
    total_accurate = accurate_true_specs + accurate_spurious_specs
    acc_percent = round(100*(total_accurate / total), 2)

    # calculating precision...
    true_spec_precision = round(( accurate_true_specs / len(dsi_lv_set)) * 100, 2)
    spurious_spec_precision = round(( accurate_spurious_specs / len(dsi_ls_set)) * 100, 2)

    # calculating recall...
    true_spec_recall = round((accurate_true_specs / len(manual_true_set)) * 100, 2)
    spurious_spec_recall = round((accurate_spurious_specs / len(manual_spurious_set)) * 100, 2)

    return { "project" : project, "total-specs" : total, "accurate-true-specs" : accurate_true_specs, "accurate-spurious-specs" : accurate_spurious_specs, "total-accurate" : total_accurate, "acc-percent" : acc_percent, "true-spec-precision" : true_spec_precision, "spurious-spec-precision" : spurious_spec_precision, "true-spec-recall" : true_spec_recall, "spurious-spec-recall" : spurious_spec_recall , "#dsi-lv" : len(dsi_lv_set) , "#dsi-ls" : len(dsi_ls_set), "#man-ts" : len(manual_true_set), "#man-ss" : len(manual_spurious_set) }


# set add_total_accuracy(accuracy_keys, accuracy_list):
#     total = { key : 0 for key in accuracy_keys }
#     lst = ["total-specs", "accurate-true-specs", "accurate-spurious-specs", "total-accurate" ]

#     for col in lst:
#         for entry in accuracy_list:
#             total[col] += entry[col]
#     total["acc-percent"] = round(100*(total["total-accurate"] / total["total-specs"]), 2)

#     total["true-spec-precision"] = round(( total["accurate-true-specs"] / ) * 100, 2)
#     spurious_spec_precision = round(( accurate_spurious_specs / len(dsi_ls_set)) * 100, 2)

#     # calculating recall...
#     true_spec_recall = round((accurate_true_specs / len(manual_true_set)) * 100, 2)
#     spurious_spec_recall = round((accurate_spurious_specs / len(manual_spurious_set)) * 100, 2)

def output_stats_for_option(option, in_dir):
    if (option == "dsi"):
        col_header = "original-dsi-verdict"
    elif (option == "state-and-dsi"):
        col_header = "updated-dsi-verdict"
    all_projects_data = {}
    accuracy_keys = [ "project", "total-specs", "accurate-true-specs", "accurate-spurious-specs", "total-accurate", "acc-percent", "true-spec-precision", "spurious-spec-precision", "true-spec-recall", "spurious-spec-recall" , "#dsi-lv", "#dsi-ls", "#man-ts", "#man-ss" ]
    accuracy_list = [{ key : key for key in accuracy_keys }]
    acc_accurate_true_specs = 0
    acc_accurate_spurious_specs = 0
    acc_dsi_lv = 0
    acc_dsi_ls = 0
    acc_man_ts = 0
    acc_man_ss = 0
    total_entry = { key : 0 for key in accuracy_keys }
    total_entry["project"] = "TOTAL"
    for results_file in os.listdir(in_dir):
        print(os.path.join(in_dir, results_file))
        project_name = results_file.split("-results")[0]
        all_projects_data[project_name] = read_csv(os.path.join(in_dir, results_file))
        project_stats = output_original_dsi_stats(col_header, project_name, all_projects_data[project_name])
        accuracy_list.append(project_stats)
        for key in accuracy_keys:
            if key != "project":
                total_entry[key] += project_stats[key]
    total_entry["true-spec-precision"] = round((total_entry["accurate-true-specs"] / total_entry["#dsi-lv"]) * 100, 2)
    total_entry["spurious-spec-precision"] = round((total_entry["accurate-spurious-specs"] / total_entry["#dsi-ls"]) * 100, 2)
    total_entry["true-spec-recall"] = round((total_entry["accurate-true-specs"] / total_entry["#man-ts"]) * 100, 2)
    total_entry["spurious-spec-recall"] = round((total_entry["accurate-spurious-specs"] / total_entry["#man-ss"]) * 100, 2)
    total_entry["acc-percent"] = round((total_entry["total-accurate"] / total_entry["total-specs"]) * 100, 2)
    accuracy_list.append(total_entry)

    # true_spec_precision = round(( accurate_true_specs / len(dsi_lv_set)) * 100, 2)
    # spurious_spec_precision = round(( accurate_spurious_specs / len(dsi_ls_set)) * 100, 2)

    # # calculating recall...
    # true_spec_recall = round((accurate_true_specs / len(manual_true_set)) * 100, 2)
    # spurious_spec_recall = round((accurate_spurious_specs / len(manual_spurious_set)) * 100, 2)

    output_to_csv(accuracy_keys, accuracy_list, out_dir, "new-" + option + "-accuracies.csv")

def wrapper(in_dir):
    output_stats_for_option("dsi", in_dir)
    output_stats_for_option("state-and-dsi", in_dir)

if __name__ == '__main__':
    if len(sys.argv) != 2:
        print("Usage: " + sys.argv[0] + " in_dir")
        sys.exit(1)
    # if not os.path.exists(sys.argv[1]):
    #     print("file does not exist!")
    #     sys.exit(1)
    wrapper(sys.argv[1])

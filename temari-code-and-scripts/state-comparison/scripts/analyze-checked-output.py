from analysis_helper import read_csv
import pandas as pd
import os
import sys
import copy
import csv

def get_relevant_tags(relations, tags):
    relevant_tags = ["TOTAL"]
    for tag in tags.split(";"):
        if tag in relations:
            relevant_tags.append(tag)

    if len(relevant_tags) == 1:
        relevant_tags.insert(0, "OTHER")

    return relevant_tags

def tags_in_category(spec_tags, cat_tags):
    spec_tags_set = set(spec_tags)
    cat_tags_set = set(cat_tags)
    return (len(spec_tags_set.intersection(cat_tags_set)) > 0)


# check the automated spec validation outcome with the manual inspection outcome
# mode is either "original-dsi-verdict" or "updated-dsi-verdict"
def compute_num_verdict_accurate(data, mode):
    accurate_set = set()
    ts_sts_hit_set = set()
    spurious_hit_set = set()
    total_ts_sts = 0
    total_spurious = 0
    for entry in data:
        dsi = entry[mode]
        manual = entry["manual-verdict"]
        # accumulating totals
        if (manual == "spurious-spec"):
            total_spurious += 1
        elif ("true-spec" in manual):
            total_ts_sts += 1
        # different conditional guard for collecting hits
        if (dsi == "ls" and manual == "spurious-spec"):
            accurate_set.add(entry["id"])
            spurious_hit_set.add(entry["id"])
        elif (dsi == "lv" and "true-spec" in manual): # counting STS as well.
            accurate_set.add(entry["id"])
            ts_sts_hit_set.add(entry["id"])
    ts_sts_hit_percent = round(100*(len(ts_sts_hit_set) / total_ts_sts), 2)
    spurious_hit_percent = round(100*(len(spurious_hit_set) / total_spurious), 2)
    return accurate_set, len(ts_sts_hit_set), len(spurious_hit_set), ts_sts_hit_percent, spurious_hit_percent

# dump cases where the state checker was wrong
def compare_num_state_verdict_inaccurate(data, out_dir, project_name):
    inaccurate_list = []
    for entry in data:
        state = entry["share-state"]
        manual = entry["manual-verdict"]
        if (state == "no" and "true-spec" in manual):
            inaccurate_list.append(entry)
        elif (state == "yes" and manual == "spurious-spec"):
            inaccurate_list.append(entry)
        elif (state == "err"):
            inaccurate_list.append(entry)

    keys = [ "id", "share-state", "original-dsi-verdict", "updated-dsi-verdict", "manual-verdict", "tags", "a-set-b-set", "a-set-b-get", "a-get-b-set", "a-get-b-get" ]
    with open(os.path.join(out_dir, project_name + "-inspection-starter.csv"), "w") as out:
        out.write("id,share-state(tool),original-dsi,updated-dsi,verdict,manual-tags,a-set-b-set,a-set-b-get,a-get-b-set,a-get-b-get\n")
        for inaccurate_entry in inaccurate_list:
            out.write(",".join([inaccurate_entry[key] for key in keys]) + "\n")

def compare_with_tags(data, out_dir, project_name):
    relations = ["UNRELATED_STATEFUL_METHODS", "ONE_STATELESS_METHOD", "ONE_PURE_SETTER", "CONNECTION_DOES_NOT_NECESSITATE_ORDERING", "UNRELATED_STATELESS_METHODS", "UNRELATED_PURE_SETTERS", "TRUE_SPEC_WITH_CALLER", "OTHER", "TOTAL" ]
    no_overlap_tags = ["UNRELATED_STATEFUL_METHODS", "ONE_STATELESS_METHOD", "UNRELATED_STATELESS_METHODS", "UNRELATED_PURE_SETTERS"]
    overlap_tags = [ "CONNECTION_DOES_NOT_NECESSITATE_ORDERING" ]
    unsure_tags = ["ONE_PURE_SETTER", "TRUE_SPEC_WITH_CALLER", "OTHER"]

    results = { relation : { "num_dont_share" : 0, "num_share" : 0, "num_mixed" : 0, "num_err" : 0, "total" : 0} for relation in relations}

    accurate = set()
    inaccurate = set()
    error = set()
    q = set()

    for entry in data:
        spec_id = entry["id"]
        relevant_tags = get_relevant_tags(relations, entry["tags"])

        if len(relevant_tags) > 2: # we have 1 by default for "TOTAL"
            key = ""
        if entry["share-state"] == "no":
            key = "num_dont_share"
        elif entry["share-state"] == "yes":
            key = "num_share"
        elif entry["share-state"] == "mixed":
            key = "num_mixed"
        else:
            key = "num_err"
        for relevant_tag in relevant_tags:
            results[relevant_tag][key] += 1
            results[relevant_tag]["total"] += 1

        if (tags_in_category(relevant_tags, no_overlap_tags) and (key == "num_dont_share")):
            accurate.add(spec_id)
        elif(tags_in_category(relevant_tags, overlap_tags) and (key == "num_share")): # accurate decisions
            accurate.add(spec_id)
        elif (tags_in_category(relevant_tags, overlap_tags) and (key == "num_dont_share")):
            inaccurate.add(spec_id)
        elif (tags_in_category(relevant_tags, no_overlap_tags) and (key == "num_share")):
            inaccurate.add(spec_id)
        elif (key == "num_err"):
            error.add(spec_id)
        else:
            q.add(spec_id)

    cols = ["tag", "num_dont_share", "num_share", "num_mixed", "num_err", "total"]
    header = { col : col for col in cols }
    out_list = [header]
    for relation in results:
        rmap = copy.deepcopy(results[relation])
        rmap["tag"] = relation
        out_list.append(rmap)

    with open(os.path.join(out_dir, project_name + "-tags-breakdown.csv"), "w") as out:
        writer = csv.DictWriter(out, fieldnames = cols)
        writer.writerows(out_list)

    return accurate, inaccurate, q, error

def output_set(data, set_to_output, out_dir, project_name, set_name):
    data_map = { entry["id"] : entry for entry in data }
    out_data = []
    for nid in set_to_output:
        out_data.append(data_map[nid])
    keys = [ "id", "share-state", "original-dsi-verdict", "updated-dsi-verdict", "manual-verdict", "tags", "a-set-b-set", "a-set-b-get", "a-get-b-set", "a-get-b-get" ]
    with open(os.path.join(out_dir, project_name + "-" + set_name + ".csv"), "w") as out:
        out.write("id,share-state(tool),original-dsi,updated-dsi,verdict,manual-tags,a-set-b-set,a-set-b-get,a-get-b-set,a-get-b-get,notes\n")
        for out_entry in out_data:
            out.write(",".join([out_entry[key] for key in keys]) + ",\n")

def get_num_to_run_dsi(data):
    acc=set()
    for entry in data:
        if entry["share-state"] == "yes" or entry["share-state"] == "err":
            acc.add(entry["id"])

    return acc

def get_num_with_jdk_check(data):
    acc=set()
    for entry in data:
        if entry["used-jdk"] == "yes":
            acc.add(entry["id"])

    return acc

def get_num_nbps(project_name, dsi_outcomes_file, num_evaluated):
    dsi_outcomes = pd.read_csv(dsi_outcomes_file)
    for _, row in dsi_outcomes.iterrows():
        if (row["project"] == project_name):
            return int(row["total"]) - (int(row["unknown"]) + num_evaluated)
            # return int(row["no-break-pass (direct)"]) + int(row["no-break-pass (indirect)"])

def get_total_in_proj_wo_unknowns(project_name, dsi_outcomes_file):
    dsi_outcomes = pd.read_csv(dsi_outcomes_file)
    for _, row in dsi_outcomes.iterrows():
        if (row["project"] == project_name):
            return int(row["total"]) - (int(row["unknown"]))
            # return int(row["no-break-pass (direct)"]) + int(row["no-break-pass (indirect)"])

"""
Janky function for safe division. Returns 0 if we're dividing by zero
"""
def divide(dividend, divisor):
    if divisor == 0:
        return 0
    else:
        return (dividend / divisor)


def remove_manual_unknown(data):
    new_data = []
    manual_unknown = 0
    for entry in data:
        if (entry["manual-verdict"] != "unknown"):
            new_data.append(entry)
        else:
            manual_unknown += 1
    return new_data, manual_unknown

def wrapper(in_file, out_dir, project_name, dsi_outcomes_file):
    data = read_csv(in_file)
    data, manual_unknown = remove_manual_unknown(data)
    print(manual_unknown)
    original_dsi_accurate_set, original_dsi_ts, original_dsi_ns, original_dsi_ts_hit_percent, original_dsi_ns_hit_percent = compute_num_verdict_accurate(data, "original-dsi-verdict")
    updated_dsi_accurate_set, _, _, updated_dsi_ts_hit_percent, updated_dsi_ns_hit_percent = compute_num_verdict_accurate(data, "updated-dsi-verdict")
    original_dsi_accurate_num = len(original_dsi_accurate_set)
    updated_dsi_accurate_num = len(updated_dsi_accurate_set)
    total_num = len(data)
    compare_num_state_verdict_inaccurate(data, out_dir, project_name)

    dsi_accurate_but_state_inaccurate_set = original_dsi_accurate_set.difference(updated_dsi_accurate_set)
    dsi_accurate_but_state_inaccurate = len(dsi_accurate_but_state_inaccurate_set)
    output_set(data, dsi_accurate_but_state_inaccurate_set, out_dir, project_name, "newly-inaccurate")

    state_accurate_but_dsi_inaccurate = len(updated_dsi_accurate_set.difference(original_dsi_accurate_set))

    tags_accurate, tags_inaccurate, tags_q, error = compare_with_tags(data, out_dir, project_name)
    output_set(data, tags_inaccurate, out_dir, project_name, "tags-inaccurate")
    run_dsi_set = get_num_to_run_dsi(data)
    run_jdk_set = get_num_with_jdk_check(data)
    run_dsi_jdk_num = len(run_dsi_set.intersection(run_jdk_set))
    # num_nbps = get_num_nbps(project_name, dsi_outcomes_file, total_num)
    # verdict_accurate_with_nbp_checker = num_nbps + updated_dsi_accurate_num
    # total_with_nbp = total_num + num_nbps
    total_with_nbp = get_total_in_proj_wo_unknowns(project_name, dsi_outcomes_file)
    num_nbps = total_with_nbp - total_num
    verdict_accurate_with_nbp_checker = num_nbps + updated_dsi_accurate_num

    with open(os.path.join(out_dir, project_name + "-analysis-summary.csv"), "w") as out:
        out.write("id,description,num\n")
        out.write("0,Number of total specs (non-nbp)," + str(total_num) + "\n")
        out.write("1,Number of verdict-accurate specs (DSI)," + str(original_dsi_accurate_num) + "\n")
        out.write("2,Number of verdict-accurate specs (DSI + state),"+ str(updated_dsi_accurate_num) + "\n")
        out.write("3,Number of newly verdict-accurate specs ((2) - (1))," + str(state_accurate_but_dsi_inaccurate) + "\n")
        out.write("4,Number of newly verdict-inaccurate specs ((1) - (2))," + str(dsi_accurate_but_state_inaccurate) + "\n")
        out.write("5,% accuracy (DSI)," + str(round(100*(original_dsi_accurate_num / total_num), 2)) + "\n")
        out.write("6,% accuracy (DSI + state)," + str(round(100*(updated_dsi_accurate_num / total_num), 2)) + "\n")
        out.write("7,Number of tag-accurate state comparisons," + str(len(tags_accurate)) + "\n")
        out.write("8,Number of tag-inaccurate state comparisons," + str(len(tags_inaccurate)) + "\n")
        out.write("9,Number of tag-unsure state comparisons," + str(len(tags_q)) + "\n")
        out.write("10,Number of errored state comparisons," + str(len(error)) + "\n")
        out.write("11,Number of specs for which additional DSI would be run (yes or err)," + str(len(run_dsi_set)) + "\n")
        out.write("12,Number of specs for which JDK analysis was done," + str(len(run_jdk_set)) + "\n")
        out.write("13,Number of specs for which JDK and DSI were run," + str(run_dsi_jdk_num) + "\n")
        out.write("14,Number of total specs (with NBP)," + str(total_with_nbp) + "\n")
        out.write("15,Number of verdict-accurate specs (DSI + state + ideal NBP checker)," + str(verdict_accurate_with_nbp_checker) + "\n")
        out.write("16,% accuracy (DSI + state + ideal NBP checker / total specs with NBP)," + str(round(100*(verdict_accurate_with_nbp_checker / total_with_nbp), 2)) + "\n")
        out.write("17,Number of accurate LV specs in original DSI," + str(original_dsi_ts) + "\n")
        out.write("18,Number of accurate LS specs in original DSI," + str(original_dsi_ns) + "\n")
        out.write("19,% hit on True Specs/STS for original DSI," + str(original_dsi_ts_hit_percent) + "\n")
        out.write("20,% hit on Spurious Specs for original DSI," + str(original_dsi_ns_hit_percent) + "\n")
        out.write("21,% hit on True Specs/STS for DSI + state," + str(updated_dsi_ts_hit_percent) + "\n")
        out.write("22,% hit on Spurious Specs for DSI + state," + str(updated_dsi_ns_hit_percent) + "\n")


if __name__ == '__main__':
    if len(sys.argv) != 5:
        print("Usage: " + sys.argv[0] + " in_file out_dir project_name dsi-inspection-breakdown-file")
        sys.exit(1)
    if not os.path.exists(sys.argv[1]):
        print("file does not exist!")
        sys.exit(1)
    wrapper(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4])

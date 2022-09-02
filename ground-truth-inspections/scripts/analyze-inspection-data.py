"""

This script reads in the json files in the
dsi-inspections/inspections/${project} directory for each project, and
outputs csv files/figures that correspond to the analysis that we are
looking in to.

"""
import collections
import copy
import json
import os
import sys
import csv
from create_inspections_with_tag_field import get_tags
from pathlib import Path
from shutil import rmtree
import subprocess
import matplotlib.pyplot as plt
from matplotlib_venn import venn3
from matplotlib_venn import venn3_unweighted


scripts_dir=os.path.dirname(os.path.abspath(__file__))
base_dir=os.path.dirname(scripts_dir)
templates_dir=base_dir + os.path.sep + "templates"
tagged_inspections_dir=base_dir + os.path.sep + "tagged-inspections"
intermediate_data_dir = os.path.join(base_dir, "data")
output_dir=os.path.join(intermediate_data_dir, "analysis")
tag_cat_dict = {"Return" : {"METHOD_A_RETURNS_VOID", "REPLACE_RETURN_WITH_UNEXPECTED_OUTPUT", "NULL_REPLACEMENT_CAUSED_NULLPOINTEREXCEPTION", "REPLACE_RETURN_WITH_CORRECT_VALUE_NONASSERTION", "REPLACE_RETURN_WITH_EXPECTED_OUTPUT", "DEFAULT_VALUE_SAME_AS_RETURN", "RETURN_VALUE_DISCARDED", "REPLACEMENT_TRIGGERS_CHECK_THAT_PREVENTS_B", "LOSSY_REPLACEMENT"}, "Relationship" : {"UNRELATED_STATEFUL_METHODS", "ONE_STATELESS_METHOD", "ONE_PURE_SETTER", "CONNECTION_DOES_NOT_NECESSITATE_ORDERING", "UNRELATED_STATELESS_METHODS", "UNRELATED_PURE_SETTERS", "TRUE_SPEC_WITH_CALLER", "MODIFIED_STATE_DOES_NOT_INTERSECT"}, "Exception": {"EXPECTED_EXCEPTION", "EXPECTED_EXCEPTION_NOT_THROWN", "SWALLOWED_EXCEPTION", "IMPLICITLY_EXPECTED_EXCEPTION", "DELAY_OF_A_CAUSES_UNEXPECTED_EXCEPTION"}, "Oracle" : {"WEAK_ORACLE", "ORDER_OF_ASSERTIONS"}, "Delay": {"STATE_POLLUTION_BY_DSI", "STATE_RESTORED", "CHECKS_MISDIRECTED_OUTPUT", "DELAY_CAUSES_TIMEOUT", "DELAY_CAUSES_OUTPUT_CORRUPTION"}, "Misc" : {"CONCURRENCY", "DYNAMIC_DISPATCH", "ASM_ERROR", "CONFIGURATION", "DYNAMIC_DISPATCH_SAME_METHOD", "SPECIAL_NBP", "SUPPLEMENTARY_EVIDENCE_BY_JAVADOC", "REVERSE_NBP", "UNINTERESTING_SPEC", "NO_COVERAGE", "MULTIPLE_PERTURBATIONS"}}
tag_cat_dict_cat_order = ["Relationship", "Return", "Exception", "Oracle", "Delay", "Misc"]

"""
Utility function to write output to csv.
"""
def output_to_csv(keys, list_to_output, dirname, filename):
    with open(os.path.join(dirname, filename), "w") as out:
        writer = csv.DictWriter(out, fieldnames = keys)
        writer.writerows(list_to_output)

"""
Janky function for safe division. Returns 0 if we're dividing by zero
"""
def divide(dividend, divisor):
    if divisor == 0:
        return 0
    else:
        return (dividend / divisor)

def retrieve_inspection_verdict_data_per_project(data):
    stats_map = {"true-spec" : 0, "spurious-spec" : 0, "unknown" : 0, "no-break-pass (direct)" : 0, "no-break-pass (indirect)" : 0, "sometimes-true-spec" : 0}
    for spec_entry in data:
        stats_map[spec_entry["verdict"]] += 1
    return {category : stats_map}

"""
Takes the data from a mixed verdict file, and sorts the specs into categories based on the verdict.
If there is a tie, the spec will be placed in "u" (since we couldn't come to a conclusion).
"""
def majority_from_mixed(data):
    mixed_majority_data = {"lv" : [], "ls" : [], "u" : [] }
    for spec_entry in data:
        lv_num = 0
        ls_num = 0
        u_num = 0
        if "LV-verdict-count" in spec_entry:
            lv_num = spec_entry["LV-verdict-count"]
        if "LS-verdict-count" in spec_entry:
            ls_num = spec_entry["LS-verdict-count"]
        if "U-verdict-count" in spec_entry:
            u_num += spec_entry["U-verdict-count"]
        if "E-verdict-count" in spec_entry:
            u_num += spec_entry["E-verdict-count"]
        # print("lv: " + str(lv_num) + " ls : " + str(ls_num) + " u: " + str(u_num))
        if ls_num >= lv_num and ls_num >= u_num: # majority is likely spurious
            mixed_majority_data["ls"].append(spec_entry)
        elif lv_num > ls_num and lv_num > u_num: # majority is likely valid
            mixed_majority_data["lv"].append(spec_entry)
        else:                   # unknown is majority, or we can't reach a conclusion. either way we can put it in unknown
            mixed_majority_data["u"].append(spec_entry)

    return mixed_majority_data

"""
Reads each json file for a single project, and returns a map with all the inspection info.
project_data["inspection-results"] contains all the original categorizations, and
project_data["inspection-results-with-mixed"] contains categorizations where the mixed categories are
"""
def retrieve_data_for_project(project_name):
    project_dir = os.path.join(tagged_inspections_dir, project_name)
    project_data = {"name" : project_name}
    results = {"lv" : [], "ls" : [], "u" : []}
    results_with_majority_for_mixed = {"lv" : [], "ls" : [], "u" : []}
    for filename in os.listdir(project_dir):
        if not filename.endswith(".json"):
            continue
        f = open(os.path.join(project_dir, filename), "r")
        category = filename.split(".")[0]
        data = json.load(f)
        if category == "u-e":   # u-e is the same as u because we're lumping the two categories together
            results["u"] += data
            results_with_majority_for_mixed["u"] += data
        elif "-" in category:
            results[category] = data
            mixed_majority_data = majority_from_mixed(data)
            results_with_majority_for_mixed["lv"] += mixed_majority_data["lv"]
            results_with_majority_for_mixed["ls"] += mixed_majority_data["ls"]
            results_with_majority_for_mixed["u"] += mixed_majority_data["u"]
        else:
            results[category] += data
            results_with_majority_for_mixed[category] += data

    project_data.update({"inspection-results" : results, "inspection-results-with-mixed" : results_with_majority_for_mixed})
    return project_data

"""
Helper method for retrieving how many of the weak oracles were helpful/harmful/unknown.
"""
def impact_of_oracles_per_project(project, data, option):
    oracles_impact_map = {"project" : project, "num_helpful" : 0, "num_harmful" : 0, "num_unknown" : 0, "num_total" : 0, "percent_helpful" : 0, "percent_harmful" : 0, "percent_unknown" : 0}
    for category in data:
        if "-" in category:
            continue
        for spec in data[category]:
            # skip entries if the oracle was not weak
            # if ("is-oracle-weak" in spec and spec["is-oracle-weak"]) or ("WEAK_ORACLE" in spec["tags"]):
            if ("WEAK_ORACLE" in spec["tags"]):
                oracles_impact_map["num_total"] += 1
                # print(spec)
                # helpful is when the oracle is weak and manual inspection was spurious
                if spec["verdict"] == "spurious-spec":
                    oracles_impact_map["num_helpful"] += 1
                    # harmful is when the oracle is weak and manual inspection was true
                elif "true-spec" in spec["verdict"]:
                    oracles_impact_map["num_harmful"] += 1
                elif spec["verdict"] == "sometimes-true-spec" and option == "true-spec":
                    oracles_impact_map["num_harmful"] += 1
                else:
                    oracles_impact_map["num_unknown"] += 1
    if oracles_impact_map["num_total"] > 0:
        oracles_impact_map["percent_helpful"] = (oracles_impact_map["num_helpful"] / oracles_impact_map["num_total"]) * 100
        oracles_impact_map["percent_harmful"] = (oracles_impact_map["num_harmful"] / oracles_impact_map["num_total"]) * 100
        oracles_impact_map["percent_unknown"] = (oracles_impact_map["num_unknown"] / oracles_impact_map["num_total"]) * 100
    return oracles_impact_map

"""
For every spec for which the validating test had a weak oracle, did the weak oracle help?
project_data["inspection-results"] contains all the original categorizations, and
project_data["inspection-results-with-mixed"] contains categorizations where the mixed categories are
"""
def impact_of_oracles(all_data):
    keys = ["project", "num_helpful", "num_harmful", "num_unknown", "num_total", "percent_helpful", "percent_harmful", "percent_unknown"]
    # TODO: prepend headers when we output csv
    headers = {"project" : "project", "num_helpful" : "helpful", "num_harmful" : "harmful", "num_unknown" : "unknown", "num_total" : "total", "percent_helpful" : "helpful (%)", "percent_harmful" : "harmful (%)", "percent_unknown" : "unknown (%)"}
    oracles_impact_map_default = [headers]
    oracles_impact_map_with_mixed = [headers]
    oracles_impact_map_default_stsu = [headers]
    oracles_impact_map_with_mixed_stsu = [headers]
    for project_data in all_data:
        name = project_data["name"]
        oracles_impact_map_default.append(impact_of_oracles_per_project(name, project_data["inspection-results"], "true-spec"))
        oracles_impact_map_with_mixed.append(impact_of_oracles_per_project(name, project_data["inspection-results-with-mixed"], "true-spec"))
        oracles_impact_map_default_stsu.append(impact_of_oracles_per_project(name, project_data["inspection-results"], "unknown"))
        oracles_impact_map_with_mixed_stsu.append(impact_of_oracles_per_project(name, project_data["inspection-results-with-mixed"], "unknown"))

    with open(os.path.join(output_dir, "weak-oracles-omit-mixed-sometimes-is-true.csv"), "w") as default:
        writer = csv.DictWriter(default, fieldnames = keys)
        writer.writerows(oracles_impact_map_default)
    with open(os.path.join(output_dir, "weak-oracles-majority-mixed-sometimes-is-true.csv"), "w") as with_mixed:
        writer = csv.DictWriter(with_mixed, fieldnames = keys)
        writer.writerows(oracles_impact_map_with_mixed)
    with open(os.path.join(output_dir, "weak-oracles-omit-mixed-sometimes-is-unknown.csv"), "w") as default:
        writer = csv.DictWriter(default, fieldnames = keys)
        writer.writerows(oracles_impact_map_default_stsu)
    with open(os.path.join(output_dir, "weak-oracles-majority-mixed-sometimes-is-unknown.csv"), "w") as with_mixed:
        writer = csv.DictWriter(with_mixed, fieldnames = keys)
        writer.writerows(oracles_impact_map_with_mixed_stsu)

"""
Helper function for assertions_vs_exceptions().
Returns dictionary for "How often did assertions help when we had a ${category}?" for each project. Note that 
"""
def assertions_vs_exceptions_per_project(project, category_specs, ae_dicts):
    tpae_dicts = []
    total_assertions = 0
    total_exceptions = 0
    assertion_ratio_sums = 0
    num_only_assertions = 0
    for spec in category_specs:
        spec_id = spec["spec-id"]
        spec_ae_breakdown = ae_dicts[project][spec_id]
        total_assertions += spec_ae_breakdown["lv-assertions"]
        total_exceptions += spec_ae_breakdown["lv-exceptions"]
        if spec_ae_breakdown["lv-exceptions"] == 0:
            num_only_assertions += 1
        total_for_spec = spec_ae_breakdown["lv-assertions"] + spec_ae_breakdown["lv-exceptions"]
        if total_for_spec != 0:
            assertion_ratio = (spec_ae_breakdown["lv-assertions"] / total_for_spec) * 100
            assertion_ratio_sums += assertion_ratio
    if len(category_specs) != 0:
        average_assertion_ratio = assertion_ratio_sums / len(category_specs)
    else:
        average_assertion_ratio = 0
    total = total_assertions + total_exceptions
    if total != 0:
        percent_assertion  = (total_assertions / total) * 100
    else:
        percent_assertion = 0

    return { "project" : project, "total-assertions" : total_assertions , "total-exceptions" : total_exceptions, "total" : total, "percent-assertion" : percent_assertion, "average-assertion-ratio" : average_assertion_ratio, "only-assertions" : num_only_assertions}

"""
How often did assertions help when we had a ${category}? (Where category is a key in accuracy_dict; ex. true-positive, false-positive)
"""
def assertions_vs_exceptions(category, accuracy_dicts, ae_dicts):
    tpae_dicts = {}
    keys = ["project", "total-assertions", "total-exceptions", "total", "percent-assertion", "average-assertion-ratio", "only-assertions"]
    header = { "project" : "project", "total-assertions" : "assertions" , "total-exceptions" : "exceptions", "total" : "total" , "percent-assertion" : "%-assertion", "average-assertion-ratio" : "%-assertion-per-spec", "only-assertions": "only-assertions"}

    if category == "true-positive":
        shortened = "tp"
    elif category == "false-positive":
        shortened = "fp"
    elif category != "unknown-positive":
        shortened = "up"
    else:
        print(category + " category not yet supported by assertions_vs_exceptions")
        return

    for option in accuracy_dicts:
        tpae_dict_for_option = [header]
        if "csv" in option:
            continue
        for project_accuracy_dict in accuracy_dicts[option]:
            tpae_dict_for_option.append(assertions_vs_exceptions_per_project(project_accuracy_dict["project"], project_accuracy_dict[category], ae_dicts))
        tpae_dicts[option] = tpae_dict_for_option

    with open(os.path.join(output_dir, shortened + "-assertions-omit-mixed-sometimes-is-true.csv"), "w") as default:
        writer = csv.DictWriter(default, fieldnames = keys)
        writer.writerows(tpae_dicts["default-sometimes-is-true"])
    with open(os.path.join(output_dir, shortened + "-assertions-majority-mixed-sometimes-is-true.csv"), "w") as with_mixed:
        writer = csv.DictWriter(with_mixed, fieldnames = keys)
        writer.writerows(tpae_dicts["mixed-sometimes-is-true"])


"""
For each project, generates a mapping from each spec to the number of assertions + exceptions that was in each lv/u verdict for that spec.
"""
def retrieve_assertion_exception_info(project_names):
    assertion_exception_dir=os.path.join(intermediate_data_dir, "assertion-error-info")
    ae_dicts = {}
    for project in project_names:
        project_assertion_exception_dict = {}
        with open(os.path.join(assertion_exception_dir, project + "-failure-breakdowns.csv"), mode = "r") as inp:
            reader = csv.reader(inp)
            for row in reader:
                if row[0] != "id":
                    new_ae_dict = {"lv-assertions" : int(row[1]), "lv-exceptions" : int(row[2]), "u-assertions" : int(row[3]), "u-exceptions" : int(row[4]) }
                    project_assertion_exception_dict[row[0]] = new_ae_dict
        ae_dicts[project] = project_assertion_exception_dict

    return ae_dicts

"""
Input:
project - name of the project
data - the view of the data containing lv/ls/u
sometimes_true_spec_op - "true-spec" if we want to treat a sometimes-true-spec as a true spec, "unknown" otherwise
analyze_mixed - if we want to compute whether at least one of the mixed outcomes were correct
"""
def sort_into_accuracy_per_project(project, data, sometimes_true_spec_op, analyze_mixed):
    accuracy_map = {"project" : project, "true-positive" : [], "false-positive" : [], "unknown-positive" : [], "true-negative" : [], "false-negative" : [], "unknown-negative" : [], "true-unknown" : [], "false-unknown (TS)" : [], "false-unknown (NS)" : []}
    if ("lv" not in data) and (("ls" not in data) and ("u" not in data)):
        return accuracy_map
    for lv_spec_data in data["lv"]:
        # take care of SOMETIMES_TRUE_SPEC first
        if lv_spec_data["verdict"] == "sometimes-true-spec":
            if sometimes_true_spec_op == "true-spec":
                accuracy_map["true-positive"].append(lv_spec_data)
            else:
                accuracy_map["unknown-positive"].append(lv_spec_data)
        elif lv_spec_data["verdict"] == "unknown":
            continue
        elif lv_spec_data["verdict"] == "true-spec":
            accuracy_map["true-positive"].append(lv_spec_data)
        elif lv_spec_data["verdict"] == "spurious-spec":
            accuracy_map["false-positive"].append(lv_spec_data)
        elif "no-break-pass" in lv_spec_data["verdict"]:
            accuracy_map["unknown-positive"].append(lv_spec_data)
        else:
            print("lv_spec_data fall through: " + lv_spec_data["verdict"] + " proj: " + lv_spec_data["project"] + " specid: " + lv_spec_data["spec-id"])

    for ls_spec_data in data["ls"]:
        # take care of SOMETIMES_TRUE_SPEC first
        if ls_spec_data["verdict"] == "sometimes-true-spec":
            if sometimes_true_spec_op == "true-spec":
                accuracy_map["false-negative"].append(ls_spec_data)
            else:
                accuracy_map["unknown-negative"].append(ls_spec_data)
        elif ls_spec_data["verdict"] == "unknown":
            continue
        elif ls_spec_data["verdict"] == "spurious-spec":
            accuracy_map["true-negative"].append(ls_spec_data)
        elif ls_spec_data["verdict"] == "true-spec":
            accuracy_map["false-negative"].append(ls_spec_data)
        elif "no-break-pass" in ls_spec_data["verdict"]:
            accuracy_map["unknown-negative"].append(ls_spec_data)
        else:
            print("ls_spec_data fall through: " + ls_spec_data["verdict"] + " proj: " + ls_spec_data["project"] + " specid: " + ls_spec_data["spec-id"])

    for u_spec_data in data["u"]:
        # take care of SOMETIMES_TRUE_SPEC first
        if u_spec_data["verdict"] == "sometimes-true-spec":
            if sometimes_true_spec_op == "true-spec":
                accuracy_map["false-unknown (TS)"].append(u_spec_data)
            else:
                accuracy_map["true-unknown"].append(u_spec_data)
        # if it's anything besides nbp, asm error, or concurrency, DSI is wrong
        elif u_spec_data["verdict"] == "unknown":
            continue
        elif ("no-break-pass" in u_spec_data["verdict"]): #  or (("ASM_ERROR" in u_spec_data["tags"]) or ("CONCURRENCY" in u_spec_data["tags"]))
            accuracy_map["true-unknown"].append(u_spec_data)
        elif u_spec_data["verdict"] == "true-spec":
            accuracy_map["false-unknown (TS)"].append(u_spec_data)
        elif "spurious-spec" == u_spec_data["verdict"]:
            accuracy_map["false-unknown (NS)"].append(u_spec_data)
        # else:
        #     print("u_spec_data fall through: " + u_spec_data["verdict"] + " proj: " + u_spec_data["project"] + " specid: " + u_spec_data["spec-id"])

    if analyze_mixed:
        accuracy_map["mixed-correct-num"] = 0
        accuracy_map["mixed-incorrect-num"] = 0
        lv_ls_cats = [ "lv-ls" ]
        lv_u_cats = [ "lv-u", "lv-e", "lv-u-e" ]
        ls_u_cats = [ "ls-u", "ls-e", "ls-u-e" ]
        lv_ls_u_cats = [ "lv-ls-u", "lv-ls-e", "lv-ls-u-e" ]
        for cat in lv_ls_cats:
            if cat in data:
                for spec_data in data[cat]:
                    if spec_data["verdict"] == "unknown":
                        continue
                    if spec_data["verdict"] == "sometimes-true-spec":
                        if sometimes_true_spec_op == "true-spec":
                            accuracy_map["mixed-correct-num"] += 1
                        else:
                            accuracy_map["mixed-incorrect-num"] += 1
                    elif spec_data["verdict"] == "true-spec" or spec_data["verdict"] == "spurious-spec":
                        accuracy_map["mixed-correct-num"] += 1
                    elif "no-break-pass" in spec_data["verdict"]:
                        accuracy_map["mixed-incorrect-num"] += 1
                    else:
                        print(spec_data["project"] + "," + cat + "," + spec_data["verdict"])

        for cat in lv_u_cats:
            if cat in data:
                for spec_data in data[cat]:
                    if spec_data["verdict"] == "unknown":
                        continue
                    if spec_data["verdict"] == "sometimes-true-spec":
                        if sometimes_true_spec_op == "true-spec":
                            accuracy_map["mixed-correct-num"] += 1
                        else:
                            accuracy_map["mixed-incorrect-num"] += 1
                    elif spec_data["verdict"] == "true-spec" or ("no-break-pass" in u_spec_data["verdict"]): #  or (("ASM_ERROR" in u_spec_data["tags"]) or ("CONCURRENCY" in u_spec_data["tags"]))
                        accuracy_map["mixed-correct-num"] += 1
                    elif "spurious-spec" in spec_data["verdict"]:
                        accuracy_map["mixed-incorrect-num"] += 1
                    else:
                        print(spec_data["project"] + "," + cat + "," + spec_data["verdict"])

        for cat in ls_u_cats:
            if cat in data:
                for spec_data in data[cat]:
                    if spec_data["verdict"] == "unknown":
                        continue
                    if spec_data["verdict"] == "sometimes-true-spec":
                        # if sometimes_true_spec_op != "true-spec":
                        #     accuracy_map["mixed-correct-num"] += 1
                        # else:
                        accuracy_map["mixed-incorrect-num"] += 1
                    elif spec_data["verdict"] == "spurious-spec" or ("no-break-pass" in u_spec_data["verdict"]): #  or (("ASM_ERROR" in u_spec_data["tags"]) or ("CONCURRENCY" in u_spec_data["tags"]))
                        accuracy_map["mixed-correct-num"] += 1
                    elif "no-break-pass" in spec_data["verdict"]:
                        accuracy_map["mixed-incorrect-num"] += 1
                    else:
                        print(spec_data["project"] + "," + cat + "," + spec_data["verdict"])

        for cat in lv_ls_u_cats:
            if cat in data:
                for spec_data in data[cat]:
                    if spec_data["verdict"] == "unknown":
                        continue
                    if spec_data["verdict"] == "sometimes-true-spec" and sometimes_true_spec_op != "true-spec":
                        accuracy_map["mixed-incorrect-num"] += 1
                    else:
                        accuracy_map["mixed-correct-num"] += 1#len(data[cat])

    return accuracy_map

"""
Converts the given accuracy_dict (for a single project) into a
dictonary with the cardinalities of each field in the accuracy_dict.
Inputs:
- accuracy_dict: the specific accuracy dict to use ({project, true-positive, false-positive, ...})
- helper_dict: helper for if you wanted to include mixed in precision and recall calculations
"""
def make_project_accuracy_dict_for_csv(accuracy_dict):
    card_dict = {}
    card_dict["project"] = accuracy_dict["project"]
    total = 0
    for key in accuracy_dict:
        if key == "project":
            card_dict[key] = accuracy_dict[key]
        else:
            card_dict[key] = len(accuracy_dict[key])
            total += len(accuracy_dict[key])

    card_dict["total"] = total
    card_dict["total-right"] = card_dict["true-positive"] + card_dict["true-negative"] + card_dict["true-unknown"]
    card_dict["percent-right"] = divide(card_dict["total-right"], total) * 100

    # macro averaging (I think)
    card_dict["ts-precision"] = divide(card_dict["true-positive"], (card_dict["true-positive"] + card_dict["false-positive"] + card_dict["unknown-positive"])) * 100
    card_dict["ts-recall"] = divide(card_dict["true-positive"], (card_dict["true-positive"] + card_dict["false-negative"] + card_dict["false-unknown (TS)"])) * 100
    card_dict["ns-precision"] = divide(card_dict["true-negative"], (card_dict["true-negative"] + card_dict["false-negative"] + card_dict["unknown-negative"])) * 100
    card_dict["ns-recall"] = divide(card_dict["true-negative"], (card_dict["true-negative"] + card_dict["false-positive"] + card_dict["false-unknown (NS)"])) * 100

    return card_dict

def make_project_accuracy_dict_for_csv_option_4(accuracy_csv_dict, dict_with_mixed_correctness):
    card_dict = copy.deepcopy(accuracy_csv_dict)
    card_dict["mixed-correct-num"] = dict_with_mixed_correctness["mixed-correct-num"]
    card_dict["mixed-incorrect-num"] = dict_with_mixed_correctness["mixed-incorrect-num"]
    card_dict["total"] += dict_with_mixed_correctness["mixed-correct-num"] + dict_with_mixed_correctness["mixed-incorrect-num"]
    card_dict["total-right"] += dict_with_mixed_correctness["mixed-correct-num"]
    card_dict["percent-right"] = divide(card_dict["total-right"], card_dict["total"]) * 100

    return card_dict

"""
Option 1: treat all mixed as wrong in the percent-right calculation.
The only differences between option 1 and 3 are total and percent-right.
"""
def make_mixed_wrong_csv_dict(accuracy_dict_for_csv, project_inspection_data, helper_dict, sts_true):
    num_mixed = 0
    mixed = [ "lv-ls" , "lv-u", "lv-e", "ls-u", "ls-e", "u-e", "lv-ls-u", "lv-ls-e", "lv-u-e", "ls-u-e", "lv-ls-u-e" ]
    option_1_csv_dict = copy.deepcopy(accuracy_dict_for_csv)
    for category in project_inspection_data:
        if category in mixed:
            for spec_data in project_inspection_data[category]:
                if spec_data["verdict"] != "unknown":
                    num_mixed += 1


    option_1_csv_dict["total"] += num_mixed
    option_1_csv_dict["percent-right"] = divide(option_1_csv_dict["total-right"], option_1_csv_dict["total"]) * 100

    option_1_csv_dict["additional-true-spec"] = helper_dict[option_1_csv_dict["project"]]["true-spec"]
    option_1_csv_dict["additional-spurious-spec"] = helper_dict[option_1_csv_dict["project"]]["spurious-spec"]
    if sts_true:
        option_1_csv_dict["additional-true-spec"] += helper_dict[option_1_csv_dict["project"]]["sometimes-true-spec"]
    option_1_csv_dict["additional-nbp"] = helper_dict[option_1_csv_dict["project"]]["nbp"]

    # option_1_csv_dict["ts-precision"] = divide(option_1_csv_dict["true-positive"], (option_1_csv_dict["true-positive"] + option_1_csv_dict["false-positive"] + option_1_csv_dict["unknown-positive"])) * 100
    # option_1_csv_dict["ts-recall"] = divide(option_1_csv_dict["true-positive"], (option_1_csv_dict["true-positive"] + option_1_csv_dict["false-negative"] + option_1_csv_dict["false-unknown (TS)"] + option_1_csv_dict["additional-true-spec"])) * 100
    # option_1_csv_dict["ns-precision"] = divide(option_1_csv_dict["true-negative"], (option_1_csv_dict["true-negative"] + option_1_csv_dict["false-negative"] + option_1_csv_dict["unknown-negative"])) * 100
    # option_1_csv_dict["ns-recall"] = divide(option_1_csv_dict["true-negative"], (option_1_csv_dict["true-negative"] + option_1_csv_dict["false-positive"] + option_1_csv_dict["false-unknown (NS)"] + option_1_csv_dict["additional-spurious-spec"])) * 100
    option_1_csv_dict["ts-precision"] = divide(option_1_csv_dict["true-positive"], (option_1_csv_dict["true-positive"] + option_1_csv_dict["false-positive"] + option_1_csv_dict["unknown-positive"])) * 100
    option_1_csv_dict["ts-recall"] = divide(option_1_csv_dict["true-positive"], (option_1_csv_dict["true-positive"] + option_1_csv_dict["false-negative"] + option_1_csv_dict["false-unknown (TS)"])) * 100
    option_1_csv_dict["ns-precision"] = divide(option_1_csv_dict["true-negative"], (option_1_csv_dict["true-negative"] + option_1_csv_dict["false-negative"] + option_1_csv_dict["unknown-negative"])) * 100
    option_1_csv_dict["ns-recall"] = divide(option_1_csv_dict["true-negative"], (option_1_csv_dict["true-negative"] + option_1_csv_dict["false-positive"] + option_1_csv_dict["false-unknown (NS)"])) * 100

    return option_1_csv_dict

"""
Sorts each inspection based on the DSI outcome vs the manual inspection outcome.
"""
def sort_into_accuracy(all_data, helper_dict):
    accuracy_map_default = []
    accuracy_map_default_stsu = [] # sometimes true spec is considered as unknown
    accuracy_map_with_mixed = []
    accuracy_map_with_mixed_stsu = [] # sometimes true spec is considered as unknown
    csv_header = {"project" : "project", "true-positive" : "TP", "false-positive" : "FP", "unknown-positive" : "UP", "true-negative" : "TN", "false-negative" : "FN", "unknown-negative" : "UN", "true-unknown" : "TU", "false-unknown (TS)" : "FU (TS)", "false-unknown (NS)" : "FU (NS)", "ts-precision" : "ts_precision", "ts-recall" : "ts_recall", "ns-precision" : "ns_precision", "ns-recall" : "ns_recall", "total-right" : "total-right", "total" : "total", "percent-right" : "% right"}
    mixed_wrong_csv_header = {"project" : "project", "true-positive" : "TP", "false-positive" : "FP", "unknown-positive" : "UP", "true-negative" : "TN", "false-negative" : "FN", "unknown-negative" : "UN", "true-unknown" : "TU", "false-unknown (TS)" : "FU (TS)", "false-unknown (NS)" : "FU (NS)", "ts-precision" : "ts_precision", "ts-recall" : "ts_recall", "ns-precision" : "ns_precision", "ns-recall" : "ns_recall", "total-right" : "total-right", "total" : "total", "percent-right" : "% right", "additional-true-spec" : "additional-true-spec", "additional-spurious-spec" : "additional-spurious-spec", "additional-nbp" : "additional-nbp"}
    opt_4_csv_header = {"project" : "project", "true-positive" : "TP", "false-positive" : "FP", "unknown-positive" : "UP", "true-negative" : "TN", "false-negative" : "FN", "unknown-negative" : "UN", "true-unknown" : "TU", "false-unknown (TS)" : "FU (TS)", "false-unknown (NS)" : "FU (NS)", "mixed-correct-num" : "mixed-correct", "mixed-incorrect-num" : "mixed-incorrect", "ts-precision" : "ts_precision", "ts-recall" : "ts_recall", "ns-precision" : "ns_precision", "ns-recall" : "ns_recall", "total-right" : "total-right", "total" : "total", "percent-right" : "% right", "additional-true-spec" : "additional-true-spec", "additional-spurious-spec" : "additional-spurious-spec"}
    default_for_csv = [csv_header]
    default_for_csv_stsu = [csv_header] # sometimes true spec is considered as unknown
    option_4_for_csv = [opt_4_csv_header]
    option_4_for_csv_stsu = [opt_4_csv_header] # sometimes true spec is considered as unknown
    mixed_wrong_for_csv = [mixed_wrong_csv_header]
    mixed_wrong_for_csv_stsu = [mixed_wrong_csv_header]
    with_mixed_for_csv = [csv_header]
    with_mixed_for_csv_stsu = [csv_header] # sometimes true spec is considered as unknown
    for project_data in all_data:
        name = project_data["name"]
        default_res = sort_into_accuracy_per_project(name, project_data["inspection-results"], "true-spec", False)
        default_res_stsu = sort_into_accuracy_per_project(name, project_data["inspection-results"], "unknown", False)
        with_mixed_res = sort_into_accuracy_per_project(name, project_data["inspection-results-with-mixed"], "true-spec", False)
        with_mixed_res_stsu = sort_into_accuracy_per_project(name, project_data["inspection-results-with-mixed"], "unknown", False)
        option_4_res = sort_into_accuracy_per_project(name, project_data["inspection-results"], "true-spec", True)
        option_4_res_stsu = sort_into_accuracy_per_project(name, project_data["inspection-results"], "unknown", True)
        accuracy_map_default.append(default_res)
        accuracy_map_with_mixed.append(with_mixed_res)
        accuracy_map_default_stsu.append(default_res_stsu)
        accuracy_map_with_mixed_stsu.append(with_mixed_res_stsu)
        # make csv for the default (omit mixed). Use this to generate Option 1 csv (mixed is wrong)
        accuracy_map_csv = make_project_accuracy_dict_for_csv(default_res)
        default_for_csv.append(accuracy_map_csv)
        mixed_wrong_curr_csv = make_mixed_wrong_csv_dict(accuracy_map_csv, project_data["inspection-results"], helper_dict, True)
        mixed_wrong_for_csv.append(mixed_wrong_curr_csv)
        option_4_for_csv.append(make_project_accuracy_dict_for_csv_option_4(mixed_wrong_curr_csv, option_4_res))
        # make csv for the default stsu option (omit mixed, sometimes true spec is unknown). Use this to generate Option 1 stsu csv (mixed is wrong, sometimes true spec is unknown)
        accuracy_map_csv_stsu = make_project_accuracy_dict_for_csv(default_res_stsu)
        default_for_csv_stsu.append(accuracy_map_csv_stsu)
        mixed_wrong_curr_csv_stsu = make_mixed_wrong_csv_dict(accuracy_map_csv_stsu, project_data["inspection-results"], helper_dict, False)
        mixed_wrong_for_csv_stsu.append(mixed_wrong_curr_csv_stsu)
        option_4_for_csv_stsu.append(make_project_accuracy_dict_for_csv_option_4(mixed_wrong_curr_csv_stsu, option_4_res_stsu))
        # simply create and append for mixed majority (Option 2)
        with_mixed_for_csv.append(make_project_accuracy_dict_for_csv(with_mixed_res))
        with_mixed_for_csv_stsu.append(make_project_accuracy_dict_for_csv(with_mixed_res_stsu))


    return {"default-sometimes-is-true" : accuracy_map_default, "default-sometimes-is-unknown" : accuracy_map_default_stsu, "mixed-sometimes-is-true" : accuracy_map_with_mixed, "mixed-sometimes-is-unknown" : accuracy_map_with_mixed_stsu, "default-sometimes-is-true (csv)" : default_for_csv, "default-sometimes-is-unknown (csv)" : default_for_csv_stsu, "mixed-sometimes-is-true (csv)" : with_mixed_for_csv, "mixed-sometimes-is-unknown (csv)" : with_mixed_for_csv_stsu, "mixed-wrong-sometimes-is-true (csv)" : mixed_wrong_for_csv, "mixed-wrong-sometimes-is-unknown (csv)" : mixed_wrong_for_csv_stsu, "opt-4-sometimes-is-true (csv)" : option_4_for_csv, "opt-4-sometimes-is-unknown (csv)" : option_4_for_csv_stsu}

"""
Outputs the csv accuracy_maps (containing cardinalities of each field per project)
to csv files respectively.
"""
def output_accuracy_maps_csv(accuracy_maps):
    keys = ["project", "true-positive", "false-positive", "unknown-positive", "true-negative", "false-negative", "unknown-negative", "true-unknown", "false-unknown (TS)", "false-unknown (NS)", "ts-precision", "ts-recall", "ns-precision", "ns-recall", "total-right", "total", "percent-right"]
    opt_1_keys = ["project", "true-positive", "false-positive", "unknown-positive", "true-negative", "false-negative", "unknown-negative", "true-unknown", "false-unknown (TS)", "false-unknown (NS)", "ts-precision", "ts-recall", "ns-precision", "ns-recall", "total-right", "total", "percent-right", "additional-true-spec", "additional-spurious-spec", "additional-nbp"]
    opt_4_keys = ["project", "true-positive", "false-positive", "unknown-positive", "true-negative", "false-negative", "unknown-negative", "true-unknown", "false-unknown (TS)", "false-unknown (NS)", "mixed-correct-num", "mixed-incorrect-num", "ts-precision", "ts-recall", "ns-precision", "ns-recall", "total-right", "total", "percent-right", "additional-true-spec", "additional-spurious-spec"]
    with open(os.path.join(output_dir, "accuracy-omit-mixed-sometimes-is-true.csv"), "w") as default:
        writer = csv.DictWriter(default, fieldnames = keys)
        writer.writerows(accuracy_maps["default-sometimes-is-true (csv)"])
    with open(os.path.join(output_dir, "accuracy-omit-mixed-sometimes-is-unknown.csv"), "w") as default_stsu:
        writer = csv.DictWriter(default_stsu, fieldnames = keys)
        writer.writerows(accuracy_maps["default-sometimes-is-unknown (csv)"])
    with open(os.path.join(output_dir, "accuracy-majority-mixed-sometimes-is-true.csv"), "w") as with_mixed:
        writer = csv.DictWriter(with_mixed, fieldnames = keys)
        writer.writerows(accuracy_maps["mixed-sometimes-is-true (csv)"])
    with open(os.path.join(output_dir, "accuracy-majority-mixed-sometimes-is-unknown.csv"), "w") as with_mixed_stsu:
        writer = csv.DictWriter(with_mixed_stsu, fieldnames = keys)
        writer.writerows(accuracy_maps["mixed-sometimes-is-unknown (csv)"])
    with open(os.path.join(output_dir, "accuracy-mixed-wrong-sometimes-is-true.csv"), "w") as mixed_wrong:
        writer = csv.DictWriter(mixed_wrong, fieldnames = opt_1_keys)
        writer.writerows(accuracy_maps["mixed-wrong-sometimes-is-true (csv)"])
    with open(os.path.join(output_dir, "accuracy-mixed-wrong-sometimes-is-unknown.csv"), "w") as mixed_wrong_stsu:
        writer = csv.DictWriter(mixed_wrong_stsu, fieldnames = opt_1_keys)
        writer.writerows(accuracy_maps["mixed-wrong-sometimes-is-unknown (csv)"])
        # TODO: bring below back at 4pm
    # with open(os.path.join(output_dir, "accuracy-opt-4-sometimes-is-true.csv"), "w") as out:
    #     writer = csv.DictWriter(out, fieldnames = opt_4_keys)
    #     writer.writerows(accuracy_maps["opt-4-sometimes-is-true (csv)"])
    # with open(os.path.join(output_dir, "accuracy-opt-4-sometimes-is-unknown.csv"), "w") as out:
    #     writer = csv.DictWriter(out, fieldnames = opt_4_keys)
    #     writer.writerows(accuracy_maps["opt-4-sometimes-is-unknown (csv)"])

def tag_counter_per_category(tag, accuracy_maps):
    csv_header = {"project" : "project", "true-positive" : "TP", "false-positive" : "FP", "unknown-positive" : "UP", "true-negative" : "TN", "false-negative" : "FN", "unknown-negative" : "UN", "true-unknown" : "TU", "false-unknown (TS)" : "FU (TS)", "false-unknown (NS)" : "FU (NS)", "total" : "total"}
    counters_for_tag = [csv_header]
    for accuracy_map in accuracy_maps:
        total = 0
        tag_counter = {}
        for category in accuracy_map:
            if category == "project":
                tag_counter[category] = accuracy_map[category]
            else:
                tag_counter[category] = 0
                for spec in accuracy_map[category]:
                    if (tag + ",") in spec["tags"] or spec["tags"].endswith(tag):
                        tag_counter[category] += 1
                        total += 1
        tag_counter["total"] = total
        counters_for_tag.append(tag_counter)
    return counters_for_tag

"""
For outcome-category, { option-for-mixed-and-unknowns --> {  # of times tag appearred in the outcome-category in each project } }
"""
def outcome_category_to_tag_map(global_tag_counter, project_list, category):
    csv_header = {project : project for project in project_list}
    csv_header["tags"] = "tags"
    out_dict = {}

    for tag in global_tag_counter:
        category_to_tag_map = {"tag" : tag}
        for option_for_mu in global_tag_counter[tag]:
            if option_for_mu not in out_dict:
                out_dict[option_for_mu] = [csv_header]
            for project_dict in global_tag_counter[tag][option_for_mu]:
                if (project_dict["project"] == "project"):
                    continue
                category_to_tag_map[project_dict["project"]] = project_dict[category]
            out_dict[option_for_mu].append(category_to_tag_map)
    return out_dict

"""
Returns a dictionary
tag --> { option-for-mixed-and-unknowns --> { outcome_category --> # of times tag appearred in the outcome_category}}
"""
def tag_counter(tags, accuracy_dicts):
    all_tag_counters = {}
    for tag in tags:
        tag_counter_for_options = {} # key here is options
        for option in accuracy_dicts:
            if "csv" in option:
                continue
            tag_counter_for_options[option] = tag_counter_per_category(tag, accuracy_dicts[option])

        all_tag_counters[tag] = tag_counter_for_options

    return all_tag_counters

"""
complicated_tag_counter : tag --> { option-for-mixed-and-unknowns --> { outcome_category --> # of times tag appearred in the outcome_category}}
"""
def global_tag_counter_per_category(complicated_tag_counter, option, tid_map, man_spu_map):
    keys = ["category", "tag", "true-positive", "false-positive", "unknown-positive", "true-negative", "false-negative", "unknown-negative", "true-unknown", "false-unknown (TS)", "false-unknown (NS)", "percent-non-true", "percent-spurious", "total", "percent-man-spurious", "tag_name"]
    csv_header = {"category" : "category", "tag" : "tag", "true-positive" : "TP", "false-positive" : "FP", "unknown-positive" : "UP", "true-negative" : "TN", "false-negative" : "FN", "unknown-negative" : "UN", "true-unknown" : "TU", "false-unknown (TS)" : "FU (TS)", "false-unknown (NS)" : "FU (NS)", "percent-non-true": "% not true", "percent-spurious": "% spurious", "percent-man-spurious" : "% manSpurious", "total" : "total", "tag_name" : "tag_name"}
    out_list = []
    for tag_cat in tag_cat_dict_cat_order:
        cat_list = []
        for tag in tag_cat_dict[tag_cat]:
            if tag == "Tag":
                continue
            tag_out = {"category": tag_cat, "tag" : tid_map[tag], "true-positive" : 0, "false-positive" : 0, "unknown-positive" : 0, "true-negative" : 0, "false-negative" : 0, "unknown-negative" : 0, "true-unknown" : 0, "false-unknown (TS)" : 0, "false-unknown (NS)" : 0, "total" : 0}
            if not tag in complicated_tag_counter:
                continue
            for project_tag_counter in complicated_tag_counter[tag][option]:
                if not project_tag_counter["project"] == "project":
                    for category in project_tag_counter:
                        if not category == "project":
                            tag_out[category] += project_tag_counter[category]
            non_true_sum = tag_out["false-positive"] + tag_out["unknown-positive"] + tag_out["true-negative"] + tag_out["unknown-negative"] + tag_out["false-unknown (NS)"]
            tag_out["percent-non-true"] = divide(non_true_sum, (tag_out["total"] - tag_out["true-unknown"])) * 100
            tag_out["percent-spurious"] = divide((tag_out["false-positive"]+ tag_out["true-negative"] + tag_out["false-unknown (NS)"]), (tag_out["total"] - (tag_out["true-unknown"] + tag_out["unknown-positive"] + tag_out["unknown-negative"]))) * 100
            tag_out["percent-man-spurious"] = man_spu_map[tag]
            tag_out["tag_name"] = tag
            cat_list.append(tag_out)
        cat_list = sorted(cat_list, key=lambda d: d['total'], reverse = True)
        out_list += cat_list
    out_list.insert(0, csv_header)
    output_to_csv(keys, out_list, output_dir, "global-tags-per-category-" + option + ".csv")

def output_tag_counter_to_csv(tag, tag_counters):
    tag_output_dir = os.path.join(output_dir, "tag-counters")
    if not (os.path.isdir(tag_output_dir)):
        os.mkdir(tag_output_dir)
    keys = ["project", "true-positive", "false-positive", "unknown-positive", "true-negative", "false-negative", "unknown-negative", "true-unknown", "false-unknown (TS)", "false-unknown (NS)", "total"]
    option_map = {"default-sometimes-is-true" : "omit-mixed-sometimes-is-true", "default-sometimes-is-unknown" : "omit-mixed-sometimes-is-unknown", "mixed-sometimes-is-true" : "majority-mixed-sometimes-is-true", "mixed-sometimes-is-unknown" : "majority-mixed-sometimes-is-unknown"}
    if tag not in tag_counters:
        return
    for option in tag_counters[tag]:
        with open(os.path.join(output_dir, tag + "@" + option_map[option] + ".csv"), "w") as out:
            writer = csv.DictWriter(out, fieldnames = keys)
            writer.writerows(tag_counters[tag][option])

def global_outcomes_helper(dsi_outcome_dict, dsi_outcome_count_dict, dsi_outcome_count_dict_with_mixed_breakdown, dict_name, project_data):
    if dict_name in project_data["inspection-results"]:
        for spec_data in project_data["inspection-results"][dict_name]:
            if bool(dsi_outcome_count_dict_with_mixed_breakdown):
                dsi_outcome_count_dict_with_mixed_breakdown["total"] += 1
            dsi_outcome_count_dict["total"] += 1
            if spec_data["verdict"] in dsi_outcome_dict: # true-spec, spurious-spec, sometimes-true-spec are in verdict
                dsi_outcome_dict[spec_data["verdict"]] += spec_data
                dsi_outcome_count_dict[spec_data["verdict"]] += 1
                if bool(dsi_outcome_count_dict_with_mixed_breakdown):
                    dsi_outcome_count_dict_with_mixed_breakdown[spec_data["verdict"]] += 1
            else:               # otherwise, it's nbp. put in nbp category...
                dsi_outcome_dict["nbp"] += spec_data
                dsi_outcome_count_dict["nbp"] += 1
                if bool(dsi_outcome_count_dict_with_mixed_breakdown):
                    dsi_outcome_count_dict_with_mixed_breakdown["nbp"] += 1

    return (dsi_outcome_dict, dsi_outcome_count_dict, dsi_outcome_count_dict_with_mixed_breakdown)

def global_mixed_outcomes_helper(global_mm_dict, dict_name, project_data):
    if dict_name in project_data["inspection-results"]:
        mm_breakdown_dict = majority_from_mixed(project_data["inspection-results"][dict_name])
        global_mm_dict["lv"] += len(mm_breakdown_dict["lv"])
        global_mm_dict["ls"] += len(mm_breakdown_dict["ls"])
        global_mm_dict["u"] += len(mm_breakdown_dict["u"])
        global_mm_dict["total"] += len(mm_breakdown_dict["lv"]) + len(mm_breakdown_dict["ls"]) + len(mm_breakdown_dict["u"])

    return global_mm_dict

def per_project_manual_breakdown_of_mixed(all_data):
    man_breakdown = {}
    keys = ["true-spec", "spurious-spec", "unknown", "nbp", "sometimes-true-spec", "total"]
    for project_data in all_data:
        name = project_data["name"]
        d = {key : 0 for key in keys}
        for dsi_cat in project_data["inspection-results"]:
            if not "-" in dsi_cat:
                continue
            # print(name + "," + dsi_cat + "," + str(len(project_data["inspection-results"][dsi_cat])))
            for spec_data in project_data["inspection-results"][dsi_cat]:
                d["total"] += 1
                if spec_data["verdict"] in keys:
                    d[spec_data["verdict"]] += 1
                else:
                    d["nbp"] += 1
        man_breakdown[name] = d
    # print(man_breakdown)
    return man_breakdown

def dsiPlus_global_outcomes(all_data):
    keys = ["dsi-outcome", "true-spec", "spurious-spec", "nbp", "unknown", "sometimes-true-spec", "total"]
    dsiPlus_lv = {"dsi-outcome" : "LV", "true-spec" : 0, "spurious-spec": 0, "unknown": 0, "nbp" : 0, "sometimes-true-spec" : 0, "total" : 0}
    dsiPlus_ls = {"dsi-outcome" : "LS", "true-spec" : 0, "spurious-spec": 0, "unknown": 0, "nbp" : 0, "sometimes-true-spec" : 0, "total" : 0}
    dsiPlus_u = {"dsi-outcome" : "U", "true-spec" : 0, "spurious-spec": 0, "unknown": 0, "nbp" : 0, "sometimes-true-spec" : 0, "total" : 0}
    header = { key : key for key in keys }
    for project_data in all_data:
        for cat in project_data['inspection-results']:
            for spec_data in project_data['inspection-results'][cat]:
                if spec_data["dsiPlus-outcome"] == "true":
                    dsiPlus_dict = dsiPlus_lv
                elif spec_data["dsiPlus-outcome"] == "spurious":
                    dsiPlus_dict = dsiPlus_ls
                elif spec_data["dsiPlus-outcome"] == "unknown":
                    dsiPlus_dict = dsiPlus_u
                dsiPlus_dict["total"] += 1
                if spec_data["verdict"] in dsiPlus_lv:
                    dsiPlus_dict[spec_data["verdict"]] += 1
                else:
                    dsiPlus_dict["nbp"] += 1
    total_row = {}
    for key in keys:
        if key == "dsi-outcome":
            total_row[key] = "total"
        else:
            total_row[key] = dsiPlus_lv[key] + dsiPlus_ls[key] + dsiPlus_u[key]

    output_to_csv(keys, [header, dsiPlus_lv, dsiPlus_ls, dsiPlus_u, total_row], output_dir, "global-outcomes-dsiPlus" + ".csv")


def global_outcomes(all_data):
    keys = ["dsi-outcome", "true-spec", "spurious-spec", "unknown", "nbp", "sometimes-true-spec", "total"]
    header = { key : key for key in keys }
    lv = {key : [] for key in keys}
    lv_counts = {key : 0 for key in keys}
    lv["dsi-outcome"] = "likely-valid"
    lv_counts["dsi-outcome"] = "likely-valid"
    ls_counts = copy.deepcopy(lv_counts)
    ls = copy.deepcopy(lv)
    ls["dsi-outcome"] = "likely-spurious"
    ls_counts["dsi-outcome"] = "likely-spurious"
    u = copy.deepcopy(lv)
    u["dsi-outcome"] = "unknown"
    u_counts = copy.deepcopy(lv_counts)
    u_counts["dsi-outcome"] = "unknown"
    mixed = copy.deepcopy(lv)
    mixed["dsi-outcome"] = "mixed"
    mixed_counts = copy.deepcopy(lv_counts)
    mixed_counts["dsi-outcome"] = "mixed"
    lv_ls_cats = [ "lv-ls" ]
    lv_u_cats = [ "lv-u", "lv-e", "lv-u-e" ]
    ls_u_cats = [ "ls-u", "ls-e", "ls-u-e" ]
    lv_ls_u_cats = [ "lv-ls-u", "lv-ls-e", "lv-ls-u-e" ]
    lv_ls_counts = copy.deepcopy(lv_counts)
    lv_ls_counts["dsi-outcome"] = "LV-LS"
    lv_u_counts = copy.deepcopy(lv_counts)
    lv_u_counts["dsi-outcome"] = "LV-U"
    ls_u_counts = copy.deepcopy(lv_counts)
    ls_u_counts["dsi-outcome"] = "LS-U"
    lv_ls_u_counts = copy.deepcopy(lv_counts)
    lv_ls_u_counts["dsi-outcome"] = "LV-LS-U"
    lv_ls_mm_counts={"dsi-outcome" : "LV-LS", "lv" : 0 , "ls" : 0, "u" : 0, "total" : 0}              # mixed majority
    lv_u_mm_counts={"dsi-outcome" : "LV-U", "lv" : 0 , "ls" : 0, "u" : 0, "total" : 0}              # mixed majority
    ls_u_mm_counts={"dsi-outcome" : "LS-U", "lv" : 0 , "ls" : 0, "u" : 0, "total" : 0}              # mixed majority
    lv_ls_u_mm_counts={"dsi-outcome" : "LV-LS-U", "lv" : 0 , "ls" : 0, "u" : 0, "total" : 0}              # mixed majority
    for project_data in all_data:
        name = project_data["name"]
        lv, lv_counts, _ = global_outcomes_helper(lv, lv_counts, {}, "lv", project_data)
        ls, ls_counts, _ = global_outcomes_helper(ls, ls_counts, {}, "ls", project_data)
        u, u_counts, _ = global_outcomes_helper(u, u_counts, {}, "u", project_data)
        u, u_counts, _ = global_outcomes_helper(u, u_counts, {}, "u-e", project_data) # treat u-e as u because we're lumping u and e together
        for cat in lv_ls_cats:
            mixed, mixed_counts, lv_ls_counts = global_outcomes_helper(mixed, mixed_counts, lv_ls_counts, cat, project_data)
            lv_ls_mm_counts = global_mixed_outcomes_helper(lv_ls_mm_counts, cat, project_data)
        for cat in lv_u_cats:
            mixed, mixed_counts, lv_u_counts = global_outcomes_helper(mixed, mixed_counts, lv_u_counts, cat, project_data)
            lv_u_mm_counts = global_mixed_outcomes_helper(lv_u_mm_counts, cat, project_data)
        for cat in ls_u_cats:
            mixed, mixed_counts, ls_u_counts = global_outcomes_helper(mixed, mixed_counts, ls_u_counts, cat, project_data)
            ls_u_mm_counts = global_mixed_outcomes_helper(ls_u_mm_counts, cat, project_data)
        for cat in lv_ls_u_cats:
            mixed, mixed_counts, lv_ls_u_counts = global_outcomes_helper(mixed, mixed_counts, lv_ls_u_counts, cat, project_data)
            lv_ls_u_mm_counts = global_mixed_outcomes_helper(lv_ls_u_mm_counts, cat, project_data)

    # keys = ["dsi-outcome", "true-spec", "spurious-spec", "unknown", "nbp", "sometimes-true-spec", "total"]
    lv_ls_counts["correct"] = lv_ls_counts["true-spec"] + lv_ls_counts["spurious-spec"] + lv_ls_counts["sometimes-true-spec"]
    lv_ls_counts["% correct"] = divide(lv_ls_counts["correct"], lv_ls_counts["total"]) * 100
    lv_u_counts["correct"] = lv_u_counts["true-spec"] + lv_u_counts["nbp"] + lv_u_counts["sometimes-true-spec"]
    lv_u_counts["% correct"] = divide(lv_u_counts["correct"], lv_u_counts["total"]) * 100
    ls_u_counts["correct"] = ls_u_counts["spurious-spec"] + ls_u_counts["nbp"]
    ls_u_counts["% correct"] = divide(ls_u_counts["correct"], ls_u_counts["total"]) * 100
    lv_ls_u_counts["correct"] = lv_ls_u_counts["true-spec"] + lv_ls_u_counts["spurious-spec"] + lv_ls_u_counts["nbp"] + lv_ls_u_counts["sometimes-true-spec"]
    lv_ls_u_counts["% correct"] = divide(lv_ls_u_counts["correct"], lv_ls_u_counts["total"]) * 100

    total_row = {}
    total_row_with_mixed_breakdown={}
    total_row_mm={}
    for key in keys:
        if key == "dsi-outcome":
            total_row[key] = "total"
            total_row_with_mixed_breakdown[key] = "total"
        else:
            total_row[key] = lv_counts[key] + ls_counts[key] + u_counts[key] + mixed_counts[key]
            total_row_with_mixed_breakdown[key] = lv_ls_counts[key] + lv_u_counts[key] + ls_u_counts[key] + lv_ls_u_counts[key]
    total_row_with_mixed_breakdown["correct"] = lv_ls_counts["correct"] + lv_u_counts["correct"] + ls_u_counts["correct"] + lv_ls_u_counts["correct"]
    total_row_with_mixed_breakdown["% correct"] = divide(total_row_with_mixed_breakdown["correct"], total_row_with_mixed_breakdown["total"]) * 100

    mm_header = {"dsi-outcome" : "Majority Verdict", "lv" : "Likely Valid", "ls" : "Likely Spurious", "u" : "Unknown", "total" : "total"}
    mm_keys = ["dsi-outcome", "lv", "ls", "u", "total"]
    for key in mm_keys:
        if key == "dsi-outcome":
            total_row_mm[key] = "total"
        else:
            total_row_mm[key] = lv_ls_mm_counts[key] + lv_u_mm_counts[key] + ls_u_mm_counts[key] + lv_ls_u_mm_counts[key]
    output_to_csv(mm_keys, [mm_header, lv_ls_mm_counts, lv_u_mm_counts, ls_u_mm_counts, lv_ls_u_mm_counts, total_row_mm], output_dir, "global-mixed-dsi-majorities.csv")

    return {"global-outcomes" : [lv, ls, u, mixed] , "global-outcomes-count" : [ header, lv_counts, ls_counts, u_counts, mixed_counts, total_row ], "global-outcomes-count-with-mixed-breakdown" : [ header, lv_ls_counts, lv_u_counts, ls_u_counts, lv_ls_u_counts, total_row_with_mixed_breakdown ]}

def output_global_outcomes_to_csv(global_outcomes):
    keys = ["dsi-outcome", "true-spec", "spurious-spec", "nbp", "unknown", "sometimes-true-spec", "total"]
    mixed_keys = ["dsi-outcome", "true-spec", "spurious-spec", "nbp", "unknown", "sometimes-true-spec", "correct", "total", "% correct"]
    output_to_csv(keys, global_outcomes["global-outcomes-count"], output_dir, "global-outcomes.csv")
    output_to_csv(mixed_keys, global_outcomes["global-outcomes-count-with-mixed-breakdown"], output_dir, "global-outcomes-with-mixed-breakdown.csv")

def global_tags(tags, all_data):
    keys = ["cat", "tagID", "tag"]
    csv_header = {"cat" : "category", "tagID" : "tagID", "tag" : "tag", "total" : "total"}
    out_list = []
    tag_id_map = {}
    tag_id_acc = 1
    tag_shortname_map = {"METHOD_A_RETURNS_VOID": "A_VOID", "REPLACE_RETURN_WITH_UNEXPECTED_OUTPUT" : "UNEXPECTED", "NULL_REPLACEMENT_CAUSED_NULLPOINTEREXCEPTION" : "NULLPTR", "REPLACE_RETURN_WITH_CORRECT_VALUE_NONASSERTION" : "C_NONASSERT", "REPLACE_RETURN_WITH_EXPECTED_OUTPUT" : "EXPECTED", "DEFAULT_VALUE_SAME_AS_RETURN" : "DEFAULT_EQUAL", "RETURN_VALUE_DISCARDED" : "DISCARDED", "REPLACEMENT_TRIGGERS_CHECK_THAT_PREVENTS_B" : "PREVENTS_B", "LOSSY_REPLACEMENT" : "LOSSY", "UNRELATED_STATEFUL_METHODS" : "UNRELATED", "ONE_STATELESS_METHOD" : "SINGLE_STATELESS", "ONE_PURE_SETTER" : "SINGLE_SETTER", "CONNECTION_DOES_NOT_NECESSITATE_ORDERING" : "REL_NONORDERING", "UNRELATED_STATELESS_METHODS" : "UNRELATED_STATELES", "UNRELATED_PURE_SETTERS" : "UNRELATED_SETTERS", "TRUE_SPEC_WITH_CALLER" : "TS_WITH_CALLER", "MODIFIED_STATE_DOES_NOT_INTERSECT" : "STATE_DISJOINT", "EXPECTED_EXCEPTION" : "EE", "EXPECTED_EXCEPTION_NOT_THROWN" : "EE_NOT_THROWN", "SWALLOWED_EXCEPTION" : "SWALLOWED", "IMPLICITLY_EXPECTED_EXCEPTION" : "IMPLICIT_EE", "DELAY_OF_A_CAUSES_UNEXPECTED_EXCEPTION" : "DELAY_E","WEAK_ORACLE" : "WEAK", "ORDER_OF_ASSERTIONS" : "ASRT_ORDER", "STATE_POLLUTION_BY_DSI" : "POLLUTED", "STATE_RESTORED": "RESTORED", "CHECKS_MISDIRECTED_OUTPUT" : "OUT_CHECK", "DELAY_CAUSES_TIMEOUT": "TIMEOUT", "DELAY_CAUSES_OUTPUT_CORRUPTION" : "OUT_CORRUPTION", "CONCURRENCY": "CONCURRENCY", "DYNAMIC_DISPATCH" : "DYNAMIC_DISPATCH", "ASM_ERROR" : "ERROR", "CONFIGURATION" : "CONFIGURATION", "DYNAMIC_DISPATCH_SAME_METHOD" : "DD_SAME", "SPECIAL_NBP" : "SPECIAL_NBP", "SUPPLEMENTARY_EVIDENCE_BY_JAVADOC" : "JAVADOC", "REVERSE_NBP" : "REVERSE_NBP", "UNINTERESTING_SPEC" : "UNINTERESTING", "NO_COVERAGE" : "NO_COVERAGE", "MULTIPLE_PERTURBATIONS" : "PERTURBS"}
    for cat in tag_cat_dict_cat_order:
        cat_list = []
        for tag in tag_cat_dict[cat]:
            tag_dict={"cat": cat, "tag" : tag, "total" : 0}
            for project_data in all_data:
                name = project_data["name"]
                tag_dict[name] = 0
                if not name in keys:
                    keys.append(name)
                    csv_header[name] = project_data["name"]
                for category in project_data["inspection-results"]:
                    for spec_data in project_data["inspection-results"][category]:
                        # if tag in spec_data["tags"]:
                        if (tag + ",") in spec_data["tags"] or spec_data["tags"].endswith(tag):
                            tag_dict[name] += 1
                            tag_dict["total"] += 1
            cat_list.append(tag_dict)
        cat_list = sorted(cat_list, key=lambda d: d['total'], reverse = True)
        for sorted_tag in cat_list:
            sorted_tag["tagID"] = "T" + str(tag_id_acc)
            tag_id_map[sorted_tag["tag"]] = sorted_tag["tagID"]
            tag_id_acc += 1
            # sorted_tag["tag"] = tag_shortname_map[sorted_tag["tag"]]
        out_list += cat_list
    out_list.insert(0, csv_header)
    keys.append("total")
    output_to_csv(keys, out_list, output_dir, "global-tags.csv")
    return tag_id_map

def treat_special_tags(tags):
    excludes_file = os.path.join(intermediate_data_dir, "tags-excludes.csv" )
    f = open(excludes_file, "r")
    for line in f:
        tags.remove(line.strip())
    return tags

def mc_mw_breakdown_per_project(accuracy_dicts, helper_dict, mode): # helper dict has a project to manual outcomes dictionary bc my brain is too sad to write elegant code
    breakdown_original_keys = ["project", "mixed-correct-num", "mixed-incorrect-num", "total", "% Correct"]
    breakdown_keys = ["project", "true-spec", "spurious-spec", "unknown", "nbp", "sometimes-true-spec", "mixed-correct-num", "mixed-incorrect-num", "total", "% Correct"]
    helper_keys = ["true-spec", "spurious-spec", "unknown", "nbp", "sometimes-true-spec"]
    header = {key : key for key in breakdown_keys}
    breakdowns = [header]
    if mode == "sometimes-is-true":
        dict_name = "opt-4-sometimes-is-true (csv)"
    else:
        dict_name = "opt-4-sometimes-is-unknown (csv)"
    for entry in accuracy_dicts[dict_name]:
        project_breakdown = {}
        if entry["project"] == "project":
            continue
        for key in breakdown_original_keys:
            if key == "% Correct":
                project_breakdown[key] = divide(project_breakdown["mixed-correct-num"], project_breakdown["total"]) * 100
            elif key == "total":
                project_breakdown[key] = helper_dict[entry["project"]]["true-spec"] + helper_dict[entry["project"]]["spurious-spec"] + helper_dict[entry["project"]]["unknown"] + helper_dict[entry["project"]]["nbp"] + helper_dict[entry["project"]]["sometimes-true-spec"] # project_breakdown["mixed-correct-num"] + project_breakdown["mixed-incorrect-num"]
            else:
                project_breakdown[key] = entry[key]
        for helper_key in helper_keys:
            project_breakdown[helper_key] = helper_dict[entry["project"]][helper_key]

        breakdowns.append(project_breakdown)

    output_to_csv(breakdown_keys, breakdowns, output_dir, "mixed-correctness-project-breakdown-" + mode + ".csv")

def quick(all_data):            # if you wanted to retrieve a particular tag/verdict pair you can call this
    print("=================================================================")
    for project_data in all_data:
        name = project_data["name"]
        for cat in project_data["inspection-results"]:
            # if cat != "lv":
            #     continue
            for spec_data in project_data["inspection-results"][cat]:
                show = True
                # if spec_data["verdict"] == "true-spec" and "DELAY_CAUSES_TIMEOUT" in spec_data["tags"]:
                if "true-spec" == spec_data["verdict"]:
                    # for inspected_case_index in range(len(spec_data["inspected-cases"])): # validated-
                    #     if spec_data["inspected-cases"][inspected_case_index]["exception-or-assertion"] == "assertion":
                    #         show=True
                    #         break
                    if show and "GOOD_EXAMPLE" in spec_data["tags"]:
                        print("***" + spec_data['project'] + ";" + str(spec_data['code-snippets-file']) + ";" + spec_data["tags"] + ";" + spec_data["method-a"] + ";" + spec_data["method-b"])
                    elif show:
                        print(spec_data['project'] + ";" + str(spec_data['code-snippets-file']) + ";" + spec_data["tags"] + ";" + spec_data["method-a"] + ";" + spec_data["method-b"])


def super_quick(accuracy_maps, cat, tag):
    for project_data in accuracy_maps["default-sometimes-is-true"]: # "mixed-sometimes-is-true"
        print("====================================" + str(project_data["project"]))
        for cat in project_data:
            if cat == "project":
                continue
            for spec_data in project_data[cat]:
                # if (tag + "," in spec_data["tags"] or spec_data["tags"].endswith(tag)) and ("," + tag in spec_data["tags"] or spec_data["tags"].startswith(tag)):
                # if "findings" in spec_data or "INSIGHT" in spec_data["tags"]:            # (spec_data["part-of-bigger-spec"] == "tbd")
                if spec_data["verdict"] == "unknown":
                    print(json.dumps(spec_data, indent=4))
        print("##########################################################################")


def global_return_values(all_data, man_cat, dsi_cat):
    a_void_b_void = 0
    a_void_b_nonvoid = 0
    a_nonvoid_b_void = 0
    a_nonvoid_b_nonvoid = 0
    if man_cat == "ALL":
        out_name = "global-return-values-grid.csv"
    elif dsi_cat == "":
        out_name = man_cat + "-return-values-grid.csv"
    else:
        out_name = dsi_cat + "-" + man_cat + "-return-values-grid.csv"
    for project_data in all_data:
        for cat in project_data["inspection-results"]:
            if dsi_cat != "" and dsi_cat != cat:
                continue
            for spec_data in project_data["inspection-results"][cat]:
                # "method-a-return-type": "void",
                # "method-b-return-type": "java.lang.String",
                if man_cat != "ALL" and not man_cat in spec_data["verdict"]:
                    continue
                if spec_data["method-a-return-type"] == "void" and spec_data["method-b-return-type"] == "void":
                    a_void_b_void += 1
                elif spec_data["method-a-return-type"] == "void": # b is nonvoid
                    a_void_b_nonvoid += 1
                elif spec_data["method-b-return-type"] == "void": # a is nonvoid
                    a_nonvoid_b_void += 1
                else:           # both nonvoid
                    a_nonvoid_b_nonvoid += 1

    with open(os.path.join(output_dir, out_name), "w") as fil:
        fil.write("a_void_b_void,a_void_b_nonvoid,a_nonvoid_b_void,a_nonvoid_b_nonvoid\n")
        fil.write(str(a_void_b_void) + "," + str(a_void_b_nonvoid) + "," + str(a_nonvoid_b_void) + "," + str(a_nonvoid_b_nonvoid))

"""
call with ALL to just get mining data from all inspected specs
if mixed_mode is True, then we only consider the specs from mixed DSI verdicts
"""
def venn_maker_for_manual_category(all_data, manual_category, mixed_mode):
    onlyAT = 0
    onlyTC = 0
    AT_TC = 0
    onlyTM = 0
    AT_TM = 0
    TC_TM = 0
    AT_TC_TM = 0
    for project_data in all_data:
        name = project_data["name"]
        for cat in project_data["inspection-results"]:
            if mixed_mode and not "-" in cat:
                continue
            for spec_data in project_data["inspection-results"][cat]:
                if manual_category == "ALL" or manual_category == spec_data["verdict"]:
                    at = spec_data["all-tests-mined"]
                    tc = spec_data["test-classes-mine-count"] > 0
                    tm = spec_data["test-methods-mine-count"] > 0
                    if at and tc and tm:
                        AT_TC_TM += 1
                    elif at and tc:
                        AT_TC += 1
                    elif at and tm:
                        AT_TM += 1
                    elif tc and tm:
                        TC_TM += 1
                    elif at:
                        onlyAT += 1
                    elif tc:
                        onlyTC += 1
                    elif tm:
                        onlyTM += 1

    print("onlyAT: " + str(onlyAT) + " onlyTC: " + str(onlyTC) + " onlyTM: " + str(onlyTM))
    if mixed_mode:
        out = os.path.join(output_dir, "granularity-venn-diagram-only-mixed-" + manual_category + ".eps")
    else:
        out = os.path.join(output_dir, "granularity-venn-diagram-" + manual_category + ".eps")
    v = venn3(subsets=(onlyAT,onlyTC,AT_TC,onlyTM,AT_TM,TC_TM,AT_TC_TM), set_labels=('All Tests', 'Test Classes', 'Test Methods'))
    plt.savefig(out)
    plt.close()

def get_dsi_categorizations_per_project(all_projects_raw):
    out_file=os.path.join(output_dir, "dsi-categorizations-per-project.csv")
    f = open(out_file, "w")
    f.write("project,lv,ls,u,mixed,total\n")
    for project in all_projects_raw:
        name=project["name"]
        results = project["inspection-results"]
        lv=len(results["lv"])
        ls=len(results["ls"])
        u=len(results["u"])
        mixed_acc=0
        for cat in results:
            if "-" in cat:
                mixed_acc+= len(results[cat])
        f.write(name + "," + str(lv) + "," + str(ls) + "," + str(u) + "," + str(mixed_acc) + "," + str(lv + ls + u + mixed_acc) + "\n")

def get_manual_results_per_project(all_projects_raw):
    keys=["project", "true-spec", "spurious-spec", "unknown", "no-break-pass (direct)", "no-break-pass (indirect)", "sometimes-true-spec", "total"]
    stats_map = {"true-spec" : 0, "spurious-spec" : 0, "unknown" : 0, "no-break-pass (direct)" : 0, "no-break-pass (indirect)" : 0, "sometimes-true-spec" : 0}
    data=[{key : key for key in keys}]
    for project in all_projects_raw:
        project_dict=copy.deepcopy(stats_map)
        project_dict["project"] = project["name"]
        for cat in project["inspection-results"]:
            for spec_data in project["inspection-results"][cat]:
                project_dict[spec_data["verdict"]] += 1
        project_dict["total"] = project_dict["true-spec"] + project_dict["spurious-spec"] + project_dict["unknown"] + project_dict["no-break-pass (direct)"] + project_dict["no-break-pass (indirect)"] + project_dict["sometimes-true-spec"]
        data.append(project_dict)
    output_to_csv(keys, data, output_dir, "manual-categorizations-per-project.csv")

def get_manual_spurious_spec_percent_for_tags(all_data, tags):
    out_map = {}
    for tag in tags:
        num_total = 0
        num_spurious = 0
        for project_data in all_data:
            for cat in project_data["inspection-results"]:
                for spec_data in project_data["inspection-results"][cat]:
                    if (tag + ",") in spec_data["tags"] or spec_data["tags"].endswith(tag):
                        if "no-break-pass" in spec_data["verdict"] or spec_data["verdict"] == "unknown":
                            continue
                        num_total += 1
                        if spec_data["verdict"] == "spurious-spec":
                            num_spurious += 1
        out_map[tag] = divide(num_spurious, num_total) * 100

    return out_map

def wrapper():
    tags_file= os.path.join(intermediate_data_dir, "inspection-tags.csv")
    tags = get_tags(tags_file)
    tags = treat_special_tags(tags)
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    projects = []               # stores info about all projects in a list
    for project in os.listdir(tagged_inspections_dir):
        if project == "romix.java-concurrent-hash-trie-map":
            continue
        projects.append(retrieve_data_for_project(project))
    get_dsi_categorizations_per_project(projects)
    get_manual_results_per_project(projects)
    project_names = [ project["name"] for project in projects ]
    assertion_exception_maps = retrieve_assertion_exception_info(project_names)
    global_outcomes_dict = global_outcomes(projects)
    output_global_outcomes_to_csv(global_outcomes_dict)
    tid_map = global_tags(tags, projects)
    dsiPlus_global_outcomes(projects)

    impact_of_oracles(projects)
    man_breakdown_mixed_per_project = per_project_manual_breakdown_of_mixed(projects)
    accuracy_maps = sort_into_accuracy(projects, man_breakdown_mixed_per_project)
    output_accuracy_maps_csv(accuracy_maps)
    mc_mw_breakdown_per_project(accuracy_maps, man_breakdown_mixed_per_project, "sometimes-is-true")
    mc_mw_breakdown_per_project(accuracy_maps, man_breakdown_mixed_per_project, "sometimes-is-unknown")
    assertions_vs_exceptions("true-positive", accuracy_maps, assertion_exception_maps)
    assertions_vs_exceptions("false-positive", accuracy_maps, assertion_exception_maps)
    global_tag_counter = tag_counter(tags, accuracy_maps)
    output_tag_counter_to_csv("WEAK_ORACLE", global_tag_counter)
    output_tag_counter_to_csv("MULTIPLE_PERTURBATIONS", global_tag_counter)
    outcome_category_to_tag_map(global_tag_counter, project_names, "false-positive")
    man_spu_map = get_manual_spurious_spec_percent_for_tags(projects, tags)
    global_tag_counter_per_category(global_tag_counter, "default-sometimes-is-true", tid_map, man_spu_map)
    global_tag_counter_per_category(global_tag_counter, "mixed-sometimes-is-true", tid_map, man_spu_map)

    global_return_values(projects, "ALL", "")
    global_return_values(projects, "true-spec", "")
    global_return_values(projects, "spurious-spec", "")
    global_return_values(projects, "spurious-spec", "lv")
    global_return_values(projects, "true-spec", "ls")
    # TODO: uncomment this
    # venn_maker_for_manual_category(projects, "true-spec", True)
    # venn_maker_for_manual_category(projects, "sometimes-true-spec", True)
    # venn_maker_for_manual_category(projects, "true-spec", False)
    # venn_maker_for_manual_category(projects, "sometimes-true-spec", False)
    # venn_maker_for_manual_category(projects, "ALL", False)

    # dict_keys(['project', 'true-positive', 'false-positive', 'unknown-positive', 'true-negative', 'false-negative', 'unknown-negative', 'true-unknown', 'false-unknown (TS)', 'false-unknown (NS)'])
    # super_quick(accuracy_maps, "true-negative", "WEAK_ORACLE")
    # quick(projects) # throwaway method

if __name__ == '__main__':
    wrapper()

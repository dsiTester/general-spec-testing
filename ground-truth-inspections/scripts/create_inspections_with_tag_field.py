"""

This script reads in the json files in the
dsi-inspections/inspections/${project} directory for each project, and
outputs a json file with the same data but also with an additional
"tags" field that lists all of the tags from the inspection as a
comma-separated list.

"""
import json
import os
import sys
from pathlib import Path
from shutil import rmtree
import subprocess

scripts_dir=os.path.dirname(os.path.abspath(__file__))
base_dir=os.path.dirname(scripts_dir)
templates_dir=base_dir + os.path.sep + "templates"
original_inspections_dir=base_dir + os.path.sep + "inspection-files"
intermediate_data_dir=base_dir + os.path.sep + "data"
# data_dir=os.path.join(intermediate_data_dir, "fine-grained-dsi-breakdowns")
dsiPlus_data_dir=os.path.join(intermediate_data_dir, "dsiPlus-data")
perturbations_dir=os.path.join(intermediate_data_dir, "perturbation-info")

"""
For each spec inspection in the data from a json file, checks all notes/findings/comments
 fields, collects the used tags, and updates the data with the new tags field.
This probably ought to be refactored... the current code seems a bit messy and redundant.
"""
def create_tagged_data(og_data, tags, dsiPlus_outcomes, mult_perturbation_list, data_category):
    cases = [ "inspected-cases", "validated-inspected-cases", "invalidated-inspected-cases", "unknown-inspected-cases", "error-inspected-cases" ]
    concurrent = []
    for spec_entry in og_data:
        spec_id=spec_entry["spec-id"]
        test = ""
        tags_for_spec = set()
        for case in cases:
            if case in spec_entry:
                for inspected_case_index in range(len(spec_entry[case])):
                    if test == "": # need to know at least one test that we used to inspect, so we can replace validating-test-file for NBPs with the actual test name
                        test = spec_entry[case][inspected_case_index]["test"]
                    for tag in tags:
                        if tag in spec_entry[case][inspected_case_index]["comment-on-return-values"]:
                            if tag != "EXPECTED_EXCEPTION": # don't need to do additional checks
                                tags_for_spec.add(tag)
                            elif (not "EXPECTED_EXCEPTION_NOT_THROWN" in spec_entry[case][inspected_case_index]["comment-on-return-values"]) and (not "IMPLICITLY_EXPECTED_EXCEPTION" in spec_entry[case][inspected_case_index]["comment-on-return-values"]) and (not "DELAY_OF_A_CAUSES_UNEXPECTED_EXCEPTION" in spec_entry[case][inspected_case_index]["comment-on-return-values"]): # we don't want any of the other cases to overcount the EXPECTED_EXCEPTION case
                                tags_for_spec.add(tag)
                        if tag in spec_entry[case][inspected_case_index]["notes"]:
                            if tag != "EXPECTED_EXCEPTION": # don't need to do additional checks
                                tags_for_spec.add(tag)
                            elif (not "EXPECTED_EXCEPTION_NOT_THROWN" in spec_entry[case][inspected_case_index]["notes"]) and (not "IMPLICITLY_EXPECTED_EXCEPTION" in spec_entry[case][inspected_case_index]["notes"]) and (not "DELAY_OF_A_CAUSES_UNEXPECTED_EXCEPTION" in spec_entry[case][inspected_case_index]["notes"]): # we don't want any of the other cases to overcount the EXPECTED_EXCEPTION case
                                tags_for_spec.add(tag)
        fields_for_notes = [ "notes", "findings" ]
        for field in fields_for_notes:
            if field in spec_entry:
                for tag in tags:
                    if tag in spec_entry["notes"]:
                        if tag != "EXPECTED_EXCEPTION": # don't need to do additional checks
                            tags_for_spec.add(tag)
                        elif (not "EXPECTED_EXCEPTION_NOT_THROWN" in spec_entry["notes"]) and (not "IMPLICITLY_EXPECTED_EXCEPTION" in spec_entry["notes"]) and (not "DELAY_OF_A_CAUSES_UNEXPECTED_EXCEPTION" in spec_entry["notes"]): # we don't want any of the other cases to overcount the EXPECTED_EXCEPTION case
                            tags_for_spec.add(tag)
        if spec_id in mult_perturbation_list:
            tags_for_spec.add("MULTIPLE_PERTURBATIONS")
        if (spec_entry["method-a-return-type"] == "void"):
            tags_for_spec.add("METHOD_A_RETURNS_VOID")
        if ("is-oracle-weak" in spec_entry and spec_entry["is-oracle-weak"]):
            tags_for_spec.add("WEAK_ORACLE")
        if ("DYNAMIC_DISPATCH_SAME_METHOD" in tags_for_spec):
            tags_for_spec.add("SPECIAL_NBP")
        if ("CONCURRENCY" in tags_for_spec):
            concurrent.append(spec_entry)
        tags_dir = {"tags" : ",".join(tags_for_spec)}
        # if the tags contain SOMETIMES_TRUE_SPEC, then convert the verdict to sometimes-true-spec
        if "SOMETIMES_TRUE_SPEC" in tags_dir["tags"] and spec_entry["verdict"] != "spurious-spec":
            tags_dir["verdict"] = "sometimes-true-spec"
        if ("no-break-pass" in spec_entry["verdict"]) and ("snippet" in spec_entry["validating-test-file"]):
            tags_dir["validating-test-file"] = test
        tags_dir["dsiPlus-outcome"] = dsiPlus_outcomes[spec_id]
        spec_entry.update(tags_dir)

    if "u" not in data_category:
        for c in concurrent:
            og_data.remove(c)
        return concurrent
    else:
        return []

"""
Iterates through all json inspection files in the original project directory,
and outputs the json with tags to the new tagged project directory.
"""
def project_wrapper(tags, project_name, og_dir, tagged_dir, dsiPlus_outcomes, mult_perturb_list):
    project_og_dir = os.path.join(og_dir, project_name)
    project_tagged_dir = os.path.join(tagged_dir, project_name)
    os.mkdir(project_tagged_dir)
    concurrency = []
    for filename in os.listdir(project_og_dir):
        if not filename.endswith(".json"):
            continue
        f = open(os.path.join(project_og_dir, filename), "r")
        category = filename.split(".")[0]
        data = json.load(f)
        concurrency += create_tagged_data(data, tags, dsiPlus_outcomes, mult_perturb_list, category)
        with open(os.path.join(project_tagged_dir, filename), "w") as fil:
            json.dump(data, fil, indent=4)

    with open(os.path.join(project_tagged_dir, "u.json"), "r") as f:
        u_specs = json.load(f)
    u_specs += concurrency
    with open(os.path.join(project_tagged_dir, "u.json"), "w") as f:
        json.dump(u_specs, f, indent=4)

"""
Retrieves all tags from the specified tags file.
"""
def get_tags(file_path):
    tags = set()
    f = open(file_path, "r")
    for line in f:
        tags.add(line.split(",")[0])
    return tags

"""
Remove the previously existing tagged-inspections dir and create a new one from scratch.
"""
def prepare_directory():
    tagged_inspections_dir = base_dir + os.path.sep + "tagged-inspections"
    if (os.path.isdir(tagged_inspections_dir)):
        rmtree(tagged_inspections_dir)
    os.mkdir(tagged_inspections_dir)
    return tagged_inspections_dir

def get_dsiPlus_outcomes_for_project(project):
    dsiPlus_outcome_dict={}
    project_file=os.path.join(dsiPlus_data_dir, project + ".csv")
    if os.path.exists(project_file):
        f = open(project_file, "r")
        for line in f:
            line = line.rstrip()
            s=line.split(",")
            if s[1] == "error":
                dsiPlus_outcome_dict[s[0]] = "unknown"
            else:
                dsiPlus_outcome_dict[s[0]] = s[1]
    return dsiPlus_outcome_dict

def get_mult_perturbation_ids_for_project(project):
    mult_perturb_list = []
    project_file = os.path.join(perturbations_dir, project + ".csv")
    if os.path.exists(project_file):
        f = open(project_file, "r")
        for line in f:
            if not "spec," in line:
                mult_perturb_list.append(line.split(",")[0])

    return mult_perturb_list

def wrapper():
    tagged_inspections_dir=prepare_directory()
    tags_file= os.path.join(intermediate_data_dir, "inspection-tags.csv")
    tags = get_tags(tags_file)
    for project in os.listdir(original_inspections_dir):
        if project == "README.md":
            continue
        if project != "romix.java-concurrent-hash-trie-map":
            dsiPlus_outcomes = get_dsiPlus_outcomes_for_project(project)
            mult_perturb_list = get_mult_perturbation_ids_for_project(project)
            project_wrapper(tags, project, original_inspections_dir, tagged_inspections_dir, dsiPlus_outcomes, mult_perturb_list)

if __name__ == '__main__':
    wrapper()

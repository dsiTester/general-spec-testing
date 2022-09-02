import collections
import copy
import json
import os
import sys
import csv


from jsonschema import validate

scripts_dir=os.path.dirname(os.path.abspath(__file__))
base_dir=os.path.dirname(scripts_dir)
# tagged_inspections_dir=os.path.join(base_dir, "tagged-inspections")
data_dir = os.path.join(base_dir, "data")
output_dir=os.path.join(data_dir, "initial-experiments")


def contains_state_tag(tags):
    relations = {"UNRELATED_STATEFUL_METHODS", "ONE_STATELESS_METHOD", "ONE_PURE_SETTER", "CONNECTION_DOES_NOT_NECESSITATE_ORDERING", "UNRELATED_STATELESS_METHODS", "UNRELATED_PURE_SETTERS", "TRUE_SPEC_WITH_CALLER", "MODIFIED_STATE_DOES_NOT_INTERSECT"}
    for rtag in relations:
        if rtag in tags:
            return True

    return False

def read_specs(tagged_inspections_dir, project_name, mode):
    project_dir=tagged_inspections_dir #os.path.join(tagged_inspections_dir, project_name)
    out = open(os.path.join(output_dir, project_name + "-setup.csv"), "w")
    out.write("spec-id,method-a,method-b,tst,dsi-verdict,manual-verdict,tags\n")
    for filename in os.listdir(project_dir):
        if not filename.endswith(".json"):
            continue
        cat = filename.split(".")[0]
        f = open(os.path.join(project_dir, filename))
        data = json.load(f)
        for spec_data in data:
            if (not "no-break-pass" in spec_data["verdict"]) and (mode == "ALL" or contains_state_tag(spec_data["tags"])):
                tst = ""
                if "inspected-cases" in spec_data:
                    tst = spec_data["inspected-cases"][0]["test"]
                elif "validated-inspected-cases" in spec_data:
                    tst = spec_data["validated-inspected-cases"][0]["test"]
                elif "invalidated-inspected-cases" in spec_data:
                    tst = spec_data["invalidated-inspected-cases"][0]["test"]
                elif "unknown-inspected-cases" in spec_data:
                    tst = spec_data["unknown-inspected-cases"][0]["test"]
                elif "error-inspected-cases" in spec_data:
                    tst = spec_data["error-inspected-cases"][0]["test"]
                out.write(",".join([spec_data["spec-id"], spec_data["method-a"], spec_data["method-b"], tst, cat, spec_data["verdict"], spec_data["tags"].replace(",", ";")]) + "\n")

def wrapper(tagged_inspections_dir, project_name, mode):
    read_specs(tagged_inspections_dir, project_name, mode)

if __name__ == '__main__':
    if len(sys.argv) != 4:
        print("Usage: " + sys.argv[0] + " project-tagged-inspections-dir project mode")
        print("where mode is either ALL or STATE, where STATE is only specs that had tags relating to state")
        sys.exit(1)
    if not os.path.isdir(sys.argv[1]):
        print("project-tagged-inspections-dir is not a directory!")
        sys.exit(1)
    wrapper(sys.argv[1], sys.argv[2], sys.argv[3])
    # if len(sys.argv) != 3:     
    #     print("Usage: " + sys.argv[0] + " json-file out-file")
    #     sys.exit(1)
    # true_specs = fetch(sys.argv[1])
    # with open(sys.argv[2], 'a') as out:
    #     [out.write(spec + '\n') for spec in true_specs]

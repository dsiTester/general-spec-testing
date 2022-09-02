import pandas as pd
import os
import sys
import copy
import csv

def read_csv(specs_dir):
    data = {"spec_id" : [], "project" : []}
    for filename in os.listdir(specs_dir):
        print("===Reading from file: " + filename)
        if (not filename.endswith(".csv")):
            continue
        project = filename.split('.')[0]
        with open(os.path.join(specs_dir, filename)) as csvfile:
            for line in csvfile:
                if line.startswith("id"):
                    continue
                data["spec_id"].append(line.split(",")[0])
                data["project"].append(project)
    return data

def wrapper(num_specs_to_inspect, num_total_specs, specs_dir):
    fraction = num_specs_to_inspect / num_total_specs
    print("fraction: " + str(fraction))
    data = read_csv(specs_dir)
    df = pd.DataFrame(data)

    outcome=df.groupby('project', group_keys=False).apply(lambda x: x.sample(frac=fraction))
    with open("out.txt", "w") as f:
        f.write(outcome.to_csv())

if __name__ == '__main__':
    # we want the total number of specs we want to inspect.
    # we want a directory that contains every non-inspected spec from every project. maybe we'll have bash take care of this
    if len(sys.argv) != 4:
        print("Usage: " + sys.argv[0] + " num_specs_to_inspect num_total_specs specs_dir")
        sys.exit(1)
    if not os.path.exists(sys.argv[3]):
        print("file does not exist!")
        sys.exit(1)
    wrapper(int(sys.argv[1]), int(sys.argv[2]), sys.argv[3])


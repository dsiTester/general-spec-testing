import csv
import os

def read_csv(filename):
    ret = []
    with open(filename, newline='') as csvfile:
        reader = csv.reader(csvfile, delimiter=',')
        for row in reader:
            if row[0] == "id":
                continue
            entry = {}
            # id,share-state(tool),original-dsi,updated-dsi,verdict,manual-tags,a-set-b-set,a-set-b-get,a-get-b-set,a-get-b-get
            entry["id"] = row[0]
            entry["share-state"] = row[1]
            entry["original-dsi-verdict"] = row[2]
            entry["updated-dsi-verdict"] = row[3]
            entry["manual-verdict"] = row[4]
            entry["tags"] = row[5]
            entry["a-set-b-set"] = row[6]
            entry["a-set-b-get"] = row[7]
            entry["a-get-b-set"] = row[8]
            entry["a-get-b-get"] = row[9]
            entry["used-jdk"] = row[10]
            ret.append(entry)
    return ret

"""
Utility function to write output to csv.
"""
def output_to_csv(keys, list_to_output, dirname, filename):
    with open(os.path.join(dirname, filename), "w") as out:
        writer = csv.DictWriter(out, fieldnames = keys)
        writer.writerows(list_to_output)

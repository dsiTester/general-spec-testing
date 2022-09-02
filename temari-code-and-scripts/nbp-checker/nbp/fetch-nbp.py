import json
import jsonschema
import sys

from jsonschema import validate

def fetch(json_file):
    lines = []
    with open(json_file) as jfile:
        json_data=json.load(jfile)
        for data in json_data:
            if 'no-break-pass' in data['verdict']:
                lines.append(" ".join([data['spec-id'], data['method-a'], data['method-b']]))
    return lines

if __name__ == '__main__':
    if len(sys.argv) != 3:
        print("Usage: " + sys.argv[0] + " json-file out-file")
        sys.exit(1)
    true_specs = fetch(sys.argv[1])
    with open(sys.argv[2], 'a') as out:
        [out.write(spec + '\n') for spec in true_specs]

import json
import jsonschema
import sys

from jsonschema import validate

def validate_json(schema_file, json_file):
    with open(json_file) as jfile:
        json_data=json.load(jfile)
    with open(schema_file) as sfile:
        schema_data=json.load(sfile)
    try:
        validate(instance=json_data, schema=schema_data)
    except jsonschema.exceptions.ValidationError as err:
        print(err)
        return False
    return True


if __name__ == '__main__':
    if len(sys.argv) != 3:
        print("Usage: " + sys.argv[0] + " json-schema json-file")
        sys.exit(1)
    if (validate_json(sys.argv[1], sys.argv[2])):
        print("VALID")
    else:
        print("INVALID")
        sys.exit(1)

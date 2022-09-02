# Scripts for Processing Inspections

To run all the scripts in this directory, it is sufficient to call this wrapper script that calls all the other scripts in this directory:
```
bash check-and-process-inspection-data.sh
```

This directory contains scripts that validate and process the inspection files.

- `analyze-inspection-data.py` reads the `tagged-inspections` directory and produces the computed values to the `data/analysis` directory. This script *must* be only called after `create_inspections_with_tag_field.py` is invoked.
- `check-and-process-inspection-data.sh` is a utility script that calls `check-inspections.sh`, `create_inspections_with_tag_field.py`, and `analyze-inspection-data.py`.
- `check-inspections.sh` is a wrapper script that validates every JSON file in the `inspection-files` directory.
- `create_inspections_with_tag_field.py` post-processes the `inspection-files` directory in order to produce the `tagged-inspections` directory.
- `validate-inspection-file.py` validates a single JSON file against a schema (provided in `../data/schemas/`).

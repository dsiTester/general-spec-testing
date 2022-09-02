# Files from Manual Inspection

This directory records the manual inspections. Each subdirectory contains all inspection files and snippets from each project respectively. Each DSI+ category (listed below) has its own JSON file where all specifications categorized as so via DSI+ will be placed in that file. The path to each code snippet is noted in the inspection files.

DSI+ categories:
- `lv` : DSI+ categorized the specification as likely valid
- `ls` : DSI+ categorized the specification as likely spurious
- `u` : DSI+ categorized the specification as unknown
- `e` : DSI+ categorized the specification as error (a different category of unknown)
- `lv-ls` : DSI+ categorized the specification as likely valid and likely spurious (Mixed case)
- `lv-u` : DSI+ categorized the specification as likely valid and unknown (Mixed case)
- `lv-e` : DSI+ categorized the specification as likely valid and error (Mixed case)
- `ls-u` : DSI+ categorized the specification as likely spurious and unknown (Mixed case)
- `ls-e` : DSI+ categorized the specification as likely spurious and error (Mixed case)
- `u-e` : DSI+ categorized the specification as unknown and error (condensed into one `U` case via the processing scripts)
- `lv-ls-u`: DSI+ categorized the specification as likely valid, likely spurious, and unknown (Mixed case)
- `lv-ls-e`: DSI+ categorized the specification as likely valid, likely spurious, and error (Mixed case)
- `lv-u-e`: DSI+ categorized the specification as likely valid, unknown, and error (Mixed case)
- `ls-u-e`: DSI+ categorized the specification as likely spurious, unknown, and error (Mixed case)
- `lv-ls-u-e`: DSI+ categorized the specification as likely valid, likely spurious, unknown and error (Mixed case)

Raw tags that we used during inspection, and how we initially understood them are in `inspections/data/inspection-tags.csv`.

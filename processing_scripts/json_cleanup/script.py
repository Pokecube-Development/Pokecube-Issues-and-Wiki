import os
import sys
import json
import re
from glob import glob

jsons = [y for x in os.walk("./inputs") for y in glob(os.path.join(x[0], '*.json'))]

'''
Here are some regex used earlier, npp refers to in Notepad++.

Advancements:
npp:
("items": \[)(.*)("item": ")(.*?)(")
\1\2("items": \[ ")\4(" \])

Loot tables:

npp:
("predicate": {)(\s+)("item": ")(.*?)(")
\1\2("items": \[ ")\4(" \])
here:
r'("predicate": {)(\s+)("item": ")(.*?)(")':
r'\1\2"items": [ "\4" ]',

'''

reps = {
r'("predicate": {)(\s+)("item": ")(.*?)(")':
r'\1\2"items": [ "\4" ]',
}

# reps = {}

replaced = 0

lastdir = None
for file in jsons:

    json_str = ""
    try:
        json_in = open(file, 'r', encoding='utf-8')
        json_str = json_in.read()
        json_in.close()

        for key, val in reps.items():
            (json_str, num) = re.subn(key, val, json_str, flags=re.M)
            replaced = replaced + num
            if num > 0:
                print("    Replacement in: {} ({})".format(file, num))
        json_obj = json.loads(json_str)
    except Exception as e:
        print("Error with: {}".format(file))
        #print(json_str)
        print(e)
        continue

    file = file.replace('inputs', 'outputs', 1)
    dir = os.path.dirname(file)
    try:
        os.makedirs(dir)
    except:
        pass
    if dir!=lastdir:
        print(dir)
    lastdir = dir

    json_out = open(file, 'w', encoding='utf-8')
    json_out.write(json.dumps(json_obj, ensure_ascii=False, indent=2))
    json_out.close()

print("Replacements: {}".format(replaced))
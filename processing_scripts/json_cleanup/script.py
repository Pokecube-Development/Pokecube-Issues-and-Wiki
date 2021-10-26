import os
import sys
import json
from glob import glob

jsons = [y for x in os.walk("./inputs") for y in glob(os.path.join(x[0], '*.json'))]


lastdir = None
for file in jsons:

    json_in = open(file, 'r', encoding='utf-8')

    try:
        json_obj = json.loads(json_in.read())
    except Exception as e:
        print("Error with: {}".format(file))
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
    json_out.write(json.dumps(json_obj, indent=2))
    json_out.close()
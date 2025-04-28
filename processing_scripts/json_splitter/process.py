import os
import json

file = "./conditions.json"

to_filename = lambda x: f"./{x['name']}.json"
split_field = "conditions"

data = json.load(open(file))

if split_field in data:
    for value in data[split_field]:
        filename = to_filename(value)
        json.dump(value, open(filename, 'w'),indent=2)
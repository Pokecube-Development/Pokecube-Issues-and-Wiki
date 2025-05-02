import os
import json

file = "./trades.json"
split_field = "trades"

def to_filename(x):
    if 'template' in x:
        return f"./output/{x['template'].split(':')[-1]}.json"
    if 'profession' in x:
        return f"./output/{x['profession'].split(':')[-1]}.json"
    if 'type' in x:
        return f"./output/{x['type'].split(':')[-1]}.json"

data = json.load(open(file))

def process(value):
    if 'pokemon' in value:
        value['pokemon'] = value['pokemon'].strip().split(',')
    return value

if split_field in data:
    for value in data[split_field]:
        filename = to_filename(value)
        json.dump(process(value), open(filename, 'w'),indent=2)
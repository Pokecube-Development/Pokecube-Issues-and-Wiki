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

def rep():

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

reps_start = "ASDFSDGDFGDFGDFG{}"
global reps_index
reps_index = 0
reps_map = {}


def has_sub_map_items(item):
    for key , value in item.items():
        if isinstance(value, dict):
            for key2 , value2 in value.items():
                if isinstance(value2, dict):
                    return True
                if isinstance(value2, list):
                    return True
        if isinstance(value, list):
            return True
    return False

def replace_members(json_in):
    global reps_index
    for key,value in json_in.items():
        if isinstance(value, dict):
            has_sub_map = has_sub_map_items(value)
            print(f'{has_sub_map} {key}')
            if not has_sub_map:
                as_str = json.dumps(value, ensure_ascii=False)
                rep_key = reps_start.format(reps_index)
                reps_index+=1
                reps_map[rep_key] = as_str
                json_in[key] = rep_key
            else:
                replace_members(value)
        if isinstance(value, list):
            for i in range(len(value)):
                var = value[i]
                has_sub_map = has_sub_map_items(var)
                print(f'{has_sub_map} {i}')
                if not has_sub_map:
                    as_str = json.dumps(var, ensure_ascii=False)
                    rep_key = reps_start.format(reps_index)
                    reps_index+=1
                    reps_map[rep_key] = as_str
                    value[i] = rep_key
                else:
                    replace_members(var)

def pretty():

    replaced = 0
    lastdir = None
    for file in jsons:

        json_str = ""
        try:
            json_in = open(file, 'r', encoding='utf-8')
            json_str = json_in.read()
            json_in.close()
            json_obj = json.loads(json_str)
            replace_members(json_obj)
            print(json_obj)

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

        pretty = json.dumps(json_obj, ensure_ascii=False, indent=2)
        for key,value in reps_map.items():
            pretty = pretty.replace(f'"{key}"', value)

        json_out.write(pretty)
        json_out.close()

    print("done")

pretty()
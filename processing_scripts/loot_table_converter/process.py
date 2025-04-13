import os
import json


def find_all(map, key, root, collector):
    adjust = []
    for _key, value in map.items():
        if _key == key:
            adjust.append((_key, value))
        elif isinstance(value, dict):
            find_all(value, key, _key, collector)
        elif isinstance(value, list):
            for x in value:
                if isinstance(x, dict):
                    find_all(x, key, _key, collector)
    for key_, value in adjust:
        collector(key_, value, root, map)

def fix_loot_table(key, value, root, map):
    if root != "entries":
        return
    if not ":" in value:
        map[key] = f'minecraft:{value}'
        value = map[key]
    if value == 'minecraft:loot_table':
        if 'name' in map:
            var = map['name']
            del map['name']
            map['value'] = var

def fix_function(key, value, root, map):
    if not ":" in value:
        map[key] = f'minecraft:{value}'
        value = map[key]
    if "count" in map:
        count = map["count"]
        if isinstance(count, dict):
            if not "type" in count:
                count["type"] = "minecraft:uniform"
        if count == "minecraft:set_count":
            map['count'] = 1
        if count == "minecraft:enchanted_count_increase":
            map['count'] = {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 0.0
            }
    if value == "minecraft:set_count":
        if not "add" in map:
            map["add"] = False
    if value == "minecraft:looting_enchant":
        map[key] = "minecraft:enchanted_count_increase"
        map['enchantment'] = "minecraft:looting"


def fix_uniform_range(key, value, root, map):
    fix_list = [
        "minecraft:uniform",
        "minecraft:trapezoid",
        "minecraft:clamped",
        "minecraft:clamped_normal",
        "minecraft:biased_to_bottom"
    ]


    if value in fix_list:
        if "value" in map:
            _val = map["value"]
            del map["value"]
            for k, v in _val.items():
                map[k] = v

def process_file(path):
    file = open(path)
    vals = json.load(file)
    file.close()
    # find_all(vals, "type", "root", fix_loot_table)
    # find_all(vals, "function", "root", fix_function)
    find_all(vals, "type", "root", fix_uniform_range)
    file = open(path, 'w')
    json.dump(vals, file, indent=2, sort_keys=True)
    file.close()

def process_dir(dir):
    for filename in os.listdir(dir):
        path = f'{dir}/{filename}'
        if os.path.isdir(path):
            process_dir(path)
        else:
            process_file(path)

# process_dir('./loot_table')
process_dir('./worldgen')
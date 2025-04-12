import os
import json

data_dir = '../../src/generated/resources/data/pokecube_mobs/database/pokemobs'
entries = 'pokedex_entries'

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

def process_entry(vars):
     
     # Process interacts
     if "interactions" in vars:
         interactions = vars['interactions']
         for interact in interactions:
            if 'key' in interact:
                key = interact['key']
                if 'values' in key:
                    old = key['values']
                    del key['values']
                    for k, v in old.items():
                        key[k] = v
            if "action" in interact:
                action = interact['action']
                if 'drops' in action:
                    for drop in action['drops']:
                        if 'values' in drop:
                            old = drop['values']
                            del drop['values']
                            for k, v in old.items():
                                if k == 'n':
                                    k = 'count'
                                    v = int(v)
                                drop[k] = v



for filename in os.listdir(f'{data_dir}/{entries}'):
    filename = f'{data_dir}/{entries}/{filename}'
    file = open(filename)
    vars = json.load(file)
    file.close()
    process_entry(vars)
    file = open(filename, 'w')
    json.dump(vars, file, indent=2)
    file.close()
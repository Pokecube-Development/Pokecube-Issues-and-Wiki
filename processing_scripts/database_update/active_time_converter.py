import json
from legacy_renamer import find_new_name
from utils import get_pokemon_index
import os

tag_generate_dir = './new/tags/pokemob/active_times/'

old_file = "./old/pokemobs/pokemobs_interacts.json"

if not os.path.exists(tag_generate_dir):
    os.makedirs(tag_generate_dir)

day = []
night = []
dusk = []
dawn = []

index_map = get_pokemon_index()
json_in = open(old_file, 'r', encoding='utf-8')
json_str = json_in.read()
json_in.close()
json_obj = json.loads(json_str)

for entry in json_obj["pokemon"]:
    if 'stats' in entry and "activeTimes" in entry['stats']:
        times = entry['stats']["activeTimes"].lower()
        name = find_new_name(entry['name'], index_map.keys())
        name = f'pokecube:{name}'
        if 'day' in times:
            day.append(name)
        if 'night' in times:
            night.append(name)
        if 'dusk' in times:
            dusk.append(name)
        if 'dawn' in times:
            dawn.append(name)

old_file = "./old/tags/pokemob/active_times/"
if not os.path.exists(tag_generate_dir):
    os.makedirs(tag_generate_dir)

obj = {}
obj['replace'] = False
obj['values'] = dawn
file = f'{old_file}dawn.json'
file = open(file, 'w')
json.dump(obj, file, indent=2)
file.close()
obj = {}
obj['replace'] = False
obj['values'] = day
file = f'{old_file}day.json'
file = open(file, 'w')
json.dump(obj, file, indent=2)
file.close()
obj = {}
obj['replace'] = False
obj['values'] = dusk
file = f'{old_file}dusk.json'
file = open(file, 'w')
json.dump(obj, file, indent=2)
file.close()
obj = {}
obj['replace'] = False
obj['values'] = night
file = f'{old_file}night.json'
file = open(file, 'w')
json.dump(obj, file, indent=2)
file.close()
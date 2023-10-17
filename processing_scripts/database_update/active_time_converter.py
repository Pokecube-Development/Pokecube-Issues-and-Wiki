import json
from legacy_renamer import get_interacts
from utils import get_pokemon_index
import os

def make_times(index_map):
    tag_generate_dir = './new/tags/pokemob/active_times/'

    if not os.path.exists(tag_generate_dir):
        os.makedirs(tag_generate_dir)

    day = []
    night = []
    dusk = []
    dawn = []

    interacts = get_interacts(index_map)

    for name, stats in interacts.items():
        if "activeTimes" in stats:
            times = stats["activeTimes"].lower()
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

if __name__ == "__main__":
    index_map = get_pokemon_index()
    make_times(index_map)
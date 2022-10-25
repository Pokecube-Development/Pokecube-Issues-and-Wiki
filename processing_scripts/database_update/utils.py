import json
import os
from types import SimpleNamespace

# The .cache folder is copied from https://github.com/PokeAPI/api-data
#
# generate this via: 
#
# cd ./.cache
# git clone https://github.com/PokeAPI/api-data.git

DEFAULT_GENERATION = "ultra-sun-ultra-moon"

def default_or_latest(group_details, valid):
    ret = None
    max_v = 0
    for details in group_details:
        if not valid(details):
            continue
        group = details.version_group.name
        if group == DEFAULT_GENERATION:
            return details
        index = int(details.version_group.url.split('/')[-2])
        if index > max_v:
            max_v = index
            ret = details
    return ret

def trim(string):
    string = string.strip()
    string = string.lower()
    string = string.replace("([^a-z0-9 /_-])", "")
    string = string.replace(' ', '_')
    return string

def get(folder, number):
    cache_file = f'./.cache/api-data/data/api/v2/{folder}/{number}/index.json'
    if os.path.exists(cache_file):
        file = open(cache_file, 'r')
        data = file.read()
        file.close()
        return json.loads(data, object_hook=lambda d: SimpleNamespace(**d))
    return None

def get_valid_numbers(folder):
    index_file = f'./.cache/api-data/data/api/v2/{folder}/index.json'
    index_map = {}
    if os.path.exists(index_file):
        file = open(index_file, 'r')
        data = file.read()
        file.close()
        index = json.loads(data, object_hook=lambda d: SimpleNamespace(**d))
        for var in index.results:
            key = var.name
            number = int(var.url.split('/')[-2])
            index_map[key] = number
    return index_map

def get_pokemon(number):
    return get('pokemon', number)

def get_species(number):
    return get('pokemon-species', number)

def get_form(number):
    return get('pokemon-form', number)

def get_moves_index():
    return get_valid_numbers('move')

def get_pokemon_index():
    return get_valid_numbers('pokemon')

def get_move(number):
    return get('move', number)

def url_to_id(var):
    return int(var.url.split('/')[-2])
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

def load_all_moves():
    moves_index = get_moves_index()
    moves_map = {}
    users_map = {}
    for key, number in moves_index.items():
        move = get("move", number)
        mobs = []
        if key in moves_map:
            mobs = moves_map[key]
        else:
            moves_map[key] = mobs
        for entry in move.learned_by_pokemon:
            name = entry.name
            mobs.append(name)
            moves = []
            if name in users_map:
                moves = users_map[name]
            else:
                users_map[name] = moves
            if not key in moves:
                moves.append(key)
    return moves_map, users_map


def load_evo_chains():
    evo_chain_dir = './.cache/api-data/data/api/v2/evolution-chain/'
    evos = {}

    def process_chain(chain, depth=0):
        species = chain['species']["name"]
        evos_to = []
        if species in evos:
            evos_to = evos[species]
        else:
            evos[species] = evos_to
        for evo in chain['evolves_to']:
            details = {}
            details['name'] = evo['species']["name"]
            details['evolution_details'] = evo['evolution_details']
            evos_to.append(details)
            process_chain(evo, depth + 1)

    for sub_dir in os.listdir(evo_chain_dir):
        if 'index' in sub_dir:
            continue
        file = open(evo_chain_dir + sub_dir + "/index.json", 'r')
        values = json.load(file)
        chain = values['chain']
        file.close()
        process_chain(chain)

    return evos

if __name__ == "__main__":
    # evos = load_evo_chains()
    # for key, value in evos.items():
    #     print(key)
    #     [print(" ",x['name']) for x in value]
    moves, users = load_all_moves()
    for key, value in users.items():
        print(key, value)
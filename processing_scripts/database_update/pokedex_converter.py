import json
from ignore_list import isIgnored
from legacy_renamer import find_old_name, to_model_form, find_new_name, entry_name
import utils
from utils import get_form, get_pokemon, get_species, default_or_latest, get_pokemon_index, url_to_id
from moves_converter import convert_old_move_name
import os
from glob import glob
import shutil
from types import SimpleNamespace

MEGA_SUFFIX = [
    '-mega',
    '-mega-x',
    '-mega-y',
    '-primal',
    '-battle-bond',
    '-ash',
]
GMAX_SUFFIX = [
    '-gmax',
    '=eternamax',
]

def is_mega(name):
    for suf in MEGA_SUFFIX:
        if name.endswith(suf):
            return True
    return False

def is_gmax(name):
    for suf in GMAX_SUFFIX:
        if name.endswith(suf):
            return True
    return False


index_map = get_pokemon_index()

class PokedexEntry:
    def __init__(self, forme, species) -> None:
        self.init_simple(forme, species)
        self.init_stats(forme)
        self.init_types(forme)
        self.init_abilities(forme)
        self.init_moves(forme)

    def add_models(self, models):
        self.models = models

    def post_process_evos(self, forme, species):
        evolutions = []
        if 'evolutions' in self.__dict__:
            evolutions = self.evolutions
            for evo in evolutions:
                name = evo['name']
                new_name = find_new_name(name, index_map.keys())
                if new_name is None:
                    print(f'unknown evo: {name}')
                else:
                    evo['name'] = new_name
                if 'evoMoves' in evo:
                    args = evo['evoMoves'].split(',')
                    moves = []
                    for move in args:
                        name = convert_old_move_name(move)
                        if name is not None:
                            moves.append(name)
                    if len(moves) > 0:
                        evo['evoMoves'] = str(moves).replace('[', '').replace(']', '').replace("'", '')
                    pass

        return

    def init_simple(self, forme, species):
        self.name = entry_name(forme.name)
        self.names = species.names
        self.id = forme.id
        self.stock = True
        if is_mega(self.name):
            self.mega = True
        if is_gmax(self.name):
            self.gmax = True
        self.base_experience = forme.base_experience
        self.size = {'height': forme.height/10.0}
        self.mass = forme.weight/10.0
        self.is_default = forme.is_default
        self.capture_rate = species.capture_rate

        if 'base_happiness' in species.__dict__ and species.base_happiness != None:
            self.base_happiness = species.base_happiness
        else:
             self.base_happiness = 70
        self.growth_rate = species.growth_rate.name
        gender = species.gender_rate
        if gender == 0:
            self.gender_rate = 0
        elif gender == 1:
            self.gender_rate = 30
        elif gender == 2:
            self.gender_rate = 62
        elif gender == 4:
            self.gender_rate = 127
        elif gender == 6:
            self.gender_rate = 191
        elif gender == 7:
            self.gender_rate = 225
        elif gender == 8:
            self.gender_rate = 254
        else:
            self.gender_rate = 255

    def init_stats(self, forme):
        self.stats = {}
        self.evs = {}
        for stat in forme.stats:
            name = stat.stat.name
            name = name.replace('-', '_')
            self.stats[name] = stat.base_stat
            if stat.effort!=0:
                self.evs[name] = stat.effort

    def init_types(self, forme):
        self.types = []
        for type in forme.types:
            name = type.type.name
            name = name.replace('-', '_')
            if not name in self.types:
                self.types.append(name)

    def init_abilities(self, forme):
        normal = []
        hidden = []
        abilities = {}
        for ability in forme.abilities:
            name = ability.ability.name
            if ability.is_hidden:
                hidden.append(name)
            else:
                normal.append(name)
        if len(normal) > 0:
            abilities['normal'] = normal
        if len(hidden) > 0:
            abilities['hidden'] = hidden
        if len(abilities) > 0:
            self.abilities = abilities 

    def init_moves(self, forme):
        moves = {}
        level_up = []
        misc = []

        move_levels = {}

        def is_levelup(details):
            return details.move_learn_method.name == 'level-up'

        for move in forme.moves:
            name = move.move.name

            level_up_details = default_or_latest(move.version_group_details, is_levelup)

            if level_up_details is not None:
                level = level_up_details.level_learned_at
                entry = {
                    'L':level,
                    'moves': []
                }
                key = str(level)
                if key in move_levels:
                    entry = move_levels[key]
                else:
                    move_levels[key] = entry
                    level_up.append(entry)
                entry['moves'].append(name)
            for details in move.version_group_details:
                if details == level_up_details:
                    continue
                if not name in misc:
                    misc.append(name)

        if len(level_up) > 0:
            moves['level_up'] = level_up
        if len(misc) > 0:
            moves['misc'] = misc

        if len(moves) > 0:
            self.moves = moves

class PokemonSpecies:
    def __init__(self, species, dex) -> None:
        self.species = species
        self.species_id = species.id

        numbers = []
        default = -1
        for value in species.varieties:

            if isIgnored(value.pokemon.name):
                continue

            id = url_to_id(value.pokemon)
            if value.is_default:
                default = id
            numbers.append(id)

        base = get_pokemon(default)
        numbers.remove(default)
        self.formes = []
        self.formes.append(base)
        for id in numbers:
            forme = get_pokemon(id)
            if forme is None:
                print(f'error with form {id} for {species.name}')
            else:
                self.formes.append(forme)

        added = []

        self.names = []
        self.entries = []
        for forme in self.formes:
            entry = PokedexEntry(forme, species)

            model_name = to_model_form(forme.name, species, dex)
            if(model_name is not None):
                # We need to handle this to the older model added?
                continue

            old_name = find_old_name(forme.name, species, dex)
            if(old_name is not None):

                if(old_name in added):
                    print(f'Skipping duplicate {old_name} -> {entry.name}')
                    continue

                old_entry = dex[old_name]
                added.append(old_name)

                if 'model' in old_entry:
                    entry.model = old_entry['model']
                if 'male_model' in old_entry:
                    entry.male_model = old_entry['male_model']
                if 'female_model' in old_entry:
                    entry.female_model = old_entry['female_model']

                if 'models' in old_entry:
                    entry.add_models(old_entry['models'])
                elif len(forme.forms) > 1:
                    models = []
                    # Automatically make and add models for each forme if multiple
                    for form in forme.forms:
                        name = form.name
                        id = url_to_id(form)
                        form = get_form(id)
                        key = name
                        model = {
                            'key': key,
                            'tex': key,
                            'model': key,
                            'anim': key,
                        }
                        # For now hard code in for arceus/silvally that we replace model and anim
                        if entry.name == 'silvally' or entry.name == 'arceus':
                            model['model'] = entry.name
                            model['anim'] = entry.name
                            model['key'] = key.replace('-', '_')
                            model['tex'] = key.replace('-', '_')

                        types = ''
                        for type in form.types:
                            types = types + ' '+ type.type.name
                        types = types.strip().replace(' ', ',')
                        if len(types)>0:
                            model['types'] = types

                        if form.is_default:
                            entry.model = model
                        else:
                            models.append(model)
                        
                    entry.add_models(models)

                if not "moves" in entry.__dict__ and 'moves' in old_entry:
                    entry.__dict__['moves'] = old_entry['moves']

                if 'stats' in old_entry:
                    stats = old_entry['stats']
                    if 'sizes' in stats:
                        def convert_size(old):
                            size = {}
                            if 'height' in old['values']:
                                size['height'] = float(old['values']['height'])
                            if 'width' in old['values']:
                                size['width'] = float(old['values']['width'])
                            if 'length' in old['values']:
                                size['length'] = float(old['values']['length'])
                            return size
                        entry.size = convert_size(old_entry['stats']['sizes'])
                    if 'spawnRules' in stats:
                        entry.spawn_rules = stats['spawnRules']
                    if 'megaRules' in stats:
                        entry.mega_rules = stats['megaRules']
                    if 'interactions' in stats:
                        entry.interactions = stats['interactions']
                    if 'evolutions' in stats:
                        entry.evolutions = stats['evolutions']
                else:
                    print(f'no stats for {entry.name}??')
            else:
                print(f'"{entry.name}" : "",')

            if(not '-gmax' in entry.name and (not 'moves' in entry.__dict__ or not 'level_up' in entry.moves)):
                print(f'No moves for {entry.name}??')
                pass

            entry.post_process_evos(forme, species)

            entry.id = default
            if old_name in dex and old_name!=forme.name:
                entry.old_name = old_name

            self.entries.append(entry)
            self.names.append(entry.name)

def convert_assets():
    other = [y for x in os.walk("./old/assets") for y in glob(os.path.join(x[0], '*'))]

    for file in other:

        if(os.path.isdir(file)):
            continue

        head = os.path.split(file)[0]
        name = os.path.split(file)[1]
        s = '_s.' if '_s.' in name else '.'
        
        ind = name.index(s)
        end = name[ind:]
        start = name[0:ind]

        new_name = find_new_name(start, index_map.keys())
        if new_name is not None and new_name!=start:
            start = new_name

        name = os.path.join(head.replace('old', 'new'), start+end)
        if not os.path.exists(os.path.dirname(name)):
            os.makedirs(os.path.dirname(name))
        shutil.copy(file, name)

def convert_tags():
    jsons = [y for x in os.walk("./old/tags") for y in glob(os.path.join(x[0], '*.json'))]
    for file in jsons:
        json_in = open(file, 'r', encoding='utf-8')
        json_str = json_in.read()
        json_in.close()
        json_obj = json.loads(json_str)

        if 'values' in json_obj:
            old_values = json_obj['values']

            new_values = []
            for name in old_values:
                orig = name
                name = name.replace('pokecube:', '')
                new_name = find_new_name(name, index_map.keys())
                if new_name is not None:
                    new_name = f'pokecube:{new_name}'
                    if not new_name in new_values:
                        new_values.append(new_name)
                elif 'arceus_' in name or 'silvally_' in name:
                    pass
                else:
                    if not "#" in name and not ":" in name and name != 'egg':
                        print(f"error finding name for {name} in {file}")
                    if not orig in new_values:
                        new_values.append(orig)
            json_obj['values'] = new_values

        file = file.replace('old', 'new')
        if not os.path.exists(os.path.dirname(file)):
            os.makedirs(os.path.dirname(file))
        file = open(file, 'w')
        json.dump(json_obj, file, indent=2)
        file.close()


def convert_pokedex():

    old_pokedex = './old/pokemobs/pokemobs.json'
    file = open(old_pokedex, 'r')
    data = file.read()
    file.close()
    old_pokedex = json.loads(data)
    pokedex = {}
    for var in old_pokedex["pokemon"]:
        pokedex[var["name"]] = var

    moves_dex = './old/pokemobs/pokemobs_moves.json'
    file = open(moves_dex, 'r')
    data = file.read()
    file.close()
    moves_dex = json.loads(data)

    tables = './data/pokemobs/loot_tables.json'
    file = open(tables, 'r')
    data = file.read()
    file.close()
    _loot_tables = json.loads(data)
    tables = './data/pokemobs/held_tables.json'
    file = open(tables, 'r')
    data = file.read()
    file.close()
    _held_tables = json.loads(data)

    loot_tables = {}
    for key, list in _loot_tables.items():
        for name in list:
            loot_tables[name] = key
    held_tables = {}
    for key, list in _held_tables.items():
        for name in list:
            held_tables[name] = key

    def convert_moves(old_moves, name):
        level_up_old = old_moves['lvlupMoves']

        misc_old = []

        errored_move = False

        if 'misc' in old_moves and 'moves' in old_moves['misc']:
            misc_old = old_moves['misc']['moves'].split(',')

        misc_old = [convert_old_move_name(x) for x in misc_old]
        while None in misc_old:
            misc_old.remove(None)
            errored_move = True
        moves = {}

        level_up = []

        if 'values' in level_up_old:
            level_up_old = level_up_old['values']

        for key, item in level_up_old.items():
            level = int(key)
            vars = item.split(',')
            vars = [convert_old_move_name(x) for x in vars]
            while None in vars:
                vars.remove(None)
                errored_move = True
            move = {}
            move['L'] = level
            move['moves'] = vars
            level_up.append(move)

        if errored_move:
            print(f'error with move for {name}')

        if(len(level_up)>0):
            moves['level_up'] = level_up
        if(len(misc_old)>0):
            moves['misc'] = misc_old
        return moves

    for var in moves_dex["pokemon"]:
        pokedex[var["name"]]['moves'] = convert_moves(var['moves'], var["name"])

    i = 1
    values = get_species(i)
    species = []
    dex = []

    lang_files = {}

    # Initialise this with missingno.
    pokemob_tag_names = ["pokecube:missingno"]

    while values is not None:
        entry = PokemonSpecies(values, pokedex)
        species.append(entry)
        for var in entry.entries:

            tag_name = f'pokecube:{var.name}'
            if not tag_name in pokemob_tag_names:
                pokemob_tag_names.append(tag_name)

            if var.name in held_tables:
                var.held_table = held_tables[var.name]
            if var.name in loot_tables:
                var.loot_table = loot_tables[var.name]

            for name in var.names:
                _name = name.name
                lang = utils.get('language', url_to_id(name.language))
                key = f'{lang.iso639}_{lang.iso3166}.json'
                items = {}
                if key in lang_files:
                    items = lang_files[key]
                items[f"entity.pokecube.{var.name}"] = _name
                lang_files[key] = items
            del var.names
            dex.append(var.__dict__)
        i = i + 1
        values = get_species(i)


    # Now lets handle anything defined as a "custom entry"
    custom = './data/pokemobs/custom_entries.json'
    file = open(custom, 'r')
    data = file.read()
    file.close()
    custom = json.loads(data)
    for var in custom["values"]:
        tag_name = f'pokecube:{var["name"]}'
        if not tag_name in pokemob_tag_names:
            pokemob_tag_names.append(tag_name)
        if "names" in var:
            for name in var["names"]:
                _name = name["name"]
                _id = int(name["language"]['url'].split('/')[-2])
                lang = utils.get('language', _id)
                key = f'{lang.iso639}_{lang.iso3166}.json'
                items = {}
                if key in lang_files:
                    items = lang_files[key]
                items[f"entity.pokecube.{var['name']}"] = _name
                lang_files[key] = items
            del var['names']
        dex.append(var)

    # Construct and output the default pokecube:pokemob tag
    file = f'../../src/generated/resources/data/pokecube/tags/entity_types/pokemob.json'
    var = {"replace": False,"values":pokemob_tag_names}
    if not os.path.exists(os.path.dirname(file)):
        os.makedirs(os.path.dirname(file))
    file = open(file, 'w')
    json.dump(var, file, indent=2)
    file.close()

    for key, dict in lang_files.items():
        # Output a lang file for the entries
        file = f'../../src/generated/resources/assets/pokecube_mobs/lang/{key}'
        if not os.path.exists(os.path.dirname(file)):
            os.makedirs(os.path.dirname(file))
        try:
            file = open(file, 'w', encoding='utf-8')
            json.dump(dict, file, indent=2, ensure_ascii=False)
            file.close()
        except Exception as err:
            print(f'error saving for {key}')
            print(err)

    for var in dex:
        # Output each entry into the appropriate database location
        file = f'../../src/generated/resources/data/pokecube_mobs/database/pokemobs/pokedex_entries/{var["name"]}.json'
        if not os.path.exists(os.path.dirname(file)):
            os.makedirs(os.path.dirname(file))

        file = open(file, 'w')
        json.dump(var, file, indent=2)
        file.close()

        # Now lets make a template file which will remove each entry.
        file = f'../../example_datapacks/_removal_template_/data/pokecube_mobs/database/pokemobs/pokedex_entries/{var["name"]}.json'
        var = {"remove": True,"priority":0}
        if not os.path.exists(os.path.dirname(file)):
            os.makedirs(os.path.dirname(file))
        file = open(file, 'w')
        json.dump(var, file, indent=2)
        file.close()


if __name__ == "__main__":
    convert_pokedex()
    convert_tags()
    convert_assets()
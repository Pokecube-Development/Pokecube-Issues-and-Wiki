import json
from ignore_list import isIgnored
from legacy_renamer import find_old_name, to_model_form, find_new_name, entry_name, banned_form
import utils
from utils import get_form, get_pokemon, get_species, default_or_latest, get_pokemon_index, url_to_id
from moves_converter import convert_old_move_name
from advancements_generator import make_advancments
import os
from glob import glob
import shutil

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
    '-eternamax',
]

NO_SHINY = [
    'vivillon'
]

def no_shiny(name):
    for suf in NO_SHINY:
        if name == suf:
            return True
    return False

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

# This class is a mirror of the json data structure that pokecube uses for loading
class PokedexEntry:
    def __init__(self, forme, species) -> None:
        self.init_simple(forme, species)
        self.init_stats(forme)
        self.init_types(forme)
        self.init_abilities(forme)
        self.init_moves(forme)

    # Presently this just sets the models
    def add_models(self, models):
        self.models = models

    # Post processes converting old evolutions, to convert the name and evo move names
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

    # Basic set of initialising, covering names, happiness, etc
    def init_simple(self, forme, species):
        self.name = entry_name(forme.name)
        self.names = species.names
        self.id = forme.id
        self.stock = True
        if is_mega(self.name):
            self.mega = True
        if is_gmax(self.name):
            self.gmax = True
        if no_shiny(self.name):
            self.no_shiny = True
        self.base_experience = forme.base_experience

        # These values are reported as 10x the value in the games for some reason.
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

        # Convert gender rate to the format pokecube uses
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

    # Initialise the stats and EV yields
    def init_stats(self, forme):
        self.stats = {}
        self.evs = {}
        for stat in forme.stats:
            name = stat.stat.name
            name = name.replace('-', '_')
            self.stats[name] = stat.base_stat
            if stat.effort!=0:
                self.evs[name] = stat.effort

    # Initialises the array of types
    def init_types(self, forme):
        self.types = []
        for type in forme.types:
            name = type.type.name
            name = name.replace('-', '_')
            if not name in self.types:
                self.types.append(name)

    # Initialises the normal and hidden abilities
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

    # Initialises movesets
    def init_moves(self, forme):
        moves = {}
        level_up = []
        misc = []

        move_levels = {}

        # Used to check if learn method is level up.
        def is_levelup(details):
            return details.move_learn_method.name == 'level-up'

        for move in forme.moves:
            name = move.move.name

            # See if level up data exists, default means using USUM movesets, as are most complete
            level_up_details = default_or_latest(move.version_group_details, is_levelup)

            # If we have such movesets, use them
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

            # All other moves get added to misc moves for TMs in pokecube
            for details in move.version_group_details:
                if details == level_up_details:
                    continue
                if not name in misc:
                    misc.append(name)

        # Add the moves if we found any
        if len(level_up) > 0:
            moves['level_up'] = level_up
        if len(misc) > 0:
            moves['misc'] = misc

        if len(moves) > 0:
            self.moves = moves

class PokemonSpecies:
    def __init__(self, species, dex, custom_moves, custom_sizes) -> None:
        self.species = species
        self.species_id = species.id

        numbers = []
        default = -1

        # Varieties in here are what we register as pokedex entries
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

        # Makes the pokedex entry for each one
        for forme in self.formes:
            entry = PokedexEntry(forme, species)

            model_name = to_model_form(forme.name, species, dex)
            if(model_name is not None):
                # We need to handle this to the older model added?
                continue

            # Add custom moves if assigned
            if entry.name in custom_moves:
                print(f'adding custom moves for {entry.name} from override files')
                entry.__dict__['moves'] = custom_moves[entry.name]

            if entry.name in custom_sizes:
                entry.size = custom_sizes[entry.name]

            # Check if we need to convert anything over from old ones
            old_name = find_old_name(forme.name, species, dex)
            if(old_name is not None):

                # Some things get merged, like the meteor miniors, so skip duplicates
                if(old_name in added):
                    print(f'Skipping duplicate {old_name} -> {entry.name}')
                    continue

                old_entry = dex[old_name]
                added.append(old_name)

                # Copy old model info over
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

                        if banned_form(name):
                            continue

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
                    if len(models) > 0:
                        entry.add_models(models)

                # Copy old custom values from inside stats over
                if 'stats' in old_entry:
                    stats = old_entry['stats']
                    # Same for spawns, mega rules, interactions and evolutoons
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

            # Print an error if we think it should have had moves, but has none
            if(not '-gmax' in entry.name and (not 'moves' in entry.__dict__ or not 'level_up' in entry.moves)):
                print(f'No moves for {entry.name}??')
                pass

            # Now cleanup evolutions
            entry.post_process_evos(forme, species)

            entry.id = default

            # Mark the old name for legacy supprt reasons
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

    moves_dex = './data/pokemobs/custom_movesets.json'
    file = open(moves_dex, 'r')
    data = file.read()
    file.close()
    _moves_dex = json.loads(data)
    moves_dex = {}
    for entry in _moves_dex:
        moves_dex[entry['name']] = entry['moves']

    sizes_dex = './data/pokemobs/custom_sizes.json'
    file = open(sizes_dex, 'r')
    data = file.read()
    file.close()
    _sizes_dex = json.loads(data)
    sizes_dex = {}
    for entry in _sizes_dex:
        sizes_dex[entry['name']] = entry['sizes']

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

    i = 1
    values = get_species(i)
    species = []
    dex = []

    lang_files = {}

    # Initialise this with missingno.
    pokemob_tag_names = ["pokecube:missingno"]

    sizes = []

    while values is not None:
        entry = PokemonSpecies(values, pokedex, moves_dex, sizes_dex)
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

            sizes.append({'name':var.name, 'sizes': var.size})

            dex.append(var.__dict__)
        i = i + 1
        values = get_species(i)

    # Now lets handle anything defined as a "custom entry"
    custom = './data/pokemobs/custom_entries.json'
    file = open(custom, 'r')
    data = file.read()
    file.close()
    custom = json.loads(data)
    for var in custom:
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

            sizes.append({'name':var["name"], 'sizes': var["size"]})

        dex.append(var)

    # cleanup sizes file
    copy = [x for x in sizes]
    for var in copy:
        height = var['sizes']['height'] if 'height' in var['sizes'] else 1
        width = var['sizes']['width'] if 'width' in var['sizes'] else height
        length = var['sizes']['length'] if 'length' in var['sizes'] else width
        if width == length and 'length' in var['sizes']:
            del var['sizes']['length']
        if width == height and 'width' in var['sizes']:
            del var['sizes']['width']

        if not 'height' in var['sizes']:
            var['sizes']['height'] = 1.0
        if len(var['sizes']) == 1:
            sizes.remove(var)


    # Updated the sizes file to cleanup things
    file = './data/pokemobs/custom_sizes.json'
    if not os.path.exists(os.path.dirname(file)):
        os.makedirs(os.path.dirname(file))
    file = open(file, 'w')
    json.dump(sizes, file, indent=2)
    file.close()

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

        # And also make the advancements
        make_advancments(var["name"])

        # Now lets make a template file which will remove each entry.
        file = f'../../example_datapacks/_removal_template_/data/pokecube_mobs/database/pokemobs/pokedex_entries/{var["name"]}.json'
        var = {"remove": True,"priority":0}
        if not os.path.exists(os.path.dirname(file)):
            os.makedirs(os.path.dirname(file))
        file = open(file, 'w')
        json.dump(var, file, indent=2)
        file.close()

def make_ability_langs():
    ability_index = utils.get_valid_numbers('ability')

    lang_files = {}

    for name, number in ability_index.items():
        var = utils.get('ability', number)
        for name in var.names:
            _name = name.name
            lang = utils.get('language', url_to_id(name.language))
            key = f'{lang.iso639}_{lang.iso3166}.json'
            items = {}
            if key in lang_files:
                items = lang_files[key]
            items[f"ability.{var.name}.name"] = _name
            lang_files[key] = items

    for key, dict in lang_files.items():
        # Output a lang file for the entries
        file = f'../../src/generated/resources/assets/pokecube_abilities/lang/{key}'
        if not os.path.exists(os.path.dirname(file)):
            os.makedirs(os.path.dirname(file))
        try:
            file = open(file, 'w', encoding='utf-8')
            json.dump(dict, file, indent=2, ensure_ascii=False)
            file.close()
        except Exception as err:
            print(f'error saving for {key}')
            print(err)

if __name__ == "__main__":
    convert_pokedex()
    convert_tags()
    convert_assets()
    make_ability_langs()
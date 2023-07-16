import json
import random
import os

def process(filename, root_name):
    loaded = json.load(open(filename, 'r'))
    pools = loaded['pools']

    for pool in pools:
        name = pool['name']
        options = pool['options']

        new_pool = {}
        new_pool['name'] = name
        new_pool['fallback'] = "minecraft:empty"
        elements = []
        new_pool["elements"] = elements
        projection = "rigid"

        included = False

        if "rigid" in pool:
            projection = "rigid" if pool["rigid"] else "terrain_matching"

        if "target" in pool:
            new_pool['fallback'] = pool["target"]

        for option in options:
            weight = 1
            args = option.split(";")
            location = args[0]

            element = {}

            if location == 'empty':
                element["element_type"] = "minecraft:empty_pool_element"
            else:
                element["location"] = location
                element["projection"] = projection
                element["element_type"] = "pokecube:expanded_pool_element"
                element["processors"] = "pokecube:generic"
                
                if not included and "includes" in pool:
                    included = True
                    element["extra_pools"] = pool["includes"]

            if len(args) > 1:
                extra = json.loads(args[1])
                if "weight" in extra:
                    weight = extra["weight"]
                if "flag" in extra:
                    element["flags"] = extra["flag"]
            obj = {}
            obj['element'] = element
            obj["weight"] = weight
            elements.append(obj)

        path = name.split(':')

        filename = f'./new/{path[0]}/worldgen/template_pool/{path[1]}.json'
        if not os.path.exists(os.path.dirname(filename)):
            os.makedirs(os.path.dirname(filename))
        json.dump(new_pool, open(filename, 'w'), indent=2, sort_keys=True)

        print(filename)

        # Now for the configured features.
    if not 'jigsaws' in loaded:
        return []
    jigsaws = loaded['jigsaws']
    made = []
    for jigsaw in jigsaws:
        feature = {}
        feature["type"] = "pokecube:generic_surface_jigsaw"
        feature["biomes"] = f'#pokecube_world:has_structure/{root_name}'
        feature['adapt_noise'] = True
        feature['spawn_overrides'] = {}
        config = {}
        feature['config'] = config
        config['start_pool'] = jigsaw['root']
        config['size'] = 1
        if 'size' in jigsaw:
            config['size'] = jigsaw['size']
        _name = root_name
        avoid = []
        if 'small' in jigsaw['root']:
            avoid = ["pokecube:towns","minecraft:villages","minecraft:pillager_outposts"]
        elif 'town_centers' in jigsaw['root']:
            avoid = ["minecraft:villages","minecraft:pillager_outposts"]
            _name = root_name.replace('village', 'town')
        else:
            avoid = ["pokecube:towns","pokecube:villages","minecraft:villages","minecraft:pillager_outposts"]

        config['structures_to_avoid'] = avoid
        config['avoid_range'] = 6

        if 'needed_once' in jigsaw:
            config["required_parts"] = jigsaw['needed_once']

        name = jigsaw['name']
        path = name.split(':')
        path[1] = _name
        filename = f'./new/{path[0]}/worldgen/configured_structure_feature/{path[1]}.json'
        if not os.path.exists(os.path.dirname(filename)):
            os.makedirs(os.path.dirname(filename))
        json.dump(feature, open(filename, 'w'), indent=2, sort_keys=True)

        print(filename)
        made.append(f'{path[0]}:{_name}')


        #Now make a dummy tag for this as well.
        tag = {}
        tag['replace'] = False
        tag["values"] = ["#forge:is_plains"]
        filename = f'./new/pokecube_world/tags/worldgen/biome/has_structure/{root_name}.json'
        if not os.path.exists(os.path.dirname(filename)):
            os.makedirs(os.path.dirname(filename))
        json.dump(tag, open(filename, 'w'), indent=2, sort_keys=True)



    return made

mades = []
towns = []
villages = []

for filename in os.listdir('./old'):
    print(filename)
    made = process('./old/'+filename, filename.replace('.json', ''))
    for m in made:
        if 'town' in m:
            towns.append(m)
        elif 'village' in m:
            villages.append(m)

# Structure Sets
random.seed("my random seed")
villages_set = {}
placement = {}
placement['salt'] = random.randint(0, 2147483647)
placement['spacing'] = 24
placement['separation'] = 12
placement['type'] = "minecraft:random_spread"
villages_set["placement"] = placement

structures = []
for m in villages:
    structure = {}
    structure['weight'] = 1
    structure['structure'] = m
    structures.append(structure)
villages_set["structures"] = structures

filename = f'./new/pokecube/worldgen/structure_set/villages.json'
if not os.path.exists(os.path.dirname(filename)):
    os.makedirs(os.path.dirname(filename))
json.dump(villages_set, open(filename, 'w'), indent=2, sort_keys=True)

towns_set = {}
placement['salt'] = random.randint(0, 2147483647)
placement['spacing'] = 48
placement['separation'] = 12
towns_set["placement"] = placement

structures = []
for m in towns:
    structure = {}
    structure['weight'] = 1
    structure['structure'] = m
    structures.append(structure)
towns_set["structures"] = structures

filename = f'./new/pokecube/worldgen/structure_set/towns.json'
if not os.path.exists(os.path.dirname(filename)):
    os.makedirs(os.path.dirname(filename))
json.dump(towns_set, open(filename, 'w'), indent=2, sort_keys=True)

# Feature Tags
town_tags = {}
village_tags = {}


village_tags["replace"] = False
village_tags['values'] = villages
filename = f'./new/pokecube_world/tags/worldgen/configured_structure_feature/village.json'
if not os.path.exists(os.path.dirname(filename)):
    os.makedirs(os.path.dirname(filename))
json.dump(village_tags, open(filename, 'w'), indent=2, sort_keys=True)


town_tags["replace"] = False
town_tags['values'] = towns
filename = f'./new/pokecube_world/tags/worldgen/configured_structure_feature/town.json'
if not os.path.exists(os.path.dirname(filename)):
    os.makedirs(os.path.dirname(filename))
json.dump(town_tags, open(filename, 'w'), indent=2, sort_keys=True)
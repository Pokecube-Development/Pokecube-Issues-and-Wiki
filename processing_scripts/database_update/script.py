import csv_loader
import os
import sys
import json

import pokemobs_names
import pokedex_entry
from data_files import csv_files
from unk_pokemobs import custom_names

def makeFileList():
    files = []
    for filename in os.listdir('./caches/csv/'):
        if '.csv' in filename:
            files.append(filename)
    filelist = "[\""
    for i in range(0, len(files)-1):
        filelist = filelist + files[i].replace('.csv','') + "\", \""
    filelist = filelist + files[len(files)-1] + "\"]"
    print(filelist)

def init_names_map(data):
    
    var = data.get_file("pokemon")

    unk_in_api = []
    unk_in_mod = []

    found = 0
    not_found = 0
    inv_found = 0

    replace_names = {}

    for name in pokemobs_names.pokemobs:
        _name = name.replace("_", "-")
        _name = _name.replace("gigantamax", "gmax")
        match, conf = csv_loader.match_name(_name, var.inv_map.keys())
        if conf >= 50:
            # if conf < 90:
                # print("Replacing mob: {} closest: {} confidence: {}".format(name, match, conf))
            replace_names[name] = match
            continue
        print("Skipping mob: {} closest: {} confidence: {}".format(name, match, conf))
        unk_in_mod.append(name)

    old_names = pokemobs_names.pokemobs
    pokemobs = [x if replace_names.get(x, None) is None else replace_names[x] for x in old_names]

    names_map = {}

    for key, value in var.map.items():
        name = value.rows[0][1]
        if var.inv_map.get(name, None) is not None:
            inv_found = inv_found + 1

        if name in custom_names.keys() and custom_names[name] != "NONE":
            name = custom_names[name]
        if not (name in pokemobs or name in old_names):
            unk_in_api.append(name)
            not_found = not_found + 1
        else:
            index = 0
            if name in pokemobs:
                index = pokemobs.index(name)
            else:
                index = old_names.index(name)
            pokename = old_names[index]
            if names_map.get(pokename, None) is not None:
                names_map[pokename] = names_map[pokename]+","+value.rows[0][1]
            else:
                names_map[pokename] = value.rows[0][1]
            found = found + 1

    # print("custom_names = {")
    # for name in unk_in_api:
    #     print("    \"{}\": \"NONE\"".format(name))
    # print("}")

    mapfile = open("./pokemob_name_map.csv", 'w', encoding="utf-8")

    for key, value in names_map.items():
        mapfile.write("{},{}\n".format(key, value))

    print(unk_in_api)
    print("Matched: {}, Missing: {}, Total: {}".format(found, not_found, found + not_found))
    return names_map

def init_database():
    data = csv_loader.CsvDatabase(csv_files)
    data.set_name_map(init_names_map(data))
    return data

if __name__ == "__main__":
    data = init_database()

    # names = data.names_map
    names = pokemobs_names.pokemobs
    pokedex = pokedex_entry.Pokedex(names, data)

    pokefile = open("./pokemobs_pokedex.json", 'w', encoding="utf-8")
    pokefile.write(json.dumps({"pokemon":pokedex.pokemon}, indent=2))

        

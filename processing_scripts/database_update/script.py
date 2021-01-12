import csv_loader
import os
import sys

from fuzzywuzzy import fuzz
from fuzzywuzzy import process

from pokemobs_names import pokemobs
from data_files import csv_files
from unk_pokemobs import custom_names

def makeFileList():
    filelist = "[\""
    for filename in os.listdir('./caches/csv/'):
        if '.csv' in filename:
            files.append(filename)
    for i in range(0, len(files)-1):
        filelist = filelist + files[i].replace('.csv','') + "\", \""
    filelist = filelist + files[len(files)-1] + "\"]"
    print(filelist)

def find_highest(name, list):
    matched = None
    ratio = 80

    name = name.replace("_", "-")
    name = name.replace("gigantamax", "gmax")

    for tmp in list:
        orig = tmp
        if tmp in custom_names.keys():
            tmp = custom_names[tmp]
        tmp_ratio = fuzz.ratio(tmp, name)
        if name.startswith(tmp):
            matched = orig
        if tmp_ratio >= ratio:
            ratio = tmp_ratio
            matched = orig

    if matched is not None and name.startswith(matched):
        ratio = 90

    partial_ratio = 0
    sort_ratio = 0

    if ratio < 90:
        name = name.replace("-", " ")
        for tmp in list:
            orig = tmp
            if tmp in custom_names.keys():
                tmp = custom_names[tmp]
            tmp = tmp.replace("-", " ")
            tmp_partial = fuzz.partial_ratio(tmp, name)
            tmp_sort = fuzz.token_sort_ratio(tmp, name)
            if tmp_partial > partial_ratio or tmp_sort > sort_ratio:
                matched = orig
                partial_ratio = tmp_partial
                sort_ratio = tmp_sort
                ratio = tmp_sort



    return matched, ratio

if __name__ == "__main__":

    # for i in range(len(pokemobs)):
    #     pokemobs[i] = pokemobs[i].replace("_", "-")
    #     pokemobs[i] = pokemobs[i].replace("gigantamax", 'gmax')

    file_map = {}

    for name in csv_files:
        file_map[name] = csv_loader.CsvFile(name)


    alternative_mob_names = {
        'nidoranf':'nidoran-f',
        'nidoranm':'nidoran-m'
    }

    var = file_map["pokemon"]

    unk_in_api = []
    unk_in_mod = []

    found = 0
    not_found = 0
    inv_found = 0

    replace_names = {}

    for name in pokemobs:
        if var.inv_map.get(name, None) is None:
            match, conf = find_highest(name, var.inv_map.keys())
            if conf >= 50:
                # if conf < 90:
                    # print("Replacing mob: {} closest: {} confidence: {}".format(name, match, conf))
                replace_names[name] = match
                continue
            print("Skipping mob: {} closest: {} confidence: {}".format(name, match, conf))
            unk_in_mod.append(name)

    old_names = pokemobs
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
    # print(unk_in_mod)

'''
Before running this, first run pokedex_converter.py


'''
import os
import shutil

def convert(entry_generate_dir, entry_generate_dir_new, dir_not_existing):

    if not os.path.exists(dir_not_existing):
        os.makedirs(dir_not_existing)

    for file in os.listdir(dir_not_existing):
        os.remove(dir_not_existing + file)

    for file in os.listdir(entry_generate_dir_new):
        if(os.path.exists(entry_generate_dir + file)):
            shutil.copy(entry_generate_dir_new + file, entry_generate_dir + file)
        else:
            shutil.copy(entry_generate_dir_new + file, dir_not_existing + file)

def make_entry_generate(key):
    entry_generate_dir = f'../../src/generated/resources/data/pokecube_mobs/database/pokemobs/{key}/'
    entry_generate_dir_new = f'./new/pokemobs/{key}/'
    dir_not_existing = f'./new/pokemobs/{key}_new/'
    return entry_generate_dir, entry_generate_dir_new, dir_not_existing

convert(*make_entry_generate("pokedex_entries"))
convert(*make_entry_generate("evolutions"))
convert(*make_entry_generate("mega_evos"))
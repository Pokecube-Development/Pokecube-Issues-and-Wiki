'''
Before running this, first run pokedex_converter.py


'''
import os
import shutil

entry_generate_dir = '../../src/generated/resources/data/pokecube_mobs/database/pokemobs/pokedex_entries/'
entry_generate_dir_new = './new/pokemobs/pokedex_entries/'
dir_not_existing = './new/pokemobs/pokedex_entries_new/'

if not os.path.exists(dir_not_existing):
    os.makedirs(dir_not_existing)

for file in os.listdir(dir_not_existing):
    os.remove(dir_not_existing + file)

for file in os.listdir(entry_generate_dir_new):
    if(os.path.exists(entry_generate_dir + file)):
        shutil.copy(entry_generate_dir_new + file, entry_generate_dir + file)
    else:
        shutil.copy(entry_generate_dir_new + file, dir_not_existing + file)
    
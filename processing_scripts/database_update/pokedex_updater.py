'''
Before running this, first run pokedex_converter.py


'''
import os
import shutil

entry_generate_dir = '../../src/generated/resources/data/pokecube_mobs/database/pokemobs/pokedex_entries/'
entry_generate_dir_new = './new/pokemobs/pokedex_entries/'

for file in os.listdir(entry_generate_dir_new):
    if(os.path.exists(entry_generate_dir + file)):
        shutil.copy(entry_generate_dir_new + file, entry_generate_dir + file)
    
# PokeAPI Based database generation

This is a collection of python scripts for generating database entries from a combination of data found at https://github.com/PokeAPI/api-data and custom files in `./data/`

## Initial setup

First make a folder `./.cache`, and then populate it from https://github.com/PokeAPI/api-data

This population can be done via the git command: `git clone https://github.com/PokeAPI/api-data.git` from inside the `./.cache` directory.

To update later, just pull changes into that git repository.

## Adding new pokemobs

1. To add a new pokemob, first attempt to generate the entry for it automatically. This is done by running `pokedex_converter.py` in python. It should produce files in `./new/`, and pokedex entry files in `./new/pokemobs/pokedex_entries`, go there to find the file you are trying to add.

2. Next check for what was not correctly generated in the file. Often this is things like evolutions, custom interactions, base exp, etc. These can be fixed by editing the files in `./data/pokemobs/`. The readme in that directory should contain information about where to put values for each entry missing.

3. Once you have adjusted the values in `./data/pokemobs/`, return to step 1, and repeat steps 1-3 until the json file contains the required information.

4. Now that you have the proper json file, place it in `../../src/generated/resources/data/pokecube_mobs/database/pokemobs/pokedex_entries/`, and it should be ready to add.


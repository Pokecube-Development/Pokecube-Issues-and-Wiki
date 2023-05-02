# Custom values for pokedex entries.

These files are used for making custom adjustments to what go in the database files, beyond what is auto-generated.

## Pokemob item drops and held items:

- `loot_tables.json` - use this file to add item drops for pokemobs, it is a map of loot table -> list of mobs that get that loot table
-  `held_tables.json` - use this file to add held items for pokemobs, it is a map of loot table -> list of mobs that get held items from that loot table

## Evo Moves and base EXP fixing

- `evo_moves.json` - use this to add evo moves, it is a map of mob -> move, where mob is the pokemob that should learn the move, ie the pokemob evolved to.
-  `fix_base_exp.json` - this is a map of pokemob -> base exp, it will be used if the base exp of the mob is not in the database

## Misc other things

For other things, add them do the appropriate looking `custom_<thing>.json`, the format in there goes exactly as it would in the regular database files, and they should contain examples of a variety of adjustments.
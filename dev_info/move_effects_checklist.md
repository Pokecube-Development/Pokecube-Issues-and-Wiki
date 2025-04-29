# Field Move Effects
## Custom World Actions
- [ ] Hunger cost scales with number of blocks broken for ones which affect blocks
- [ ] Hunger cost scales down with level, gets cheaper for stronger mobs
- [ ] For broken blocks, a diamond pickaxe is simulated, with enchantment of the book in the pokemob's offhand slot
### Bulldoze
- [ ] Flattens terrain around where it is used, in a 3x3 area
### Cut
- [ ] Cuts all logs in the tree if used on a log on dirt
- [ ] Cuts a 3x3x3 volume of leaves/plants if used otherwise
### Dig
- [ ] Digs out a 3x3x3 cube, centered on the target
### Flash
- [ ] Applies night vision effect to owner of the user
### Nature Power
- [ ] Changes biome around the use target
  - [ ] Configured via datapack entries in `database/nature_power`
### Payday
- [ ] Spawns items from the loot table `pokecube_mobs:moves/payday`
### Rock Throw
- [ ] Places a cobblestone
### Secret Power
- [ ] Prompts player to click in chat to make the secret base
  - [ ] Only if used on certain terrain blocks (Terrain and Logs)
- [ ] When clicked, makes a SecretBase block with the player linked to it
### Rock Smash
- [ ] Breaks rocks in a 3x3x3 cube, centered on the target
### Teleport
- [ ] Teleports owner to selected teleport location
  - [ ] recalls self and nearby owned mobs before teleport
## Crafting
- [ ] Defined by recipes of type `pokecube:move_recipe`, defaults placed in `data/pokecube_moves/recipe`
- [ ] Applies the shapeless recipe on items on the ground near the move target
- [ ] Applies the shapeless recipe to the specific block on world used on
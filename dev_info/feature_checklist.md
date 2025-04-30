# Checklist of functionality

# Mobs and Entities
## Battling
### Primary Pokemob Combat
- [ ] Stay in general area while fighting
- [ ] Wild mobs occasionally switch move used
- [ ] Attempt to dodge incoming attacks
  - [ ] Rate scales with evasion modifiers
  - [ ] attacker accuracy reduces success of dodge
- [ ] Leap at targets to use melee attacks
- [ ] Combat attack use tests
  - [ ] Melee attack use
      - [ ] Flying attackers
      - [ ] Flying targets
  - [ ] Ranged attack use
    - [ ] Flying attackers
    - [ ] Flying targets
- [ ] Pokemobs draw agro from wild mobs
- [ ] Pokemobs on stay do not draw agro
- [ ] Guarding pokemobs auto-agro things
- [ ] Wild pokemobs call for help if in a group
- [ ] Gives up if not seen target for too long
### Battle Gimmicks
#### Mega evolution
- [ ] Mobs mega evolve if conditions met
  - [ ] Owner wearing mega item
  - [ ] Mob valid for mega evolve (has item, move, etc)
- [ ] Mega revert on recall
#### Dynamax
- [ ] Mobs dynamax if conditions met
  - [ ] Owner wearing mega item
  - [ ] Dynamax power spot nearby
- [ ] If it has a gmax form, and gmax gene, gmax instead
- [ ] Moves change to D or G max variants
#### Terastal
- [ ] Mobs terastallize if conditions met
  - [ ] Owner wearing mega item
  - [ ] Mobs valid for terastal (set in gui)
#### Z-Moves
- [ ] Does not work if mob is dynamaxed
- [ ] Pokemob holding appropriate z-crystal
### Primary NPC Combat
- [ ] Npc combat near a pokecenter (should not work)
- [ ] npc combat
  - [ ] NPC targets pokemobs of enemy, if present, otherwise enemy
  - [ ] NPC switches moves used of their mobs
  - [ ] NPC sometimes mega evolves their mobs
  - [ ] defeat rewards
  - [ ] cooldowns
  - [ ] nbt edit notifyDefeat to try, confirm npc "vanishes" when on cooldown
- [ ] gym leaders
- [ ] NPCs battle wild pokemobs
  - [ ] NPC pokemobs level up if config allows
- [ ] NPCs with low mob count try to capture wild pokemobs
### Secondary Pokemob Combat
- [ ] Status effects
- [ ] Stat effects
- [ ] Abilities
- [ ] Held item use
- [ ] Held item effects
- [ ] Stat effects reset after combat
## Pokecubes
### Capturing Pokemobs
- [ ] Capture a wild pokemob
- [ ] unable to capture a tame pokemob
- [ ] capture rates for different cubes
- [ ] Cooldown on capturing, delay between attempts where cube bounces off
#### Legendary Restrictions
- [ ] Configured legends can't be captured if requirements not met
### Loot cubes
- [ ] give loot from fixed items
- [ ] give loot from loot tables
- [ ] not visible after being looted
### Animations
- [ ] Capture animations
- [ ] Send out animations
## Pokemob AI, Non Battle
### Main AI set
#### Owner related
- [ ] Follow owner
- [ ] Do not follow owner on sit or stay
- [ ] Stop sitting if not on stay and owner attacked
- [ ] Uses attacks on blocks when commanded to
  - [ ] Leaps at target location when melee attack
#### Idle wandering
- [ ] Random wandering unless disabled in gui
- [ ] Randomly sits down (if wild or on stay)
- [ ] Randomly stops sitting (if wild or on stay)
#### Hunger System
- [ ] Hunger slowly increments
- [ ] Eats berries (or other tagged food) from inventory when hungry
  - [ ] Eats berries from nearby trapped chests
- [ ] Plants restore hunger in sunlight
- [ ] Optionally eats other allowed world blocks if the mob can
  - [ ] Restores hunger in water for fish
  - [ ] Restores hunger near redstone blocks for some
  - [ ] Eats rocks
  - [ ] Eats plants
- [ ] hunts for food if no berries found, and has hunt targets
- [ ] fish swim towards fish hooks
- [ ] Sleeps at appropriate times of day
#### Storage and Gathering
- [ ] Puts excess inventory in linked storage
  - [ ] Respects listed slot access
- [ ] Gathers nearby dropped items
- [ ] Gathers nearby gatherable blocks
  - [ ] Replants seeds, etc if possible
#### Other
- [ ] Mates with nearby mobs when not hungry
  - [ ] Fights over mates if too many nearby
  - [ ] Losers give up fighting instead of fighting to death
  - [ ] Mother guards egg
    - [ ] Attack anyone who picks up egg
    - [ ] Stop attacking if egg placed back
### Gimmick AI Set
#### Ant Nests
- [ ] Ants build nests
  - [ ] Rooms for eggs
  - [ ] Rooms for farms
- [ ] Ants move eggs between egg rooms
- [ ] Ants guard nest
- [ ] Ants gather items/blocks in/around the nest
- [ ] Gathered items are stored in the nest block
- [ ] Ants sleep in the nest block at night
- [ ] Ants enter nest block when raining
#### Burrows
- [ ] Burrowing mobs dig burrows
- [ ] Eggs spawn in the burror, relevant to the mob
- [ ] Mobs stay near their burrow
#### Bees
- [ ] Bees make beehives
- [ ] Bees enter beehives
- [ ] Bees exit beehives
- [ ] Bees gather nectar from nearby flowers
#### Builders
- [ ] Builders build structures
  - [ ] Creative mode owner needs no items
  - [ ] Survival mode owner needs items
#### Shoulder riding
- [ ] Allowed mobs jump on shoulder occasionally when happy
- [ ] Randomly jump off shoulder, after somewhat long delay
  - [ ] Instantly jump off shoulder when recall key pressed
## Pokemob Eggs
- [ ] Laid by mating pokemobs
- [ ] Hatch after delay
  - [ ] Sped up by flame body, etc
- [ ] Imprint to owned by nearest mob (within a few blocks)
  - [ ] If mob is owned, imprint to owner of that mob instead
## Spawning
### Pokemobs
- [ ] Spawn in configured areas
  - [ ] Respect blacklists
### Structures
- [ ] All structures generate
  - [ ] Do not generate overlapping

# Blocks and BlockEntities
## Pokecenter
- [ ] Can heal mobs
- [ ] Removes status effects
## PC
- [ ] Items go to PC on death
- [ ] PC only allows configured items
- [ ] Boxes are renamable, persist on servers
- [ ] Button for releasing pokemobs
  - [ ] Click to enable releasing
  - [ ] Click an item to mark/unmark
  - [ ] Click confirm to delete marked
## Trading
- [ ] Can trade mobs
- [ ] Trade with empty cube replaces cube (destroys old one)
- [ ] Can apply pokeseals
- [ ] Marks pokemobs as traded for evolution
## TM Machine
- [ ] Applies move to TM
- [ ] Displays currently known moves
- [ ] Displays moves for lower level
- [ ] Displays "forgotten" moves
## Berries
- [ ] Berries plant to crops
- [ ] Crops grow
  - [ ] Some grow to trees
    - [ ] Leaves place berries underneath
    - [ ] Sheared leaves do not make berries
    - [ ] Sheared leaves do not decay
  - [ ] Some place berry on top
  - [ ] Growth drops a stage when berry picked
    - [ ] re-grows after
## Repels
- [ ] Prevents nearby spawns
- [ ] Prevents field move use
- [ ] Range adjustable by right clicking with berries
## Secret Bases
- [ ] Made using secret power
- [ ] Teleports to owner's base
- [ ] NBT edit `any` to teleport to user's base instead
- [ ] Bases show up on the base radar in watch 
## Genetics blocks
- [ ] Use and attach to energy sources
### Extracting
- [ ] Extracts genes from mobs in pokecubes
- [ ] Extracts genes from eggs
- [ ] Extracts genes from dna bottles
- [ ] Extracts genes from recipes
- [ ] Respects selector for extraction
- [ ] Only removes extracted genes from the source
### Splicing
- [ ] Merges genes together
- [ ] Mutations can occur on merging
### Cloning
- [ ] Revives from eggs
- [ ] Revives from dna bottles
- [ ] Revives from recipes
- [ ] Configured remaining items remain
## Energy siphon
- [ ] Makes forge energy from nearby electric types/wormholes
- [ ] Extracting the energy increases hunger in pokemob extracted from
- [ ] Supplies energy to nearby blocks
- [ ] Wireless links for supplying energy to distant blocks
## Warp Pad
- [ ] Consumes energy to warp
- [ ] Linkable to arbitrary location
- [ ] Works across dimensions
## Statue
- [ ] Renders mob
- [ ] Renders overlay texture
- [ ] Renders animation in single frame
- [ ] Powered and fueled adjusts spawn rates nearby
## Daycare
- [ ] Consumes fuel from hoppers
- [ ] Gives exp to nearby mobs
## AFA
- [ ] Consumes energy to run
- [ ] Applies field abilities nearby
- [ ] Applies shiny charm effect (difficut to test, as just doubles rate)
## Commander
- [ ] Links to a pokemob
- [ ] Tells pokemob to attack location
- [ ] Tells pokemob to move to location
- [ ] Tells pokemob to set move index
## Raid Spawner
- [ ] Right click with wishing piece to make active
  - [ ] Alternately wait for it to randomly activate
- [ ] Raid mob shows a boss bar to nearby players
- [ ] Raid boss stays dead on ground when fainted, allows capture
- [ ] Raid boss drops random loot from appropriate loot table
## Mirage Spot
- [ ] right click middle to spwan a random mob
  - [ ] Resets after configured delay
  - [ ] If nbt has despawns set to true, despawns after configured delay (Middle tile needs the nbt)
# Items
## Pokewatch
- [ ] Shift right click on pokecenter block to add as a teleport location
- [ ] Right click on a pokemob to open for that pokemob (if owned)
   - [ ] Marks the pokemob as insepcted otheriwse, clearing obfuscated name
### Start
- [ ] Selectable pokemob to render
  - [ ] Button for sound playing
  - [ ] Allows changing gender
  - [ ] Allows changing forme
  - [ ] Allows changing shininess
- [ ] Buttons to other pages
### Pokemob info
- [ ] Render of the mob
- [ ] Buttons like Start for form/sound/etc 
  - [ ] If used on mob, only sound button works
#### Stats
- [ ] Base stats shown if not used on mob
- [ ] Specific stats shown if used on mob
  - [ ] Shows EVs
  - [ ] Shows IVs
  - [ ] Shows nature
  - [ ] Updates based on stat modifiers
#### Battle
- [ ] Blank if not used on mob
- [ ] Shows details if used on mob
  - [ ] Ability
  - [ ] Size
  - [ ] Nature
  - [ ] Happiness message
#### Moves
- [ ] Move tooltips show damage and stat used
- [ ] List of learnable moves if not used on mob
  - [ ] First showing level
  - [ ] Then TM listing
- [ ] Mob's moves if used on mob
  - [ ] Drag and drop to re-order
  - [ ] 5th slot showing forgotten/learnable moves
    - [ ] Scroll wheel to cycle the list
    - [ ] Drag to learn move
    - [ ] Forgotten move gets added to the list
#### Spawns
- [ ] Description of each registered spawn for the entry
- [ ] Updates to show form spawns if form button pressed
#### Breeding
- [ ] List of related mobs
- [ ] Clicking value in list takes to page for that mob
#### Description
- [ ] Flavour text about the mob
- [ ] Type information about the mob
- [ ] Evolution details
  - [ ] Clicking the name of an evolution takes to page for it
### Wiki page
- [ ] Displays some basic information books
  - [ ] Links take to sections of book
### Local Spawns
- [ ] Scrollable list of spawns
  - [ ] Sorted by rate
  - [ ] Location sensitive to where player is standing
  - [ ] Clicking a name takes to the Pokemob Info page for it
- [ ] Message if not able to spawn
### Trainer Career
#### Total Progress
- [ ] Button to check progress to get rewards
- [ ] Lists stats regarding capture info
- [ ] Lists number of mobs nearby
#### Per type Progress
- [ ] Similar to total, but per type
- [ ] Box for searching for the type
#### Per Mob Progress
- [ ] Similar again, but specific pokedex entry
- [ ] Box for selecting the entry
### Teleport Locations
- [ ] List of registered teleports
- [ ] Allows deleting teleports
- [ ] Allows renaming teleports
- [ ] Allows re-ordering teleports
### Radar page
#### Secret Base
- [ ] Shows nearby secret bases
#### Meteor compass
- [ ] Shows nearest meteor
#### Spawn Inhibitor
- [ ] If in range of a repel, nest, etc, shows where it is
## Pokedex
- [ ] Lookup of pokemob by name
- [ ] Displays description of pokemob, similar to watch Pokemob Info description page
- [ ] Shift right click on pokecenter block to add as a teleport location
- [ ] Right click on a pokemob to open for that pokemob
   - [ ] Marks the pokemob as insepcted, clearing obfuscated name
## Bag
- [ ] Only allows configured items
- [ ] Pages can be renamed
- [ ] Search colours matching items
## Berries
- [ ] Can be fed to pokemobs
- [ ] Apply effects on fed
  - [ ] EV berries reduce EVs
  - [ ] Healing berries heal
  - [ ] Status berries clear status
## TMs
- [ ] Adds mapped move to learnable moves shown in the watch's move page
## Vitamins
- [ ] Increases the EVs for the mob
## Revive
- [ ] Craftable with dead pokemob to revive it
- [ ] Right click on a fainted wild pokemob to revive it (in time before it poofs)

# Moves Effects
See [Moves](move_effects_checklist.md) 
# Ability Effects
See [Abilities](ability_effects_checklist.md)
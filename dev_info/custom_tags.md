# Custom Tags

Pokecube adds a variety of custom tag-like data objects for general use with tagging of pokemobs, abilities, moves, etc.

These tags are located in datapacks as follows:

-   `ABILITY` (`tags/pokemob_abilities/`) - tags for abilities, example: [`no_wandering_spirit`](../src/main/resources/data/pokecube/tags/pokemob_abilities/no_wandering_spirit.json)
-   `MOVE` (`tags/pokemob_moves/`) - tags for moves, example: [`contact-moves`](../src/generated/resources/data/pokecube/tags/pokemob_moves/contact-moves.json)
-   `BREEDING` (`tags/pokemob_egg_groups/`) - mobs with `PokedexEntry`s in the same tag can breed, example: [bird](../src/generated/resources/data/pokecube/tags/pokemob_egg_groups/bird.json)
-   `POKEMOB` (`tags/pokemob/`) - used for a variery of general pokemob lists, these also use `PokedexEntry` names in the `values`, example: [starters](../src/generated/resources/data/pokecube/tags/pokemob/starters.json)
-   `MOVEMENT` (`tags/pokemob_movements/`) - movement types for `PokedexEntry`s, ie whether they walk, fly, float or swim, can be in multiple tags, example: [floats](../src/generated/resources/data/pokecube/tags/pokemob_movements/floats.json)
-   `GENES` (`database/genes/`) - These allow for genetic mutations on breeding, example: [breeding_mutations](../src/main/resources/data/pokecube_mobs/database/genes/breeding_mutations.json)

## Pokemob Tags

Besides for the tags in `MOVEMENT` and `BREEDING`, the following are important tags for pokemobs, they are defined by the `PokedexEntry`:

-   [starters](../src/generated/resources/data/pokecube/tags/pokemob/starters.json) - these will form the list in the choose-first gui
-   [legends](../src/generated/resources/data/pokecube/tags/pokemob/legends.json) - these are treated as special for DNA/breeding purposes
-   [no_breeding](../src/generated/resources/data/pokecube/tags/pokemob/no_breeding.json) - these are not allowed to breed
-   [breeding_whitelist](../src/generated/resources/data/pokecube/tags/pokemob/breeding_whitelist.json) - these are exempt from the `no_breeding` tag
-   [fire_proof](../src/generated/resources/data/pokecube/tags/pokemob/fire_proof.json) - these are explicitly fireproof pokemobs, generally all fire types are as well
-   [shadow](../src/generated/resources/data/pokecube/tags/pokemob/shadow.json) - these are special "shadow" pokemobs, legacy left over from MC 1.6 days (list is presently empty)
-   [shoulder_allowed](../src/generated/resources/data/pokecube/tags/pokemob/shoulder_allowed.json) - these can go on your shoulder
-   `hms` - these tags define special behaviour while riding a pokemob
-   `food_types` - these group by things which can be eaten other than berries
-   `active_times` - pokemobs in these tags will only be active in the times associated with the tag, otherwise they will try to find somewhere to sleep
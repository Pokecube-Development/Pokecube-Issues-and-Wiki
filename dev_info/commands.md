# Commands

## /pokemake

This command is used to create/spawn pokemobs with more details than the normal `/summon` command. The format is as follows:

`/pokemake <mob>[nbt] [location] [player]`

if `[location]` and `[player]` are specified, then the pokemob will be assigned as owned by `[player]`

`<mob>` matches the id of a pokemob, and will randomly select something if it ends with `*`, `unown_*` for example will select a random unown, and `*` will select any random pokemob.

`[nbt]` directly follows `<mob>`, without spaces, and is formatted as a standard minecraft nbt tag in commands. The following keys have built in support:
-   `tag` - an nbt tag to load the mob from, similar to `/summon`
-   `moves` - either an array of move names or a single move
    - examples: `moves:[tackle, ice-beam]`, `moves:teleport`
    - presently the pokemob then calls "learn" on the moves, if `level` is defined, then the pokemob may overwrite the moves from here when it levels
-   `move` - similar to the single move case of `moves`
-   `nature` - nature to apply
    - examples: `nature:SERIOUS`, `nature:timid`
-   `size` - size of the pokemob
    - examples: `size:1.5`, `size:0.5`
-   `sex` - sexe of the mob to apply
    - examples: `sex:male`, `sex:female`
-   `shiny` - if the mob is shiny
    - example: `shiny:1b`
-   `colour` - rgb or rgba colour array for the mob
    - examples: `colour:[255,255,0]`, `colour:[255,255,255,128]`
-   `ability` - ability for the mob, name or index
    - examples: `ability:battle-bond`, `ability:0`
-   `name` - nickname for the mob
    - examples: `name:Bob`, `name:Jeb`
-   `ivs` - IVs for the mob, either 1 for all, or array of 6
    - examples: `ivs:0`, `ivs:[0,0,0,0,0,0]`
-   `wild` - whether the mob gets wild setup when exp is set
    - example: `wild:1b`
-   `level` - level for the mob
    - examples: `level:5`, `level:50`
-   `raid` - makes this mob a raid boss of specified type
    - examples: `raid:random`, `raid:terastal`, `raid:dynamax`

Before the above is processed, there is a [PokemakeArgumentEvent](../src/main/java/pokecube/api/events/init/PokemakeArgumentEvent.java) fired. This event allows pre-processing the pokemob, and adding additional handlers.

By default, there is 1 such example in the [Gimmicks](gimmicks.md), the [TerastalMechanic](../src/main/java/pokecube/gimmicks/terastal/TerastalMechanic.java#L274-295) adds the additional arguments:
-   `tera_type` - terastal type for the mob
    - examples: `tera_type:fire`, `tera_type:ice`
-   `is_tera` - whether it spawns terastalized
    - example: `is_tera:1b`

## /pokecube kill

This command deletes all of the pokemobs presently loaded. It will normally ignore tamed ones, though they can be targetted via `/pokecube kill_all`.

If you want to prevent a mob from being killed this way, listen for the [KillCommandEvent](../src/main/java/pokecube/core/commands/Kill.java), and cancel it.
# Pokemobs

## Main Pokemob Structure

Pokemobs consist of an `IPokemob` providing capability attached to an entity.

- `IPokemob` inherits from:
  - `IHasMobAIStates` - provides state tracking for combat, states of the mob's orders, etc
    - `IHasEntry` - tracking of a `PokedexEntry` and some breeding related methods
  - `IHasMoves` - provides ability to use attacks, combat tracking, etc
  - `ICanEvolve` - provies ability to change to different `IPokemob` types
  - `IHasOwner` - provides tracking for an owner, as well as for sending messages to the owner, also tracks traded status and the pokecube the pokemob is in, as well as the controller for when ridden
    - `IOwnable` - Thutcore api for generic ownable things
  - `IHasStats` - Tracks the pokemob's `Ability`, `Nature`, `PokeType`s, combat stats, etc
  - `IHungrymob` - Used for deciding on what the pokemob eats, and when it needs to
  - `IHasCommands` - Handles commands issued to the mob, usually from the owner
  - `IMobColourable` - Handles RGBA tints for the mob
  - `IShearable` - Handles a generic "sheared" state, similar to vanilla sheep
  - `ICopyMob` - Handles the ability for the pokemob to copy the appearance of another mob, mostly for transform and similar

`IPokemob` has the following for storing other information:

- `Mob` via `IHasEntry.getEntity()` - A vanilla mob associated with the Pokemob, if we are attached to a `Mob`, this will be what we are attached to, otherwise it will be a `Mob` that has the relevant values synced to/from the attached entity
- `PokedexEntry` via `IHasEntry.getPokedexEntry()` - Contains most of the standard pokemon related information for the pokemob
- `FormeHolder` - via `IPokemob.getCustomHolder()` - can be null, contains override information for the model/texture/types/etc for the individual `IPokemob`, separate from the standard `PokedexEntry` for it.
- `PokemobMoveStats` - holder for a variety of combat related things, such as `Ability` as modified in battle, the target for combat, the selected moves, etc
- `StatModifiers` - Holds type information for the Pokemob, as well as modifications for stats for use in combat

## Pokemob AI

Pokemob AI is done using a list of `Behavior` (Vanilla type) objects just like standard mob AI. For the custom Pokecube classes however, they inherit from `IAIRunnable`, and can be obtained via `IPokemob.getTasks()`. Pokemob AI is initialised via `pokecube.core.impl.capabilities.impl.PokemobAI`

These are arranged into three groupings:

- `IDLE` - handles most non-combat AI
    - `GuardEggTask`
    - `MateTask`
    - `HungerTask`
    - `IdleWalkTask`
    - `IdleRestTask`
    - `IdleJumpOnShoulderTask`
    - `FollowOwnerTask`
- `COMBAT` - handles combat movement, move use, enemy management, etc
    - `SelectMoveTask`
    - `UseAttacksTask`
    - `DodgeTask`
    - `CicleTask`
    - `ForgetTargetTask`
    - `CallForHelpTask`
    - `FindTargetsTask`
- `UTILITY` - handles item gathering and out-of-combat move use
    - `StoreTask`
    - `GatherTask`
    - `UseMoveTask`
    - `ForgetHuntedByTask`

And can be modified via the `InitAIEvent.Init` events which are fired when the lists are populated

There is then an additional list of `Logic` objects, obtainable via `IPokemob.getTickLogic()`

These are presently as follows:
- `LogicMountedControl` - handles controls while ridden, effects like water breathing for dive, etc
- `LogicInLiquid` - any needed adjustments for when the pokemob is in water or lava
- `LogicMovesUpdates` - Ticks abilities while in combat, checks held item use, etc
- `LogicInMaterials` - Handles additional effects for when the pokemob is inside a material (damage from water, etc)
- `LogicFloatFlySwim` - deals with switching path finders/navigators for mobs which swim/fly/etc
- `LogicMiscUpdate` - ticks inventory items, validates AI states, checks for particle effects and animations for the mob
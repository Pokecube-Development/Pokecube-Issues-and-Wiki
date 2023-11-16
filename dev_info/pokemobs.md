# Pokemobs

## Main Pokemob Structure

Pokemobs consist of an [IPokemob](../src/main/java/pokecube/api/entity/pokemob/IPokemob.java) providing capability attached to an entity.

- [IPokemob](../src/main/java/pokecube/api/entity/pokemob/IPokemob.java) inherits from:
  - `IHasMobAIStates` - provides state tracking for combat, states of the mob's orders, etc
    - `IHasEntry` - tracking of a [PokedexEntry](../src/main/java/pokecube/api/data/PokedexEntry.java) and some breeding related methods
  - `IHasMoves` - provides ability to use attacks, combat tracking, etc
  - `ICanEvolve` - provies ability to change to different [IPokemob](../src/main/java/pokecube/api/entity/pokemob/IPokemob.java) types
  - `IHasOwner` - provides tracking for an owner, as well as for sending messages to the owner, also tracks traded status and the pokecube the pokemob is in, as well as the controller for when ridden
    - `IOwnable` - [ThutAPI](thut_api.md#generic-ownable-objects) for generic ownable things
  - `IHasStats` - Tracks the pokemob's `Ability`, `Nature`, `PokeType`s, combat stats, etc
  - `IHungrymob` - Used for deciding on what the pokemob eats, and when it needs to
  - `IHasCommands` - Handles commands issued to the mob, usually from the owner
  - `IMobColourable` - Handles RGBA tints for the mob
  - `IShearable` - Handles a generic "sheared" state, similar to vanilla sheep
  - `ICopyMob` - Handles the ability for the pokemob to copy the appearance of another mob, mostly for transform and similar

[IPokemob](../src/main/java/pokecube/api/entity/pokemob/IPokemob.java) has the following for storing other information:

- `Mob` via `IHasEntry.getEntity()` - A vanilla mob associated with the Pokemob, if we are attached to a `Mob`, this will be what we are attached to, otherwise it will be a `Mob` that has the relevant values synced to/from the attached entity
- [PokedexEntry](../src/main/java/pokecube/api/data/PokedexEntry.java) via `IHasEntry.getPokedexEntry()` - Contains most of the standard pokemon related information for the pokemob
- `FormeHolder` - via `IPokemob.getCustomHolder()` - can be null, contains override information for the model/texture/types/etc for the individual [IPokemob](../src/main/java/pokecube/api/entity/pokemob/IPokemob.java), separate from the standard [PokedexEntry](../src/main/java/pokecube/api/data/PokedexEntry.java) for it.
- `PokemobMoveStats` - holder for a variety of combat related things, such as `Ability` as modified in battle, the target for combat, the selected moves, etc
- `StatModifiers` - Holds type information for the Pokemob, as well as modifications for stats for use in combat

## Pokemob AI

Pokemob AI is done using a list of `Behavior` (Vanilla type) objects just like standard mob AI. For the custom Pokecube classes however, they inherit from `IAIRunnable`, and can be obtained via `IPokemob.getTasks()`. Pokemob AI is initialised via `pokecube.core.impl.capabilities.impl.PokemobAI`

These are arranged into three groupings:

- [IDLE](../src/main/java/pokecube/core/ai/tasks/Tasks.java#L85-L156) - handles most non-combat AI
    - [GuardEggTask](../src/main/java/pokecube/core/ai/tasks/misc/GuardEggTask.java)
    - [MateTask](../src/main/java/pokecube/core/ai/tasks/idle/MateTask.java)
    - [HungerTask](../src/main/java/pokecube/core/ai/tasks/idle/HungerTask.java)
    - [IdleWalkTask](../src/main/java/pokecube/core/ai/tasks/idle/IdleWalkTask.java)
    - [IdleRestTask](../src/main/java/pokecube/core/ai/tasks/idle/IdleRestTask.java)
    - [IdleJumpOnShoulderTask](../src/main/java/pokecube/core/ai/tasks/idle/IdleJumpOnShoulderTask.java)
    - [FollowOwnerTask](../src/main/java/pokecube/core/ai/tasks/misc/FollowOwnerTask.java)
- [COMBAT](../src/main/java/pokecube/core/ai/tasks/Tasks.java#L159-L214) - handles combat movement, move use, enemy management, etc
    - [SelectMoveTask](../src/main/java/pokecube/core/ai/tasks/combat/attacks/SelectMoveTask.java)
    - [UseAttacksTask](../src/main/java/pokecube/core/ai/tasks/combat/attacks/UseAttacksTask.java)
    - [DodgeTask](../src/main/java/pokecube/core/ai/tasks/combat/movement/DodgeTask.java)
    - [LeapTask](../src/main/java/pokecube/core/ai/tasks/combat/movement/LeapTask.java)
    - [CicleTask](../src/main/java/pokecube/core/ai/tasks/combat/movement/CicleTask.java)
    - [ForgetTargetTask](../src/main/java/pokecube/core/ai/tasks/combat/management/ForgetTargetTask.java)
    - [CallForHelpTask](../src/main/java/pokecube/core/ai/tasks/combat/management/CallForHelpTask.java)
    - [FindTargetsTask](../src/main/java/pokecube/core/ai/tasks/combat/management/FindTargetsTask.java)
- [UTILITY](../src/main/java/pokecube/core/ai/tasks/Tasks.java#L217-L271) - handles item gathering and out-of-combat move use
    - [StoreTask](../src/main/java/pokecube/core/ai/tasks/utility/StoreTask.java)
    - [GatherTask](../src/main/java/pokecube/core/ai/tasks/utility/GatherTask.java)
    - [UseMoveTask](../src/main/java/pokecube/core/ai/tasks/utility/UseMoveTask.java)
    - [ForgetHuntedByTask](../src/main/java/pokecube/core/ai/tasks/idle/ForgetHuntedByTask.java)

### Adding/Modifying AI

The `InitAIEvent.Init` events are fired when the above lists are populated. This can be used to add to or modify the lists, if using the `IAIRunnable` type of task. An example of this usage can be found via the [Structure Builder Gimmick](../src/main/java/pokecube/gimmicks/builders/BuilderTasks.java), which adds 2 tasks to the `UTILITY` tasks list.

AI tasks can also be set to be automatically added by registering a [ITaskAdder](../src/main/java/pokecube/api/ai/ITaskAdder.java) with [TaskAdders.register](../src/main/java/pokecube/api/ai/TaskAdders.java). An example of this can be found via the [Ant Nest Gimmick](../src/main/java/pokecube/gimmicks/nests/tasks/ants/AntTasks.java), where it adds a variety of sensors, memories and tasks. This method allows adding any arbitrary task type, rather than just `IAIRunnable`.

### Additional Tick Logic

There is then an additional list of `Logic` objects, obtainable via `IPokemob.getTickLogic()`

These are presently as follows:
- [LogicMountedControl](../src/main/java/pokecube/core/ai/logic/LogicMountedControl.java) - handles controls while ridden, effects like water breathing for dive, etc
- [LogicInLiquid](../src/main/java/pokecube/core/ai/logic/LogicInLiquid.java) - any needed adjustments for when the pokemob is in water or lava
- [LogicMovesUpdates](../src/main/java/pokecube/core/ai/logic/LogicMovesUpdates.java) - Ticks abilities while in combat, checks held item use, etc
- [LogicInMaterials](../src/main/java/pokecube/core/ai/logic/LogicInMaterials.java) - Handles additional effects for when the pokemob is inside a material (damage from water, etc)
- [LogicFloatFlySwim](../src/main/java/pokecube/core/ai/logic/LogicFloatFlySwim.java) - deals with switching path finders/navigators for mobs which swim/fly/etc
- [LogicMiscUpdate](../src/main/java/pokecube/core/ai/logic/LogicMiscUpdate.java) - ticks inventory items, validates AI states, checks for particle effects and animations for the mob

## Combat Structure

Combat consists of a [Battle](../src/main/java/pokecube/api/moves/Battle.java), where there are 2 lists of `LivingEntity` which are listed as on opposing teams. A [Battle](../src/main/java/pokecube/api/moves/Battle.java) ends when one of the teams is empty.

The `PokemobMoveStats` for the [IPokemob](../src/main/java/pokecube/api/entity/pokemob/IPokemob.java) contains a tracker for both the current enemy `LivingEntity`, and the index of the `LivingEntity` in the list in the [Battle](../src/main/java/pokecube/api/moves/Battle.java). The owner of the [IPokemob](../src/main/java/pokecube/api/entity/pokemob/IPokemob.java) can change the target by setting that index.

Once a [IPokemob](../src/main/java/pokecube/api/entity/pokemob/IPokemob.java) has a target, it is up to the `COMBAT` AI tasks to deal with what to do, and that is where the actual combat is done. The default behaviour is as follows:

- `UseAttacksTask` - Queues up a move use for the pokemob if the position/timing conditions are correct
- `DodgeTask` - Attempts to dodge incoming moves
- `LeapTask` - Attempts to jump at the enemy if a contact move is being executed
- `CicleTask` - Keeps the pokemob close to the general center of where the fight started

## Attack Use and Order

A move gets excuted normally via the `IHasMoves.executeMove` method, which takes parameters for a target entity, as well as a target location. The target location is more important in this case, but both parameters can be null, the following then occur:

### Attack Use

1. pokemob's selected move is determined, to ensure it is not on cooldown or disabled
2. Next the attack cooldown for the pokemob is set (call to `IPokemob.setAttackCooldown`)
3. `ActualMoveUse.PreMoveStatus` event is fired on `PokecubeAPI.MOVE_BUS` to determine if the test for status effects
  - If cancelled, skips to step 4.
  - Otherwise if the pokemob has flinched, is confused, or is infatuated, that is then checked next, and if the negative condition occurs, we exit here
4. Finally `MovesUtils.useMove` is called to generate and start application of the move.

In `MovesUtils.useMove` a `MoveApplication` is made for the attack, and it is applied to targets based on the result of testing targets via `MoveApplicationRegistry.getValidator`. The targets are selected either from the active [Battle](../src/main/java/pokecube/api/moves/Battle.java) that the pokemob is in, or from just the target location and given user (for cases of out of combat move use). Before queueing the attack for application, a `MoveUse.ActualMoveUse.Init` event is fired on the `PokecubeAPI.MOVE_BUS`, and if it is cancelled, the attack will not be queued.

Queueing of the move consists of constructing a `EntityMoveUse`, and marking it as ready to start applying next tick. Here is also where the `CombatStates.USEDZMOVE` for the mob is recorded if nessisary. The queued moves get executed the next tick, and are sorted by priority there (including checks to `VIT` stat), this however is not generally relevant. The `EntityMoveUse` is then added to the level, and the move's hunger cost is applied to the pokemob, and a message about the move use is sent to those involved.

### EntityMoveUse

The `EntityMoveUse` is a `ThrowableProjectile` object and then works as follows:
1. Initialises start/end/target/user information, and determines if it is a contact move or not. 
  - If contact/self, it will wrap around the user
  - if AOE move, it is a 8m wide projectile
  - if it has a custom size defined in the `MoveEntry`, it uses that.
  - otherwise is a 0.75m wide projectile
2. Next the `EntityMoveUse` continues to move depending on if projectile or contact/self move, checking other targets it comes into contact with, as well as applying particle effects.
3. Once the `EntityMoveUse` has reached the age in ticks defined by the move's `IMoveAnimation`'s `getApplicationTick`, it will start applying to each hit target once.
4. Once the move has reached the end destination, or has timed out, or hit for a contact move, the `EntityMoveUse` is removed.
 - If the move hits the target end destination, or is sufficiently close for a hit contact move, then it will attempt to apply any in-world effects, via the `MoveEntry.doWorldAction`

### Applying to a target

Applying the move effects is done as follows for each `LivingEntity` found during step 2 above:
1. Check if the target is generally valid to hit, this checks that it was not already hit, if not valid, exit.
2. Check if the move is allowed to hit others (`MoveEntry.canHitNonTarget`), and exit if target is not valid for that.
3. If we are not server side, exit.
4. then initiate a battle if the target is not the original target
5. mark the target as having been hurt by the user
6. Call `MovesUtils.doAttack` to apply further
7. If the move is not AOE, and the target is not blocking, Apply the `MoveEntry.doWorldAction` and mark as finished, and remove the `EntityMoveUse`.

`MovesUtils.doAttack` validates the `MoveEntry`, and then forwards the call to `MoveEntry.applyMove`, which calls `MoveApplicationRegistry.apply`

`MoveApplicationRegistry.apply` initialises the `MoveApplication` for use via `MoveApplicationRegistry.preApply`, where registered `MOVE_MODIFIERS` are applied to the move, and the `EFFECT_REGISTRY` is used to check of the move should have lasting effects. Next `MoveApplication.preApply()` is called once to reset counters, and `MoveApplication.apply()` is called for each time the move is expected to hit.

### World Actions

Some moves have effects which occur during `MoveEntry.doWorldAction`, which works as follows:

1. `MoveWorldAction.PreAction` is fired on `PokecubeAPI.MOVE_BUS`, if cancelled, exit
2. `MoveWorldAction.OnAction` is fired on `PokecubeAPI.MOVE_BUS`
3. `MoveWorldAction.PostAction` is fired on `PokecubeAPI.MOVE_BUS`

The default actions are applied during a low priority event listener in step 2.

### Final Move Application

`MoveApplication.apply()` does the following:

1. Fires the `DuringUse.Pre` event on the `PokecubeAPI.MOVE_BUS`, if cancelled, exits.
2. Checks if the `MoveApplication` is `cancelled` or `failed`.
  - If not failed, checks the `PreApplyTests` for the `MoveApplication`
  - If either of these failed, or there is no target for the move:
    1. Send messages to those involved in the battle
    2. Play sounds if we got here only because of no target
    3. Call our `OnMoveFail.onMoveFail`
    4. Fires a `DuringUse.Post` event on the `PokecubeAPI.MOVE_BUS` then exits
3. Plays the move sounds if present
4. Checks if we should `infatuate`, if so and the target is the pokemob, apply the infatuation
5. Constructs a `MoveApplication.Damage` via call to our `DamageApplier.applyDamage`
6. If the `efficiency` for the `Damage` is <= 0 skip to step applying `PostMoveUse`
7. Apply stats effects/checks via `StatApplier.applyStats`
8. Apply status effects/checks via `StatusApplier.applyStatus`
9. Apply recoil via `RecoilApplier.applyRecoil`
10. Apply healing via `HealProvider.applyHealing`
11. Apply ongoing effects if present via `OngoingApplier.applyOngoingEffects`
12. Apply `PostMoveUse.applyPostMove`
13. Fires a `DuringUse.Post` event on the `PokecubeAPI.MOVE_BUS` then exits

The various appliers mentioned above do the following for their default behaviour:

- `PreApplyTests` - checks status effects `SLP`, `PAR`, `FRZ` and returns false if they apply
- `StatusApplier` - Applies any status effects the move should have to the target
- `StatApplier` - Applies stat modifications to the target, and sets the `applied_stat_effects` accordingly
- `DamageApplier` - Calculates damage to apply to the target, and applies it, also apply lightning bolt effects for appropriate moves, and makes psychic moves detonate creepers
  - Checks the `AccuracyProvider` to get the `efficiency` for the move.
- `OngoingApplier` - no default actions
- `RecoilApplier` - Checks for damage/healing which occurs on move use, and applies to the user it based on the damage dealt
- `HealProvider` - Checks for healing effects on the target, and applies it accordingly
- `PostMoveUse` - no default actions
- `OnMoveFail` - no default actions

These appliers can by replaced by registering appropriate classes in `pokecube.mobs.moves.attacks` and including a `@MoveProvider` annotation to declare which move it applies to. These will then replace the default appliers in the `MoveApplicationRegistry.preApply` step above. There is some default parsing which occurs from loading in `pokecube.api.data.moves.Moves` objects from json files, which attempts to generate appropriate effects for most moves, but for now custom logic needs to be implemented manually.

### Default Event Results

The `DuringUse.Post`, `DuringUse.Pre` and `MoveWorldAction.OnAction` events have `LOWEST` priority event handlers to do further processing. If they are cancelled before, they will not gety to the default processing.

- `DuringUse.Post`
  1. ticks held items after move use
  2. calls `postMoveUse` for abilities of involved pokemobs

- `DuringUse.Pre`
  1. ticks held items before move use
  2. Checks for `substitute` effects and fails the move accordingly
  3. second step of held item use checks
  4. Check for false-swipe
  5. Check for block moves (like protect) via the `block-move` moves tag
  6. Check for un-blockable moves via the `no-block-move` moves tag
  7. Decrement counter for using block moves

- `MoveWorldAction.OnAction`
  1. Looks up the move actions for the given attack.
  2. if no move actions, checks if it should have defaults applied for water, ice, electric or fire types
  3. if permissions are enabled, checks if owner has permission to use the action
  4. If in combat, applies `IMoveWorldEffect.applyInCombat`, otherwise apply `IMoveWorldEffect.applyOutOfCombat`


## Abilities and their effects

Abilities are implemented via classes which extend the `Ability` class, they will be automatically loaded in from classes defined in `pokecube.mobs.abilities`, and annotated with `@AbilityProvider`. Addons can register other places to load from as done in `pokecube.mobs.abilities.AbilityRegister::init`

Abilities have the following methods:

- `startCombat` - called when a pokemob is added to a [Battle](../src/main/java/pokecube/api/moves/Battle.java)
- `endCombat` - called when the pokemob is removed from the [Battle](../src/main/java/pokecube/api/moves/Battle.java)
- `beforeDamage` - called from the `DamageApplier.applyDamage` to possibly modify damage dealt
- `canChange` - Ability dependant check for mega evolution
- `onAgress` - called when combat target is set
- `preMoveUse` - called during an event handler for `DuringUse.Pre` events
- `postMoveUse` - called during an event handler for `DuringUse.Post` events
- `onUpdate` - called each tick by `LogicMovesUpdates` 
- `onRecall` - called when we recall to a pokecube, either via death or command
- `destroy` - cleanup of ability for when mob is removed, only needed for abilities which register event listeners (like `Damp`)

## Pokemob Genetics

Pokemobs use the [genetics system](thut_api.md#genetics-system) provided by ThutAPI. Pokemobs by default track the following genes:

- [AbilityGene](../src/main/java/pokecube/core/entity/pokemobs/genetics/genes/AbilityGene.java)
- [ColourGene](../src/main/java/pokecube/core/entity/pokemobs/genetics/genes/ColourGene.java)
- [SpeciesGene](../src/main/java/pokecube/core/entity/pokemobs/genetics/genes/SpeciesGene.java)
- [IVsGene](../src/main/java/pokecube/core/entity/pokemobs/genetics/genes/IVsGene.java)
- [EVsGene](../src/main/java/pokecube/core/entity/pokemobs/genetics/epigenes/EVsGene.java)
- [MovesGene](../src/main/java/pokecube/core/entity/pokemobs/genetics/epigenes/MovesGene.java)
- [NatureGene](../src/main/java/pokecube/core/entity/pokemobs/genetics/genes/NatureGene.java)
- [ShinyGene](../src/main/java/pokecube/core/entity/pokemobs/genetics/genes/ShinyGene.java)
- [SizeGene](../src/main/java/pokecube/core/entity/pokemobs/genetics/genes/SizeGene.java)
- [DynamaxGene](../src/main/java/pokecube/core/entity/pokemobs/genetics/genes/DynamaxGene.java)
- [TeraTypeGene](../src/main/java/pokecube/core/entity/pokemobs/genetics/genes/TeraTypeGene.java)

## Pokemob Data Structure

Pokemobs are loaded from data via files placed in `database/pokemobs/pokedex_entries`, such as [Abra](../src/generated/resources/data/pokecube_mobs/database/pokemobs/pokedex_entries/abra.json). Normally these are generated by the data generation [scripts](../processing_scripts/database_update/readme.md), from data at PokeAPI. These are first loaded as either a [JsonPokedexEntry](../src/main/java/pokecube/core/database/pokedex/JsonPokedexEntry.java) or a list thereof. Then each `JsonPokedexEntry` is converted to [PokedexEntry](../src/main/java/pokecube/api/data/PokedexEntry.java).

If the value of `stock` for the `JsonPokedexEntry` is `true`, then a minecraft `EntityType` is registered for that entry. Otherwise it will be linked to the `pokecube:missingno` entity type.

## Pokemob Animation States

Pokemob animations are run by selecting the first state which occurs in the list provided by the [IAnimated](../src/main/java/thut/api/entity/IAnimated.java) associated with the pokemob. These are populated in the orders listed below. If the mob is being ridden, then "ridden_\<anim>" is added to the list before each of the strings listed below.

Once an animation is selected, it may then be randomised if sub-animations are registered via the xml files for that animation.

### Main animations

These are put in `IAnimated.getChoices()`

- "dead" - if the mob is dying
- lower case names of active [LogicStates](../src/main/java/pokecube/api/entity/pokemob/ai/LogicStates.java) in order
- "floating" - if the mob is not moving horizontally, and has pose `FALL_FLYING`
- "flying" - if the mob has pose, has pose `FALL_FLYING`
- "floating" - if the mob is moving horizontally, and has pose `FALL_FLYING`
- "sleeping" - if the mob has pose `SLEEPING`
- "in_water" - if the mob is not moving horizontally, and has pose `SWIMMING`
- "swimming" - if the mob has pose `SWIMMING`
- "in_water" - if the mob is moving horizontally, and has pose `SWIMMING`
- "guarding_sprinting" - if the mob is guarding and sprinting
- "sprinting" - if the mob is sprinting
- "guarding_walking" - if the mob is guarding and walking
- "walking" - if the mob is walking
- lower case names of active [CombatStates](../src/main/java/pokecube/api/entity/pokemob/ai/CombatStates.java) in order
- lower case names of active [GeneralStates](../src/main/java/pokecube/api/entity/pokemob/ai/CombatStates.java) in order
- "idle"

### Transient animations

These are put in `IAnimated.transientAnimations()`

- "blink" - randomly approximately every 4 seconds
- "attack_\<name\>" - if the mob is [EXECUTINGMOVE](../src/main/java/pokecube/api/entity/pokemob/ai/CombatStates.java), where `<name>` is the name of the attack being used.
- "attack_contact" - similar to above, but using a contact move
- "attack_ranged" - similar to above, but using a ranged move
- "attack_status" - similar to above, but using a status move
- "attack_other" - similar to above, but using a unknown move - this state shouldn't occur
- "battling" - if the mob is in a battle

### Editing animations via addons

The [AnimationSelectionEvent](../src/main/java/pokecube/api/events/pokemobs/ai/AnimationSelectionEvent.java) is fired on the `PokecubeAPI.POKEMOB_BUS` after the above lists are populated, this can be used to edit them accordingly.
# NPCs and Trainers

## General NPCs

Pokecube NPCs (`NpcMob`) are sub-classes of Vanilla `Villager`s. They have an associated `NpcType`, which is generally used to set their trades, textures, etc. Due to being villagers, they also have a `VillagerProfession`, which is normally set based on their `NpcType`, but can also be obtained "naturally" via profession blocks.

By default, the NPCs get vanilla-like AI attached to them, which may be modified during the `BrainInitEvent` which is fired on the `MinecraftForge.EVENT_BUS`

## Trainer NPCs

Trainer NPCs inherit from regular `NpcMob`s, and have a special version of `NpcType`, the `TypeTrainer`, which includes some additional information regarding pokemobs. This is stored via the `IHasPokemobs` capability, discussed further below.

### Loading data for Trainers

`TypeTrainer` information is loaded via loading `TrainerEntry` objects from data. These presently load in as a `XMLDatabase` object, which contains a list of `TrainerEntry` called `trainers`. These load in from the `database/trainers/` directory in datapacks.

## Other NPCs

Non-pokecube added NPCs (such as vanilla villagers) may also have an `IHasPokemobs` capability attached, presently toggleable via configs.

## IHasPokemobs Capability

the `IHasPokemobs` is the base capability used for organising those who can control pokemobs. There are presently 2 versions of this used, one for NPCs, and one for Players.

### Trainer Combat AI

Non-players with the `IHasPokemobs` capabilty attached get the following AI tasks added during the `BrainInitEvent` event, attached to the `Battle` `Activity` (`pokecube:battling`):

-   `AgroTargets` - automatic agression to start battle for various targets (usually players, zombies and wild pokemobs)
-   `CaptureMob` - attempts to capture wild pokemobs we battle
-   `DeAgro` - ends battles if the target has not been seen for a while
-   `Retaliate` - starts a battle if we are hurt by another entity
-   `ChooseAttacks` - manages attacks used by our pokemob
-   `ManageOutMob` - ensures that we send out our pokemobs if we are in a battle
-   `ManagePokemobTarget` - tells our pokemob which enemy is the target to fight

## IHasMessages Capability

The `IHasMessages` capability is also attached to npcs when they get the `IHasPokemobs` one attached. This one controls the processing of messages/actions when the npc interacts with others.

The various states involved are defined in the `MessageState` enum
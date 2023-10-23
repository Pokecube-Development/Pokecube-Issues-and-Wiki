# NPCs and Trainers

## General NPCs

Pokecube NPCs ([NpcMob](../src/main/java/pokecube/core/entity/npc/NpcMob.java)) are sub-classes of Vanilla `Villager`s. They have an associated [NpcType](../src/main/java/pokecube/core/entity/npc/NpcType.java), which is generally used to set their trades, textures, etc. Due to being villagers, they also have a `VillagerProfession`, which is normally set based on their `NpcType`, but can also be obtained "naturally" via profession blocks.

By default, the NPCs get vanilla-like AI attached to them, which may be modified during the `BrainInitEvent` which is fired on the `MinecraftForge.EVENT_BUS`

## Trainer NPCs

Trainer NPCs inherit from regular [NpcMob](../src/main/java/pokecube/core/entity/npc/NpcMob.java)s, and have a special version of [NpcType](../src/main/java/pokecube/core/entity/npc/NpcType.java), the [TypeTrainer](../src/main/java/pokecube/adventures/capabilities/utils/TypeTrainer.java), which includes some additional information regarding pokemobs. This is stored via the [IHasPokemobs](../src/main/java/pokecube/api/entity/trainers/IHasPokemobs.java) capability, discussed further below.

### Loading data for Trainers

[TypeTrainer](../src/main/java/pokecube/adventures/capabilities/utils/TypeTrainer.java) information is loaded via loading `TrainerEntry` objects from data. These presently load in as a `XMLDatabase` object, which contains a list of `TrainerEntry` called `trainers`. These load in from the `database/trainers/` directory in datapacks.

## Other NPCs

Non-pokecube added NPCs (such as vanilla villagers) may also have an [IHasPokemobs](../src/main/java/pokecube/api/entity/trainers/IHasPokemobs.java) capability attached, presently toggleable via configs.

## IHasPokemobs Capability

the [IHasPokemobs](../src/main/java/pokecube/api/entity/trainers/IHasPokemobs.java) is the base capability used for organising those who can control pokemobs. There are presently 2 versions of this used, one for NPCs, and one for Players.

### Trainer Combat AI

Non-players with the [IHasPokemobs](../src/main/java/pokecube/api/entity/trainers/IHasPokemobs.java) capabilty attached get the following AI tasks added during the `BrainInitEvent` event, attached to the [Battle](../src/main/java/pokecube/api/moves/Battle.java) `Activity` (`pokecube:battling`):

-   [AgroTargets](../src/main/java/pokecube/adventures/ai/tasks/battle/agro/AgroTargets.java) - automatic agression to start battle for various targets (usually players, zombies and wild pokemobs)
-   [CaptureMob](../src/main/java/pokecube/adventures/ai/tasks/battle/CaptureMob.java) - attempts to [capture](pokecubes.md#capturing-pokemobs) wild pokemobs we battle
-   [DeAgro](../src/main/java/pokecube/adventures/ai/tasks/battle/agro/DeAgro.java) - ends battles if the target has not been seen for a while
-   [Retaliate](../src/main/java/pokecube/adventures/ai/tasks/battle/agro/Retaliate.java) - starts a battle if we are hurt by another entity
-   [ChooseAttacks](../src/main/java/pokecube/adventures/ai/tasks/battle/ChooseAttacks.java) - manages attacks used by our pokemob
-   [ManageOutMob](../src/main/java/pokecube/adventures/ai/tasks/battle/ManageOutMob.java) - ensures that we send out our pokemobs if we are in a battle
-   [ManagePokemobTarget](../src/main/java/pokecube/adventures/ai/tasks/battle/ManagePokemobTarget.java) - tells our pokemob which enemy is the target to fight

## IHasMessages Capability

The [IHasMessages](../src/main/java/pokecube/api/entity/trainers/IHasMessages.java) capability is also attached to npcs when they get the [IHasPokemobs](../src/main/java/pokecube/api/entity/trainers/IHasPokemobs.java) one attached. This one controls the processing of messages/actions when the npc interacts with others.

The various states involved are defined in the [MessageState](../src/main/java/pokecube/api/entity/trainers/actions/MessageState.java) enum
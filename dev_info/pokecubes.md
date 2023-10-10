# Pokecubes

Pokecubes are the items used to capture Pokemobs. They are primarily governed by the [IPokecube](../src/main/java/pokecube/api/items/IPokecube.java) interface.
[IPokecube](../src/main/java/pokecube/api/items/IPokecube.java)
[IPokecube](../src/main/java/pokecube/api/items/IPokecube.java)s are used to determine capture rates, as well as to provide the functions needed to throw pokecubes at things.

## Registering Pokecubes

By default, the capture rates are defined by registering `PokecubeBehaviour` for the pokecube, these are stored in a map that uses a `ResourceLocation` as the key, and are registered during the `RegisterPokecubes` event, which is fired on the `PokecubeAPI.POKEMOB_BUS`.

The `PokecubeBehaviour` provides the standard capture modifier (1, 1.5, 2 for example for poke, great, ultra), as well as additional modifications post/pre capture. The default implementations are set in `PokecubeMobs::registerPokecubes`, and should provide examples for using all of the functions.

## Tossing Pokecubes

The default [IPokecube](../src/main/java/pokecube/api/items/IPokecube.java) implementation is the [Pokecube](../src/main/java/pokecube/core/items/pokecubes/Pokecube.java) item class. It provides the `throwPokecube` and `throwPokecubeAt` functions, which produce a [EntityPokecube](../src/main/java/pokecube/core/entity/pokecubes/EntityPokecube.java) to throw. Capture itself is then handled by the [EntityPokecube](../src/main/java/pokecube/core/entity/pokecubes/EntityPokecube.java).

`throwPokecube` and `throwPokecubeAt` are presently called in 3 locations: 

1. When the player finishes "using" the [Pokecube](../src/main/java/pokecube/core/items/pokecubes/Pokecube.java)
2. When a dispenser fires a [Pokecube](../src/main/java/pokecube/core/items/pokecubes/Pokecube.java)
3. When an NPC attempts to [capture](npcs.md#trainer-combat-ai) a pokemob

## Capturing Pokemobs

Capturing pokemobs occurs when an [EntityPokecube](../src/main/java/pokecube/core/entity/pokecubes/EntityPokecube.java) who's item does not contain an entity collides with a wild entity. This is done via `CaptureManager::captureAttempt`, which does the following:

1.  Checks that the target entity is:
    -   alive
    -   not invulnerable
    -   not being captured by something else
    -   not owned by someone
    -   allowed to be captured by `IPokecube::canCapture`
2.  Tests for `IPokecube::getCaptureModifier`, if <=0, capture exits.
3.  Fires `CaptureEvent.Pre` on the `PokecubeAPI.POKEMOB_BUS`
    -  If the entity is a [IPokemob](../src/main/java/pokecube/api/entity/pokemob/IPokemob.java), then does as follows:
        1.  if the event has result of `DENY`, exits
        2.  if the event is `isCancelled`, uses capture rate from event, proceeds to attempt capture
        3.  otherwise uses capture rate from `Tools::computeCatchRate`, proceeds to attempt capture
    -   If not a [IPokemob](../src/main/java/pokecube/api/entity/pokemob/IPokemob.java):
        1.  if the event is `isCancelled`, exits
        2.  otherwise uses capture rate from `Tools::getCatchRate`, proceeds to attempt capture
4.  If the capture was attempted, the entity is removed from the level

If a capture was attempted, the [EntityPokecube](../src/main/java/pokecube/core/entity/pokecubes/EntityPokecube.java) will attempt to shake 4 times, the number of shakes was determined during the `Tools::computeCatchRate` or `Tools::getCatchRate` steps above.

If it is successful, `CaptureManager::captureSucceed` is called. Here the captured mob is tamed by the thrower if the mob is tameable, and marked as added to the [Pokecube](../src/main/java/pokecube/core/items/pokecubes/Pokecube.java). If the mob is an [IPokemob](../src/main/java/pokecube/api/entity/pokemob/IPokemob.java), the happiness is set accordingly.

Next the `CaptureEvent.Post` event is fired on the `PokecubeAPI.POKEMOB_BUS`, by default this results in the player collecting the cube, or going to PC. This is marked by cancelling the event. If the event was not cancelled, the cube drops on the ground instead. There is a default handler for `CaptureEvent.Post` in `StatsHandler` which is used to reward advancements and to record the capturing of the pokemob.

If the capture was un-successful, `CaptureManager::captureFailed` is called instead, which releases the captured mob, and then attempts to make it aggress the one who threw the cube. The releasing of the mob is handled by `SendOutManager::sendOut`

## Sending out Pokemobs

Releasing of an entity from a [Pokecube](../src/main/java/pokecube/core/items/pokecubes/Pokecube.java) is handled via `SendOutManager::sendOut`. This re-creates the entity from the stored data inside the [Pokecube](../src/main/java/pokecube/core/items/pokecubes/Pokecube.java), and then attempts to add it to the world. Here is checks the following:

-   If thrown by a player, are they allowed to send it out
-   Is there enough room for it to be sent out, attempt to move such that there is.

If those checks pass, the following are then done:

-   Mob added to level
-   If a [IPokemob](../src/main/java/pokecube/api/entity/pokemob/IPokemob.java):
    -   `IPokemob::onSendOut` is called if relevant
    -   Ownership flag is checked
    -   Exiting cube status is set for animations
    -   Message sent to owner about sending out
    -   `SendOut.Post` is fired on `PokecubeAPI.POKEMOB_BUS`

If the mob already exists for whatever reason (say removed in the same tick as release), then these are scheduled for the next level tick.

These are triggered in the following situtations:

1.  On capture failure
2.  When a [EntityPokecube](../src/main/java/pokecube/core/entity/pokecubes/EntityPokecube.java) is filled and collides with a block, and is marked to release on collision (sneaking while tossing does not mark as such)
3.  When a [EntityPokecube](../src/main/java/pokecube/core/entity/pokecubes/EntityPokecube.java) is filled and collides with a mob
4.  When a [EntityPokecube](../src/main/java/pokecube/core/entity/pokecubes/EntityPokecube.java) is filled and punched or interacted with, while not sneaking
5.  If a [EntityPokecube](../src/main/java/pokecube/core/entity/pokecubes/EntityPokecube.java) is marked to `autoRelease` and times out

In the cases where the release occurs due to collision with a mob, a [Battle](../src/main/java/pokecube/api/moves/Battle.java) is initiated.

## Loot Pokecubes

An [EntityPokecube](../src/main/java/pokecube/core/entity/pokecubes/EntityPokecube.java) can also be marked as loot via the `isLoot` field (alternately by setting `isLoot` in the nbt and reloading it). If marked as such, then the interaction behaviour changes to giving items. These can either be set via the `lootTable` parameter as a vanilla `LootTable`, or manually as items via `LootEntry` objects in the `loot` nbt list. 

If a [EntityPokecube](../src/main/java/pokecube/core/entity/pokecubes/EntityPokecube.java) is loot, then when interacted with, it gives the item, and then disappears for a configurable `resetTime`. Once collected, the [EntityPokecube](../src/main/java/pokecube/core/entity/pokecubes/EntityPokecube.java) will remain invisible to the player until the `resetTime` has passed, and the player leaves and re-enters the area (ie client load/unload of the [EntityPokecube](../src/main/java/pokecube/core/entity/pokecubes/EntityPokecube.java))
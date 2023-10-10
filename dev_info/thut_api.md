# Content provided via ThutAPI

## Genetics System

Mob genetics are handled via a capability exposing a [IMobGenetics](../src/main/java/thut/api/entity/genetics/IMobGenetics.java) interface. This allows attaching triplets of [Genes](../src/main/java/thut/api/entity/genetics/Gene.java) to a mob, organised in [Alleles](../src/main/java/thut/api/entity/genetics/Alleles.java). Genes must be [registered](../src/main/java/thut/api/entity/genetics/GeneRegistry.java) to be properly saved/loaded with the mob.

[Genes](../src/main/java/thut/api/entity/genetics/Gene.java) are a holder for an object, associated with a `ResourceLocation` key. They can also be saved/loaded to/from `CompoundTag` nbt formats. They can have optional on-tick effects if attached to an `Entity`. [Genes](../src/main/java/thut/api/entity/genetics/Gene.java) of the same type have an `interpolate` function which allows producing a mixed version, or just returning a random one of the inputs. They also have a `mutate` function, which returns a possibly new [Gene](../src/main/java/thut/api/entity/genetics/Gene.java). There is also a `mutate` function which takes two [IMobGenetics](../src/main/java/thut/api/entity/genetics/IMobGenetics.java) as arguments.

[Alleles](../src/main/java/thut/api/entity/genetics/Alleles.java) work as follows:

-   Stores 2 [Genes](../src/main/java/thut/api/entity/genetics/Gene.java) which are treated as the ones obtained from the parents
-   Stores a third [Gene](../src/main/java/thut/api/entity/genetics/Gene.java) which is the one which is used for expression

The expressed gene can either be manually set, or is otherwise generated from the parent genes, during this automatic generation, the argumentless `mutate` function is called, based on a random chance based on the [Gene's](../src/main/java/thut/api/entity/genetics/Gene.java) `getMutationRate`.

When initialising a [IMobGenetics](../src/main/java/thut/api/entity/genetics/IMobGenetics.java) from two parent [IMobGenetics](../src/main/java/thut/api/entity/genetics/IMobGenetics.java), the genes get mixed/mutated between the resulting [Alleles](../src/main/java/thut/api/entity/genetics/Alleles.java). Normally the expressed gene is not used for this mixing, unless the `getEpigeneticRate` for that gene is above a random number from [0,1)

[Genes](../src/main/java/thut/api/entity/genetics/Gene.java) can be added to a [IMobGenetics](../src/main/java/thut/api/entity/genetics/IMobGenetics.java) at any time, though should probably be done via either a low priority handler for the `AttachCapabilitiesEvent`, or during the forge `EntityJoinLevelEvent`

## Generic Ownable Objects

Object owning is handled via a capability exposing a [IOwnable](../src/main/java/thut/api/IOwnable.java) interface. These are as generic as possible, and track a `LivingEntity` owner, along with a `UUID`. They also track whether said owner is a `Player`. Wrapper classes are made for all vanilla tameable mobs, and there is a base implementations meant for use with `BlockEntity`s and `Entity`s.

[IOwnable](../src/main/java/thut/api/IOwnable.java) blocks have then additional protection against breaking. They can only be broken by their owner, when left clicked by something in the [OwnableCaps.STICKTAG](../src/main/resources/data/thutcore/tags/items/pokeystick.json). Ownership is automatically applied for these blocks when they are placed.

## Generic Mob Mimicing

Mob Mimicing is handled via a capability exposing a [ICopyMob](../src/main/java/thut/api/entity/ICopyMob.java) interface. This allows something to mimic a `LivingEntity`. If the holder of the [ICopyMob](../src/main/java/thut/api/entity/ICopyMob.java) is not a `LivingEntity`, then it is responsible for ensuring it gets updated each tick via `onBaseTick`.

The default implementation of `onBaseTick` handles the following:

-   Ensures the `ICopyMob` is holding a `LivingEntity`
-   If the holder is a `LivingEntity`, then does the following:
    -   Calls `baseTick` of the copy
    -   Syncs held items to the copy
    -   Syncs visible transforms (location, rotations, etc)
    -   Syncs health and air supply
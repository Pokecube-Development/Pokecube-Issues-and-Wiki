# Gimmick Features

Gimmicks in Pokecube are generally small, mostly standalone features which are added by using the API. They are implemented this way for two main reasons:

1.  To assist with updates/debugging, as they are not integrated with the main codebase
2.  To provide API examples for adding relatively complex features using entirely API hooks

## List of Gimmicks

-   [builders](../src/mobs/java/pokecube/gimmicks/builders/BuilderTasks.java) - AI tasks system to allow pokemobs to build structures based on instructions in their offhand slot.
    -   Example of AI adding
-   [dynamax](../src/mobs/java/pokecube/gimmicks/dynamax/DynamaxHelper.java) - Dynamax implementation
    -   Example of new genes adding
-   [evolutions](../src/mobs/java/pokecube/gimmicks/evolutions/GimmickEvos.java) - Handling for custom evolutions, such as Tyrouge, Shedinja, etc.
    -   Has example of making a json config file
-   [mega](../src/mobs/java/pokecube/gimmicks/mega/MegaEvolveHelper.java) - Mega evolution implementation
    -   Loads rules from data
-   [nests](../src/mobs/java/pokecube/gimmicks/nests/NestTasks.java) - handles durant nest building, bee hive mechanics and burrow digging
    -   Example of AI adding
-   [terastal](../src/mobs/java/pokecube/gimmicks/terastal/TerastalMechanic.java) - Terastal implementation
    -   Example of new genes adding
-   [zmoves](../src/mobs/java/pokecube/gimmicks/zmoves/GZMoveManager.java) - Z moves implementation
  - [pokeplayer](../src/mobs/java/pokecube/gimmicks/pokeplayer/Pokeplayer.java) - Pokeplayer implementation
    -   Example of adding and handling packets
    -   Example of adding and handling custom keybinds
-   [vanilla_pokemobs](../src/mobs/java/pokecube/gimmicks/vanilla_pokemobs/VanillaPokemobs.java) - Terastal implementation
    -   Example of adding and handling packets.
    -   Example of making arbitrary mobs pokemobs

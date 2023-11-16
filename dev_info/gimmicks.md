# Gimmick Features

Gimmicks in Pokecube are generally small, mostly standalone features which are added by using the API. They are implemented this way for two main reasons:

1.  To assist with updates/debugging, as they are not integrated with the main codebase
2.  To provide API examples for adding relatively complex features using entirely API hooks

## List of Gimmicks

-   [builders](../src/main/java/pokecube/gimmicks/builders/BuilderTasks.java) - AI tasks system to allow pokemobs to build structures based on instructions in their offhand slot.
    -   Example of AI adding
-   [dynamax](../src/main/java/pokecube/gimmicks/dynamax/DynamaxHelper.java) - Dynamax implementation
    -   Example of new genes adding
-   [evolutions](../src/main/java/pokecube/gimmicks/evolutions/GimmickEvos.java) - Handling for custom evolutions, such as Tyrouge, Shedinja, etc.
    -   Has example of making a json config file
-   [mega](../src/main/java/pokecube/gimmicks/mega/MegaEvolveHelper.java) - Mega evolution implementation
    -   Loads rules from data
-   [nests](../src/main/java/pokecube/gimmicks/nests/NestTasks.java) - handles durant nest building, bee hive mechanics and burrow digging
    -   Example of AI adding
-   [terastal](../src/main/java/pokecube/gimmicks/terastal/TerastalMechanic.java) - Terastal implementation
    -   Example of new genes adding
-   [zmoves](../src/main/java/pokecube/gimmicks/zmoves/GZMoveManager.java) - Z moves implementation
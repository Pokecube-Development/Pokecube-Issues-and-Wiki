# Instructions Books

Written and Writable books are used to provide a variety of instructions, beit for Pokemobs or Machines. Presently the following instructions are implemented:

-   Gene Selectors for Genetics Machines
-   Item Filters for pokemob storage/empty filtering
-   Instructions for pokemobs to build structures

## Instructions format

Instructions start with a line in the book containing a particular key, followed by a `:`. That line is then handed as the first entry of the list of instructions. Instructions are then read until either the end of the book, or a line starting with `end:` is located. This reading is handled by [BookInstructionsParser](../src/main/java/pokecube/api/utils/BookInstructionsParser.java#L81-L110). If the `includeHeader` argument is `false`, it will remove the key line before returning the list.

### Known Start Keys

-   `genes` - used for Gene Selectors
-   `item filters` - for pokemob storage/empty filtering
-   `build` - for pokemobs to build structures

### Handling Instructions

The instructions are a list of not-blank lines, including the key line, but not including the `end:` line. How these instructions are processed is up to the handler to decide.

-   `genes` - Treats the list as a list of gene ids. 
    -   For a single array gene also parses the array index from the following format: `<gene id>#<index>`
    -   does not include key
-   `item filters` - Treats the list as a list of item tags
    -   does not include key
-   [build](../src/main/java/pokecube/gimmicks/builders/builders/BuilderManager.java#L161-L1178) - Argument parsing defined by `<key>:<value>`, order defined as below
    1.   `build` - type of structure to build, defaults allowed are `jigsaw` and `building`
    2.   Next line is the resource location to build, following lines can be in any order, and are all optional
    -   `o` - origin - override location for where the structure is built
    -   `s` - shift - vector added to the origin
    -   `r` - Rotation - one of `NONE`, `CLOCKWISE_90`, `CLOCKWISE_180`, `COUNTERCLOCKWISE_90`
    -   `m` - Mirror - one of `NONE`, `LEFT_RIGHT`, `FRONT_BACK`
    -   `d` - Depth - used with `jigsaw` for the max recursion depth for building jigsaws
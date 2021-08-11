# Pokecube-Issues-and-Wiki
This is where all issues for the core pokecube mods should go, also this is where the main wiki should be maintained


Useful Documentation about the mod can be found here: https://pokecube.readthedocs.io/en/latest/

# Setting up Pokecube Development

Here are some instructions for setting up a development environment for Pokecube. This instruction set is for Eclipse, it may vary for a different IDE.

### Step 1: download this repository.

This can be easily done via `git clone https://github.com/Pokecube-Development/Pokecube-Issues-and-Wiki.git`, which should produce a Pokecube-Issues-and-Wiki directory, containing this git repository.

### Step 2: Import gradle project into Eclipse

In Eclipse, in `File->Import->Gradle`, select `Existing Gradle Project`, then for `Project root directory` select the directory cloned in Step 1, then press finish. This should start setting up the gradle project in eclipse, and may take a while to run, as it also downloads the forge SDK, and other dependencies.

### Step 3: Wait for Eclipse to finish building

After Step 2, Eclipse should try to build the project, this may also take a while, but once it finishes, you should be able to run Minecraft from inside Eclipse.
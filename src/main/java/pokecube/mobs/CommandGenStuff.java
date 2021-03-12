package pokecube.mobs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.ItemGenerator;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.megastuff.ItemMegawearable;
import pokecube.core.items.vitamins.ItemVitamin;
import pokecube.core.utils.PokeType;;

public class CommandGenStuff
{

    public static class AdvancementGenerator
    {
        static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

        public static JsonObject fromCriteria(final PokedexEntry entry, final String id)
        {
            final JsonObject critmap = new JsonObject();
            final JsonObject sub = new JsonObject();
            sub.addProperty("trigger", "pokecube:" + id);
            final JsonObject conditions = new JsonObject();
            conditions.addProperty("entry", entry.getTrimmedName());
            sub.add("conditions", conditions);
            critmap.add(id + "_" + entry.getTrimmedName(), sub);
            return critmap;
        }

        public static JsonObject fromInfo(final PokedexEntry entry, final String id)
        {
            final JsonObject displayJson = new JsonObject();
            final JsonObject icon = new JsonObject();
            icon.addProperty("item", "pokecube:pokecube");
            final JsonObject title = new JsonObject();
            title.addProperty("translate", "achievement.pokecube." + id);
            final JsonArray item = new JsonArray();
            final JsonObject pokemobName = new JsonObject();
            pokemobName.addProperty("translate", entry.getUnlocalizedName());
            item.add(pokemobName);
            title.add("with", item);
            final JsonObject description = new JsonObject();
            description.addProperty("translate", "achievement.pokecube." + id + ".desc");
            description.add("with", item);
            displayJson.add("icon", icon);
            displayJson.add("title", title);
            displayJson.add("description", description);
            if (entry.isLegendary()) displayJson.addProperty("frame", "challenge");
            return displayJson;
        }

        public static String makeJson(final PokedexEntry entry, final String id, String parent)
        {
            final JsonObject json = new JsonObject();
            json.add("display", AdvancementGenerator.fromInfo(entry, id));
            json.add("criteria", AdvancementGenerator.fromCriteria(entry, id));
            if (parent != null)
            {
                if (entry._evolvesFrom != null)
                {
                    final String newParent = id + "_" + entry._evolvesFrom.getTrimmedName();
                    parent = parent.replace("root", newParent);
                    parent = parent.replace("get_first_pokemob", newParent);

                }
                json.addProperty("parent", parent);
            }
            return AdvancementGenerator.GSON.toJson(json);
        }

        public static String[][] makeRequirements(final PokedexEntry entry)
        {
            return new String[][] { { entry.getTrimmedName() } };
        }
    }

    public static class SoundJsonGenerator
    {
        public static String generateSoundJson(final boolean small)
        {
            final JsonObject soundJson = new JsonObject();
            final List<PokedexEntry> pokedexEntries = Database.getSortedFormes();
            final Set<ResourceLocation> added = Sets.newHashSet();
            final int num = small ? 1 : 3;
            for (final PokedexEntry entry : pokedexEntries)
            {
                if (entry.getSoundEvent() == null)
                {
                    System.out.println(entry);
                    continue;
                }
                ResourceLocation event = entry.getSoundEvent().getLocation();
                if (added.contains(event)) continue;
                added.add(event);

                final String backup = "rattata";

                final ResourceLocation test = new ResourceLocation(event.getNamespace() + ":" + event.getPath()
                        .replaceFirst("mobs.", "sounds/mobs/") + ".ogg");
                try
                {
                    Minecraft.getInstance().getResourceManager().getResource(test);
                }
                catch (final Exception e)
                {
                    event = new ResourceLocation(backup);
                    System.out.println(entry + "->" + backup + " " + test);
                }

                final String soundName = event.getPath().replaceFirst("mobs.", "");
                final JsonObject soundEntry = new JsonObject();
                soundEntry.addProperty("category", "hostile");
                soundEntry.addProperty("subtitle", entry.getUnlocalizedName());
                final JsonArray sounds = new JsonArray();

                for (int i = 0; i < num; i++)
                {
                    final JsonObject sound = new JsonObject();
                    sound.addProperty("name", "pokecube_mobs:mobs/" + soundName);
                    if (!small)
                    {
                        sound.addProperty("volume", i == 0 ? 0.8 : i == 1 ? 0.9 : 1);
                        sound.addProperty("pitch", i == 0 ? 0.9 : i == 1 ? 0.95 : 1);
                    }
                    sounds.add(sound);
                }
                soundEntry.add("sounds", sounds);
                soundJson.add("mobs." + entry.getTrimmedName(), soundEntry);
            }
            return AdvancementGenerator.GSON.toJson(soundJson);
        }
    }

    public static void execute(final ServerPlayerEntity sender, final String[] args)
    {
        sender.sendMessage(new StringTextComponent("Starting File Output"), Util.NIL_UUID);
        for (final PokedexEntry e : Database.getSortedFormes())
        {
            if (e == Database.missingno || e.dummy || e.isMega()) continue;
            CommandGenStuff.registerAchievements(e);
        }
        sender.sendMessage(new StringTextComponent("Advancements Done"), Util.NIL_UUID);
        final File dir = new File("./mods/pokecube/assets/pokecube_mobs/");
        if (!dir.exists()) dir.mkdirs();
        File file = null;
        boolean small = false;
        for (final String s : args)
            if (s.startsWith("s")) small = true;
        String json = "";
        try
        {
            file = new File(dir, "sounds.json");
            json = SoundJsonGenerator.generateSoundJson(small);
            final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName(
                    "UTF-8").newEncoder());
            writer.write(json);
            writer.close();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        sender.sendMessage(new StringTextComponent("Sounds Done"), Util.NIL_UUID);
        CommandGenStuff.generateBlockAndItemJsons();
        CommandGenStuff.generateMobsLang();

        sender.sendMessage(new StringTextComponent("Finished File Output"), Util.NIL_UUID);
    }

    public static void generateMobsLang()
    {
        final JsonObject langJson = new JsonObject();
        final File dir = new File("./mods/pokecube/assets/pokecube_mobs/lang/");
        if (!dir.exists()) dir.mkdirs();

        langJson.addProperty("_comment", "Pokemob Names");

        for (PokedexEntry entry : Database.getSortedFormes())
        {
            final String name = entry.getUnlocalizedName();
            if (entry.getBaseForme() != null) entry = entry.getBaseForme();
            if (Database.dummyMap.containsKey(entry.getPokedexNb())) entry = Database.dummyMap.get(entry
                    .getPokedexNb());
            langJson.addProperty(name, entry.getName());
        }

        File file = new File(dir, "en_us.json");
        final String json = AdvancementGenerator.GSON.toJson(langJson);

        try
        {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8")
                    .newEncoder());
            writer.write(json);
            writer.close();

            try
            {
                file = new File(dir, "sounds.json");
                final String sounds = SoundJsonGenerator.generateSoundJson(false);
                writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8").newEncoder());
                writer.write(sounds);
                writer.close();
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }

    }

    public static void generateBlockAndItemJsons()
    {
        final boolean berries = true;
        final boolean cubes = false;
        final boolean vitamins = true;
        final boolean badges = true;
        final boolean megastones = true;
        final boolean megawearables = true;
        final boolean fossils = true;

        if (badges) for (final PokeType type : PokeType.values())
            CommandGenStuff.generateItemJson(type.name, "badge_", "pokecube_adventures", "pokecube_adventures");
        if (fossils) for (final String type : ItemGenerator.fossilVariants)
            CommandGenStuff.generateItemJson(type, "fossil_", "pokecube_mobs", "pokecube");
        if (megastones) for (final String type : ItemGenerator.variants)
            CommandGenStuff.generateItemJson(type, "", "pokecube_mobs", "pokecube");
        if (vitamins) for (final String type : ItemVitamin.vitamins)
            CommandGenStuff.generateItemJson(type, "vitamin_", "pokecube_mobs", "pokecube");
        if (megawearables) for (final String type : ItemMegawearable.getWearables())
        {
            final String dir = type.equals("ring") || type.equals("hat") || type.equals("belt") ? "pokecube"
                    : "pokecube_mobs";
            CommandGenStuff.generateItemJson(type, "mega_", dir, "pokecube");
        }

        if (berries) for (final String name : BerryManager.berryNames.values())
        {
            final String dir = name.equals("null") ? "pokecube" : "pokecube_mobs";
            CommandGenStuff.generateItemJson(name, "berry_", dir, "pokecube");
        }

        if (cubes) for (final ResourceLocation l : IPokecube.BEHAVIORS.getKeys())
        {
            final String cube = l.getPath();
            final JsonObject blockJson = new JsonObject();
            blockJson.addProperty("parent", "pokecube:block/pokecubes");
            final JsonObject textures = new JsonObject();
            textures.addProperty("top", "pokecube:items/" + cube + "cube" + "top");
            textures.addProperty("bottom", "pokecube:items/" + cube + "cube" + "bottom");
            textures.addProperty("front", "pokecube:items/" + cube + "cube" + "front");
            textures.addProperty("side", "pokecube:items/" + cube + "cube" + "side");
            textures.addProperty("back", "pokecube:items/" + cube + "cube" + "back");
            blockJson.add("textures", textures);

            File dir = new File("./mods/pokecube/assets/pokecube/models/block/");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, cube + "cube" + ".json");
            String json = AdvancementGenerator.GSON.toJson(blockJson);
            try
            {
                final FileWriter write = new FileWriter(file);
                write.write(json);
                write.close();
            }
            catch (final IOException e)
            {
                e.printStackTrace();
            }

            final JsonObject itemJson = new JsonObject();
            itemJson.addProperty("parent", "pokecube:block/" + cube + "cube");
            final JsonObject display = new JsonObject();
            final JsonObject thirdPerson = new JsonObject();
            final JsonArray rotation = new JsonArray();
            final JsonArray translation = new JsonArray();
            final JsonArray scale = new JsonArray();

            rotation.add(new JsonPrimitive(10));
            rotation.add(new JsonPrimitive(-45));
            rotation.add(new JsonPrimitive(170));

            translation.add(new JsonPrimitive(0));
            translation.add(new JsonPrimitive(1.5));
            translation.add(new JsonPrimitive(-2.75));

            scale.add(new JsonPrimitive(0.375));
            scale.add(new JsonPrimitive(0.375));
            scale.add(new JsonPrimitive(0.375));

            thirdPerson.add("rotation", rotation);
            thirdPerson.add("translation", translation);
            thirdPerson.add("scale", scale);
            display.add("thirdperson", thirdPerson);
            itemJson.add("display", display);

            dir = new File("./mods/pokecube/assets/pokecube/models/item/");
            if (!dir.exists()) dir.mkdirs();
            file = new File(dir, cube + "cube" + ".json");
            json = AdvancementGenerator.GSON.toJson(itemJson);
            try
            {
                final FileWriter write = new FileWriter(file);
                write.write(json);
                write.close();
            }
            catch (final IOException e)
            {
                e.printStackTrace();
            }

        }

        for (final Block b : ForgeRegistries.BLOCKS.getValues())
        {
            if (b.getRegistryName().toString().startsWith("minecraft")) continue;
            CommandGenStuff.generateBlockDropJson(b);
        }

        // Generate the berry log recipes
        for (final String s : ItemGenerator.berryWoods.keySet())
        {
            final Block log = ItemGenerator.logs.get(s);
            final Block plank = ItemGenerator.planks.get(s);
            if (log != null && plank != null)
            {
                final File dir = new File("./mods/data/" + log.getRegistryName().getNamespace() + "/recipes");
                dir.mkdirs();
                final File out = new File(dir, log.getRegistryName().getPath() + ".json");
                String loottable = "{\"type\":\"minecraft:crafting_shapeless\",\"group\":\"planks\",\"ingredients\":[{\"tag\":\""
                        + log.getRegistryName() + "\"}],\"result\":{\"item\":\"" + plank.getRegistryName()
                        + "\",\"count\":4}}";
                final JsonObject obj = AdvancementGenerator.GSON.fromJson(loottable, JsonObject.class);
                loottable = AdvancementGenerator.GSON.toJson(obj);
                FileWriter write;
                try
                {
                    write = new FileWriter(out);
                    write.write(loottable);
                    write.close();
                }
                catch (final IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        // Generate some more item related tags
    }

    private static void generateBlockDropJson(final Block block)
    {
        final File dir = new File("./mods/data/" + block.getRegistryName().getNamespace() + "/loot_tables/blocks");
        dir.mkdirs();
        final File out = new File(dir, block.getRegistryName().getPath() + ".json");

        String loottable = "{\"type\": \"minecraft:block\",\"pools\":[{\"name\":\"pool_0\",\"rolls\":1,\"entries\":[{\"type\":\"minecraft:item\",\"name\":\""
                + block.getRegistryName()
                + "\"}],\"conditions\":[{\"condition\": \"minecraft:survives_explosion\"}]}]}";
        final JsonObject obj = AdvancementGenerator.GSON.fromJson(loottable, JsonObject.class);
        loottable = AdvancementGenerator.GSON.toJson(obj);
        FileWriter write;
        try
        {
            write = new FileWriter(out);
            write.write(loottable);
            write.close();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void generateItemJson(String name, final String prefix, final String outerdir, final String innerdir)
    {
        if (name.equals("???")) name = "unknown";
        final JsonObject blockJson = new JsonObject();
        blockJson.addProperty("parent", "item/generated");
        final JsonObject textures = new JsonObject();

        final Map<String, String> meganames = Maps.newHashMap();
        meganames.put("aerodactylmega", "pokecube:items/aerodactylite");
        meganames.put("abomasnowmega", "pokecube:items/abomasite");
        meganames.put("absolmega", "pokecube:items/absolite");
        meganames.put("aggronmega", "pokecube:items/aggronite");
        meganames.put("alakazammega", "pokecube:items/alakazite");
        meganames.put("altariamega", "pokecube:items/altarianite");
        meganames.put("ampharosmega", "pokecube:items/ampharosite");
        meganames.put("audinomega", "pokecube:items/audinite");
        meganames.put("banettemega", "pokecube:items/banettite");
        meganames.put("beedrillmega", "pokecube:items/beedrillite");
        meganames.put("blastoisemega", "pokecube:items/blastoisinite");
        meganames.put("blazikenmega", "pokecube:items/blazikenite");
        meganames.put("cameruptmega", "pokecube:items/cameruptite");
        meganames.put("charizardmega-x", "pokecube:items/charizardite_x");
        meganames.put("charizardmega-y", "pokecube:items/charizardite_y");
        meganames.put("dianciemega", "pokecube:items/diancite");
        meganames.put("gallademega", "pokecube:items/galladite");
        meganames.put("garchompmega", "pokecube:items/garchompite");
        meganames.put("gardevoirmega", "pokecube:items/gardevoirite");
        meganames.put("gengarmega", "pokecube:items/gengarite");
        meganames.put("glaliemega", "pokecube:items/glalitite");
        meganames.put("gyaradosmega", "pokecube:items/gyaradosite");
        meganames.put("heracrossmega", "pokecube:items/heracronite");
        meganames.put("houndoommega", "pokecube:items/houndoominite");
        meganames.put("kangaskhanmega", "pokecube:items/kangaskhanite");
        meganames.put("latiasmega", "pokecube:items/latiasite");
        meganames.put("latiosmega", "pokecube:items/latiosite");
        meganames.put("lopunnymega", "pokecube:items/lopunnite");
        meganames.put("lucariomega", "pokecube:items/lucarionite");
        meganames.put("manectricmega", "pokecube:items/manectite");
        meganames.put("mawilemega", "pokecube:items/mawilite");
        meganames.put("medichammega", "pokecube:items/medichamite");
        meganames.put("metagrossmega", "pokecube:items/metagrossite");
        meganames.put("mewtwomega-x", "pokecube:items/mewtwonite_x");
        meganames.put("mewtwomega-y", "pokecube:items/mewtwonite_y");
        meganames.put("pidgeotmega", "pokecube:items/pidgeotite");
        meganames.put("pinsirmega", "pokecube:items/pinsirite");
        meganames.put("sableyemega", "pokecube:items/sablenite");
        meganames.put("salamencemega", "pokecube:items/salamencite");
        meganames.put("sceptilemega", "pokecube:items/sceptilite");
        meganames.put("scizormega", "pokecube:items/scizorite");
        meganames.put("sharpedomega", "pokecube:items/sharpedonite");
        meganames.put("slowbromega", "pokecube:items/slowbronite");
        meganames.put("steelixmega", "pokecube:items/steelixite");
        meganames.put("swampertmega", "pokecube:items/swampertite");
        meganames.put("tyranitarmega", "pokecube:items/tyranitarite");
        meganames.put("venusaurmega", "pokecube:items/venusaurite");

        String tex = innerdir + ":items/" + prefix + name;
        if (meganames.containsKey(name)) tex = meganames.get(name);

        textures.addProperty("layer0", tex);
        blockJson.add("textures", textures);
        final File dir = new File("./mods/" + outerdir + "/assets/" + innerdir + "/models/item/");
        dir.mkdirs();
        final File file = new File(dir, prefix + name + ".json");
        final String json = AdvancementGenerator.GSON.toJson(blockJson);
        try
        {
            final FileWriter write = new FileWriter(file);
            write.write(json);
            write.close();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
    }

    protected static void make(final PokedexEntry entry, final String id, final String parent, final String path)
    {
        final ResourceLocation key = new ResourceLocation(entry.getModId(), id + "_" + entry.getTrimmedName());
        String json = AdvancementGenerator.makeJson(entry, id, parent);
        final File dir = new File("./mods/pokecube/data/pokecube_mobs/advancements/" + path + "/");
        if (!dir.exists()) dir.mkdirs();
        final File file = new File(dir, key.getPath() + ".json");
        FileWriter write;
        try
        {
            write = new FileWriter(file);
            write.write(json);
            write.close();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        if (id.equals("catch"))
        {
            final File first = new File(dir, "get_first_pokemob.json");
            if (!first.exists())
            {
                final JsonObject rootObj = new JsonObject();
                final JsonObject displayJson = new JsonObject();
                final JsonObject icon = new JsonObject();
                icon.addProperty("item", "pokecube:pokecube");
                final JsonObject title = new JsonObject();
                title.addProperty("translate", "achievement.pokecube.get1st");
                final JsonObject description = new JsonObject();
                description.addProperty("translate", "achievement.pokecube.get1st.desc");
                displayJson.add("icon", icon);
                displayJson.add("title", title);
                displayJson.add("description", description);
                final JsonObject critmap = new JsonObject();
                final JsonObject sub = new JsonObject();
                sub.addProperty("trigger", "pokecube:get_first_pokemob");
                critmap.add("get_first_pokemob", sub);
                rootObj.add("display", displayJson);
                rootObj.addProperty("parent", "pokecube_mobs:capture/root");
                rootObj.add("criteria", critmap);
                json = AdvancementGenerator.GSON.toJson(rootObj);
                try
                {
                    write = new FileWriter(first);
                    write.write(json);
                    write.close();
                }
                catch (final IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        final File root = new File(dir, "root.json");
        if (!root.exists())
        {
            final JsonObject rootObj = new JsonObject();
            final JsonObject displayJson = new JsonObject();
            final JsonObject icon = new JsonObject();
            icon.addProperty("item", "pokecube:pokecube");
            final JsonObject title = new JsonObject();
            title.addProperty("translate", "achievement.pokecube." + id + ".root");
            final JsonObject description = new JsonObject();
            description.addProperty("translate", "achievement.pokecube." + id + ".root.desc");
            displayJson.add("icon", icon);
            displayJson.add("title", title);
            displayJson.add("description", description);
            displayJson.addProperty("background", "minecraft:textures/gui/advancements/backgrounds/adventure.png");
            displayJson.addProperty("show_toast", false);
            displayJson.addProperty("announce_to_chat", false);
            final JsonObject critmap = new JsonObject();
            final JsonObject sub = new JsonObject();
            sub.addProperty("trigger", "pokecube:get_first_pokemob");
            critmap.add("get_first_pokemob", sub);
            rootObj.add("display", displayJson);
            rootObj.add("criteria", critmap);
            json = AdvancementGenerator.GSON.toJson(rootObj);
            try
            {
                write = new FileWriter(root);
                write.write(json);
                write.close();
            }
            catch (final IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /** Comment these out to re-generate advancements. */
    public static void registerAchievements(final PokedexEntry entry)
    {
        CommandGenStuff.make(entry, "catch", "pokecube_mobs:capture/get_first_pokemob", "capture");
        CommandGenStuff.make(entry, "kill", "pokecube_mobs:kill/root", "kill");
        CommandGenStuff.make(entry, "hatch", "pokecube_mobs:hatch/root", "hatch");
    }
}

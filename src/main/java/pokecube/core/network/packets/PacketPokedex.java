package pokecube.core.network.packets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.GuiPokedex;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.database.rewards.XMLRewardsHandler;
import pokecube.core.database.tags.Tags;
import pokecube.core.database.util.QNameAdaptor;
import pokecube.core.database.util.UnderscoreIgnore;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.PokedexInspector;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.handlers.events.SpawnHandler.ForbidReason;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.commandhandlers.TeleportHandler;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.world.dimension.SecretBaseDimension;
import thut.api.entity.ThutTeleporter.TeleDest;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;
import thut.core.common.handlers.PlayerDataHandler;
import thut.core.common.network.Packet;

public class PacketPokedex extends Packet
{
    public static final byte REWARDS      = -13;
    public static final byte BREEDLIST    = -12;
    public static final byte OPEN         = -11;
    public static final byte REORDER      = -10;
    public static final byte INSPECTMOB   = -9;
    public static final byte SETWATCHPOKE = -8;
    public static final byte REQUESTLOC   = -7;
    public static final byte REQUESTMOB   = -6;
    public static final byte REQUEST      = -5;
    public static final byte INSPECT      = -4;
    public static final byte BASERADAR    = -3;
    public static final byte REMOVE       = -2;
    public static final byte RENAME       = -1;

    private static final Gson gson = new GsonBuilder().registerTypeAdapter(QName.class, QNameAdaptor.INSTANCE)
            .setExclusionStrategies(UnderscoreIgnore.INSTANCE).create();

    public static List<String>                         values       = Lists.newArrayList();
    public static List<SpawnBiomeMatcher>              selectedMob  = Lists.newArrayList();
    public static Map<PokedexEntry, SpawnBiomeMatcher> selectedLoc  = Maps.newHashMap();
    public static Map<String, List<String>>            relatedLists = Maps.newHashMap();

    public static boolean repelled = false;

    @OnlyIn(value = Dist.CLIENT)
    public static void sendChangePagePacket(final byte page, final boolean mode, final PokedexEntry selected)
    {
        final PacketPokedex packet = new PacketPokedex();
        packet.message = page;
        packet.data.putBoolean("M", mode);
        if (selected != null) packet.data.putString("F", selected.getName());
        PokecubeCore.packets.sendToServer(packet);
    }

    @OnlyIn(value = Dist.CLIENT)
    public static void sendInspectPacket(final boolean reward, final String lang)
    {
        final PacketPokedex packet = new PacketPokedex();
        packet.message = PacketPokedex.INSPECT;
        packet.data.putBoolean("R", reward);
        packet.data.putString("L", lang);
        PokecubeCore.packets.sendToServer(packet);
    }

    @OnlyIn(value = Dist.CLIENT)
    public static void sendInspectPacket(final IPokemob pokemob)
    {
        final PacketPokedex packet = new PacketPokedex(PacketPokedex.INSPECTMOB);
        PlayerDataHandler.getInstance().getPlayerData(PokecubeCore.proxy.getPlayer()).getData(PokecubePlayerStats.class)
                .inspect(PokecubeCore.proxy.getPlayer(), pokemob);
        packet.data.putInt("V", pokemob.getEntity().getEntityId());
        PokecubeCore.packets.sendToServer(packet);
    }

    @OnlyIn(value = Dist.CLIENT)
    public static void sendLocationSpawnsRequest()
    {
        PacketPokedex.selectedLoc.clear();
        final PacketPokedex packet = new PacketPokedex(PacketPokedex.REQUESTLOC);
        PokecubeCore.packets.sendToServer(packet);
    }

    public static void sendOpenPacket(final ServerPlayerEntity player, final Entity pokemob, final boolean watch)
    {
        final PacketPokedex packet = new PacketPokedex();
        packet.message = PacketPokedex.OPEN;
        packet.data.putBoolean("W", watch);
        if (pokemob != null) packet.data.putInt("M", pokemob.getEntityId());
        PokecubeCore.packets.sendTo(packet, player);
    }

    @OnlyIn(value = Dist.CLIENT)
    public static void sendRemoveTelePacket(final int index)
    {
        final PacketPokedex packet = new PacketPokedex();
        packet.message = PacketPokedex.REMOVE;
        packet.data.putInt("I", index);
        PokecubeCore.packets.sendToServer(packet);
    }

    @OnlyIn(value = Dist.CLIENT)
    public static void sendRenameTelePacket(final String newName, final int index)
    {
        final PacketPokedex packet = new PacketPokedex();
        packet.message = PacketPokedex.RENAME;
        packet.data.putString("N", newName);
        packet.data.putInt("I", index);
        PokecubeCore.packets.sendToServer(packet);
    }

    @OnlyIn(value = Dist.CLIENT)
    public static void sendReorderTelePacket(final int index1, final int index2)
    {
        final PacketPokedex packet = new PacketPokedex();
        packet.message = PacketPokedex.REORDER;
        packet.data.putInt("2", index2);
        packet.data.putInt("1", index1);
        PokecubeCore.packets.sendToServer(packet);
        TeleportHandler.swapTeleports(PokecubeCore.proxy.getPlayer().getCachedUniqueIdString(), index1, index2);
    }

    public static void sendSecretBaseInfoPacket(final ServerPlayerEntity player, final boolean watch)
    {
        final PacketPokedex packet = new PacketPokedex();
        ListNBT list = new ListNBT();
        final BlockPos pos = player.getPosition();
        final GlobalPos here = GlobalPos.getPosition(player.getEntityWorld().getDimensionKey(), pos);
        for (final GlobalPos c : SecretBaseDimension.getNearestBases(here, PokecubeCore.getConfig().baseRadarRange))
        {
            final CompoundNBT nbt = NBTUtil.writeBlockPos(c.getPos());
            list.add(nbt);
        }
        packet.data.put("_bases_", list);
        packet.data.putBoolean("M", watch);
        packet.data.putInt("R", PokecubeCore.getConfig().baseRadarRange);
        final List<GlobalPos> meteors = Lists.newArrayList(PokecubeSerializer.getInstance().meteors);
        meteors.removeIf(p -> p.getDimension() != here.getDimension());
        meteors.sort((c1, c2) ->
        {
            final int d1 = c1.getPos().compareTo(pos);
            final int d2 = c2.getPos().compareTo(pos);
            return d2 - d1;
        });
        list = new ListNBT();
        for (int i = 0; i < Math.min(10, meteors.size()); i++)
        {
            final CompoundNBT nbt = NBTUtil.writeBlockPos(meteors.get(i).getPos());
            list.add(nbt);
        }
        packet.data.put("_meteors_", list);
        packet.message = PacketPokedex.BASERADAR;
        PokecubeCore.packets.sendTo(packet, player);
    }

    @OnlyIn(value = Dist.CLIENT)
    public static void sendSpecificSpawnsRequest(final PokedexEntry entry)
    {
        final PacketPokedex packet = new PacketPokedex(PacketPokedex.REQUESTMOB);
        packet.data.putString("V", entry.getName());
        PokecubeCore.packets.sendToServer(packet);
    }

    @OnlyIn(value = Dist.CLIENT)
    public static void updateWatchEntry(final PokedexEntry entry)
    {
        final String name = PokecubePlayerDataHandler.getCustomDataTag(PokecubeCore.proxy.getPlayer()).getString(
                "WEntry");
        if (Database.getEntry(name) == entry) return;
        final PacketPokedex packet = new PacketPokedex(PacketPokedex.SETWATCHPOKE);
        PokecubePlayerDataHandler.getCustomDataTag(PokecubeCore.proxy.getPlayer()).putString("WEntry", entry.getName());
        packet.data.putString("V", entry.getName());
        PokecubeCore.packets.sendToServer(packet);
    }

    public static void sendLoginPacket(final ServerPlayerEntity target)
    {
        for (final String reward : XMLRewardsHandler.loadedRecipes)
        {
            final PacketPokedex message = new PacketPokedex(PacketPokedex.REWARDS);
            message.data.putString("R", reward);
            PokecubeCore.packets.sendTo(message, target);
        }
    }

    byte message;

    public CompoundNBT data = new CompoundNBT();

    public PacketPokedex()
    {
    }

    public PacketPokedex(final byte message)
    {
        this.message = message;
    }

    public PacketPokedex(final PacketBuffer buf)
    {
        this.message = buf.readByte();
        if (buf.isReadable()) this.data = buf.readCompoundTag();
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public void handleClient()
    {
        final PlayerEntity player = PokecubeCore.proxy.getPlayer();
        int n = 0;
        int num = 0;
        CompoundNBT data;
        switch (this.message)
        {
        case OPEN:
            final Entity mob = PokecubeCore.getEntityProvider().getEntity(player.getEntityWorld(), this.data.getInt(
                    "M"), true);
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
            final boolean watch = this.data.getBoolean("W");
            if (watch) net.minecraft.client.Minecraft.getInstance().displayGuiScreen(new GuiPokeWatch(player,
                    mob instanceof LivingEntity ? (LivingEntity) mob : null));
            else net.minecraft.client.Minecraft.getInstance().displayGuiScreen(new GuiPokedex(pokemob, player));
            return;
        case REQUEST:
            PacketPokedex.values.clear();
            n = this.data.keySet().size();
            for (int i = 0; i < n; i++)
                PacketPokedex.values.add(this.data.getString("" + i));
            return;
        case BASERADAR:
            if (this.data.contains("_meteors_") && this.data.get("_meteors_") instanceof ListNBT)
            {
                final ListNBT list = (ListNBT) this.data.get("_meteors_");
                pokecube.core.client.gui.watch.SecretBaseRadarPage.meteors.clear();
                for (int i = 0; i < list.size(); i++)
                {
                    final CompoundNBT tag = list.getCompound(i);
                    pokecube.core.client.gui.watch.SecretBaseRadarPage.meteors.add(NBTUtil.readBlockPos(tag));
                }
            }
            if (this.data.contains("_bases_") && this.data.get("_bases_") instanceof ListNBT)
            {
                final ListNBT list = (ListNBT) this.data.get("_bases_");
                pokecube.core.client.gui.watch.SecretBaseRadarPage.bases.clear();
                for (int i = 0; i < list.size(); i++)
                {
                    final CompoundNBT tag = list.getCompound(i);
                    pokecube.core.client.gui.watch.SecretBaseRadarPage.bases.add(NBTUtil.readBlockPos(tag));
                }
            }
            pokecube.core.client.gui.watch.SecretBaseRadarPage.baseRange = this.data.getInt("R");
            return;
        case REQUESTLOC:
            PacketPokedex.selectedLoc.clear();
            data = this.data.getCompound("V");
            n = data.keySet().size() / 2;
            for (int i = 0; i < n; i++)
                PacketPokedex.selectedLoc.put(Database.getEntry(data.getString("e" + i)), PacketPokedex.gson.fromJson(
                        data.getString("" + i), SpawnBiomeMatcher.class));
            if (this.data.contains("E")) PokecubePlayerDataHandler.getCustomDataTag(player).putString("WEntry",
                    this.data.getString("E"));
            PacketPokedex.repelled = this.data.getBoolean("R");
            return;
        case REQUESTMOB:
            PacketPokedex.selectedMob.clear();
            data = this.data.getCompound("V");
            num = data.getInt("n");
            for (int i = 0; i < num; i++)
                PacketPokedex.selectedMob.add(PacketPokedex.gson.fromJson(data.getString("" + i),
                        SpawnBiomeMatcher.class));
            return;
        case BREEDLIST:
            data = this.data.getCompound("V");
            final String entry = this.data.getString("e");
            final List<String> related = Lists.newArrayList();

            System.out.println(Database.getEntry(entry) + " " + Tags.BREEDING.lookupTags(entry));

            num = data.getInt("n");
            for (int i = 0; i < num; i++)
                related.add(data.getString("" + i));
            PacketPokedex.relatedLists.put(entry, related);
            return;
        case REWARDS:
            final String reward = this.data.getString("R");
            if (!reward.isEmpty()) Database.loadRewards(reward);
            pokecube.core.client.gui.GuiInfoMessages.clear();
            return;
        }
    }

    @Override
    public void handleServer(final ServerPlayerEntity player)
    {
        // Declair some stuff before the switch;
        IPokemob pokemob;
        Entity mob;
        PacketPokedex packet;
        Map<PokedexEntry, Float> rates;
        Vector3 pos;
        SpawnCheck checker;
        ArrayList<PokedexEntry> names = new ArrayList<>();
        float total = 0;
        int n = 0;
        List<String> biomes;
        PokedexEntry entry;
        SpawnData data;
        final CompoundNBT spawns = new CompoundNBT();
        switch (this.message)
        {
        case INSPECTMOB:
            mob = PokecubeCore.getEntityProvider().getEntity(player.getEntityWorld(), this.data.getInt("V"), true);
            pokemob = CapabilityPokemob.getPokemobFor(mob);
            if (pokemob != null) PlayerDataHandler.getInstance().getPlayerData(player).getData(
                    PokecubePlayerStats.class).inspect(player, pokemob);
            return;
        case REQUESTMOB:
            pos = Vector3.getNewVector().set(player);
            checker = new SpawnCheck(pos, player.getEntityWorld());
            entry = Database.getEntry(this.data.getString("V"));
            packet = new PacketPokedex(PacketPokedex.REQUESTMOB);
            if (entry.getSpawnData() != null) for (final SpawnBiomeMatcher matcher : entry.getSpawnData().matchers
                    .keySet())
            {
                spawns.putString("" + n, PacketPokedex.gson.toJson(matcher));
                n++;
            }
            spawns.putInt("n", n);
            packet.data.put("V", spawns);
            PokecubeCore.packets.sendTo(packet, player);
            packet = new PacketPokedex(PacketPokedex.BREEDLIST);
            final CompoundNBT breedable = new CompoundNBT();
            packet.data.putString("e", entry.getTrimmedName());
            breedable.putString("0", entry.getTrimmedName());
            n = 1;
            for (final PokedexEntry e : entry.getRelated())
            {
                breedable.putString("" + n, e.getTrimmedName());
                n++;
            }
            breedable.putInt("n", n);
            packet.data.put("V", breedable);
            PokecubeCore.packets.sendTo(packet, player);
            return;
        case REQUESTLOC:
            rates = Maps.newHashMap();
            pos = Vector3.getNewVector().set(player);
            checker = new SpawnCheck(pos, player.getEntityWorld());
            names = new ArrayList<>();
            final boolean repelled = SpawnHandler.getNoSpawnReason(player.getEntityWorld(), pos
                    .getPos()) != ForbidReason.NONE;
            for (final PokedexEntry e : Database.spawnables)
                if (e.getSpawnData().getMatcher(checker, false) != null) names.add(e);
            final Map<PokedexEntry, SpawnBiomeMatcher> matchers = Maps.newHashMap();
            for (final PokedexEntry e : names)
            {
                final SpawnBiomeMatcher matcher = e.getSpawnData().getMatcher(checker, false);
                matchers.put(e, matcher);
                float val = e.getSpawnData().getWeight(matcher);
                final float min = e.getSpawnData().getMin(matcher);
                final float num = min + (e.getSpawnData().getMax(matcher) - min) / 2;
                val *= num;
                total += val;
                rates.put(e, val);
            }
            if (total != 0) for (final PokedexEntry e : names)
            {
                final float val = rates.get(e) / total;
                rates.put(e, val);
            }
            packet = new PacketPokedex(PacketPokedex.REQUESTLOC);
            for (final PokedexEntry e : names)
            {
                final SpawnBiomeMatcher matcher = matchers.get(e);
                matcher.spawnRule.values.put(new QName("Local_Rate"), rates.get(e) + "");
                spawns.putString("e" + n, e.getName());
                spawns.putString("" + n, PacketPokedex.gson.toJson(matcher));
                n++;
            }
            packet.data.put("V", spawns);
            packet.data.putBoolean("R", repelled);
            entry = Database.getEntry(PokecubePlayerDataHandler.getCustomDataTag(player).getString("WEntry"));
            if (entry != null) packet.data.putString("E", entry.getName());
            PokecubeCore.packets.sendTo(packet, player);
            return;
        case REQUEST:
            final boolean mode = this.data.getBoolean("M");
            packet = new PacketPokedex(PacketPokedex.REQUEST);
            if (!mode)
            {
                rates = Maps.newHashMap();
                pos = Vector3.getNewVector().set(player);
                checker = new SpawnCheck(pos, player.getEntityWorld());
                names = new ArrayList<>();
                for (final PokedexEntry e : Database.spawnables)
                    if (e.getSpawnData().getMatcher(checker, false) != null) names.add(e);
                Collections.sort(names, (o1, o2) ->
                {
                    final float rate1 = o1.getSpawnData().getWeight(o1.getSpawnData().getMatcher(checker, false))
                            * 10e5f;
                    final float rate2 = o2.getSpawnData().getWeight(o2.getSpawnData().getMatcher(checker, false))
                            * 10e5f;
                    return (int) (-rate1 + rate2);
                });
                for (final PokedexEntry e : names)
                {
                    final SpawnBiomeMatcher matcher = e.getSpawnData().getMatcher(checker, false);
                    float val = e.getSpawnData().getWeight(matcher);
                    final float min = e.getSpawnData().getMin(matcher);
                    final float num = min + (e.getSpawnData().getMax(matcher) - min) / 2;
                    val *= num;
                    total += val;
                    rates.put(e, val);
                }
                if (total != 0) for (final PokedexEntry e : names)
                {
                    final float val = rates.get(e) * 100 / total;
                    rates.put(e, val);
                }
                final int biome = TerrainManager.getInstance().getTerrainForEntity(player).getBiome(pos);
                packet.data.putString("0", "" + biome);
                packet.data.putString("1", BiomeDatabase.getUnlocalizedNameFromType(biome));
                for (int i = 0; i < names.size(); i++)
                {
                    final PokedexEntry e = names.get(i);
                    packet.data.putString("" + (i + 2), e.getUnlocalizedName() + "`" + rates.get(e));
                }
            }
            else
            {
                biomes = Lists.newArrayList();
                entry = Database.getEntry(this.data.getString("F"));
                if (entry.getSpawnData() == null && entry.getChild() != entry)
                {
                    PokedexEntry child;
                    if ((child = entry.getChild()).getSpawnData() != null) entry = child;
                }
                data = entry.getSpawnData();
                if (data != null)
                {
                    boolean hasBiomes = false;
                    for (final SpawnBiomeMatcher matcher : data.matchers.keySet())
                    {
                        final String biomeString = matcher.spawnRule.values.get(SpawnBiomeMatcher.BIOMES);
                        final String typeString = matcher.spawnRule.values.get(SpawnBiomeMatcher.TYPES);
                        if (biomeString != null) hasBiomes = true;
                        else if (typeString != null)
                        {
                            final String[] args = typeString.split(",");
                            BiomeType subBiome = null;
                            for (final String s : args)
                            {
                                for (final BiomeType b : BiomeType.values())
                                    if (b.name.replaceAll(" ", "").equalsIgnoreCase(s))
                                    {
                                        subBiome = b;
                                        break;
                                    }
                                if (subBiome == null) hasBiomes = true;
                                subBiome = null;
                                if (hasBiomes) break;
                            }
                        }
                        if (hasBiomes) break;
                    }
                    if (hasBiomes) for (final RegistryKey<Biome> b : SpawnBiomeMatcher.getAllBiomes())
                        if (b != null) if (data.isValid(b)) biomes.add(b.getRegistryName().toString());
                    for (final BiomeType b : BiomeType.values())
                        if (data.isValid(b)) biomes.add(b.readableName);
                    for (int i = 0; i < biomes.size(); i++)
                        packet.data.putString("" + i, biomes.get(i));
                }
            }
            PokecubeCore.packets.sendTo(packet, player);
        case REORDER:
            final int index1 = this.data.getInt("1");
            final int index2 = this.data.getInt("2");
            PlayerDataHandler.getInstance().save(player.getCachedUniqueIdString());
            TeleportHandler.swapTeleports(player.getCachedUniqueIdString(), index1, index2);
            PacketDataSync.syncData(player, "pokecube-data");
            return;
        case REMOVE:
            final int index = this.data.getInt("I");
            final TeleDest loc = TeleportHandler.getTeleport(player.getCachedUniqueIdString(), index);
            TeleportHandler.unsetTeleport(index, player.getCachedUniqueIdString());
            player.sendMessage(new StringTextComponent("Deleted " + loc.getName()), Util.DUMMY_UUID);
            PlayerDataHandler.getInstance().save(player.getCachedUniqueIdString());
            PacketDataSync.syncData(player, "pokecube-data");
            return;
        case RENAME:
            final String name = this.data.getString("N");
            TeleportHandler.renameTeleport(player.getCachedUniqueIdString(), this.data.getInt("I"), name);
            player.sendMessage(new StringTextComponent("Set teleport as " + name), Util.DUMMY_UUID);
            PlayerDataHandler.getInstance().save(player.getCachedUniqueIdString());
            PacketDataSync.syncData(player, "pokecube-data");
            return;
        case INSPECT:
            final boolean reward = this.data.getBoolean("R");
            final String lang = this.data.getString("L");
            PokecubePlayerDataHandler.getCustomDataTag(player).putString("lang", lang);
            final boolean inspected = PokedexInspector.inspect(player, reward);

            if (!reward)
            {
                if (inspected) player.sendMessage(new TranslationTextComponent("pokedex.inspect.available"),
                        Util.DUMMY_UUID);
            }
            else
            {
                if (!inspected) player.sendMessage(new TranslationTextComponent("pokedex.inspect.nothing"),
                        Util.DUMMY_UUID);
                player.closeScreen();
            }
            return;
        }
        if (this.data.contains("E")) PokecubePlayerDataHandler.getCustomDataTag(player).putString("WEntry", this.data
                .getString("E"));
    }

    @Override
    public void write(final PacketBuffer buf)
    {
        buf.writeByte(this.message);
        if (!this.data.isEmpty()) buf.writeCompoundTag(this.data);
    }
}

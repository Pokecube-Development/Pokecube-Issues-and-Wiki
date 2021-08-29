package pokecube.core.network.packets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
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
import pokecube.core.database.stats.ISpecialCaptureCondition;
import pokecube.core.database.util.QNameAdaptor;
import pokecube.core.database.util.UnderscoreIgnore;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.PokedexInspector;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.handlers.events.SpawnHandler.ForbidReason;
import pokecube.core.handlers.events.SpawnHandler.ForbiddenEntry;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.commandhandlers.TeleportHandler;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.world.dimension.SecretBaseDimension;
import thut.api.entity.ThutTeleporter.TeleDest;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;
import thut.core.common.handlers.PlayerDataHandler;
import thut.core.common.network.NBTPacket;
import thut.core.common.network.PacketAssembly;

public class PacketPokedex extends NBTPacket
{
    public static final PacketAssembly<PacketPokedex> ASSEMBLER = PacketAssembly.registerAssembler(PacketPokedex.class,
            PacketPokedex::new, PokecubeCore.packets);

    public static final byte CHECKLEGEND  = -14;
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

    public static final Gson gson = new GsonBuilder().registerTypeAdapter(QName.class, QNameAdaptor.INSTANCE)
            .setExclusionStrategies(UnderscoreIgnore.INSTANCE).create();

    public static List<String>                         values       = Lists.newArrayList();
    public static List<SpawnBiomeMatcher>              selectedMob  = Lists.newArrayList();
    public static Map<PokedexEntry, SpawnBiomeMatcher> selectedLoc  = Maps.newHashMap();
    public static Map<String, List<String>>            relatedLists = Maps.newHashMap();

    public static Set<PokedexEntry> haveConditions = Sets.newHashSet();

    public static Set<PokedexEntry> noBreeding = Sets.newHashSet();

    public static boolean repelled = false;

    @OnlyIn(value = Dist.CLIENT)
    public static void sendChangePagePacket(final byte page, final boolean mode, final PokedexEntry selected)
    {
        final PacketPokedex packet = new PacketPokedex(page);
        packet.getTag().putBoolean("M", mode);
        if (selected != null) packet.getTag().putString("F", selected.getName());
        PacketPokedex.ASSEMBLER.sendToServer(packet);
    }

    @OnlyIn(value = Dist.CLIENT)
    public static void sendInspectPacket(final boolean reward, final String lang)
    {
        final PacketPokedex packet = new PacketPokedex(PacketPokedex.INSPECT);
        packet.getTag().putBoolean("R", reward);
        packet.getTag().putString("L", lang);
        PacketPokedex.ASSEMBLER.sendToServer(packet);
    }

    @OnlyIn(value = Dist.CLIENT)
    public static void sendInspectPacket(final IPokemob pokemob)
    {
        final PacketPokedex packet = new PacketPokedex(PacketPokedex.INSPECTMOB);
        PlayerDataHandler.getInstance().getPlayerData(PokecubeCore.proxy.getPlayer()).getData(PokecubePlayerStats.class)
                .inspect(PokecubeCore.proxy.getPlayer(), pokemob);
        packet.getTag().putInt("V", pokemob.getEntity().getId());
        PacketPokedex.ASSEMBLER.sendToServer(packet);
    }

    @OnlyIn(value = Dist.CLIENT)
    public static void sendCaptureCheck(final PokedexEntry pokemob)
    {
        final PacketPokedex packet = new PacketPokedex(PacketPokedex.CHECKLEGEND);
        packet.getTag().putString("V", pokemob.getTrimmedName());
        PacketPokedex.ASSEMBLER.sendToServer(packet);
    }

    @OnlyIn(value = Dist.CLIENT)
    public static void sendLocationSpawnsRequest()
    {
        PacketPokedex.selectedLoc.clear();
        final PacketPokedex packet = new PacketPokedex(PacketPokedex.REQUESTLOC);
        PacketPokedex.ASSEMBLER.sendToServer(packet);
    }

    public static void sendOpenPacket(final ServerPlayerEntity player, final Entity pokemob, final boolean watch)
    {
        final PacketPokedex packet = new PacketPokedex(PacketPokedex.OPEN);
        packet.getTag().putBoolean("W", watch);
        if (pokemob != null) packet.getTag().putInt("M", pokemob.getId());
        PacketPokedex.ASSEMBLER.sendTo(packet, player);
    }

    @OnlyIn(value = Dist.CLIENT)
    public static void sendRemoveTelePacket(final int index)
    {
        final PacketPokedex packet = new PacketPokedex(PacketPokedex.REMOVE);
        packet.getTag().putInt("I", index);
        PacketPokedex.ASSEMBLER.sendToServer(packet);
    }

    @OnlyIn(value = Dist.CLIENT)
    public static void sendRenameTelePacket(final String newName, final int index)
    {
        final PacketPokedex packet = new PacketPokedex(PacketPokedex.RENAME);
        packet.getTag().putString("N", newName);
        packet.getTag().putInt("I", index);
        PacketPokedex.ASSEMBLER.sendToServer(packet);
    }

    @OnlyIn(value = Dist.CLIENT)
    public static void sendReorderTelePacket(final int index1, final int index2)
    {
        final PacketPokedex packet = new PacketPokedex(PacketPokedex.REORDER);
        packet.getTag().putInt("2", index2);
        packet.getTag().putInt("1", index1);
        PacketPokedex.ASSEMBLER.sendToServer(packet);
        TeleportHandler.swapTeleports(PokecubeCore.proxy.getPlayer().getStringUUID(), index1, index2);
    }

    public static void sendSecretBaseInfoPacket(final ServerPlayerEntity player, final boolean watch)
    {
        final PacketPokedex packet = new PacketPokedex(PacketPokedex.BASERADAR);
        ListNBT list = new ListNBT();

        final World world = player.getCommandSenderWorld();

        final BlockPos pos = player.blockPosition();
        final GlobalPos here = GlobalPos.of(world.dimension(), pos);
        for (final GlobalPos c : SecretBaseDimension.getNearestBases(here, PokecubeCore.getConfig().baseRadarRange))
        {
            final CompoundNBT nbt = NBTUtil.writeBlockPos(c.pos());
            list.add(nbt);
        }
        packet.getTag().put("_bases_", list);

        packet.getTag().putBoolean("M", watch);
        packet.getTag().putInt("R", PokecubeCore.getConfig().baseRadarRange);
        final List<GlobalPos> meteors = Lists.newArrayList(PokecubeSerializer.getInstance().meteors);
        meteors.removeIf(p -> p.dimension() != here.dimension());
        meteors.sort((c1, c2) ->
        {
            final int d1 = c1.pos().compareTo(pos);
            final int d2 = c2.pos().compareTo(pos);
            return d2 - d1;
        });

        list = new ListNBT();
        for (int i = 0; i < Math.min(10, meteors.size()); i++)
        {
            final CompoundNBT nbt = NBTUtil.writeBlockPos(meteors.get(i).pos());
            list.add(nbt);
        }
        packet.getTag().put("_meteors_", list);

        list = new ListNBT();
        final List<ForbiddenEntry> repels = SpawnHandler.getForbiddenEntries(world, pos);
        for (final ForbiddenEntry entry : repels)
        {
            final CompoundNBT nbt = NBTUtil.writeBlockPos(entry.region.getPos());
            list.add(nbt);
        }
        packet.getTag().put("_repels_", list);

        PacketPokedex.ASSEMBLER.sendTo(packet, player);
    }

    @OnlyIn(value = Dist.CLIENT)
    public static void sendSpecificSpawnsRequest(final PokedexEntry entry)
    {
        final PacketPokedex packet = new PacketPokedex(PacketPokedex.REQUESTMOB);
        packet.getTag().putString("V", entry.getName());
        PacketPokedex.ASSEMBLER.sendToServer(packet);
    }

    @OnlyIn(value = Dist.CLIENT)
    public static void updateWatchEntry(final PokedexEntry entry)
    {
        final String name = PokecubePlayerDataHandler.getCustomDataTag(PokecubeCore.proxy.getPlayer()).getString(
                "WEntry");
        if (Database.getEntry(name) == entry) return;
        final PacketPokedex packet = new PacketPokedex(PacketPokedex.SETWATCHPOKE);
        PokecubePlayerDataHandler.getCustomDataTag(PokecubeCore.proxy.getPlayer()).putString("WEntry", entry.getName());
        packet.getTag().putString("V", entry.getName());
        PacketPokedex.ASSEMBLER.sendToServer(packet);
    }

    public static void sendLoginPacket(final ServerPlayerEntity target)
    {
        for (final String reward : XMLRewardsHandler.loadedRecipes)
        {
            final PacketPokedex message = new PacketPokedex(PacketPokedex.REWARDS);
            message.getTag().putString("R", reward);
            PacketPokedex.ASSEMBLER.sendTo(message, target);
        }
        final PacketPokedex message = new PacketPokedex(PacketPokedex.CHECKLEGEND);
        final ListNBT legends = new ListNBT();
        for (final PokedexEntry e : ISpecialCaptureCondition.captureMap.keySet())
            legends.add(StringNBT.valueOf(e.getTrimmedName()));
        message.getTag().put("legends", legends);
        final ListNBT no_breed = new ListNBT();
        for (final PokedexEntry e : Database.getSortedFormes())
            if (!e.breeds) no_breed.add(StringNBT.valueOf(e.getTrimmedName()));
        message.getTag().put("no_breed", no_breed);
        PacketPokedex.ASSEMBLER.sendTo(message, target);
    }

    byte message;

    public PacketPokedex()
    {
    }

    public PacketPokedex(final byte message)
    {
        this.getTag().putByte("__message__", message);
    }

    public PacketPokedex(final PacketBuffer buffer)
    {
        super(buffer);
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    protected void onCompleteClient()
    {
        final PlayerEntity player = PokecubeCore.proxy.getPlayer();
        this.message = this.getTag().getByte("__message__");
        int n = 0;
        int num = 0;
        CompoundNBT data;
        switch (this.message)
        {
        case OPEN:
            final Entity mob = PokecubeCore.getEntityProvider().getEntity(player.getCommandSenderWorld(), this.getTag()
                    .getInt("M"), true);
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
            final boolean watch = this.getTag().getBoolean("W");
            if (watch) net.minecraft.client.Minecraft.getInstance().setScreen(new GuiPokeWatch(player,
                    mob instanceof LivingEntity ? (LivingEntity) mob : null));
            else net.minecraft.client.Minecraft.getInstance().setScreen(new GuiPokedex(pokemob, player));
            return;
        case REQUEST:
            PacketPokedex.values.clear();
            n = this.getTag().getAllKeys().size();
            for (int i = 0; i < n; i++)
                PacketPokedex.values.add(this.getTag().getString("" + i));
            return;
        case BASERADAR:
            pokecube.core.client.gui.watch.SecretBaseRadarPage.updateRadar(this.getTag());
            return;
        case REQUESTLOC:
            PacketPokedex.selectedLoc.clear();
            data = this.getTag().getCompound("V");
            n = data.getAllKeys().size() / 2;
            for (int i = 0; i < n; i++)
                PacketPokedex.selectedLoc.put(Database.getEntry(data.getString("e" + i)), PacketPokedex.gson.fromJson(
                        data.getString("" + i), SpawnBiomeMatcher.class));
            if (this.getTag().contains("E")) PokecubePlayerDataHandler.getCustomDataTag(player).putString("WEntry", this
                    .getTag().getString("E"));
            PacketPokedex.repelled = this.getTag().getBoolean("R");
            return;
        case REQUESTMOB:
            PacketPokedex.selectedMob.clear();
            data = this.getTag().getCompound("V");
            num = data.getInt("n");
            for (int i = 0; i < num; i++)
                PacketPokedex.selectedMob.add(PacketPokedex.gson.fromJson(data.getString("" + i),
                        SpawnBiomeMatcher.class));
            return;
        case BREEDLIST:
            data = this.getTag().getCompound("V");
            final String entry = this.getTag().getString("e");
            final List<String> related = Lists.newArrayList();
            num = data.getInt("n");
            for (int i = 0; i < num; i++)
                related.add(data.getString("" + i));
            PacketPokedex.relatedLists.put(entry, related);
            return;
        case REWARDS:
            final String reward = this.getTag().getString("R");
            if (!reward.isEmpty()) Database.loadRewards(reward);
            pokecube.core.client.gui.GuiInfoMessages.clear();
            return;
        case CHECKLEGEND:
            PacketPokedex.haveConditions.clear();
            final ListNBT legends = this.getTag().getList("legends", 8);
            for (int i = 0; i < legends.size(); i++)
            {
                final PokedexEntry p = Database.getEntry(legends.getString(i));
                if (p != null) PacketPokedex.haveConditions.add(p);
            }
            PacketPokedex.noBreeding.clear();
            final ListNBT no_breed = this.getTag().getList("no_breed", 8);
            for (int i = 0; i < no_breed.size(); i++)
            {
                final PokedexEntry p = Database.getEntry(no_breed.getString(i));
                if (p != null) PacketPokedex.noBreeding.add(p);
            }
            return;
        }
    }

    private String serialize(final SpawnBiomeMatcher matcher)
    {
        // First ensure the client side stuff is cleared.
        matcher.clientBiomes.clear();
        matcher.clientTypes.clear();
        // Then populate it for serialisation
        matcher.clientBiomes.addAll(matcher.getValidBiomes());
        matcher._validSubBiomes.forEach(b -> matcher.clientTypes.add(b.name));
        final String ret = PacketPokedex.gson.toJson(matcher);
        // Then clear afterwards
        matcher.clientBiomes.clear();
        matcher.clientBiomes.clear();
        return ret;
    }

    @Override
    protected void onCompleteServer(final ServerPlayerEntity player)
    {
        this.message = this.getTag().getByte("__message__");
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
            mob = PokecubeCore.getEntityProvider().getEntity(player.getCommandSenderWorld(), this.getTag().getInt("V"),
                    true);
            pokemob = CapabilityPokemob.getPokemobFor(mob);
            if (pokemob != null) PlayerDataHandler.getInstance().getPlayerData(player).getData(
                    PokecubePlayerStats.class).inspect(player, pokemob);
            return;
        case SETWATCHPOKE:
            PokecubePlayerDataHandler.getCustomDataTag(player).putString("WEntry", this.getTag().getString("V"));
            return;
        case REQUESTMOB:
            pos = Vector3.getNewVector().set(player);
            checker = new SpawnCheck(pos, player.getCommandSenderWorld());
            entry = Database.getEntry(this.getTag().getString("V"));
            packet = new PacketPokedex(PacketPokedex.REQUESTMOB);
            if (entry.getSpawnData() != null) for (final SpawnBiomeMatcher matcher : entry.getSpawnData().matchers
                    .keySet())
            {
                spawns.putString("" + n, this.serialize(matcher));
                n++;
            }
            spawns.putInt("n", n);
            packet.getTag().put("V", spawns);
            PacketPokedex.ASSEMBLER.sendTo(packet, player);
            packet = new PacketPokedex(PacketPokedex.BREEDLIST);
            final CompoundNBT breedable = new CompoundNBT();
            packet.getTag().putString("e", entry.getTrimmedName());
            breedable.putString("0", entry.getTrimmedName());
            n = 1;
            for (final PokedexEntry e : entry.getRelated())
            {
                breedable.putString("" + n, e.getTrimmedName());
                n++;
            }
            breedable.putInt("n", n);
            packet.getTag().put("V", breedable);
            PacketPokedex.ASSEMBLER.sendTo(packet, player);
            return;
        case REQUESTLOC:
            rates = Maps.newHashMap();
            pos = Vector3.getNewVector().set(player);
            checker = new SpawnCheck(pos, player.getCommandSenderWorld());
            names = new ArrayList<>();
            final boolean repelled = SpawnHandler.getNoSpawnReason(player.getCommandSenderWorld(), pos
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
                spawns.putString("" + n, this.serialize(matcher));
                n++;
            }
            packet.getTag().put("V", spawns);
            packet.getTag().putBoolean("R", repelled);
            entry = Database.getEntry(PokecubePlayerDataHandler.getCustomDataTag(player).getString("WEntry"));
            if (entry != null) packet.getTag().putString("E", entry.getName());
            PacketPokedex.ASSEMBLER.sendTo(packet, player);
            return;
        case REQUEST:
            final boolean mode = this.getTag().getBoolean("M");
            packet = new PacketPokedex(PacketPokedex.REQUEST);
            if (!mode)
            {
                rates = Maps.newHashMap();
                pos = Vector3.getNewVector().set(player);
                checker = new SpawnCheck(pos, player.getCommandSenderWorld());
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
                final BiomeType biome = TerrainManager.getInstance().getTerrainForEntity(player).getBiome(pos);
                packet.getTag().putString("0", "" + biome.getType());
                packet.getTag().putString("1", biome.readableName);
                for (int i = 0; i < names.size(); i++)
                {
                    final PokedexEntry e = names.get(i);
                    packet.getTag().putString("" + (i + 2), e.getUnlocalizedName() + "`" + rates.get(e));
                }
            }
            else
            {
                biomes = Lists.newArrayList();
                entry = Database.getEntry(this.getTag().getString("F"));
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
                    if (hasBiomes) for (final RegistryKey<Biome> b : SpawnBiomeMatcher.getAllBiomeKeys())
                        if (b != null) if (data.isValid(b)) biomes.add(b.getRegistryName().toString());
                    for (final BiomeType b : BiomeType.values())
                        if (data.isValid(b)) biomes.add(b.readableName);
                    for (int i = 0; i < biomes.size(); i++)
                        packet.getTag().putString("" + i, biomes.get(i));
                }
            }
            PacketPokedex.ASSEMBLER.sendTo(packet, player);
        case REORDER:
            final int index1 = this.getTag().getInt("1");
            final int index2 = this.getTag().getInt("2");
            PlayerDataHandler.getInstance().save(player.getStringUUID());
            TeleportHandler.swapTeleports(player.getStringUUID(), index1, index2);
            PacketDataSync.syncData(player, "pokecube-data");
            return;
        case REMOVE:
            final int index = this.getTag().getInt("I");
            final TeleDest loc = TeleportHandler.getTeleport(player.getStringUUID(), index);
            TeleportHandler.unsetTeleport(index, player.getStringUUID());
            player.sendMessage(new StringTextComponent("Deleted " + loc.getName()), Util.NIL_UUID);
            PlayerDataHandler.getInstance().save(player.getStringUUID());
            PacketDataSync.syncData(player, "pokecube-data");
            return;
        case RENAME:
            final String name = this.getTag().getString("N");
            TeleportHandler.renameTeleport(player.getStringUUID(), this.getTag().getInt("I"), name);
            player.sendMessage(new StringTextComponent("Set teleport as " + name), Util.NIL_UUID);
            PlayerDataHandler.getInstance().save(player.getStringUUID());
            PacketDataSync.syncData(player, "pokecube-data");
            return;
        case INSPECT:
            final boolean reward = this.getTag().getBoolean("R");
            final String lang = this.getTag().getString("L");
            PokecubePlayerDataHandler.getCustomDataTag(player).putString("lang", lang);
            final boolean inspected = PokedexInspector.inspect(player, reward);

            if (!reward)
            {
                if (inspected) player.sendMessage(new TranslationTextComponent("pokedex.inspect.available"),
                        Util.NIL_UUID);
            }
            else
            {
                if (!inspected) player.sendMessage(new TranslationTextComponent("pokedex.inspect.nothing"),
                        Util.NIL_UUID);
                player.closeContainer();
            }
            return;
        case CHECKLEGEND:
            entry = Database.getEntry(this.getTag().getString("V"));
            if (entry != null && ISpecialCaptureCondition.captureMap.containsKey(entry))
            {
                final ISpecialCaptureCondition condition = ISpecialCaptureCondition.captureMap.get(entry);
                final boolean valid = condition.canCapture(player);
                if (valid) player.sendMessage(new TranslationTextComponent("pokewatch.capture.check.yes", entry
                        .getTranslatedName()), Util.NIL_UUID);
                else
                {
                    player.sendMessage(condition.getFailureMessage(player), Util.NIL_UUID);
                    player.sendMessage(new TranslationTextComponent("pokewatch.capture.check.no", entry
                            .getTranslatedName()), Util.NIL_UUID);
                }
            }
            return;
        }
        if (this.getTag().contains("E")) PokecubePlayerDataHandler.getCustomDataTag(player).putString("WEntry", this
                .getTag().getString("E"));
    }
}

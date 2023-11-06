package pokecube.core.network.packets;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.commandhandlers.TeleportHandler;
import pokecube.api.events.pokemobs.SpawnEvent.SpawnContext;
import pokecube.api.stats.ISpecialCaptureCondition;
import pokecube.api.stats.SpecialCaseRegister;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.GuiPokedex;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.database.Database;
import pokecube.core.database.rewards.XMLRewardsHandler;
import pokecube.core.database.spawns.PokemobSpawns;
import pokecube.core.eventhandlers.SpawnHandler;
import pokecube.core.eventhandlers.SpawnHandler.ForbidReason;
import pokecube.core.eventhandlers.SpawnHandler.ForbiddenEntry;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.PokedexInspector;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.world.dimension.SecretBaseDimension;
import thut.api.Tracker;
import thut.api.entity.teleporting.TeleDest;
import thut.api.maths.Cruncher.SquareLoopCruncher;
import thut.api.maths.Vector3;
import thut.api.util.UnderscoreIgnore;
import thut.core.common.handlers.PlayerDataHandler;
import thut.core.common.network.nbtpacket.NBTPacket;
import thut.core.common.network.nbtpacket.PacketAssembly;
import thut.lib.TComponent;

public class PacketPokedex extends NBTPacket
{
    public static final PacketAssembly<PacketPokedex> ASSEMBLER = PacketAssembly.registerAssembler(PacketPokedex.class,
            PacketPokedex::new, PokecubeCore.packets);

    public static final byte CHECKLEGEND = -14;
    public static final byte REWARDS = -13;
    public static final byte BREEDLIST = -12;
    public static final byte OPEN = -11;
    public static final byte REORDER = -10;
    public static final byte INSPECTMOB = -9;
    public static final byte SETWATCHPOKE = -8;
    public static final byte REQUESTLOC = -7;
    public static final byte REQUESTMOB = -6;
    public static final byte REQUEST = -5;
    public static final byte INSPECT = -4;
    public static final byte BASERADAR = -3;
    public static final byte REMOVE = -2;
    public static final byte RENAME = -1;

    public static final Gson gson = new GsonBuilder().setExclusionStrategies(UnderscoreIgnore.INSTANCE).create();

    public static List<String> values = new ArrayList<>();
    public static List<SpawnBiomeMatcher> selectedMob = new ArrayList<>();
    public static BitSet validSpawnIndex = new BitSet();
    public static Map<PokedexEntry, SpawnBiomeMatcher> selectedLoc = Maps.newHashMap();
    public static Map<String, List<String>> relatedLists = Maps.newHashMap();
    public static long changed = 0;

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

    public static void sendOpenPacket(final ServerPlayer player, final Entity pokemob, final boolean watch)
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

    public static void sendLocalSpawnsPacket(final ServerPlayer player)
    {
        int n = 0;
        PokedexEntry entry;
        final CompoundTag spawns = new CompoundTag();

        Map<PokedexEntry, Float> rates;
        ArrayList<PokedexEntry> names = new ArrayList<>();
        float total = 0;
        ServerLevel level = player.getLevel();
        Vector3 pos = new Vector3().set(player);
        SpawnCheck checker = new SpawnCheck(pos, level);

        rates = Maps.newHashMap();
        names = new ArrayList<>();
        final boolean repelled = SpawnHandler.getNoSpawnReason(level, pos.getPos()) != ForbidReason.NONE;
        for (final PokedexEntry e : Database.spawnables)
            if (e.getSpawnData().getMatcher(new SpawnContext(player, e), checker, false) != null) names.add(e);
        final Map<PokedexEntry, SpawnBiomeMatcher> matchers = Maps.newHashMap();
        for (final PokedexEntry e : names)
        {
            SpawnContext scontext = new SpawnContext(player, e);
            final SpawnBiomeMatcher matcher = e.getSpawnData().getMatcher(scontext, checker, false);
            if (matcher == null) continue;
            float val = e.getSpawnData().getWeight(scontext, checker, true);
            final float min = e.getSpawnData().getMin(matcher);
            final float num = min + (e.getSpawnData().getMax(matcher) - min) / 2;
            val *= num;
            matchers.put(e, matcher);
            total += val;
            rates.put(e, val);
        }
        if (total != 0) for (final PokedexEntry e : names)
        {
            final float val = rates.get(e) / total;
            rates.put(e, val);
        }

        SpawnContext base = new SpawnContext(player, Database.missingno);
        SpawnContext context = SpawnHandler.getSpawnForLoc(base);
        if (context != null && context.entry() != null) rates.put(context.entry(), 1f);

        var packet = new PacketPokedex(PacketPokedex.REQUESTLOC);
        for (final PokedexEntry e : names)
        {
            final SpawnBiomeMatcher matcher = matchers.get(e);
            matcher.spawnRule.values.put("Local_Rate", rates.get(e) + "");
            String match = PacketPokedex.serialize(matcher);
            // This is null in the case that the spawn is not valid, or is
            // hidden by the desc being set to __hidden__
            if (match == null) continue;
            spawns.putString("e" + n, e.getName());
            spawns.putString("" + n, match);
            n++;
        }
        packet.getTag().put("V", spawns);
        packet.getTag().putBoolean("R", repelled);
        entry = Database.getEntry(PokecubePlayerDataHandler.getCustomDataTag(player).getString("WEntry"));
        if (entry != null) packet.getTag().putString("E", entry.getName());
        PacketPokedex.ASSEMBLER.sendTo(packet, player);
    }

    public static void sendMobPacket(final ServerPlayer player, CompoundTag tag)
    {
        int n = 0;
        final CompoundTag spawns = new CompoundTag();
        CompoundTag spawnHere = new CompoundTag();
        var entry = Database.getEntry(tag.getString("V"));
        var packet = new PacketPokedex(PacketPokedex.REQUESTMOB);
        SpawnCheck check = new SpawnCheck(new Vector3(player), player.level);
        if (entry.getSpawnData() != null) for (final SpawnBiomeMatcher matcher : entry.getSpawnData().matchers.keySet())
        {
            String serialised = PacketPokedex.serialize(matcher);
            // This is null in the case that the spawn is not valid, or is
            // hidden by the desc being set to __hidden__
            if (serialised == null) continue;
            spawns.putString("" + n, serialised);
            if (matcher.matches(check)) spawnHere.putBoolean("" + n, true);
            n++;
        }
        if (PokemobSpawns.REGEX_SPAWNS.containsKey(entry))
        {
            var list = PokemobSpawns.REGEX_SPAWNS.get(entry);
            for (var set : list)
            {
                var matcher = set.matcher();
                String serialised = PacketPokedex.serialize(matcher);
                // This is null in the case that the spawn is not valid, or is
                // hidden by the desc being set to __hidden__
                if (serialised == null) continue;
                spawns.putString("" + n, serialised);
                if (matcher.matches(check)) spawnHere.putBoolean("" + n, true);
                n++;
            }
        }
        spawns.putInt("n", n);
        packet.getTag().put("V", spawns);
        packet.getTag().put("H", spawnHere);
        PacketPokedex.ASSEMBLER.sendTo(packet, player);
        packet = new PacketPokedex(PacketPokedex.BREEDLIST);
        final CompoundTag breedable = new CompoundTag();
        packet.getTag().putString("e", entry.getTrimmedName());
        breedable.putString("0", entry.getTrimmedName());
        n = 1;
        for (final PokedexEntry e : entry.getRelated())
        {
            if (!e.breeds) continue;
            breedable.putString("" + n, e.getTrimmedName());
            n++;
        }
        breedable.putInt("n", n);
        packet.getTag().put("V", breedable);
        PacketPokedex.ASSEMBLER.sendTo(packet, player);
    }

    public static void sendSecretBaseInfoPacket(final ServerPlayer player, final boolean watch)
    {
        final PacketPokedex packet = new PacketPokedex(PacketPokedex.BASERADAR);
        ListTag list = new ListTag();

        final ServerLevel level = player.getLevel();

        final BlockPos pos = player.blockPosition();
        final GlobalPos here = GlobalPos.of(level.dimension(), pos);
        for (final GlobalPos c : SecretBaseDimension.getNearestBases(here, PokecubeCore.getConfig().baseRadarRange))
        {
            final CompoundTag nbt = NbtUtils.writeBlockPos(c.pos());
            list.add(nbt);
        }
        packet.getTag().put("_bases_", list);

        packet.getTag().putBoolean("M", watch);
        packet.getTag().putInt("R", PokecubeCore.getConfig().baseRadarRange);
        final List<GlobalPos> meteors = new ArrayList<>(PokecubeSerializer.getInstance().meteors);
        meteors.removeIf(p -> p.dimension() != here.dimension());

        SquareLoopCruncher searcher = new SquareLoopCruncher();
        int step = 12 * 16;
        BlockPos testPos = searcher.getNext(pos, step);

        ResourceLocation resourcelocation1 = new ResourceLocation("pokecube_world:meteorites");
        var registry = level.registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
        var key = TagKey.create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, resourcelocation1);
        HolderSet<ConfiguredStructureFeature<?, ?>> holderset = HolderSet
                .direct(registry.getOrCreateTag(key).stream().toList());

        long time = System.nanoTime();
        while ((System.nanoTime() - time) < 5e5)
        {
            Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> thing = level.getChunkSource().getGenerator()
                    .findNearestMapFeature(level, holderset, testPos, 1, false);
            if (thing != null)
            {
                BlockPos p2 = thing.getFirst();
                if (level.isPositionEntityTicking(p2))
                    p2 = p2.atY(level.getHeight(Types.WORLD_SURFACE, p2.getX(), p2.getZ()));
                meteors.add(GlobalPos.of(level.dimension(), p2));
            }
            testPos = searcher.getNext(pos, step);
        }

        meteors.sort((c1, c2) -> {
            final int d1 = c1.pos().compareTo(pos);
            final int d2 = c2.pos().compareTo(pos);
            return d2 - d1;
        });

        list = new ListTag();
        for (int i = 0; i < Math.min(10, meteors.size()); i++)
        {
            final CompoundTag nbt = NbtUtils.writeBlockPos(meteors.get(i).pos());
            list.add(nbt);
        }
        packet.getTag().put("_meteors_", list);

        list = new ListTag();
        final List<ForbiddenEntry> repels = SpawnHandler.getForbiddenEntries(level, pos);
        for (final ForbiddenEntry entry : repels)
        {
            final CompoundTag nbt = NbtUtils.writeBlockPos(entry.region.getPos());
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
        final String name = PokecubePlayerDataHandler.getCustomDataTag(PokecubeCore.proxy.getPlayer())
                .getString("WEntry");
        if (Database.getEntry(name) == entry) return;
        final PacketPokedex packet = new PacketPokedex(PacketPokedex.SETWATCHPOKE);
        PokecubePlayerDataHandler.getCustomDataTag(PokecubeCore.proxy.getPlayer()).putString("WEntry", entry.getName());
        packet.getTag().putString("V", entry.getName());
        PacketPokedex.ASSEMBLER.sendToServer(packet);
    }

    public static void sendLoginPacket(final ServerPlayer target)
    {
        for (final String reward : XMLRewardsHandler.loadedRecipes)
        {
            final PacketPokedex message = new PacketPokedex(PacketPokedex.REWARDS);
            message.getTag().putString("R", reward);
            PacketPokedex.ASSEMBLER.sendTo(message, target);
        }
        final PacketPokedex message = new PacketPokedex(PacketPokedex.CHECKLEGEND);

        // Put in the legendaries needing conditions
        final ListTag legends = new ListTag();
        for (final PokedexEntry e : ISpecialCaptureCondition.captureMap.keySet())
            legends.add(StringTag.valueOf(e.getTrimmedName()));
        message.getTag().put("legends", legends);

        // List of mobs that cannot breed
        final ListTag no_breed = new ListTag();

        // Put in a list of mobs that cannot breed
        for (final PokedexEntry e : Database.getSortedFormes())
        {
            if (!e.breeds) no_breed.add(StringTag.valueOf(e.getTrimmedName()));
            CompoundTag tag = new CompoundTag();
            tag.putString("id", e.getTrimmedName());
        }
        message.getTag().put("no_breed", no_breed);
        PacketPokedex.ASSEMBLER.sendTo(message, target);
    }

    byte message;

    public PacketPokedex()
    {}

    public PacketPokedex(final byte message)
    {
        this.getTag().putByte("__message__", message);
    }

    public PacketPokedex(final FriendlyByteBuf buffer)
    {
        super(buffer);
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    protected void onCompleteClient()
    {
        final Player player = PokecubeCore.proxy.getPlayer();
        this.message = this.getTag().getByte("__message__");
        int n = 0;
        int num = 0;
        CompoundTag data;
        switch (this.message)
        {
        case OPEN:
            final Entity mob = PokecubeAPI.getEntityProvider().getEntity(player.getLevel(), this.getTag().getInt("M"),
                    true);
            final IPokemob pokemob = PokemobCaps.getPokemobFor(mob);
            final boolean watch = this.getTag().getBoolean("W");
            if (watch) net.minecraft.client.Minecraft.getInstance()
                    .setScreen(new GuiPokeWatch(player, mob instanceof LivingEntity ? (LivingEntity) mob : null));
            else net.minecraft.client.Minecraft.getInstance().setScreen(new GuiPokedex(pokemob, player));
            break;
        case BASERADAR:
            pokecube.core.client.gui.watch.SecretBaseRadarPage.updateRadar(this.getTag());
            break;
        case REQUESTLOC:
            PacketPokedex.selectedLoc.clear();
            data = this.getTag().getCompound("V");
            // This is / 2 as every other key is entry vs tag
            n = data.getAllKeys().size() / 2;
            for (int i = 0; i < n; i++)
            {
                PacketPokedex.selectedLoc.put(Database.getEntry(data.getString("e" + i)),
                        PacketPokedex.gson.fromJson(data.getString("" + i), SpawnBiomeMatcher.class).setClient());
            }
            if (this.getTag().contains("E"))
                PokecubePlayerDataHandler.getCustomDataTag(player).putString("WEntry", this.getTag().getString("E"));
            PacketPokedex.repelled = this.getTag().getBoolean("R");
            break;
        case REQUESTMOB:
            PacketPokedex.selectedMob.clear();
            data = this.getTag().getCompound("V");
            num = data.getInt("n");
            var validHere = this.getTag().getCompound("H");
            validSpawnIndex.clear();
            for (var key : validHere.getAllKeys())
            {
                boolean var = validHere.getBoolean(key);
                if (var) validSpawnIndex.set(Integer.parseInt(key));
            }
            for (int i = 0; i < num; i++)
            {
                String value = data.getString("" + i);
                var matcher = PacketPokedex.gson.fromJson(value, SpawnBiomeMatcher.class).setClient();
                PacketPokedex.selectedMob.add(matcher);
            }
            break;
        case BREEDLIST:
            data = this.getTag().getCompound("V");
            final String entry = this.getTag().getString("e");
            final List<String> related = new ArrayList<>();
            num = data.getInt("n");
            for (int i = 0; i < num; i++) related.add(data.getString("" + i));
            PacketPokedex.relatedLists.put(entry, related);
            break;
        case REWARDS:
            final String reward = this.getTag().getString("R");
            if (!reward.isEmpty()) Database.loadRewards(reward);
            pokecube.core.client.gui.GuiInfoMessages.clear();
            break;
        case CHECKLEGEND:
            PacketPokedex.haveConditions.clear();
            final ListTag legends = this.getTag().getList("legends", 8);
            for (int i = 0; i < legends.size(); i++)
            {
                final PokedexEntry p = Database.getEntry(legends.getString(i));
                if (p != null) PacketPokedex.haveConditions.add(p);
            }
            PacketPokedex.noBreeding.clear();
            final ListTag no_breed = this.getTag().getList("no_breed", 8);
            for (int i = 0; i < no_breed.size(); i++)
            {
                final PokedexEntry p = Database.getEntry(no_breed.getString(i));
                if (p != null) PacketPokedex.noBreeding.add(p);
            }
            break;
        }
        changed = Tracker.instance().getTick();
    }

    public static String serialize(final SpawnBiomeMatcher matcher)
    {
        if ("__hidden__".equals(matcher.spawnRule.desc)) return null;
        // Initialise the client lists of biomes/types
        SpawnBiomeMatcher.populateClientValues(matcher);
        // Then serialise it
        final String ret = PacketPokedex.gson.toJson(matcher);
        // Then clear afterwards
        SpawnBiomeMatcher.clearClientValues(matcher);
        return ret;
    }

    @Override
    protected void onCompleteServer(final ServerPlayer player)
    {
        this.message = this.getTag().getByte("__message__");
        // Declair some stuff before the switch;
        IPokemob pokemob;
        Entity mob;
        PokedexEntry entry;
        ServerLevel level = player.getLevel();

        switch (this.message)
        {
        case INSPECTMOB:
            mob = PokecubeAPI.getEntityProvider().getEntity(level, this.getTag().getInt("V"), true);
            pokemob = PokemobCaps.getPokemobFor(mob);
            if (pokemob != null) PlayerDataHandler.getInstance().getPlayerData(player)
                    .getData(PokecubePlayerStats.class).inspect(player, pokemob);
            return;
        case SETWATCHPOKE:
            PokecubePlayerDataHandler.getCustomDataTag(player).putString("WEntry", this.getTag().getString("V"));
            return;
        case REQUESTMOB:
            sendMobPacket(player, this.getTag());
            return;
        case REQUESTLOC:
            sendLocalSpawnsPacket(player);
            return;
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
            thut.lib.ChatHelper.sendSystemMessage(player, TComponent.literal("Deleted " + loc.getName()));
            PlayerDataHandler.getInstance().save(player.getStringUUID());
            PacketDataSync.syncData(player, "pokecube-data");
            return;
        case RENAME:
            final String name = this.getTag().getString("N");
            TeleportHandler.renameTeleport(player.getStringUUID(), this.getTag().getInt("I"), name);
            thut.lib.ChatHelper.sendSystemMessage(player, TComponent.literal("Set teleport as " + name));
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
                if (inspected)
                    thut.lib.ChatHelper.sendSystemMessage(player, TComponent.translatable("pokedex.inspect.available"));
            }
            else
            {
                if (!inspected)
                    thut.lib.ChatHelper.sendSystemMessage(player, TComponent.translatable("pokedex.inspect.nothing"));
                player.closeContainer();
            }
            return;
        case CHECKLEGEND:
            entry = Database.getEntry(this.getTag().getString("V"));
            ISpecialCaptureCondition condition = SpecialCaseRegister.getCaptureCondition(entry);
            if (condition != null)
            {
                final boolean valid = condition.canCapture(player);
                if (valid) thut.lib.ChatHelper.sendSystemMessage(player,
                        TComponent.translatable("pokewatch.capture.check.yes", entry.getTranslatedName()));
                else
                {
                    thut.lib.ChatHelper.sendSystemMessage(player, condition.getFailureMessage(player));
                    thut.lib.ChatHelper.sendSystemMessage(player,
                            TComponent.translatable("pokewatch.capture.check.no", entry.getTranslatedName()));
                }
            }
            return;
        }
        if (this.getTag().contains("E"))
            PokecubePlayerDataHandler.getCustomDataTag(player).putString("WEntry", this.getTag().getString("E"));
    }
}

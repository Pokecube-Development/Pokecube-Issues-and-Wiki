/**
 *
 */
package pokecube.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Dynamic;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.playerdata.PokecubePlayerData;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.maths.Vector4;
import thut.bling.bag.SaveHandler;
import thut.core.common.ThutCore;
import thut.core.common.handlers.PlayerDataHandler;

/** @author Manchou */
public class PokecubeSerializer
{
    private static final String POKECUBE = "pokecube";
    private static final String DATA     = "data";
    private static final String METEORS  = "meteors";
    private static final String STRUCTS  = "structs";
    private static final String BASES    = "bases";
    private static final String LASTUID  = "lastUid";

    public static int MeteorDistance = 3000 * 3000;

    private static PokecubeSerializer instance;
    private static PokecubeSerializer client = new PokecubeSerializer((ServerWorld) null);

    public static double distSq(final GlobalPos location, final GlobalPos meteor)
    {
        return location.getPos().distanceSq(meteor.getPos());
    }

    public static PokecubeSerializer getInstance()
    {
        return PokecubeSerializer.getInstance(true);
    }

    public static PokecubeSerializer getInstance(final boolean serverside)
    {
        if (serverside) return PokecubeSerializer.instance;
        return PokecubeSerializer.client;
    }

    public static void newInstance(final ServerWorld world)
    {
        PokecubeSerializer.instance = new PokecubeSerializer(world);
    }

    SaveHandler saveHandler;

    public ArrayList<GlobalPos> meteors;
    public ArrayList<GlobalPos> bases;

    public Map<String, List<GlobalPos>> structs;

    private int lastId = 1;

    public ServerWorld myWorld;
    public CompoundNBT customData = new CompoundNBT();

    private PokecubeSerializer(final MinecraftServer server)
    {
        this(server != null ? server.getWorld(World.OVERWORLD) : null);
    }

    private PokecubeSerializer(final ServerWorld world)
    {
        /** This data is saved to surface world's folder. */
        this.myWorld = world;
        if (this.myWorld != null) this.saveHandler = this.myWorld.getSaveHandler();
        this.lastId = 0;
        this.meteors = new ArrayList<>();
        this.bases = new ArrayList<>();
        this.structs = Maps.newConcurrentMap();
        if (this.myWorld != null) this.loadData();
    }

    public void addMeteorLocation(final GlobalPos v)
    {
        this.meteors.add(v);
        this.save();
    }

    public boolean shouldPlace(final String struct, final BlockPos pos, final RegistryKey<World> dim, final int seperation)
    {
        final List<GlobalPos> locs = this.structs.get(struct);
        if (locs == null) return true;
        final GlobalPos check = GlobalPos.getPosition(dim, pos);
        for (final GlobalPos v : locs)
        {
            if (v.getDimension() != dim) continue;
            if (check.getPos().withinDistance(v.getPos(), seperation)) return false;
        }
        return true;
    }

    public void place(final String struct, final BlockPos pos, final RegistryKey<World> dim)
    {
        final GlobalPos check = GlobalPos.getPosition(dim, pos);
        final List<GlobalPos> locs = this.structs.getOrDefault(struct, Lists.newArrayList());
        locs.add(check);
        this.structs.put(struct, locs);
        this.save();
    }

    public void setPlacedCenter()
    {
        PokecubeSerializer.getInstance().customData.putBoolean("start_pokecentre", true);
        PokecubeSerializer.getInstance().save();
    }

    public void setPlacedSpawn()
    {
        PokecubeSerializer.getInstance().customData.putBoolean("set_world_spawn", true);
        PokecubeSerializer.getInstance().save();
    }

    public void setPlacedProf()
    {
        PokecubeSerializer.getInstance().customData.putBoolean("set_professor", true);
        PokecubeSerializer.getInstance().save();
    }

    public boolean hasPlacedProf()
    {
        return PokecubeSerializer.getInstance().customData.contains("set_professor");
    }

    public boolean hasPlacedSpawn()
    {
        return PokecubeSerializer.getInstance().customData.contains("set_world_spawn");
    }

    public boolean hasPlacedCenter()
    {
        return PokecubeSerializer.getInstance().customData.contains("start_pokecentre");
    }

    public boolean hasPlacedSpawnOrCenter()
    {
        return this.hasPlacedSpawn() || this.hasPlacedCenter();
    }

    public boolean canMeteorLand(final GlobalPos location, final ServerWorld world)
    {
        for (final GlobalPos v : this.meteors)
            if (this.tooClose(location, v)) return false;
        return true;
    }

    public static void clearInstance()
    {
        PokecubeSerializer.client = new PokecubeSerializer((ServerWorld) null);
        if (PokecubeSerializer.instance == null) return;
        PokecubeSerializer.instance.save();
        PokecubeItems.times = new Vector<>();
        PokecubeSerializer.instance = null;
    }

    public int getNextID()
    {
        return this.lastId++;
    }

    public boolean hasStarter(final PlayerEntity player)
    {
        if (player == null)
        {
            PokecubeCore.LOGGER.error("Checking starter for null player!");
            return true;
        }
        return PlayerDataHandler.getInstance().getPlayerData(player).getData(PokecubePlayerData.class).hasStarter();
    }

    public void loadData()
    {
        if (this.saveHandler != null) try
        {
            final File worlddir = this.saveHandler.getWorldDirectory();
            File file = new File(worlddir, PokecubeSerializer.POKECUBE);
            file.mkdirs();
            file = new File(file, "global.dat");
            if (file != null && file.exists())
            {
                final FileInputStream fileinputstream = new FileInputStream(file);
                final CompoundNBT CompoundNBT = CompressedStreamTools.readCompressed(fileinputstream);
                fileinputstream.close();
                this.readFromNBT(CompoundNBT.getCompound(PokecubeSerializer.DATA));
            }
        }
        catch (final Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public void readFromNBT(final CompoundNBT tag)
    {
        this.lastId = tag.getInt(PokecubeSerializer.LASTUID);
        this.customData = tag.getCompound("data");
        INBT temp;
        temp = tag.get(PokecubeSerializer.METEORS);
        if (temp instanceof ListNBT)
        {
            final ListNBT tagListMeteors = (ListNBT) temp;
            if (tagListMeteors.size() > 0) meteors:
            for (int i = 0; i < tagListMeteors.size(); i++)
            {
                final CompoundNBT pokemobData = tagListMeteors.getCompound(i);
                if (pokemobData != null) try
                {
                    final GlobalPos location = GlobalPos.deserialize(new Dynamic<>(NBTDynamicOps.INSTANCE,
                            pokemobData));
                        for (final GlobalPos v : this.meteors)
                            if (PokecubeSerializer.distSq(location, v) < 4) continue meteors;
                        this.meteors.add(location);
                }
                catch (final Exception e)
                {
                    final Vector4 tmp = new Vector4(pokemobData);
                    if (tmp != null && !tmp.isEmpty())
                    {
                        final BlockPos pos = new BlockPos(tmp.x, tmp.y, tmp.z);
                        final RegistryKey<World> dim = DimensionType.getById((int) tmp.w);
                        final GlobalPos location = GlobalPos.getPosition(dim, pos);
                        for (final GlobalPos v : this.meteors)
                            if (PokecubeSerializer.distSq(location, v) < 4) continue meteors;
                        this.meteors.add(location);
                    }
                    PokecubeCore.LOGGER.error("Error loading meteor from tag: "+pokemobData);
                }
            }
        }
        temp = tag.get(PokecubeSerializer.BASES);
        if (temp instanceof ListNBT)
        {
            final ListNBT tagListMeteors = (ListNBT) temp;
            if (tagListMeteors.size() > 0) meteors:
            for (int i = 0; i < tagListMeteors.size(); i++)
            {
                final CompoundNBT pokemobData = tagListMeteors.getCompound(i);
                if (pokemobData != null) try
                {
                    final GlobalPos location = GlobalPos.deserialize(new Dynamic<>(NBTDynamicOps.INSTANCE,
                            pokemobData));
                    for (final GlobalPos v : this.bases)
                        if (PokecubeSerializer.distSq(location, v) < 4) continue meteors;
                        this.bases.add(location);
                }
                catch (final Exception e)
                {
                    PokecubeCore.LOGGER.error("Error loading base from tag: " + pokemobData);
                }
            }
        }

        temp = tag.get(PokecubeSerializer.STRUCTS);
        if (temp instanceof ListNBT)
        {
            final ListNBT tagListStructs = (ListNBT) temp;
            if (tagListStructs.size() > 0) for (int i = 0; i < tagListStructs.size(); i++)
            {
                final CompoundNBT pokemobData = tagListStructs.getCompound(i);
                if (pokemobData != null) try
                {
                    final GlobalPos location = GlobalPos.deserialize(new Dynamic<>(NBTDynamicOps.INSTANCE,
                            pokemobData));
                    final String struct = pokemobData.getString("type");
                    final List<GlobalPos> locs = this.structs.getOrDefault(struct, Lists.newArrayList());
                        locs.add(location);
                        this.structs.put(struct, locs);
                }
                catch (final Exception e)
                {
                    PokecubeCore.LOGGER.error("Error loading structure from tag: " + pokemobData);
                }
            }
        }

        temp = tag.get("tmtags");
        if (temp instanceof CompoundNBT) PokecubeItems.loadTime((CompoundNBT) temp);
    }

    public void save()
    {
        this.saveData();
    }

    private void saveData()
    {
        if (this.saveHandler == null) return;

        try
        {
            final File worlddir = this.saveHandler.getWorldDirectory();
            File file = new File(worlddir, PokecubeSerializer.POKECUBE);
            file.mkdirs();
            file = new File(file, "global.dat");
            if (file != null)
            {
                final CompoundNBT CompoundNBT = new CompoundNBT();
                this.writeToNBT(CompoundNBT);
                final CompoundNBT CompoundNBT1 = new CompoundNBT();
                CompoundNBT1.put(PokecubeSerializer.DATA, CompoundNBT);
                final FileOutputStream fileoutputstream = new FileOutputStream(file);
                CompressedStreamTools.writeCompressed(CompoundNBT1, fileoutputstream);
                fileoutputstream.close();
            }
        }
        catch (final Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public void setHasStarter(final PlayerEntity player)
    {
        this.setHasStarter(player, true);
    }

    public void setHasStarter(final PlayerEntity player, final boolean value)
    {
        try
        {
            PlayerDataHandler.getInstance().getPlayerData(player).getData(PokecubePlayerData.class).setHasStarter(
                    value);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error setting has starter state for " + player, e);
        }
        if (ThutCore.proxy.isServerSide()) PlayerDataHandler.getInstance().save(player.getCachedUniqueIdString());
    }

    public ItemStack starter(final PokedexEntry entry, final PlayerEntity owner)
    {
        final World worldObj = owner.getEntityWorld();
        final IPokemob entity = CapabilityPokemob.getPokemobFor(PokecubeCore.createPokemob(entry, worldObj));

        if (entity != null)
        {
            entity.setForSpawn(Tools.levelToXp(entity.getExperienceMode(), 5));
            entity.setHealth(entity.getMaxHealth());
            entity.setOwner(owner.getUniqueID());
            entity.setPokecube(new ItemStack(PokecubeItems.getFilledCube(PokecubeBehavior.DEFAULTCUBE)));
            final ItemStack item = PokecubeManager.pokemobToItem(entity);
            PokecubeManager.heal(item, owner.getEntityWorld());
            entity.getEntity().remove();
            return item;
        }
        return ItemStack.EMPTY;
    }

    private boolean tooClose(final GlobalPos location, final GlobalPos meteor)
    {
        if (location.getDimension() != meteor.getDimension()) return false;
        return PokecubeSerializer.distSq(location, meteor) < PokecubeSerializer.MeteorDistance;
    }

    public void writeToNBT(final CompoundNBT tag)
    {
        tag.putInt(PokecubeSerializer.LASTUID, this.lastId);
        tag.put("data", this.customData);
        final ListNBT tagListMeteors = new ListNBT();
        for (final GlobalPos v : this.meteors)
            {
                final INBT nbt = v.serialize(NBTDynamicOps.INSTANCE);
                tagListMeteors.add(nbt);
            }
        tag.put(PokecubeSerializer.METEORS, tagListMeteors);
        final ListNBT tagListStructs = new ListNBT();
        for (final String s : this.structs.keySet())
        {
            final List<GlobalPos> list = this.structs.get(s);
            for (final GlobalPos v : list)
                {
                    final CompoundNBT nbt = (CompoundNBT) v.serialize(NBTDynamicOps.INSTANCE);
                    nbt.putString("type", s);
                    tagListStructs.add(nbt);
                }
        }
        tag.put(PokecubeSerializer.STRUCTS, tagListStructs);
        final ListNBT tagListBases = new ListNBT();
        for (final GlobalPos v : this.bases)
            {
                final INBT nbt = v.serialize(NBTDynamicOps.INSTANCE);
                tagListMeteors.add(nbt);
            }
        tag.put(PokecubeSerializer.BASES, tagListBases);
        final CompoundNBT tms = new CompoundNBT();
        PokecubeItems.saveTime(tms);
        tag.put("tmtags", tms);
    }
}

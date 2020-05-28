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
import com.mojang.datafixers.Dynamic;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.SaveHandler;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.playerdata.PokecubePlayerData;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.common.ThutCore;
import thut.core.common.handlers.PlayerDataHandler;

/** @author Manchou */
public class PokecubeSerializer
{
    public static class TeleDest
    {
        public static TeleDest readFromNBT(final CompoundNBT nbt)
        {
            final Vector4 loc = new Vector4(nbt);
            final String name = nbt.getString("name");
            final int index = nbt.getInt("i");
            GlobalPos pos = null;
            if (nbt.contains("pos")) pos = GlobalPos.deserialize(new Dynamic<>(NBTDynamicOps.INSTANCE, nbt.get("pos")));
            return new TeleDest().setLoc(loc).setPos(pos).setName(name).setIndex(index);
        }

        public Vector4 loc;

        private GlobalPos pos;

        private Vector3 subLoc;

        private String name;

        public int index;

        public TeleDest()
        {
        }

        public TeleDest setLoc(final Vector4 loc)
        {
            this.loc = loc;
            this.subLoc = Vector3.getNewVector().set(loc.x, loc.y, loc.z);
            this.name = loc.toIntString();
            this.pos = GlobalPos.of(DimensionType.getById((int) loc.w), this.subLoc.getPos());
            loc.dim = this.pos.getDimension();
            return this;
        }

        public TeleDest setPos(final GlobalPos pos)
        {
            if (pos != null)
            {
                if (this.loc == null) this.loc = new Vector4(pos);
                else
                {
                    this.loc.x = pos.getPos().getX();
                    this.loc.y = pos.getPos().getY();
                    this.loc.z = pos.getPos().getZ();
                }
                this.loc.dim = pos.getDimension();
                this.subLoc = Vector3.getNewVector().set(this.loc.x, this.loc.y, this.loc.z);
                this.pos = pos;
                this.name = this.loc.toIntString();
            }
            return this;
        }

        public GlobalPos getPos()
        {
            return this.pos;
        }

        public Vector3 getLoc()
        {
            return this.subLoc;
        }

        public String getName()
        {
            return this.name;
        }

        public TeleDest setIndex(final int index)
        {
            this.index = index;
            return this;
        }

        public TeleDest setName(final String name)
        {
            this.name = name;
            return this;
        }

        public void writeToNBT(final CompoundNBT nbt)
        {
            this.loc.writeToNBT(nbt);
            nbt.put("pos", this.pos.serialize(NBTDynamicOps.INSTANCE));
            nbt.putString("name", this.name);
            nbt.putInt("i", this.index);
        }

        public void shift(final double dx, final int dy, final double dz)
        {
            this.loc.x += dx;
            this.loc.y += dy;
            this.loc.z += dz;
            this.subLoc.x += dx;
            this.subLoc.y += dy;
            this.subLoc.z += dz;
        }
    }

    private static final String POKECUBE = "pokecube";
    private static final String DATA     = "data";
    private static final String METEORS  = "meteors";
    private static final String STRUCTS  = "structs";
    private static final String BASES    = "bases";
    private static final String LASTUID  = "lastUid";

    public static int MeteorDistance = 3000 * 3000;

    private static PokecubeSerializer instance;
    private static PokecubeSerializer client = new PokecubeSerializer((ServerWorld) null);

    public static double distSq(final Vector4 location, final Vector4 meteor)
    {
        final double dx = location.x - meteor.x;
        final double dy = location.y - meteor.y;
        final double dz = location.z - meteor.z;
        return dx * dx + dy * dy + dz * dz;
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

    public ArrayList<Vector4> meteors;
    public ArrayList<Vector4> bases;

    public Map<String, List<Vector4>> structs;

    private int lastId = 1;

    public ServerWorld myWorld;
    public CompoundNBT customData = new CompoundNBT();

    private PokecubeSerializer(final MinecraftServer server)
    {
        this(server != null ? server.getWorld(DimensionType.OVERWORLD) : null);
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

    public void addMeteorLocation(final Vector4 v)
    {
        this.meteors.add(v);
        this.save();
    }

    public boolean shouldPlace(final String struct, final BlockPos pos, final DimensionType dim, final int seperation)
    {
        final List<Vector4> locs = this.structs.get(struct);
        if (locs == null) return true;
        final Vector4 check = new Vector4(pos, dim);
        for (final Vector4 v : locs)
        {
            if (v.w != dim.getId()) continue;
            if (check.withinDistance(seperation, v)) return false;
        }
        return true;
    }

    public void place(final String struct, final BlockPos pos, final DimensionType dim)
    {
        final Vector4 check = new Vector4(pos, dim);
        final List<Vector4> locs = this.structs.getOrDefault(struct, Lists.newArrayList());
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

    public boolean canMeteorLand(final Vector4 location, final ServerWorld world)
    {
        for (final Vector4 v : this.meteors)
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
                if (pokemobData != null)
                {
                    final Vector4 location = new Vector4(pokemobData);
                    if (location != null && !location.isEmpty())
                    {
                        for (final Vector4 v : this.meteors)
                            if (PokecubeSerializer.distSq(location, v) < 4) continue meteors;
                        this.meteors.add(location);
                    }
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
                if (pokemobData != null)
                {
                    final Vector4 location = new Vector4(pokemobData);
                    if (location != null && !location.isEmpty())
                    {
                        for (final Vector4 v : this.bases)
                            if (PokecubeSerializer.distSq(location, v) < 1) continue meteors;
                        this.bases.add(location);
                    }
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
                if (pokemobData != null)
                {
                    final Vector4 location = new Vector4(pokemobData);
                    final String struct = pokemobData.getString("type");
                    if (location != null && !location.isEmpty())
                    {
                        final List<Vector4> locs = this.structs.getOrDefault(struct, Lists.newArrayList());
                        locs.add(location);
                        this.structs.put(struct, locs);
                    }
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

    private boolean tooClose(final Vector4 location, final Vector4 meteor)
    {
        if (location.w != meteor.w) return false;
        return PokecubeSerializer.distSq(location, meteor) < PokecubeSerializer.MeteorDistance;
    }

    public void writeToNBT(final CompoundNBT tag)
    {
        tag.putInt(PokecubeSerializer.LASTUID, this.lastId);
        tag.put("data", this.customData);
        final ListNBT tagListMeteors = new ListNBT();
        for (final Vector4 v : this.meteors)
            if (v != null && !v.isEmpty())
            {
                final CompoundNBT nbt = new CompoundNBT();
                v.writeToNBT(nbt);
                tagListMeteors.add(nbt);
            }
        tag.put(PokecubeSerializer.METEORS, tagListMeteors);
        final ListNBT tagListStructs = new ListNBT();
        for (final String s : this.structs.keySet())
        {
            final List<Vector4> list = this.structs.get(s);
            for (final Vector4 v : list)
                if (v != null && !v.isEmpty())
                {
                    final CompoundNBT nbt = new CompoundNBT();
                    nbt.putString("type", s);
                    v.writeToNBT(nbt);
                    tagListStructs.add(nbt);
                }
        }
        tag.put(PokecubeSerializer.STRUCTS, tagListStructs);
        final ListNBT tagListBases = new ListNBT();
        for (final Vector4 v : this.bases)
            if (v != null && !v.isEmpty())
            {
                final CompoundNBT nbt = new CompoundNBT();
                v.writeToNBT(nbt);
                tagListMeteors.add(nbt);
            }
        tag.put(PokecubeSerializer.BASES, tagListBases);
        final CompoundNBT tms = new CompoundNBT();
        PokecubeItems.saveTime(tms);
        tag.put("tmtags", tms);
    }
}

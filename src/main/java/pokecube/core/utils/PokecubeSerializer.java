/**
 *
 */
package pokecube.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Vector;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.SaveHandler;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.contributors.Contributor;
import pokecube.core.contributors.ContributorManager;
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
            return new TeleDest(loc).setName(name).setIndex(index);
        }

        public Vector4 loc;
        Vector3        subLoc;
        String         name;
        public int     index;

        public TeleDest(final Vector4 loc)
        {
            this.loc = loc;
            this.subLoc = Vector3.getNewVector().set(loc.x, loc.y, loc.z);
            this.name = loc.toIntString();
        }

        public int getDim()
        {
            return (int) this.loc.w;
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
            nbt.putString("name", this.name);
            nbt.putInt("i", this.index);
        }
    }

    private static final String POKECUBE = "pokecube";
    private static final String DATA     = "data";
    private static final String METEORS  = "meteors";
    private static final String LASTUID  = "lastUid";

    public static int MeteorDistance = 3000 * 3000;

    public static PokecubeSerializer instance = null;

    public static double distSq(final Vector4 location, final Vector4 meteor)
    {
        final double dx = location.x - meteor.x;
        final double dy = location.y - meteor.y;
        final double dz = location.z - meteor.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public static PokecubeSerializer getInstance()
    {
        final World world = PokecubeCore.proxy.getWorld();
        if (world == null || world.isRemote) return PokecubeSerializer.instance == null
                ? PokecubeSerializer.instance = new PokecubeSerializer(world.getServer()) : PokecubeSerializer.instance;

        if (PokecubeSerializer.instance == null || PokecubeSerializer.instance.myWorld == world)
        {
            boolean toNew = false;
            toNew = PokecubeSerializer.instance == null || PokecubeSerializer.instance.saveHandler == null;
            if (!toNew)
            {
                PokecubeSerializer.instance.myWorld = PokecubeCore.proxy.getServerWorld();
                if (PokecubeSerializer.instance.myWorld != null)
                    PokecubeSerializer.instance.saveHandler = PokecubeSerializer.instance.myWorld.getSaveHandler();
            }
            if (toNew) PokecubeSerializer.instance = new PokecubeSerializer(world.getServer());
        }
        return PokecubeSerializer.instance;
    }

    SaveHandler saveHandler;

    public ArrayList<Vector4> meteors;

    private int lastId = 1;

    public ServerWorld myWorld;
    public CompoundNBT customData = new CompoundNBT();

    private PokecubeSerializer(final MinecraftServer server)
    {
        /** This data is saved to surface world's folder. */
        this.myWorld = server != null ? server.getWorld(DimensionType.OVERWORLD) : null;
        if (this.myWorld != null) this.saveHandler = this.myWorld.getSaveHandler();
        this.lastId = 0;
        this.meteors = new ArrayList<>();
        this.loadData();
    }

    public void addMeteorLocation(final Vector4 v)
    {
        this.meteors.add(v);
        this.save();
    }

    public boolean canMeteorLand(final Vector4 location)
    {
        for (final Vector4 v : this.meteors)
            if (this.tooClose(location, v)) return false;
        return true;
    }

    public void clearInstance()
    {
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
        INBT temp;
        temp = tag.get(PokecubeSerializer.METEORS);
        this.customData = tag.getCompound("data");
        if (temp instanceof ListNBT)
        {
            final ListNBT tagListMeteors = (ListNBT) temp;
            if (tagListMeteors.size() > 0) meteors:
            for (int i = 0; i < tagListMeteors.size(); i++)
            {
                final CompoundNBT pokemobData = tagListMeteors.getCompound(i);

                if (pokemobData != null)
                {
                    Vector4 location;
                    // TODO remove this in a few versions.
                    if (pokemobData.contains(PokecubeSerializer.METEORS + "x"))
                    {
                        final int posX = pokemobData.getInt(PokecubeSerializer.METEORS + "x");
                        final int posY = pokemobData.getInt(PokecubeSerializer.METEORS + "y");
                        final int posZ = pokemobData.getInt(PokecubeSerializer.METEORS + "z");
                        final int w = pokemobData.getInt(PokecubeSerializer.METEORS + "w");
                        location = new Vector4(posX, posY, posZ, w);
                    }
                    else location = new Vector4(pokemobData);
                    if (location != null && !location.isEmpty())
                    {
                        for (final Vector4 v : this.meteors)
                            if (PokecubeSerializer.distSq(location, v) < 4) continue meteors;
                        this.meteors.add(location);
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
        if (this.saveHandler == null || ThutCore.proxy.isClientSide()) return;

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
            final Contributor contrib = ContributorManager.instance().getContributor(owner.getGameProfile());
            if (contrib != null) entity.setPokecube(contrib.getStarterCube());
            else entity.setPokecube(new ItemStack(PokecubeItems.getFilledCube(PokecubeBehavior.DEFAULTCUBE)));
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
        final ListNBT tagListMeteors = new ListNBT();
        for (final Vector4 v : this.meteors)
            if (v != null && !v.isEmpty())
            {
                final CompoundNBT nbt = new CompoundNBT();
                v.writeToNBT(nbt);
                tagListMeteors.add(nbt);
            }
        tag.put("data", this.customData);
        tag.put(PokecubeSerializer.METEORS, tagListMeteors);
        final CompoundNBT tms = new CompoundNBT();
        PokecubeItems.saveTime(tms);
        tag.put("tmtags", tms);
    }
}

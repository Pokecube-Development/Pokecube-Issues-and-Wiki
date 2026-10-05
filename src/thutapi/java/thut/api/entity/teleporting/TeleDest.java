package thut.api.entity.teleporting;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import thut.api.ThutAPI;
import thut.api.maths.Vector3;

public class TeleDest
{

    public static TeleDest readFromNBT(final CompoundTag nbt)
    {
        Vector3 loc = Vector3.readFromNBT(nbt, "v");
        String name = nbt.getString("name");
        int index = nbt.getInt("i");
        int version = nbt.getInt("_v_");
        TeleDest dest = new TeleDest().setName(name).setIndex(index).setVersion(version);
        // New method
        if (nbt.contains("dim"))
        {
            var dim = ResourceLocation.parse(nbt.getString("dim"));
            dest.setLoc(GlobalPos.of(ResourceKey.create(Registries.DIMENSION, dim), loc.getPos()), loc);
        }
        else
        {
            // TODO remove legacy support
            GlobalPos pos;
            try
            {
                pos = GlobalPos.CODEC.decode(NbtOps.INSTANCE, nbt.get("pos")).result().get().getFirst();
                dest.setLoc(pos, loc);
            }
            catch (final Exception e)
            {
                ThutAPI.LOGGER.error("Error loading value", e);
                return null;
            }
        }
        TeleLoadEvent event = new TeleLoadEvent(dest);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return null;
        // The event can override the destination, it defaults to dest.
        return event.getOverride();
    }

    public GlobalPos loc;
    private final Vector3 teleLoc = new Vector3();
    private String name;

    public int index;

    // This can be used for tracking things like if worlds update and
    // teledests need resetting, etc.
    public int version = 0;

    public TeleDest()
    {}

    public TeleDest setLoc(final GlobalPos loc, final Vector3 subLoc)
    {
        this.loc = loc;
        this.teleLoc.set(subLoc);
        this.name = "";
        return this;
    }

    public TeleDest setPos(final GlobalPos pos)
    {
        if (pos != null)
        {
            this.loc = pos;
            this.teleLoc.set(this.loc.pos().getX() + 0.5, this.loc.pos().getY(), this.loc.pos().getZ() + 0.5);
            this.name = "";
        }
        return this;
    }

    public TeleDest copy()
    {
        return new TeleDest().setLoc(this.loc, this.getTeleLoc());
    }

    public TeleDest setVersion(final int version)
    {
        this.version = version;
        return this;
    }

    public GlobalPos getPos()
    {
        return this.loc;
    }

    public Vector3 getTeleLoc()
    {
        return teleLoc;
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

    public void writeToNBT(final CompoundTag nbt)
    {
        this.teleLoc.writeToNBT(nbt, "v");
        nbt.putString("dim", loc.dimension().location().toString());
        nbt.putString("name", this.name);
        nbt.putInt("i", this.index);
        nbt.putInt("_v_", this.version);
    }

    public void shift(final double dx, final double dy, final double dz)
    {
        this.teleLoc.x += dx;
        this.teleLoc.y += dy;
        this.teleLoc.z += dz;
        this.loc = new GlobalPos(this.loc.dimension(), this.teleLoc.getPos());
    }

    public Component getInfoName()
    {
        return Component.translatableEscape("teledest.location", this.loc.pos().getX(), this.loc.pos().getY(),
                this.loc.pos().getZ(), this.loc.dimension().location());
    }

    public boolean withinDist(final TeleDest other, final double dist)
    {
        if (other.loc.dimension() == this.loc.dimension()) return other.loc.pos().closerThan(this.loc.pos(), dist);
        return false;
    }
}

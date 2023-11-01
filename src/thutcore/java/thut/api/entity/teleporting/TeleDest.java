package thut.api.entity.teleporting;

import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;
import thut.lib.TComponent;

public class TeleDest
{

    public static TeleDest readFromNBT(final CompoundTag nbt)
    {
        final Vector3 loc = Vector3.readFromNBT(nbt, "v");
        final String name = nbt.getString("name");
        final int index = nbt.getInt("i");
        final int version = nbt.getInt("_v_");
        GlobalPos pos = null;
        try
        {
            pos = GlobalPos.CODEC.decode(NbtOps.INSTANCE, nbt.get("pos")).result().get().getFirst();
        }
        catch (final Exception e)
        {
            ThutCore.LOGGER.error("Error loading value", e);
            return null;
        }
        final TeleDest dest = new TeleDest().setLoc(pos, loc).setName(name).setIndex(index).setVersion(version);
        final TeleLoadEvent event = new TeleLoadEvent(dest);
        ThutCore.FORGE_BUS.post(event);
        if (event.isCanceled()) return null;
        // The event can override the destination, it defaults to dest.
        return event.getOverride();
    }

    public GlobalPos loc;
    private Vector3 subLoc;
    private Vector3 teleLoc = new Vector3();
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
        this.subLoc = subLoc;
        this.name = "";
        return this;
    }

    public TeleDest setPos(final GlobalPos pos)
    {
        if (pos != null)
        {
            this.loc = pos;
            this.subLoc = new Vector3().set(this.loc.pos().getX() + 0.5, this.loc.pos().getY(),
                    this.loc.pos().getZ() + 0.5);
            this.name = "";
        }
        return this;
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

    public Vector3 getLoc()
    {
        return this.subLoc;
    }

    public Vector3 getTeleLoc()
    {
        double dx = subLoc.x > 0 ? this.subLoc.x % 1 : -this.subLoc.x % 1;
        double dy = subLoc.y > 0 ? this.subLoc.y % 1 : -this.subLoc.y % 1;
        double dz = subLoc.z > 0 ? this.subLoc.z % 1 : -this.subLoc.z % 1;
        return teleLoc.set(this.getPos().pos()).add(dx, dy, dz);
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
        if (this.subLoc == null) this.subLoc = new Vector3().set(this.loc.pos()).add(0.5, 0, 0.5);
        this.subLoc.writeToNBT(nbt, "v");
        nbt.put("pos", GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, this.loc).get().left().get());
        nbt.putString("name", this.name);
        nbt.putInt("i", this.index);
        nbt.putInt("_v_", this.version);
    }

    public void shift(final double dx, final int dy, final double dz)
    {
        this.subLoc.x += dx;
        this.subLoc.y += dy;
        this.subLoc.z += dz;
    }

    public Component getInfoName()
    {
        return TComponent.translatable("teledest.location", this.loc.pos().getX(), this.loc.pos().getY(),
                this.loc.pos().getZ(), this.loc.dimension().location());
    }

    public boolean withinDist(final TeleDest other, final double dist)
    {
        if (other.loc.dimension() == this.loc.dimension()) return other.loc.pos().closerThan(this.loc.pos(), dist);
        return false;
    }
}

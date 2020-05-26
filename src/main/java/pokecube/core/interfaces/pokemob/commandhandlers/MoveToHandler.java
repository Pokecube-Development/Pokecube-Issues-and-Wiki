package pokecube.core.interfaces.pokemob.commandhandlers;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.network.pokemobs.PacketCommand.DefaultHandler;
import thut.api.maths.Vector3;

public class MoveToHandler extends DefaultHandler
{
    Vector3 location;
    float   speed;

    public MoveToHandler()
    {
    }

    public MoveToHandler(final Vector3 location, final Float speed)
    {
        this.location = location.copy();
        this.speed = Math.abs(speed);
    }

    @Override
    public void handleCommand(final IPokemob pokemob) throws Exception
    {
        this.speed = Math.min(this.speed, 1);
        pokemob.getEntity().getBrain().setMemory(MemoryModules.WALK_TARGET, new WalkTarget(this.location.toVec3d(),
                this.speed, 0));
    }

    @Override
    public void readFromBuf(final ByteBuf buf)
    {
        super.readFromBuf(buf);
        this.location = Vector3.readFromBuff(buf);
        this.speed = buf.readFloat();
    }

    @Override
    public void writeToBuf(final ByteBuf buf)
    {
        super.writeToBuf(buf);
        this.location.writeToBuff(buf);
        buf.writeFloat(this.speed);
    }
}

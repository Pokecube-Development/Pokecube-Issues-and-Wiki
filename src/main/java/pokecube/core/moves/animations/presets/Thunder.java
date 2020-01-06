package pokecube.core.moves.animations.presets;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.effect.LightningBoltEntity;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.moves.animations.AnimPreset;
import pokecube.core.moves.animations.MoveAnimationBase;

@AnimPreset(getPreset = "thunder")
public class Thunder extends MoveAnimationBase
{

    public Thunder()
    {
    }

    @Override
    public int getDuration()
    {
        return 0;
    }

    @Override
    public void initColour(long time, float partialTicks, Move_Base move)
    {
        // No colouring for thunder.
    }

    @Override
    public void spawnClientEntities(MovePacketInfo info)
    {
        final ClientWorld theRealWorld = (ClientWorld) info.attacker.getEntityWorld();
        final LightningBoltEntity lightning = new LightningBoltEntity(theRealWorld, info.target.x, info.target.y,
                info.target.z, false);
        theRealWorld.addEntity(lightning);
        theRealWorld.addLightning(lightning);
    }
}

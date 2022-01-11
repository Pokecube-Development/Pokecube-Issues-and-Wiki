package pokecube.core.moves.animations.presets;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
    public void initColour(final long time, final float partialTicks, final Move_Base move)
    {
        // No colouring for thunder.
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public void spawnClientEntities(final MovePacketInfo info)
    {
        final net.minecraft.client.multiplayer.ClientLevel theRealWorld = (net.minecraft.client.multiplayer.ClientLevel) info.attacker
                .getLevel();
        final LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, theRealWorld);
        info.target.moveEntity(lightning);
        lightning.setVisualOnly(false);
        theRealWorld.addFreshEntity(lightning);
    }
}

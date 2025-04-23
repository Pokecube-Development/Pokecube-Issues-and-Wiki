package pokecube.core.moves.animations.presets;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import pokecube.api.moves.MoveEntry;
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
    public void initColour(final float time, final MoveEntry move)
    {
        // No colouring for thunder.
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public void spawnClientEntities(final MovePacketInfo info, float partialTicks)
    {
        var theRealWorld = info.attacker.level();
        final LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, theRealWorld);
        info.target.moveEntity(lightning);
        lightning.setVisualOnly(true);
        theRealWorld.addFreshEntity(lightning);
    }
}

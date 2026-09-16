package pokecube.core.effects.presets;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import pokecube.api.moves.MoveEntry;
import pokecube.core.effects.AnimPreset;
import pokecube.core.effects.MoveAnimationBase;
import thut.api.maths.Vector3;

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
    public void spawnClientEntities(final EffectPacketInfo info, float partialTicks)
    {
        final LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, info.level);
        var target = new Vector3(info.getTarget());
        target.moveEntity(lightning);
        lightning.setVisualOnly(true);
        info.level.addFreshEntity(lightning);
    }
}

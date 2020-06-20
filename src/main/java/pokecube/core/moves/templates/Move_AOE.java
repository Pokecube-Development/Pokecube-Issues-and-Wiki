package pokecube.core.moves.templates;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.Vec3d;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.animations.AnimationMultiAnimations;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;

public class Move_AOE extends Move_Basic
{
    public Move_AOE(final String name)
    {
        super(name);
    }

    @Override
    public void attack(final IPokemob attacker, final Vector3 location, final Predicate<Entity> valid,
            final Consumer<Entity> onHit)
    {
        final Entity entity = attacker.getEntity();
        if (!this.move.isNotIntercepable())
        {
            final Vec3d loc1 = new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
            final Vec3d loc2 = new Vec3d(location.x, location.y, location.z);
            final BlockRayTraceResult result = entity.getEntityWorld().rayTraceBlocks(new RayTraceContext(loc1, loc2,
                    RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, entity));
            if (result != null) location.set(result.getHitVec());
        }
        final List<LivingEntity> targets = MovesUtils.targetsHit(entity, location, 8);
        final int n = targets.size();
        if (n > 0)
        {
            this.playSounds(entity, null, location);
            for (final LivingEntity e : targets)
                if (e != null && valid.test(e))
                {
                    if (AnimationMultiAnimations.isThunderAnimation(this.getAnimation(attacker)))
                    {
                        final LightningBoltEntity lightning = new LightningBoltEntity(e.getEntityWorld(), 0, 0, 0,
                                false);
                        e.onStruckByLightning(lightning);
                    }
                    if (e instanceof CreeperEntity)
                    {
                        final CreeperEntity creeper = (CreeperEntity) e;
                        if (this.move.type == PokeType.getType("psychic") && creeper.getHealth() > 0) creeper.explode();
                    }
                    byte statusChange = IMoveConstants.STATUS_NON;
                    byte changeAddition = IMoveConstants.CHANGE_NONE;
                    if (this.move.statusChange != IMoveConstants.STATUS_NON && MovesUtils.rand
                            .nextFloat() <= this.move.statusChance) statusChange = this.move.statusChange;
                    if (this.move.change != IMoveConstants.CHANGE_NONE && MovesUtils.rand
                            .nextFloat() <= this.move.chanceChance) changeAddition = this.move.change;
                    final MovePacket packet = new MovePacket(attacker, e, this.name, this.getType(attacker), this
                            .getPWR(attacker, e), this.move.crit, statusChange, changeAddition);
                    this.onAttack(packet);
                    onHit.accept(e);
                }
        }
        else MovesUtils.displayEfficiencyMessages(attacker, null, -1, 0);
        this.doWorldAction(attacker, location);
    }

}

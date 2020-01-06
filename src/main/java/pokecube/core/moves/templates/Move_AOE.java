package pokecube.core.moves.templates;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
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
    public Move_AOE(String name)
    {
        super(name);
    }

    @Override
    public void attack(IPokemob attacker, Vector3 location)
    {
        final List<Entity> targets = new ArrayList<>();

        final Entity entity = attacker.getEntity();

        if (!this.move.isNotIntercepable())
        {
            final Vec3d loc1 = new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
            final Vec3d loc2 = new Vec3d(location.x, location.y, location.z);
            final BlockRayTraceResult result = entity.getEntityWorld().rayTraceBlocks(new RayTraceContext(loc1, loc2,
                    RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, entity));
            // TODO check if this is relative or absolute positon.
            if (result != null) location.set(result.getHitVec());

        }
        targets.addAll(MovesUtils.targetsHit(entity, location, 2, 8));
        final int n = targets.size();
        if (n > 0)
        {
            this.playSounds(entity, null, location);
            for (final Entity e : targets)
                if (e != null)
                {
                    final Entity attacked = e;
                    if (AnimationMultiAnimations.isThunderAnimation(this.getAnimation(attacker)))
                    {
                        final LightningBoltEntity lightning = new LightningBoltEntity(attacked.getEntityWorld(), 0, 0,
                                0, false);
                        attacked.onStruckByLightning(lightning);
                    }
                    if (attacked instanceof CreeperEntity)
                    {
                        final CreeperEntity creeper = (CreeperEntity) attacked;
                        if (this.move.type == PokeType.getType("psychic") && creeper.getHealth() > 0) creeper.explode();
                    }
                    byte statusChange = IMoveConstants.STATUS_NON;
                    byte changeAddition = IMoveConstants.CHANGE_NONE;
                    if (this.move.statusChange != IMoveConstants.STATUS_NON && MovesUtils.rand
                            .nextFloat() <= this.move.statusChance) statusChange = this.move.statusChange;
                    if (this.move.change != IMoveConstants.CHANGE_NONE && MovesUtils.rand
                            .nextFloat() <= this.move.chanceChance) changeAddition = this.move.change;
                    final MovePacket packet = new MovePacket(attacker, attacked, this.name, this.getType(attacker), this
                            .getPWR(attacker, attacked), this.move.crit, statusChange, changeAddition);
                    this.onAttack(packet);
                }
        }
        else MovesUtils.displayEfficiencyMessages(attacker, null, -1, 0);
        this.doWorldAction(attacker, location);
    }

}

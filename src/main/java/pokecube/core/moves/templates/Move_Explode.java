/**
 *
 */
package pokecube.core.moves.templates;

import java.util.BitSet;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Explosion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ExplosionEvent;
import pokecube.api.data.moves.IMove;
import pokecube.api.data.moves.MoveApplicationRegistry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.DamageApplier;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;
import pokecube.core.PokecubeCore;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.core.init.Config;
import pokecube.core.moves.MovesUtils;
import thut.api.boom.ExplosionCustom;
import thut.api.boom.ExplosionCustom.IEntityHitter;
import thut.api.maths.Vector3;

/** @author Manchou */
public class Move_Explode implements IMove
{
    private static class Hitter implements IEntityHitter
    {
        private final IPokemob user;
        private final MoveApplication move;
        private final BitSet hit = new BitSet();

        public Hitter(MoveApplication move)
        {
            this.move = move;
            user = move.getUser();
        }

        @Override
        public void hitEntity(final Entity e, final float power, final Explosion boom)
        {
            // Dont hit twice, and only hit living entities.
            if (this.hit.get(e.getId()) || !(e instanceof LivingEntity living)) return;
            // Dont hit self, that is taken care of elsewhere.
            if (e == this.user.getEntity()) return;
            // Flag as already hit.
            this.hit.set(e.getId());
            MoveApplicationRegistry.apply(move);
        }

    }

    // TODO: Fix .setExplosion().bypassMagic()
    public static final ResourceKey<DamageType> SELF_BOOM = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(PokecubeCore.MODID, "pokemob.explosion"));
//    public static final DamageSource SELF_BOOMS = new DamageSource((Holder<DamageType>) SELF_BOOM).is(DamageTypeTags.BYPASSES_ENCHANTMENTS).is(DamageTypeTags.IS_EXPLOSION).setExplosion().bypassMagic();

    public Move_Explode()
    {}

    DamageApplier damage = new DamageApplier()
    {
        @Override
        public Damage applyDamage(MoveApplication t)
        {
            Object test = t.customFlags.get("explosion-moves-flag");
            if (test instanceof Hitter hitter && t.getTarget() != null)
            {
                hitter.hit.set(t.getTarget().getId());
            }
            return DamageApplier.super.applyDamage(t);
        }
    };

    PostMoveUse postUse = new PostMoveUse()
    {
        @Override
        public void applyPostMove(Damage t)
        {
            IPokemob user = t.move().getUser();
            Mob mob = user.getEntity();
            Object test = t.move().customFlags.get("explosion-moves-flag");
            Object hit = t.move().customFlags.get("explosion-moves-boomed");
            if (test instanceof Hitter hitter && hit == null)
            {
                t.move().customFlags.put("explosion-moves-boomed", Boolean.TRUE);

                final float f1 = (float) (t.move().pwr * PokecubeCore.getConfig().blastStrength
                        * user.getStat(Stats.ATTACK, true) / 500000f);

                final boolean explodeDamage = mob.level() instanceof ServerLevel level && Config.Rules.doBoom(level);
                final boolean damagePerms = MoveEventsHandler.canAffectBlock(user, new Vector3().set(mob),
                        t.move().getName());

                if (explodeDamage && damagePerms)
                {
                    final ExplosionCustom boom = MovesUtils.newExplosion(mob, mob.getX(), mob.getY(), mob.getZ(), f1);
                    boom.hitter = hitter;
                    final ExplosionEvent.Start evt = new ExplosionEvent.Start(mob.level(), boom);
                    MinecraftForge.EVENT_BUS.post(evt);
                    if (!evt.isCanceled()) boom.doExplosion();
                }

                // First give it some health so it is alive
                mob.setHealth(1);
                // Now we kill the user via a damage source.
                mob.hurt(mob.level.damageSources().source(SELF_BOOM), mob.getMaxHealth() * 1e5f);
            }
        }
    };

    @Override
    public void accept(MoveApplication t)
    {
        // Only run this the first time, it gets re-used after that
        t.customFlags.computeIfAbsent("explosion-moves-flag", s -> {
            IMove.super.accept(t);
            Hitter hitter = new Hitter(t);
            return hitter;
        });
    }

    @Override
    public PostMoveUse getPostUse(MoveApplication t)
    {
        return postUse;
    }
}

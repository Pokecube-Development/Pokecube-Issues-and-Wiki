package pokecube.mobs.abilities.simple;

import net.minecraft.world.damagesource.DamageSource;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.database.Database;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.damage.GenericDamageSource;

@AbilityProvider(name = "gulp-missile")
public class GulpMissile extends Ability
{
    private static PokedexEntry baseNormal;
    private static PokedexEntry arrakuda;
    private static PokedexEntry pikachu;

    private static boolean noTurnBase = false;

    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeUser(mob, move)) return;
        if (areWeTarget(mob, move)) return;
        if (GulpMissile.noTurnBase) return;
        if (GulpMissile.baseNormal == null)
        {
            GulpMissile.baseNormal = Database.getEntry("cramorant");
            GulpMissile.arrakuda = Database.getEntry("cramorant-gulping");
            GulpMissile.pikachu = Database.getEntry("cramorant-gorging");
            GulpMissile.noTurnBase = GulpMissile.baseNormal == null;
            if (GulpMissile.noTurnBase) return;
        }

        String name = move.getName();
        // Turn Forme
        final PokedexEntry mobs = mob.getPokedexEntry();
        if (mobs == GulpMissile.baseNormal) if (name.equals("surf") || name.equals("dive"))
            if (mob.getEntity().getHealth() < mob.getEntity().getMaxHealth() / 2)
        {
            if (mobs == GulpMissile.baseNormal) mob.setPokedexEntry(GulpMissile.pikachu);
        }
            else if (mob.getEntity().getHealth() > mob.getEntity().getMaxHealth() / 2)
                if (mobs == GulpMissile.baseNormal) mob.setPokedexEntry(GulpMissile.arrakuda);

        final IPokemob attacker = move.getUser();
        final float amount = attacker.getEntity().getMaxHealth() / 4;
        final DamageSource source = new GenericDamageSource(this.getName(), mob.getEntity()).bypassMagic()
                .setProjectile();
        // Hit for Arrakuda
        if (mobs == GulpMissile.arrakuda)
        {
            if (move.hit)
            {
                attacker.getEntity().hurt(source, amount);
                MovesUtils.handleStats2(mob, attacker.getEntity(), IMoveConstants.DEFENSE, IMoveConstants.FALL);
                mob.setPokedexEntry(GulpMissile.baseNormal);
            }
        }
        // Hit for Pikachu
        else if (mobs == GulpMissile.pikachu) if (move.hit)
        {
            attacker.getEntity().hurt(source, amount);
            MovesUtils.setStatus(mob, attacker.getEntity(), IMoveConstants.STATUS_PAR);
            mob.setPokedexEntry(GulpMissile.baseNormal);
        }
    }

    @Override
    public IPokemob onRecall(final IPokemob mob)
    {
        final PokedexEntry mobs = mob.getPokedexEntry();
        final boolean isSurf = mobs == GulpMissile.arrakuda;
        final boolean isDive = mobs == GulpMissile.pikachu;
        if (isSurf || isDive) return mob.setPokedexEntry(GulpMissile.baseNormal);
        return super.onRecall(mob);
    }
}

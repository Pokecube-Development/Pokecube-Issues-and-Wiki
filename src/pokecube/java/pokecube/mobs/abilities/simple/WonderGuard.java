package pokecube.mobs.abilities.simple;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.utils.Tools;
import pokecube.core.moves.damage.sources.IPokedamage;

@AbilityProvider(name = "wonder-guard")
@EventBusSubscriber
public class WonderGuard extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        final MoveEntry attack = move.getMove();
        final IPokemob attacker = move.getUser();
        if (!areWeTarget(mob, move)) return;
        final float eff = Tools.getAttackEfficiency(attack.getType(attacker), mob.getType1(), mob.getType2());
        if (eff <= 1 && attack.getPWR(attacker, mob.getEntity()) > 0) move.canceled = true;
    }

    public static final TagKey<DamageType> BYPASSES_WONDERGUARD = TagKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.parse("pokecube_mobs:bypasses_wonder_guard"));

    @SubscribeEvent
    public static void preMobDamaged(EntityInvulnerabilityCheckEvent event)
    {
        var pokemob = PokemobCaps.getPokemobFor(event.getEntity());
        if (pokemob == null) return;
        var ability = pokemob.getAbility();
        if (ability == null) return;
        if (ability.toString().equals("wonder-guard"))
        {
            // Pokemob damage sources are not marked as bypassing, so we can do a type check like this.
            if (event.getSource() instanceof IPokedamage pdamage)
            {
                var type = pdamage.getType();
                float eff = Tools.getAttackEfficiency(type, pokemob.getType1(), pokemob.getType2());
                if (eff > 1) return;
            }
            if (!event.getSource().is(BYPASSES_WONDERGUARD)) event.setInvulnerable(true);
        }
    }
}

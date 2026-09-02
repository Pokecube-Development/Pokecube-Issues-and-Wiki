package pokecube.mobs.abilities.simple;

import pokecube.api.PokecubeAPI;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.PokecubeCore;
import pokecube.core.moves.MovesUtils;

@AbilityProvider(name = "rock-head")
public class RockHead extends Ability
{
    public static MoveApplication.RecoilApplier ROCKHEAD = new MoveApplication.RecoilApplier() {
        @Override
        public void applyRecoil(MoveApplication.Damage t) // Ignore negative recoil.
        {
            if (PokecubeCore.getConfig().debug_moves)
                PokecubeAPI.LOGGER.info("User of {} has rock head - ignoring recoil damage", t.move().getName());

            var moveAppl = t.move();
            MoveEntry move = moveAppl.getMove();
            int dealt = t.dealt();
            float recoil = dealt * move.root_entry._drain / 100.0f;

            IPokemob other = PokemobCaps.getPokemobFor(moveAppl.getTarget());
            // This means the move heals as recoil.
            if (recoil > 0)
            {
                if (PokecubeCore.getConfig().debug_moves)
                    PokecubeAPI.LOGGER.info("Applying recoil healing for move {} of amount {}", t.move().getName(),
                            recoil);
                recoil = Math.min(recoil, moveAppl.getUser().getMaxHealth() - moveAppl.getUser().getHealth());
                if (recoil > 0) moveAppl.getUserEntity().heal(recoil);
                MovesUtils.sendPairedMessages(moveAppl.getUser().getEntity(), other, "pokemob.move.recoil.heal");
            }
        }
    };

    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        move.recoil = ROCKHEAD;
    }

}

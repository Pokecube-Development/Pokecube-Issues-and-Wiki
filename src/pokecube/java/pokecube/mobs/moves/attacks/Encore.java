package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;
import pokecube.core.PokecubeCore;
import pokecube.core.moves.MovesUtils;

@MoveProvider(name = "encore")
public class Encore implements PostMoveUse
{
    @Override
    public void applyPostMove(Damage t)
    {
        MoveApplication packet = t.move();
        if (packet.canceled || packet.failed) return;

        IPokemob attacker = packet.getUser();
        final IPokemob target = PokemobCaps.getPokemobFor(packet.getTarget());
        if (target != null)
        {
            String[] targetMoves = target.getMoves();
            int lastMoveIndex = -1;
            for (int i = 0; i < targetMoves.length; i++) {
                if (targetMoves[i] == target.getLastMoveUsed()) {
                    lastMoveIndex = i; // Finds index of last move used
                }
            }
            if (lastMoveIndex == -1) lastMoveIndex = attacker.getEntity().getRandom().nextInt(4); // If move is not found, select a random one to encore.

            final int timer = attacker.getEntity().getRandom().nextInt(7); // Same as Disable

            // (lastMoveIndex + 1) % targetMoves.length selects a move that is sure to not be the last move.
            if (target.getDisableTimer((lastMoveIndex + 1) % targetMoves.length) <= 0 && timer > 0) { // Applies timer if the disabled moves are not yet disabled.
                for (int i = 0; i < targetMoves.length; i++) {
                    if (i != lastMoveIndex) target.setDisableTimer(lastMoveIndex, PokecubeCore.getConfig().attackCooldown * timer);
                }
            }

            else MovesUtils.displayEfficiencyMessages(attacker, packet.getTarget(), -2, 0);
        }
    }
}

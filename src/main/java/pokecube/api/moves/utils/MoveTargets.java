package pokecube.api.moves.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.events.pokemobs.combat.MoveUse;
import pokecube.api.moves.MoveEntry;
import pokecube.api.utils.PokeType;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;

public class MoveTargets
{
    public static interface IMoveTargetter
    {
        default boolean isValid(LivingEntity target)
        {
            return true;
        }

        default boolean isAoE()
        {
            return false;
        }

        default boolean isInterceptable()
        {
            return true;
        }

        default void initiateMove(@Nonnull final MoveEntry move, @Nonnull IPokemob user,
                @Nullable LivingEntity expectedTarget, @Nullable Vector3 expectedEnd)
        {
            // AoE moves have their damage dealt with by the move entity itself.
            if (isAoE())
            {

            }
        }
    }

    public static interface IMoveApplier
    {
        default void onMoveUse(IPokemob user, LivingEntity target)
        {
            Mob attacker = user.getEntity();
            MovePacket packet = new MovePacket(user, attacker, getMove());

            // First check if we had the move denied in a preMoveUse check
            preMoveUse(packet);

            // If denied, return early, no message. denier should have sent
            // that, also no postMoveUse application.
            if (packet.denied) return;

            final LivingEntity attacked = packet.attacked;

            final String attack = packet.attack;
            final PokeType type = packet.attackType;
            final int PWR = packet.PWR;
            int criticalLevel = packet.criticalLevel;
            final int statusChange = packet.statusChange;
            final int changeAddition = packet.changeAddition;

            // Now check other conditions. Cancelled first. This should still
            // trigger a post-move used call, with the details of what happened.
            if (packet.canceled || packet.failed || attacked == null)
            {
                // for now, buth have the same message, as "cancelled" and
                // "failed" are normally similar causes.

                // If the attacked was null, this message should tell at least
                // the user that things broke.
                MovesUtils.displayEfficiencyMessages(user, attacked, -2, 0);
                packet = new MovePacket(user, attacked, attack, type, PWR, criticalLevel, statusChange, changeAddition,
                        false);
                packet.hit = false;
                packet.didCrit = false;
                this.postMoveUse(packet);
                return;
            }

        }

        default void preMoveUse(final MovePacket packet)
        {
            PokecubeAPI.MOVE_BUS.post(new MoveUse.ActualMoveUse.Pre(packet.attacker, this.getMove(), packet.attacked));
        }

        default void postMoveUse(final MovePacket packet)
        {
            PokecubeAPI.MOVE_BUS.post(new MoveUse.ActualMoveUse.Post(packet.attacker, this.getMove(), packet.attacked));
        }

        MoveEntry getMove();
    }
}

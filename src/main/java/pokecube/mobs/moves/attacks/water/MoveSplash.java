package pokecube.mobs.moves.attacks.water;

import net.minecraft.util.text.ITextComponent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.templates.Move_Basic;
import thut.core.common.commands.CommandTools;

public class MoveSplash extends Move_Basic
{

    public MoveSplash()
    {
        super("splash");
    }

    @Override
    public void preAttack(MovePacket packet)
    {
        super.preAttack(packet);
        packet.denied = true;
        final IPokemob attacked = CapabilityPokemob.getPokemobFor(packet.attacked);
        if (attacked != null)
        {
            ITextComponent text = CommandTools.makeTranslatedMessage("pokemob.move.doesnt.affect", "red", attacked
                    .getDisplayName().getFormattedText());
            packet.attacker.displayMessageToOwner(text);
            text = CommandTools.makeTranslatedMessage("pokemob.move.doesnt.affect", "green", attacked
                    .getDisplayName().getFormattedText());
            attacked.displayMessageToOwner(text);
        }
    }

}

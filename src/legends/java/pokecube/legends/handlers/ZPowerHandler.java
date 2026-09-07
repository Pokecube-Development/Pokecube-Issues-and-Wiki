package pokecube.legends.handlers;

import net.minecraft.world.item.ItemStack;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.core.moves.MovesUtils;
import pokecube.gimmicks.zmoves.ZPower;
import pokecube.legends.items.zmove.ItemZCrystal;

public class ZPowerHandler implements ZPower
{
    public ZPowerHandler()
    {}

    @Override
    public boolean canZMove(final IPokemob pokemob, final String moveIn)
    {
        final MoveEntry move = MovesUtils.getMove(moveIn);
        if (move == null) return false;
        final ItemStack held = pokemob.getHeldItem();
        if (held.isEmpty()) return false;
        if (!(held.getItem() instanceof ItemZCrystal zcrys)) return false;
        return zcrys.type == move.getType(pokemob);
    }
}

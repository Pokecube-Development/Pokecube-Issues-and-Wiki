package pokecube.legends.handlers;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.zmoves.CapabilityZMove;
import pokecube.core.moves.zmoves.ZPower;
import pokecube.legends.items.zmove.ItemZCrystal;

public class ZPowerHandler implements ZPower, ICapabilityProvider
{

    private final LazyOptional<ZPower> holder = LazyOptional.of(() -> this);

    public ZPowerHandler()
    {
    }

    @Override
    public boolean canZMove(final IPokemob pokemob, final String moveIn)
    {
        if (pokemob.getCombatState(CombatStates.USEDZMOVE)) return false;
        final Move_Base move = MovesUtils.getMoveFromName(moveIn);
        if (move == null) return false;
        final ItemStack held = pokemob.getHeldItem();
        if (held.isEmpty()) return false;
        if (!(held.getItem() instanceof ItemZCrystal)) return false;
        final ItemZCrystal zcrys = (ItemZCrystal) held.getItem();
        if (zcrys.type != move.getType(pokemob)) return false;
        return true;
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
    {
        return CapabilityZMove.CAPABILITY.orEmpty(cap, this.holder);
    }

}

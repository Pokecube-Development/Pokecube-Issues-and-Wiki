package pokecube.legends.handlers;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.core.moves.MovesUtils;
import pokecube.gimmicks.zmoves.CapabilityZMove;
import pokecube.gimmicks.zmoves.GZMoveManager;
import pokecube.gimmicks.zmoves.ZPower;
import pokecube.legends.items.zmove.ItemZCrystal;
import thut.api.Tracker;

public class ZPowerHandler implements ZPower, ICapabilityProvider
{

    private final LazyOptional<ZPower> holder = LazyOptional.of(() -> this);

    public ZPowerHandler()
    {}

    @Override
    public boolean canZMove(final IPokemob pokemob, final String moveIn)
    {
        LivingEntity owner = pokemob.getOwner();
        if (owner != null)
        {
            long lastUse = owner.getPersistentData().getLong("pokecube:used-z-move");
            long tick = Tracker.instance().getTick();
            if (lastUse + GZMoveManager.Z_MOVE_COOLDOWN > tick) return false;
        }
        final MoveEntry move = MovesUtils.getMove(moveIn);
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

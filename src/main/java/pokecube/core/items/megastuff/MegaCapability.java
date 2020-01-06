package pokecube.core.items.megastuff;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;

public class MegaCapability implements ICapabilityProvider, IMegaCapability
{
    public static interface RingChecker
    {
        boolean canMegaEvolve(PlayerEntity player, PokedexEntry toEvolve);
    }

    public static RingChecker checker = (player, toEvolve) ->
    {
        for (int i1 = 0; i1 < player.inventory.getSizeInventory(); i1++)
        {
            final ItemStack stack1 = player.inventory.getStackInSlot(i1);
            if (stack1 != null) if (MegaCapability.matches(stack1, toEvolve)) return true;
        }
        for (int i2 = 0; i2 < player.inventory.armorInventory.size(); i2++)
        {
            final ItemStack stack2 = player.inventory.armorInventory.get(i2);
            if (stack2 != null) if (MegaCapability.matches(stack2, toEvolve)) return true;
        }
        return false;
    };

    @CapabilityInject(IMegaCapability.class)
    public static final Capability<IMegaCapability> MEGA_CAP = null;

    public static boolean canMegaEvolve(PlayerEntity player, IPokemob target)
    {
        final PokedexEntry entry = target.getPokedexEntry();
        return MegaCapability.checker.canMegaEvolve(player, entry);
    }

    public static boolean matches(ItemStack stack, PokedexEntry entry)
    {
        final IMegaCapability cap = stack.getCapability(MegaCapability.MEGA_CAP, null).orElse(null);
        if (cap != null)
        {
            if (cap.isStone(stack)) return false;
            PokedexEntry stacks;
            if ((stacks = cap.getEntry(stack)) == null) return true;
            final PokedexEntry stackbase = stacks.getBaseForme() == null ? stacks : stacks.getBaseForme();
            final PokedexEntry entrybase = entry.getBaseForme() == null ? entry : entry.getBaseForme();
            return entrybase == stackbase;
        }
        return false;
    }

    final ItemStack stack;

    private final LazyOptional<IMegaCapability> holder;

    public MegaCapability(ItemStack itemStack)
    {
        this.stack = itemStack;
        this.holder = LazyOptional.of(() -> this.stack.getItem() instanceof IMegaCapability
                ? (IMegaCapability) this.stack.getItem() : this);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing)
    {
        return MegaCapability.MEGA_CAP.orEmpty(capability, this.holder);
    }

    @Override
    public PokedexEntry getEntry(ItemStack stack)
    {
        if (stack.getItem() instanceof IMegaCapability) return ((IMegaCapability) stack.getItem()).getEntry(stack);
        if (PokecubeItems.is(PokecubeItems.HELDKEY, stack)) return Database.getEntry(stack.getItem().getRegistryName()
                .getPath());
        if (stack.hasTag() && stack.getTag().contains("gemTag"))
        {
            final ItemStack stack2 = ItemStack.read(stack.getChildTag("gemTag"));
            if (!stack2.isEmpty()) return this.getEntry(stack2);
        }
        return null;
    }

    @Override
    public boolean isStone(ItemStack stack)
    {
        if (stack.getItem() instanceof IMegaCapability) return ((IMegaCapability) stack.getItem()).isStone(stack);
        return PokecubeItems.is(PokecubeItems.HELDKEY, stack) && stack.getItem().getRegistryName().getPath().contains(
                "mega");
    }

    @Override
    public boolean isValid(ItemStack stack, PokedexEntry entry)
    {
        if (stack.getItem() instanceof IMegaCapability) return ((IMegaCapability) stack.getItem()).isValid(stack,
                entry);
        final PokedexEntry stacks = this.getEntry(stack);
        if (entry == null) return true;
        if (stacks == null) return true;
        final PokedexEntry stackbase = stacks.getBaseForme() == null ? stacks : stacks.getBaseForme();
        final PokedexEntry entrybase = entry.getBaseForme() == null ? entry : entry.getBaseForme();
        return entrybase == stackbase;
    }
}

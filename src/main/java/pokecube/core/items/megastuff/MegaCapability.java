package pokecube.core.items.megastuff;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.CapHolders;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.PlayerWearables;

public class MegaCapability implements ICapabilityProvider, IMegaCapability
{
    private static final ResourceLocation MEGAWORNTAG = new ResourceLocation("pokecube", "mega_wearables");

    public static interface RingChecker
    {
        boolean canMegaEvolve(PlayerEntity player, PokedexEntry toEvolve);
    }

    public static RingChecker checker = (player, toEvolve) ->
    {
        final PlayerWearables worn = ThutWearables.getWearables(player);
        for (final ItemStack stack : worn.getWearables())
            if (PokecubeItems.is(MegaCapability.MEGAWORNTAG, stack) && MegaCapability.matches(stack, toEvolve))
                return true;
        return false;
    };

    public static boolean canMegaEvolve(final PlayerEntity player, final IPokemob target)
    {
        final PokedexEntry entry = target.getPokedexEntry();
        return MegaCapability.checker.canMegaEvolve(player, entry);
    }

    public static boolean matches(final ItemStack stack, final PokedexEntry entry)
    {
        final IMegaCapability cap = stack.getCapability(CapHolders.MEGA_CAP, null).orElse(null);
        if (cap != null)
        {
            if (cap.isStone(stack)) return false;
            if (!cap.isValid(stack, entry)) return false;
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

    public MegaCapability(final ItemStack itemStack)
    {
        this.stack = itemStack;
        this.holder = LazyOptional.of(() -> this.stack.getItem() instanceof IMegaCapability
                ? (IMegaCapability) this.stack.getItem()
                : this);
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> capability, final Direction facing)
    {
        return CapHolders.MEGA_CAP.orEmpty(capability, this.holder);
    }

    @Override
    public PokedexEntry getEntry(final ItemStack stack)
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
    public boolean isStone(final ItemStack stack)
    {
        if (stack.getItem() instanceof IMegaCapability) return ((IMegaCapability) stack.getItem()).isStone(stack);
        return PokecubeItems.is(PokecubeItems.HELDKEY, stack) && stack.getItem().getRegistryName().getPath().contains(
                "mega");
    }

    @Override
    public boolean isValid(final ItemStack stack, final PokedexEntry entry)
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

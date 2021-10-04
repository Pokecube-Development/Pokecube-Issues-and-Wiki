package pokecube.core.items.megastuff;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.CapHolders;
import thut.api.item.ItemList;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.PlayerWearables;

public class MegaCapability implements ICapabilityProvider, IMegaCapability
{
    public static final ResourceLocation MEGAWORNTAG = new ResourceLocation(PokecubeCore.MODID, "mega_wearables");
    public static final ResourceLocation MEGASTONES  = new ResourceLocation(PokecubeCore.MODID, "megastones");
    public static final ResourceLocation BLINGTAG    = new ResourceLocation("thut_bling", "bling");

    public static interface RingChecker
    {
        boolean canMegaEvolve(LivingEntity player, PokedexEntry toEvolve);
    }

    public static RingChecker checker = (player, toEvolve) ->
    {
        final PlayerWearables worn = ThutWearables.getWearables(player);
        for (final ItemStack stack : worn.getWearables())
            if (MegaCapability.matches(stack, toEvolve)) return true;
        return false;
    };

    public static boolean canMegaEvolve(final LivingEntity player, final IPokemob target)
    {
        final PokedexEntry entry = target.getPokedexEntry();
        return MegaCapability.checker.canMegaEvolve(player, entry);
    }

    public static boolean matches(final ItemStack stack, final PokedexEntry entry)
    {
        final IMegaCapability cap = stack.getCapability(CapHolders.MEGA_CAP, null).orElse(null);
        if (cap != null) return cap.isValid(stack, entry);
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
        return MegaCapability.getForStack(stack);
    }

    @Override
    public boolean isStone(final ItemStack stack)
    {
        if (stack.getItem() instanceof IMegaCapability) return ((IMegaCapability) stack.getItem()).isStone(stack);
        return ItemList.is(MegaCapability.MEGASTONES, stack);
    }

    @Override
    public boolean isValid(final ItemStack stack, final PokedexEntry entry)
    {
        if (stack.getItem() instanceof IMegaCapability) return ((IMegaCapability) stack.getItem()).isValid(stack,
                entry);
        final PokedexEntry stacks = this.getEntry(stack);

        final boolean isStone = ItemList.is(MegaCapability.MEGASTONES, stack);
        final boolean isMegaWear = ItemList.is(MegaCapability.MEGAWORNTAG, stack);
        final boolean isBling = ItemList.is(MegaCapability.BLINGTAG, stack);

        // Bling only works if a stone is attached, so if it is bling, check if
        // it has correct entry.
        if (isStone || isBling)
        {
            if (stacks == null) return false;

            final PokedexEntry stackbase = stacks.getBaseForme() == null ? stacks : stacks.getBaseForme();
            final PokedexEntry entrybase = entry.getBaseForme() == null ? entry : entry.getBaseForme();
            return entrybase == stackbase;
        }
        // All normal mega wearables are valid at all times
        if (isMegaWear) return true;
        return false;
    }

    public static PokedexEntry getForStack(final ItemStack stack)
    {
        final boolean isMegaWear = ItemList.is(MegaCapability.MEGAWORNTAG, stack);
        if (isMegaWear) return Database.missingno;
        final boolean isStone = ItemList.is(MegaCapability.MEGASTONES, stack);
        if (isStone)
        {
            PokedexEntry e = Database.getEntry(stack.getItem().getRegistryName().getPath());
            if (e == null) e = Database.missingno;
            return e;
        }
        final boolean isBling = ItemList.is(MegaCapability.BLINGTAG, stack);
        if (isBling && stack.hasTag() && stack.getTag().contains("gemTag"))
        {
            final ItemStack stack2 = ItemStack.of(stack.getTagElement("gemTag"));
            if (!stack2.isEmpty()) return MegaCapability.getForStack(stack2);
        }
        return Database.missingno;
    }

    public static boolean isStoneOrWearable(final ItemStack object)
    {
        final boolean isStone = ItemList.is(MegaCapability.MEGASTONES, object);
        final boolean isMegaWear = ItemList.is(MegaCapability.MEGAWORNTAG, object);
        final boolean isBling = ItemList.is(MegaCapability.BLINGTAG, object);
        return isStone || isMegaWear || isBling;
    }
}

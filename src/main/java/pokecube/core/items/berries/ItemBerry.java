package pokecube.core.items.berries;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.berries.BerryGenManager;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Nature;
import pokecube.core.items.UsableItemEffects.BerryUsable.BerryEffect;

/**
 * @author Oracion
 * @author Manchou
 */
public class ItemBerry extends Item implements IMoveConstants, IPlantable
{
    public static class BerryType extends Properties
    {
        public final int         index;
        public final int[]       flavours;
        public final String      name;
        public final BerryEffect effect;

        public BerryType(final String name, final BerryEffect effect, final int index, final int... flavours)
        {
            this.name = name;
            this.effect = effect;
            this.index = index;
            this.flavours = flavours;
            if (BerryManager.berryItems.containsKey(index))
            {
                PokecubeCore.LOGGER.error("Duplicate Berry Index for " + index, new IllegalStateException());
                return;
            }
            this.group(PokecubeItems.POKECUBEBERRIES);
            final ItemBerry berry = new ItemBerry(this);
            BerryManager.berryItems.put(index, berry);
            if (index == 0) PokecubeItems.POKECUBE_BERRIES = new ItemStack(berry);
        }

    }

    public final BerryType type;

    public ItemBerry(final BerryType type)
    {
        super(type);
        this.type = type;
        BerryManager.addBerry(this);
    }

    /**
     * allows items to add custom lines of information to the mouseover
     * description
     */
    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, @Nullable final World playerIn,
            final List<ITextComponent> tooltip, final ITooltipFlag advanced)
    {
        ITextComponent info = null;
        if (advanced.isAdvanced()) tooltip.add(new StringTextComponent("ID: " + this.type.index));
        tooltip.add(new TranslationTextComponent("item.pokecube.berry.desc"));
        final String berryName = this.type.name;
        info = new TranslationTextComponent("item.pokecube.berry_" + berryName + ".desc");
        tooltip.add(info);
        if (BerryGenManager.trees.containsKey(this.type.index))
        {
            info = new TranslationTextComponent("item.berry.istree.desc");
            tooltip.add(info);
        }
        if (PokecubeCore.proxy.getPlayer() == null) return;
        if (PokecubeCore.proxy.getPlayer().openContainer instanceof ContainerPokemob)
        {
            final ContainerPokemob container = (ContainerPokemob) PokecubeCore.proxy.getPlayer().openContainer;
            final IPokemob pokemob = container.getPokemob();
            if (pokemob == null || pokemob.getEntity() == null) return;
            final Nature nature = pokemob.getNature();
            final int fav = Nature.getFavouriteBerryIndex(nature);
            if (fav == this.type.index)
            {
                info = new TranslationTextComponent("item.berry.favourite.desc", pokemob.getDisplayName()
                        .getFormattedText());
                tooltip.add(info);
                info = null;
            }
            final int weight = Nature.getBerryWeight(this.type.index, nature);
            if (weight == 0) info = new TranslationTextComponent("item.berry.nomind.desc", pokemob.getDisplayName()
                    .getFormattedText());
            if (weight >= 10) info = new TranslationTextComponent("item.berry.like1.desc", pokemob.getDisplayName()
                    .getFormattedText());
            if (weight >= 20) info = new TranslationTextComponent("item.berry.like2.desc", pokemob.getDisplayName()
                    .getFormattedText());
            if (weight >= 30) info = new TranslationTextComponent("item.berry.like3.desc", pokemob.getDisplayName()
                    .getFormattedText());
            if (weight <= -10) info = new TranslationTextComponent("item.berry.hate1.desc", pokemob.getDisplayName()
                    .getFormattedText());
            if (weight <= -20) info = new TranslationTextComponent("item.berry.hate2.desc", pokemob.getDisplayName()
                    .getFormattedText());
            if (weight <= -30) info = new TranslationTextComponent("item.berry.hate3.desc", pokemob.getDisplayName()
                    .getFormattedText());
            if (info != null) tooltip.add(info);
        }
    }

    @Override
    public BlockState getPlant(final IBlockReader world, final BlockPos pos)
    {
        return BerryManager.getCrop(this).getDefaultState();
    }

    @Override
    public PlantType getPlantType(final IBlockReader world, final BlockPos pos)
    {
        return PlantType.Crop;
    }

    @Override
    public ActionResultType onItemUse(final ItemUseContext context)
    {
        final PlayerEntity playerIn = context.getPlayer();
        final World worldIn = context.getWorld();
        final BlockPos pos = context.getPos();
        final Hand hand = context.getHand();
        final Direction side = context.getFace();

        final ItemStack stack = playerIn.getHeldItem(hand);
        final BlockState state = worldIn.getBlockState(pos);
        if (side == Direction.UP && playerIn.canPlayerEdit(pos.offset(side), side, stack) && state.getBlock()
                .canSustainPlant(state, worldIn, pos, Direction.UP, this) && worldIn.isAirBlock(pos.up()))
        {
            worldIn.setBlockState(pos.up(), BerryManager.getCrop(this).getDefaultState());
            stack.split(1);
        }
        return ActionResultType.SUCCESS;
    }
}

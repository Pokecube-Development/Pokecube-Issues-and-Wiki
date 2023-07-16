package pokecube.core.items.berries;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.Nature;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.berries.BerryGenManager;
import pokecube.core.inventory.pokemob.PokemobContainer;
import pokecube.core.items.UsableItemEffects.BerryUsable.BerryEffect;
import thut.lib.TComponent;

/**
 * @author Oracion
 * @author Manchou
 */
public class ItemBerry extends BlockItem implements IMoveConstants, IPlantable
{
    public static class BerryType extends Properties
    {
        public final int index;
        public final int[] flavours;
        public final String name;
        public final BerryEffect effect;

        public BerryType(final String name, final BerryEffect effect, final int index, final int... flavours)
        {
            this.name = name;
            this.effect = effect;
            this.index = index;
            this.flavours = flavours;
            this.tab(PokecubeItems.TAB_BERRIES);
        }
    }

    public static void registerBerryType(final String name, final BerryEffect effect, final int index,
            final int... flavours)
    {
        if (BerryManager.berryTypes.containsKey(index))
        {
            PokecubeAPI.LOGGER.error("Duplicate Berry Index for " + index, new IllegalStateException());
            return;
        }
        BerryType type = new BerryType(name, effect, index, flavours);
        BerryManager.berryNames.put(type.index, type.name);
        BerryManager.berryTypes.put(type.index, type);
        BerryManager.indexByName.put(type.name, type.index);
    }

    public final BerryType type;

    public ItemBerry(final BerryType type)
    {
        super(BerryManager.berryCrops.get(type.index).get(), type);
        this.type = type;
    }

    @Override
    public Block getBlock()
    {
        return this.getBlockRaw() == null ? null : this.getBlockRaw().delegate.get();
    }

    public Block getBlockRaw()
    {
        return BerryManager.berryCrops.get(this.type.index).get();
    }

    /**
     * allows items to add custom lines of information to the mouseover
     * description
     */
    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(final ItemStack stack, @Nullable final Level playerIn, final List<Component> tooltip,
            final TooltipFlag advanced)
    {
        Component info = null;
        tooltip.add(TComponent.translatable("item.pokecube.berry.desc"));
        final String berryName = this.type.name;
        info = TComponent.translatable("item.pokecube.berry_" + berryName + ".desc");
        tooltip.add(info);
        if (BerryGenManager.isTree(this.type.index))
        {
            info = TComponent.translatable("item.berry.istree.desc");
            tooltip.add(info);
        }
        if (PokecubeCore.proxy.getPlayer() == null) return;
        if (PokecubeCore.proxy.getPlayer().containerMenu instanceof PokemobContainer container)
        {
            final IPokemob pokemob = container.getPokemob();
            if (pokemob == null || pokemob.getEntity() == null) return;
            final Nature nature = pokemob.getNature();
            final int fav = Nature.getFavouriteBerryIndex(nature);
            if (fav == this.type.index)
            {
                final String tooltips = I18n.get("item.berry.favourite.desc", ChatFormatting.GOLD, ChatFormatting.RESET,
                        pokemob.getDisplayName().getString());
                info = TComponent.translatable(tooltips);
                tooltip.add(info);
                info = null;
            }
            final int weight = Nature.getBerryWeight(this.type.index, nature);
            String tooltips = I18n.get("item.berry.nomind.desc", ChatFormatting.YELLOW, ChatFormatting.RESET,
                    pokemob.getDisplayName().getString());
            if (weight == 0) info = TComponent.translatable(tooltips);

            tooltips = I18n.get("item.berry.like1.desc", ChatFormatting.GREEN, ChatFormatting.RESET,
                    pokemob.getDisplayName().getString());
            if (weight >= 10) info = TComponent.translatable(tooltips);

            tooltips = I18n.get("item.berry.like2.desc", ChatFormatting.DARK_GREEN, ChatFormatting.RESET,
                    pokemob.getDisplayName().getString());
            if (weight >= 20) info = TComponent.translatable(tooltips);

            tooltips = I18n.get("item.berry.like3.desc", ChatFormatting.DARK_GREEN, ChatFormatting.RESET,
                    pokemob.getDisplayName().getString());
            if (weight >= 30) info = TComponent.translatable(tooltips);

            tooltips = I18n.get("item.berry.hate1.desc", ChatFormatting.RED, ChatFormatting.RESET,
                    pokemob.getDisplayName().getString());
            if (weight <= -10) info = TComponent.translatable(tooltips);

            tooltips = I18n.get("item.berry.hate2.desc", ChatFormatting.RED, ChatFormatting.RESET,
                    pokemob.getDisplayName().getString());
            if (weight <= -20) info = TComponent.translatable(tooltips);

            tooltips = I18n.get("item.berry.hate3.desc", ChatFormatting.DARK_RED, ChatFormatting.RESET,
                    pokemob.getDisplayName().getString());
            if (weight <= -30) info = TComponent.translatable(tooltips);

            if (info != null) tooltip.add(info);
        }

        if (advanced.isAdvanced()) tooltip.add(TComponent.literal("ID: " + this.type.index).withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public BlockState getPlant(final BlockGetter world, final BlockPos pos)
    {
        return BerryManager.getCrop(this).defaultBlockState();
    }

    @Override
    public PlantType getPlantType(final BlockGetter world, final BlockPos pos)
    {
        return PlantType.CROP;
    }

    @Override
    public InteractionResult useOn(final UseOnContext context)
    {
        final Player playerIn = context.getPlayer();
        final Level worldIn = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        final InteractionHand hand = context.getHand();
        final Direction side = context.getClickedFace();

        final ItemStack stack = playerIn.getItemInHand(hand);
        final BlockState state = worldIn.getBlockState(pos);
        final Block block = state.getBlock();
        if (side == Direction.UP && playerIn.mayUseItemAt(pos.relative(side), side, stack)
                && block.canSustainPlant(state, worldIn, pos, Direction.UP, this) && worldIn.isEmptyBlock(pos.above()))
        {
            worldIn.setBlockAndUpdate(pos.above(), BerryManager.getCrop(this).defaultBlockState());
            stack.split(1);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }
}

package pokecube.core.items.berries;

import java.util.List;

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
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.util.TriState;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.Nature;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.berries.BerryGenManager;
import pokecube.core.inventory.pokemob.PokemobContainer;
import pokecube.core.items.UsableItemEffects.BerryUsable.BerryEffect;
import pokecube.core.items.berries.BerryManager.BerryType;

/**
 * @author Oracion
 * @author Manchou
 */
public class ItemBerry extends BlockItem implements IMoveConstants
{
    public static void registerBerryType(final String name, final BerryEffect effect, final int index,
            final int... flavours)
    {
        if (BerryManager.berryTypes.containsKey(index))
        {
            PokecubeAPI.LOGGER.error("Duplicate Berry Index for {}", index, new IllegalStateException());
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
        return this.getBlockRaw() == null ? null : this.getBlockRaw();
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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
            TooltipFlag tooltipFlag)
    {
        Component info;
        tooltipComponents.add(Component.translatable("item.pokecube.berry.desc"));
        final String berryName = this.type.name;
        info = Component.translatable("item.pokecube.berry_" + berryName + ".desc");
        tooltipComponents.add(info);
        if (BerryGenManager.isTree(this.type.index))
        {
            info = Component.translatable("item.berry.istree.desc");
            tooltipComponents.add(info);
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
                info = Component.translatable(tooltips);
                tooltipComponents.add(info);
                info = null;
            }
            final int weight = Nature.getBerryWeight(this.type.index, nature);
            String tooltips = I18n.get("item.berry.nomind.desc", ChatFormatting.YELLOW, ChatFormatting.RESET,
                    pokemob.getDisplayName().getString());
            if (weight == 0) info = Component.translatable(tooltips);

            tooltips = I18n.get("item.berry.like1.desc", ChatFormatting.GREEN, ChatFormatting.RESET,
                    pokemob.getDisplayName().getString());
            if (weight >= 10) info = Component.translatable(tooltips);

            tooltips = I18n.get("item.berry.like2.desc", ChatFormatting.DARK_GREEN, ChatFormatting.RESET,
                    pokemob.getDisplayName().getString());
            if (weight >= 20) info = Component.translatable(tooltips);

            tooltips = I18n.get("item.berry.like3.desc", ChatFormatting.DARK_GREEN, ChatFormatting.RESET,
                    pokemob.getDisplayName().getString());
            if (weight >= 30) info = Component.translatable(tooltips);

            tooltips = I18n.get("item.berry.hate1.desc", ChatFormatting.RED, ChatFormatting.RESET,
                    pokemob.getDisplayName().getString());
            if (weight <= -10) info = Component.translatable(tooltips);

            tooltips = I18n.get("item.berry.hate2.desc", ChatFormatting.RED, ChatFormatting.RESET,
                    pokemob.getDisplayName().getString());
            if (weight <= -20) info = Component.translatable(tooltips);

            tooltips = I18n.get("item.berry.hate3.desc", ChatFormatting.DARK_RED, ChatFormatting.RESET,
                    pokemob.getDisplayName().getString());
            if (weight <= -30) info = Component.translatable(tooltips);

            if (info != null) tooltipComponents.add(info);
        }

        if (tooltipFlag.isAdvanced())
            tooltipComponents.add(Component.literal("ID: " + this.type.index).withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    protected BlockState getPlacementState(BlockPlaceContext context)
    {
        BlockState state = BerryManager.getCrop(this).defaultBlockState();
        return state != null && this.canPlace(context, state) ? state : null;
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

        // Only on top
        if (side != Direction.UP) return InteractionResult.FAIL;

        // Only if empty
        if (!worldIn.isEmptyBlock(pos.above())) return InteractionResult.FAIL;

        // Only if we can place it
        if (!playerIn.mayUseItemAt(pos.relative(side), side, stack)) return InteractionResult.FAIL;

	// Is this placeable on the location
        TriState placeable = block.canSustainPlant(state, worldIn, pos, Direction.UP,
                this.getPlacementState(new BlockPlaceContext(context)));

        // Does this survive at the location
        BlockState cropState = BerryManager.getCrop(this).defaultBlockState();
        BlockPos plantPos = pos.above();

        if (placeable != TriState.FALSE && cropState.canSurvive(worldIn, plantPos))
        {
            worldIn.setBlockAndUpdate(plantPos, cropState);
            stack.split(1);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }
}

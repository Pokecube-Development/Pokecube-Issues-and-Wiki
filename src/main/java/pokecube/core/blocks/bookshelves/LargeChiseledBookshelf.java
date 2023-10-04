package pokecube.core.blocks.bookshelves;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.core.handlers.ModTags;

public class LargeChiseledBookshelf extends ChiseledBookShelfBlock
{
    private static final int MAX_BOOKS_IN_STORAGE = 12;
    public static final int BOOKS_PER_ROW = 3;
    public static final BooleanProperty SLOT_6 = BooleanProperty.create("slot_6_occupied");
    public static final BooleanProperty SLOT_7 = BooleanProperty.create("slot_7_occupied");
    public static final BooleanProperty SLOT_8 = BooleanProperty.create("slot_8_occupied");
    public static final BooleanProperty SLOT_9 = BooleanProperty.create("slot_9_occupied");
    public static final BooleanProperty SLOT_10 = BooleanProperty.create("slot_10_occupied");
    public static final BooleanProperty SLOT_11 = BooleanProperty.create("slot_11_occupied");
    public static final List<BooleanProperty> SLOT_OCCUPIED_PROPERTIES =
            List.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_0_OCCUPIED, BlockStateProperties.CHISELED_BOOKSHELF_SLOT_1_OCCUPIED,
                    BlockStateProperties.CHISELED_BOOKSHELF_SLOT_2_OCCUPIED, BlockStateProperties.CHISELED_BOOKSHELF_SLOT_3_OCCUPIED,
                    BlockStateProperties.CHISELED_BOOKSHELF_SLOT_4_OCCUPIED, BlockStateProperties.CHISELED_BOOKSHELF_SLOT_5_OCCUPIED,
                    SLOT_6, SLOT_7, SLOT_8, SLOT_9, SLOT_10, SLOT_11);

    public LargeChiseledBookshelf(final Properties props)
    {
        super(props);
        BlockState state = this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
                .setValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_0_OCCUPIED, Boolean.FALSE)
                .setValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_1_OCCUPIED, Boolean.FALSE)
                .setValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_2_OCCUPIED, Boolean.FALSE)
                .setValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_3_OCCUPIED, Boolean.FALSE)
                .setValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_4_OCCUPIED, Boolean.FALSE)
                .setValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_5_OCCUPIED, Boolean.FALSE)
                .setValue(SLOT_6, Boolean.FALSE).setValue(SLOT_7, Boolean.FALSE)
                .setValue(SLOT_8, Boolean.FALSE).setValue(SLOT_9, Boolean.FALSE)
                .setValue(SLOT_10, Boolean.FALSE).setValue(SLOT_11, Boolean.FALSE);
        this.registerDefaultState(state);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(HorizontalDirectionalBlock.FACING, BlockStateProperties.CHISELED_BOOKSHELF_SLOT_0_OCCUPIED,
                BlockStateProperties.CHISELED_BOOKSHELF_SLOT_1_OCCUPIED, BlockStateProperties.CHISELED_BOOKSHELF_SLOT_2_OCCUPIED,
                BlockStateProperties.CHISELED_BOOKSHELF_SLOT_3_OCCUPIED, BlockStateProperties.CHISELED_BOOKSHELF_SLOT_4_OCCUPIED,
                BlockStateProperties.CHISELED_BOOKSHELF_SLOT_5_OCCUPIED, SLOT_6,
                SLOT_7, SLOT_8, SLOT_9, SLOT_10, SLOT_11);
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state)
    {
        return new LargeChiseledBookshelfBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {

        boolean filled = false;
        if (context.getItemInHand().getOrCreateTagElement("BlockEntityTag").contains("LootTable")) filled = true;

        return this.defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite())
                .setValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_0_OCCUPIED, filled)
                .setValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_1_OCCUPIED, filled)
                .setValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_2_OCCUPIED, filled)
                .setValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_3_OCCUPIED, filled)
                .setValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_4_OCCUPIED, filled)
                .setValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_5_OCCUPIED, filled)
                .setValue(SLOT_6, filled).setValue(SLOT_7, filled)
                .setValue(SLOT_8, filled).setValue(SLOT_9, filled)
                .setValue(SLOT_10, filled).setValue(SLOT_11, filled);
    }

    @Override
    public void setPlacedBy(final Level world, final BlockPos pos, final BlockState state,
            @Nullable final LivingEntity entity, final ItemStack stack)
    {
        final BlockEntity tile = world.getBlockEntity(pos);
        if (stack.hasCustomHoverName())
            if (tile instanceof LargeChiseledBookshelfBlockEntity shelf) shelf.setCustomName(stack.getHoverName());
    }

    @Deprecated
    @Override
    public BlockState rotate(final BlockState state, final Rotation rot)
    {
        return state.setValue(HorizontalDirectionalBlock.FACING, rot.rotate(state.getValue(HorizontalDirectionalBlock.FACING)));
    }

    @Deprecated
    @Override
    public BlockState mirror(final BlockState state, final Mirror mirrorIn)
    {
        return state.rotate(mirrorIn.getRotation(state.getValue(HorizontalDirectionalBlock.FACING)));
    }

    @Override
    public boolean hasAnalogOutputSignal(final BlockState state)
    {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(final BlockState state, final Level world, final BlockPos pos)
    {
        if (world.isClientSide()) {
            return 0;
        } else {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof LargeChiseledBookshelfBlockEntity) {
                LargeChiseledBookshelfBlockEntity shelfBlockEntity = (LargeChiseledBookshelfBlockEntity)blockEntity;
                return shelfBlockEntity.getLastInteractedSlot() + 1;
            } else {
                return 0;
            }
        }
    }

//    @Override
//    public float getEnchantPowerBonus(final BlockState state, final LevelReader world, final BlockPos pos)
//    {
//        final int books = this.getBooks(state);
//        return books / 3f;
//    }

    private static int getHitSlot(Vec2 vec2) {
        int i = vec2.y >= 0.5F ? 0 : 1;
        int j = getSection(vec2.x);
        return j + i * 3;
    }

    private static int getSection(float v) {
        float f = 0.0625F;
        float f1 = 0.375F;
        if (v < 0.375F) {
            return 0;
        } else {
            float f2 = 0.6875F;
            return v < 0.6875F ? 1 : 2;
        }
    }

    @Override
    public InteractionResult use(final BlockState state, final Level world, final BlockPos pos, final Player player,
            final InteractionHand hand, final BlockHitResult hit)
    {
        final BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof LargeChiseledBookshelfBlockEntity shelf && !player.isShiftKeyDown())
        {
            Optional<Vec2> optional = getRelativeHitCoordinatesForBlockFace(hit, state.getValue(HorizontalDirectionalBlock.FACING));
            Optional<Vec2> optional2 = getHitCoordinatesForBlockFace(hit, state.getValue(HorizontalDirectionalBlock.FACING));
//            if (optional.isEmpty() && optional2.isEmpty())
//            {
//                return InteractionResult.PASS;
//            }
            if (optional.isPresent()) {
                int i = getHitSlot(optional.get());
                if (state.getValue(SLOT_OCCUPIED_PROPERTIES.get(i)))
                {
                    removeBook(world, pos, player, shelf, i);
                    return InteractionResult.sidedSuccess(world.isClientSide);
                } else {
                    ItemStack stack = player.getItemInHand(hand);
                    if ((stack.is(ItemTags.BOOKSHELF_BOOKS) || stack.is(ModTags.BOOKS) ||
                            stack.is(ModTags.BOOKSHELF_ITEMS)) && !state.getValue(SLOT_OCCUPIED_PROPERTIES.get(i)))
                    {
                        addBook(world, pos, player, shelf, stack, i);
                        return InteractionResult.sidedSuccess(world.isClientSide);
                    } else {
                        return InteractionResult.CONSUME;
                    }
                }
            }
            if (optional2.isPresent()) {
                int i2 = getHitSlot(optional2.get());
                if (state.getValue(SLOT_OCCUPIED_PROPERTIES.get(i2 + 6)))
                {
                    removeBook(world, pos, player, shelf, i2 + 6);
                    return InteractionResult.sidedSuccess(world.isClientSide);
                } else {
                    ItemStack stack = player.getItemInHand(hand);
                    if ((stack.is(ItemTags.BOOKSHELF_BOOKS) || stack.is(ModTags.BOOKS) ||
                            stack.is(ModTags.BOOKSHELF_ITEMS)) && !state.getValue(SLOT_OCCUPIED_PROPERTIES.get(i2 + 6)))
                    {
                        addBook(world, pos, player, shelf, stack, i2 + 6);
                        return InteractionResult.sidedSuccess(world.isClientSide);
                    } else {
                        return InteractionResult.CONSUME;
                    }
                }
            }
        }
        if (world.isClientSide) return InteractionResult.SUCCESS;
        else if (tile instanceof LargeChiseledBookshelfBlockEntity shelf && player.isShiftKeyDown())
        {
            player.openMenu(shelf);
            PiglinAi.angerNearbyPiglins(player, true);
        }
        return InteractionResult.PASS;
    }

    private static Optional<Vec2> getHitCoordinatesForBlockFace(BlockHitResult hitResult, Direction direction)
    {
        Direction hitDirection = hitResult.getDirection().getOpposite();
        if (direction != hitDirection)
        {
            return Optional.empty();
        } else {
            BlockPos posRelative = hitResult.getBlockPos().relative(hitDirection);
            Vec3 vec3 = hitResult.getLocation().subtract(posRelative.getX(), posRelative.getY(), posRelative.getZ());
            double d0 = vec3.x();
            double d1 = vec3.y();
            double d2 = vec3.z();
            Optional optional;
            switch (hitDirection) {
                case NORTH:
                    optional = Optional.of(new Vec2((float)d0, (float)d1));
                    break;
                case SOUTH:
                    optional = Optional.of(new Vec2((float)(1.0D - d0), (float)d1));
                    break;
                case WEST:
                    optional = Optional.of(new Vec2((float)(1.0D - d2), (float)d1));
                    break;
                case EAST:
                    optional = Optional.of(new Vec2((float)d2, (float)d1));
                    break;
                case DOWN:
                case UP:
                    optional = Optional.empty();
                    break;
                default:
                    throw new IncompatibleClassChangeError();
            }

            return optional;
        }
    }

    private static void addBook(Level world, BlockPos pos, Player player, LargeChiseledBookshelfBlockEntity tile, ItemStack stack, int i)
    {
        if (!world.isClientSide)
        {
            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            SoundEvent soundevent = stack.is(Items.ENCHANTED_BOOK) ? SoundEvents.CHISELED_BOOKSHELF_INSERT_ENCHANTED :
                    SoundEvents.CHISELED_BOOKSHELF_INSERT;
            tile.setItem(i, stack.split(1));
            world.playSound(null, pos, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (player.isCreative())
            {
                stack.grow(1);
            }

            world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        }
    }

    private static void removeBook(Level world, BlockPos pos, Player player, LargeChiseledBookshelfBlockEntity tile, int i)
    {
        if (!world.isClientSide)
        {
            ItemStack itemstack = tile.removeItem(i, 1);
            SoundEvent soundevent = itemstack.is(Items.ENCHANTED_BOOK) ? SoundEvents.CHISELED_BOOKSHELF_PICKUP_ENCHANTED :
                    SoundEvents.CHISELED_BOOKSHELF_PICKUP;
            world.playSound(null, pos, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!player.getInventory().add(itemstack))
            {
                player.drop(itemstack, false);
            }

            world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        }
    }

    @Override
    public RenderShape getRenderShape(final BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public float getShadeBrightness(final BlockState state, final BlockGetter world, final BlockPos pos)
    {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(final BlockState state, final BlockGetter reader, final BlockPos pos)
    {
        return false;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState state1, boolean requiresCorrectToolForDrops) {
        if (!state.is(state1.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof LargeChiseledBookshelfBlockEntity bookshelfBlockEntity) {
                if (!bookshelfBlockEntity.isEmpty()) {
                    for(int i = 0; i < MAX_BOOKS_IN_STORAGE; ++i) {
                        ItemStack itemstack = bookshelfBlockEntity.getItem(i);
                        if (!itemstack.isEmpty()) {
                            Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), itemstack);
                        }
                    }

                    bookshelfBlockEntity.clearContent();
                    world.updateNeighbourForOutputSignal(pos, this);
                }
            }

            super.onRemove(state, world, pos, state1, requiresCorrectToolForDrops);
        }
    }
}
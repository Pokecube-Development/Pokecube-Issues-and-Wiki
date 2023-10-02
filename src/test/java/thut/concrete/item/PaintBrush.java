package thut.concrete.item;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import thut.api.block.IDyedBlock;
import thut.api.block.flowing.IFlowingBlock;
import thut.concrete.Concrete;

public class PaintBrush extends BrushItem
{
    private static final double MAX_BRUSH_DISTANCE = Math.sqrt(ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE) - 1.0D;
    private final DyeColor colour;
    public static final Map<Block, IDyedBlock> PAINTABLE_BLOCKS = Maps.newHashMap();

    public static class ManualPaintable implements IDyedBlock
    {
        Block ours;
        DyeColor colour;
        Map<DyeColor, Block> variants;

        public ManualPaintable(Block ours, DyeColor colour, Map<DyeColor, Block> variants)
        {
            this.variants = variants;
            this.colour = colour;
            this.ours = ours;
        }

        @Override
        public DyeColor getColour()
        {
            return colour;
        }

        @Override
        public Block getFor(DyeColor c)
        {
            if (c == colour) return ours;
            return variants.get(c);
        }

    }

    public PaintBrush(Properties properties, DyeColor colour)
    {
        super(properties);
        this.colour = colour;
    }

    public static void registerPaintable(Block block, IDyedBlock paintable)
    {
        PAINTABLE_BLOCKS.put(block, paintable);
    }

    private static void registerVanillalike(Function<DyeColor, String> name_lookup)
    {
        Map<DyeColor, Block> variants = Maps.newHashMap();
        for (DyeColor color : DyeColor.values())
        {
            String name = name_lookup.apply(color);
            @SuppressWarnings("deprecation")
            Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(name));
            variants.put(color, block);
        }
        for (var pair : variants.entrySet())
            registerPaintable(pair.getValue(), new ManualPaintable(pair.getValue(), pair.getKey(), variants));
    }

    static
    {
        for (final String s : Concrete.config.dyeable_blocks)
        {
            registerVanillalike(colour -> s.replace("red", colour.getName()));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Player player = context.getPlayer();
        HitResult hitresult = this.calculateHitResult(player);
        if (player != null && (hitresult.getType() == HitResult.Type.BLOCK || hitresult.getType() == HitResult.Type.ENTITY)) {
            player.startUsingItem(context.getHand());
        }
        return InteractionResult.CONSUME;
//        TODO ?
//        if (world instanceof ServerLevel)
//        {
//            ExplosionCustom boom = new ExplosionCustom(world, null, Vector3.getNewVector().set(pos), 200);
//            boom.breaker = new ChamberBoom(4);
//            boom.doExplosion();
//        }
    }

    @Override
    public void onUseTick(Level world, LivingEntity entity, ItemStack stack, int ticks) {
        if (ticks >= 0 && entity instanceof Player player)
        {
            HitResult hitresult = this.calculateHitResult(entity);
            if (hitresult instanceof BlockHitResult hitResult)
            {
                if (hitresult.getType() == HitResult.Type.BLOCK || hitresult.getType() == HitResult.Type.ENTITY)
                {
                    int i = this.getUseDuration(stack) - ticks + 1;
                    boolean flag = i % 4 == 2;
                    if (flag)
                    {
                        BlockPos pos = hitResult.getBlockPos();
                        BlockState state = world.getBlockState(pos);
                        HumanoidArm arm = entity.getUsedItemHand() == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
                        this.spawnDustParticles(world, hitResult, state, entity.getViewVector(0.0F), arm);
                        Block block = state.getBlock();
                        SoundEvent soundevent;
                        if (block instanceof BrushableBlock) {
                            BrushableBlock brushableblock = (BrushableBlock)block;
                            soundevent = brushableblock.getBrushSound();
                        } else {
                            soundevent = SoundEvents.BRUSH_GENERIC;
                        }
                        world.playSound(player, pos, soundevent, SoundSource.BLOCKS);

                        IDyedBlock dyeable = null;

                        if (state.getBlock() instanceof IDyedBlock dyed) dyeable = dyed;
                        else dyeable = PAINTABLE_BLOCKS.get(state.getBlock());
                        if (dyeable != null)
                        {
                            if (colour != dyeable.getColour() && dyeable.getFor(colour) != null)
                            {
                                BlockState painted = IFlowingBlock.copyValidTo(state, dyeable.getFor(colour).defaultBlockState());
                                if (painted != null && !(painted.getBlock() instanceof AirBlock))
                                {
                                    if (painted.hasBlockEntity())
                                    {
                                        BlockEntity blockEntity = world.getBlockEntity(pos);
                                        if (blockEntity != null && !world.isClientSide)
                                        {
                                            CompoundTag tag = blockEntity.saveWithFullMetadata();
                                            world.setBlockEntity(blockEntity);
                                            world.setBlock(pos, painted, 3);
                                        }
                                    }
                                    if (!world.isClientSide)
                                        world.setBlock(pos, painted, 3);
                                }
                                if (player instanceof ServerPlayer serverPlayer && !serverPlayer.isCreative())
                                {
                                    boolean broke = stack.hurt(1, player.getRandom(), serverPlayer);
                                    if (broke) stack = new ItemStack(Concrete.BRUSHES[16].get());
                                    player.setItemInHand(serverPlayer.getUsedItemHand(), stack);
                                }
                            }
                        }
                    }
                    return;
                }
            }
            entity.releaseUsingItem();
        } else {
            entity.releaseUsingItem();
        }
    }

    public HitResult calculateHitResult(LivingEntity entity) {
        return ProjectileUtil.getHitResultOnViewVector(entity, (player) -> {
            return !player.isSpectator() && player.isPickable();
        }, MAX_BRUSH_DISTANCE);
    }
}

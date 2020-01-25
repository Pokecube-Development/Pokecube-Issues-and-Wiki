package pokecube.legends.blocks;

import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.DimensionManager;
import pokecube.legends.init.function.WormHoleActiveFunction;
import thut.api.maths.Vector3;
import thut.core.common.entity.Transporter;

public class UltraSpacePortal extends Rotates
{
    public UltraSpacePortal(final String name, final Properties props)
    {
        super(name, props.tickRandomly());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(final ItemStack stack, final IBlockReader worldIn, final List<ITextComponent> tooltip,
            final ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        String message;
        if (Screen.hasShiftDown()) message = I18n.format("legends.ultraportal.tooltip");
        else message = I18n.format("pokecube.tooltip.advanced");
        tooltip.add(new TranslationTextComponent(message));
    }

    // Time for Despawn
    @Override
    public int tickRate(final IWorldReader world)
    {
        return 700;
    }

    @Override
    public void onBlockAdded(final BlockState state, final World worldIn, final BlockPos pos, final BlockState oldState,
            final boolean isMoving)
    {
        // TODO Auto-generated method stub
        // final int x = pos.getX();
        // final int y = pos.getY();
        // final int z = pos.getZ();
        // worldIn.getPendingBlockTicks().scheduleUpdate(new BlockPos(x, y, z),
        // this, this.tickRate(world));
    }

    @Override
    public void randomTick(final BlockState state, final World worldIn, final BlockPos pos, final Random random)
    {
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();

        final java.util.HashMap<String, Object> dependencies = new java.util.HashMap<>();
        dependencies.put("x", x);
        dependencies.put("y", y);
        dependencies.put("z", z);
        dependencies.put("world", worldIn);
        WormHoleActiveFunction.executeProcedure(dependencies);

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(final BlockState stateIn, final World world, final BlockPos pos, final Random random)
    {
        Minecraft.getInstance();
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();
        final int i = x;
        final int j = y;
        final int k = z;
        if (true) for (int l = 0; l < 1; ++l)
        {
            final double d0 = i + random.nextFloat();
            final double d1 = j + random.nextFloat();
            final double d2 = k + random.nextFloat();
            random.nextInt(2);
            final double d3 = (random.nextFloat() - 0.5D) * 0.5D;
            final double d4 = (random.nextFloat() - 0.5D) * 0.5D;
            final double d5 = (random.nextFloat() - 0.5D) * 0.5D;
            world.addParticle(ParticleTypes.CRIT, d0, d1, d2, d3, d4, d5);
        }
        if (random.nextInt(100) == 0) world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                SoundEvents.AMBIENT_CAVE, SoundCategory.BLOCKS, 0.5F, random.nextFloat() * 0.4F + 0.8F, false);
    }

    @Override
    public boolean onBlockActivated(final BlockState state, final World worldIn, final BlockPos pos,
            final PlayerEntity playerIn, final Hand handIn, final BlockRayTraceResult hit)
    {
        if (!playerIn.isPassenger() && !playerIn.isBeingRidden() && playerIn.isNonBoss()
                && playerIn instanceof ServerPlayerEntity)
        {
            final ServerPlayerEntity player = (ServerPlayerEntity) playerIn;
            final MinecraftServer minecraftServer = player.getServer();
            final DimensionType dimensionId = worldIn.getDimension().getType();
            DimensionType dimensionIn;

            if (player.timeUntilPortal > 0) player.timeUntilPortal = 5;
            else
            {
                // TODO ultraspace dim instead
                player.isInvulnerableDimensionChange();
                if (dimensionId == DimensionType.THE_NETHER) dimensionIn = DimensionType.OVERWORLD;
                else dimensionIn = DimensionType.THE_NETHER;

                final ServerWorld world1 = DimensionManager.getWorld(minecraftServer, dimensionIn, true, true);
                player.timeUntilPortal = 5;

                final BlockPos entityPos = player.getPosition();
                int destY = entityPos.getY();
                world1.getChunkAt(entityPos);
                // finds surface height for location.
                destY = world1.getHeight(Heightmap.Type.WORLD_SURFACE, entityPos.getX(), entityPos.getZ()) + 2;
                final Vector3 dest = Vector3.getNewVector().set(player);
                dest.y = destY;
                Transporter.teleportEntity(playerIn, dest, dimensionIn.getId(), true);
                return true;
            }
        }
        return false;
    }

}
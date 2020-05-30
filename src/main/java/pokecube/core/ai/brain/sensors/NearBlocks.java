package pokecube.core.ai.brain.sensors;

import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import thut.api.maths.Cruncher;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;

public class NearBlocks extends Sensor<LivingEntity>
{
    public static class NearBlock
    {
        private final BlockState state;
        private final BlockPos   pos;

        public NearBlock(final BlockState state, final BlockPos pos)
        {
            this.state = state;
            this.pos = pos;
        }

        public BlockPos getPos()
        {
            return this.pos;
        }

        public BlockState getState()
        {
            return this.state;
        }
    }

    private static final int[][] indexArr = new int[32 * 32 * 32][3];

    static
    {
        final Vector3 r = Vector3.getNewVector();
        for (int i = 0; i < NearBlocks.indexArr.length; i++)
        {
            Cruncher.indexToVals(i, r);
            if (Math.abs(r.y) <= 4)
            {
                NearBlocks.indexArr[i][0] = r.intX();
                NearBlocks.indexArr[i][1] = r.intY();
                NearBlocks.indexArr[i][2] = r.intZ();
            }
            else NearBlocks.indexArr[i] = null;
        }
    }

    int tick = 0;

    @Override
    protected void update(final ServerWorld worldIn, final LivingEntity entityIn)
    {
        if (BrainUtils.hasAttackTarget(entityIn)) return;
        if (BrainUtils.hasMoveUseTarget(entityIn)) return;

        this.tick++;
        if (this.tick % PokecubeCore.getConfig().nearBlockUpdateRate != 0) return;
        if (!TerrainManager.isAreaLoaded(entityIn.dimension, entityIn.getPosition(), PokecubeCore
                .getConfig().movementPauseThreshold)) return;

        final Vector3 r = Vector3.getNewVector(), rAbs = Vector3.getNewVector();
        final Vector3 origin = Vector3.getNewVector();
        origin.set(entityIn);
        final List<NearBlock> list = Lists.newArrayList();
        final int size = 8;

        final Vec3d start = entityIn.getEyePosition(1);

        final Predicate<BlockPos> visible = input ->
        {
            final Vec3d end = new Vec3d(input);
            final RayTraceContext context = new RayTraceContext(start, end, BlockMode.COLLIDER, FluidMode.NONE,
                    entityIn);
            final RayTraceResult result = worldIn.rayTraceBlocks(context);
            if (result.getType() == Type.MISS) return true;
            final BlockRayTraceResult hit = (BlockRayTraceResult) result;
            return hit.getPos().equals(input);
        };

        for (int i = 0; i < size * size * size; i++)
        {
            final int[] pos = NearBlocks.indexArr[i];
            if (pos == null) continue;
            r.set(pos);
            rAbs.set(r).addTo(origin);
            if (rAbs.isAir(worldIn)) continue;
            if (!visible.apply(rAbs.getPos())) continue;
            final BlockPos bpos = new BlockPos(rAbs.getPos());
            final BlockState state = worldIn.getBlockState(bpos);
            list.add(new NearBlock(state, bpos));
        }
        final BlockPos o0 = entityIn.getPosition();
        list.sort((o1, o2) -> (int) (o1.getPos().distanceSq(o0) - o1.getPos().distanceSq(o0)));
        final Brain<?> brain = entityIn.getBrain();
        if (!list.isEmpty()) brain.setMemory(MemoryModules.VISIBLE_BLOCKS, list);
        else brain.removeMemory(MemoryModules.VISIBLE_BLOCKS);
    }

    @Override
    public Set<MemoryModuleType<?>> getUsedMemories()
    {
        return ImmutableSet.of(MemoryModules.VISIBLE_BLOCKS);
    }

}

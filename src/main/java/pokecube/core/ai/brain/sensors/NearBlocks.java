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
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.utils.Tools;
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

    int tick = 0;

    private boolean tameCheck(final IPokemob pokemob)
    {
        return pokemob.getGeneralState(GeneralStates.STAYING) || PokecubeCore.getConfig().tameGather;
    }

    @Override
    protected void update(final ServerWorld worldIn, final LivingEntity entityIn)
    {
        if (BrainUtils.hasAttackTarget(entityIn)) return;
        if (BrainUtils.hasMoveUseTarget(entityIn)) return;
        this.tick++;
        if (this.tick % PokecubeCore.getConfig().nearBlockUpdateRate != 0) return;
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entityIn);
        final boolean gathering = pokemob != null && pokemob.isPlayerOwned() && pokemob.isRoutineEnabled(
                AIRoutine.GATHER) && this.tameCheck(pokemob);
        final int size = gathering ? 15 : 8;
        if (!TerrainManager.isAreaLoaded(worldIn, entityIn.getPosition(), size + 8)) return;

        final Vector3 r = Vector3.getNewVector(), rAbs = Vector3.getNewVector();
        final Vector3 origin = Vector3.getNewVector();
        origin.set(entityIn);
        final List<NearBlock> list = Lists.newArrayList();

        final Vec3d start = entityIn.getEyePosition(1);

        final Predicate<BlockPos> visible = input ->
        {
            final Vec3d end = new Vec3d(input).add(0.5, 0.5, 0.5);
            final RayTraceContext context = new RayTraceContext(start, end, BlockMode.COLLIDER, FluidMode.NONE,
                    entityIn);
            final RayTraceResult result = worldIn.rayTraceBlocks(context);
            if (result.getType() == Type.MISS) return true;
            final BlockRayTraceResult hit = (BlockRayTraceResult) result;
            return hit.getPos().equals(input);
        };

        for (int i = 0; i < size * size * size; i++)
        {
            final byte[] pos = Tools.indexArr[i];
            if (pos[1] > 4 || pos[1] < -4) continue;
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

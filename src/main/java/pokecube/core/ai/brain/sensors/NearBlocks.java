package pokecube.core.ai.brain.sensors;

import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
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
    protected void doTick(final ServerLevel worldIn, final LivingEntity entityIn)
    {
        try
        {
            if (BrainUtils.hasAttackTarget(entityIn)) return;
            if (BrainUtils.hasMoveUseTarget(entityIn)) return;
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            return;
        }
        this.tick++;
        if (this.tick % PokecubeCore.getConfig().nearBlockUpdateRate != 0) return;
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entityIn);
        final boolean gathering = pokemob != null && pokemob.isPlayerOwned() && pokemob.isRoutineEnabled(
                AIRoutine.GATHER) && this.tameCheck(pokemob);
        final int size = gathering ? 15 : 8;
        if (!TerrainManager.isAreaLoaded(entityIn.getLevel(), entityIn.blockPosition(), size + 8)) return;

        final Vector3 r = new Vector3(), rAbs = new Vector3();
        final Vector3 origin = new Vector3();
        origin.set(entityIn);
        final List<NearBlock> list = Lists.newArrayList();

        final Vec3 start = entityIn.getEyePosition(1);

        final Predicate<BlockPos> visible = input ->
        {
            final Vec3 end = new Vec3(input.getX() + 0.5, input.getY() + 0.5, input.getZ() + 0.5);
            final ClipContext context = new ClipContext(start, end, Block.COLLIDER, Fluid.NONE,
                    entityIn);
            final HitResult result = worldIn.clip(context);
            if (result.getType() == Type.MISS) return true;
            final BlockHitResult hit = (BlockHitResult) result;
            return hit.getBlockPos().equals(input);
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

        final BlockPos o0 = entityIn.blockPosition();
        list.sort((o1, o2) -> (int) (o1.getPos().distSqr(o0) - o1.getPos().distSqr(o0)));
        final Brain<?> brain = entityIn.getBrain();
        if (!list.isEmpty()) brain.setMemory(MemoryModules.VISIBLE_BLOCKS, list);
        else brain.eraseMemory(MemoryModules.VISIBLE_BLOCKS);
    }

    @Override
    public Set<MemoryModuleType<?>> requires()
    {
        return ImmutableSet.of(MemoryModules.VISIBLE_BLOCKS);
    }

}

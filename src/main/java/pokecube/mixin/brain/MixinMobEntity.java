package pokecube.mixin.brain;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import pokecube.adventures.ai.brain.MemoryTypes;
import thut.api.entity.ai.BrainUtil;
import thut.api.entity.ai.RootTask;

@Mixin(Mob.class)
public abstract class MixinMobEntity extends LivingEntity
{
    public static class DummySetTask extends RootTask<LivingEntity>
    {
        public DummySetTask()
        {
            super(ImmutableMap.of(MemoryTypes.DUMMY, MemoryStatus.REGISTERED));
        }

        @Override
        protected boolean checkExtraStartConditions(final ServerLevel worldIn, final LivingEntity owner)
        {
            final Brain<?> brain = owner.getBrain();
            brain.setMemory(MemoryTypes.DUMMY, true);
            return false;
        }
    }

    public MixinMobEntity(final EntityType<? extends LivingEntity> type, final Level worldIn)
    {
        super(type, worldIn);
    }

    private boolean ticked_default_ai = false;
    private boolean checked_for_ai = false;

    @Inject(method = "serverAiStep", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V", args =
    { "ldc=mob tick" }))
    /**
     * Here, during the first tick, we add a dummy task to the brain, to see if
     * it does get called. If this task is not called, then we need to manually
     * tick the brain itself.
     */
    protected void onPreUpdateAITasks(final CallbackInfo cbi)
    {
        if (!this.checked_for_ai)
        {
            LivingEntity living = (LivingEntity) (Object) this;
            final Brain<?> brain = living.getBrain();
            BrainUtil.addToBrain(brain, Lists.newArrayList(MemoryTypes.DUMMY), Lists.newArrayList());
            final List<Pair<Integer, ? extends Behavior<? super LivingEntity>>> dummyTasks = Lists.newArrayList();
            dummyTasks.add(Pair.of(0, new DummySetTask()));
            for (final Activity a : brain.activeActivities) BrainUtil.addToActivity(brain, a, dummyTasks);
            brain.setMemory(MemoryTypes.DUMMY, false);
        }
    }

    @Inject(method = "serverAiStep", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V", args =
    { "ldc=controls" }))
    /**
     * Here we check if the dummy task above was ticked. If it isn't, we tick
     * the brain manually ourselves.
     */
    protected void onPostUpdateAITasks(final CallbackInfo cbi)
    {
        if (!this.checked_for_ai)
        {
            this.ticked_default_ai = this.brain.getMemory(MemoryTypes.DUMMY).get();
            this.checked_for_ai = true;
        }
        if (!this.ticked_default_ai)
        {
            @SuppressWarnings("unchecked")
            final Brain<LivingEntity> brain = (Brain<LivingEntity>) this.getBrain();
            this.getLevel().getProfiler().push("custom_brain");
            brain.tick((ServerLevel) this.getLevel(), this);
            this.getLevel().getProfiler().pop();
        }
    }
}

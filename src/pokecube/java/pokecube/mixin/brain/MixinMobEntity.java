package pokecube.mixin.brain;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.DummySetTask;
import pokecube.core.ai.brain.MemoryModules;
import thut.api.entity.ai.BrainUtil;

import java.util.List;

@Mixin(Mob.class)
public abstract class MixinMobEntity extends LivingEntity
{
    public MixinMobEntity(final EntityType<? extends LivingEntity> type, final Level worldIn)
    {
        super(type, worldIn);
    }

    @Unique
    private boolean ticked_default_ai = false;
    @Unique
    private boolean checked_for_ai = false;

    /**
     * Here, during the first tick, we add a dummy task to the brain, to see if
     * it does get called. If this task is not called, then we need to manually
     * tick the brain itself.
     */
    @SuppressWarnings("deprecation")
    @Inject(method = "serverAiStep", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V", args = {
            "ldc=mob tick" }))
    protected void onPreUpdateAITasks(final CallbackInfo cbi)
    {
        if (!this.checked_for_ai)
        {
            LivingEntity living = this;
            final Brain<?> brain = living.getBrain();
            BrainUtil.addToBrain(brain, Lists.newArrayList(MemoryModules.DUMMY.get()), Lists.newArrayList());
            final List<Pair<Integer, ? extends Behavior<? super LivingEntity>>> dummyTasks = Lists.newArrayList();
            dummyTasks.add(Pair.of(0, new DummySetTask()));
            for (final Activity a : brain.getActiveActivities()) BrainUtil.addToActivity(brain, a, dummyTasks);
            brain.setMemory(MemoryModules.DUMMY.get(), false);
        }
    }

    /**
     * Here we check if the dummy task above was ticked. If it isn't, we tick the brain manually ourselves.
     */
    @Inject(method = "serverAiStep", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V", args = {
            "ldc=controls" }))
    protected void onPostUpdateAITasks(final CallbackInfo cbi)
    {
        if (!this.checked_for_ai)
        {
            this.ticked_default_ai = this.brain.getMemory(MemoryModules.DUMMY.get()).get();
            this.checked_for_ai = true;
            BrainUtils.removeMatchingTasks(this.brain, s -> s instanceof DummySetTask);
        }
        if (!this.ticked_default_ai)
        {
            @SuppressWarnings("unchecked")
            final Brain<LivingEntity> brain = (Brain<LivingEntity>) this.getBrain();
            this.level().getProfiler().push("custom_brain");
            brain.tick((ServerLevel) this.level(), this);
            this.level().getProfiler().pop();
        }
    }
}

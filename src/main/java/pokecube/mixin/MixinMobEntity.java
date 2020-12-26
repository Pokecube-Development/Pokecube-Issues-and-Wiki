package pokecube.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import pokecube.adventures.ai.brain.MemoryTypes;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.RootTask;
import pokecube.core.events.BrainInitEvent;

@Mixin(MobEntity.class)
public abstract class MixinMobEntity extends LivingEntity
{
    private static class DummySetTask extends RootTask<LivingEntity>
    {
        public DummySetTask()
        {
            super(ImmutableMap.of(MemoryTypes.DUMMY, MemoryModuleStatus.REGISTERED));
        }

        @Override
        protected boolean shouldExecute(final ServerWorld worldIn, final LivingEntity owner)
        {
            final Brain<?> brain = owner.getBrain();
            brain.setMemory(MemoryTypes.DUMMY, true);
            return false;
        }
    }

    public MixinMobEntity(final EntityType<? extends LivingEntity> type, final World worldIn)
    {
        super(type, worldIn);
    }

    private boolean ticked_default_ai = false;
    private boolean checked_for_ai    = false;

    @Inject(method = "updateEntityActionState", at = @At(value = "INVOKE_STRING", target = "startSection(Ljava/lang/String;)V", args = {
            "ldc=mob tick" }))
    protected void onPreUpdateAITasks(final CallbackInfo cbi)
    {
        if (!this.checked_for_ai)
        {
            final Brain<?> brain = this.getBrain();
            BrainUtils.addToBrain(brain, Lists.newArrayList(MemoryTypes.DUMMY), Lists.newArrayList());
            MinecraftForge.EVENT_BUS.post(new BrainInitEvent(this));
            final List<Pair<Integer, ? extends Task<? super LivingEntity>>> dummyTasks = Lists.newArrayList();
            dummyTasks.add(Pair.of(0, new DummySetTask()));
            for (final Activity a : brain.activities)
                BrainUtils.addToActivity(brain, a, dummyTasks);
            brain.setMemory(MemoryTypes.DUMMY, false);
        }
    }

    @Inject(method = "updateEntityActionState", at = @At(value = "INVOKE_STRING", target = "startSection(Ljava/lang/String;)V", args = {
            "ldc=controls" }))
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
            this.getEntityWorld().getProfiler().startSection("custom_brain");
            brain.tick((ServerWorld) this.getEntityWorld(), this);
            this.getEntityWorld().getProfiler().endSection();
        }
    }

}

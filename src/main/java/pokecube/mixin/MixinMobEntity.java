package pokecube.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.adventures.ai.brain.MemoryTypes;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.RootTask;
import pokecube.core.events.BrainInitEvent;

@Mixin(Mob.class)
public abstract class MixinMobEntity extends LivingEntity
{
    private static class DummySetTask extends RootTask<LivingEntity>
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
    private boolean checked_for_ai    = false;

    @Inject(method = "serverAiStep", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/profiler/IProfiler;push(Ljava/lang/String;)V", args = {
            "ldc=mob tick" }))
    /**
     * Here, during the first tick, we add a dummy task to the brain, to see if
     * it does get called. If this task is not called, then we need to manually
     * tick the brain itself.
     */
    protected void onPreUpdateAITasks(final CallbackInfo cbi)
    {
        if (!this.checked_for_ai)
        {
            final Brain<?> brain = this.getBrain();
            BrainUtils.addToBrain(brain, Lists.newArrayList(MemoryTypes.DUMMY), Lists.newArrayList());
            MinecraftForge.EVENT_BUS.post(new BrainInitEvent(this));
            final List<Pair<Integer, ? extends Behavior<? super LivingEntity>>> dummyTasks = Lists.newArrayList();
            dummyTasks.add(Pair.of(0, new DummySetTask()));
            for (final Activity a : brain.activeActivities)
                BrainUtils.addToActivity(brain, a, dummyTasks);
            brain.setMemory(MemoryTypes.DUMMY, false);
        }
    }

    @Inject(method = "serverAiStep", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/profiler/IProfiler;push(Ljava/lang/String;)V", args = {
            "ldc=controls" }))
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
            this.getCommandSenderWorld().getProfiler().push("custom_brain");
            brain.tick((ServerLevel) this.getCommandSenderWorld(), this);
            this.getCommandSenderWorld().getProfiler().pop();
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At(value = "RETURN"))
    /**
     * Here we load the brain's memories again, this properly loads in the
     * memories, which seem to be forgotten however vanilla is doing it...
     */
    protected void onPostReadAdditional(final CompoundTag compound, final CallbackInfo cbi)
    {
        if (this.level instanceof ServerLevel)
        {
            MinecraftForge.EVENT_BUS.post(new BrainInitEvent(this));
            if (compound.contains("Brain", 10))
            {
                final Brain<?> brain = this.getBrain();
                final CompoundTag mems = compound.getCompound("Brain").getCompound("memories");
                for (final String s : mems.getAllKeys())
                {
                    final Tag nbt = mems.get(s);
                    try
                    {
                        final Dynamic<Tag> d = new Dynamic<>(NbtOps.INSTANCE, nbt);
                        @SuppressWarnings("unchecked")
                        final MemoryModuleType<Object> mem = (MemoryModuleType<Object>) ForgeRegistries.MEMORY_MODULE_TYPES
                                .getValue(new ResourceLocation(s));
                        final DataResult<?> res = mem.getCodec().map(DataResult::success).orElseGet(
                                () -> DataResult.error("Error loading Memory??")).flatMap(codec -> codec.parse(d));
                        final ExpirableValue<?> memory = (ExpirableValue<?>) res.getOrThrow(true, s1 -> PokecubeCore.LOGGER.error(s1));
                        brain.setMemory(mem, memory.getValue());
                    }
                    catch (final Throwable e)
                    {
                        PokecubeCore.LOGGER.error(e);
                    }
                }
            }
        }
    }

}

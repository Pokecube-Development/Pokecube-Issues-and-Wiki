package pokecube.mixin.brain;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.events.BrainInitEvent;
import pokecube.core.utils.AITools;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntityInit extends Entity
{

    protected MixinLivingEntityInit(EntityType<? extends LivingEntity> type, Level level)
    {
        super(type, level);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructor(EntityType<? extends LivingEntity> type, Level level, CallbackInfo ci)
    {
        LivingEntity living = (LivingEntity) (Object) this;
        MinecraftForge.EVENT_BUS.post(new BrainInitEvent(living));
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
            LivingEntity living = (LivingEntity) (Object) this;
            MinecraftForge.EVENT_BUS.post(new BrainInitEvent(living));
            AITools.reloadBrain(living, compound);
        }
    }

}

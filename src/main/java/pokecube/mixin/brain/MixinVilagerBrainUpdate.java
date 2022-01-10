package pokecube.mixin.brain;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.events.BrainInitEvent;

@Mixin(Villager.class)
public abstract class MixinVilagerBrainUpdate extends AbstractVillager
{

    public MixinVilagerBrainUpdate(EntityType<? extends AbstractVillager> type, Level level)
    {
        super(type, level);
    }

    @Inject(method = "refreshBrain", at = @At(value = "RETURN"))
    public void onRefreshBrain(ServerLevel level, CallbackInfo ci)
    {
        MinecraftForge.EVENT_BUS.post(new BrainInitEvent(this));
    }
}

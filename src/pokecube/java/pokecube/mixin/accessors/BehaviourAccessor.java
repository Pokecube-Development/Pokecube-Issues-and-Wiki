package pokecube.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import pokecube.core.utils.mixin.IBehaviourAccessor;

@Mixin(Behavior.class)
public interface BehaviourAccessor<E extends LivingEntity> extends IBehaviourAccessor<E>
{
    @Invoker("canStillUse")
    boolean invokeCanStillUse(ServerLevel level, E entity, long gameTime);
}

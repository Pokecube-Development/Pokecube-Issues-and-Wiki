package pokecube.mixin.accessors;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;

@Mixin(Behavior.class)
public abstract class BehaviourAccessor<E extends LivingEntity> extends Behavior<E>
{
    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    public BehaviourAccessor(Map entryCondition)
    {
        super(entryCondition);
    }

    @Shadow
    public abstract boolean canStillUse(ServerLevel level, E entity, long gameTime);

}

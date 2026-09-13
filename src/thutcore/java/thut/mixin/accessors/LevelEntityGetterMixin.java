package thut.mixin.accessors;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.LevelEntityGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import thut.api.level.ILevelEntityGetter;

@Mixin(Level.class)
public abstract class LevelEntityGetterMixin implements ILevelEntityGetter
{
    /**
     * Server side this function is public, client side it is not. We need to apply position tracker on both sides,
     * so we will use the invoker rather than an access transformer on the client side.
     */
    @Invoker("getEntities")
    public abstract LevelEntityGetter<Entity> thutcore$getEntityGetter();
}

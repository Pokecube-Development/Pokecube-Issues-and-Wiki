package thut.mixin.entity;

import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thut.api.entity.event.LevelEntityEvent;
import thut.api.entity.multipart.IMultpart;
import thut.core.common.ThutCore;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Mixin(Level.class)
public class LevelEntityEventClient
{
    @Inject(method = "broadcastEntityEvent", at = @At(value = "RETURN"))
    public void thutcore$onBroadcastEntityEvent(Entity entity, byte key, CallbackInfo cb)
    {
        ThutCore.FORGE_BUS.post(new LevelEntityEvent(entity, key));
    }

    @Inject(method = "getEntities(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;Ljava/util/List;I)V", at = @At(value = "RETURN"))
    public <T extends Entity> void thutcore$onGetEntitiesA(EntityTypeTest<Entity, T> entityTypeTest, AABB bounds,
            Predicate<? super T> predicate, List<? super T> output, int maxResults, CallbackInfo ci)
    {
        List<T> parts = new ArrayList<>();
        List<T> remove = new ArrayList<>();
        for (var v : output)
        {
            if (v instanceof IMultpart<?, ?> multi && !multi.shouldSyncParts() && multi.getUseParts() != null
                    && !multi.getUseParts().isEmpty())
            {
                remove.add(entityTypeTest.tryCast((Entity) v));
                for (var p : multi.getUseParts())
                {
                    var t = entityTypeTest.tryCast(p);
                    if (t == null || !predicate.test(t) || !t.getBoundingBox().intersects(bounds)) continue;
                    parts.add(t);
                }
            }
        }
        output.removeAll(remove);
        output.addAll(parts);
    }

    @Inject(method = "getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;", at = @At(value = "RETURN"))
    public void thutcore$onGetEntitiesB(Entity entity, AABB bounds, Predicate<? super Entity> predicate,
            CallbackInfoReturnable<List<Entity>> cir)
    {
        var output = cir.getReturnValue();
        List<Entity> parts = new ArrayList<>();
        List<Entity> remove = new ArrayList<>();
        for (var v : output)
        {
            if (v instanceof IMultpart<?, ?> multi && !multi.shouldSyncParts() && multi.getUseParts() != null
                    && !multi.getUseParts().isEmpty())
            {
                remove.add(v);
                for (var p : multi.getUseParts())
                {
                    if (!predicate.test(p) || !p.getBoundingBox().intersects(bounds)) continue;
                    parts.add(p);
                }
            }
        }
        output.removeAll(remove);
        output.addAll(parts);
    }
}

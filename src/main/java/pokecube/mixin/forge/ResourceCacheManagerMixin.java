package pokecube.mixin.forge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraftforge.resource.ResourceCacheManager;
import pokecube.core.database.Database;

@Mixin(ResourceCacheManager.class)
public class ResourceCacheManagerMixin
{

    @Inject(method = "shouldUseCache()Z", at = @At(value = "HEAD"), cancellable = true)
    private static void onShouldUseCache(CallbackInfoReturnable<Boolean> cir)
    {
        if (!Database.finished_early_loading) cir.setReturnValue(false);
    }
}

package pokecube.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.SharedAttributes;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeCore;

@Mixin(LivingEntity.class)
public class EntityScaleMixin
{
    IPokemob _pokemob = null;
    LivingEntity _this = null;
    boolean _checkedPokemob = false;

    @Inject(method = "getScale", at = @At(value = "RETURN"), cancellable = true)
    public void pokecube$getScale(CallbackInfoReturnable<Float> cbr)
    {
        if (!_checkedPokemob)
        {
            _this = LivingEntity.class.cast(this);
            _pokemob = PokemobCaps.getPokemobFor(_this);
            _checkedPokemob = true;
        }
        float size = cbr.getReturnValueF();
        size *= SharedAttributes.getScale(_this);
        if (_pokemob != null)
        {
            size *= PokecubeCore.getConfig().scalefactor;
            // Reset this if we set it from dynamaxing
            if (_this.getParts() != null && _this.getParts().length == 0) _this.noCulling = false;
            if (size > 3) _this.noCulling = true;
        }
        cbr.setReturnValue(size);
    }
}

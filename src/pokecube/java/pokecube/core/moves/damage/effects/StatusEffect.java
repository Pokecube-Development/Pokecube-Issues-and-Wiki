package pokecube.core.moves.damage.effects;

import it.unimi.dsi.fastutil.ints.Int2DoubleFunction;
import it.unimi.dsi.fastutil.ints.Int2DoubleFunctions;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.PokecubeAPI;
import pokecube.api.events.pokemobs.combat.StatusEvent;
import pokecube.core.PokecubeCore;

public abstract class StatusEffect extends MobEffect
{
    public static final Int2DoubleFunction NEGATIVE_ONE = Int2DoubleFunctions.primitive(i -> -1.0);
    public static final Int2DoubleFunction NEGATIVE_PTFIVE = Int2DoubleFunctions.primitive(i -> -0.5);

    protected final Holder<MobEffect> effect;

    protected StatusEffect(MobEffectCategory category, int color, Holder<MobEffect> effect)
    {
        super(category, color);
        this.effect = effect;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier)
    {
        if (PokecubeCore.getConfig().attackCooldown > 0) return duration % PokecubeCore.getConfig().attackCooldown == 0;
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier)
    {
        if (!(entity.level() instanceof ServerLevel level)) return true;
        LivingEntity source = StatusEffects.getSource(StatusEffects.POISON, entity, level);
        StatusEvent.OnApplyTick event = new StatusEvent.OnApplyTick(entity, source, effect, amplifier);
        PokecubeAPI.MOVE_BUS.post(event);
        return !event.isCanceled();
    }

    @Override
    public void onEffectAdded(LivingEntity entity, int amplifier)
    {
        if (!(entity.level() instanceof ServerLevel level)) return;
        LivingEntity source = StatusEffects.getSource(StatusEffects.POISON, entity, level);
        PokecubeAPI.MOVE_BUS.post(new StatusEvent.OnAdded(entity, source, effect, amplifier));
        super.onEffectAdded(entity, amplifier);
    }
}

package pokecube.core.moves.damage.effects;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.events.pokemobs.combat.StatusEvent;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.core.PokecubeCore;
import pokecube.core.network.pokemobs.PacketSyncStatus;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class StatusEffects
{
    public static final DeferredRegister<MobEffect> REGISTER;

    public static final DeferredHolder<MobEffect, Burn> BURN;
    public static final DeferredHolder<MobEffect, Freeze> FREEZE;
    public static final DeferredHolder<MobEffect, Paralysis> PARALYSIS;
    public static final DeferredHolder<MobEffect, Poison> POISON;
    public static final DeferredHolder<MobEffect, Sleep> SLEEP;

    public static final DeferredHolder<MobEffect, Confusion> CONFUSE;
    public static final DeferredHolder<MobEffect, Curse> CURSE;
    public static final DeferredHolder<MobEffect, Flinch> FLINCH;

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<EffectHolder>> EFFECT_SOURCES;

    public static final Map<Holder<MobEffect>, Set<Holder<MobEffect>>> EXCLUSIVE_EFFECTS = new HashMap<>();
    public static final Map<Integer, Holder<MobEffect>> EFFECT_BY_ID = new HashMap<>();
    public static final Set<Holder<MobEffect>> TEMPORARY = new HashSet<>();
    public static final Set<Holder<MobEffect>> CURE_ON_RECALL = new HashSet<>();

    static
    {
        REGISTER = DeferredRegister.create(Registries.MOB_EFFECT, PokecubeCore.MODID);

        BURN = REGISTER.register("burn", () -> new Burn(0xFFAA3333));
        FREEZE = REGISTER.register("freeze", () -> new Freeze(0xFF3333AA));
        PARALYSIS = REGISTER.register("paralysis", () -> new Paralysis(0xFFAAAA33));
        POISON = REGISTER.register("poison", () -> new Poison(0xFF33AA33));
        SLEEP = REGISTER.register("sleep", () -> new Sleep(0xFF555555));

        CONFUSE = REGISTER.register("confuse", () -> new Confusion(0xFF555555));
        CURSE = REGISTER.register("curse", () -> new Curse(0xFF555555));
        FLINCH = REGISTER.register("flinch", () -> new Flinch(0xFF555555));

        EFFECT_SOURCES = PokecubeCore.ATTACHMENTS.register("effect_sources",
                () -> AttachmentType.serializable(i -> new EffectHolder()).build());

        registerDefaultExclusions();
        initDefaultIDs();

        TEMPORARY.add(SLEEP);
        TEMPORARY.add(PARALYSIS);
        TEMPORARY.add(FREEZE);

        TEMPORARY.add(CONFUSE);
        TEMPORARY.add(FLINCH);

        CURE_ON_RECALL.add(CURSE);
        CURE_ON_RECALL.add(CONFUSE);
        CURE_ON_RECALL.add(FLINCH);
    }

    @SuppressWarnings("unchecked")
    private static void registerDefaultExclusions()
    {
        addExclusions(SLEEP, BURN, FREEZE, PARALYSIS, POISON);
        addExclusions(BURN, SLEEP, FREEZE, PARALYSIS, POISON);
        addExclusions(FREEZE, BURN, SLEEP, PARALYSIS, POISON);
        addExclusions(PARALYSIS, BURN, FREEZE, SLEEP, POISON);
        addExclusions(POISON, BURN, FREEZE, PARALYSIS, SLEEP);
    }

    private static void initDefaultIDs()
    {
        EFFECT_BY_ID.put(IMoveConstants.STATUS_BRN, BURN);
        EFFECT_BY_ID.put(IMoveConstants.STATUS_SLP, SLEEP);
        EFFECT_BY_ID.put(IMoveConstants.STATUS_FRZ, FREEZE);
        EFFECT_BY_ID.put(IMoveConstants.STATUS_PAR, PARALYSIS);
        EFFECT_BY_ID.put(IMoveConstants.STATUS_PSN, POISON);
        EFFECT_BY_ID.put(IMoveConstants.STATUS_PSN2, POISON);

        EFFECT_BY_ID.put(IMoveConstants.CHANGE_CONFUSED, CONFUSE);
        EFFECT_BY_ID.put(IMoveConstants.CHANGE_CURSE, CURSE);
        EFFECT_BY_ID.put(IMoveConstants.CHANGE_FLINCH, FLINCH);
    }

    public static Holder<MobEffect> getForId(int id)
    {
        return EFFECT_BY_ID.get(id);
    }

    public static void addExclusions(Holder<MobEffect> effect, Holder<MobEffect>... exclude)
    {
        var exclusions = EXCLUSIVE_EFFECTS.computeIfAbsent(effect, i -> new HashSet<>());
        for (Holder<MobEffect> holder : exclude)
        {
            var _exclusions = EXCLUSIVE_EFFECTS.computeIfAbsent(holder, i -> new HashSet<>());
            _exclusions.add(effect);
            exclusions.add(holder);
        }
    }

    public static void cureEffectsOnRecall(LivingEntity mob)
    {
        CURE_ON_RECALL.forEach(effect->removeEffect(mob, effect));
    }

    private static boolean removeEffect(LivingEntity mob, Holder<MobEffect> effect)
    {
        if (mob.hasEffect(effect))
        {
            boolean removed = mob.removeEffect(effect);
            if (removed)
            {
                var sources = mob.getData(EFFECT_SOURCES);
                sources.effectSources().remove(effect);
            }
            return removed;
        }
        return true;
    }

    public static boolean healStatusEffects(LivingEntity mob)
    {
        boolean removed = false;
        removed = removeEffect(mob, BURN) || removed;
        removed = removeEffect(mob, CONFUSE) || removed;
        removed = removeEffect(mob, FREEZE) || removed;
        removed = removeEffect(mob, PARALYSIS) || removed;
        removed = removeEffect(mob, POISON) || removed;
        removed = removeEffect(mob, SLEEP) || removed;
        removed = removeEffect(mob, BURN) || removed;
        if (removed) PacketSyncStatus.sendUpdate(mob);
        return removed;
    }

    public static boolean hasAnyStatusEffects(LivingEntity mob)
    {
        var data = mob.getData(EFFECT_SOURCES).effectSources();
        if (!data.isEmpty())
        {
            Set<Holder<MobEffect>> stale = new HashSet<>();
            data.forEach((h, id) -> {if (!mob.hasEffect(h)) stale.add(h);});
            stale.forEach(data::remove);
        }
        return !data.isEmpty();
    }

    public static MobEffectInstance getStatusEffect(LivingEntity mob)
    {
        var status = EXCLUSIVE_EFFECTS.keySet().stream().filter(mob::hasEffect).findFirst();
        return status.map(mob::getEffect).orElse(null);
    }

    public static boolean setStatus(IPokemob mob, IPokemob source, int status)
    {
        return setStatus(mob.getEntity(), source == null ? null : source.getEntity(), status, 0);
    }

    public static boolean setStatus(IPokemob mob, IPokemob source, int status, int turns)
    {
        return setStatus(mob.getEntity(), source == null ? null : source.getEntity(), status, turns);
    }

    public static boolean setStatus(LivingEntity mob, LivingEntity source, int status, int turns)
    {
        // Special case to clear status
        if (status == IMoveConstants.STATUS_NON)
        {
            return healStatusEffects(mob);
        }
        var effect = EFFECT_BY_ID.get(status);
        if (effect == null) return false;
        int amplifier = 1;
        if (status == IMoveConstants.STATUS_PSN2) amplifier = Poison.BAD_POISON_AMPLIFIER;
        return setStatus(mob, source, effect, turns, amplifier);
    }

    public static boolean setStatus(LivingEntity mob, LivingEntity source, Holder<MobEffect> status, int turns,
            int amplifier)
    {
        // If it already has the status, skip
        if (mob.hasEffect(status)) return false;
        if (turns == 0 && TEMPORARY.contains(status)) turns = mob.getRandom().nextInt(2, 5);
        // make and apply the status.
        int duration = turns * PokecubeCore.getConfig().attackCooldown;
        if (duration <= 0) duration = -1;
        var instance = new MobEffectInstance(status, duration, amplifier);
        return setStatus(mob, source, instance);
    }

    public static boolean setStatus(LivingEntity mob, LivingEntity source, MobEffectInstance instance)
    {
        var status = instance.getEffect();
        // If it already has the status, skip
        if (mob.hasEffect(status)) return false;

        // Next check exclusions
        var exclusions = EXCLUSIVE_EFFECTS.getOrDefault(status, Collections.emptySet());
        for (var e : exclusions) if (mob.hasEffect(e)) return false;

        // Now check events for if the status can be applied, this handles pokemob types, etc
        var event = new StatusEvent.PreAdd(mob, source, status, instance.getAmplifier());
        if (PokecubeAPI.MOVE_BUS.post(event).getResult() == TriState.FALSE) return false;

        // finally apply the status
        var applied = mob.addEffect(instance, source);
        if (applied && source != null)
        {
            var sources = mob.getData(EFFECT_SOURCES);
            sources.effectSources().put(status, source.getUUID());
        }
        if (applied)
        {
            PacketSyncStatus.sendUpdate(mob);
        }
        return applied;
    }

    public static LivingEntity getSource(Holder<MobEffect> effect, LivingEntity affected, ServerLevel level)
    {
        UUID sourceMob = affected.getUUID();
        if (affected.hasData(EFFECT_SOURCES))
        {
            var sources = affected.getData(EFFECT_SOURCES);
            sourceMob = sources.effectSources.getOrDefault(effect, sourceMob);
        }
        if (sourceMob == affected.getUUID()) return affected;
        // Otherwise try to find it from the world
        var mob = level.getEntity(sourceMob);
        // Return if it was there
        if (mob instanceof LivingEntity e) return e;
        // Otherwise return affected.
        return affected;
    }

    public static record EffectHolder(Map<Holder<MobEffect>, UUID> effectSources) implements INBTSerializable<ListTag>
    {
        public EffectHolder()
        {
            this(new HashMap<>());
        }

        @Override
        public ListTag serializeNBT(HolderLookup.Provider provider)
        {
            ListTag nbt = new ListTag();
            effectSources.forEach((effect, id) -> {
                CompoundTag comp = new CompoundTag();
                comp.putIntArray("id", UUIDUtil.uuidToIntArray(id));
                comp.putString("e", effect.getRegisteredName());
                nbt.add(comp);
            });
            return nbt;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider provider, ListTag nbt)
        {
            effectSources.clear();
            nbt.forEach(tag -> {
                if (tag instanceof CompoundTag comp)
                {
                    UUID id = UUIDUtil.uuidFromIntArray(comp.getIntArray("id"));
                    String key = comp.getString("e");
                    Holder<MobEffect> effect = provider.holderOrThrow(
                            ResourceKey.create(Registries.MOB_EFFECT, ResourceLocation.parse(key)));
                    effectSources.put(effect, id);
                }
            });
        }
    }
}

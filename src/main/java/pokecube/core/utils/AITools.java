package pokecube.core.utils;

import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.TeamManager;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.core.PokecubeCore;
import pokecube.core.database.tags.Tags;
import pokecube.core.entity.pokecubes.EntityPokecubeBase;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.moves.damage.IPokedamage;
import thut.core.common.ThutCore;
import thut.lib.RegHelper;

public class AITools
{

    public static final String FRIENDLY = "friendly";
    public static final String HOSTILE = "hostile";
    public static final String TIMID = "timid";

    private static class AgroCheck implements Predicate<IPokemob>
    {
        @Override
        public boolean test(final IPokemob input)
        {
            final boolean tame = input.getGeneralState(GeneralStates.TAMED);
            if (tame) return false;
            if (input.getEntity().getPersistentData().getBoolean("alwaysAgress")) return true;
            boolean wildAgress = Tags.POKEMOB.isIn(HOSTILE, input.getPokedexEntry().getTrimmedName());
            if (wildAgress)
            {
                if (PokecubeCore.getConfig().hostileAgroRate > 0)
                    wildAgress = ThutCore.newRandom().nextInt(PokecubeCore.getConfig().hostileAgroRate) == 0;
                else wildAgress = false;
                return wildAgress;
            }
            boolean friendly = Tags.POKEMOB.isIn(FRIENDLY, input.getPokedexEntry().getTrimmedName());
            if (friendly) return false;

            /// If not hostile, or not friendly, it uses the normal config
            /// option for aggressive
            if (PokecubeCore.getConfig().aggressiveAgroRate > 0)
                wildAgress = ThutCore.newRandom().nextInt(PokecubeCore.getConfig().aggressiveAgroRate) == 0;
            else wildAgress = false;
            return wildAgress;
        }
    }

    private static class ValidTargetCheck implements Predicate<Entity>
    {
        @Override
        public boolean test(Entity input)
        {
            if (input == null) return true;
            final Entity core = EntityTools.getCoreEntity(input);
            synchronized (_blacklist)
            {
                if (_blacklist.stream().anyMatch(e -> e.test(core))) return false;
            }
            // Then check if is a valid player.
            if (core instanceof ServerPlayer player)
            {
                // Do not target spectator players.
                if (player.isSpectator()) return false;
                // Otherwise it is fine.
                return true;
            }
            // Confirm is not an egg or a pokecube as well
            if (core instanceof EntityPokemobEgg) return false;
            // Confirm is not an egg or a pokecube as well
            if (core instanceof EntityPokecubeBase) return false;
            return core instanceof Mob;
        }
    }

    private static class ValidAgressionCheck implements Predicate<Entity>
    {
        @Override
        public boolean test(Entity input)
        {
            if (input == null) return true;
            final Entity core = EntityTools.getCoreEntity(input);
            synchronized (_blacklist)
            {
                if (_blacklist.stream().anyMatch(e -> e.test(core))) return false;
            }
            // Then check if is a valid player.
            if (core instanceof ServerPlayer player)
            {
                // Do not target creative or spectator
                if (player.isCreative() || player.isSpectator()) return false;
                // Do not target any player on easy or peaceful
                if (player.getLevel().getDifficulty().getId() <= Difficulty.EASY.getId()) return false;
                return true;
            }
            // Confirm is not an egg or a pokecube as well
            if (core instanceof EntityPokemobEgg) return false;
            // Confirm is not an egg or a pokecube as well
            if (core instanceof EntityPokecubeBase) return false;
            return core instanceof Mob;
        }
    }

    private static class CanNavigate implements Predicate<IPokemob>
    {
        @Override
        public boolean test(final IPokemob input)
        {
            if (input.swims() && input.getEntity().isInWater()) return true;
            if (input.floats() || input.flys()) return true;
            return input.isOnGround();
        }
    }

    private static class ValidDamageToPokemob implements Predicate<DamageSource>
    {
        @Override
        public boolean test(final DamageSource t)
        {
            if (!PokecubeCore.getConfig().onlyPokemobsDamagePokemobs) return true;
            return t instanceof IPokedamage;
        }
    }

    /**
     * Checks the blacklists set via configs, to see whether the target is a
     * valid choice.
     */
    public static final Predicate<Entity> validCombatTargets = new ValidTargetCheck();

    /**
     * Checks the blacklists set via configs, to see whether the target is a
     * valid choice.
     */
    public static final Predicate<Entity> validAgroTarget = new ValidAgressionCheck();

    /**
     * Checks to see if the wild pokemob should try to agro the nearest visible
     * player.
     */
    public static Predicate<IPokemob> shouldAgroNearestPlayer = new AgroCheck();

    /**
     * Checks to see if the damage source can affect pokemobs
     */
    public static Predicate<DamageSource> validToHitPokemob = new ValidDamageToPokemob();

    /**
     * Checks to see if the pokemob is capable of changing its motion, this is
     * used for things like dodging in combat, etc
     */
    public static Predicate<IPokemob> canNavigate = new CanNavigate();

    private static final List<Predicate<Entity>> _blacklist = Lists.newArrayList();

    public static void clearAgroBlacklist()
    {
        synchronized (_blacklist)
        {
            _blacklist.clear();
        }
    }

    public static void registerAgroBlacklist(String var)
    {
        synchronized (_blacklist)
        {
            if (var.startsWith("#"))
            {
                TagKey<EntityType<?>> tag = TagKey.create(Registry.ENTITY_TYPE_REGISTRY,
                        new ResourceLocation(var.replace("#", "")));
                _blacklist.add(e -> e.getType().is(tag));
            }
            else
            {
                ResourceLocation id = new ResourceLocation(var.replace("#", ""));
                _blacklist.add(e -> id.equals(RegHelper.getKey(e)));
            }
        }
    }

    public static void initIDs()
    {
        clearAgroBlacklist();
        for (String s : PokecubeCore.getConfig().aggroBlacklist)
        {
            registerAgroBlacklist(s);
        }
    }

    public static boolean shouldBeAbleToAgro(final LivingEntity entity, final Entity target)
    {
        // If target is null, can't agro.
        if (target == null) return false;
        // Never target self
        if (target == entity) return false;
        // Never target blacklisted things
        if (!AITools.validAgroTarget.test(target)) return false;
        // Only target living entities
        if (!(target instanceof LivingEntity)) return false;
        final IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
        // Some pokemob specific checks
        if (pokemob != null)
        {
            // Wild pokemobs can target whatever they feel like
            if (pokemob.getOwnerId() == null) return true;
            // Pokemobs fighting over mates don't care if they are wild
            if (pokemob.getCombatState(CombatStates.MATEFIGHT)) return true;
        }
        // We were checking in general, from a null mob, so valid at this point.
        if (entity == null) return true;
        // Otherwise, prevent combat on same team
        if (TeamManager.sameTeam(entity, target)) return false;
        // If we got to here, it was a valid target
        return true;
    }

    public static void reloadBrain(LivingEntity entity, CompoundTag compound)
    {
        if (compound.contains("Brain", 10))
        {
            final Brain<?> brain = entity.getBrain();
            final CompoundTag mems = compound.getCompound("Brain").getCompound("memories");
            for (final String s : mems.getAllKeys())
            {
                final Tag nbt = mems.get(s);
                try
                {
                    final Dynamic<Tag> d = new Dynamic<>(NbtOps.INSTANCE, nbt);
                    @SuppressWarnings("unchecked")
                    final MemoryModuleType<Object> mem = (MemoryModuleType<Object>) ForgeRegistries.MEMORY_MODULE_TYPES
                            .getValue(new ResourceLocation(s));
                    final DataResult<?> res = mem.getCodec().map(DataResult::success)
                            .orElseGet(() -> DataResult.error("Error loading Memory??"))
                            .flatMap(codec -> codec.parse(d));
                    final ExpirableValue<?> memory = (ExpirableValue<?>) res.getOrThrow(true,
                            s1 -> PokecubeAPI.LOGGER.error(s1));
                    brain.setMemory(mem, memory.getValue());
                }
                catch (final Throwable e)
                {
                    PokecubeAPI.LOGGER.error(e);
                }
            }
        }
    }
}

package pokecube.core.utils;

import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Sets;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.TeamManager;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.moves.damage.IPokedamage;
import thut.core.common.ThutCore;

public class AITools
{
    public static class AgroCheck implements Predicate<IPokemob>
    {
        @Override
        public boolean test(final IPokemob input)
        {
            final boolean tame = input.getGeneralState(GeneralStates.TAMED);
            boolean wildAgress = !tame;
            if (PokecubeCore.getConfig().mobAgroRate > 0) wildAgress = wildAgress && ThutCore.newRandom().nextInt(
                    PokecubeCore.getConfig().mobAgroRate) == 0;
            else wildAgress = false;
            // Check if the mob should always be agressive.
            if (!tame && !wildAgress && input.getEntity().tickCount % 20 == 0) wildAgress = input.getEntity()
                    .getPersistentData().getBoolean("alwaysAgress");
            return wildAgress;
        }
    }

    private static class ValidCheck implements Predicate<Entity>
    {
        @Override
        public boolean test(Entity input)
        {
            if (input == null) return true;
            input = EntityTools.getCoreEntity(input);
            final ResourceLocation eid = input.getType().getRegistryName();
            if (AITools.invalidIDs.contains(eid)) return false;
            for (final String tag : AITools.invalidTags)
                if (input.getTags().contains(tag)) return false;

            // Then check if is a valid player.
            if (input instanceof ServerPlayer)
            {
                final ServerPlayer player = (ServerPlayer) input;
                // Do not target creative or spectator
                if (player.isCreative() || player.isSpectator()) return false;
                // Do not target any player on easy or peaceful
                if (player.getLevel().getDifficulty().getId() <= Difficulty.EASY.getId()) return false;
                return true;
            }
            // Confirm is not an egg or a pokecube as well
            if (input instanceof EntityPokemobEgg) return false;
            return input instanceof Mob;
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

    public static boolean handleDamagedTargets = true;

    public static int DEAGROTIMER = 50;

    public static Set<ResourceLocation> invalidIDs = Sets.newHashSet();

    public static Set<String> invalidTags = Sets.newHashSet();

    /**
     * Checks the blacklists set via configs, to see whether the target is a
     * valid choice.
     */
    public static final Predicate<Entity> validTargets = new ValidCheck();

    /**
     * Checks to see if the wild pokemob should try to agro the nearest visible
     * player.
     */
    public static Predicate<IPokemob> shouldAgroNearestPlayer = new AgroCheck();

    /**
     * Checks to see if the wild pokemob should try to agro the nearest visible
     * player.
     */
    public static Predicate<DamageSource> validToHitPokemob = new ValidDamageToPokemob();

    /**
     * Checks to see if the pokemob is capable of changing its motion, this is
     * used for things like dodging in combat, etc
     */
    public static Predicate<IPokemob> canNavigate = new CanNavigate();

    public static void initIDs()
    {
        final Set<ResourceLocation> keys = ForgeRegistries.ENTITIES.getKeys();
        for (String s : PokecubeCore.getConfig().aggroBlacklistIds)
            if (s.endsWith("*"))
            {
                s = s.substring(0, s.length() - 1);
                for (final ResourceLocation res : keys)
                    if (res.toString().startsWith(s)) AITools.invalidIDs.add(res);
            }
            else AITools.invalidIDs.add(new ResourceLocation(s));

        for (final String s : PokecubeCore.getConfig().aggroBlacklistTags)
            AITools.invalidTags.add(s);
    }

    public static boolean shouldBeAbleToAgro(final LivingEntity entity, final Entity target)
    {
        // Never target self
        if (target == entity) return false;
        // Never target blacklisted things
        if (!AITools.validTargets.test(target)) return false;
        // Only target living entities
        if (!(target instanceof LivingEntity)) return false;
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
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

}

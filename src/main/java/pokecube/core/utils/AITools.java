package pokecube.core.utils;

import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.items.pokecubes.EntityPokecubeBase;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.moves.damage.IPokedamage;

public class AITools
{
    public static class AgroCheck implements Predicate<IPokemob>
    {
        @Override
        public boolean test(final IPokemob input)
        {
            final boolean tame = input.getGeneralState(GeneralStates.TAMED);
            boolean wildAgress = !tame;
            if (PokecubeCore.getConfig().mobAgroRate > 0) wildAgress = wildAgress && new Random().nextInt(PokecubeCore
                    .getConfig().mobAgroRate) == 0;
            else wildAgress = false;
            // Check if the mob should always be agressive.
            if (!tame && !wildAgress && input.getEntity().ticksExisted % 20 == 0) wildAgress = input.getEntity()
                    .getPersistentData().getBoolean("alwaysAgress");
            return wildAgress;
        }
    }

    private static class ValidCheck implements Predicate<Entity>
    {
        @Override
        public boolean test(final Entity input)
        {
            final ResourceLocation eid = input.getType().getRegistryName();
            if (AITools.invalidIDs.contains(eid)) return false;
            // Then check if disabled via class
            for (final Class<?> clas : AITools.invalidClasses)
                if (clas.isInstance(input)) return false;
            // Then check if is a spectating player.
            if (input instanceof ServerPlayerEntity)
            {
                final ServerPlayerEntity player = (ServerPlayerEntity) input;
                if (player.isSpectator() || player.getServerWorld().getDifficulty().getId() <= Difficulty.EASY.getId())
                    return false;
            }
            // Confirm is not an egg or a pokecube as well
            if (input instanceof EntityPokemobEgg) return false;
            if (input instanceof EntityPokecubeBase) return false;
            return true;
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

    public static int           DEAGROTIMER    = 50;
    public static Set<Class<?>> invalidClasses = Sets.newHashSet();

    public static Set<ResourceLocation> invalidIDs = Sets.newHashSet();

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
        for (final String s : PokecubeCore.getConfig().guardBlacklistClass)
            try
            {
                final Class<?> c = Class.forName(s, false, PokecubeCore.getConfig().getClass().getClassLoader());
                AITools.invalidClasses.add(c);
            }
            catch (final ClassNotFoundException e)
            {
                e.printStackTrace();
            }
        final Set<ResourceLocation> keys = ForgeRegistries.ENTITIES.getKeys();
        for (String s : PokecubeCore.getConfig().guardBlacklistId)
            if (s.endsWith("*"))
            {
                s = s.substring(0, s.length() - 1);
                for (final ResourceLocation res : keys)
                    if (res.toString().startsWith(s)) AITools.invalidIDs.add(res);
            }
            else AITools.invalidIDs.add(new ResourceLocation(s));
    }

}

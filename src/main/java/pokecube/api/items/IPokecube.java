package pokecube.api.items;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.pokemobs.CaptureEvent;
import pokecube.api.events.pokemobs.CaptureEvent.Post;
import pokecube.api.events.pokemobs.CaptureEvent.Pre;
import pokecube.core.items.pokecubes.EntityPokecubeBase;
import thut.api.maths.Vector3;

public interface IPokecube
{
    /**
     * helper class so extensions don't need to include blank onPostCapture and
     * onPreCapture.
     */
    public abstract static class DefaultPokecubeBehavior extends PokecubeBehavior
    {

        @Override
        public void onPostCapture(final Post evt)
        {}

        @Override
        public void onPreCapture(final Pre evt)
        {}
    }

    public static class NormalPokecubeBehavoir extends DefaultPokecubeBehavior
    {
        final double rate;

        public NormalPokecubeBehavoir(final double rate)
        {
            this.rate = rate;
        }

        @Override
        public double getCaptureModifier(final IPokemob mob)
        {
            return this.rate;
        }

    }

    public static abstract class PokecubeBehavior extends ForgeRegistryEntry<PokecubeBehavior>
    {
        public static Supplier<IForgeRegistry<PokecubeBehavior>> BEHAVIORS;

        // Whoever registers the default pokecube should set this.
        public static ResourceLocation DEFAULTCUBE = null;
        public static ResourceLocation POKESEAL = null;

        /**
         * Adds it to the list of behaviours to run when a pokecube is used.
         *
         * @param cubeId
         * @param behavior
         */
        public static void addCubeBehavior(final PokecubeBehavior behavior)
        {
            BEHAVIORS.get().register(behavior);
        }

        public String name;

        public PokecubeBehavior setName(String name)
        {
            this.name = name;
            return this;
        }

        /**
         * Additional flat bonus to capture rate, this is mostly used for
         * HeavyCube's flat adjustment rate.
         *
         * @param mob
         * @return
         */
        public int getAdditionalBonus(final IPokemob mob)
        {
            return 0;
        }

        /**
         * this is the capture strength of the cube, 0 is never capture, 255 is
         * always capture. Override this and place something other than -1 if
         * you wish to use it.
         *
         * @param mob
         * @param pokecubeId
         * @return
         */
        public abstract double getCaptureModifier(IPokemob mob);

        public abstract void onPostCapture(CaptureEvent.Post evt);

        /**
         * Called before the pokemob is captured, cancel the event to prevent
         * capture from occuring.
         *
         * @param evt
         */
        public abstract void onPreCapture(CaptureEvent.Pre evt);

        /**
         * Allows modifications of updates for the pokemob.
         *
         * @param mob
         */
        public void onUpdate(final IPokemob mob)
        {

        }
    }

    default boolean canCapture(final Entity e, final ItemStack cube)
    {
        return PokemobCaps.getPokemobFor(e) != null;
    }

    /**
     * this is the capture strength of the cube, 0 is never capture, 255 is
     * always capture.
     *
     * @param mob
     * @param pokecubeId
     * @return
     */
    double getCaptureModifier(IPokemob mob, ResourceLocation pokecubeId);

    default double getCaptureModifier(final Entity mob, final ResourceLocation pokecubeId)
    {
        final IPokemob pokemob = PokemobCaps.getPokemobFor(mob);
        return pokemob != null ? this.getCaptureModifier(pokemob, pokecubeId) : 0;
    }

    /**
     * Used for throwing cubes out into the air without a specific target.
     *
     * @param world
     * @param player
     * @param cube
     * @param direction
     * @param power
     * @return
     */
    EntityPokecubeBase throwPokecube(Level world, LivingEntity thrower, ItemStack cube, Vector3 direction, float power);

    /**
     * Used to throw the pokecube at a specific target
     *
     * @param world
     * @param player
     * @param cube
     * @param targetLocation
     * @param target
     * @return
     */
    EntityPokecubeBase throwPokecubeAt(Level world, LivingEntity thrower, ItemStack cube,
            @Nullable Vector3 targetLocation, @Nullable Entity target);

}

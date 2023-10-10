package pokecube.api.items;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.pokemobs.CaptureEvent;
import pokecube.api.events.pokemobs.CaptureEvent.Post;
import pokecube.api.events.pokemobs.CaptureEvent.Pre;
import pokecube.core.PokecubeCore;
import pokecube.core.entity.pokecubes.EntityPokecubeBase;
import thut.api.maths.Vector3;

public interface IPokecube
{
    /**
     * helper class so extensions don't need to include blank onPostCapture and
     * onPreCapture.
     */
    public abstract static class DefaultPokecubeBehaviour extends PokecubeBehaviour
    {

        @Override
        public void onPostCapture(final Post evt)
        {}

        @Override
        public void onPreCapture(final Pre evt)
        {}
    }

    public static class NormalPokecubeBehaviour extends DefaultPokecubeBehaviour
    {
        final double rate;

        public NormalPokecubeBehaviour(final double rate)
        {
            this.rate = rate;
        }

        @Override
        public double getCaptureModifier(final IPokemob mob)
        {
            return this.rate;
        }

    }

    public static abstract class PokecubeBehaviour
    {
        public static Map<ResourceLocation, PokecubeBehaviour> BEHAVIORS = Maps.newHashMap();

        // Whoever registers the default pokecube should set this.
        public static ResourceLocation DEFAULTCUBE = null;
        public static ResourceLocation POKESEAL = null;

        /**
         * Adds it to the list of behaviours to run when a pokecube is used.
         *
         * @param cubeId
         * @param behavior
         */
        public static void addCubeBehavior(final PokecubeBehaviour behaviour)
        {
            BEHAVIORS.put(behaviour.getKey(), behaviour);
        }

        public String name;
        private ResourceLocation key;

        public PokecubeBehaviour setName(String name)
        {
            this.name = name;
            this.key = new ResourceLocation(PokecubeCore.MODID, name + "cube");
            return this;
        }

        public ResourceLocation getKey()
        {
            return key;
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

    /**
     * 
     * @param e - mob to try to capture
     * @param cube - the item we are attached to
     * @return whether the capture is allowed
     */
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

package pokecube.core.impl.capabilities;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Lists;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.entity.pokemobs.EntityPokemob;
import thut.api.ThutCaps;
import thut.api.entity.IAnimated;
import thut.api.entity.IMobTexturable;
import thut.lib.RegHelper;

public class TextureableCaps
{
    public static class NPCCap<E extends LivingEntity> implements IMobTexturable, ICapabilityProvider
    {
        private final LazyOptional<IMobTexturable> holder = LazyOptional.of(() -> this);
        E mob;
        String modid;
        public Function<LivingEntity, ResourceLocation> texGetter;
        public Function<LivingEntity, Boolean> slim;

        public NPCCap()
        {}

        @SuppressWarnings("unchecked")
        public NPCCap(final E mob, final Function<E, ResourceLocation> texGetter, final Function<E, Boolean> slim)
        {
            this();
            this.mob = mob;
            this.modid = RegHelper.getKey(this.mob.getType()).getNamespace();
            this.texGetter = (Function<LivingEntity, ResourceLocation>) texGetter;
            this.slim = (Function<LivingEntity, Boolean>) slim;
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return ThutCaps.MOBTEX_CAP.orEmpty(cap, this.holder);
        }

        @Override
        public LivingEntity getEntity()
        {
            return this.mob;
        }

        @Override
        public String getModId()
        {
            return this.modid;
        }

        @Override
        public ResourceLocation getTexture(final String part)
        {
            return this.texGetter.apply(this.mob);
        }

        @Override
        public List<String> getTextureStates()
        {
            return TextureableCaps.STATES;
        }

        @Override
        public ResourceLocation preApply(final ResourceLocation in)
        {
            return this.texGetter.apply(this.mob);
        }

    }

    public static class PokemobCap implements IMobTexturable, ICapabilityProvider
    {
        private final LazyOptional<IMobTexturable> holder = LazyOptional.of(() -> this);
        EntityPokemob mob;
        public IPokemob pokemob;
        IAnimated animated;
        String forme;
        List<String> states = Lists.newArrayList();

        public PokemobCap()
        {}

        public PokemobCap(final EntityPokemob mob)
        {
            this();
            this.mob = mob;
        }

        private void checkPokemob()
        {
            if (this.pokemob == null)
            {
                this.pokemob = PokemobCaps.getPokemobFor(this.mob);
                this.animated = ThutCaps.getAnimated(this.mob);
            }
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return ThutCaps.MOBTEX_CAP.orEmpty(cap, this.holder);
        }

        @Override
        public LivingEntity getEntity()
        {
            return this.mob;
        }

        @Override
        public int getRandomSeed()
        {
            checkPokemob();
            return this.pokemob.getRNGValue();
        }

        @Override
        public String getModId()
        {
            checkPokemob();
            return this.pokemob.getPokedexEntry().getModId();
        }

        @Override
        public ResourceLocation getTexture(final String part)
        {
            checkPokemob();
            return this.pokemob.getTexture();
        }

        @Override
        public List<String> getTextureStates()
        {
            checkPokemob();
            if (this.animated != null)
            {
                states.clear();
                states.addAll(animated.getChoices());
                states.addAll(animated.transientAnimations());
                return states;
            }
            return this.states;
        }

        @Override
        public ResourceLocation preApply(final ResourceLocation in)
        {
            checkPokemob();
            return this.pokemob.modifyTexture(in);
        }

        PokedexEntry lastEntry = null;

        @Override
        public String getForm()
        {
            checkPokemob();
            if (this.forme == null || this.pokemob == null || this.pokemob.getPokedexEntry() != this.lastEntry)
            {
                if (this.pokemob == null) this.pokemob = PokemobCaps.getPokemobFor(this.mob);
                this.lastEntry = this.pokemob.getPokedexEntry();
                this.forme = this.pokemob.getPokedexEntry().getTrimmedName();
            }
            if (this.pokemob.getCustomHolder() != null) this.forme = this.pokemob.getCustomHolder().key.getPath();
            return this.forme;
        }
    }

    private static final List<String> STATES = Lists.newArrayList();

    public static IMobTexturable forMob(final Entity mob)
    {
        return ThutCaps.getTexturable(mob);
    }
}

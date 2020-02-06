package pokecube.core.interfaces.capabilities;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Lists;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import thut.api.entity.IMobTexturable;
import thut.core.common.ThutCore;

public class TextureableCaps
{
    public static class NPCCap<E extends LivingEntity> implements IMobTexturable, ICapabilityProvider
    {
        private final LazyOptional<IMobTexturable>      holder = LazyOptional.of(() -> this);
        E                                               mob;
        String                                          modid;
        public Function<LivingEntity, ResourceLocation> texGetter;
        public Function<LivingEntity, Boolean>          slim;

        public NPCCap()
        {
        }

        @SuppressWarnings("unchecked")
        public NPCCap(final E mob, final Function<E, ResourceLocation> texGetter, final Function<E, Boolean> slim)
        {
            this();
            this.mob = mob;
            this.modid = this.mob.getType().getRegistryName().getNamespace();
            this.texGetter = (Function<LivingEntity, ResourceLocation>) texGetter;
            this.slim = (Function<LivingEntity, Boolean>) slim;
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return TextureableCaps.CAPABILITY.orEmpty(cap, this.holder);
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
        EntityPokemob                              mob;
        IPokemob                                   pokemob;

        public PokemobCap()
        {
        }

        public PokemobCap(final EntityPokemob mob)
        {
            this();
            this.mob = mob;
            this.pokemob = CapabilityPokemob.getPokemobFor(mob);
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return TextureableCaps.CAPABILITY.orEmpty(cap, this.holder);
        }

        @Override
        public LivingEntity getEntity()
        {
            return this.mob;
        }

        @Override
        public String getModId()
        {
            if (this.pokemob == null) this.pokemob = CapabilityPokemob.getPokemobFor(this.mob);
            return this.pokemob.getPokedexEntry().getModId();
        }

        @Override
        public ResourceLocation getTexture(final String part)
        {
            if (this.pokemob == null) this.pokemob = CapabilityPokemob.getPokemobFor(this.mob);
            return this.pokemob.getTexture();
        }

        @Override
        public List<String> getTextureStates()
        {
            return TextureableCaps.STATES;
        }

        @Override
        public ResourceLocation preApply(final ResourceLocation in)
        {
            if (this.pokemob == null) this.pokemob = CapabilityPokemob.getPokemobFor(this.mob);
            return this.pokemob.modifyTexture(in);
        }

    }

    @CapabilityInject(IMobTexturable.class)
    public static final Capability<IMobTexturable> CAPABILITY = null;
    private static final List<String>              STATES     = Lists.newArrayList();

    {
        for (final GeneralStates state : GeneralStates.values())
            TextureableCaps.STATES.add(ThutCore.trim(state.name()));
        for (final LogicStates state : LogicStates.values())
            TextureableCaps.STATES.add(ThutCore.trim(state.name()));
        for (final CombatStates state : CombatStates.values())
            TextureableCaps.STATES.add(ThutCore.trim(state.name()));
    }
}

package pokecube.core.interfaces.capabilities;

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
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import thut.api.ThutCaps;
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
        EntityPokemob                              mob;
        public IPokemob                            pokemob;
        String                                     forme;
        List<String>                               states = Lists.newArrayList();

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
            return ThutCaps.MOBTEX_CAP.orEmpty(cap, this.holder);
        }

        @Override
        public LivingEntity getEntity()
        {
            if (this.pokemob == null) this.pokemob = CapabilityPokemob.getPokemobFor(this.mob);
            return this.mob;
        }

        @Override
        public int getRandomSeed()
        {
            if (this.pokemob == null) this.pokemob = CapabilityPokemob.getPokemobFor(this.mob);
            return this.pokemob.getRNGValue();
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
            if (this.pokemob != null)
            {
                this.states.clear();
                for (final GeneralStates state : GeneralStates.values())
                    if (this.pokemob.getGeneralState(state)) this.states.add(ThutCore.trim(state.name()));
                for (final LogicStates state : LogicStates.values())
                    if (this.pokemob.getLogicState(state)) this.states.add(ThutCore.trim(state.name()));
                for (final CombatStates state : CombatStates.values())
                    if (this.pokemob.getCombatState(state)) this.states.add(ThutCore.trim(state.name()));
            }
            return this.states;
        }

        @Override
        public ResourceLocation preApply(final ResourceLocation in)
        {
            if (this.pokemob == null) this.pokemob = CapabilityPokemob.getPokemobFor(this.mob);
            return this.pokemob.modifyTexture(in);
        }

        PokedexEntry lastEntry = null;

        @Override
        public String getForm()
        {
            if (this.forme == null || this.pokemob == null || this.pokemob.getPokedexEntry() != this.lastEntry)
            {
                if (this.pokemob == null) this.pokemob = CapabilityPokemob.getPokemobFor(this.mob);
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
        if (mob == null) return null;
        return mob.getCapability(ThutCaps.MOBTEX_CAP).orElse(null);
    }
}

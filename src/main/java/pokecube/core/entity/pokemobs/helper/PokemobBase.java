package pokecube.core.entity.pokemobs.helper;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.logic.LogicMiscUpdate;
import pokecube.core.interfaces.capabilities.DefaultPokemob;
import pokecube.core.interfaces.capabilities.PokemobCaps;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import thut.api.entity.IMobColourable;

public abstract class PokemobBase extends ShoulderRidingEntity implements IEntityAdditionalSpawnData, FlyingAnimal,
        IMobColourable
{
    public final DefaultPokemob pokemobCap;

    public PokemobBase(final EntityType<? extends ShoulderRidingEntity> type, final Level worldIn)
    {
        super(type, worldIn);
        final DefaultPokemob cap = (DefaultPokemob) this.getCapability(PokemobCaps.POKEMOB_CAP, null).orElse(null);
        this.pokemobCap = cap == null ? new DefaultPokemob(this) : cap;
        this.dimensions = EntityDimensions.fixed(cap.getPokedexEntry().width, cap.getPokedexEntry().height);
        this.setPersistenceRequired();
    }

    @Override
    public float getScale()
    {
        float size = this.pokemobCap.getSize();
        if (this.pokemobCap.getGeneralState(GeneralStates.EXITINGCUBE))
        {
            float scale = 1;
            scale = Math.min(1, (this.tickCount + 1) / (float) LogicMiscUpdate.EXITCUBEDURATION);
            size = Math.max(0.01f, size * scale);
        }
        if (this.pokemobCap.getCombatState(CombatStates.DYNAMAX))
        {
            // Since we don't change hitbox, we need toset this here.
            this.noCulling = true;
            size = (float) (PokecubeCore.getConfig().dynamax_scale / this.pokemobCap.getMobSizes().y);
        }
        // Reset this if we set it from dynamaxing
        else if (this.getParts().length == 0) this.noCulling = false;
        return size;
    }

    @Override
    public EntityDimensions getDimensions(final Pose poseIn)
    {
        return this.dimensions.scale(this.getScale());
    }
}
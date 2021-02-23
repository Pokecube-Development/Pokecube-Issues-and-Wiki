package pokecube.core.entity.pokemobs.helper;

import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.passive.IFlyingAnimal;
import net.minecraft.entity.passive.ShoulderRidingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.logic.LogicMiscUpdate;
import pokecube.core.interfaces.capabilities.DefaultPokemob;
import pokecube.core.interfaces.capabilities.PokemobCaps;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import thut.api.entity.IMobColourable;

public abstract class PokemobBase extends ShoulderRidingEntity implements IEntityAdditionalSpawnData, IFlyingAnimal,
        IMobColourable
{
    public final DefaultPokemob pokemobCap;

    public PokemobBase(final EntityType<? extends ShoulderRidingEntity> type, final World worldIn)
    {
        super(type, worldIn);
        final DefaultPokemob cap = (DefaultPokemob) this.getCapability(PokemobCaps.POKEMOB_CAP, null).orElse(null);
        this.pokemobCap = cap == null ? new DefaultPokemob(this) : cap;
        this.size = EntitySize.fixed(cap.getPokedexEntry().width, cap.getPokedexEntry().height);
        this.enablePersistence();
    }

    @Override
    public float getRenderScale()
    {
        float size = (float) (this.pokemobCap.getSize() * PokecubeCore.getConfig().scalefactor);
        if (this.pokemobCap.getGeneralState(GeneralStates.EXITINGCUBE))
        {
            float scale = 1;
            scale = Math.min(1, (this.ticksExisted + 1) / (float) LogicMiscUpdate.EXITCUBEDURATION);
            size = Math.max(0.01f, size * scale);
        }
        if (this.pokemobCap.getCombatState(CombatStates.DYNAMAX))
        {
            // Since we don't change hitbox, we need toset this here.
            this.ignoreFrustumCheck = true;
            size = (float) (PokecubeCore.getConfig().dynamax_scale / this.pokemobCap.getMobSizes().y);
        }
        // Reset this if we set it from dynamaxing
        else if (this.getParts().length == 0) this.ignoreFrustumCheck = false;
        return size;
    }

    @Override
    public EntitySize getSize(final Pose poseIn)
    {
        return this.size.scale(this.getRenderScale());
    }

    abstract boolean attackFromPart(final PokemobPart pokemobPart, final DamageSource source, final float amount);
}

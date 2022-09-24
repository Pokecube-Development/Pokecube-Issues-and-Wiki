package pokecube.core.entity.pokemobs.helper;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.logic.LogicMiscUpdate;
import pokecube.core.impl.capabilities.DefaultPokemob;
import thut.api.entity.IMobColourable;

public abstract class PokemobBase extends TamableAnimal
        implements IEntityAdditionalSpawnData, FlyingAnimal, IMobColourable, InventoryCarrier
{
    public final DefaultPokemob pokemobCap;

    public PokemobBase(final EntityType<? extends TamableAnimal> type, final Level worldIn)
    {
        super(type, worldIn);
        final DefaultPokemob cap = (DefaultPokemob) this.getCapability(PokemobCaps.POKEMOB_CAP, null).orElse(null);
        this.pokemobCap = cap == null ? new DefaultPokemob(this) : cap;
        this.dimensions = EntityDimensions.fixed(cap.getPokedexEntry().width, cap.getPokedexEntry().height);
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
        else if (this.pokemobCap.getCombatState(CombatStates.DYNAMAX))
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

    @Override
    public SimpleContainer getInventory()
    {
        return pokemobCap.getInventory();
    }
}
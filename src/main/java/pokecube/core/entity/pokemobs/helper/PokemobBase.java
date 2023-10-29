package pokecube.core.entity.pokemobs.helper;

import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import pokecube.api.entity.pokemob.PokemobCaps;
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
    public EntityDimensions getDimensions(final Pose poseIn)
    {
        return this.dimensions.scale(this.getScale());
    }

    @Override
    public Container getInventory()
    {
        return pokemobCap.getInventory();
    }
}
package pokecube.adventures.entity.trainer;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.capabilities.utils.TypeTrainer;

public class EntityLeader extends EntityTrainer
{
    public static final EntityType<EntityLeader> TYPE;

    static
    {
        TYPE = EntityType.Builder.create(EntityLeader::new, EntityClassification.CREATURE).setCustomClientFactory((s,
                w) -> EntityLeader.TYPE.create(w)).build("trainer");
    }

    private boolean randomBadge = false;

    public EntityLeader(final EntityType<? extends TrainerBase> type, final World worldIn)
    {
        super(type, worldIn);
        this.enablePersistence();
        this.pokemobsCap.setType(TypeTrainer.mobTypeMapper.getType(this, true));
        this.aiStates.setAIState(IHasNPCAIStates.STATIONARY, true);
        this.aiStates.setAIState(IHasNPCAIStates.TRADES, false);
        this.pokemobsCap.resetTime = 0;
    }

    @Override
    public void writeAdditional(final CompoundNBT nbt)
    {
        super.writeAdditional(nbt);
        nbt.putBoolean("randomBadge", this.randomBadge);
    }

    @Override
    public void readAdditional(final CompoundNBT nbt)
    {
        super.readAdditional(nbt);
        this.randomBadge = nbt.getBoolean("randomBadge");
    }

    public boolean randomBadge()
    {
        return this.randomBadge;
    }
}

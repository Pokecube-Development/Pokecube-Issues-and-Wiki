package thut.api.entity.blockentity;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.entity.PartEntity;

public class BlockEntityBasePart extends PartEntity<BlockEntityBase>
{

    public BlockEntityBasePart(BlockEntityBase parent, final String id)
    {
        super(parent);
    }
    

    @Override
    protected void defineSynchedData()
    {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag p_20052_)
    {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag p_20139_)
    {
    }

}

package thut.api.entity.blockentity;

import thut.api.entity.multipart.GenericPartEntity;

public class BlockEntityBasePart extends GenericPartEntity<BlockEntityBase>
{

    public BlockEntityBasePart(BlockEntityBase parent, float width, float height, float x, float y, float z, String id)
    {
        super(parent, width, height, x, y, z, id);
    }
    
}

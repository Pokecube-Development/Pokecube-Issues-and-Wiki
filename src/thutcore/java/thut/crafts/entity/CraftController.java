package thut.crafts.entity;

import thut.api.entity.blockentity.world.IBlockEntityWorld;

public class CraftController
{
    public boolean leftInputDown    = false;
    public boolean rightInputDown   = false;
    public boolean forwardInputDown = false;
    public boolean backInputDown    = false;
    public boolean leftRotateDown   = false;
    public boolean rightRotateDown  = false;
    public boolean upInputDown      = false;
    public boolean downInputDown    = false;

    final EntityCraft entity;

    public CraftController(final EntityCraft entityCraft)
    {
        this.entity = entityCraft;
    }

    public void doServerTick(final IBlockEntityWorld iBlockEntityWorld)
    {
        if (!this.entity.isVehicle()) return;

        if (this.leftRotateDown || this.rightRotateDown) this.entity.yRotO = this.entity.getYRot();
        // this.entity.prevRenderYawOffset = this.entity.renderYawOffset;
        // TODO craft rotation
//        if (this.leftRotateDown)
//        {
//            for (final Entity passenger : this.entity.getPassengers())
//                passenger.yRot -= 5;
//            this.entity.yRot -= 5;
//        }
//        if (this.rightRotateDown)
//        {
//            for (final Entity passenger : this.entity.getPassengers())
//                passenger.yRot += 5;
//            this.entity.yRot += 5;
//        }
    }
}

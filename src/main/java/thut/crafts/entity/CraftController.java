package thut.crafts.entity;

import net.minecraft.entity.Entity;
import thut.api.entity.blockentity.world.client.IBlockEntityWorld;

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

    public void doServerTick(final IBlockEntityWorld<?> iBlockEntityWorld)
    {
        if (!this.entity.isBeingRidden()) return;

        if (this.leftRotateDown || this.rightRotateDown) this.entity.prevRotationYaw = this.entity.rotationYaw;
        // this.entity.prevRenderYawOffset = this.entity.renderYawOffset;

        if (this.leftRotateDown)
        {
            for (final Entity passenger : this.entity.getPassengers())
                passenger.rotationYaw -= 5;
            this.entity.rotationYaw -= 5;
        }
        if (this.rightRotateDown)
        {
            for (final Entity passenger : this.entity.getPassengers())
                passenger.rotationYaw += 5;
            this.entity.rotationYaw += 5;
        }
    }
}

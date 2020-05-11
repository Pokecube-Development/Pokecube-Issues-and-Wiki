package pokecube.core.utils;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import pokecube.core.interfaces.IPokemob;

public class EntityTools
{
    public static void copyEntityData(final LivingEntity to, final LivingEntity from)
    {
        final CompoundNBT tag = new CompoundNBT();
        from.writeAdditional(tag);
        to.readAdditional(tag);
    }

    public static void copyEntityTransforms(final LivingEntity to, final LivingEntity from)
    {
        to.rotationPitch = from.rotationPitch;
        to.ticksExisted = from.ticksExisted;
        to.rotationYaw = from.rotationYaw;
        to.setRotationYawHead(from.getRotationYawHead());
        to.prevRotationPitch = from.prevRotationPitch;
        to.prevRotationYaw = from.prevRotationYaw;
        to.prevRotationYawHead = from.prevRotationYawHead;
        to.prevRenderYawOffset = from.prevRenderYawOffset;
        to.renderYawOffset = from.renderYawOffset;

        to.onGround = from.onGround;

        to.prevLimbSwingAmount = from.prevLimbSwingAmount;
        to.limbSwing = from.limbSwing;
        to.limbSwingAmount = from.limbSwingAmount;
    }

    public static void copyPokemobData(final IPokemob from, final IPokemob to)
    {
        to.read(from.write());
    }
}

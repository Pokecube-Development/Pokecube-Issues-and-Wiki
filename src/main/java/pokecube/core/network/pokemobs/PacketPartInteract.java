package pokecube.core.network.pokemobs;

import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.entity.PartEntity;
import pokecube.core.entity.pokemobs.helper.PokemobPart;
import thut.core.common.network.Packet;

/**
 * This is a custom implementation of CUseEntityPacket, to support pokemob parts
 * more properly.
 */
public class PacketPartInteract extends Packet
{
    int                             entityId;
    boolean                         sneaking;
    private Vector3d                hitVec;
    private CUseEntityPacket.Action action;
    private Hand                    hand;

    private String id;

    public PacketPartInteract()
    {
        super(null);
    }

    public PacketPartInteract(final String name, final Entity entityIn, final boolean sneak)
    {
        this.entityId = entityIn.getId();
        this.action = CUseEntityPacket.Action.ATTACK;
        this.sneaking = sneak;
        this.id = name;
    }

    public PacketPartInteract(final String name, final Entity entityIn, final Hand handIn, final boolean sneak)
    {
        this.entityId = entityIn.getId();
        this.action = CUseEntityPacket.Action.INTERACT;
        this.hand = handIn;
        this.sneaking = sneak;
        this.id = name;
    }

    public PacketPartInteract(final String name, final Entity entityIn, final Hand handIn, final Vector3d hitVecIn,
            final boolean sneak)
    {
        this.entityId = entityIn.getId();
        this.action = CUseEntityPacket.Action.INTERACT_AT;
        this.hand = handIn;
        this.hitVec = hitVecIn;
        this.sneaking = sneak;
        this.id = name;
    }

    public PacketPartInteract(final PacketBuffer buf)
    {
        this.entityId = buf.readVarInt();
        this.action = buf.readEnum(CUseEntityPacket.Action.class);
        if (this.action == CUseEntityPacket.Action.INTERACT_AT) this.hitVec = new Vector3d(buf.readFloat(), buf
                .readFloat(), buf.readFloat());
        if (this.action == CUseEntityPacket.Action.INTERACT || this.action == CUseEntityPacket.Action.INTERACT_AT)
            this.hand = buf.readEnum(Hand.class);
        this.sneaking = buf.readBoolean();
        this.id = buf.readUtf(32767);
    }

    @Override
    public void write(final PacketBuffer buf)
    {
        buf.writeVarInt(this.entityId);
        buf.writeEnum(this.action);
        if (this.action == CUseEntityPacket.Action.INTERACT_AT)
        {
            buf.writeFloat((float) this.hitVec.x);
            buf.writeFloat((float) this.hitVec.y);
            buf.writeFloat((float) this.hitVec.z);
        }
        if (this.action == CUseEntityPacket.Action.INTERACT || this.action == CUseEntityPacket.Action.INTERACT_AT) buf
                .writeEnum(this.hand);
        buf.writeBoolean(this.sneaking);
        buf.writeUtf(this.id);
    }

    @Nullable
    public Entity getEntityFromWorld(final World worldIn)
    {
        return worldIn.getEntity(this.entityId);
    }

    public CUseEntityPacket.Action getAction()
    {
        return this.action;
    }

    @Nullable
    public Hand getHand()
    {
        return this.hand;
    }

    public Vector3d getHitVec()
    {
        return this.hitVec;
    }

    public boolean isSneaking()
    {
        return this.sneaking;
    }

    @Override
    public void handleServer(final ServerPlayerEntity player)
    {
        final ServerWorld serverworld = player.getLevel();
        Entity entity = this.getEntityFromWorld(serverworld);

        // Most of the stuff from here is copied from CUseEntityPacket!

        player.resetLastActionTime();
        player.setShiftKeyDown(this.isSneaking());

        if (entity != null)
        {
            // Convert to the relevant part if found.
            if (entity.isMultipartEntity()) for (final PartEntity<?> p : entity.getParts())
                if (p instanceof PokemobPart && ((PokemobPart) p).id.equals(this.id))
                {
                    entity = p;
                    break;
                }
            // Do this before checking distance stuff, as the mobs can be
            // sufficiently large that the distance check fails otherwise.

            final double d0 = 36.0D;
            if (player.distanceToSqr(entity) < d0)
            {
                final Hand hand = this.getHand();
                final ItemStack itemstack = hand != null ? player.getItemInHand(hand).copy() : ItemStack.EMPTY;
                Optional<ActionResultType> optional = Optional.empty();

                if (this.getAction() == CUseEntityPacket.Action.INTERACT) optional = Optional.of(player.interactOn(
                        entity, hand));
                else if (this.getAction() == CUseEntityPacket.Action.INTERACT_AT)
                {
                    if (net.minecraftforge.common.ForgeHooks.onInteractEntityAt(player, entity, this.getHitVec(),
                            hand) != null) return;
                    optional = Optional.of(entity.interactAt(player, this.getHitVec(), hand));
                }
                else if (this.getAction() == CUseEntityPacket.Action.ATTACK) player.attack(
                        entity);

                if (optional.isPresent() && optional.get().consumesAction())
                {
                    CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.trigger(player, itemstack, entity);
                    if (optional.get().shouldSwing()) player.swing(hand, true);
                }
            }
        }
    }

}

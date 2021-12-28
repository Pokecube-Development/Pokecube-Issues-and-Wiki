package thut.core.common.network;

import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import thut.api.entity.GenericPartEntity;

/**
 * This is a custom implementation of CUseEntityPacket, to support pokemob parts
 * more properly.
 */
public class PacketPartInteract extends Packet
{
    int entityId;
    boolean sneaking;
    private Vec3 hitVec;
    private ServerboundInteractPacket.ActionType action;
    private InteractionHand hand;

    private String id;

    public PacketPartInteract()
    {
        super(null);
    }

    public PacketPartInteract(final String name, final Entity entityIn, final boolean sneak)
    {
        this.entityId = entityIn.getId();
        this.action = ServerboundInteractPacket.ActionType.ATTACK;
        this.sneaking = sneak;
        this.id = name;
    }

    public PacketPartInteract(final String name, final Entity entityIn, final InteractionHand handIn,
            final boolean sneak)
    {
        this.entityId = entityIn.getId();
        this.action = ServerboundInteractPacket.ActionType.INTERACT;
        this.hand = handIn;
        this.sneaking = sneak;
        this.id = name;
    }

    public PacketPartInteract(final String name, final Entity entityIn, final InteractionHand handIn,
            final Vec3 hitVecIn, final boolean sneak)
    {
        this.entityId = entityIn.getId();
        this.action = ServerboundInteractPacket.ActionType.INTERACT_AT;
        this.hand = handIn;
        this.hitVec = hitVecIn;
        this.sneaking = sneak;
        this.id = name;
    }

    public PacketPartInteract(final FriendlyByteBuf buf)
    {
        this.entityId = buf.readVarInt();
        this.action = buf.readEnum(ServerboundInteractPacket.ActionType.class);
        if (this.action == ServerboundInteractPacket.ActionType.INTERACT_AT)
            this.hitVec = new Vec3(buf.readFloat(), buf.readFloat(), buf.readFloat());
        if (this.action == ServerboundInteractPacket.ActionType.INTERACT
                || this.action == ServerboundInteractPacket.ActionType.INTERACT_AT)
            this.hand = buf.readEnum(InteractionHand.class);
        this.sneaking = buf.readBoolean();
        this.id = buf.readUtf(32767);
    }

    @Override
    public void write(final FriendlyByteBuf buf)
    {
        buf.writeVarInt(this.entityId);
        buf.writeEnum(this.action);
        if (this.action == ServerboundInteractPacket.ActionType.INTERACT_AT)
        {
            buf.writeFloat((float) this.hitVec.x);
            buf.writeFloat((float) this.hitVec.y);
            buf.writeFloat((float) this.hitVec.z);
        }
        if (this.action == ServerboundInteractPacket.ActionType.INTERACT
                || this.action == ServerboundInteractPacket.ActionType.INTERACT_AT)
            buf.writeEnum(this.hand);
        buf.writeBoolean(this.sneaking);
        buf.writeUtf(this.id);
    }

    @Nullable
    public Entity getEntityFromWorld(final Level worldIn)
    {
        return worldIn.getEntity(this.entityId);
    }

    public ServerboundInteractPacket.ActionType getAction()
    {
        return this.action;
    }

    @Nullable
    public InteractionHand getHand()
    {
        return this.hand;
    }

    public Vec3 getHitVec()
    {
        return this.hitVec;
    }

    public boolean isSneaking()
    {
        return this.sneaking;
    }

    @Override
    public void handleServer(final ServerPlayer player)
    {
        final ServerLevel serverworld = player.getLevel();
        Entity entity = this.getEntityFromWorld(serverworld);

        // Most of the stuff from here is copied from CUseEntityPacket!

        player.resetLastActionTime();
        player.setShiftKeyDown(this.isSneaking());

        if (entity != null)
        {
            // Convert to the relevant part if found.
            if (entity.isMultipartEntity()) for (final PartEntity<?> p : entity.getParts())
                if (p instanceof GenericPartEntity<?> p2 && p2.id.equals(this.id))
            {
                entity = p;
                break;
            }
            // Do this before checking distance stuff, as the mobs can be
            // sufficiently large that the distance check fails otherwise.

            final double d0 = 36.0D;
            if (player.distanceToSqr(entity) < d0)
            {
                final InteractionHand hand = this.getHand();
                final ItemStack itemstack = hand != null ? player.getItemInHand(hand).copy() : ItemStack.EMPTY;
                Optional<InteractionResult> optional = Optional.empty();

                if (this.getAction() == ServerboundInteractPacket.ActionType.INTERACT)
                    optional = Optional.of(player.interactOn(entity, hand));
                else if (this.getAction() == ServerboundInteractPacket.ActionType.INTERACT_AT)
                {
                    if (net.minecraftforge.common.ForgeHooks.onInteractEntityAt(player, entity, this.getHitVec(),
                            hand) != null)
                        return;
                    optional = Optional.of(entity.interactAt(player, this.getHitVec(), hand));
                }
                else if (this.getAction() == ServerboundInteractPacket.ActionType.ATTACK) player.attack(entity);

                if (optional.isPresent() && optional.get().consumesAction())
                {
                    CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.trigger(player, itemstack, entity);
                    if (optional.get().shouldSwing()) player.swing(hand, true);
                }
            }
        }
    }

}

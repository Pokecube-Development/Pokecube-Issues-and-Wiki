package pokecube.core.inventory.pc;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.inventory.big.BigInventory;
import thut.api.inventory.big.Manager;
import thut.lib.TComponent;

public class PCInventory extends BigInventory
{
    public static boolean addPokecubeToPC(final ItemStack mob, final Level world)
    {
        if (!PokecubeManager.isFilled(mob)) return false;
        final UUID id = PokecubeManager.getOwnerId(mob);
        if (id != null)
        {
            if (id.equals(PokecubeMod.fakeUUID)) return false;
            return PCInventory.addStackToPC(id, mob, world);
        }
        return false;
    }

    public static boolean addStackToPC(final UUID uuid, final ItemStack mob, final Level world)
    {
        if (uuid == null || mob.isEmpty())
        {
            System.err.println("Could not find the owner of this item " + mob + " " + uuid);
            return false;
        }
        final PCInventory pc = PCInventory.getPC(uuid);

        if (pc == null) return false;

        if (PokecubeManager.isFilled(mob))
        {
            final ItemStack stack = mob;
            if (world != null) PokecubeManager.heal(stack, world, false);
            PlayerPokemobCache.UpdateCache(mob, true, false);
            Player player = PokecubeCore.proxy.getPlayer(uuid);
            if (player != null) thut.lib.ChatHelper.sendSystemMessage(player,
                    TComponent.translatable("block.pc.sentto", mob.getHoverName()));
        }
        pc.addItem(mob.copy());
        return true;
    }

    public static PCInventory getPC(final Entity player)
    {
        return PCInventory.getPC(player.getUUID());
    }

    public static PCInventory getPC(final UUID uuid)
    {
        return PCManager.INSTANCE.get(uuid);
    }

    private boolean autoToPC = false;
    private boolean seenOwner = false;

    public PCInventory(final Manager<? extends BigInventory> manager, final UUID id)
    {
        super(manager, id);
    }

    public PCInventory(final Manager<? extends BigInventory> manager, final CompoundTag tag)
    {
        super(manager, tag);
        CompoundTag boxes = tag.getCompound("boxes");
        this.deserializeBoxInfo(boxes);
    }

    public PCInventory(final Manager<? extends BigInventory> manager, final FriendlyByteBuf buffer)
    {
        super(manager, buffer);
        if (buffer != null)
        {
            buffer.resetReaderIndex();
            CompoundTag tag = buffer.readNbt();
            CompoundTag boxes = tag.getCompound("boxes");
            this.deserializeBoxInfo(boxes);
        }
    }

    @Override
    public int boxCount()
    {
        return PokecubeCore.getConfig().pcPageCount;
    }

    @Override
    public void serializeBoxInfo(final CompoundTag boxes)
    {
        super.serializeBoxInfo(boxes);
        boxes.putBoolean("seenOwner", this.hasSeenOwner());
        boxes.putBoolean("autoSend", this.isAutoToPC());
    }

    @Override
    public void deserializeBoxInfo(final CompoundTag boxes)
    {
        super.deserializeBoxInfo(boxes);
        this.setAutoToPC(boxes.getBoolean("autoSend"));
        this.setSeenOwner(boxes.getBoolean("seenOwner"));
    }

    public boolean isAutoToPC()
    {
        return autoToPC;
    }

    public void setAutoToPC(boolean autoToPC)
    {
        this.autoToPC = autoToPC;
    }

    public boolean hasSeenOwner()
    {
        return seenOwner;
    }

    public void setSeenOwner(boolean seenOwner)
    {
        this.seenOwner = seenOwner;
    }

}

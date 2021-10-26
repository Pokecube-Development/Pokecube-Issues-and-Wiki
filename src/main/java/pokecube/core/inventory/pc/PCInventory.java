package pokecube.core.inventory.pc;

import java.util.UUID;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.inventory.big.BigInventory;
import thut.api.inventory.big.Manager;

public class PCInventory extends BigInventory
{
    public static void addPokecubeToPC(final ItemStack mob, final Level world)
    {
        if (!PokecubeManager.isFilled(mob)) return;
        final UUID id = PokecubeManager.getOwnerId(mob);
        if (id != null) PCInventory.addStackToPC(id, mob, world);
    }

    public static void addStackToPC(final UUID uuid, final ItemStack mob, final Level world)
    {
        if (uuid == null || mob.isEmpty())
        {
            System.err.println("Could not find the owner of this item " + mob + " " + uuid);
            return;
        }
        final PCInventory pc = PCInventory.getPC(uuid);

        if (pc == null) return;

        if (PokecubeManager.isFilled(mob))
        {
            final ItemStack stack = mob;
            if (world != null) PokecubeManager.heal(stack, world);
            PlayerPokemobCache.UpdateCache(mob, true, false);
            if (PokecubeCore.proxy.getPlayer(uuid) != null) PokecubeCore.proxy.getPlayer(uuid).sendMessage(
                    new TranslatableComponent("block.pc.sentto", mob.getHoverName()), Util.NIL_UUID);
        }
        pc.addItem(mob.copy());
    }

    public static PCInventory getPC(final Entity player)
    {
        return PCInventory.getPC(player.getUUID());
    }

    public static PCInventory getPC(final UUID uuid)
    {
        return PCManager.INSTANCE.get(uuid);
    }

    public boolean autoToPC  = false;
    public boolean seenOwner = false;

    public PCInventory(final Manager<? extends BigInventory> manager, final UUID id)
    {
        super(manager, id);
    }

    public PCInventory(final Manager<? extends BigInventory> manager, final CompoundTag tag)
    {
        super(manager, tag);
    }

    public PCInventory(final Manager<? extends BigInventory> manager, final FriendlyByteBuf buffer)
    {
        super(manager, buffer);
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
        boxes.putBoolean("seenOwner", this.seenOwner);
        boxes.putBoolean("autoSend", this.autoToPC);
    }

    @Override
    public void deserializeBoxInfo(final CompoundTag boxes)
    {
        super.deserializeBoxInfo(boxes);
        this.autoToPC = boxes.getBoolean("autoSend");
        this.seenOwner = boxes.getBoolean("seenOwner");
    }

}

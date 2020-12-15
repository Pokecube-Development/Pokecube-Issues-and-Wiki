package pokecube.core.inventory.pc;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.inventory.big.BigInventory;
import thut.api.inventory.big.Manager;

public class PCInventory extends BigInventory
{
    public static void addPokecubeToPC(final ItemStack mob, final World world)
    {
        if (!PokecubeManager.isFilled(mob)) return;
        final String player = PokecubeManager.getOwner(mob);
        UUID id;
        try
        {
            id = UUID.fromString(player);
            PCInventory.addStackToPC(id, mob, world);
        }
        catch (final Exception e)
        {

        }
    }

    public static void addStackToPC(final UUID uuid, final ItemStack mob, final World world)
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
                    new TranslationTextComponent("block.pc.sentto", mob.getDisplayName()), Util.DUMMY_UUID);
        }
        pc.addItem(mob.copy());
    }

    public static PCInventory getPC(final Entity player)
    {
        return PCInventory.getPC(player.getUniqueID());
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

    public PCInventory(final Manager<? extends BigInventory> manager, final CompoundNBT tag)
    {
        super(manager, tag);
    }

    public PCInventory(final Manager<? extends BigInventory> manager, final PacketBuffer buffer)
    {
        super(manager, buffer);
    }

    @Override
    public int boxCount()
    {
        return PokecubeCore.getConfig().pcPageCount;
    }

    @Override
    public void serializeBoxInfo(final CompoundNBT boxes)
    {
        super.serializeBoxInfo(boxes);
        boxes.putBoolean("seenOwner", this.seenOwner);
        boxes.putBoolean("autoSend", this.autoToPC);
    }

    @Override
    public void deserializeBoxInfo(final CompoundNBT boxes)
    {
        super.deserializeBoxInfo(boxes);
        this.autoToPC = boxes.getBoolean("autoSend");
        this.seenOwner = boxes.getBoolean("seenOwner");
    }

}

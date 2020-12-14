package pokecube.pokeplayer.data;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.packets.PacketDataSync;
import thut.api.world.mobs.data.DataSync;
import thut.core.common.handlers.PlayerDataHandler.PlayerData;
import thut.core.common.world.mobs.data.SyncHandler;

public class PokeInfo extends PlayerData
{
    private ItemStack stack = ItemStack.EMPTY;

    private IPokemob pokemob = null;

    private boolean attached = false;

    public PokeInfo()
    {
    }

    public void onAttach(final LivingEntity player)
    {
        if (this.attached) return;
        if (PokecubeManager.isFilled(this.stack))
        {
            this.attached = true;
            this.pokemob = PokecubeManager.itemToPokemob(this.stack, player.getEntityWorld());
            this.pokemob.getEntity().getPersistentData().putBoolean("is_a_player", true);
            this.pokemob.getEntity().setEntityId(player.getEntityId());
            this.pokemob.initAI();
            final DataSync sync = SyncHandler.getData(player);
            if (sync instanceof DataSyncWrapper) ((DataSyncWrapper) sync).wrapped = this.pokemob.dataSync();
            if (player instanceof ServerPlayerEntity) PacketDataSync.sendInitPacket((PlayerEntity) player, this
                    .getIdentifier());
        }
    }

    public ItemStack detach()
    {
        this.attached = false;
        if (this.pokemob == null) return ItemStack.EMPTY;
        this.pokemob.getEntity().getPersistentData().remove("is_a_player");
        return PokecubeManager.pokemobToItem(this.pokemob);
    }

    public void setStack(final ItemStack stack)
    {
        this.stack = stack;
    }

    public IPokemob getPokemob()
    {
        return this.pokemob;
    }

    @Override
    public String dataFileName()
    {
        return "PokePlayer";
    }

    @Override
    public String getIdentifier()
    {
        return "pokeplayer-data";
    }

    @Override
    public void readFromNBT(final CompoundNBT tag)
    {
        this.stack = ItemStack.read(tag);
    }

    @Override
    public boolean shouldSync()
    {
        return true;
    }

    @Override
    public void writeToNBT(final CompoundNBT tag)
    {
        if (this.pokemob != null) this.stack = PokecubeManager.pokemobToItem(this.pokemob);
        this.stack.write(tag);
    }

}

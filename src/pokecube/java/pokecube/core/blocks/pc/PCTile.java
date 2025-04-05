package pokecube.core.blocks.pc;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.inventory.pc.PCInventory;

public class PCTile extends InteractableTile
{
    public UUID boundId = PokecubeMod.fakeUUID;
    public PCInventory inventory = PCInventory.getPC(PokecubeCore.proxy.getRegistries(), this.boundId);

    public PCTile(final BlockPos pos, final BlockState state)
    {
        this(PokecubeItems.PC_TYPE.get(), pos, state);
    }

    public PCTile(final BlockEntityType<?> tileEntityTypeIn, final BlockPos pos, final BlockState state)
    {
        super(tileEntityTypeIn, pos, state);
    }

    @Override
    public void saveAdditional(final CompoundTag compound, HolderLookup.Provider registries)
    {
        if (this.isBound()) compound.putString("boundid", this.boundId.toString());
        super.saveAdditional(compound, registries);
    }

    @Override
    public void loadAdditional(final CompoundTag compound, HolderLookup.Provider registries)
    {
        super.loadAdditional(compound, registries);
        if (compound.contains("boundid")) this.boundId = UUID.fromString(compound.getString("boundid"));
    }

    public boolean isBound()
    {
        return this.boundId != PokecubeMod.fakeUUID;
    }

    public void bind(final ServerPlayer player)
    {
        if (player == null) this.boundId = PokecubeMod.fakeUUID;
        else this.boundId = player.getUUID();
        this.inventory = PCInventory.getPC(player.registryAccess(), this.boundId);
    }

}

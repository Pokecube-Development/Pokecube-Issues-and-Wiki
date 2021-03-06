package pokecube.core.blocks.pc;

import java.util.UUID;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.inventory.pc.PCInventory;

public class PCTile extends InteractableTile
{
    public UUID        boundId   = PokecubeMod.fakeUUID;
    public PCInventory inventory = PCInventory.getPC(this.boundId);

    public PCTile()
    {
        this(PokecubeItems.PC_TYPE.get());
    }

    public PCTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    @Override
    public CompoundNBT save(final CompoundNBT compound)
    {
        if (this.isBound()) compound.putString("boundid", this.boundId.toString());
        return super.save(compound);
    }

    @Override
    public void load(final BlockState state, final CompoundNBT compound)
    {
        super.load(state, compound);
        if (compound.contains("boundid")) this.boundId = UUID.fromString(compound.getString("boundid"));
    }

    public boolean isBound()
    {
        return this.boundId != PokecubeMod.fakeUUID;
    }

    public void bind(final ServerPlayerEntity player)
    {
        if (player == null) this.boundId = PokecubeMod.fakeUUID;
        else this.boundId = player.getUUID();
        this.inventory = PCInventory.getPC(this.boundId);
    }

}

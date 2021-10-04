package pokecube.core.blocks.pc;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
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

    public PCTile(final BlockEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    @Override
    public CompoundTag save(final CompoundTag compound)
    {
        if (this.isBound()) compound.putString("boundid", this.boundId.toString());
        return super.save(compound);
    }

    @Override
    public void load(final BlockState state, final CompoundTag compound)
    {
        super.load(state, compound);
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
        this.inventory = PCInventory.getPC(this.boundId);
    }

}

package pokecube.core.blocks.pc;

import java.util.UUID;

import net.minecraft.core.BlockPos;
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

    public PCTile(final BlockPos pos, final BlockState state)
    {
        this(PokecubeItems.PC_TYPE.get(), pos, state);
    }

    public PCTile(final BlockEntityType<?> tileEntityTypeIn, final BlockPos pos, final BlockState state)
    {
        super(tileEntityTypeIn, pos, state);
    }

    @Override
    public CompoundTag save(final CompoundTag compound)
    {
        if (this.isBound()) compound.putString("boundid", this.boundId.toString());
        return super.save(compound);
    }

    @Override
    public void load(final CompoundTag compound)
    {
        super.load(compound);
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

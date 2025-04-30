package pokecube.legends.tileentity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import pokecube.core.blocks.maxspot.MaxTile;
import pokecube.legends.blocks.customblocks.RaidSpawnBlock;
import pokecube.legends.blocks.customblocks.RaidSpawnBlock.State;
import pokecube.legends.init.BlockInit;

import java.util.List;

public class RaidSpawn extends MaxTile
{
    private static final List<BeaconBlockEntity.BeaconBeamSection> empty = ImmutableList.of();

    private final List<BeaconBlockEntity.BeaconBeamSection> normal = Lists.newArrayList();
    private final List<BeaconBlockEntity.BeaconBeamSection> rare = Lists.newArrayList();

    public String type = "random";

    public RaidSpawn(final BlockPos pos, final BlockState state)
    {
        this(BlockInit.RAID_SPAWN_ENTITY.get(), pos, state);
    }

    public RaidSpawn(final BlockEntityType<?> tileEntityTypeIn, final BlockPos pos, final BlockState state)
    {
        super(tileEntityTypeIn, pos, state);
        var seg = new BeaconBlockEntity.BeaconBeamSection(0xFFD40000);
        this.normal.add(seg);
        seg = new BeaconBlockEntity.BeaconBeamSection(0xFFFABD24);
        this.rare.add(seg);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        if (nbt.contains("raid_type")) this.type = nbt.getString("raid_type");
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        nbt.putString("raid_type", type);
    }

    @OnlyIn(Dist.CLIENT)
    public List<BeaconBlockEntity.BeaconBeamSection> getBeamSections()
    {
        final BlockState blocks = this.level.getBlockState(this.getBlockPos());
        if (!blocks.hasProperty(RaidSpawnBlock.ACTIVE)) return RaidSpawn.empty;
        final State state = blocks.getValue(RaidSpawnBlock.ACTIVE);
        return switch (state)
        {
            case EMPTY -> RaidSpawn.empty;
            case NORMAL -> this.normal;
            case RARE -> this.rare;
        };
    }
}

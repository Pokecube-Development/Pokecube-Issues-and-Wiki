package pokecube.core.blocks.barrels;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.core.init.ItemGenerator;

import java.util.HashSet;
import java.util.Set;

public class GenericBarrelTile extends BarrelBlockEntity
{

    public GenericBarrelTile(BlockPos pos, BlockState blockState)
    {
        super(pos, blockState);
    }

    private static final Set<ResourceLocation> CHECKED = new HashSet<>();

    @Override
    public boolean isValidBlockState(BlockState state)
    {
        if (CHECKED.isEmpty()) for (var v : ItemGenerator.BARRELS) CHECKED.add(v.getId());
        return super.isValidBlockState(state) || CHECKED.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }
}

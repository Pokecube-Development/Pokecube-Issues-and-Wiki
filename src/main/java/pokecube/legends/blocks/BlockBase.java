package pokecube.legends.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;
import pokecube.core.PokecubeItems;
import pokecube.legends.Reference;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.ItemInit;

public class BlockBase extends Block
{
    VoxelShape customShape = null;

    public BlockBase(final String name, final Material material, final float hardnessresistance, final SoundType sound)
    {
        this(name, material, hardnessresistance, hardnessresistance, sound);
    }

    public BlockBase(final String name, final Material material, final float hardness, final float resistance,
            final SoundType sound)
    {
        super(Block.Properties.create(material).hardnessAndResistance(hardness, resistance).sound(sound));
        this.initName(name);
    }

    public BlockBase(final String name, final Material material, final ToolType tool, final int level)
    {
        super(Block.Properties.create(material).harvestTool(tool).harvestLevel(level));
        this.initName(name);
    }

    public BlockBase(final String name, final Properties props)
    {
        super(props);
        this.initName(name);
    }

    public BlockBase setShape(final VoxelShape shape)
    {
        this.customShape = shape;
        return this;
    }

    private void initName(final String name)
    {
        this.setRegistryName(Reference.ID, name);
        BlockInit.BLOCKS.add(this);
        ItemInit.ITEMS.add(new BlockItem(this, new Item.Properties().group(PokecubeItems.POKECUBEBLOCKS))
                .setRegistryName(this.getRegistryName()));
    }

    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return this.customShape == null ? VoxelShapes.fullCube() : this.customShape;
    }
}

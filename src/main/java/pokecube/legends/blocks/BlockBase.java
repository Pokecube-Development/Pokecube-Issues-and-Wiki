package pokecube.legends.blocks;

import java.util.List;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

public class BlockBase extends Block
{
    VoxelShape customShape = null;
    String     infoname;
    boolean    hasTextInfo = true;

    public BlockBase(final String name, final Material material, final MaterialColor color, final float hardnessresistance, final SoundType sound, final ToolType tool, final int harvest)
    {
        this(name, material, color, hardnessresistance, hardnessresistance, sound, tool, harvest);
        this.setInfoBlockName(name);
    }

    public BlockBase(final String name, final Material material, final MaterialColor color, final float hardness, final float resistance,
            final SoundType sound, final ToolType tool, final int harvestLevel)
    {
        super(AbstractBlock.Properties.of(material, color).strength(hardness, resistance).sound(sound)
            .harvestTool(tool).harvestLevel(harvestLevel).requiresCorrectToolForDrops());
    }

    public BlockBase(final String name, final Material material, final MaterialColor color, final ToolType tool, final int level)
    {
        super(AbstractBlock.Properties.of(material, color).harvestTool(tool).harvestLevel(level));
    }

    public BlockBase(final String name, final Properties props)
    {
        super(props);
    }

    public BlockBase setShape(final VoxelShape shape)
    {
        this.customShape = shape;
        return this;
    }

    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return this.customShape == null ? VoxelShapes.block() : this.customShape;
    }

    public BlockBase setInfoBlockName(final String infoname)
    {
        this.infoname = infoname;
        return this;
    }

    public BlockBase noInfoBlock()
    {
        this.hasTextInfo = false;
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(final ItemStack stack, final IBlockReader worldIn, final List<ITextComponent> tooltip,
            final ITooltipFlag flagIn)
    {
        if (!this.hasTextInfo) return;
        String message;
        if (Screen.hasShiftDown()) message = I18n.get("legendblock." + this.infoname + ".tooltip");
        else message = I18n.get("pokecube.tooltip.advanced");
        tooltip.add(new TranslationTextComponent(message));
    }

	public int ticksRandomly() {
		return 0;
	}
}

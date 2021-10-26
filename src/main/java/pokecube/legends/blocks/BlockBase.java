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
import net.minecraft.potion.Effect;
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
    boolean    hasTextInfo = false;
    boolean	   hasDropRequired = false;

    //ToolTip
    public BlockBase(final String name, final Material material, final MaterialColor color, final float hardness, final float resistance,
    		final SoundType sound, final ToolType tool, final int harvestLevel, final boolean dropRequired)
    {
    	super(AbstractBlock.Properties.of(material, color).strength(hardness, resistance).sound(sound).harvestTool(tool).harvestLevel(harvestLevel));
        this.infoname = name;
        this.hasTextInfo = true;
        this.hasDropRequired(dropRequired);
    }
    
    // No Tooltip
    public BlockBase(final Material material, final MaterialColor color, final float hardness, final float resistance,
    		final SoundType sound, final ToolType tool, final int harvestLevel, final boolean dropRequired)
    {
    	super(AbstractBlock.Properties.of(material, color).strength(hardness, resistance).sound(sound).harvestTool(tool).harvestLevel(harvestLevel));
        this.hasDropRequired(dropRequired);
    }

    //Vertex
    public BlockBase(String name, Properties props) {
		super(props);
		this.infoname = name;
    	this.hasTextInfo = true;
	}
    
    //Vertex -No ToolTip-
    public BlockBase(Properties props) {
		super(props);
	}

    //Effects -ToolTip-
	public BlockBase(String name, Material material, MaterialColor color, float hardness, float resistance,
			SoundType sound, ToolType tool, int harvestLevel, boolean hadDrop, Effect effects) {
		super(AbstractBlock.Properties.of(material, color).strength(hardness, resistance).sound(sound).harvestTool(tool).harvestLevel(harvestLevel));
		this.infoname = name;
    	this.hasTextInfo = true;
	}
	
	//Effects -No ToolTip-
	public BlockBase(Material material, MaterialColor color, float hardness, float resistance,
			SoundType sound, ToolType tool, int harvestLevel, boolean hadDrop, Effect effects) {
		super(AbstractBlock.Properties.of(material, color).strength(hardness, resistance).sound(sound).harvestTool(tool).harvestLevel(harvestLevel));
	}

	public BlockBase setToolTip(String infoname) {
		this.infoname = infoname;
    	this.hasTextInfo = true;
		return this;
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
    
    //Drop Required
    public BlockBase hasDropRequired(boolean hasDrop)
    {
        this.hasDropRequired = hasDrop;
        if(hasDropRequired == true) {
        	this.properties.requiresCorrectToolForDrops();
        }
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

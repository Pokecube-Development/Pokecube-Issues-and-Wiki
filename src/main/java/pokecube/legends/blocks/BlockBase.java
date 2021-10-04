package pokecube.legends.blocks;

import java.util.List;

import com.minecolonies.api.util.constant.ToolType;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
    	super(BlockBehaviour.Properties.of(material, color).strength(hardness, resistance).sound(sound).harvestTool(tool).harvestLevel(harvestLevel));
        this.infoname = name;
        this.hasTextInfo = true;
        this.hasDropRequired(dropRequired);
    }
    
    // No Tooltip
    public BlockBase(final Material material, final MaterialColor color, final float hardness, final float resistance,
    		final SoundType sound, final ToolType tool, final int harvestLevel, final boolean dropRequired)
    {
    	super(BlockBehaviour.Properties.of(material, color).strength(hardness, resistance).sound(sound).harvestTool(tool).harvestLevel(harvestLevel));
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
			SoundType sound, ToolType tool, int harvestLevel, boolean hadDrop, MobEffect effects) {
		super(BlockBehaviour.Properties.of(material, color).strength(hardness, resistance).sound(sound).harvestTool(tool).harvestLevel(harvestLevel));
		this.infoname = name;
    	this.hasTextInfo = true;
	}
	
	//Effects -No ToolTip-
	public BlockBase(Material material, MaterialColor color, float hardness, float resistance,
			SoundType sound, ToolType tool, int harvestLevel, boolean hadDrop, MobEffect effects) {
		super(BlockBehaviour.Properties.of(material, color).strength(hardness, resistance).sound(sound).harvestTool(tool).harvestLevel(harvestLevel));
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
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
    {
        return this.customShape == null ? Shapes.block() : this.customShape;
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
    public void appendHoverText(final ItemStack stack, final BlockGetter worldIn, final List<Component> tooltip,
            final TooltipFlag flagIn)
    {
        if (!this.hasTextInfo) return;
        String message;
        if (Screen.hasShiftDown()) message = I18n.get("legendblock." + this.infoname + ".tooltip");
        else message = I18n.get("pokecube.tooltip.advanced");
        tooltip.add(new TranslatableComponent(message));
    }

	public int ticksRandomly() {
		return 0;
	}
}

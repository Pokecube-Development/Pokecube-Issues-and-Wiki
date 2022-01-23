package pokecube.legends.blocks;

import java.util.List;

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
    String infoname;
    boolean hasTextInfo = false;
    boolean hasRequiresCorrectToolForDrops = false;

    // ToolTip
    public BlockBase(final String name, final Material material, final MaterialColor color, final float hardness,
            final float resistance, final SoundType sound, final boolean requiresCorrectToolForDrops)
    {
        super(BlockBehaviour.Properties.of(material, color).strength(hardness, resistance).sound(sound).requiresCorrectToolForDrops());
        this.infoname = name;
        this.hasTextInfo = true;
        this.hasRequiresCorrectToolForDrops(requiresCorrectToolForDrops);
    }

    // No Tooltip
    public BlockBase(final Material material, final MaterialColor color, final float hardness, final float resistance,
            final SoundType sound, final boolean requiresCorrectToolForDrops)
    {
        super(BlockBehaviour.Properties.of(material, color).strength(hardness, resistance).sound(sound));
        this.hasRequiresCorrectToolForDrops(requiresCorrectToolForDrops);
    }

    // Vertex
    public BlockBase(final String name, final Properties props)
    {
        super(props);
        this.infoname = name;
        this.hasTextInfo = true;
    }

    // Vertex -No ToolTip-
    public BlockBase(final Properties props)
    {
        super(props);
    }

    // Effects -ToolTip-
    public BlockBase(final String name, final Material material, final MaterialColor color, final float hardness,
            final float resistance, final SoundType sound, final boolean requiresCorrectToolForDrops, final MobEffect effects)
    {
        super(BlockBehaviour.Properties.of(material, color).strength(hardness, resistance).sound(sound));
        this.infoname = name;
        this.hasTextInfo = true;
    }

    // Effects -No ToolTip-
    public BlockBase(final Material material, final MaterialColor color, final float hardness, final float resistance,
            final SoundType sound, final boolean requiresCorrectToolForDrops, final MobEffect effects)
    {
        super(BlockBehaviour.Properties.of(material, color).strength(hardness, resistance).sound(sound));
    }

    public BlockBase setToolTip(final String infoname)
    {
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

    // Drop Required
    public BlockBase hasRequiresCorrectToolForDrops(final boolean hasDrop)
    {
        this.hasRequiresCorrectToolForDrops = hasDrop;
        if (this.hasRequiresCorrectToolForDrops == true)
            this.properties.requiresCorrectToolForDrops();
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(final ItemStack stack, final BlockGetter worldIn, final List<Component> tooltip,
            final TooltipFlag flagIn)
    {
        if (!this.hasTextInfo)
            return;
        String message;
        if (Screen.hasShiftDown())
            message = I18n.get("legendblock." + this.infoname + ".tooltip");
        else
            message = I18n.get("pokecube.tooltip.advanced");
        tooltip.add(new TranslatableComponent(message));
    }

    public int ticksRandomly()
    {
        return 0;
    }
}

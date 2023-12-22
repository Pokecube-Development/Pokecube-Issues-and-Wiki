package pokecube.legends.blocks;

import java.util.List;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import thut.lib.TComponent;

public class BlockBase extends Block
{
    VoxelShape customShape = null;
    String tooltip_id;
    boolean hasTooltip = false;
    boolean hasRequiredCorrectToolForDrops = false;

    // ToolTip
    public BlockBase(final String name, final Material material, final MaterialColor color, final float hardness,
            final float resistance, final SoundType sound, final boolean requiresCorrectToolForDrops)
    {
        super(BlockBehaviour.Properties.of(material, color).strength(hardness, resistance).sound(sound).requiresCorrectToolForDrops());
        this.tooltip_id = name;
        this.hasTooltip = true;
        this.hasRequiredCorrectToolForDrops(requiresCorrectToolForDrops);
    }

    // No Tooltip
    public BlockBase(final Material material, final MaterialColor color, final float hardness, final float resistance,
            final SoundType sound, final boolean requiresCorrectToolForDrops)
    {
        super(BlockBehaviour.Properties.of(material, color).strength(hardness, resistance).sound(sound));
        this.hasRequiredCorrectToolForDrops(requiresCorrectToolForDrops);
    }

    // Vertex
    public BlockBase(final String name, final Properties props)
    {
        super(props);
        this.tooltip_id = name;
        this.hasTooltip = true;
    }

    // Vertex -No ToolTip-
    public BlockBase(final Properties props)
    {
        super(props);
    }

    // Effects -ToolTip-
    public BlockBase(final String tooltipName, final Material material, final MaterialColor color, final float hardness,
            final float resistance, final SoundType sound, final boolean requiresCorrectToolForDrops, final MobEffect effects)
    {
        super(BlockBehaviour.Properties.of(material, color).strength(hardness, resistance).sound(sound));
        this.tooltip_id = tooltipName;
        this.hasTooltip = true;
    }

    // Effects -No ToolTip-
    public BlockBase(final Material material, final MaterialColor color, final float hardness, final float resistance,
            final SoundType sound, final boolean requiresCorrectToolForDrops, final MobEffect effects)
    {
        super(BlockBehaviour.Properties.of(material, color).strength(hardness, resistance).sound(sound));
    }

    public BlockBase setToolTip(final String infoname)
    {
        this.tooltip_id = infoname;
        this.hasTooltip = true;
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
    public BlockBase hasRequiredCorrectToolForDrops(final boolean hasDrop)
    {
        this.hasRequiredCorrectToolForDrops = hasDrop;
        if (this.hasRequiredCorrectToolForDrops)
            this.properties.requiresCorrectToolForDrops();
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(final ItemStack stack, final BlockGetter worldIn, final List<Component> tooltip,
            final TooltipFlag flagIn)
    {
        if (!this.hasTooltip)
            return;
        if (Screen.hasShiftDown())
            tooltip.add(TComponent.translatable("legends." + this.tooltip_id + ".tooltip"));
        else tooltip.add(TComponent.translatable("pokecube.tooltip.advanced"));
    }

    public int ticksRandomly()
    {
        return 0;
    }
}

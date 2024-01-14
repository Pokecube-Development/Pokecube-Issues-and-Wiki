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
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
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
    public BlockBase(final String name, final MapColor color, final SoundType sound, NoteBlockInstrument instrument, 
                     final boolean requiresCorrectToolForDrops, final float destroyTime, final float blastResistance)
    {
        super(BlockBehaviour.Properties.of().strength(destroyTime, blastResistance).mapColor(color).sound(sound).instrument(instrument).requiresCorrectToolForDrops());
        this.tooltip_id = name;
        this.hasTooltip = true;
        this.hasRequiredCorrectToolForDrops(requiresCorrectToolForDrops);
    }

    // No Tooltip
    public BlockBase(final MapColor color, final SoundType sound, NoteBlockInstrument instrument,
                     final boolean requiresCorrectToolForDrops, final float destroyTime, final float blastResistance)
    {
        super(BlockBehaviour.Properties.of().mapColor(color).strength(destroyTime, blastResistance).sound(sound).instrument(instrument));
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
    public BlockBase(final String name, final MapColor color, final SoundType sound, NoteBlockInstrument instrument,
                     final boolean requiresCorrectToolForDrops, final MobEffect effects, final float destroyTime, final float blastResistance)
    {
        super(BlockBehaviour.Properties.of().mapColor(color).strength(destroyTime, blastResistance).sound(sound).instrument(instrument));
        this.tooltip_id = name;
        this.hasTooltip = true;
    }

    // Effects -No ToolTip-
    public BlockBase(final MapColor color, final SoundType sound, NoteBlockInstrument instrument,
                     final boolean requiresCorrectToolForDrops, final MobEffect effects, final float destroyTime, final float blastResistance)
    {
        super(BlockBehaviour.Properties.of().mapColor(color).strength(destroyTime, blastResistance).sound(sound).instrument(instrument));
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
    public void hasRequiredCorrectToolForDrops(final boolean hasDrop)
    {
        this.hasRequiredCorrectToolForDrops = hasDrop;
        if (this.hasRequiredCorrectToolForDrops)
            this.properties.requiresCorrectToolForDrops();
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

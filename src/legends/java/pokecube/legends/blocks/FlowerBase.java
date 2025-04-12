package pokecube.legends.blocks;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thut.lib.TComponent;

import java.util.List;

public class FlowerBase extends FlowerBlock
{
    public static final Block block = null;
    String tooltip_id;
    boolean hasTooltip = false;
    int tooltipLineAmt = 0;

    public FlowerBase(final Holder<MobEffect> effects, int seconds, final BlockBehaviour.Properties properties)
    {
        super(effects, seconds, properties);
    }


    // Tooltips with extra lines
    public FlowerBase(final String tooltipName, final int tooltipExtraLineAmt, final Holder<MobEffect> effects, int seconds, final BlockBehaviour.Properties properties)
    {
        super(effects, seconds, properties);
        this.hasTooltip = true;
        this.tooltip_id = tooltipName;
        this.tooltipLineAmt = tooltipExtraLineAmt;
    }

    @Override
    public boolean canBeReplaced(final BlockState state, final BlockPlaceContext useContext)
    {
        return false;
    }

    @Override
    public int getFlammability(final BlockState state, final BlockGetter world, final BlockPos pos,
            final Direction face)
    {
        return 2;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
            TooltipFlag tooltipFlag)
    {
        if (!this.hasTooltip)
            return;
        if (Screen.hasShiftDown())
        {
            tooltipComponents.add(TComponent.translatable("legends." + this.tooltip_id + ".tooltip"));
            for (int lineAmt = 1; lineAmt <= tooltipLineAmt;)
            {
                tooltipComponents.add(TComponent.translatable("legends." + this.tooltip_id + ".tooltip.line" + lineAmt));
                lineAmt++;
            }
        }
        else tooltipComponents.add(TComponent.translatable("pokecube.tooltip.advanced"));
    }
}

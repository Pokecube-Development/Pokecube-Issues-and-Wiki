package pokecube.legends.items.armor;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import pokecube.legends.Reference;

import java.util.List;

public class ImprisonmentArmorItem extends ArmorItem
{
    String tooltip_id;
    boolean hasTooltip = false;
    int tooltipLineAmt = 0;

    public ImprisonmentArmorItem(final String tooltipName, final int tooltipExtraLineAmt,
            final Holder<ArmorMaterial> material, final ArmorItem.Type slot, final int maxStackSize,
            final Properties properties)
    {
        super(material, slot, properties);
        this.hasTooltip = true;
        this.tooltip_id = tooltipName;
        this.tooltipLineAmt = tooltipExtraLineAmt;
    }

    private static final ResourceLocation TEX = ResourceLocation.parse(
            Reference.ID + ":textures/models/armor/" + "imprisonment_layer_1.png");

    @Override
    public @Nullable ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
            ArmorMaterial.Layer layer, boolean innerModel)
    {
        return TEX;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
            TooltipFlag tooltipFlag)
    {
        if (!this.hasTooltip) return;
        if (Screen.hasShiftDown())
        {
            tooltipComponents.add(Component.translatable("legends." + this.tooltip_id + ".tooltip"));
            for (int lineAmt = 1; lineAmt <= tooltipLineAmt; )
            {
                tooltipComponents.add(
                        Component.translatable("legends." + this.tooltip_id + ".tooltip.line" + lineAmt));
                lineAmt++;
            }
        }
        else tooltipComponents.add(Component.translatable("pokecube.tooltip.advanced"));
    }
}

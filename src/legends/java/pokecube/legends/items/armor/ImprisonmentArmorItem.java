package pokecube.legends.items.armor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import pokecube.legends.Reference;
import pokecube.legends.client.render.model.LegendsModelLayers;
import pokecube.legends.client.render.model.armor.ImprisonmentArmorModel;
import thut.lib.TComponent;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ImprisonmentArmorItem extends ArmorItem
{
    String tooltip_id;
    boolean hasTooltip = false;
    int tooltipLineAmt = 0;

    public ImprisonmentArmorItem(final Holder<ArmorMaterial> material, final ArmorItem.Type slot,
            final Properties properties)
    {
        super(material, slot, properties);
    }

    public ImprisonmentArmorItem(final String tooltipName, final int tooltipExtraLineAmt,
            final Holder<ArmorMaterial> material, final ArmorItem.Type slot, final int maxStackSize,
            final Properties properties)
    {
        super(material, slot, properties);
        this.hasTooltip = true;
        this.tooltip_id = tooltipName;
        this.tooltipLineAmt = tooltipExtraLineAmt;
    }

    @Override
    public String getArmorTexture(ItemStack itemstack, Entity entity, EquipmentSlot slot, String layer)
    {
        return Reference.ID + ":textures/models/armor/" + "imprisonment_layer_1.png";
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer)
    {
        consumer.accept(new IClientItemExtensions()
        {
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack,
                    EquipmentSlot slot, HumanoidModel<?> defaultModel)
            {
                EntityModelSet models = Minecraft.getInstance().getEntityModels();

                HumanoidModel<? extends LivingEntity> armorModel = new HumanoidModel<>(
                        new ModelPart(Collections.emptyList(), Map.of("head", new ImprisonmentArmorModel<>(
                                        models.bakeLayer(LegendsModelLayers.IMPRISONMENT_ARMOR_INNER)).head, "hat",
                                new ModelPart(Collections.emptyList(), Collections.emptyMap()), "body",
                                new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_arm",
                                new ModelPart(Collections.emptyList(), Collections.emptyMap()), "left_arm",
                                new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_leg",
                                new ModelPart(Collections.emptyList(), Collections.emptyMap()), "left_leg",
                                new ModelPart(Collections.emptyList(), Collections.emptyMap()))));

                armorModel.crouching = entity.isShiftKeyDown();
                armorModel.riding = defaultModel.riding;
                armorModel.young = entity.isBaby();

                return armorModel;
            }
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
            TooltipFlag tooltipFlag)
    {
        if (!this.hasTooltip) return;
        if (Screen.hasShiftDown())
        {
            tooltipComponents.add(TComponent.translatable("legends." + this.tooltip_id + ".tooltip"));
            for (int lineAmt = 1; lineAmt <= tooltipLineAmt; )
            {
                tooltipComponents.add(
                        TComponent.translatable("legends." + this.tooltip_id + ".tooltip.line" + lineAmt));
                lineAmt++;
            }
        }
        else tooltipComponents.add(TComponent.translatable("pokecube.tooltip.advanced"));
    }
}

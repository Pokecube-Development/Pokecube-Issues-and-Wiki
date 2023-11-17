package pokecube.legends.items.armor;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemRenderProperties;
import pokecube.legends.Reference;
import pokecube.legends.client.render.model.LegendsModelLayers;
import pokecube.legends.client.render.model.armor.BaseArmorModel;

public class ImprisonmentArmorItem extends ArmorItem
{
    public ImprisonmentArmorItem(final ArmorMaterial material, final EquipmentSlot slot, final Properties builder)
    {
        super(material, slot, builder);
    }

    @Override
    public String getArmorTexture(ItemStack itemstack, Entity entity, EquipmentSlot slot, String layer)
    {
        return Reference.ID + ":textures/models/armor/" + "imprisonment_layer_1.png";
    }

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer)
    {
        consumer.accept(ArmorRender.INSTANCE);
    }

    private static final class ArmorRender implements IItemRenderProperties
    {
        private static final ArmorRender INSTANCE = new ArmorRender();

        @Override
        public HumanoidModel<?> getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot slot, HumanoidModel<?> _default)
        {
            EntityModelSet models = Minecraft.getInstance().getEntityModels();
            ModelPart root = models.bakeLayer(LegendsModelLayers.IMPRISONMENT_ARMOR_INNER);
            return new BaseArmorModel(root);
        }
    }
}

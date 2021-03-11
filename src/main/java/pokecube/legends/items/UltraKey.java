package pokecube.legends.items;

import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.init.FeaturesInit;
import pokecube.legends.init.ItemInit;
import pokecube.legends.worldgen.DimensionTranserHelper;

public class UltraKey extends ItemBase
{

    public UltraKey(final String name, final int num)
    {
        super(name, num, PokecubeLegends.TAB);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(final ItemStack stack, final World worldIn, final List<ITextComponent> tooltip,
            final ITooltipFlag flagIn)
    {
        String message;
        if (Screen.hasShiftDown()) message = I18n.get("legends." + this.tooltipname + ".tooltip",
                TextFormatting.LIGHT_PURPLE, PokecubeLegends.config.itemCombustiveStack);
        else message = I18n.get("pokecube.tooltip.advanced");
        tooltip.add(new TranslationTextComponent(message));
    }

    @Override
    public ActionResult<ItemStack> use(final World world, final PlayerEntity entity, final Hand hand)
    {
        final ActionResult<ItemStack> ar = super.use(world, entity, hand);
        final double x = entity.getX();
        final double y = entity.getY();
        final double z = entity.getZ();
        UltraKey.dimensionTP(entity, x, y, z, world);
        return ar;
    }

    public static void dimensionTP(final Entity entity, final double x, final double y, final double z,
            final World world)
    {
        final RegistryKey<World> dim = world.dimension();

        // Comsume Item Enabled
        if (PokecubeLegends.config.enabledkeyusecombustible == true) if (dim == World.OVERWORLD)
        {
            if ((entity instanceof ServerPlayerEntity ? ((PlayerEntity) entity).inventory.contains(new ItemStack(
                    ItemInit.COSMIC_DUST.get())) : true) && ((PlayerEntity) entity).inventory.countItem(ItemInit.COSMIC_DUST
                            .get()) >= 5)
            {

                world.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(
                        "block.end_portal_frame.fill")), SoundCategory.NEUTRAL, 1, 1, false);

                if (entity instanceof PlayerEntity) ((PlayerEntity) entity).inventory.clearOrCountMatchingItems(p -> new ItemStack(
                        ItemInit.COSMIC_DUST.get(), 1).getItem() == p.getItem(),
                        PokecubeLegends.config.itemCombustiveStack, ((PlayerEntity) entity).inventoryMenu.getCraftSlots());

                if (entity instanceof ServerPlayerEntity) DimensionTranserHelper.sentToUltraspace(
                        (ServerPlayerEntity) entity);

                if (entity instanceof PlayerEntity) ((PlayerEntity) entity).getCooldowns().addCooldown(
                        ItemInit.ULTRAKEY.get(), 200);
            }
            else world.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(
                    "block.shulker_box.close")), SoundCategory.NEUTRAL, 1, 1, false);
        }
        else if (dim == FeaturesInit.ULTRASPACE_KEY) if ((entity instanceof ServerPlayerEntity
                ? ((PlayerEntity) entity).inventory.contains(new ItemStack(ItemInit.COSMIC_DUST.get(), 1))
                : true) && ((PlayerEntity) entity).inventory.countItem(ItemInit.COSMIC_DUST.get()) >= 5)
        {

            world.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(
                    "block.end_portal_frame.fill")), SoundCategory.NEUTRAL, 1, 1, false);

            if (entity instanceof PlayerEntity) ((PlayerEntity) entity).inventory.clearOrCountMatchingItems(p -> new ItemStack(
                    ItemInit.COSMIC_DUST.get(), 1).getItem() == p.getItem(), PokecubeLegends.config.itemCombustiveStack,
                    ((PlayerEntity) entity).inventoryMenu.getCraftSlots());

            if (entity instanceof ServerPlayerEntity) DimensionTranserHelper.sendToOverworld(
                    (ServerPlayerEntity) entity);

            if (entity instanceof PlayerEntity) ((PlayerEntity) entity).getCooldowns().addCooldown(
                    ItemInit.ULTRAKEY.get(), 200);
        }
        else world.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(
                "block.shulker_box.close")), SoundCategory.NEUTRAL, 1, 1, false);

        // Comsume Item Disable
        if (PokecubeLegends.config.enabledkeyusecombustible == false) if (dim == World.OVERWORLD)
        {
            world.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(
                    "block.end_portal_frame.fill")), SoundCategory.NEUTRAL, 1, 1, false);

            if (entity instanceof ServerPlayerEntity) DimensionTranserHelper.sentToUltraspace(
                    (ServerPlayerEntity) entity);

            if (entity instanceof PlayerEntity) ((PlayerEntity) entity).getCooldowns().addCooldown(
                    ItemInit.ULTRAKEY.get(), 200);
        }
        else if (dim == FeaturesInit.ULTRASPACE_KEY)
        {
            world.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(
                    "block.end_portal_frame.fill")), SoundCategory.NEUTRAL, 1, 1, false);

            if (entity instanceof ServerPlayerEntity) DimensionTranserHelper.sendToOverworld(
                    (ServerPlayerEntity) entity);

            if (entity instanceof PlayerEntity) ((PlayerEntity) entity).getCooldowns().addCooldown(
                    ItemInit.ULTRAKEY.get(), 200);
        }
    }
}
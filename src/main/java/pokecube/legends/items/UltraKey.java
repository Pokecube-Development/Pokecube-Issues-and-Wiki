package pokecube.legends.items;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
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
    public void appendHoverText(final ItemStack stack, final Level worldIn, final List<Component> tooltip,
            final TooltipFlag flagIn)
    {
        String message;
        if (Screen.hasShiftDown()) message = I18n.get("legends." + this.tooltipname + ".tooltip",
                ChatFormatting.LIGHT_PURPLE, PokecubeLegends.config.ultraKeyConsumeAmount);
        else message = I18n.get("pokecube.tooltip.advanced");
        tooltip.add(new TranslatableComponent(message));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level world, final Player entity, final InteractionHand hand)
    {
        final InteractionResultHolder<ItemStack> ar = super.use(world, entity, hand);
        final double x = entity.getX();
        final double y = entity.getY();
        final double z = entity.getZ();
        UltraKey.dimensionTP(entity, x, y, z, world);
        return ar;
    }

    public static void dimensionTP(final Entity entity, final double x, final double y, final double z,
            final Level world)
    {
        final ResourceKey<Level> dim = world.dimension();

        // Comsume Item Enabled
        if (PokecubeLegends.config.enableUltraKeyConsume == true) if (dim == Level.OVERWORLD)
        {
            if ((entity instanceof ServerPlayer ? ((Player) entity).getInventory().contains(new ItemStack(
                    ItemInit.COSMIC_DUST.get())) : true) && ((Player) entity).getInventory().countItem(
                            ItemInit.COSMIC_DUST
                .get()) >= PokecubeLegends.config.ultraKeyConsumeAmount || ((Player) entity).isCreative() == true)
            {

                world.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(
                        "block.end_portal_frame.fill")), SoundSource.NEUTRAL, 1, 1, false);

                if (entity instanceof Player) ((Player) entity).getInventory().clearOrCountMatchingItems(
                        p -> new ItemStack(
                        ItemInit.COSMIC_DUST.get(), 1).getItem() == p.getItem(),
                        PokecubeLegends.config.ultraKeyConsumeAmount, ((Player) entity).inventoryMenu.getCraftSlots());

                if (entity instanceof ServerPlayer) DimensionTranserHelper.sentToUltraspace(
                        (ServerPlayer) entity);

                if (entity instanceof Player) ((Player) entity).getCooldowns().addCooldown(
                        ItemInit.ULTRAKEY.get(), 200);
            } else
            {
                world.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(
                    "block.shulker_box.close")), SoundSource.NEUTRAL, 1, 1, false);
                if (entity instanceof Player && ((Player) entity).getInventory().countItem(
                        ItemInit.COSMIC_DUST
                    .get()) < PokecubeLegends.config.ultraKeyConsumeAmount) {
                    final Player player = (Player) entity;
                    final String message = I18n.get("msg.pokecube_legends.ultrakey.no_dust",
                        ChatFormatting.RED, PokecubeLegends.config.ultraKeyConsumeAmount);
                    player.displayClientMessage(new TranslatableComponent(message), true);
                }
            }
        }
        else if (dim == FeaturesInit.ULTRASPACE_KEY) if ((entity instanceof ServerPlayer
                ? ((Player) entity).getInventory().contains(new ItemStack(ItemInit.COSMIC_DUST.get(), 1))
                : true) && ((Player) entity).getInventory().countItem(ItemInit.COSMIC_DUST.get()) >=
            PokecubeLegends.config.ultraKeyConsumeAmount || ((Player) entity).isCreative() == true)
        {

            world.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(
                    "block.end_portal_frame.fill")), SoundSource.NEUTRAL, 1, 1, false);

            if (entity instanceof Player) ((Player) entity).getInventory().clearOrCountMatchingItems(
                    p -> new ItemStack(
                    ItemInit.COSMIC_DUST.get(), 1).getItem() == p.getItem(), PokecubeLegends.config.ultraKeyConsumeAmount,
                    ((Player) entity).inventoryMenu.getCraftSlots());

            if (entity instanceof ServerPlayer) DimensionTranserHelper.sendToOverworld(
                    (ServerPlayer) entity);

            if (entity instanceof Player) ((Player) entity).getCooldowns().addCooldown(
                    ItemInit.ULTRAKEY.get(), 200);
        }
        else
        {
            world.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(
                "block.shulker_box.close")), SoundSource.NEUTRAL, 1, 1, false);
            if (entity instanceof Player && ((Player) entity).getInventory().countItem(
                    ItemInit.COSMIC_DUST
                .get()) < PokecubeLegends.config.ultraKeyConsumeAmount) {
                final Player player = (Player) entity;
                final String message = I18n.get("msg.pokecube_legends.ultrakey.no_dust",
                    ChatFormatting.RED, PokecubeLegends.config.ultraKeyConsumeAmount);
                player.displayClientMessage(new TranslatableComponent(message), true);
            }
        }

        // Comsume Item Disable
        if (PokecubeLegends.config.enableUltraKeyConsume == false) if (dim == Level.OVERWORLD)
        {
            world.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(
                    "block.end_portal_frame.fill")), SoundSource.NEUTRAL, 1, 1, false);

            if (entity instanceof ServerPlayer) DimensionTranserHelper.sentToUltraspace(
                    (ServerPlayer) entity);

            if (entity instanceof Player) ((Player) entity).getCooldowns().addCooldown(
                    ItemInit.ULTRAKEY.get(), 200);
        }
        else if (dim == FeaturesInit.ULTRASPACE_KEY)
        {
            world.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(
                    "block.end_portal_frame.fill")), SoundSource.NEUTRAL, 1, 1, false);

            if (entity instanceof ServerPlayer) DimensionTranserHelper.sendToOverworld(
                    (ServerPlayer) entity);

            if (entity instanceof Player) ((Player) entity).getCooldowns().addCooldown(
                    ItemInit.ULTRAKEY.get(), 200);
        }
    }
}
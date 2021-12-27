package pokecube.legends.items;

import java.util.List;

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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
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

public class DistortedMirror extends ItemBase
{

    public DistortedMirror(final String name, final CreativeModeTab tab, final int maxStackSize)
    {
        super(name, tab, maxStackSize);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(final ItemStack stack, final Level worldIn, final List<Component> tooltip,
            final TooltipFlag flagIn)
    {
        String message;
        if (Screen.hasShiftDown()) message = I18n.get("legends." + this.tooltipname + ".tooltip");
        else message = I18n.get("pokecube.tooltip.advanced");
        tooltip.add(new TranslatableComponent(message));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level world, final Player entity, final InteractionHand hand)
    {
        final InteractionResultHolder<ItemStack> ar = super.use(world, entity, hand);
        final ResourceKey<Level> dim = world.dimension();
        final double x = entity.getX();
        final double y = entity.getY();
        final double z = entity.getZ();

        if (dim == Level.OVERWORLD)
        {
            if (entity instanceof ServerPlayer) DimensionTranserHelper.sentToDistorted((ServerPlayer) entity);

            if (entity instanceof Player) entity.getCooldowns().addCooldown(ItemInit.GIRATINA_MIRROR.get(),
                    PokecubeLegends.config.mirrorCooldown);

            world.playLocalSound(x, y, z,
                    ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.end_gateway.spawn")),
                    SoundSource.NEUTRAL, 1, 1, false);

            return ar;
        }
        else if (dim == FeaturesInit.DISTORTEDWORLD_KEY)
        {
            if (entity instanceof ServerPlayer) DimensionTranserHelper.sendToOverworld((ServerPlayer) entity);

            if (entity instanceof Player) entity.getCooldowns().addCooldown(ItemInit.GIRATINA_MIRROR.get(),
                    PokecubeLegends.config.mirrorCooldown);

            world.playLocalSound(x, y, z,
                    ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.end_gateway.spawn")),
                    SoundSource.NEUTRAL, 1, 1, false);

            return ar;
        }
        return ar;
    }
}

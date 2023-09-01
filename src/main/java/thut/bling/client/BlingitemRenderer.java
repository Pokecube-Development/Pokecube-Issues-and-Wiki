package thut.bling.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import thut.core.common.ThutCore;
import thut.wearables.IWearable;
import thut.wearables.ThutWearables;

public class BlingitemRenderer extends BlockEntityWithoutLevelRenderer implements IClientItemExtensions
{
    public static final BlingitemRenderer INSTANCE = new BlingitemRenderer();

    public BlingitemRenderer()
    {
        super(null, null);
    }

    private IWearable getWearable(final ItemStack stack)
    {
        if (stack.getItem() instanceof IWearable) return (IWearable) stack.getItem();
        return stack.getCapability(ThutWearables.WEARABLE_CAP, null).orElse(null);
    }

    @Override
    public void renderByItem(ItemStack item, ItemDisplayContext displayContext, PoseStack mat, MultiBufferSource bufs, int light,
                             int overlay)
    {
        IWearable wearable = this.getWearable(item);
        if (wearable == null)
        {
            ThutCore.LOGGER.error(new IllegalStateException("Not a wearable???"));
            return;
        }
        BlingRender.INSTANCE.initModels();
        mat.pushPose();
        mat.scale(1, -1, -1);
        mat.mulPose(Axis.YP.rotationDegrees(180));
        mat.mulPose(Axis.XP.rotationDegrees(180));
        mat.translate(-0.5, 0, -0.5);
        wearable.renderWearable(mat, bufs, wearable.getSlot(item), 0, Minecraft.getInstance().player, item, 0, light,
                overlay);
        mat.popPose();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer()
    {
        return this;
    }
}

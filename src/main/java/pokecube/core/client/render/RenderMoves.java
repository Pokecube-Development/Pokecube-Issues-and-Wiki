package pokecube.core.client.render;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.interfaces.IMoveAnimation.MovePacketInfo;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.moves.animations.EntityMoveUse;

public class RenderMoves extends EntityRenderer<EntityMoveUse>
{
    private static final ResourceLocation EMPTY = new ResourceLocation("");

    public RenderMoves(final EntityRendererManager renderManager)
    {
        super(renderManager);
    }

    @Override
    public void doRender(final EntityMoveUse entity, final double x, final double y, final double z,
            final float entityYaw, final float partialTicks)
    {
        if (entity.getStartTick() > 0) return;
        final Move_Base move = entity.getMove();
        IMoveAnimation animation;
        if (move != null && (animation = move.getAnimation(CapabilityPokemob.getPokemobFor(entity.getUser()))) != null
                && entity.getUser() != null)
        {
            mat.push();
            final MovePacketInfo info = entity.getMoveInfo();
            mat.translate((float) x, (float) y, (float) z);
            animation.clientAnimation(info, partialTicks);
            mat.pop();
        }
    }

    @Override
    public ResourceLocation getEntityTexture(final EntityMoveUse entity)
    {
        return RenderMoves.EMPTY;
    }
}

package pokecube.core.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
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
    public void render(final EntityMoveUse entity, final float entityYaw, final float partialTicks,
            final MatrixStack mat, final IRenderTypeBuffer bufferIn, final int packedLightIn)
    {
        if (entity.getStartTick() > 0) return;
        final Move_Base move = entity.getMove();
        IMoveAnimation animation;
        if (move != null && (animation = move.getAnimation(CapabilityPokemob.getPokemobFor(entity.getUser()))) != null
                && entity.getUser() != null)
        {
            mat.pushPose();
            final MovePacketInfo info = entity.getMoveInfo();
            animation.clientAnimation(mat, bufferIn, info, partialTicks);
            mat.popPose();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(final EntityMoveUse entity)
    {
        return RenderMoves.EMPTY;
    }
}

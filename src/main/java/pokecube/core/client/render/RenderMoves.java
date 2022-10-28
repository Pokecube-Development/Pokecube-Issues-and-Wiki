package pokecube.core.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveAnimation;
import pokecube.api.moves.utils.IMoveAnimation.MovePacketInfo;
import pokecube.core.moves.damage.EntityMoveUse;

public class RenderMoves extends EntityRenderer<EntityMoveUse>
{
    private static final ResourceLocation EMPTY = new ResourceLocation("");

    public RenderMoves(final EntityRendererProvider.Context renderManager)
    {
        super(renderManager);
    }

    @Override
    public void render(final EntityMoveUse entity, final float entityYaw, final float partialTicks,
            final PoseStack mat, final MultiBufferSource bufferIn, final int packedLightIn)
    {
        if (entity.getStartTick() > 0) return;
        final MoveEntry move = entity.getMove();
        IMoveAnimation animation;
        if (move != null && (animation = move.getAnimation(PokemobCaps.getPokemobFor(entity.getUser()))) != null
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

package pokecube.core.client.render.mobs;

import java.util.Optional;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.core.entity.pokemobs.PokemobType;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.EntityTools;

public class ShoulderLayer<T extends PlayerEntity> extends LayerRenderer<T, PlayerModel<T>>
{
    public interface IShoulderHolder
    {
        public static Capability.IStorage<IShoulderHolder> STORAGE = new Capability.IStorage<IShoulderHolder>()
        {
            @Override
            public INBT writeNBT(final Capability<IShoulderHolder> capability, final IShoulderHolder instance,
                    final Direction side)
            {
                return null;
            }

            @Override
            public void readNBT(final Capability<IShoulderHolder> capability, final IShoulderHolder instance,
                    final Direction side, final INBT nbt)
            {
            }
        };

        IPokemob getLeft();

        IPokemob getRight();
    }

    @CapabilityInject(IShoulderHolder.class)
    public static Capability<IShoulderHolder> CAPABILITY = null;

    public static class ShoulderHolder implements IShoulderHolder, ICapabilityProvider
    {
        private final LazyOptional<IShoulderHolder> holder = LazyOptional.of(() -> this);

        PlayerEntity                                player;
        IPokemob                                    left   = null;
        IPokemob                                    right  = null;

        // Do not call this one, it is only for capability register!
        public ShoulderHolder()
        {
        }

        public ShoulderHolder(@Nonnull final PlayerEntity player)
        {
            this.player = player;
        }

        @Override
        public IPokemob getLeft()
        {
            final CompoundNBT tag = this.player.getLeftShoulderEntity();
            EntityType.byKey(tag.getString("id")).filter((type) ->
            {
                return type instanceof PokemobType;
            }).ifPresent((type) ->
            {
                final int uid = tag.getInt("pokemob:uid");
                if (this.left != null) if (this.left.getPokemonUID() == uid) return;
                final Optional<Entity> mob = EntityType.loadEntityUnchecked(tag, this.player.getEntityWorld());
                if (mob.isPresent()) this.left = CapabilityPokemob.getPokemobFor(mob.get());
            });
            return this.left;
        }

        @Override
        public IPokemob getRight()
        {
            final CompoundNBT tag = this.player.getRightShoulderEntity();
            EntityType.byKey(tag.getString("id")).filter((type) ->
            {
                return type instanceof PokemobType;
            }).ifPresent((type) ->
            {
                final int uid = tag.getInt("pokemob:uid");
                if (this.right != null) if (this.right.getPokemonUID() == uid) return;
                final Optional<Entity> mob = EntityType.loadEntityUnchecked(tag, this.player.getEntityWorld());
                if (mob.isPresent()) this.right = CapabilityPokemob.getPokemobFor(mob.get());
            });
            return this.right;
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return ShoulderLayer.CAPABILITY.orEmpty(cap, this.holder);
        }

    }

    public ShoulderLayer(final IEntityRenderer<T, PlayerModel<T>> entityRendererIn)
    {
        super(entityRendererIn);
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T player,
            float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
            float headPitch)
    {
        renderShoulder(matrixStackIn, bufferIn, packedLightIn, player, limbSwing, limbSwingAmount, partialTicks,
                ageInTicks, netHeadYaw, headPitch, true);
        renderShoulder(matrixStackIn, bufferIn, packedLightIn, player, limbSwing, limbSwingAmount, partialTicks,
                ageInTicks, netHeadYaw, headPitch, false);
    }

    private void renderShoulder(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T player,
            float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
            float headPitch, final boolean leftside)
    {
        final CompoundNBT compoundnbt = leftside ? player.getLeftShoulderEntity() : player.getRightShoulderEntity();
        EntityType.byKey(compoundnbt.getString("id")).filter((type) ->
        {
            return type instanceof PokemobType;
        }).ifPresent((type) ->
        {

            final IShoulderHolder holder = player.getCapability(ShoulderLayer.CAPABILITY).orElse(null);
            if (holder == null) return;
            final IPokemob pokemob = leftside ? holder.getLeft() : holder.getRight();
            if (pokemob == null) return;
            @SuppressWarnings("unchecked")
            final LivingRenderer<LivingEntity, ?> render = (LivingRenderer<LivingEntity, ?>) Minecraft.getInstance()
                    .getRenderManager().getRenderer(pokemob.getEntity());
            final LivingEntity to = pokemob.getEntity();

            EntityTools.copyEntityTransforms(pokemob.getEntity(), player);

            matrixStackIn.push();

            if (leftside)
            {
                to.prevRenderYawOffset = 180;
                to.renderYawOffset = 180;

                matrixStackIn.scale(1, -1, 1);
            }
            else
            {
                to.prevRenderYawOffset = 0;
                to.renderYawOffset = 0;
                matrixStackIn.scale(1, -1, -1);
            }
            matrixStackIn.translate(leftside ? 0.4F : -0.4F, player.isCrouching() ? -0.2F : -0.0F, 0.0F);
            render.render(pokemob.getEntity(), 0, partialTicks, matrixStackIn, bufferIn, packedLightIn);
            matrixStackIn.pop();
        });
    }
}

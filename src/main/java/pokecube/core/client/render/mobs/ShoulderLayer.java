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

        PlayerEntity player;
        IPokemob     left  = null;
        IPokemob     right = null;

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
            final CompoundNBT tag = this.player.getShoulderEntityLeft();
            EntityType.byString(tag.getString("id")).filter((type) ->
            {
                return type instanceof PokemobType;
            }).ifPresent((type) ->
            {
                final int uid = tag.getInt("pokemob:uid");
                if (this.left != null) if (this.left.getPokemonUID() == uid) return;
                final Optional<Entity> mob = EntityType.create(tag, this.player.getCommandSenderWorld());
                if (mob.isPresent()) this.left = CapabilityPokemob.getPokemobFor(mob.get());
            });
            return this.left;
        }

        @Override
        public IPokemob getRight()
        {
            final CompoundNBT tag = this.player.getShoulderEntityRight();
            EntityType.byString(tag.getString("id")).filter((type) ->
            {
                return type instanceof PokemobType;
            }).ifPresent((type) ->
            {
                final int uid = tag.getInt("pokemob:uid");
                if (this.right != null) if (this.right.getPokemonUID() == uid) return;
                final Optional<Entity> mob = EntityType.create(tag, this.player.getCommandSenderWorld());
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
    public void render(final MatrixStack matrixStackIn, final IRenderTypeBuffer bufferIn, final int packedLightIn,
            final T player, final float limbSwing, final float limbSwingAmount, final float partialTicks,
            final float ageInTicks, final float netHeadYaw, final float headPitch)
    {
        this.renderShoulder(matrixStackIn, bufferIn, packedLightIn, player, limbSwing, limbSwingAmount, partialTicks,
                ageInTicks, netHeadYaw, headPitch, true);
        this.renderShoulder(matrixStackIn, bufferIn, packedLightIn, player, limbSwing, limbSwingAmount, partialTicks,
                ageInTicks, netHeadYaw, headPitch, false);
    }

    private void renderShoulder(final MatrixStack matrixStackIn, final IRenderTypeBuffer bufferIn,
            final int packedLightIn, final T player, final float limbSwing, final float limbSwingAmount,
            final float partialTicks, final float ageInTicks, final float netHeadYaw, final float headPitch,
            final boolean leftside)
    {
        final CompoundNBT compoundnbt = leftside ? player.getShoulderEntityLeft() : player.getShoulderEntityRight();
        EntityType.byString(compoundnbt.getString("id")).filter((type) ->
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
                    .getEntityRenderDispatcher().getRenderer(pokemob.getEntity());
            final LivingEntity to = pokemob.getEntity();

            EntityTools.copyEntityTransforms(pokemob.getEntity(), player);

            matrixStackIn.pushPose();

            final float s = 1;

            if (leftside)
            {
                to.yBodyRotO = 180;
                to.yBodyRot = 180;
                matrixStackIn.scale(s, -s, s);
            }
            else
            {
                to.yBodyRotO = 0;
                to.yBodyRot = 0;
                matrixStackIn.scale(s, -s, -s);
            }
            float dw = pokemob.getEntity().getBbWidth() * pokemob.getSize() * 1.5f;
            if (dw < 1) dw = 0.4f;
            matrixStackIn.translate(leftside ? dw : -dw, player.isCrouching() ? -0.2F : -0.0F, 0.0F);
            render.render(pokemob.getEntity(), 0, partialTicks, matrixStackIn, bufferIn, packedLightIn);
            matrixStackIn.popPose();
        });
    }
}

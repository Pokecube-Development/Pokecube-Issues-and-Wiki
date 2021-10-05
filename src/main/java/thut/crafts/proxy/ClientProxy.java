package thut.crafts.proxy;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import thut.api.entity.blockentity.render.RenderBlockEntity;
import thut.api.maths.Vector3;
import thut.crafts.Reference;
import thut.crafts.ThutCrafts;
import thut.crafts.entity.CraftController;
import thut.crafts.entity.EntityCraft;
import thut.crafts.network.PacketCraftControl;

public class ClientProxy extends CommonProxy
{
    KeyMapping UP;
    KeyMapping DOWN;
    KeyMapping ROTATERIGHT;
    KeyMapping ROTATELEFT;

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Reference.MODID, value = Dist.CLIENT)
    public static class RegistryEvents
    {
        @SubscribeEvent
        public static void registerRenderers(final RegisterRenderers event)
        {
            event.registerEntityRenderer(EntityCraft.CRAFTTYPE, RenderBlockEntity::new);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void clientTick(final TickEvent.PlayerTickEvent event)
    {
        if (event.phase == Phase.START || event.player != Minecraft.getInstance().player) return;
        control:
        if (event.player.isPassenger() && Minecraft.getInstance().screen == null)
        {
            final Entity e = event.player.getVehicle();
            if (e instanceof EntityCraft)
            {
                final LocalPlayer player = (LocalPlayer) event.player;
                final CraftController controller = ((EntityCraft) e).controller;
                if (controller == null) break control;
                controller.backInputDown = player.input.down;
                controller.forwardInputDown = player.input.up;
                controller.leftInputDown = player.input.left;
                controller.rightInputDown = player.input.right;
                controller.upInputDown = this.UP.isDown();
                controller.downInputDown = this.DOWN.isDown();

                if (ThutCrafts.conf.canRotate)
                {
                    controller.rightRotateDown = this.ROTATERIGHT.isDown();
                    controller.leftRotateDown = this.ROTATELEFT.isDown();
                }
                PacketCraftControl.sendControlPacket(e, controller);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void RenderBounds(final RenderWorldLastEvent event)
    {
        ItemStack held;
        final Player player = Minecraft.getInstance().player;
        if (!(held = player.getMainHandItem()).isEmpty() || !(held = player.getOffhandItem()).isEmpty())
        {
            if (held.getItem() != ThutCrafts.CRAFTMAKER) return;
            if (held.getTag() != null && held.getTag().contains("min"))
            {
                final Minecraft mc = Minecraft.getInstance();
                final Vec3 projectedView = mc.gameRenderer.getMainCamera().getPosition();
                Vec3 pointed = new Vec3(projectedView.x, projectedView.y, projectedView.z).add(mc.player.getViewVector(
                        event.getPartialTicks()));
                if (mc.hitResult != null && mc.hitResult.getType() == Type.BLOCK)
                {
                    final BlockHitResult result = (BlockHitResult) mc.hitResult;
                    pointed = new Vec3(result.getBlockPos().getX(), result.getBlockPos().getY(), result.getBlockPos()
                            .getZ());
                    //
                }
                final Vector3 v = Vector3.readFromNBT(held.getTag().getCompound("min"), "");

                final AABB one = new AABB(v.getPos());
                final AABB two = new AABB(new BlockPos(pointed));

                final double minX = Math.min(one.minX, two.minX);
                final double minY = Math.min(one.minY, two.minY);
                final double minZ = Math.min(one.minZ, two.minZ);
                final double maxX = Math.max(one.maxX, two.maxX);
                final double maxY = Math.max(one.maxY, two.maxY);
                final double maxZ = Math.max(one.maxZ, two.maxZ);

                final PoseStack mat = event.getMatrixStack();
                mat.translate(-projectedView.x, -projectedView.y, -projectedView.z);

                final List<Pair<Vector3f, Vector3f>> lines = Lists.newArrayList();

                lines.add(Pair.of(new Vector3f((float) minX, (float) minY, (float) minZ), new Vector3f((float) maxX,
                        (float) minY, (float) minZ)));
                lines.add(Pair.of(new Vector3f((float) minX, (float) maxY, (float) minZ), new Vector3f((float) maxX,
                        (float) maxY, (float) minZ)));
                lines.add(Pair.of(new Vector3f((float) minX, (float) minY, (float) maxZ), new Vector3f((float) maxX,
                        (float) minY, (float) maxZ)));
                lines.add(Pair.of(new Vector3f((float) minX, (float) maxY, (float) maxZ), new Vector3f((float) maxX,
                        (float) maxY, (float) maxZ)));

                lines.add(Pair.of(new Vector3f((float) minX, (float) minY, (float) minZ), new Vector3f((float) minX,
                        (float) minY, (float) maxZ)));
                lines.add(Pair.of(new Vector3f((float) maxX, (float) minY, (float) minZ), new Vector3f((float) maxX,
                        (float) minY, (float) maxZ)));
                lines.add(Pair.of(new Vector3f((float) minX, (float) maxY, (float) minZ), new Vector3f((float) minX,
                        (float) maxY, (float) maxZ)));
                lines.add(Pair.of(new Vector3f((float) maxX, (float) maxY, (float) minZ), new Vector3f((float) maxX,
                        (float) maxY, (float) maxZ)));

                lines.add(Pair.of(new Vector3f((float) minX, (float) minY, (float) minZ), new Vector3f((float) minX,
                        (float) maxY, (float) minZ)));
                lines.add(Pair.of(new Vector3f((float) maxX, (float) minY, (float) minZ), new Vector3f((float) maxX,
                        (float) maxY, (float) minZ)));
                lines.add(Pair.of(new Vector3f((float) minX, (float) minY, (float) maxZ), new Vector3f((float) minX,
                        (float) maxY, (float) maxZ)));
                lines.add(Pair.of(new Vector3f((float) maxX, (float) minY, (float) maxZ), new Vector3f((float) maxX,
                        (float) maxY, (float) maxZ)));

                mat.pushPose();

                final Matrix4f positionMatrix = mat.last().pose();

                final MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
                final VertexConsumer builder = buffer.getBuffer(RenderType.LINES);
                for (final Pair<Vector3f, Vector3f> line : lines)
                    thut.core.proxy.ClientProxy.line(builder, positionMatrix, line.getLeft(), line.getRight(), 1, 0, 0,
                            1f);
                mat.popPose();
            }
        }
    }

    @Override
    public void setup(final FMLCommonSetupEvent event)
    {
        super.setup(event);
    }

    @Override
    public void setupClient(final FMLClientSetupEvent event)
    {
        this.UP = new KeyMapping("crafts.key.up", InputConstants.UNKNOWN.getValue(), "keys.crafts");
        this.DOWN = new KeyMapping("crafts.key.down", InputConstants.UNKNOWN.getValue(), "keys.crafts");

        final KeyConflictContext inGame = KeyConflictContext.IN_GAME;
        this.UP.setKeyConflictContext(inGame);
        this.DOWN.setKeyConflictContext(inGame);

        this.ROTATERIGHT = new KeyMapping("crafts.key.left", InputConstants.UNKNOWN.getValue(), "keys.crafts");
        this.ROTATELEFT = new KeyMapping("crafts.key.right", InputConstants.UNKNOWN.getValue(), "keys.crafts");
        this.ROTATELEFT.setKeyConflictContext(inGame);
        this.ROTATERIGHT.setKeyConflictContext(inGame);

        ClientRegistry.registerKeyBinding(this.UP);
        ClientRegistry.registerKeyBinding(this.DOWN);
        ClientRegistry.registerKeyBinding(this.ROTATELEFT);
        ClientRegistry.registerKeyBinding(this.ROTATERIGHT);
    }
}

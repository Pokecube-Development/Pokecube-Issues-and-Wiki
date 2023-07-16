package thut.crafts.init;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
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
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import thut.api.entity.blockentity.render.RenderBlockEntity;
import thut.api.maths.Vector3;
import thut.crafts.Reference;
import thut.crafts.ThutCrafts;
import thut.crafts.entity.CraftController;
import thut.crafts.entity.EntityCraft;
import thut.crafts.network.PacketCraftControl;

@Mod.EventBusSubscriber(bus = Bus.FORGE, value = Dist.CLIENT)
public class ClientInit
{
    static KeyMapping UP;
    static KeyMapping DOWN;
    static KeyMapping ROTATERIGHT;
    static KeyMapping ROTATELEFT;

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Reference.MODID, value = Dist.CLIENT)
    public static class RegistryEvents
    {
        @SubscribeEvent
        public static void registerRenderers(final RegisterRenderers event)
        {
            event.registerEntityRenderer(ThutCrafts.CRAFTTYPE.get(), RenderBlockEntity::new);
        }

        @SubscribeEvent
        public static void setupClient(final FMLClientSetupEvent event)
        {
            ClientInit.UP = new KeyMapping("crafts.key.up", InputConstants.UNKNOWN.getValue(), "keys.crafts");
            ClientInit.DOWN = new KeyMapping("crafts.key.down", InputConstants.UNKNOWN.getValue(), "keys.crafts");

            final KeyConflictContext inGame = KeyConflictContext.IN_GAME;
            ClientInit.UP.setKeyConflictContext(inGame);
            ClientInit.DOWN.setKeyConflictContext(inGame);

            ClientInit.ROTATERIGHT = new KeyMapping("crafts.key.left", InputConstants.UNKNOWN.getValue(),
                    "keys.crafts");
            ClientInit.ROTATELEFT = new KeyMapping("crafts.key.right", InputConstants.UNKNOWN.getValue(),
                    "keys.crafts");
            ClientInit.ROTATELEFT.setKeyConflictContext(inGame);
            ClientInit.ROTATERIGHT.setKeyConflictContext(inGame);

            ClientRegistry.registerKeyBinding(ClientInit.UP);
            ClientRegistry.registerKeyBinding(ClientInit.DOWN);
            ClientRegistry.registerKeyBinding(ClientInit.ROTATELEFT);
            ClientRegistry.registerKeyBinding(ClientInit.ROTATERIGHT);
        }
    }

    @SubscribeEvent
    public static void clientTick(final TickEvent.PlayerTickEvent event)
    {
        if (event.phase == Phase.START || event.player != Minecraft.getInstance().player) return;
        control:
        if (event.player.isPassenger() && Minecraft.getInstance().screen == null)
        {
            final Entity e = event.player.getVehicle();
            if (e instanceof EntityCraft)
            {
                final net.minecraft.client.player.LocalPlayer player = (net.minecraft.client.player.LocalPlayer) event.player;
                final CraftController controller = ((EntityCraft) e).controller;
                if (controller == null) break control;
                controller.backInputDown = player.input.down;
                controller.forwardInputDown = player.input.up;
                controller.leftInputDown = player.input.left;
                controller.rightInputDown = player.input.right;
                controller.upInputDown = ClientInit.UP.isDown();
                controller.downInputDown = ClientInit.DOWN.isDown();

                if (ThutCrafts.conf.canRotate)
                {
                    controller.rightRotateDown = ClientInit.ROTATERIGHT.isDown();
                    controller.leftRotateDown = ClientInit.ROTATELEFT.isDown();
                }
                PacketCraftControl.sendControlPacket(e, controller);
            }
        }
    }

    @SubscribeEvent
    public static void RenderBounds(final RenderLevelStageEvent event)
    {
        if (event.getStage() != Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        ItemStack held;
        final Player player = Minecraft.getInstance().player;
        if (!(held = player.getMainHandItem()).isEmpty() || !(held = player.getOffhandItem()).isEmpty())
        {
            if (held.getItem() != ThutCrafts.CRAFTMAKER.get()) return;
            if (held.getTag() != null && held.getTag().contains("min"))
            {
                final Minecraft mc = Minecraft.getInstance();
                final Vec3 projectedView = mc.gameRenderer.getMainCamera().getPosition();
                Vec3 pointed = new Vec3(projectedView.x, projectedView.y, projectedView.z)
                        .add(mc.player.getViewVector(event.getPartialTick()));
                if (mc.hitResult != null && mc.hitResult.getType() == Type.BLOCK)
                {
                    final BlockHitResult result = (BlockHitResult) mc.hitResult;
                    pointed = new Vec3(result.getBlockPos().getX(), result.getBlockPos().getY(),
                            result.getBlockPos().getZ());
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
                AABB box = new AABB(minX, minY, minZ, maxX, maxY, maxZ);

                final PoseStack matrix = event.getPoseStack();
                MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
                VertexConsumer builder = buffer.getBuffer(RenderType.LINES);
                Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
                matrix.pushPose();
                matrix.translate(-camera.x, -camera.y, -camera.z);
                LevelRenderer.renderLineBox(matrix, builder, box, 1.0F, 0.0F, 0.0F, 1.0F);
                matrix.popPose();
                buffer.endBatch(RenderType.LINES);
            }
        }
    }
}

package thut.crafts.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import thut.api.entity.blockentity.render.RenderBlockEntity;
import thut.api.maths.Vector3;
import thut.crafts.CommonProxy;
import thut.crafts.ThutCrafts;
import thut.crafts.entity.CraftController;
import thut.crafts.entity.EntityCraft;
import thut.crafts.network.PacketCraftControl;

public class ClientProxy extends CommonProxy
{
    KeyBinding UP;
    KeyBinding DOWN;
    KeyBinding ROTATERIGHT;
    KeyBinding ROTATELEFT;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void clientTick(final TickEvent.PlayerTickEvent event)
    {
        if (event.phase == Phase.START || event.player != Minecraft.getInstance().player) return;
        control:
        if (event.player.isPassenger() && Minecraft.getInstance().currentScreen == null)
        {
            final Entity e = event.player.getRidingEntity();
            if (e instanceof EntityCraft)
            {
                final ClientPlayerEntity player = (ClientPlayerEntity) event.player;
                final CraftController controller = ((EntityCraft) e).controller;
                if (controller == null) break control;
                controller.backInputDown = player.movementInput.backKeyDown;
                controller.forwardInputDown = player.movementInput.forwardKeyDown;
                controller.leftInputDown = player.movementInput.leftKeyDown;
                controller.rightInputDown = player.movementInput.rightKeyDown;
                controller.upInputDown = this.UP.isKeyDown();
                controller.downInputDown = this.DOWN.isKeyDown();
                if (ThutCrafts.conf.canRotate)
                {
                    controller.rightRotateDown = this.ROTATERIGHT.isKeyDown();
                    controller.leftRotateDown = this.ROTATELEFT.isKeyDown();
                }
                PacketCraftControl.sendControlPacket(e, controller);
            }
        }
    }

    @Override
    public PlayerEntity getPlayer()
    {
        return Minecraft.getInstance().player;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void RenderBounds(final DrawBlockHighlightEvent event)
    {
        if (!(event.getTarget() instanceof BlockRayTraceResult)) return;
        final BlockRayTraceResult target = (BlockRayTraceResult) event.getTarget();
        ItemStack held;
        final PlayerEntity player = Minecraft.getInstance().player;
        if (!(held = player.getHeldItemMainhand()).isEmpty() || !(held = player.getHeldItemOffhand()).isEmpty())
        {
            BlockPos pos = target.getPos();
            if (pos == null || held.getItem() != ThutCrafts.CRAFTMAKER) return;
            if (!player.world.getBlockState(pos).getMaterial().isSolid())
            {
                final Vec3d loc = player.getPositionVector().add(0, player.getEyeHeight(), 0).add(player.getLookVec()
                        .scale(2));
                pos = new BlockPos(loc);
            }

            if (held.getTag() != null && held.getTag().contains("min"))
            {
                BlockPos min = Vector3.readFromNBT(held.getTag().getCompound("min"), "").getPos();
                BlockPos max = pos;
                AxisAlignedBB box = new AxisAlignedBB(min, max);
                min = new BlockPos(box.minX, box.minY, box.minZ);
                max = new BlockPos(box.maxX, box.maxY, box.maxZ).add(1, 1, 1);
                box = new AxisAlignedBB(min, max);
                final float partialTicks = event.getPartialTicks();
                final double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
                final double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
                final double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
                box = box.offset(-d0, -d1 - player.getEyeHeight(), -d2);
                GlStateManager.enableBlend();
                GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                        GlStateManager.DestFactor.ZERO);
                GlStateManager.color4f(0.0F, 0.0F, 0.0F, 0.4F);
                GlStateManager.lineWidth(2.0F);
                GlStateManager.disableTexture();
                GlStateManager.depthMask(false);
                GlStateManager.color4f(1.0F, 0.0F, 0.0F, 1F);
                final Tessellator tessellator = Tessellator.getInstance();
                final BufferBuilder vertexbuffer = tessellator.getBuffer();
                vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
                vertexbuffer.pos(box.minX, box.minY, box.minZ).endVertex();
                vertexbuffer.pos(box.maxX, box.minY, box.minZ).endVertex();
                vertexbuffer.pos(box.maxX, box.minY, box.maxZ).endVertex();
                vertexbuffer.pos(box.minX, box.minY, box.maxZ).endVertex();
                vertexbuffer.pos(box.minX, box.minY, box.minZ).endVertex();
                tessellator.draw();
                vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
                vertexbuffer.pos(box.minX, box.maxY, box.minZ).endVertex();
                vertexbuffer.pos(box.maxX, box.maxY, box.minZ).endVertex();
                vertexbuffer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
                vertexbuffer.pos(box.minX, box.maxY, box.maxZ).endVertex();
                vertexbuffer.pos(box.minX, box.maxY, box.minZ).endVertex();
                tessellator.draw();
                vertexbuffer.begin(1, DefaultVertexFormats.POSITION);
                vertexbuffer.pos(box.minX, box.minY, box.minZ).endVertex();
                vertexbuffer.pos(box.minX, box.maxY, box.minZ).endVertex();
                vertexbuffer.pos(box.maxX, box.minY, box.minZ).endVertex();
                vertexbuffer.pos(box.maxX, box.maxY, box.minZ).endVertex();
                vertexbuffer.pos(box.maxX, box.minY, box.maxZ).endVertex();
                vertexbuffer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
                vertexbuffer.pos(box.minX, box.minY, box.maxZ).endVertex();
                vertexbuffer.pos(box.minX, box.maxY, box.maxZ).endVertex();
                tessellator.draw();
                GlStateManager.depthMask(true);
                GlStateManager.enableTexture();
                GlStateManager.disableBlend();
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
        this.UP = new KeyBinding("crafts.key.up", GLFW.GLFW_KEY_SPACE, "keys.crafts");
        this.DOWN = new KeyBinding("crafts.key.down", GLFW.GLFW_KEY_LEFT_CONTROL, "keys.crafts");

        final KeyConflictContext inGame = KeyConflictContext.IN_GAME;
        this.UP.setKeyConflictContext(inGame);
        this.DOWN.setKeyConflictContext(inGame);

        this.ROTATERIGHT = new KeyBinding("crafts.key.left", GLFW.GLFW_KEY_RIGHT_BRACKET, "keys.crafts");
        this.ROTATELEFT = new KeyBinding("crafts.key.right", GLFW.GLFW_KEY_LEFT_BRACKET, "keys.crafts");
        this.ROTATELEFT.setKeyConflictContext(inGame);
        this.ROTATERIGHT.setKeyConflictContext(inGame);

        ClientRegistry.registerKeyBinding(this.UP);
        ClientRegistry.registerKeyBinding(this.DOWN);
        ClientRegistry.registerKeyBinding(this.ROTATELEFT);
        ClientRegistry.registerKeyBinding(this.ROTATERIGHT);

        RenderingRegistry.registerEntityRenderingHandler(EntityCraft.class, (manager) -> new RenderBlockEntity<>(
                manager));
    }
}

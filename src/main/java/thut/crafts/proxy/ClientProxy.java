package thut.crafts.proxy;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
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
    public void RenderBounds(final RenderWorldLastEvent event)
    {
        ItemStack held;
        final PlayerEntity player = Minecraft.getInstance().player;
        if (!(held = player.getHeldItemMainhand()).isEmpty() || !(held = player.getHeldItemOffhand()).isEmpty())
        {
            if (held.getItem() != ThutCrafts.CRAFTMAKER) return;
            if (held.getTag() != null && held.getTag().contains("min"))
            {
                final Minecraft mc = Minecraft.getInstance();
                final Vector3d projectedView = mc.gameRenderer.getActiveRenderInfo().getProjectedView();
                Vector3d pointed = new Vector3d(projectedView.x, projectedView.y, projectedView.z).add(mc.player
                        .getLook(event.getPartialTicks()));
                if (mc.objectMouseOver != null && mc.objectMouseOver.getType() == Type.BLOCK)
                {
                    final BlockRayTraceResult result = (BlockRayTraceResult) mc.objectMouseOver;
                    pointed = new Vector3d(result.getPos().getX(), result.getPos().getY(), result.getPos().getZ());
                    //
                }
                final Vector3 v = Vector3.readFromNBT(held.getTag().getCompound("min"), "");

                final AxisAlignedBB one = new AxisAlignedBB(v.getPos());
                final AxisAlignedBB two = new AxisAlignedBB(new BlockPos(pointed));

                final double minX = Math.min(one.minX, two.minX);
                final double minY = Math.min(one.minY, two.minY);
                final double minZ = Math.min(one.minZ, two.minZ);
                final double maxX = Math.max(one.maxX, two.maxX);
                final double maxY = Math.max(one.maxY, two.maxY);
                final double maxZ = Math.max(one.maxZ, two.maxZ);

                final MatrixStack mat = event.getMatrixStack();
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

                mat.push();

                final Matrix4f positionMatrix = mat.getLast().getMatrix();

                final IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
                final IVertexBuilder builder = buffer.getBuffer(RenderType.LINES);
                for (final Pair<Vector3f, Vector3f> line : lines)
                    thut.core.proxy.ClientProxy.line(builder, positionMatrix, line.getLeft(), line.getRight(), 1, 0, 0,
                            1f);
                mat.pop();
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
        this.UP = new KeyBinding("crafts.key.up", InputMappings.INPUT_INVALID.getKeyCode(), "keys.crafts");
        this.DOWN = new KeyBinding("crafts.key.down", InputMappings.INPUT_INVALID.getKeyCode(), "keys.crafts");

        final KeyConflictContext inGame = KeyConflictContext.IN_GAME;
        this.UP.setKeyConflictContext(inGame);
        this.DOWN.setKeyConflictContext(inGame);

        this.ROTATERIGHT = new KeyBinding("crafts.key.left", InputMappings.INPUT_INVALID.getKeyCode(), "keys.crafts");
        this.ROTATELEFT = new KeyBinding("crafts.key.right", InputMappings.INPUT_INVALID.getKeyCode(), "keys.crafts");
        this.ROTATELEFT.setKeyConflictContext(inGame);
        this.ROTATERIGHT.setKeyConflictContext(inGame);

        ClientRegistry.registerKeyBinding(this.UP);
        ClientRegistry.registerKeyBinding(this.DOWN);
        ClientRegistry.registerKeyBinding(this.ROTATELEFT);
        ClientRegistry.registerKeyBinding(this.ROTATERIGHT);

        RenderingRegistry.registerEntityRenderingHandler(EntityCraft.CRAFTTYPE, RenderBlockEntity::new);
    }
}

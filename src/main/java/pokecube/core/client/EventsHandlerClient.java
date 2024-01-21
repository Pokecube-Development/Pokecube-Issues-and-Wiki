package pokecube.core.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.InputEvent.RawMouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.client.event.ScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.ScreenEvent.MouseScrollEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty;
import net.minecraftforge.eventbus.api.EventPriority;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IHasCommands.Command;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.FormeHolder;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.commandhandlers.ChangeFormHandler;
import pokecube.api.entity.pokemob.commandhandlers.StanceHandler;
import pokecube.api.utils.TagNames;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.logic.LogicMountedControl;
import pokecube.core.client.gui.AnimationGui;
import pokecube.core.client.gui.GuiArranger;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.client.gui.GuiInfoMessages;
import pokecube.core.client.gui.GuiTeleport;
import pokecube.core.client.render.mobs.RenderMobOverlays;
import pokecube.core.client.render.mobs.RenderPokemob;
import pokecube.core.database.Database;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.entity.pokecubes.EntityPokecubeBase;
import pokecube.core.impl.capabilities.DefaultPokemob;
import pokecube.core.init.ClientSetupHandler;
import pokecube.core.items.pokecubes.Pokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.moves.animations.MoveAnimationHelper;
import pokecube.core.network.pokemobs.PacketBattleTargets;
import pokecube.core.network.pokemobs.PacketCommand;
import pokecube.core.network.pokemobs.PacketMountedControl;
import pokecube.core.utils.PokemobTracker;
import thut.core.common.ThutCore;

public class EventsHandlerClient
{
    public static HashMap<PokedexEntry, IPokemob> renderMobs = new HashMap<>();

    static long lastSetTime = 0;
    public static Entity hovorTarget = null;

    /**
     * In here we register all of the methods for the event listening, this is
     * to keep better track of what events we listen for, and to include notes
     * as to what each event is tracking.
     */
    public static void register()
    {
        // This handles ridden input controls, auto-recalling of pokemobs, and
        // auto-selection of moves.
        ThutCore.FORGE_BUS.addListener(EventsHandlerClient::onPlayerTick);

        // This one handles allowing the player to interact with mobs which are
        // larger than the vanilla hitboxes.
        ThutCore.FORGE_BUS.addListener(EventPriority.LOWEST, EventsHandlerClient::onMouseInput);

        // This one handles scrolling the message display while in chat.
        ThutCore.FORGE_BUS.addListener(EventPriority.LOWEST, EventsHandlerClient::onMouseScroll);

        // Here we handle the various keybindings for the mod
        ThutCore.FORGE_BUS.addListener(EventsHandlerClient::onKeyInput);

        // This renders the pokemob's icons over the pokecubes when alt is held
        // in an inventory.
        ThutCore.FORGE_BUS.addListener(EventPriority.LOWEST, EventsHandlerClient::onRenderGUIScreenPre);
        // And this does it for the hotbar.
        ThutCore.FORGE_BUS.addListener(EventPriority.LOWEST, EventsHandlerClient::onRenderHotbar);

        // Now for some additional client side handlers

        // Register the handler for drawing things like evolution, etc
        ThutCore.FORGE_BUS.addListener(RenderMobOverlays::renderPost);
        ThutCore.FORGE_BUS.addListener(RenderMobOverlays::renderPre);
        ThutCore.FORGE_BUS.addListener(RenderMobOverlays::renderNameplate);
        // Register the handler for drawing selected box around targeted
        // entities for throwing cubes at
        ThutCore.FORGE_BUS.addListener(EventsHandlerClient::renderBounds);
        // Used to dismount shoulder mobs
        ThutCore.FORGE_BUS.addListener(EventsHandlerClient::onLeftClickEmpty);

        // Initialise this gui
        GuiDisplayPokecubeInfo.instance();
        MoveAnimationHelper.Instance();
    }

    /**
     * Gets all pokemobs owned by owner within the given distance.
     *
     * @param owner
     * @param distance
     * @return
     */
    public static List<IPokemob> getPokemobs(final LivingEntity owner, final double distance)
    {
        final List<IPokemob> ret = new ArrayList<>();

        for (final Entity e : PokemobTracker.getMobs(owner, e -> !(e instanceof EntityPokecubeBase)))
        {
            final IPokemob mob = PokemobCaps.getPokemobFor(e);
            if (mob != null) ret.add(mob);
        }
        return ret;
    }

    private static void onPlayerTick(final TickEvent.PlayerTickEvent event)
    {
        if (event.phase == Phase.START || event.player != Minecraft.getInstance().player) return;
        IPokemob pokemob = GuiDisplayPokecubeInfo.instance().getCurrentPokemob();
        if (pokemob != null && PokecubeCore.getConfig().autoSelectMoves)
        {
            Entity target = pokemob.getEntity().getLevel().getEntity(pokemob.getTargetID());
            if (target instanceof LivingEntity living && !pokemob.getGeneralState(GeneralStates.MATING))
                EventsHandlerClient.setMostDamagingMove(pokemob, living);
        }
        if (PokecubeCore.getConfig().autoRecallPokemobs)
        {
            final IPokemob mob = GuiDisplayPokecubeInfo.instance().getCurrentPokemob();
            if (mob != null && mob.getEntity().isAlive() && mob.getEntity().isAddedToWorld()
                    && event.player.distanceTo(mob.getEntity()) > PokecubeCore.getConfig().autoRecallDistance)
                mob.onRecall();
        }
        control:
        if (event.player.isPassenger() && Minecraft.getInstance().screen == null
                && event.player == Minecraft.getInstance().player)
        {
            final Entity e = event.player.getVehicle();
            pokemob = PokemobCaps.getPokemobFor(e);
            if (pokemob != null && e.getControllingPassenger() == event.player)
            {
                final LogicMountedControl controller = pokemob.getController();
                if (controller == null) break control;
                final net.minecraft.client.player.LocalPlayer player = (net.minecraft.client.player.LocalPlayer) event.player;
                controller.backInputDown = player.input.down;
                controller.forwardInputDown = player.input.up;
                controller.leftInputDown = player.input.left;
                controller.rightInputDown = player.input.right;

                final boolean up = ClientSetupHandler.mobUp.isDown();
                final boolean down = ClientSetupHandler.mobDown.isDown();

                controller.upInputDown = up;
                controller.downInputDown = down;
                controller.followOwnerLook = PokecubeCore.getConfig().riddenMobsAscendWithLook;
                controller.canPathWhileRidden = PokecubeCore.getConfig().riddenMobsTryPathing;

                if (ClientSetupHandler.throttleDown.isDown())
                {
                    controller.throttle -= 0.05;
                    controller.throttle = Math.max(controller.throttle, 0.01);
                }
                else if (ClientSetupHandler.throttleUp.isDown())
                {
                    controller.throttle += 0.05;
                    controller.throttle = Math.min(controller.throttle, 1);
                }
                PacketMountedControl.sendControlPacket(e, controller);
            }
        }
        long now = System.currentTimeMillis();
        if (lastSetTime < now)
        {
            var selector = GuiDisplayPokecubeInfo.instance().getAttackSelector();
            hovorTarget = Tools.getPointedEntity(event.player, 32, selector, 1);
            EventsHandlerClient.lastSetTime = now + 250;
        }
        if (hovorTarget != null && !hovorTarget.isAddedToWorld()) hovorTarget = null;
    }

    private static void onMouseInput(final RawMouseEvent evt)
    {
        final Player player = Minecraft.getInstance().player;
        // We only handle these ingame anyway.
        if (player == null) return;
        //
        if (evt.getAction() == GLFW.GLFW_PRESS && evt.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT
                && (Minecraft.getInstance().hitResult == null
                        || Minecraft.getInstance().hitResult.getType() == Type.MISS))
        {
            final Entity entity = Tools.getPointedEntity(player, 6);
            if (entity != null) hands:
            for (final InteractionHand hand : InteractionHand.values())
                if (Minecraft.getInstance().gameMode.interact(player, entity, hand) == InteractionResult.SUCCESS)
            {
                evt.setCanceled(true);
                break hands;
            }
        }
        for (var comp : GuiDisplayPokecubeInfo.COMPONENTS)
            if (comp.handleClick(evt.getAction(), evt.getButton(), evt.getModifiers())) break;
    }

    private static void onMouseScroll(MouseScrollEvent.Pre event)
    {
        if (!GuiInfoMessages.fullDisplay()) return;
        if (event.getScrollDelta() > 0) GuiDisplayPokecubeInfo.messageRenderer.offset++;
        if (event.getScrollDelta() < 0) GuiDisplayPokecubeInfo.messageRenderer.offset--;
    }

    private static void onLeftClickEmpty(final LeftClickEmpty event)
    {
        if (Screen.hasShiftDown() && !Minecraft.getInstance().player.getPassengers().isEmpty())
        {
            for (Entity e : Minecraft.getInstance().player.getPassengers())
            {
                IPokemob poke = PokemobCaps.getPokemobFor(e);
                if (poke != null)
                {
                    PacketCommand.sendCommand(poke, Command.STANCE,
                            new StanceHandler(false, StanceHandler.SIT).setFromOwner(true));
                }
            }
        }
    }

    private static final ResourceLocation IS_POKECUBE = new ResourceLocation("pokecube:pokecubes");

    private static void renderBounds(final RenderLevelStageEvent event)
    {
        boolean alt = Screen.hasAltDown();
        boolean ctrl = Screen.hasControlDown();
        Pokecube.renderingOverlay = alt || ctrl;

        if (event.getStage() != Stage.AFTER_SOLID_BLOCKS || !PokecubeCore.getConfig().showTargetBox) return;
        final Player player = Minecraft.getInstance().player;

        boolean validToShow = true;
        ItemStack held;
        if (!(held = player.getMainHandItem()).isEmpty() || (held = player.getOffhandItem()).isEmpty())
        {
            validToShow = PokecubeItems.is(IS_POKECUBE, held);
        }
        if (!validToShow) validToShow = GuiDisplayPokecubeInfo.instance().getCurrentPokemob() != null;

        if (validToShow)
        {
            Entity entity = hovorTarget;
            if (entity != null)
            {
                AABB box = entity.getBoundingBox().move(-entity.getX(), -entity.getY(), -entity.getZ());
                final PoseStack matrix = event.getPoseStack();
                float f = Minecraft.getInstance().getDeltaFrameTime();
                double x = Mth.lerp(f, entity.xOld, entity.getX());
                double y = Mth.lerp(f, entity.yOld, entity.getY());
                double z = Mth.lerp(f, entity.zOld, entity.getZ());
                MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
                VertexConsumer builder = buffer.getBuffer(RenderType.LINES);
                Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
                matrix.pushPose();
                matrix.translate(x - camera.x, y - camera.y, z - camera.z);
                LevelRenderer.renderLineBox(matrix, builder, box, 1.0F, 0.0F, 0.0F, 1.0F);
                matrix.popPose();
                buffer.endBatch(RenderType.LINES);
            }
        }
    }

    private static void onKeyInput(final KeyInputEvent evt)
    {
        final Player player = Minecraft.getInstance().player;
        // We only handle these ingame anyway.
        if (player == null) return;
        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_F3)
                && evt.getKey() == GLFW.GLFW_KEY_D)
            GuiInfoMessages.clear();
        if (evt.getKey() == GLFW.GLFW_KEY_F5)
        {
            if (AnimationGui.entry != null && Minecraft.getInstance().screen instanceof AnimationGui)
            {
                PokedexEntryLoader.updateEntry(AnimationGui.entry);
                RenderPokemob.reloadModel(AnimationGui.entry);
            }
            else if (player.getVehicle() != null && Minecraft.getInstance().screen != null)
            {
                final IPokemob pokemob = PokemobCaps.getPokemobFor(player.getVehicle());
                if (pokemob != null) PokedexEntryLoader.updateEntry(pokemob.getPokedexEntry());
            }
        }
        if (ClientSetupHandler.animateGui.consumeClick() && Minecraft.getInstance().screen == null)
            Minecraft.getInstance().setScreen(new AnimationGui());
        if (ClientSetupHandler.mobMegavolve.consumeClick())
        {
            final IPokemob current = GuiDisplayPokecubeInfo.instance().getCurrentPokemob();
            if (current != null && !current.getGeneralState(GeneralStates.EVOLVING))
                PacketCommand.sendCommand(current, Command.CHANGEFORM, new ChangeFormHandler());
        }
        if (ClientSetupHandler.arrangeGui.consumeClick()) GuiArranger.toggle = !GuiArranger.toggle;
        if (ClientSetupHandler.noEvolve.consumeClick() && GuiDisplayPokecubeInfo.instance().getCurrentPokemob() != null)
            GuiDisplayPokecubeInfo.instance().getCurrentPokemob().cancelEvolve();
        if (ClientSetupHandler.nextMob.consumeClick()) GuiDisplayPokecubeInfo.instance().nextPokemob();
        if (ClientSetupHandler.previousMob.consumeClick()) GuiDisplayPokecubeInfo.instance().previousPokemob();
        if (ClientSetupHandler.nextMove.consumeClick())
        {
            int num = Screen.hasControlDown() ? 2 : 1;
            if (Screen.hasShiftDown()) num++;
            if (GuiTeleport.instance().getState()) GuiTeleport.instance().nextMove();
            else GuiDisplayPokecubeInfo.instance().nextMove(num);
        }
        if (ClientSetupHandler.previousMove.consumeClick())
        {
            int num = Screen.hasControlDown() ? 2 : 1;
            if (Screen.hasShiftDown()) num++;
            if (GuiTeleport.instance().getState()) GuiTeleport.instance().previousMove();
            else GuiDisplayPokecubeInfo.instance().previousMove(num);
        }
        if (ClientSetupHandler.mobBack.consumeClick())
            if (GuiTeleport.instance().getState()) GuiTeleport.instance().setState(false);
            else GuiDisplayPokecubeInfo.instance().pokemobBack();
        if (ClientSetupHandler.mobAttack.consumeClick()) GuiDisplayPokecubeInfo.instance().pokemobAttack();
        if (ClientSetupHandler.mobStance.consumeClick()) GuiDisplayPokecubeInfo.instance().pokemobStance();
        if (ClientSetupHandler.mobMove1.consumeClick()) GuiDisplayPokecubeInfo.instance().setMove(0);
        if (ClientSetupHandler.mobMove2.consumeClick()) GuiDisplayPokecubeInfo.instance().setMove(1);
        if (ClientSetupHandler.mobMove3.consumeClick()) GuiDisplayPokecubeInfo.instance().setMove(2);
        if (ClientSetupHandler.mobMove4.consumeClick()) GuiDisplayPokecubeInfo.instance().setMove(3);

        final IPokemob current = GuiDisplayPokecubeInfo.instance().getCurrentPokemob();
        if (current != null)
        {
            if (ClientSetupHandler.previousAlly.consumeClick()) PacketBattleTargets.cycleAlly(current, true);
            if (ClientSetupHandler.nextAlly.consumeClick()) PacketBattleTargets.cycleAlly(current, false);
            if (ClientSetupHandler.previousTarget.consumeClick()) PacketBattleTargets.cycleEnemy(current, true);
            if (ClientSetupHandler.nextTarget.consumeClick()) PacketBattleTargets.cycleEnemy(current, false);

            if (ClientSetupHandler.gzmove.consumeClick())
            {
                PacketCommand.sendCommand(current, Command.STANCE,
                        new StanceHandler(true, StanceHandler.MODE).setFromOwner(true));
            }
        }
    }

    private static void onRenderGUIScreenPre(final DrawScreenEvent.Post event)
    {
        try
        {
            if (!(event.getScreen() instanceof AbstractContainerScreen<?> gui)) return;
            boolean alt = Screen.hasAltDown();
            boolean ctrl = Screen.hasControlDown();
            if (alt || ctrl)
            {
                final List<Slot> slots = gui.getMenu().slots;
                for (final Slot slot : slots) if (slot.hasItem() && PokecubeManager.isFilled(slot.getItem()))
                {
                    final IPokemob pokemob = EventsHandlerClient.getPokemobForRender(slot.getItem(),
                            gui.getMinecraft().level);
                    if (pokemob == null) continue;
                    int i, j;
                    i = slot.x;
                    j = slot.y;
                    final int x = i + gui.getGuiLeft();
                    final int y = j + gui.getGuiTop();
                    if (ctrl)
                    {
                        final float z = Minecraft.getInstance().getItemRenderer().blitOffset;
                        Minecraft.getInstance().getItemRenderer().blitOffset += 200;
                        Minecraft.getInstance().getItemRenderer().renderGuiItem(pokemob.getHeldItem(), x, y);
                        Minecraft.getInstance().getItemRenderer().blitOffset = z;
                    }
                    else EventsHandlerClient.renderIcon(pokemob, x, y, 16, 16);
                }
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void onRenderHotbar(final RenderGameOverlayEvent.Post event)
    {
        if (event.getType() == ElementType.LAYER)
        {
            if (!Screen.hasAltDown()) return;
            final Player player = Minecraft.getInstance().player;
            final int w = event.getWindow().getGuiScaledWidth();
            final int h = event.getWindow().getGuiScaledHeight();
            int i, j;
            i = -80;
            j = -9;
            for (int l = 0; l < 9; l++)
            {
                final ItemStack stack = player.getInventory().items.get(l);
                if (stack != null && PokecubeManager.isFilled(stack))
                {
                    final IPokemob pokemob = EventsHandlerClient.getPokemobForRender(stack, player.getLevel());
                    if (pokemob == null) continue;
                    int x = w / 2;
                    x = i + x + 20 * l - 8;
                    int y = h;
                    y = j + y - 9;
                    if (Screen.hasControlDown())
                    {
                        final float z = Minecraft.getInstance().getItemRenderer().blitOffset;
                        Minecraft.getInstance().getItemRenderer().blitOffset += 100;
                        Minecraft.getInstance().getItemRenderer().renderGuiItem(pokemob.getHeldItem(), x, y);
                        Minecraft.getInstance().getItemRenderer().blitOffset = z;
                    }
                    else EventsHandlerClient.renderIcon(pokemob, x, y, 16, 16);
                }
            }
        }
    }

    public static IPokemob getPokemobForRender(final ItemStack itemStack, final Level world)
    {
        if (!itemStack.hasTag()) return null;
        final PokedexEntry entry = PokecubeManager.getPokedexEntry(itemStack);
        if (entry != null && entry != Database.missingno)
        {
            final IPokemob pokemob = EventsHandlerClient.getRenderMob(entry, world);
            if (pokemob == null) return null;
            final CompoundTag pokeTag = itemStack.getTag();
            EventsHandlerClient.setFromNBT(pokemob, pokeTag);
            pokemob.setPokecube(itemStack);
            pokemob.setStatus(null, PokecubeManager.getStatus(itemStack));
            pokemob.getEntity().clearFire();
            return pokemob;
        }
        return null;
    }

    public static IPokemob getRenderMob(final PokedexEntry entry, final Level world)
    {
        IPokemob pokemob = EventsHandlerClient.renderMobs.get(entry);
        if (pokemob != null) pokemob = pokemob.setPokedexEntry(entry);
        if (pokemob == null || pokemob != EventsHandlerClient.renderMobs.get(entry))
        {
            if (pokemob == null) pokemob = PokemobCaps.getPokemobFor(PokecubeCore.createPokemob(entry, world));
            if (pokemob == null) return null;
            pokemob.spawnInit();
            EventsHandlerClient.renderMobs.put(entry, pokemob);
        }
        return pokemob;
    }

    public static void renderIcon(final IPokemob realMob, final int left, final int top, final int width,
            final int height)
    {
        final PokedexEntry entry = realMob.getPokedexEntry();
        EventsHandlerClient.renderIcon(entry, realMob.getCustomHolder(), realMob.getSexe() == IPokemob.FEMALE, left,
                top, width, height, realMob.isShiny());
    }

    public static void renderIcon(final PokedexEntry entry, final FormeHolder holder, final boolean female, int left,
            int top, final int width, final int height, final boolean shiny)
    {
        int right = left + width;
        int bottom = top + height;

        if (left < right)
        {
            final int i1 = left;
            left = right;
            right = i1;
        }

        if (top < bottom)
        {
            final int j1 = top;
            top = bottom;
            bottom = j1;
        }
        ResourceLocation icon = entry.getIcon(!female, shiny);
        if (holder != null) icon = holder.getIcon(!female, shiny, entry);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, icon);

        Minecraft.getInstance().getTextureManager().getTexture(icon).setFilter(false, false);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        Lighting.setupForFlatItems();

        final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuilder();

        final int zLevel = 300;
        bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(left, bottom, zLevel).uv(0, 0).endVertex();
        bufferbuilder.vertex(right, bottom, zLevel).uv(1, 0).endVertex();
        bufferbuilder.vertex(right, top, zLevel).uv(1, 1).endVertex();
        bufferbuilder.vertex(left, top, zLevel).uv(0, 1).endVertex();
        tessellator.end();

        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();
    }

    public static void setFromNBT(final IPokemob pokemob, final CompoundTag tag)
    {
        final CompoundTag pokemobTag = TagNames.getPokecubePokemobTag(tag);
        final Tag genesTag = TagNames.getPokecubeGenesTag(tag);
        pokemobTag.remove(TagNames.AITAG);
        pokemobTag.remove(TagNames.MOVESTAG);
        pokemob.setHealth(tag.getFloat("CHP"));
        pokemob.read(pokemobTag);
        if (pokemob instanceof DefaultPokemob) try
        {
            final DefaultPokemob poke = (DefaultPokemob) pokemob;
            poke.getGenes().deserializeNBT((ListTag) genesTag);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        pokemob.onGenesChanged();
    }

    private static void setMostDamagingMove(final IPokemob outMob, final LivingEntity target)
    {
        int index = outMob.getMoveIndex();
        int max = 0;
        final String[] moves = outMob.getMoves();
        for (int i = 0; i < 4; i++)
        {
            final String s = moves[i];
            if (s != null)
            {
                final int temp = Tools.getPower(s, outMob, target);
                if (temp > max)
                {
                    index = i;
                    max = temp;
                }
            }
        }
        if (index != outMob.getMoveIndex()) GuiDisplayPokecubeInfo.instance().setMove(index);
    }

}

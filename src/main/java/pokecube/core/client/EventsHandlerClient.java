package pokecube.core.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.InputEvent.RawMouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ScreenEvent.DrawScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.logic.LogicMountedControl;
import pokecube.core.client.gui.AnimationGui;
import pokecube.core.client.gui.GuiArranger;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.client.gui.GuiInfoMessages;
import pokecube.core.client.gui.GuiTeleport;
import pokecube.core.client.render.mobs.RenderMobOverlays;
import pokecube.core.client.render.mobs.RenderPokemob;
import pokecube.core.client.render.mobs.ShoulderLayer;
import pokecube.core.client.render.mobs.ShoulderLayer.ShoulderHolder;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.init.ClientSetupHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.FormeHolder;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.capabilities.DefaultPokemob;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.commandhandlers.ChangeFormHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.StanceHandler;
import pokecube.core.items.pokecubes.EntityPokecubeBase;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.moves.animations.MoveAnimationHelper;
import pokecube.core.network.pokemobs.PacketCommand;
import pokecube.core.network.pokemobs.PacketMountedControl;
import pokecube.core.proxy.ClientProxy;
import pokecube.core.utils.PokemobTracker;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;

public class EventsHandlerClient
{
    public static HashMap<PokedexEntry, IPokemob> renderMobs = new HashMap<>();

    private static final Set<PlayerRenderer> addedLayers = Sets.newHashSet();

    static long lastSetTime = 0;

    /**
     * In here we register all of the methods for the event listening, this is
     * to keep better track of what events we listen for, and to include notes
     * as to what each event is tracking.
     */
    public static void register()
    {
        // This handles ridden input controls, auto-recalling of pokemobs, and
        // auto-selection of moves.
        MinecraftForge.EVENT_BUS.addListener(EventsHandlerClient::onPlayerTick);

        // Here we remove the fog from rendering if we are riding a pokemob that
        // can dive, and we are under water.
        MinecraftForge.EVENT_BUS.addListener(EventsHandlerClient::onFogRender);

        // This one handles allowing the player to interact with mobs which are
        // larger than the vanilla hitboxes.
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, EventsHandlerClient::onMouseInput);

        // Here we handle the various keybindings for the mod
        MinecraftForge.EVENT_BUS.addListener(EventsHandlerClient::onKeyInput);

        // This adds the "ShoulderLayer" to player renderer for rendering
        // pokemobs on shoulders.
        MinecraftForge.EVENT_BUS.addListener(EventsHandlerClient::onPlayerRender);

        // Here we add the ShoulderHolder capability for the above mentioned
        // render layer
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, EventsHandlerClient::onCapabilityAttach);

        // This renders the pokemob's icons over the pokecubes when alt is held
        // in an inventory.
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, EventsHandlerClient::onRenderGUIScreenPre);
        // And this does it for the hotbar.
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, EventsHandlerClient::onRenderHotbar);

        // Now for some additional client side handlers

        // Register the event for drawing the move messages
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, GuiInfoMessages::draw);
        // Register the handler for drawing things like evolution, etc
        MinecraftForge.EVENT_BUS.addListener(RenderMobOverlays::renderSpecial);

        // Initialise this gui
        GuiDisplayPokecubeInfo.instance();
        MoveAnimationHelper.Instance();

        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, EventsHandlerClient::serverAboutToStart);
    }

    private static void serverAboutToStart(final ServerAboutToStartEvent event)
    {
        ClientProxy.pokecenter_sounds.clear();
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
            final IPokemob mob = CapabilityPokemob.getPokemobFor(e);
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
            final Entity target = BrainUtils.getAttackTarget(pokemob.getEntity());
            if (target != null && !pokemob.getGeneralState(GeneralStates.MATING)) EventsHandlerClient
                    .setMostDamagingMove(pokemob, target);
        }
        if (PokecubeCore.getConfig().autoRecallPokemobs)
        {
            final IPokemob mob = GuiDisplayPokecubeInfo.instance().getCurrentPokemob();
            if (mob != null && mob.getEntity().isAlive() && mob.getEntity().isAddedToWorld() && event.player.distanceTo(
                    mob.getEntity()) > PokecubeCore.getConfig().autoRecallDistance) mob.onRecall();
        }
        control:
        if (event.player.isPassenger() && Minecraft.getInstance().screen == null)
        {
            final Entity e = event.player.getVehicle();
            pokemob = CapabilityPokemob.getPokemobFor(e);
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
                controller.followOwnerLook = PokecubeCore.getConfig().riddenMobsTurnWithLook;
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
        EventsHandlerClient.lastSetTime = System.currentTimeMillis() + 500;
    }

    private static void onFogRender(final EntityViewRenderEvent.FogDensity evt)
    {
//        IPokemob mount;
//
//        if (evt.getCamera().getEntity() instanceof Player && evt.getCamera().getEntity().getVehicle() != null
//                && (mount = CapabilityPokemob.getPokemobFor(evt.getCamera().getEntity().getVehicle())) != null) if (evt
//                        .getCamera().getEntity().isInWater() && mount.canUseDive())
//        {
//            evt.setDensity(0.0f);
//            evt.setCanceled(true);
//        }
    }

    private static void onMouseInput(final RawMouseEvent evt)
    {
        final Player player = Minecraft.getInstance().player;
        // We only handle these ingame anyway.
        if (player == null) return;
        //
        if (evt.getAction() == GLFW.GLFW_PRESS && evt.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) if (Minecraft
                .getInstance().hitResult == null || Minecraft.getInstance().hitResult.getType() == Type.MISS)
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
    }

    private static void onKeyInput(final KeyInputEvent evt)
    {
        final Player player = Minecraft.getInstance().player;
        // We only handle these ingame anyway.
        if (player == null) return;
        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_F3) && evt
                .getKey() == GLFW.GLFW_KEY_D) GuiInfoMessages.clear();

        if (evt.getKey() == GLFW.GLFW_KEY_F5) if (AnimationGui.entry != null && Minecraft
                .getInstance().screen instanceof AnimationGui)
        {
            PokedexEntryLoader.updateEntry(AnimationGui.entry);
            RenderPokemob.reloadModel(AnimationGui.entry);
        }
        else if (player.getVehicle() != null && Minecraft.getInstance().screen != null)
        {
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(player.getVehicle());
            if (pokemob != null) PokedexEntryLoader.updateEntry(pokemob.getPokedexEntry());
        }
        if (ClientSetupHandler.animateGui.consumeClick() && Minecraft.getInstance().screen == null) Minecraft
                .getInstance().setScreen(new AnimationGui());
        if (ClientSetupHandler.mobMegavolve.consumeClick())
        {
            final IPokemob current = GuiDisplayPokecubeInfo.instance().getCurrentPokemob();
            if (current != null && !current.getGeneralState(GeneralStates.EVOLVING)) PacketCommand.sendCommand(current,
                    Command.CHANGEFORM, new ChangeFormHandler());
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
        if (ClientSetupHandler.mobBack.consumeClick()) if (GuiTeleport.instance().getState()) GuiTeleport.instance()
                .setState(false);
        else GuiDisplayPokecubeInfo.instance().pokemobBack();
        if (ClientSetupHandler.mobAttack.consumeClick()) GuiDisplayPokecubeInfo.instance().pokemobAttack();
        if (ClientSetupHandler.mobStance.consumeClick()) GuiDisplayPokecubeInfo.instance().pokemobStance();
        if (ClientSetupHandler.mobMove1.consumeClick()) GuiDisplayPokecubeInfo.instance().setMove(0);
        if (ClientSetupHandler.mobMove2.consumeClick()) GuiDisplayPokecubeInfo.instance().setMove(1);
        if (ClientSetupHandler.mobMove3.consumeClick()) GuiDisplayPokecubeInfo.instance().setMove(2);
        if (ClientSetupHandler.mobMove4.consumeClick()) GuiDisplayPokecubeInfo.instance().setMove(3);

        if (ClientSetupHandler.gzmove.consumeClick())
        {
            final IPokemob current = GuiDisplayPokecubeInfo.instance().getCurrentPokemob();
            if (current != null) PacketCommand.sendCommand(current, Command.STANCE, new StanceHandler(!current
                    .getCombatState(CombatStates.USINGGZMOVE), StanceHandler.GZMOVE).setFromOwner(true));
        }
    }

    private static void onPlayerRender(final RenderPlayerEvent.Post event)
    {
        if (EventsHandlerClient.addedLayers.contains(event.getRenderer())) return;
        event.getRenderer().addLayer(new ShoulderLayer<>(event.getRenderer()));
        EventsHandlerClient.addedLayers.add(event.getRenderer());
    }

    private static void onCapabilityAttach(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player) event.addCapability(new ResourceLocation("pokecube:shouldermobs"),
                new ShoulderHolder((Player) event.getObject()));
    }

    private static void onRenderGUIScreenPre(final DrawScreenEvent.Post event)
    {
        try
        {
            if (!(event.getScreen() instanceof AbstractContainerScreen)) return;
            if (!Screen.hasAltDown()) return;
            final AbstractContainerScreen<?> gui = (AbstractContainerScreen<?>) event.getScreen();
            final List<Slot> slots = gui.getMenu().slots;
            for (final Slot slot : slots)
                if (slot.hasItem() && PokecubeManager.isFilled(slot.getItem()))
                {
                    final IPokemob pokemob = EventsHandlerClient.getPokemobForRender(slot.getItem(), gui
                            .getMinecraft().level);
                    if (pokemob == null) continue;

                    int i, j;
                    i = slot.x;
                    j = slot.y;
                    final int x = i + gui.getGuiLeft();
                    final int y = j + gui.getGuiTop();
                    if (Screen.hasControlDown())
                    {
                        final float z = Minecraft.getInstance().getItemRenderer().blitOffset;
                        Minecraft.getInstance().getItemRenderer().blitOffset += 200;
                        Minecraft.getInstance().getItemRenderer().renderGuiItem(pokemob.getHeldItem(), x, y - 2);
                        Minecraft.getInstance().getItemRenderer().blitOffset = z;
                    }
                    else EventsHandlerClient.renderIcon(pokemob, x, y, 16, 16);
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
                    final IPokemob pokemob = EventsHandlerClient.getPokemobForRender(stack, player
                            .getLevel());
                    if (pokemob == null) continue;
                    int x = w / 2;
                    x = i + x + 20 * l - 8;
                    int y = h;
                    y = j + y - 9;
                    if (Screen.hasControlDown())
                    {
                        final float z = Minecraft.getInstance().getItemRenderer().blitOffset;
                        Minecraft.getInstance().getItemRenderer().blitOffset += 100;
                        Minecraft.getInstance().getItemRenderer().renderGuiItem(pokemob.getHeldItem(), x, y - 2);
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
            pokemob.setStatus(PokecubeManager.getStatus(itemStack));
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
            if (pokemob == null) pokemob = CapabilityPokemob.getPokemobFor(PokecubeCore.createPokemob(entry, world));
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

        // RenderSystem.pushMatrix();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, icon);

        Minecraft.getInstance().getTextureManager().getTexture(icon).setFilter(false, false);

        // RenderSystem.enableRescaleNormal();
        // RenderSystem.enableAlphaTest();
        // RenderSystem.defaultAlphaFunc();
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
        //
        // RenderSystem.disableAlphaTest();
        // RenderSystem.disableRescaleNormal();
        // RenderSystem.popMatrix();
    }

    // private static Map<ResourceLocation, RenderType> cache =
    // Maps.newHashMap();
    //
    // private static RenderType getRenderTypeForIcon(final ResourceLocation
    // icon)
    // {
    // if (EventsHandlerClient.cache.containsKey(icon)) return
    // EventsHandlerClient.cache.get(icon);
    // else
    // {
    // final RenderType.State rendertype$state =
    // RenderType.State.builder().setTextureState(
    // new RenderState.TextureState(icon, false,
    // false)).setAlphaState(RenderState.DEFAULT_ALPHA)
    // .setLayeringState(RenderState.VIEW_OFFSET_Z_LAYERING).setTransparencyState(
    // RenderState.TRANSLUCENT_TRANSPARENCY).setOutputState(RenderState.ITEM_ENTITY_TARGET)
    // .setWriteMaskState(RenderState.COLOR_DEPTH_WRITE).createCompositeState(true);
    // EventsHandlerClient.cache.put(icon, RenderType.create("_icon_" + icon,
    // DefaultVertexFormats.POSITION_TEX, 7,
    // 256, rendertype$state));
    // }
    // return EventsHandlerClient.cache.get(icon);
    // }

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
            poke.genes.deserializeNBT((ListTag) genesTag);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        pokemob.onGenesChanged();
    }

    private static void setMostDamagingMove(final IPokemob outMob, final Entity target)
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

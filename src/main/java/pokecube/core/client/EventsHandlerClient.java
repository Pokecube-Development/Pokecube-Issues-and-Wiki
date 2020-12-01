package pokecube.core.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.InputEvent.RawMouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.logic.LogicMountedControl;
import pokecube.core.client.gui.AnimationGui;
import pokecube.core.client.gui.GuiArranger;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.client.gui.GuiTeleport;
import pokecube.core.client.render.mobs.RenderPokemob;
import pokecube.core.client.render.mobs.ShoulderLayer;
import pokecube.core.client.render.mobs.ShoulderLayer.ShoulderHolder;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.capabilities.DefaultPokemob;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.commandhandlers.StanceHandler;
import pokecube.core.items.pokecubes.EntityPokecubeBase;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.pokemobs.PacketChangeForme;
import pokecube.core.network.pokemobs.PacketCommand;
import pokecube.core.network.pokemobs.PacketMountedControl;
import pokecube.core.proxy.ClientProxy;
import pokecube.core.utils.PokemobTracker;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;
import thut.api.entity.genetics.GeneRegistry;

public class EventsHandlerClient
{
    public static HashMap<PokedexEntry, IPokemob>        renderMobs = new HashMap<>();
    private static Map<PokedexEntry, ResourceLocation[]> icons      = Maps.newHashMap();
    static boolean                                       notifier   = false;

    static long lastSetTime = 0;

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

    @SubscribeEvent
    public static void clientTick(final TickEvent.PlayerTickEvent event)
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
            if (mob != null && mob.getEntity().isAlive() && mob.getEntity().addedToChunk && event.player.getDistance(mob
                    .getEntity()) > PokecubeCore.getConfig().autoRecallDistance) mob.onRecall();
        }
        control:
        if (event.player.isPassenger() && Minecraft.getInstance().currentScreen == null)
        {
            final Entity e = event.player.getRidingEntity();
            pokemob = CapabilityPokemob.getPokemobFor(e);
            if (pokemob != null && e.getControllingPassenger() == event.player)
            {
                final LogicMountedControl controller = pokemob.getController();
                if (controller == null) break control;
                controller.backInputDown = ((ClientPlayerEntity) event.player).movementInput.backKeyDown;
                controller.forwardInputDown = ((ClientPlayerEntity) event.player).movementInput.forwardKeyDown;
                controller.leftInputDown = ((ClientPlayerEntity) event.player).movementInput.leftKeyDown;
                controller.rightInputDown = ((ClientPlayerEntity) event.player).movementInput.rightKeyDown;

                final boolean up = ClientProxy.mobUp.isKeyDown();
                final boolean down = ClientProxy.mobDown.isKeyDown();

                controller.upInputDown = up;
                controller.downInputDown = down;
                controller.followOwnerLook = PokecubeCore.getConfig().riddenMobsTurnWithLook;

                if (ClientProxy.throttleDown.isKeyDown())
                {
                    controller.throttle -= 0.05;
                    controller.throttle = Math.max(controller.throttle, 0.01);
                }
                else if (ClientProxy.throttleUp.isKeyDown())
                {
                    controller.throttle += 0.05;
                    controller.throttle = Math.min(controller.throttle, 1);
                }
                PacketMountedControl.sendControlPacket(e, controller);
            }
        }
        EventsHandlerClient.lastSetTime = System.currentTimeMillis() + 500;
    }

    @SubscribeEvent
    public static void FogRenderTick(final EntityViewRenderEvent.FogDensity evt)
    {
        IPokemob mount;

        if (evt.getInfo().getRenderViewEntity() instanceof PlayerEntity && evt.getInfo().getRenderViewEntity()
                .getRidingEntity() != null && (mount = CapabilityPokemob.getPokemobFor(evt.getInfo()
                        .getRenderViewEntity().getRidingEntity())) != null) if (evt.getInfo().getRenderViewEntity()
                                .isInWater() && mount.canUseDive())
        {
            evt.setDensity(0.05f);
            evt.setCanceled(true);
        }
    }

    public static IPokemob getPokemobForRender(final ItemStack itemStack, final World world)
    {
        if (!itemStack.hasTag()) return null;

        final int num = PokecubeManager.getPokedexNb(itemStack);
        if (num != 0)
        {
            final PokedexEntry entry = Database.getEntry(num);
            final IPokemob pokemob = EventsHandlerClient.getRenderMob(entry, world);
            if (pokemob == null) return null;
            final CompoundNBT pokeTag = itemStack.getTag();
            EventsHandlerClient.setFromNBT(pokemob, pokeTag);
            pokemob.setPokecube(itemStack);
            pokemob.setStatus(PokecubeManager.getStatus(itemStack));
            pokemob.getEntity().extinguish();
            return pokemob;
        }

        return null;
    }

    public static IPokemob getRenderMob(final PokedexEntry entry, final World world)
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void mouseInput(final RawMouseEvent evt)
    {
        final ClientPlayerEntity player = Minecraft.getInstance().player;
        // We only handle these ingame anyway.
        if (player == null) return;
        //
        if (evt.getAction() == GLFW.GLFW_PRESS && evt.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) if (Minecraft
                .getInstance().objectMouseOver == null || Minecraft.getInstance().objectMouseOver
                        .getType() == Type.MISS)
        {
            final Entity entity = Tools.getPointedEntity(player, 5);
            if (entity != null) hands:
            for (final Hand hand : Hand.values())
                if (Minecraft.getInstance().playerController.interactWithEntity(player, entity,
                        hand) == ActionResultType.SUCCESS)
                {
                    evt.setCanceled(true);
                    break hands;
                }
        }
    }

    @SubscribeEvent
    public static void keyInput(final KeyInputEvent evt)
    {
        final ClientPlayerEntity player = Minecraft.getInstance().player;
        // We only handle these ingame anyway.
        if (player == null) return;

        if (evt.getKey() == GLFW.GLFW_KEY_F5) if (AnimationGui.entry != null && Minecraft
                .getInstance().currentScreen instanceof AnimationGui)
        {
            PokedexEntryLoader.updateEntry(AnimationGui.entry);
            RenderPokemob.reloadModel(AnimationGui.entry);
        }
        else if (player.getRidingEntity() != null && Minecraft.getInstance().currentScreen != null)
        {
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(player.getRidingEntity());
            if (pokemob != null) PokedexEntryLoader.updateEntry(pokemob.getPokedexEntry());
        }
        if (ClientProxy.animateGui.isPressed() && Minecraft.getInstance().currentScreen == null) Minecraft.getInstance()
                .displayGuiScreen(new AnimationGui());
        if (ClientProxy.mobMegavolve.isPressed())
        {
            final IPokemob current = GuiDisplayPokecubeInfo.instance().getCurrentPokemob();
            if (current != null && !current.getGeneralState(GeneralStates.EVOLVING)) PacketChangeForme
                    .sendPacketToServer(current.getEntity(), null);
        }
        if (ClientProxy.arrangeGui.isPressed()) GuiArranger.toggle = !GuiArranger.toggle;
        if (ClientProxy.noEvolve.isPressed() && GuiDisplayPokecubeInfo.instance().getCurrentPokemob() != null)
            GuiDisplayPokecubeInfo.instance().getCurrentPokemob().cancelEvolve();
        if (ClientProxy.nextMob.isPressed()) GuiDisplayPokecubeInfo.instance().nextPokemob();
        if (ClientProxy.previousMob.isPressed()) GuiDisplayPokecubeInfo.instance().previousPokemob();
        if (ClientProxy.nextMove.isPressed())
        {
            int num = Screen.hasControlDown() ? 2 : 1;
            if (Screen.hasShiftDown()) num++;
            if (GuiTeleport.instance().getState()) GuiTeleport.instance().nextMove();
            else GuiDisplayPokecubeInfo.instance().nextMove(num);
        }
        if (ClientProxy.previousMove.isPressed())
        {
            int num = Screen.hasControlDown() ? 2 : 1;
            if (Screen.hasShiftDown()) num++;
            if (GuiTeleport.instance().getState()) GuiTeleport.instance().previousMove();
            else GuiDisplayPokecubeInfo.instance().previousMove(num);
        }
        if (ClientProxy.mobBack.isPressed()) if (GuiTeleport.instance().getState()) GuiTeleport.instance().setState(
                false);
        else GuiDisplayPokecubeInfo.instance().pokemobBack();
        if (ClientProxy.mobAttack.isPressed()) GuiDisplayPokecubeInfo.instance().pokemobAttack();
        if (ClientProxy.mobStance.isPressed()) GuiDisplayPokecubeInfo.instance().pokemobStance();

        if (ClientProxy.mobMove1.isPressed()) GuiDisplayPokecubeInfo.instance().setMove(0);
        if (ClientProxy.mobMove2.isPressed()) GuiDisplayPokecubeInfo.instance().setMove(1);
        if (ClientProxy.mobMove3.isPressed()) GuiDisplayPokecubeInfo.instance().setMove(2);
        if (ClientProxy.mobMove4.isPressed()) GuiDisplayPokecubeInfo.instance().setMove(3);

        if (ClientProxy.gzmove.isPressed())
        {
            final IPokemob current = GuiDisplayPokecubeInfo.instance().getCurrentPokemob();
            if (current != null) PacketCommand.sendCommand(current, Command.STANCE, new StanceHandler(!current
                    .getCombatState(CombatStates.USINGGZMOVE), StanceHandler.GZMOVE).setFromOwner(true));
        }
    }

    private static final Set<PlayerRenderer> addedLayers = Sets.newHashSet();

    @SubscribeEvent
    public static void onPlayerRender(final RenderPlayerEvent.Post event)
    {
        if (EventsHandlerClient.addedLayers.contains(event.getRenderer())) return;
        event.getRenderer().addLayer(new ShoulderLayer<>(event.getRenderer()));
        EventsHandlerClient.addedLayers.add(event.getRenderer());
    }

    @SubscribeEvent
    public static void capabilityEntities(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof PlayerEntity) event.addCapability(new ResourceLocation(
                "pokecube:shouldermobs"), new ShoulderHolder((PlayerEntity) event.getObject()));
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderGUIScreenPre(final GuiScreenEvent.DrawScreenEvent.Post event)
    {
        try
        {
            if (!Screen.hasAltDown()) return;
            if (!(event.getGui() instanceof ContainerScreen)) return;
            final ContainerScreen<?> gui = (ContainerScreen<?>) event.getGui();
            final List<Slot> slots = gui.getContainer().inventorySlots;
            for (final Slot slot : slots)
                if (slot.getHasStack() && PokecubeManager.isFilled(slot.getStack()))
                {
                    final IPokemob pokemob = EventsHandlerClient.getPokemobForRender(slot.getStack(), gui
                            .getMinecraft().world);
                    if (pokemob == null) continue;

                    int i, j;
                    i = slot.xPos;
                    j = slot.yPos;
                    final int x = i + gui.getGuiLeft();
                    final int y = j + gui.getGuiTop();
                    EventsHandlerClient.renderIcon(pokemob, x, y, 16, 16);
                }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void onRenderHotbar(final RenderGameOverlayEvent.Post event)
    {
        if (event.getType() == ElementType.HOTBAR)
        {
            if (!Screen.hasAltDown()) return;
            final PlayerEntity player = Minecraft.getInstance().player;
            final int w = Minecraft.getInstance().getMainWindow().getScaledWidth();
            final int h = Minecraft.getInstance().getMainWindow().getScaledHeight();
            int i, j;
            i = -80;
            j = -9;
            for (int l = 0; l < 9; l++)
            {
                final ItemStack stack = player.inventory.mainInventory.get(l);
                if (stack != null && PokecubeManager.isFilled(stack))
                {
                    final IPokemob pokemob = EventsHandlerClient.getPokemobForRender(stack, player.getEntityWorld());
                    if (pokemob == null) continue;
                    int x = w / 2;
                    x = i + x + 20 * l - 8;
                    int y = h;
                    y = j + y - 9;
                    EventsHandlerClient.renderIcon(pokemob, x, y, 16, 16);
                }
            }
        }
    }

    public static void renderIcon(final IPokemob realMob, final int left, final int top, final int width,
            final int height)
    {
        final PokedexEntry entry = realMob.getPokedexEntry();
        EventsHandlerClient.renderIcon(entry, left, top, width, height, realMob.isShiny());
    }

    public static void renderIcon(final PokedexEntry entry, int left, int top, final int width, final int height,
            final boolean shiny)
    {
        ResourceLocation[] texs = EventsHandlerClient.icons.get(entry);
        ResourceLocation tex = null;
        if (texs != null) tex = texs[shiny ? 1 : 0];
        if (tex == null)
        {
            texs = new ResourceLocation[2];
            EventsHandlerClient.icons.put(entry, texs);
            final String texture = entry.getModId() + ":" + entry.getTexture((byte) 0).replace("/entity/",
                    "/entity_icon/");
            final String textureS = entry.hasShiny ? texture.replace(".png", "s.png") : texture;
            tex = new ResourceLocation(texture);
            texs[0] = tex;
            try
            {
                Minecraft.getInstance().getResourceManager().getResource(tex).getInputStream().close();
                try
                {
                    final ResourceLocation tex2 = new ResourceLocation(textureS);
                    Minecraft.getInstance().getResourceManager().getResource(tex2).getInputStream().close();
                    texs[1] = tex2;
                }
                catch (final IOException e)
                {
                    texs[1] = tex;
                }
            }
            catch (final IOException e)
            {
                PokecubeCore.LOGGER.error("no Icon for " + entry, e);
            }
        }

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
        Minecraft.getInstance().getTextureManager().bindTexture(tex);
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        final int zLevel = 300;
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(left, bottom, zLevel).tex(0, 0).endVertex();
        bufferbuilder.pos(right, bottom, zLevel).tex(1, 0).endVertex();
        bufferbuilder.pos(right, top, zLevel).tex(1, 1).endVertex();
        bufferbuilder.pos(left, top, zLevel).tex(0, 1).endVertex();
        tessellator.draw();
        RenderSystem.disableBlend();
    }

    public static void setFromNBT(final IPokemob pokemob, final CompoundNBT tag)
    {
        final CompoundNBT pokemobTag = TagNames.getPokecubePokemobTag(tag);
        final INBT genesTag = TagNames.getPokecubeGenesTag(tag);
        pokemobTag.remove(TagNames.INVENTORYTAG);
        pokemobTag.remove(TagNames.AITAG);
        pokemobTag.remove(TagNames.MOVESTAG);
        pokemob.setHealth(tag.getFloat("CHP"));
        pokemob.read(pokemobTag);
        if (pokemob instanceof DefaultPokemob) try
        {
            final DefaultPokemob poke = (DefaultPokemob) pokemob;
            GeneRegistry.GENETICS_CAP.readNBT(poke.genes, null, genesTag);
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

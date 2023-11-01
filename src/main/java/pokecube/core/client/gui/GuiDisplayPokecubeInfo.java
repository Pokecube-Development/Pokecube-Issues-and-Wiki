/**
 *
 */
package pokecube.core.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.moves.MoveApplicationRegistry;
import pokecube.api.entity.TeamManager;
import pokecube.api.entity.pokemob.IHasCommands.Command;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.api.entity.pokemob.commandhandlers.AttackEntityHandler;
import pokecube.api.entity.pokemob.commandhandlers.AttackLocationHandler;
import pokecube.api.entity.pokemob.commandhandlers.AttackNothingHandler;
import pokecube.api.entity.pokemob.commandhandlers.MoveIndexHandler;
import pokecube.api.entity.pokemob.commandhandlers.MoveToHandler;
import pokecube.api.entity.pokemob.commandhandlers.StanceHandler;
import pokecube.api.entity.pokemob.commandhandlers.TeleportHandler;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveNames;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.client.GuiEvent;
import pokecube.core.client.GuiEvent.RenderMoveMessages;
import pokecube.core.client.GuiEvent.RenderSelectedInfo;
import pokecube.core.client.GuiEvent.RenderTargetInfo;
import pokecube.core.client.GuiEvent.RenderTeleports;
import pokecube.core.client.gui.components.GuiEventComponent;
import pokecube.core.client.gui.components.MessageInfo;
import pokecube.core.client.gui.components.OutMobInfo;
import pokecube.core.client.gui.components.TargetInfo;
import pokecube.core.client.gui.components.TeleportInfo;
import pokecube.core.network.pokemobs.PacketAIRoutine;
import pokecube.core.network.pokemobs.PacketCommand;
import pokecube.core.utils.AITools;
import pokecube.core.utils.EntityTools;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public class GuiDisplayPokecubeInfo extends GuiComponent implements IIngameOverlay
{
    public static int lightGrey = 0xDDDDDD;
    public static GuiDisplayPokecubeInfo instance;

    public static OutMobInfo outMobRenderer = new OutMobInfo();
    public static TargetInfo targetRenderer = new TargetInfo();
    public static MessageInfo messageRenderer = new MessageInfo();
    public static TeleportInfo teleRenderer = new TeleportInfo();

    public static Map<Class<? extends GuiEvent>, List<GuiEventComponent>> GUI_HANDLERS = new HashMap<>();
    public static List<GuiEventComponent> COMPONENTS = new ArrayList<>();

    public static void register(Class<? extends GuiEvent> classs, GuiEventComponent handler)
    {
        List<GuiEventComponent> list = GUI_HANDLERS.getOrDefault(classs, new ArrayList<>());
        GUI_HANDLERS.put(classs, list);
        list.add(handler);
        COMPONENTS.add(handler);
    }

    static
    {
        register(RenderSelectedInfo.class, outMobRenderer);
        register(RenderTargetInfo.class, targetRenderer);
        register(RenderMoveMessages.class, messageRenderer);
        register(RenderTeleports.class, teleRenderer);
    }

    public static GuiDisplayPokecubeInfo instance()
    {
        if (GuiDisplayPokecubeInfo.instance == null) GuiDisplayPokecubeInfo.instance = new GuiDisplayPokecubeInfo();
        return GuiDisplayPokecubeInfo.instance;
    }

    public static void sendMoveIndexPacket(final IPokemob pokemob, final int moveIndex)
    {
        PacketCommand.sendCommand(pokemob, Command.CHANGEMOVEINDEX,
                new MoveIndexHandler((byte) moveIndex).setFromOwner(true));
    }

    private static final IPokemob[] EMPTY = new IPokemob[0];

    protected Font fontRenderer;

    protected Minecraft minecraft;

    IPokemob[] pokemobsCache = new IPokemob[0];

    int refreshCounter = 0;

    public int indexPokemob = 0;

    IIngameOverlay infoOverlay = this;

    /**
     *
     */
    public GuiDisplayPokecubeInfo()
    {
        this.minecraft = Minecraft.getInstance();
        this.fontRenderer = this.minecraft.font;
        if (GuiDisplayPokecubeInfo.instance != null)
            ThutCore.FORGE_BUS.unregister(GuiDisplayPokecubeInfo.instance);
        GuiDisplayPokecubeInfo.instance = this;
        OverlayRegistry.registerOverlayTop("Pokecube Info", this.infoOverlay);
        ThutCore.FORGE_BUS.register(this);
    }

    public void disable()
    {
        OverlayRegistry.enableOverlay(this.infoOverlay, false);
    }

    public void enable()
    {
        OverlayRegistry.enableOverlay(this.infoOverlay, true);
    }

    @Override
    public void render(final ForgeIngameGui gui, final PoseStack mStack, final float partialTicks, final int width,
            final int height)
    {
        ThutCore.FORGE_BUS.post(new GuiEvent.RenderMoveMessages(mStack, gui));
        if (this.indexPokemob > this.getPokemobsToDisplay().length)
        {
            this.refreshCounter = 0;
            this.indexPokemob = 0;
            this.pokemobsCache = this.getPokemobsToDisplay();
        }
        if (this.indexPokemob >= this.getPokemobsToDisplay().length) this.indexPokemob = 0;
        if (this.fontRenderer == null) this.fontRenderer = this.minecraft.font;

        if (Minecraft.getInstance().screen instanceof ChatScreen)
        {
            GuiDisplayPokecubeInfo.outMobRenderer.isMouseOver();
            GuiDisplayPokecubeInfo.targetRenderer.isMouseOver();
        }

        ThutCore.FORGE_BUS.post(new GuiEvent.RenderSelectedInfo(mStack, gui));
        ThutCore.FORGE_BUS.post(new GuiEvent.RenderTargetInfo(mStack, gui));
        ThutCore.FORGE_BUS.post(new GuiEvent.RenderTeleports(mStack, gui));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void drawSelected(final GuiEvent evt)
    {
        var list = GUI_HANDLERS.get(evt.getClass());
        if (list != null) list.forEach(c -> c.drawGui(evt));
    }

    /** @return the currently selected pokemob */
    public IPokemob getCurrentPokemob()
    {
        IPokemob pokemob = null;
        if (this.indexPokemob < this.pokemobsCache.length && this.indexPokemob >= 0 && this.pokemobsCache.length > 0)
            pokemob = this.pokemobsCache[this.indexPokemob];
        return pokemob;
    }

    public IPokemob[] getPokemobsToDisplay()
    {
        if (this.refreshCounter++ > 5) this.refreshCounter = 0;
        if (this.refreshCounter > 0) return this.pokemobsCache;

        final Player player = this.minecraft.player;

        if (player == null || player.getLevel() == null) return GuiDisplayPokecubeInfo.EMPTY;

        final List<IPokemob> pokemobs = EventsHandlerClient.getPokemobs(player, 96);
        final List<IPokemob> ret = new ArrayList<>();
        for (final IPokemob pokemob : pokemobs)
        {
            boolean owner = pokemob.getOwnerId() != null;
            if (owner) owner = player.getUUID().equals(pokemob.getOwnerId());
            if (owner && !pokemob.getLogicState(LogicStates.SITTING) && !pokemob.getGeneralState(GeneralStates.STAYING))
                ret.add(pokemob);
        }
        if (this.pokemobsCache.length != ret.size()) this.pokemobsCache = ret.toArray(new IPokemob[ret.size()]);
        else this.pokemobsCache = ret.toArray(this.pokemobsCache);
        Arrays.sort(this.pokemobsCache, (o1, o2) -> {
            final Entity e1 = o1.getEntity();
            final Entity e2 = o2.getEntity();

            if (e1.tickCount == e2.tickCount)
            {
                if (o2.getLevel() == o1.getLevel())
                    return o1.getDisplayName().getString().compareTo(o2.getDisplayName().getString());
                return o2.getLevel() - o1.getLevel();
            }
            return e1.tickCount - e2.tickCount;
        });
        return this.pokemobsCache;
    }

    /**
     * Incremenrs pokemob move index
     *
     * @param i
     */
    public void nextMove(final int i)
    {
        final IPokemob pokemob = this.getCurrentPokemob();
        if (pokemob != null)
        {
            int index = pokemob.getMoveIndex() + i;
            int max = 0;
            for (max = 0; max < 4; max++) if (pokemob.getMove(max) == null) break;
            if (index >= 5) index = 0;
            if (index >= max) index = 5;
            this.setMove(index);
        }
    }

    /** Select next pokemob */
    public void nextPokemob()
    {
        this.indexPokemob++;
        if (this.indexPokemob >= this.pokemobsCache.length) this.indexPokemob = 0;
    }

    public Predicate<Entity> getAttackSelector()
    {
        final IPokemob pokemob = this.getCurrentPokemob();
        if (pokemob == null) return AITools.validCombatTargets;
        final Player player = this.minecraft.player;
        return input -> {

            input = EntityTools.getCoreEntity(input);
            // First check basic rules for targets.
            if (!(input instanceof LivingEntity living)) return false;
            if (!(AITools.validCombatTargets.test(living))) return false;
            // Next see if this should count as a battle target.
            boolean sameTeam = TeamManager.sameTeam(input, player);

            // Cache the old target
            var oldMob = sameTeam ? pokemob.getMoveStats().targetAlly : pokemob.getMoveStats().targetEnemy;

            // Temporarily set the target to the input
            if (sameTeam) pokemob.getMoveStats().targetAlly = living;
            else pokemob.getMoveStats().targetEnemy = living;

            MoveEntry move = pokemob.getSelectedMove();
            // Check if is valid
            boolean valid = MoveApplicationRegistry.isValidTarget(pokemob, living, move);

            // Reset target to cached value
            if (sameTeam) pokemob.getMoveStats().targetAlly = oldMob;
            else pokemob.getMoveStats().targetEnemy = oldMob;

            // Return valid.
            return valid;
        };
    }

    /** Identifies target of attack, and sends the packet with info to server */
    public void pokemobAttack()
    {
        final IPokemob pokemob = this.getCurrentPokemob();
        if (pokemob == null) return;
        final Player player = this.minecraft.player;
        var selector = getAttackSelector();
        Entity target = Tools.getPointedEntity(player, 32, selector, 1);

        if (PokecubeCore.getConfig().debug_commands) PokecubeAPI.logInfo("Targets: {}", target);

        target = EntityTools.getCoreEntity(target);
        if (target == null && Minecraft.getInstance().crosshairPickEntity != null
                && selector.test(Minecraft.getInstance().crosshairPickEntity))
            target = Minecraft.getInstance().crosshairPickEntity;
        final Vector3 targetLocation = Tools.getPointedLocation(player, 32);
        if (pokemob != null)
        {
            if (pokemob.getMove(pokemob.getMoveIndex()) == null) return;
            if (pokemob.getMove(pokemob.getMoveIndex()).equalsIgnoreCase(IMoveNames.MOVE_TELEPORT))
            {
                if (!GuiTeleport.instance().getState())
                {
                    GuiTeleport.instance().setState(true);
                    return;
                }
                GuiTeleport.instance().setState(false);
                PacketCommand.sendCommand(pokemob, Command.TELEPORT, new TeleportHandler().setFromOwner(true));
                return;
            }
        }
        if (target != null && (target instanceof LivingEntity || target instanceof PartEntity<?>)) PacketCommand
                .sendCommand(pokemob, Command.ATTACKENTITY, new AttackEntityHandler(target.getId()).setFromOwner(true));
        else if (targetLocation != null) PacketCommand.sendCommand(pokemob, Command.ATTACKLOCATION,
                new AttackLocationHandler(targetLocation).setFromOwner(true));
        else PacketCommand.sendCommand(pokemob, Command.ATTACKNOTHING, new AttackNothingHandler().setFromOwner(true));
    }

    /**
     * Recalls selected pokemob, if none selected, will try to identify a
     * pokemob being looked at, and recalls that
     */
    public void pokemobBack()
    {
        final IPokemob pokemob = this.getCurrentPokemob();

        if (Screen.hasShiftDown() && pokemob != null && pokemob.getOwner() != null)
        {
            PacketCommand.sendCommand(pokemob, Command.MOVETO,
                    new MoveToHandler(new Vector3().set(pokemob.getOwner()), 1.0f));
            return;
        }
        if (pokemob != null) pokemob.onRecall();
        else
        {
            final Player player = this.minecraft.player;
            final Entity target = Tools.getPointedEntity(player, 32);
            final IPokemob targetMob = PokemobCaps.getPokemobFor(target);
            if (targetMob != null && player.getUUID().equals(targetMob.getOwnerId())) targetMob.onRecall();
        }

        if (this.indexPokemob >= this.pokemobsCache.length) this.indexPokemob--;
        if (this.indexPokemob < 0) this.indexPokemob = 0;
    }

    /**
     * Sends the packet to toggle all pokemobs set to follow between sit and
     * stand
     */
    public void pokemobStance()
    {
        IPokemob pokemob;
        if ((pokemob = this.getCurrentPokemob()) != null)
        {
            final boolean isRiding = pokemob.getEntity().hasIndirectPassenger(pokemob.getOwner());
            if (!isRiding) PacketCommand.sendCommand(pokemob, Command.STANCE,
                    new StanceHandler(!pokemob.getLogicState(LogicStates.SITTING), StanceHandler.SIT)
                            .setFromOwner(true));
            else
            {
                final AIRoutine routine = AIRoutine.AIRBORNE;
                final boolean state = !pokemob.isRoutineEnabled(routine);
                pokemob.setRoutineState(routine, state);
                PacketAIRoutine.sentCommand(pokemob, routine, state);
            }
        }
        else
        {
            final Player player = this.minecraft.player;
            final Entity target = Tools.getPointedEntity(player, 32);
            final IPokemob targetMob = PokemobCaps.getPokemobFor(target);
            if (targetMob != null && targetMob.getOwner() == player) PacketCommand.sendCommand(targetMob,
                    Command.STANCE, new StanceHandler(!targetMob.getLogicState(LogicStates.SITTING), StanceHandler.SIT)
                            .setFromOwner(true));
        }
    }

    /**
     * Decrements pokemob move index
     *
     * @param j
     */
    public void previousMove(final int j)
    {
        final IPokemob pokemob = this.getCurrentPokemob();
        if (pokemob != null)
        {
            int index = pokemob.getMoveIndex();
            if (index == 5)
            {
                for (int i = 3; i > 0; i -= j) if (pokemob.getMove(i) != null)
                {
                    index = i;
                    break;
                }
            }
            else index -= j;

            if (index % 5 >= 0) index = index % 5;
            else index = 5;
            this.setMove(index);
        }
    }

    /** Select previous pokemob */
    public void previousPokemob()
    {
        this.indexPokemob--;
        if (this.indexPokemob < 0) this.indexPokemob = this.pokemobsCache.length - 1;
    }

    public void saveConfig()
    {
        PokecubeCore.getConfig().onUpdated();
        PokecubeCore.getConfig().write();
    }

    /**
     * Sets pokemob's move index.
     *
     * @param num
     */
    public void setMove(final int num)
    {
        final IPokemob pokemob = this.getCurrentPokemob();
        if (pokemob != null)
        {
            int index = num;
            if (index > 5) index = index % 6;
            GuiDisplayPokecubeInfo.sendMoveIndexPacket(pokemob, index);
        }
    }
}

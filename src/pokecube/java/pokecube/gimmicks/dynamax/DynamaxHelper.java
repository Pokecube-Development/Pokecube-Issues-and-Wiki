package pokecube.gimmicks.dynamax;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.SharedAttributes;
import pokecube.api.entity.pokemob.ICanEvolve;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.commandhandlers.ChangeFormHandler;
import pokecube.api.entity.pokemob.commandhandlers.ChangeFormHandler.IChangeHandler;
import pokecube.api.events.pokemobs.ChangeForm;
import pokecube.api.raids.RaidManager;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.init.CoreCreativeTabs;
import pokecube.gimmicks.dynamax.blocks.MaxBlock;
import pokecube.gimmicks.dynamax.blocks.MaxTile;
import pokecube.core.database.Database;
import pokecube.core.eventhandlers.PokemobEventsHandler.MegaEvoTicker;
import pokecube.core.eventhandlers.SpawnHandler;
import pokecube.core.eventhandlers.SpawnHandler.ForbiddenEntry;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import thut.api.Tracker;
import thut.api.entity.genetics.GeneRegistry;
import thut.lib.TComponent;

import java.util.List;
import java.util.function.Supplier;

/**
 * This class handles the dynamax mechanic
 */
@Mod(value = PokecubeCore.MODID)
@EventBusSubscriber(modid = PokecubeCore.MODID)
public class DynamaxHelper
{
    public static final DeferredBlock<Block> DYNAMAX;
    public static final Supplier<BlockEntityType<?>> MAX_TYPE;

    static
    {
        DYNAMAX = PokecubeCore.BLOCKS.register("dynamax",
                () -> new MaxBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA).strength(0.8F)
                        .requiresCorrectToolForDrops().noOcclusion().forceSolidOn().lightLevel(i -> 5)
                        .sound(SoundType.AMETHYST_CLUSTER)));
        MAX_TYPE = PokecubeCore.TILES.register("dynamax",
                () -> BlockEntityType.Builder.of(MaxTile::new, DYNAMAX.get()).build(null));
    }

    public DynamaxHelper()
    {
    }

    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTab().equals(CoreCreativeTabs.BLOCKS_ITEMS_TAB.get()))
        {
            CoreCreativeTabs.addAfter(event, PokecubeItems.TM, DYNAMAX);
        }
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            CoreCreativeTabs.addAfter(event, PokecubeItems.TM, DYNAMAX);
        }
    }

    /**
     * Setup and register stuff.
     */
    @SubscribeEvent
    public static void init(FMLLoadCompleteEvent event)
    {
        // Register the genes
        GeneRegistry.register(DynamaxGene.class);
        // Handles reverting from dynamax
        PokecubeAPI.POKEMOB_BUS.addListener(DynamaxHelper::onFormRevert);
        // Handles reverting from dynamax
        PokecubeAPI.POKEMOB_BUS.addListener(DynamaxHelper::postFormChange);
        // Register handler for players dynamaxing their mobs
        ChangeFormHandler.addChangeHandler(new DynaMaxer());
        // Register dynamax raid
        RaidManager.registerBossType(new DynamaxRaid());
    }

    /**
     * Handles dynamaxing on command. Checks for valid max spots, etc.
     */
    private static class DynaMaxer implements IChangeHandler
    {
        @Override
        public boolean handleChange(IPokemob pokemob)
        {
            final LivingEntity owner = pokemob.getOwner();

            final Entity mob = pokemob.getEntity();
            Player player = owner instanceof Player p ? p : null;
            final Level world = mob.level();
            final BlockPos pos = mob.blockPosition();
            final PokedexEntry entry = pokemob.getPokedexEntry();
            final Component oldName = pokemob.getDisplayName();

            // Check dynamax/gigantamax first.
            List<ForbiddenEntry> reasons = SpawnHandler.getForbiddenEntries(world, pos);
            boolean isMaxSpot = false;
            for (ForbiddenEntry e : reasons)
            {
                if (e.reason == MaxTile.MAXSPOT)
                {
                    isMaxSpot = true;
                    break;
                }
            }
            var reg = mob.registryAccess();

            boolean isDyna = DynamaxHelper.isDynamax(pokemob);
            if (isMaxSpot)
            {
                PokedexEntry newEntry = entry;
                if (isDyna)
                {
                    Component mess = TComponent.translatable("pokemob.dynamax.command.revert", oldName);
                    pokemob.displayMessageToOwner(mess);
                    newEntry = pokemob.getBasePokedexEntry();
                    mess = TComponent.translatable("pokemob.dynamax.revert", oldName);
                    MegaEvoTicker.scheduleRevert(newEntry, pokemob, mess);
                }
                else
                {
                    final long dynatime = PokecubePlayerDataHandler.getCustomDataTag(reg, owner.getUUID())
                            .getLong("pokecube:dynatime");
                    final long time = Tracker.instance().getTick();
                    if (dynatime != 0 && time - dynatime < PokecubeCore.getConfig().dynamax_cooldown)
                    {
                        thut.lib.ChatHelper.sendSystemMessage(player,
                                TComponent.translatable("pokemob.dynamax.too_soon", pokemob.getDisplayName()));
                        return true;
                    }
                    var info = DynamaxGene.getDyna(mob);
                    if (info.gigantamax)
                    {
                        var gEntry = Database.getEntry(entry.getName() + "-gmax");
                        if (gEntry != null) newEntry = gEntry;
                    }

                    Component mess = TComponent.translatable("pokemob.dynamax.command.evolve", oldName);
                    pokemob.displayMessageToOwner(mess);
                    mess = TComponent.translatable("pokemob.dynamax.success", oldName);
                    PokecubePlayerDataHandler.getCustomDataTag(reg, owner.getUUID()).putLong("pokecube:dynatime", time);
                    doDynamax(pokemob, newEntry, PokecubeCore.getConfig().dynamax_duration, mess);
                }
                return true;
            }
            if (isDyna)
            {
                Component mess = TComponent.translatable("pokemob.dynamax.command.revert", oldName);
                pokemob.displayMessageToOwner(mess);
                mess = TComponent.translatable("pokemob.dynamax.revert", oldName);
                MegaEvoTicker.scheduleRevert(entry, pokemob, mess);
                return true;
            }
            return false;
        }

        @Override
        public String changeKey()
        {
            return "dynamax";
        }

        @Override
        public int getPriority()
        {
            return 10;
        }

        @Override
        public void onFail(IPokemob pokemob)
        {
            final LivingEntity owner = pokemob.getOwner();
            if (owner instanceof ServerPlayer player) thut.lib.ChatHelper.sendSystemMessage(player,
                    TComponent.translatable("pokemob.dynamax.failed", pokemob.getDisplayName()));
        }
    }

    private static void onFormRevert(ChangeForm.Revert event)
    {
        var entity = event.getPokemob().getEntity();
        entity.getPersistentData().putBoolean("pokecube:dyna_reverted", true);

        var entry = event.getPokemob().getPokedexEntry();
        // TODO better way to decide this
        if ((entry.getName().endsWith("-gmax") || entry.getName().endsWith("-eternamax"))
                && entry.getBaseForme() != null)
        {
            event.getPokemob().setBasePokedexEntry(entry.getBaseForme());
        }
    }

    private static void postFormChange(ChangeForm.Post event)
    {
        removeDynamax(event.getPokemob());
    }

    protected static void removeDynamax(IPokemob pokemob)
    {
        var entity = pokemob.getEntity();
        if (entity.getPersistentData().contains("pokecube:dyna_reverted"))
        {
            entity.getPersistentData().remove("pokecube:dyna_reverted");
            entity.getPersistentData().remove("pokecube:dynadur");
            var hpAttr = entity.getAttribute(Attributes.MAX_HEALTH);
            hpAttr.removeModifier(DYNAMOD);
            // Reset health to clip to new maximum.
            entity.setHealth(Math.min(entity.getHealth(), entity.getMaxHealth()));
            SharedAttributes.clearScale(pokemob.getEntity(), DYNAMOD);
        }
    }

    private static final ResourceLocation DYNAMOD = ResourceLocation.parse("pokecube:dynamax");

    public static void doDynamax(IPokemob pokemob, PokedexEntry newEntry, int duration, Component mess)
    {
        MegaEvoTicker.scheduleChange(PokecubeCore.getConfig().evolutionTicks, newEntry, pokemob, mess, () -> {
            // Flag as evolving for animation effects
            pokemob.setGeneralState(GeneralStates.EVOLVING, true);
            pokemob.setGeneralState(GeneralStates.EXITINGCUBE, false);
            pokemob.setEvolutionTicks(PokecubeCore.getConfig().evolutionTicks + 50);
            pokemob.setEvolutionStack(PokecubeItems.getStack(ICanEvolve.EVERSTONE));
            PokecubeAPI.POKEMOB_BUS.post(new ChangeForm.Pre(pokemob));
        }, () -> {
            DynamaxHelper.onDynamax(pokemob, duration);
            PokecubeAPI.POKEMOB_BUS.post(new ChangeForm.Post(pokemob));
            pokemob.setGeneralState(GeneralStates.EVOLVING, false);
            pokemob.setEvolutionStack(ItemStack.EMPTY);
        });
    }

    /**
     * Applies the modifiers for when the mob has dynamaxed, This includes the health boost and the size boost. Then
     * also marks the duration and start time for the dynamaxing.
     */
    private static void onDynamax(IPokemob pokemob, int duration)
    {
        var entity = pokemob.getEntity();
        long time = Tracker.instance().getTick();
        entity.getPersistentData().putLong("pokecube:dynatime", time);
        entity.getPersistentData().putInt("pokecube:dynadur", duration);

        var info = DynamaxGene.getDyna(entity);
        float scale = 1.5f + 0.05f * info.dynaLevel;
        var hpBoost = new AttributeModifier(DYNAMOD, scale, Operation.ADD_MULTIPLIED_TOTAL);
        var hpAttr = entity.getAttribute(Attributes.MAX_HEALTH);
        hpAttr.removeModifier(DYNAMOD);
        float health = entity.getMaxHealth();
        hpAttr.addPermanentModifier(hpBoost);
        float toAdd = entity.getMaxHealth() - health;
        entity.heal(toAdd);

        if (!info.gigantamax)
            SharedAttributes.adjustScale(entity, PokecubeCore.getConfig().dynamax_scale, DYNAMOD, true);
    }

    public static boolean isDynamax(IPokemob pokemob)
    {
        var entity = pokemob.getEntity();
        var hpAttr = entity.getAttribute(Attributes.MAX_HEALTH);
        return hpAttr.getModifier(DYNAMOD) != null;
    }
}

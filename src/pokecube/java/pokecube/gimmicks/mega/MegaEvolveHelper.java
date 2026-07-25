package pokecube.gimmicks.mega;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.commandhandlers.ChangeFormHandler;
import pokecube.api.entity.pokemob.commandhandlers.ChangeFormHandler.IChangeHandler;
import pokecube.api.events.pokemobs.ChangeForm;
import pokecube.core.PokecubeCore;
import pokecube.core.eventhandlers.PokemobEventsHandler.MegaEvoTicker;
import pokecube.core.inventory.pc.PCContainer;
import pokecube.core.items.megastuff.ItemMegawearable;
import pokecube.gimmicks.mega.MegaCapability.MegaStone;
import pokecube.gimmicks.mega.MegaCapability.MegaWearable;
import thut.api.Tracker;
import thut.api.item.ItemList;
import thut.lib.TComponent;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.PlayerWearables;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * This class handles the mega evolution mechanic. Primarily via the following:<br>
 * <br>
 * - Registers a handler for commands to mega-evolve<br> - Ensures that pokemobs are able to mega-evolve<br> - Ensures
 * that they un-mega-evolve when recalled<br>
 */
@EventBusSubscriber(bus = Bus.MOD, modid = PokecubeCore.MODID)
public class MegaEvolveHelper
{

    public static final DeferredRegister<DataComponentType<?>> ITEM_DATA_REG;
    public static final Supplier<DataComponentType<MegaWearable>> MEGA_WEARABLE;
    public static final Supplier<DataComponentType<MegaStone>> MEGA_STONE;

    public static final ResourceLocation BLANK_MEGASTONE = ResourceLocation.fromNamespaceAndPath("pokecube", "blank_megastone");

    static
    {
        ITEM_DATA_REG = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, PokecubeCore.MODID);
        MEGA_WEARABLE = PokecubeCore.ITEM_DATA.register("mega_item",
                name -> new DataComponentType.Builder<MegaWearable>().persistent(MegaWearable.CODEC)
                        .networkSynchronized(MegaWearable.STREAM_CODEC).build());
        MEGA_STONE = PokecubeCore.ITEM_DATA.register("mega_stone",
                name -> new DataComponentType.Builder<MegaStone>().persistent(MegaStone.CODEC)
                        .networkSynchronized(MegaStone.STREAM_CODEC).build());

        MegaCapability.RegisterMegaType(ResourceLocation.parse("pokecube:default"), MegaCapability::new);
    }

    @SubscribeEvent
    public static void initMegaItems(ModifyDefaultComponentsEvent event)
    {
        var KEY = ResourceLocation.parse("pokecube:mega_wearables");
        MegaCapability.RegisterMegaType(KEY, MegaCapability::new);
        event.modifyMatching(item -> item instanceof ItemMegawearable,
                builder -> builder.set(MEGA_WEARABLE.get(), new MegaWearable(KEY)));
    }

    @SubscribeEvent
    public static void init(FMLLoadCompleteEvent event)
    {
        // Handle clearing mega evolution when recalling to pokecube
        PokecubeAPI.POKEMOB_BUS.addListener(MegaEvolveHelper::onFormRevert);
        // Actually apply said changes
        PokecubeAPI.POKEMOB_BUS.addListener(MegaEvolveHelper::postFormChange);
        // Register the ability to mega evolve from the owner command
        ChangeFormHandler.addChangeHandler(new MegaEvolver());
        // Init mega evo data, this will then load in mega evos when the
        // datapacks load during world load.
        MegaEvoData.init();
        MegaStoneColours.init();

        PCContainer.CUSTOMPCWHILTELIST.add(stack -> stack.has(MEGA_WEARABLE)
                && stack.get(MEGA_WEARABLE).withItem(stack).details().getEntry(stack) != null);

        ChangeFormHandler.checker = (player, toEvolve) -> {
            PokedexEntry entry = toEvolve.getPokedexEntry();
            final PlayerWearables worn = ThutWearables.getWearables(player);
            for (final ItemStack stack : worn.getWearables()) if (MegaCapability.matches(stack, entry)) return true;
            return false;
        };
    }

    /**
     * Class for implementing the mega evolution via owner command
     */
    private static class MegaEvolver implements IChangeHandler
    {
        @Override
        public boolean handleChange(IPokemob pokemob)
        {
            final Component oldName = pokemob.getDisplayName();
            boolean isMega = MegaEvolveHelper.isMega(pokemob);
            final LivingEntity owner = pokemob.getOwner();
            Player player = owner instanceof Player p ? p : null;
            PokedexEntry newEntry;
            newEntry = MegaEvoData.getMegaEvo(pokemob);

            if (isMega)
            {
                Component mess = TComponent.translatable("pokemob.megaevolve.command.revert", oldName);
                pokemob.displayMessageToOwner(mess);
                newEntry = pokemob.getBasePokedexEntry();
                mess = TComponent.translatable("pokemob.megaevolve.revert", oldName,
                        TComponent.translatable(newEntry.getUnlocalizedName()));
                MegaEvoTicker.scheduleRevert(newEntry, pokemob, mess);
            }
            else if (newEntry != null)
            {
                Component mess = TComponent.translatable("pokemob.megaevolve.command.evolve", oldName);
                pokemob.displayMessageToOwner(mess);
                mess = TComponent.translatable("pokemob.megaevolve.success", oldName,
                        TComponent.translatable(newEntry.getUnlocalizedName()));
                MegaEvolveHelper.megaEvolve(pokemob, newEntry, mess);
            }
            else{
                // First, try transforming the item if it is a blank mega stone, if so, recall this function again.
                var stack = pokemob.getHeldItem();
                if (ItemList.is(BLANK_MEGASTONE, stack) && !stack.has(MEGA_STONE) && stack.getCount() == 1)
                {
                    List<MegaEvoData.MegaRule> rules = MegaEvoData.RULES.getOrDefault(pokemob.getPokedexEntry(), Collections.emptyList());
                    if(!rules.isEmpty()&&pokemob.getHappiness()>250)
                    {
                        Collections.shuffle(rules);
                        newEntry = rules.getFirst().getResult();
                        var stone = new MegaStone();
                        stone.entry = newEntry.getTrimmedName();
                        stone.colours = MegaCapability.COLOUR_MAPPER.apply(pokemob.getPokedexEntry(), newEntry);
                        stack.set(MEGA_STONE, stone);
                        final Component customName = stack.get(DataComponents.CUSTOM_NAME);
                        if (customName == null || "Mega Stone".equalsIgnoreCase(customName.getString().trim()))
                        {
                            final String stoneName = MegaStoneColours.getName(newEntry);
                            stack.set(DataComponents.CUSTOM_NAME,
                                    stoneName == null ? newEntry.getTranslatedName() : Component.literal(stoneName));
                        }
                        return handleChange(pokemob);
                    }
                }
                thut.lib.ChatHelper.sendSystemMessage(player,
                        TComponent.translatable("pokemob.megaevolve.failed", pokemob.getDisplayName()));
            }
            return true;
        }

        @Override
        public String changeKey()
        {
            return "mega-evolve";
        }

        @Override
        public int getPriority()
        {
            // high number so we go last.
            return 100;
        }

        @Override
        public void onFail(IPokemob pokemob)
        {
            final LivingEntity owner = pokemob.getOwner();
            if (owner instanceof ServerPlayer player) thut.lib.ChatHelper.sendSystemMessage(player,
                    TComponent.translatable("pokecube.mega.noring", pokemob.getDisplayName()));
        }

    }

    private static boolean isMega(IPokemob pokemob)
    {
        var entity = pokemob.getEntity();
        return entity.getPersistentData().contains("pokecube:megatime");
    }

    private static void megaEvolve(IPokemob pokemob, PokedexEntry newEntry, Component mess)
    {
        var entity = pokemob.getEntity();
        entity.getPersistentData().putLong("pokecube:megatime", Tracker.instance().getTick());
        MegaEvoTicker.scheduleEvolve(newEntry, pokemob, mess);
    }

    private static void onFormRevert(ChangeForm.Revert event)
    {
        var entity = event.getPokemob().getEntity();
        entity.getPersistentData().remove("pokecube:megatime");
        entity.getPersistentData().putBoolean("pokecube:mega_reverted", true);

        var reversion = MegaEvoData.REVERSIONS.get(event.getPokemob().getPokedexEntry());
        if (reversion != null && reversion != event.getPokemob().getBasePokedexEntry())
        {
            event.getPokemob().setBasePokedexEntry(reversion);
        }
    }

    private static void postFormChange(ChangeForm.Post event)
    {
        var entity = event.getPokemob().getEntity();
        if (entity.getPersistentData().contains("pokecube:mega_reverted"))
        {
            entity.getPersistentData().remove("pokecube:mega_reverted");
            entity.getPersistentData().remove("pokecube:mega_ability");
            entity.getPersistentData().remove("pokecube:mega_base");
        }
    }
}

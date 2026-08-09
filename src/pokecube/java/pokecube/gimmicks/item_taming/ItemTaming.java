package pokecube.gimmicks.item_taming;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.Nature;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.api.events.pokemobs.CaptureEvent;
import pokecube.api.events.pokemobs.FaintEvent;
import pokecube.api.events.pokemobs.InteractEvent;
import pokecube.api.events.pokemobs.RecallEvent;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.tasks.idle.HungerTask;
import pokecube.core.ai.tasks.idle.hunger.BaitCheckEvent;
import pokecube.core.entity.pokecubes.EntityPokecube;
import pokecube.core.init.EntityTypes;
import pokecube.core.init.Sounds;
import pokecube.core.items.berries.ItemBerry;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.item.ItemList;
import thut.core.common.ThutCore;
import thut.core.common.config.Config;
import thut.lib.TComponent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = PokecubeCore.MODID)
public class ItemTaming
{
    public static TamingConfig config = new TamingConfig();
    @SubscribeEvent
    public static void setupConfigs(final NewRegistryEvent event)
    {
        Config.setupConfigs(ModList.get().getModContainerById("pokecube").get(), config, PokecubeCore.MODID, "pokecube_item_taming");
    }

    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event)
    {
        if(config.itemTamingDisabled) return;

        // Subscribe events to pokecube bus, as @EventBusSubscriber does not handle those
        PokecubeAPI.POKEMOB_BUS.addListener(ItemTaming::onBaitCheck);
        PokecubeAPI.POKEMOB_BUS.addListener(ItemTaming::onFaint);
        PokecubeAPI.POKEMOB_BUS.addListener(ItemTaming::onPreRecall);

        // Subscribe this manually so that we could have disabled it above
        ThutCore.FORGE_BUS.addListener(ItemTaming::onInteract);
    }

    /**
     * Right clicking on it with an empty cube, will set that as the cube it belongs in
     */
    public static void onInteract(InteractEvent event)
    {
        var pokemob = event.pokemob;
        if(pokemob.getOwner() != event.player || pokemob.getEntity().level().isClientSide()) return;
        if(pokemob.getPokecube().getItem() == Items.BARRIER)
        {
            if(PokecubeItems.getCubeId(event.event.getItemStack()) != null && !PokecubeManager.isFilled(event.event.getItemStack()))
            {
                var stack = event.event.getItemStack().copyWithCount(1);
                event.event.getItemStack().consume(1, event.player);
                pokemob.setPokecube(stack);
                event.setCanceled(true);
            }
        }
    }

    public static void onBaitCheck(BaitCheckEvent event)
    {
        // Only handle this if it is hungry
        if(!event.task.hitThreshold(HungerTask.EATTHRESHOLD)) return;

        var pokemob = event.pokemob;
        if(pokemob.getOwnerId() != null) return;
        var mob = pokemob.getEntity();
        // Check for bait nearby
        List<ItemEntity> items = BrainUtils.getNearItems(mob);
        if(items==null || items.isEmpty()) return;
        items = new ArrayList<>(items);
        // Filter list to be only edible ones
        items.removeIf(item->!ItemList.is(HungerTask.FOODTAG, item.getItem()));
        if(items.isEmpty()) return;
        // Sort by closest
        items.sort((e0, e1)-> {
            var d0 = e0.distanceToSqr(mob);
            var d1 = e1.distanceToSqr(mob);
            return Double.compare(d0, d1);
        });
        // Now take the first one
        var nearest = items.getFirst();
        // If close enough, eat it
        if(nearest.distanceToSqr(mob)<1
                || nearest.getBoundingBox().inflate(1).intersects(mob.getBoundingBox()))
        {
            if(nearest.getOwner() instanceof LivingEntity owner)
            {
                double rate = 1.0; // pokecube rate by default
                int weight = 0;
                // Compute the cube bonus from how much it likes the berry if it is a berry
                if(nearest.getItem().getItem() instanceof ItemBerry berry){
                    var nature = pokemob.getNature();
                    weight = Nature.getBerryWeight(berry.type.index, nature);
                }

                int chance = Tools.computeCatchRate(pokemob, rate, weight);
                if(chance >= 4)
                {
                    var cube = new EntityPokecube(EntityTypes.getPokecube(), mob.level());
                    var stack = new ItemStack(Items.BARRIER);
                    cube.setItem(stack);
                    cube.copyPosition(mob);
                    final CaptureEvent.Pre capturePre = new CaptureEvent.Pre(pokemob, cube, mob);
                    PokecubeAPI.POKEMOB_BUS.post(capturePre);
                    if (capturePre.getResult() != TriState.FALSE)
                    {
                        if(mob instanceof Animal animal && owner instanceof Player player)
                            EventHooks.onAnimalTame(animal, player);
                        // Tame the mob
                        pokemob.setOwner(owner);
                        // Set the "cube" to something invalid for a cube
                        pokemob.setPokecube(stack);
                        // Ensure it is not sitting anymore
                        pokemob.setLogicState(LogicStates.SITTING, false);
                        if(owner instanceof Player player)
                            player.displayClientMessage(TComponent.translatable("pokecube.caught", pokemob.getDisplayName()), true);
                        owner.playSound(Sounds.CAPTURE_SOUND.get(), (float) PokecubeCore.getConfig().captureVolume, 1);

                        PokecubeManager.addToCube(stack, mob);
                        cube.setItem(stack);
                        CaptureEvent.Post capturePost = new CaptureEvent.Post(cube);
                        capturePost.setCaught(pokemob);
                        capturePost.setInWorldAfter();
                        PokecubeAPI.POKEMOB_BUS.post(capturePost);
                    }
                }
            }
            pokemob.eat(nearest);
        }
        else
        {
            // Try to path to it
            event.task.setWalkTo(mob, nearest,1,0);
        }
    }

    /**
     * Prevents the mob from poofing when it is dead
     */
    public static void onFaint(FaintEvent event)
    {
        var pokemob = event.pokemob;
        if(pokemob.getOwnerId() == null) return;
        if(pokemob.getPokecube().getItem() == Items.BARRIER)
        {
            event.setResult(TriState.FALSE);
        }
    }

    /**
     * Prevents the mob from being recalled, as it has no cube to recall to
     */
    public static void onPreRecall(RecallEvent.Pre event)
    {
        var pokemob = event.recalled;
        if(pokemob.getOwnerId() == null || pokemob.getEntity().level().isClientSide()) return;
        if(pokemob.getPokecube().getItem() == Items.BARRIER)
        {
            event.setCanceled(true);
        }
    }
}

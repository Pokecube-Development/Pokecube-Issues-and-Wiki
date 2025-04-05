package pokecube.core.items.pokecubes;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.pokemobs.HealEvent;
import pokecube.api.items.IPokecube.PokecubeBehaviour;
import pokecube.api.items.PokecubeContents;
import pokecube.api.items.PokesealContents;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.utils.TagNames;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import thut.lib.TComponent;

public class PokecubeManager
{
    public static final List<String> TAGSTOREMOVE = Lists.newArrayList();

    public static void init()
    {
        PokecubeManager.TAGSTOREMOVE.clear();
        PokecubeManager.TAGSTOREMOVE.add(TagNames.CAPTURING);
        PokecubeManager.TAGSTOREMOVE.add(TagNames.REMOVED);
        PokecubeManager.TAGSTOREMOVE.addAll(PokecubeCore.getConfig().persistent_tag_blacklist);
    }

    public static void addToCube(final ItemStack cube, final LivingEntity mob)
    {
        PokecubeContents contents = new PokecubeContents(mob);
        PokemobCaps.updatePokecube(cube, contents);
    }

    public static String getOwner(final ItemStack itemStack, Level level)
    {
        final UUID id = PokecubeManager.getOwnerId(itemStack, level);
        return id == null ? "" : id.toString();
    }

    public static UUID getOwnerId(final ItemStack itemStack, Level level)
    {
        PokecubeContents contents = PokemobCaps.getPokemobIn(itemStack, level);
        if (contents == null) return null;
        return contents.pokemob().getOwnerId();
    }

    public static PokedexEntry getPokedexEntry(final ItemStack itemStack, Level level)
    {
        PokecubeContents contents = PokemobCaps.getPokemobIn(itemStack, level);
        if (contents == null) return null;
        return contents.pokemob().getPokedexEntry();
    }

    public static CompoundTag getSealTag(final Entity pokemob)
    {
        final IPokemob poke = PokemobCaps.getPokemobFor(pokemob);
        ItemStack cube;
        if ((cube = poke.getPokecube()).isEmpty()) return null;
        return getSealTag(cube);
    }

    public static CompoundTag getSealTag(final ItemStack stack)
    {
        PokesealContents contents = PokemobCaps.getPokeseal(stack);
        return contents == null ? null : contents.tag();
    }

    public static int getStatus(final ItemStack itemStack, Level level)
    {
        PokecubeContents contents = PokemobCaps.getPokemobIn(itemStack, level);
        if (contents == null) return 0;
        return contents.pokemob().getStatus();
    }

    public static int getTilt(final ItemStack itemStack, Level level)
    {
        PokecubeContents contents = PokemobCaps.getPokemobIn(itemStack, level);
        return contents != null ? contents.getTilt() : 0;
    }

    public static Integer getUID(final ItemStack stack, Level level)
    {
        PokecubeContents contents = PokemobCaps.getPokemobIn(stack, level);
        if (contents == null) return 0;
        return contents.pokemob().getPokemonUID();
    }

    public static UUID getUUID(final ItemStack stack, Level level)
    {
        PokecubeContents contents = PokemobCaps.getPokemobIn(stack, level);
        return contents == null ? null : contents.entity().getUUID();
    }

    /**
     * Called the heal the mob, it will set health to max health, will reset
     * hurtTime and deathTime, and if a pokemob, will reset hunger back to full.
     * 
     * @param mob
     */
    public static void heal(final LivingEntity mob)
    {
        final IPokemob pokemob = PokemobCaps.getPokemobFor(mob);
        float maxHP = mob.getMaxHealth();
        if (pokemob != null)
        {
            pokemob.revive(true);
            maxHP = pokemob.getStat(Stats.HP, false);
            pokemob.setHungerTime(-PokecubeCore.getConfig().pokemobLifeSpan / 4);
        }
        mob.hurtTime = 0;
        mob.deathTime = 0;
        mob.setHealth(maxHP);
    }

    public static void heal(final ItemStack stack, final Level world, boolean fromHealer)
    {
        if (PokecubeManager.isFilled(stack))
        {
            try
            {
                final LivingEntity mob = PokecubeManager.itemToMob(stack, world);
                PokecubeAPI.POKEMOB_BUS.post(new HealEvent.Pre(mob, fromHealer));
                PokecubeManager.heal(mob);
                PokecubeAPI.POKEMOB_BUS.post(new HealEvent.Post(mob, fromHealer));
                PokecubeManager.addToCube(stack, mob);
                stack.set(DataComponents.ITEM_NAME, mob.getDisplayName());
            }
            catch (final Throwable e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void heal(final ItemStack stack, final Level world)
    {
        heal(stack, world, true);
    }

    public static boolean isFilled(final ItemStack stack)
    {
        return PokemobCaps.isFilled(stack);
    }

    public static LivingEntity itemToMob(final ItemStack stack, Level world)
    {
        if (world == null)
        {
            world = PokecubeCore.proxy.getWorld();
            PokecubeAPI.LOGGER.catching(new NullPointerException("World null when itemToMob!"));
        }
        return PokemobCaps.getPokemobIn(stack, world).entity();
    }

    public static IPokemob itemToPokemob(final ItemStack itemStack, final Level world)
    {
        return PokemobCaps.getPokemobIn(itemStack, world).pokemob();
    }

    public static ItemStack pokemobToItem(final IPokemob pokemob)
    {
        ItemStack itemStack = pokemob.getPokecube();
        if (itemStack.isEmpty())
            itemStack = new ItemStack(PokecubeItems.getFilledCube(PokecubeBehaviour.DEFAULTCUBE), 1);
        itemStack = itemStack.copy();
        PokecubeManager.addToCube(itemStack, pokemob.getEntity());
        itemStack.setCount(1);
        PokecubeManager.setColor(itemStack);
        final int status = pokemob.getStatus();
        Component name = pokemob.getDisplayName();
        if (status == IMoveConstants.STATUS_BRN) name = TComponent.translatable("pokecube.filled.brn", name);
        else if (status == IMoveConstants.STATUS_FRZ) name = TComponent.translatable("pokecube.filled.frz", name);
        else if (status == IMoveConstants.STATUS_PAR) name = TComponent.translatable("pokecube.filled.par", name);
        else if (status == IMoveConstants.STATUS_SLP) name = TComponent.translatable("pokecube.filled.slp", name);
        else if (status == IMoveConstants.STATUS_PSN || status == IMoveConstants.STATUS_PSN2)
            name = TComponent.translatable("pokecube.filled.psn", name);
        itemStack.set(DataComponents.ITEM_NAME, name);
        return itemStack;
    }

    public static void setColor(final ItemStack itemStack)
    {
        // TODO tooltip colour
//        int color = 0xEEEEEE;
//
//        final ResourceLocation id = PokecubeItems.getCubeId(itemStack);
//
//        if (ItemList.is(PokecubeItems.POKEMOBEGG, itemStack)) color = 0x78C848;
//        else if (id != null) if (id.getPath().equals("poke")) color = 0xEE0000;
//        else if (id.getPath().equals("great")) color = 0x0B90CE;
//        else if (id.getPath().equals("ultra")) color = 0xDCA937;
//        else if (id.getPath().equals("master")) color = 0x332F6A;
//
//        CompoundTag var3 = itemStack.getTag();
//
//        if (var3 == null)
//        {
//            var3 = new CompoundTag();
//            itemStack.setTag(var3);
//        }
//
//        final CompoundTag var4 = var3.getCompound("display");
//
//        if (!var3.contains("display")) var3.put("display", var4);
//
//        var4.putInt("cubecolor", color);
    }

    public static void setTilt(final ItemStack stack, final int number, Level level)
    {
        PokecubeContents contents = PokemobCaps.getPokemobIn(stack, level);
        if (contents == null) return;
        PokemobCaps.updatePokecube(stack, contents.withTilt(number));
    }
}
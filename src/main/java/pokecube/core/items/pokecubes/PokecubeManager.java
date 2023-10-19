package pokecube.core.items.pokecubes;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.pokemobs.HealEvent;
import pokecube.api.items.IPokecube.PokecubeBehaviour;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.utils.TagNames;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import thut.api.item.ItemList;
import thut.core.common.network.EntityUpdate;
import thut.lib.RegHelper;
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
        final ResourceLocation id = RegHelper.getKey(mob.getType());
        if (!cube.hasTag()) cube.setTag(new CompoundTag());
        cube.getTag().putString(TagNames.MOBID, id.toString());
        final CompoundTag tag = new CompoundTag();
        mob.saveWithoutId(tag);
        cube.getTag().putFloat("CHP", mob.getHealth());
        cube.getTag().putFloat("MHP", mob.getMaxHealth());
        cube.getTag().put(TagNames.POKEMOB, tag);
    }

    public static PokedexEntry getEntry(final ItemStack cube)
    {
        PokedexEntry ret = null;
        if (PokecubeManager.isFilled(cube))
        {
            final CompoundTag poketag = cube.getTag().getCompound(TagNames.POKEMOB);
            if (poketag != null)
            {
                final String forme = poketag.getString("forme");
                if (forme != null && !forme.isEmpty()) ret = Database.getEntry(forme);
            }
        }
        return ret;
    }

    public static ItemStack getHeldItem(final ItemStack stack)
    {
        if (!PokecubeManager.isFilled(stack)) return ItemStack.EMPTY;
        try
        {
            final ListTag equipmentTags = (ListTag) TagNames.getPokecubePokemobTag(stack.getTag())
                    .getCompound(TagNames.INVENTORYTAG).get(TagNames.ITEMS);
            for (int i = 0; i < equipmentTags.size(); i++)
            {
                final byte slot = equipmentTags.getCompound(i).getByte("Slot");
                if (slot != 1) continue;
                final ItemStack held = ItemStack.of(equipmentTags.getCompound(i));
                return held;
            }
        }
        catch (final Exception e)
        {}
        return ItemStack.EMPTY;
    }

    public static String getOwner(final ItemStack itemStack)
    {
        final UUID id = PokecubeManager.getOwnerId(itemStack);
        return id == null ? "" : id.toString();
    }

    public static UUID getOwnerId(final ItemStack itemStack)
    {
        if (itemStack.isEmpty() || !itemStack.hasTag()) return null;

        // First try reading the ownership from the mob's data directly:
        Tag owner = itemStack.getTag().getCompound(TagNames.POKEMOB).get("Owner");

        if (owner == null)
        {
            // Otherwise try looking in the pokemob's ownership tag
            final CompoundTag poketag = TagNames.getPokecubePokemobTag(itemStack.getTag());
            if (!poketag.contains(TagNames.OWNERSHIPTAG)) return null;
            owner = poketag.getCompound(TagNames.OWNERSHIPTAG).get(TagNames.OWNER);
        }

        if (owner != null) try
        {
            final UUID id = NbtUtils.loadUUID(owner);
            return id;
        }
        catch (final Exception e)
        {
            // Failed there
        }
        return null;
    }

    public static PokedexEntry getPokedexEntry(final ItemStack itemStack)
    {
        if (itemStack.isEmpty() || !itemStack.hasTag()) return null;
        final CompoundTag poketag = TagNames.getPokecubePokemobTag(itemStack.getTag());
        if (!poketag.contains(TagNames.VISUALSTAG)) return null;
        final String forme = poketag.getCompound(TagNames.VISUALSTAG).getString(TagNames.FORME);
        final PokedexEntry entry = Database.getEntry(forme);
        return entry == null ? Database.missingno : entry;
    }

    public static CompoundTag getSealTag(final Entity pokemob)
    {
        final IPokemob poke = PokemobCaps.getPokemobFor(pokemob);
        ItemStack cube;
        if ((cube = poke.getPokecube()).isEmpty()) return null;
        return cube.getTagElement(TagNames.POKESEAL);
    }

    public static CompoundTag getSealTag(final ItemStack stack)
    {
        if (PokecubeManager.isFilled(stack))
            return stack.getTag().getCompound(TagNames.POKEMOB).getCompound(TagNames.VISUALSTAG)
                    .getCompound(TagNames.POKECUBE).getCompound("tag").getCompound(TagNames.POKESEAL);
        else if (stack.hasTag()) return stack.getTag().getCompound(TagNames.POKESEAL);
        return null;
    }

    public static byte getStatus(final ItemStack itemStack)
    {
        if (!itemStack.hasTag()) return 0;
        final CompoundTag poketag = TagNames.getPokecubePokemobTag(itemStack.getTag());
        return poketag.getCompound(TagNames.STATSTAG).getByte(TagNames.STATUS);
    }

    public static int getTilt(final ItemStack itemStack)
    {
        return itemStack.hasTag() && itemStack.getTag().contains("tilt") ? itemStack.getTag().getInt("tilt") : 0;
    }

    public static Integer getUID(final ItemStack stack)
    {
        if (!PokecubeManager.isFilled(stack)) return null;
        final CompoundTag poketag = TagNames.getPokecubePokemobTag(stack.getTag());
        return poketag.getCompound(TagNames.MISCTAG).getInt(TagNames.UID);
    }

    public static UUID getUUID(final ItemStack stack)
    {
        if (!PokecubeManager.isFilled(stack)) return null;
        final CompoundTag pokeTag = stack.getTag().getCompound(TagNames.POKEMOB);
        try
        {
            return pokeTag.getUUID("UUID");
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.warn("Error getting UUID from cube! " + stack + " " + pokeTag);
            return null;
        }
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
                PokecubeManager.setStatus(stack, IMoveConstants.STATUS_NON);
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
        return stack.hasTag() && stack.getTag().contains(TagNames.MOBID);
    }

    public static LivingEntity itemToMob(final ItemStack stack, Level world)
    {
        if (!stack.hasTag()) return null;
        final String id = stack.getTag().getString(TagNames.MOBID);
        if (id.isEmpty()) return null;
        if (world == null)
        {
            world = PokecubeCore.proxy.getWorld();
            PokecubeAPI.LOGGER.catching(new NullPointerException("World null when itemToMob!"));
        }
        if (world == null)
        {
            PokecubeAPI.LOGGER.catching(new NullPointerException("World Still null when itemToMob!"));
            return null;
        }
        final EntityType<?> type = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id));
        final LivingEntity mob = (LivingEntity) type.create(world);
        try
        {
            final CompoundTag tag = stack.getTag().getCompound(TagNames.POKEMOB);
            for (final String key : PokecubeManager.TAGSTOREMOVE) tag.getCompound("ForgeData").remove(key);
            EntityUpdate.readMob(mob, tag);
        }
        catch (final Exception e)
        {
            // Nope, some mobs can't read from this on clients.
            if (world instanceof ServerLevel)
            {
                PokecubeAPI.LOGGER.error("Error reading cube: {}", stack.getTag());
                PokecubeAPI.LOGGER.error(e);
            }
        }
        return mob;
    }

    public static IPokemob itemToPokemob(final ItemStack itemStack, final Level world)
    {
        final Entity mob = PokecubeManager.itemToMob(itemStack, world);
        if (mob == null) return null;
        final IPokemob pokemob = PokemobCaps.getPokemobFor(mob);
        if (pokemob == null) return null;
        ItemStack cubeStack = pokemob.getPokecube();
        if (cubeStack.isEmpty())
        {
            cubeStack = itemStack.copy();
            cubeStack.getTag().remove(TagNames.POKEMOB);
            pokemob.setPokecube(cubeStack);
        }
        return pokemob;
    }

    public static ItemStack pokemobToItem(final IPokemob pokemob)
    {
        ItemStack itemStack = pokemob.getPokecube();
        if (itemStack.isEmpty())
            itemStack = new ItemStack(PokecubeItems.getFilledCube(PokecubeBehaviour.DEFAULTCUBE), 1);
        itemStack = itemStack.copy();
        PokecubeManager.addToCube(itemStack, pokemob.getEntity());
        itemStack.setCount(1);
        itemStack.getTag().remove(TagNames.POKESEAL);
        PokecubeManager.setColor(itemStack);
        final int status = pokemob.getStatus();
        PokecubeManager.setStatus(itemStack, pokemob.getStatus());
        Component name = pokemob.getDisplayName();
        if (status == IMoveConstants.STATUS_BRN) name = TComponent.translatable("pokecube.filled.brn", name);
        else if (status == IMoveConstants.STATUS_FRZ) name = TComponent.translatable("pokecube.filled.frz", name);
        else if (status == IMoveConstants.STATUS_PAR) name = TComponent.translatable("pokecube.filled.par", name);
        else if (status == IMoveConstants.STATUS_SLP) name = TComponent.translatable("pokecube.filled.slp", name);
        else if (status == IMoveConstants.STATUS_PSN || status == IMoveConstants.STATUS_PSN2)
            name = TComponent.translatable("pokecube.filled.psn", name);
        itemStack.setHoverName(name);
        return itemStack;
    }

    public static void setColor(final ItemStack itemStack)
    {
        int color = 0xEEEEEE;

        final ResourceLocation id = PokecubeItems.getCubeId(itemStack);

        if (ItemList.is(PokecubeItems.POKEMOBEGG, itemStack)) color = 0x78C848;
        else if (id != null) if (id.getPath().equals("poke")) color = 0xEE0000;
        else if (id.getPath().equals("great")) color = 0x0B90CE;
        else if (id.getPath().equals("ultra")) color = 0xDCA937;
        else if (id.getPath().equals("master")) color = 0x332F6A;

        CompoundTag var3 = itemStack.getTag();

        if (var3 == null)
        {
            var3 = new CompoundTag();
            itemStack.setTag(var3);
        }

        final CompoundTag var4 = var3.getCompound("display");

        if (!var3.contains("display")) var3.put("display", var4);

        var4.putInt("cubecolor", color);
    }

    public static void setStatus(final ItemStack itemStack, final int status)
    {
        if (!itemStack.hasTag()) return;
        final CompoundTag poketag = TagNames.getPokecubePokemobTag(itemStack.getTag());
        poketag.getCompound(TagNames.STATSTAG).putInt(TagNames.STATUS, status);
    }

    public static void setTilt(final ItemStack itemStack, final int number)
    {
        if (!itemStack.hasTag()) itemStack.setTag(new CompoundTag());
        itemStack.getTag().putInt("tilt", number);
    }
}
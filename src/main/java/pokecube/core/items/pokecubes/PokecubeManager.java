package pokecube.core.items.pokecubes;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.TagNames;
import thut.api.item.ItemList;

public class PokecubeManager
{
    public static void addToCube(final ItemStack cube, final LivingEntity mob)
    {
        final ResourceLocation id = mob.getType().getRegistryName();
        if (!cube.hasTag()) cube.setTag(new CompoundNBT());
        cube.getTag().putString(TagNames.MOBID, id.toString());
        final CompoundNBT tag = new CompoundNBT();
        mob.writeWithoutTypeId(tag);
        cube.getTag().putFloat("CHP", mob.getHealth());
        cube.getTag().putFloat("MHP", mob.getMaxHealth());
        cube.getTag().put(TagNames.POKEMOB, tag);
    }

    public static PokedexEntry getEntry(final ItemStack cube)
    {
        PokedexEntry ret = null;
        if (PokecubeManager.isFilled(cube))
        {
            final CompoundNBT poketag = cube.getTag().getCompound(TagNames.POKEMOB);
            if (poketag != null)
            {
                final String forme = poketag.getString("forme");
                if (forme != null && !forme.isEmpty()) ret = Database.getEntry(forme);
            }
            if (ret == null)
            {
                final int num = PokecubeManager.getPokedexNb(cube);
                ret = Database.getEntry(num);
            }
        }
        return ret;
    }

    public static ItemStack getHeldItem(final ItemStack stack)
    {
        if (!PokecubeManager.isFilled(stack)) return ItemStack.EMPTY;
        try
        {
            final ListNBT equipmentTags = (ListNBT) TagNames.getPokecubePokemobTag(stack.getTag()).getCompound(
                    TagNames.INVENTORYTAG).get(TagNames.ITEMS);
            for (int i = 0; i < equipmentTags.size(); i++)
            {
                final byte slot = equipmentTags.getCompound(i).getByte("Slot");
                if (slot != 1) continue;
                final ItemStack held = ItemStack.read(equipmentTags.getCompound(i));
                return held;
            }
        }
        catch (final Exception e)
        {
        }
        return ItemStack.EMPTY;
    }

    public static String getOwner(final ItemStack itemStack)
    {
        if (itemStack.isEmpty() || !itemStack.hasTag()) return "";
        final CompoundNBT poketag = TagNames.getPokecubePokemobTag(itemStack.getTag());
        return poketag.getCompound(TagNames.OWNERSHIPTAG).getString(TagNames.OWNER);
    }

    public static PokedexEntry getPokedexEntry(final ItemStack itemStack)
    {
        if (itemStack.isEmpty() || !itemStack.hasTag()) return null;
        final CompoundNBT poketag = TagNames.getPokecubePokemobTag(itemStack.getTag());
        final int number = poketag.getCompound(TagNames.OWNERSHIPTAG).getInt(TagNames.POKEDEXNB);
        if (!poketag.contains(TagNames.OWNERSHIPTAG)) return null;
        if (poketag.isEmpty() || number == 0) return Database.getEntry(PokecubeManager.getPokedexNb(itemStack));
        final String forme = poketag.getCompound(TagNames.VISUALSTAG).getString(TagNames.FORME);
        final PokedexEntry entry = Database.getEntry(forme);
        return entry == null ? Database.getEntry(number) : entry;
    }

    public static int getPokedexNb(final ItemStack itemStack)
    {
        if (itemStack.isEmpty() || !itemStack.hasTag()) return 0;
        final CompoundNBT poketag = TagNames.getPokecubePokemobTag(itemStack.getTag());
        if (poketag == null || poketag.isEmpty()) return 0;
        return poketag.getCompound(TagNames.OWNERSHIPTAG).getInt(TagNames.POKEDEXNB);
    }

    public static CompoundNBT getSealTag(final Entity pokemob)
    {
        final IPokemob poke = CapabilityPokemob.getPokemobFor(pokemob);
        ItemStack cube;
        if ((cube = poke.getPokecube()).isEmpty()) return null;
        return cube.getChildTag(TagNames.POKESEAL);
    }

    public static CompoundNBT getSealTag(final ItemStack stack)
    {
        if (PokecubeManager.isFilled(stack)) return stack.getTag().getCompound(TagNames.POKEMOB).getCompound(
                TagNames.VISUALSTAG).getCompound(TagNames.POKECUBE).getCompound("tag").getCompound(TagNames.POKESEAL);
        else if (stack.hasTag()) return stack.getTag().getCompound(TagNames.POKESEAL);
        return null;
    }

    public static byte getStatus(final ItemStack itemStack)
    {
        if (!itemStack.hasTag()) return 0;
        final CompoundNBT poketag = TagNames.getPokecubePokemobTag(itemStack.getTag());
        return poketag.getCompound(TagNames.STATSTAG).getByte(TagNames.STATUS);
    }

    public static int getTilt(final ItemStack itemStack)
    {
        return itemStack.hasTag() && itemStack.getTag().contains("tilt") ? itemStack.getTag().getInt("tilt") : 0;
    }

    public static Integer getUID(final ItemStack stack)
    {
        if (!PokecubeManager.isFilled(stack)) return null;
        final CompoundNBT poketag = TagNames.getPokecubePokemobTag(stack.getTag());
        return poketag.getCompound(TagNames.MISCTAG).getInt(TagNames.UID);
    }

    public static UUID getUUID(final ItemStack stack)
    {
        if (!PokecubeManager.isFilled(stack)) return null;
        final CompoundNBT pokeTag = stack.getTag().getCompound(TagNames.POKEMOB);
        final long min = pokeTag.getLong("UUIDLeast");
        final long max = pokeTag.getLong("UUIDMost");
        return new UUID(max, min);
    }

    public static void heal(final LivingEntity mob)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        float maxHP = mob.getMaxHealth();
        if (pokemob != null)
        {
            pokemob.revive();
            maxHP = pokemob.getStat(Stats.HP, false);
        }
        mob.hurtTime = 0;
        mob.deathTime = 0;
        mob.setHealth(maxHP);
    }

    public static void heal(final ItemStack stack, final World world)
    {
        if (PokecubeManager.isFilled(stack))
        {
            try
            {
                final LivingEntity mob = PokecubeManager.itemToMob(stack, world);
                PokecubeManager.heal(mob);
                PokecubeManager.addToCube(stack, mob);
            }
            catch (final Throwable e)
            {
                e.printStackTrace();
            }
            final CompoundNBT poketag = TagNames.getPokecubePokemobTag(stack.getTag());
            poketag.getCompound(TagNames.AITAG).putInt(TagNames.HUNGER, -PokecubeCore.getConfig().pokemobLifeSpan / 4);
            PokecubeManager.setStatus(stack, IMoveConstants.STATUS_NON);
        }
    }

    public static boolean isFilled(final ItemStack stack)
    {
        return stack.hasTag() && stack.getTag().contains(TagNames.MOBID);
    }

    public static LivingEntity itemToMob(final ItemStack stack, World world)
    {
        if (!stack.hasTag()) return null;
        final String id = stack.getTag().getString(TagNames.MOBID);
        if (id.isEmpty()) return null;
        if (world == null)
        {
            world = PokecubeCore.proxy.getWorld();
            PokecubeCore.LOGGER.catching(new NullPointerException("World null when itemToMob!"));
        }
        if (world == null)
        {
            PokecubeCore.LOGGER.catching(new NullPointerException("World Still null when itemToMob!"));
            return null;
        }
        final EntityType<?> type = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id));
        final LivingEntity mob = (LivingEntity) type.create(world);
        try
        {
            mob.read(stack.getTag().getCompound(TagNames.POKEMOB));
        }
        catch (final Exception e)
        {
            // Nope, some mobs can't read from this on clients.
            if (world instanceof ServerWorld)
            {
                PokecubeCore.LOGGER.error("Error reading cube: {}", stack.getTag());
                PokecubeCore.LOGGER.error(e);
            }
        }
        return mob;
    }

    public static IPokemob itemToPokemob(final ItemStack itemStack, final World world)
    {
        final Entity mob = PokecubeManager.itemToMob(itemStack, world);
        if (mob == null) return null;
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
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
        if (itemStack.isEmpty()) itemStack = new ItemStack(PokecubeItems.getFilledCube(PokecubeBehavior.DEFAULTCUBE),
                1);
        itemStack = itemStack.copy();
        PokecubeManager.addToCube(itemStack, pokemob.getEntity());
        itemStack.setCount(1);
        itemStack.getTag().remove(TagNames.POKESEAL);
        PokecubeManager.setColor(itemStack);
        final int status = pokemob.getStatus();
        PokecubeManager.setStatus(itemStack, pokemob.getStatus());
        ITextComponent name = pokemob.getDisplayName();
        if (status == IMoveConstants.STATUS_BRN) name = new TranslationTextComponent("pokecube.filled.brn", name);
        else if (status == IMoveConstants.STATUS_FRZ) name = new TranslationTextComponent("pokecube.filled.frz", name);
        else if (status == IMoveConstants.STATUS_PAR) name = new TranslationTextComponent("pokecube.filled.par", name);
        else if (status == IMoveConstants.STATUS_SLP) name = new TranslationTextComponent("pokecube.filled.slp", name);
        else if (status == IMoveConstants.STATUS_PSN || status == IMoveConstants.STATUS_PSN2)
            name = new TranslationTextComponent("pokecube.filled.psn", name);
        itemStack.setDisplayName(name);
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

        CompoundNBT var3 = itemStack.getTag();

        if (var3 == null)
        {
            var3 = new CompoundNBT();
            itemStack.setTag(var3);
        }

        final CompoundNBT var4 = var3.getCompound("display");

        if (!var3.contains("display")) var3.put("display", var4);

        var4.putInt("cubecolor", color);
    }

    @Deprecated
    public static void setOwner(final ItemStack itemStack, final UUID owner)
    {
        if (!itemStack.hasTag()) return;
        final CompoundNBT poketag = TagNames.getPokecubePokemobTag(itemStack.getTag());
        if (owner == null) poketag.getCompound(TagNames.OWNERSHIPTAG).remove(TagNames.OWNER);
        else poketag.getCompound(TagNames.OWNERSHIPTAG).putString(TagNames.OWNER, owner.toString());
    }

    public static void setStatus(final ItemStack itemStack, final byte status)
    {
        if (!itemStack.hasTag()) return;
        final CompoundNBT poketag = TagNames.getPokecubePokemobTag(itemStack.getTag());
        poketag.getCompound(TagNames.STATSTAG).putByte(TagNames.STATUS, status);
    }

    public static void setTilt(final ItemStack itemStack, final int number)
    {
        if (!itemStack.hasTag()) itemStack.setTag(new CompoundNBT());
        itemStack.getTag().putInt("tilt", number);
    }
}
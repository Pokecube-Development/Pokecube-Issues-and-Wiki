package pokecube.core.items;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokeType;
import thut.core.common.ThutCore;

public class ItemTM extends Item
{
    private static Map<PokeType, ItemTM> tms = Maps.newHashMap();

    public static boolean applyEffect(final LivingEntity mob, final ItemStack stack)
    {
        if (mob.getEntityWorld().isRemote) return stack.hasTag();
        if (stack.hasTag()) return ItemTM.feedToPokemob(stack, mob);
        return false;
    }

    public static boolean feedToPokemob(final ItemStack stack, final Entity entity)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
        if (pokemob != null) return ItemTM.teachToPokemob(stack, pokemob);
        return false;
    }

    public static String getMoveFromStack(final ItemStack stack)
    {
        if (stack.getItem() instanceof ItemTM)
        {
            final CompoundNBT nbt = stack.getTag();
            if (nbt == null) return null;
            final String name = nbt.getString("move");
            if (!name.contentEquals("")) return name;
        }
        return null;
    }

    public static ItemStack getTM(final String move)
    {
        ItemStack stack = ItemStack.EMPTY;
        final Move_Base attack = MovesUtils.getMoveFromName(move.trim());
        if (attack == null)
        {
            PokecubeCore.LOGGER.error("Attempting to make TM for un-registered move: " + move);
            return stack;
        }
        stack = new ItemStack(ItemTM.tms.get(attack.move.type));
        final CompoundNBT nbt = stack.getTag() == null ? new CompoundNBT() : stack.getTag();
        nbt.putString("move", move.trim());
        stack.setTag(nbt);
        final ITextComponent name = MovesUtils.getMoveName(move.trim());
        stack.setDisplayName(name);
        return stack;
    }

    public static boolean teachToPokemob(final ItemStack tm, final IPokemob mob)
    {
        if (tm.getItem() instanceof ItemTM)
        {
            final CompoundNBT nbt = tm.getTag();
            if (nbt == null) return false;
            final String name = nbt.getString("move");
            if (name.contentEquals("")) return false;
            for (final String move : mob.getMoves())
                if (name.equals(move)) return false;
            final String[] learnables = mob.getPokedexEntry().getMoves().toArray(new String[0]);
            final int index = mob.getMoveIndex();
            if (index > 3) return false;
            for (final String s : learnables)
                if (mob.getPokedexNb() == 151 || ThutCore.trim(s).equals(ThutCore.trim(name)) || PokecubeMod.debug)
                {
                    if (mob.getMove(0) == null) mob.setMove(0, name);
                    else if (mob.getMove(1) == null) mob.setMove(1, name);
                    else if (mob.getMove(2) == null) mob.setMove(2, name);
                    else if (mob.getMove(3) == null) mob.setMove(3, name);
                    else mob.setMove(index, name);
                    return true;
                }
        }

        return false;
    }

    public final PokeType type;

    public ItemTM(final Properties props, final PokeType type)
    {
        super(props);
        this.setRegistryName(PokecubeMod.ID, "tm" + type.ordinal());
        this.type = type;
        ItemTM.tms.put(type, this);
    }

    /**
     * If this function returns true (or the item is damageable), the
     * ItemStack's NBT tag will be sent to the client.
     */
    @Override
    public boolean shouldSyncTag()
    {
        return true;
    }

}

package pokecube.core.items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.MoveEntry;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.moves.MovesUtils;
import thut.core.common.ThutCore;

public class ItemTM extends Item
{
    public static List<Predicate<String>> INVALID_TMS = new ArrayList<>();

    static
    {
        INVALID_TMS.add(move -> move.equals(MoveEntry.CONFUSED.name));
    }

    public static boolean applyEffect(final LivingEntity mob, final ItemStack stack)
    {
        if (mob.getLevel().isClientSide) return stack.hasTag();
        if (stack.hasTag()) return ItemTM.feedToPokemob(stack, mob);
        return false;
    }

    public static boolean feedToPokemob(final ItemStack stack, final Entity entity)
    {
        final IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
        if (pokemob != null) return ItemTM.teachToPokemob(stack, pokemob);
        return false;
    }

    public static String getMoveFromStack(final ItemStack stack)
    {
        if (stack.getItem() instanceof ItemTM)
        {
            final CompoundTag nbt = stack.getTag();
            if (nbt == null) return null;
            final String name = nbt.getString("move");
            if (!name.contentEquals("")) return name;
        }
        return null;
    }

    public static ItemStack getTM(final String move)
    {
        ItemStack stack = ItemStack.EMPTY;
        if (INVALID_TMS.stream().anyMatch(s -> s.test(move))) return stack;

        final MoveEntry attack = MovesUtils.getMove(move.trim());
        if (attack == null)
        {
            PokecubeAPI.LOGGER.error("Attempting to make TM for un-registered move: " + move);
            return stack;
        }
        stack = new ItemStack(PokecubeItems.TM.get());
        final CompoundTag nbt = stack.getTag() == null ? new CompoundTag() : stack.getTag();
        nbt.putString("move", move.trim());
        stack.setTag(nbt);
        final Component name = MovesUtils.getMoveName(move.trim(), null);
        stack.setHoverName(name);
        return stack;
    }

    public static boolean teachToPokemob(final ItemStack tm, final IPokemob mob)
    {
        if (tm.getItem() instanceof ItemTM)
        {
            final CompoundTag nbt = tm.getTag();
            if (nbt == null) return false;
            final String name = nbt.getString("move");
            if (name.contentEquals("")) return false;
            if (mob.knowsMove(name)) return false;
            final String[] learnables = mob.getPokedexEntry().getMoves().toArray(new String[0]);
            for (final String s : learnables) if (mob.getPokedexNb() == 151
                    || ThutCore.trim(s).equals(ThutCore.trim(name)) || PokecubeCore.getConfig().debug_misc)
            {
                mob.learn(name);
                return true;
            }
        }
        return false;
    }

    public ItemTM(final Properties props)
    {
        super(props);
    }

    /**
     * If this function returns true (or the item is damageable), the
     * ItemStack's NBT tag will be sent to the client.
     */
    @Override
    public boolean shouldOverrideMultiplayerNbt()
    {
        return true;
    }

}

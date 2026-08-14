package pokecube.core.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.registries.DeferredRegister;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.MoveEntry;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.moves.MovesUtils;
import thut.core.common.ThutCore;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ItemTM extends Item
{
    public static record TMData(String moveName)
    {
        public static final Codec<TMData> CODEC = Codec.STRING.<TMData>comapFlatMap(TMData::read, TMData::moveName)
                .stable();

        public static final StreamCodec<ByteBuf, TMData> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(TMData::new,
                TMData::moveName);

        public static DataResult<TMData> read(String location)
        {
            try
            {
                return DataResult.success(new TMData(location));
            }
            catch (ResourceLocationException resourcelocationexception)
            {
                return DataResult.error(() -> "Not a valid resource location: " + location + " "
                        + resourcelocationexception.getMessage());
            }
        }
    }

    public static Supplier<DataComponentType<TMData>> TM_DATA;

    public static List<Predicate<String>> INVALID_TMS = new ArrayList<>();

    static
    {
        INVALID_TMS.add(move -> move.equals(MoveEntry.CONFUSED.name));
    }

    public static void registerComponents(DeferredRegister<DataComponentType<?>> registry)
    {
        TM_DATA = registry.register("tm_data", name -> new DataComponentType.Builder<TMData>().persistent(TMData.CODEC)
                .networkSynchronized(TMData.STREAM_CODEC).build());
    }

    public static boolean applyEffect(final LivingEntity mob, final ItemStack stack)
    {
        var info = stack.get(TM_DATA);
        if (mob.level().isClientSide) return info != null;
        if (info != null) return ItemTM.feedToPokemob(stack, mob);
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
        var info = stack.get(TM_DATA);
        return info == null ? null : info.moveName();
    }

    public static void addMoveToStack(ItemStack stack, String move)
    {
        stack.set(TM_DATA, new TMData(move));
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
        addMoveToStack(stack, move.trim());
        final Component name = MovesUtils.getMoveName(move.trim(), null);
        stack.set(DataComponents.CUSTOM_NAME, name);
        return stack;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
            TooltipFlag tooltipFlag)
    {
        var info = stack.get(TM_DATA);
        if (info != null)
        {
            Component name = Component.translatable("pokemob.move." + info.moveName());
            var entry = MovesUtils.getMove(info.moveName());
            if (!stack.has(DataComponents.CUSTOM_NAME))
            {
                stack.set(DataComponents.CUSTOM_NAME, name);
            }
            if (!stack.has(DataComponents.DYED_COLOR) && entry != null)
            {
                stack.set(DataComponents.DYED_COLOR, new DyedItemColor(entry.type.colour | 0xFF000000, false));
            }
            if (tooltipFlag.hasShiftDown())
            {
                if (entry != null && entry.root_entry._effect_text_simple != null)
                    tooltipComponents.add(Component.literal(entry.root_entry._effect_text_simple));
            }
        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    public static boolean teachToPokemob(final ItemStack tm, final IPokemob mob)
    {
        var info = tm.get(TM_DATA);
        if (tm != null)
        {
            final String name = info.moveName();
            if (name.contentEquals("")) return false;
            if (mob.knowsMove(name)) return false;
            final String[] learnables = mob.getPokedexEntry().getMoves().toArray(new String[0]);
            for (final String s : learnables)
                if (mob.getPokedexNb() == 151 || ThutCore.trim(s).equals(ThutCore.trim(name))
                        || PokecubeCore.getConfig().debug_misc)
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
}

package pokecube.compat.jei.ingredients;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.FormeHolder;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.database.Database;
import thut.lib.TCodecs;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Pokemob implements IIngredientType<PokedexEntry>
{
    public static class IngredientHelper implements IIngredientHelper<Pokemob>
    {
        @Override
        public Pokemob copyIngredient(final Pokemob arg0)
        {
            return arg0;
        }

        @Override
        public String getDisplayName(final Pokemob arg0)
        {
            return arg0.entry.getName();
        }

        @Override
        public String getErrorInfo(final Pokemob arg0)
        {
            return arg0.entry.getTrimmedName();
        }

        @Override
        public ResourceLocation getResourceLocation(Pokemob arg0)
        {
            return ResourceLocation.fromNamespaceAndPath(arg0.entry.getModId(), arg0.entry.getTrimmedName());
        }

        @Override
        public String getUniqueId(final Pokemob ingredient, final UidContext context)
        {
            return ingredient.entry.getTrimmedName();
        }

        @Override
        public IIngredientType<Pokemob> getIngredientType()
        {
            return TYPE;
        }

    }

    public static class IngredientRenderer implements IIngredientRenderer<Pokemob>
    {
        @Override
        public List<Component> getTooltip(Pokemob pokemob, TooltipFlag flag)
        {
            final List<Component> list = Lists.newArrayList(
                    Component.translatable(pokemob.entry.getUnlocalizedName()));
            if (pokemob.holder != null) list.add(Component.literal(pokemob.holder.name));
            return list;
        }

        @Override
        public void render(GuiGraphics graphics, Pokemob pokemob)
        {
            if (pokemob != null)
            {
                final byte gender = pokemob.gender;
                final FormeHolder holder = pokemob.holder == null ? pokemob.entry.getModel(gender) : pokemob.holder;
//                Vector4f test = new Vector4f(1, 1, 1, 1);
//                test.mul(graphics.pose().last().pose());
//                int x = (int) test.x();
//                int y = (int) test.y();
                EventsHandlerClient.renderIcon(graphics, pokemob.entry, holder, gender == IPokemob.FEMALE, 0, 0,
                        16, 16, false);
            }
        }
    }

    public static final IIngredientType<Pokemob> TYPE = () -> Pokemob.class;

    public static final IngredientRenderer RENDER = new IngredientRenderer();
    public static final IngredientHelper HELPER = new IngredientHelper();

    private static final List<Pokemob> ALL = Lists.newArrayList();

    public static final Map<PokedexEntry, Pokemob> ALLMAP = Maps.newHashMap();
    public static final Map<FormeHolder, Pokemob> FORMMAP = Maps.newHashMap();

    public static final Codec<PokedexEntry> POKEDEX_ENTRY_CODEC = RecordCodecBuilder.create(
            i -> i.group(Codec.STRING.fieldOf("entry").forGetter(p -> p.name)).apply(i, Database::getEntry));
    public static final Codec<FormeHolder> FORME_HOLDER_CODEC = TCodecs.jsonCodec(FormeHolder.class);

    public static final Codec<Pokemob> CODEC = RecordCodecBuilder.create(
            i -> i.group(POKEDEX_ENTRY_CODEC.fieldOf("entry").forGetter(p -> p.entry),
                    FORME_HOLDER_CODEC.optionalFieldOf("holder", null).forGetter(p -> p.holder),
                    Codec.BYTE.fieldOf("gender").forGetter(p -> p.gender)).apply(i, Pokemob::new));

    public final PokedexEntry entry;
    public final FormeHolder holder;

    public final byte gender;

    public Pokemob(final PokedexEntry entry, final FormeHolder holder, final byte gender)
    {
        this.entry = entry;
        this.holder = holder;
        this.gender = gender;
    }

    @Override
    public Class<? extends PokedexEntry> getIngredientClass()
    {
        return PokedexEntry.class;
    }

    public static Collection<Pokemob> getIngredients()
    {
        final List<Pokemob> toAdd = Pokemob.ALL;
        if (!toAdd.isEmpty()) return toAdd;
        for (final PokedexEntry entry : Database.getSortedFormes())
            if (entry != Database.missingno && entry.stock)
            {
                byte gender = entry.isFemaleForme ? IPokemob.FEMALE : IPokemob.MALE;
                Pokemob add = new Pokemob(entry, null, gender);
                Pokemob.ALLMAP.put(entry, add);
                toAdd.add(add);
            }
        return toAdd;
    }

}

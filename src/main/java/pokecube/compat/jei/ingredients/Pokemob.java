package pokecube.compat.jei.ingredients;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

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
        public Pokemob getMatch(final Iterable<Pokemob> ingredients, final Pokemob ingredientToMatch)
        {
            for (final Pokemob mob : ingredients)
                if (ingredientToMatch.entry == mob.entry) return mob;
            return null;
        }

        @Override
        public String getModId(final Pokemob arg0)
        {
            return arg0.entry.getModId();
        }

        @Override
        public String getResourceId(final Pokemob arg0)
        {
            return arg0.entry.getTrimmedName();
        }

        @Override
        public String getUniqueId(final Pokemob arg0)
        {
            return arg0.entry.getTrimmedName();
        }

    }

    public static class IngredientRenderer implements IIngredientRenderer<Pokemob>
    {
        @Override
        public List<ITextComponent> getTooltip(final Pokemob arg0, final ITooltipFlag arg1)
        {
            final List<ITextComponent> list = Lists.newArrayList(new TranslationTextComponent(arg0.entry
                    .getUnlocalizedName()));
            return list;
        }

        @Override
        public void render(final MatrixStack stack, final int x, final int y, final Pokemob pokemob)
        {
            if (pokemob != null) EventsHandlerClient.renderIcon(pokemob.entry, x, y, 16, 16, false);
        }

    }

    public static final IIngredientType<Pokemob> TYPE = () -> Pokemob.class;

    public static final IngredientRenderer RENDER = new IngredientRenderer();
    public static final IngredientHelper   HELPER = new IngredientHelper();

    private static final List<Pokemob> ALL = Lists.newArrayList();

    public static final Map<PokedexEntry, Pokemob> ALLMAP = Maps.newHashMap();

    public final PokedexEntry entry;

    public Pokemob(final PokedexEntry entry)
    {
        this.entry = entry;
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
            if (entry != Database.missingno) toAdd.add(new Pokemob(entry));
        Pokemob.ALL.forEach(p -> Pokemob.ALLMAP.put(p.entry, p));
        return toAdd;
    }

}

package pokecube.compat.jei.ingredients;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector4f;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.FormeHolder;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.database.Database;
import thut.lib.TComponent;

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
        public Pokemob getMatch(final Iterable<Pokemob> ingredients, final Pokemob ingredientToMatch,
                final UidContext context)
        {
            for (final Pokemob mob : ingredients) if (ingredientToMatch.entry == mob.entry) return mob;
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
        public List<Component> getTooltip(final Pokemob pokemob, final TooltipFlag flag)
        {
            final List<Component> list = Lists
                    .newArrayList(TComponent.translatable(pokemob.entry.getUnlocalizedName()));
            if (pokemob.holder != null) list.add(TComponent.literal(pokemob.holder.name));
            return list;
        }

        @Override
        public void render(final PoseStack poseStack, int x, int y, final Pokemob pokemob)
        {
            if (pokemob != null)
            {
                final byte gender = pokemob.gender;
                Vector4f test = new Vector4f(1, 1, 1, 1);
                test.transform(poseStack.last().pose());
                x = (int) test.x();
                y = (int) test.y();
                EventsHandlerClient.renderIcon(pokemob.entry, pokemob.holder, gender == IPokemob.MALE, x, y, 16, 16,
                        false);
            }
        }

    }

    public static final IIngredientType<Pokemob> TYPE = () -> Pokemob.class;

    public static final IngredientRenderer RENDER = new IngredientRenderer();
    public static final IngredientHelper HELPER = new IngredientHelper();

    private static final List<Pokemob> ALL = Lists.newArrayList();

    public static final Map<PokedexEntry, Pokemob> ALLMAP = Maps.newHashMap();
    public static final Map<FormeHolder, Pokemob> FORMMAP = Maps.newHashMap();

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
        for (final PokedexEntry entry : Database.getSortedFormes()) if (entry != Database.missingno && entry.stock)
        {
            byte gender = entry.isFemaleForme ? IPokemob.FEMALE : IPokemob.MALE;
            Pokemob add = new Pokemob(entry, null, gender);
            Pokemob.ALLMAP.put(entry, add);
            toAdd.add(add);
        }
        return toAdd;
    }

}

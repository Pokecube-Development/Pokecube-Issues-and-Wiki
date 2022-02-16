package pokecube.core.client.gui.watch.util;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import pokecube.core.client.gui.helper.ListHelper;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.util.LineEntry.IClickListener;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.spawns.SpawnBiomeMatcher;

public class SpawnListEntry
{
    public static List<MutableComponent> makeDescription(Font fontRender, final SpawnBiomeMatcher matcher,
            final PokedexEntry entry, final int width)
    {
        final List<MutableComponent> output = Lists.newArrayList();
        matcher.reset();
        matcher.parse();

        if (fontRender == null) fontRender = Minecraft.getInstance().font;

        if (matcher.clientBiomes.isEmpty() && matcher.clientTypes.isEmpty())
        {
            // First ensure the client side stuff is cleared.
            matcher.clientBiomes.clear();
            matcher.clientTypes.clear();
            // Then populate it for serialisation
            matcher.parse();

            List<ResourceLocation> biomes = Lists.newArrayList();
            List<String> types = Lists.newArrayList();

            SpawnBiomeMatcher.addForMatcher(biomes, types, matcher);

            matcher.clientBiomes.addAll(biomes);
            matcher.clientTypes.addAll(types);
        }

        if (entry != null)
        {
            final MutableComponent name = entry.getTranslatedName().copy().append(":");
            name.setStyle(name.getStyle().withClickEvent(new ClickEvent(Action.CHANGE_PAGE, entry.getTrimmedName()))
                    .withColor(TextColor.fromLegacyFormat(ChatFormatting.GREEN)));
            output.add(name);
        }

        final List<Component> biomes = Lists.newArrayList();

        for (final ResourceLocation b : matcher.clientBiomes)
            biomes.add(new TranslatableComponent(String.format("biome.%s.%s", b.getNamespace(), b.getPath())));

        final String ind = entry != null ? "  " : "";
        if (!biomes.isEmpty())
        {
            String biomeString = I18n.get("pokewatch.spawns.biomes") + "\n";
            for (final Component s : biomes) biomeString = biomeString + s.getString() + ",\n";
            biomeString = biomeString.substring(0, biomeString.length() - 2) + ".";
            for (final MutableComponent line : ListHelper.splitText(new TextComponent(biomeString),
                    width - fontRender.width(ind), fontRender, true))
                output.add(new TextComponent(ind + line.getString()));
        }

        final List<String> types = Lists.newArrayList();
        if (matcher.clientTypes.size() > 1)
        {
            matcher.clientTypes.remove("all");
        }
        for (final String s : matcher.clientTypes) types.add(I18n.get("thutcore.biometype." + s));
        if (!types.isEmpty())
        {
            String typeString = I18n.get("pokewatch.spawns.types") + " ";
            for (final String s : types) typeString = typeString + s + ", ";
            for (final MutableComponent line : ListHelper.splitText(new TextComponent(typeString),
                    width - fontRender.width(ind), fontRender, false))
                output.add(new TextComponent(ind + line.getString()));
        }
        final boolean day = matcher.day;
        final boolean night = matcher.night;
        final boolean dusk = matcher.dusk;
        final boolean dawn = matcher.dawn;
        final boolean water = matcher.water;
        final boolean air = matcher.air;
        if (water) if (air) for (final MutableComponent line : ListHelper.splitText(
                new TextComponent(ind + I18n.get("pokewatch.spawns.water_optional")), width, fontRender, false))
            output.add(line);
        else for (final MutableComponent line : ListHelper
                .splitText(new TextComponent(ind + I18n.get("pokewatch.spawns.water_only")), width, fontRender, false))
            output.add(line);
        String times = I18n.get("pokewatch.spawns.times");
        if (day) times = times + " " + I18n.get("pokewatch.spawns.day");
        if (night)
        {
            if (day) times = times + ", ";
            times = times + I18n.get("pokewatch.spawns.night");
        }
        if (dusk)
        {
            if (day || night) times = times + ", ";
            times = times + I18n.get("pokewatch.spawns.dusk");
        }
        if (dawn)
        {
            if (day || night || dawn) times = times + ", ";
            times = times + I18n.get("pokewatch.spawns.dawn");
        }
        for (final MutableComponent line : ListHelper.splitText(new TextComponent(times), width - fontRender.width(ind),
                fontRender, false))
            output.add(new TextComponent(ind + line.getString()));
        String rate = "";
        if (matcher.spawnRule.values.containsKey("Local_Rate"))
        {
            float val = 0;
            try
            {
                val = Float.parseFloat(matcher.spawnRule.values.get("Local_Rate"));
            }
            catch (final Exception e)
            {

            }
            if (val > 10e-4) val = (int) (val * 1000) / 10f;
            else if (val != 0)
            {
                float denom = 1000f;
                float numer = 100000f;
                float val2 = (int) (val * numer) / denom;
                if ((val * numer) != 0) while (val2 == 0)
                {
                    numer *= 100;
                    denom *= 100;
                    val2 = (int) (val * numer) / denom;
                }
                val = val2;
            }
            rate = I18n.get("pokewatch.spawns.rate_local", val + "%");
        }
        else
        {
            float val = 0;
            try
            {
                val = Float.parseFloat(matcher.spawnRule.values.get("rate"));
            }
            catch (final Exception e)
            {

            }
            if (val > 10e-4) val = (int) (val * 1000) / 10f;
            else val = (int) (val * 10000) / 100f;

            if (val == 0) rate = "";
            else
            {
                final String var = val + "%";
                rate = ind + I18n.get("pokewatch.spawns.rate_single", var);
            }
        }
        if (!rate.isEmpty()) output.add(new TextComponent(ind + rate));
        output.add(new TextComponent(""));
        return output;
    }

    final int width;
    final int height;
    final int yMin;
    final SpawnBiomeMatcher value;
    final WatchPage parent;
    final Font fontRender;

    public final List<MutableComponent> output = Lists.newArrayList();

    public SpawnListEntry(final WatchPage parent, final Font fontRender, final SpawnBiomeMatcher value,
            final PokedexEntry entry, final int width, final int height, final int yMin)
    {
        this.width = width;
        this.height = height;
        this.yMin = yMin;
        this.value = value;
        this.parent = parent;
        this.fontRender = fontRender;
        output.addAll(makeDescription(fontRender, value, entry, width));
    }

    public List<LineEntry> getLines(final ScrollGui<LineEntry> parent, final IClickListener listener)
    {
        final List<LineEntry> lines = Lists.newArrayList();
        final int textColour = GuiPokeWatch.nightMode ? 0xFFFFFF : 0x333333;
        for (final Component s : this.output)
            lines.add(new LineEntry(parent, 0, 0, this.fontRender, s, textColour).setClickListner(listener));
        return lines;
    }
}

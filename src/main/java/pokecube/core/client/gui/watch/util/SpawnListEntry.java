package pokecube.core.client.gui.watch.util;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.util.LineEntry.IClickListener;
import thut.lib.TComponent;

public class SpawnListEntry
{
    private static List<FormattedCharSequence> makeDescription(Font font, final SpawnBiomeMatcher matcher,
            final PokedexEntry entry, final int width, boolean includeRate)
    {
        final List<FormattedCharSequence> output = Lists.newArrayList();

        if (font == null) font = Minecraft.getInstance().font;

        if (entry != null)
        {
            final MutableComponent name = entry.getTranslatedName().copy().append(":");
            name.setStyle(name.getStyle().withClickEvent(new ClickEvent(Action.CHANGE_PAGE, entry.getTrimmedName()))
                    .withColor(0x22aa22));
            output.add(name.getVisualOrderText());
        }
        final String ind = entry != null ? "  " : "";

        matcher.parse();
        boolean newDesc = matcher.spawnRule.desc != null && !matcher.spawnRule.desc.isBlank();
        if (newDesc)
        {
            output.addAll(font.split(TComponent.translatable(matcher.spawnRule.desc), width - font.width(ind)));
        }
        else
        {
            if (matcher._usesMatchers)
            {
                if (matcher._description != null) output.add(matcher._description.getVisualOrderText());
                return output;
            }
            else
            {
                for (var stuff : matcher.clientStuff)
                {
                    final List<Component> biomes = Lists.newArrayList();
                    for (final ResourceLocation b : stuff.clientBiomes()) biomes
                            .add(TComponent.translatable(String.format("biome.%s.%s", b.getNamespace(), b.getPath())));

                    if (!biomes.isEmpty())
                    {
                        String biomeString = I18n.get("pokewatch.spawns.biomes") + "\n";
                        for (final Component s : biomes) biomeString = biomeString + s.getString() + ",\n";
                        biomeString = biomeString.substring(0, biomeString.length() - 2) + ".";
                        output.addAll(font.split(TComponent.literal(biomeString), width - font.width(ind)));
                    }

                    final List<String> types = Lists.newArrayList();
                    if (stuff.clientTypes().size() > 1)
                    {
                        stuff.clientTypes().remove("all");
                    }
                    for (final String s : stuff.clientTypes()) types.add(I18n.get("thutcore.biometype." + s));
                    if (!types.isEmpty())
                    {
                        String typeString = I18n.get("pokewatch.spawns.types") + "\n";
                        for (final String s : types) typeString = typeString + s + ", ";
                        output.addAll(font.split(TComponent.literal(typeString), width - font.width(ind)));
                    }

                    if (!stuff.clientStructures().isEmpty())
                    {
                        String msg = I18n.get("pokewatch.spawns.structures") + "\n";
                        for (String s : stuff.clientStructures())
                        {
                            msg = msg + I18n.get("spawn.structure." + s.replace(":", ".")) + ", ";
                        }
                        output.addAll(font.split(TComponent.literal(msg), width - font.width(ind)));
                    }
                }

                final boolean day = matcher.day;
                final boolean night = matcher.night;
                final boolean dusk = matcher.dusk;
                final boolean dawn = matcher.dawn;
                final boolean water = matcher.water;
                final boolean air = matcher.air;
                if (water) if (air) output.addAll(
                        font.split(TComponent.literal(ind + I18n.get("pokewatch.spawns.water_optional")), width));
                else output
                        .addAll(font.split(TComponent.literal(ind + I18n.get("pokewatch.spawns.water_only")), width));
                String times = I18n.get("pokewatch.spawns.times");
                int time = 0;
                if (day)
                {
                    times = times + " " + I18n.get("pokewatch.spawns.day");
                    time++;
                }
                if (night)
                {
                    if (time > 0) times = times + ", ";
                    else times = times + " ";
                    time++;
                    times = times + I18n.get("pokewatch.spawns.night");
                }
                if (dusk)
                {
                    if (time > 0) times = times + ", ";
                    else times = times + " ";
                    time++;
                    times = times + I18n.get("pokewatch.spawns.dusk");
                }
                if (dawn)
                {
                    if (time > 0) times = times + ", ";
                    else times = times + " ";
                    time++;
                    times = times + I18n.get("pokewatch.spawns.dawn");
                }
                output.addAll(font.split(TComponent.literal(times), width - font.width(ind)));
            }
        }

        if (includeRate)
        {
            String rate = "";
            if (matcher.spawnRule.values.containsKey("Local_Rate"))
            {
                float val = 0;
                try
                {
                    val = Float.parseFloat(matcher.spawnRule.getString("Local_Rate"));
                }
                catch (final Exception e)
                {

                }
                if (val > 1e-3) val = (int) (val * 1000) / 10f;
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
                    val = Float.parseFloat(matcher.spawnRule.getString("rate"));
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
            if (!rate.isEmpty()) output.add(TComponent.literal(ind + rate).getVisualOrderText());
        }
        output.add(TComponent.literal("").getVisualOrderText());
        return output;
    }

    final int width;
    final int height;
    final int yMin;
    final SpawnBiomeMatcher value;
    final WatchPage parent;
    final Font fontRender;
    final PokedexEntry entry;
    boolean includeRate = true;

    public SpawnListEntry(final WatchPage parent, final Font fontRender, final SpawnBiomeMatcher value,
            final PokedexEntry entry, final int width, final int height, final int yMin)
    {
        this.width = width;
        this.height = height;
        this.yMin = yMin;
        this.value = value;
        this.parent = parent;
        this.fontRender = fontRender;
        this.entry = entry;
    }

    public SpawnListEntry noRate()
    {
        includeRate = false;
        return this;
    }

    public List<LineEntry> getLines(final ScrollGui<LineEntry> parent, final IClickListener listener)
    {
        return getLines(parent, listener, -1);
    }

    public List<LineEntry> getLines(final ScrollGui<LineEntry> parent, final IClickListener listener,
            int overrideColour)
    {
        final List<LineEntry> lines = Lists.newArrayList();
        int textColour = overrideColour > 0 ? overrideColour : 0x333333;
        for (var s : makeDescription(fontRender, value, entry, width, includeRate))
            lines.add(new LineEntry(parent, 0, 0, this.fontRender, s, textColour).setClickListner(listener));
        return lines;
    }
}

package pokecube.core.client.gui.watch.util;

import java.util.List;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import pokecube.core.client.gui.helper.ListHelper;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.util.LineEntry.IClickListener;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.spawns.SpawnBiomeMatcher;

public class SpawnListEntry
{
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

        value.reset();
        value.parse();

        final List<Component> biomes = Lists.newArrayList();
        for (final ResourceKey<Biome> b : value.clientBiomes) biomes.add(new TranslatableComponent(
                String.format("biome.%s.%s", b.location().getNamespace(), b.location().getPath())));

        if (entry != null)
        {
            final MutableComponent name = entry.getTranslatedName().copy().append(":");
            name.setStyle(name.getStyle().withClickEvent(new ClickEvent(Action.CHANGE_PAGE, entry.getTrimmedName()))
                    .withColor(TextColor.fromLegacyFormat(ChatFormatting.GREEN)));
            this.output.add(name);
        }
        final String ind = entry != null ? "  " : "";
        if (!biomes.isEmpty())
        {
            String biomeString = I18n.get("pokewatch.spawns.biomes") + "\n";
            for (final Component s : biomes) biomeString = biomeString + s.getString() + ",\n";
            biomeString = biomeString.substring(0, biomeString.length() - 2) + ".";
            for (final MutableComponent line : ListHelper.splitText(new TextComponent(biomeString),
                    width - fontRender.width(ind), fontRender, true))
                this.output.add(new TextComponent(ind + line.getString()));
        }

        final List<String> types = Lists.newArrayList();
        if (value.clientTypes != null)
            for (final String s : value.clientTypes) types.add(I18n.get("thutcore.biometype." + s));
        if (!types.isEmpty())
        {
            String typeString = I18n.get("pokewatch.spawns.types") + " ";
            for (final String s : types) typeString = typeString + s + ", ";
            for (final MutableComponent line : ListHelper.splitText(new TextComponent(typeString),
                    width - fontRender.width(ind), fontRender, false))
                this.output.add(new TextComponent(ind + line.getString()));
        }
        final boolean day = value.day;
        final boolean night = value.night;
        final boolean dusk = value.dusk;
        final boolean dawn = value.dawn;
        final boolean water = value.water;
        final boolean air = value.air;
        if (water) if (air) for (final MutableComponent line : ListHelper.splitText(
                new TextComponent(ind + I18n.get("pokewatch.spawns.water_optional")), width, fontRender, false))
            this.output.add(line);
        else for (final MutableComponent line : ListHelper
                .splitText(new TextComponent(ind + I18n.get("pokewatch.spawns.water_only")), width, fontRender, false))
            this.output.add(line);
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
            this.output.add(new TextComponent(ind + line.getString()));
        String rate = "";
        if (value.spawnRule.values.containsKey(new QName("Local_Rate")))
        {
            float val = 0;
            try
            {
                val = Float.parseFloat(value.spawnRule.values.get(new QName("Local_Rate")));
            }
            catch (final Exception e)
            {

            }
            System.out.println(val);

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
                val = Float.parseFloat(value.spawnRule.values.get(new QName("rate")));
            }
            catch (final Exception e)
            {

            }
            if (val > 10e-4) val = (int) (val * 1000) / 10f;
            else val = (int) (val * 10000) / 100f;

            final String var = val + "%";
            rate = ind + I18n.get("pokewatch.spawns.rate_single", var);
        }
        if (!rate.isEmpty()) this.output.add(new TextComponent(ind + rate));
        this.output.add(new TextComponent(""));
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

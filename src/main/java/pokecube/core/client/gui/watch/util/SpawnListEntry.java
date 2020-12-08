package pokecube.core.client.gui.watch.util;

import java.util.List;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import net.minecraft.world.biome.Biome;
import pokecube.core.client.gui.helper.ListHelper;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.util.LineEntry.IClickListener;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.SpawnBiomeMatcher;
import thut.api.terrain.BiomeType;

public class SpawnListEntry
{
    final int               width;
    final int               height;
    final int               yMin;
    final SpawnBiomeMatcher value;
    final WatchPage         parent;
    final FontRenderer      fontRender;

    public final List<IFormattableTextComponent> output = Lists.newArrayList();

    public SpawnListEntry(final WatchPage parent, final FontRenderer fontRender, final SpawnBiomeMatcher value,
            final PokedexEntry entry, final int width, final int height, final int yMin)
    {
        this.width = width;
        this.height = height;
        this.yMin = yMin;
        this.value = value;
        this.parent = parent;
        this.fontRender = fontRender;
        value._additionalConditions = Sets.newHashSet();
        value._blackListSubBiomes = Sets.newHashSet();
        value._validSubBiomes = Sets.newHashSet();
        value.reset();
        value.parse();

        final List<ITextComponent> biomes = Lists.newArrayList();
        for (final RegistryKey<Biome> b : value.getValidBiomes())
            biomes.add(new TranslationTextComponent(b.getLocation().getPath()));

        if (entry != null)
        {
            final IFormattableTextComponent name = new StringTextComponent(entry.getTranslatedName() + ":");
            name.setStyle(name.getStyle().setClickEvent(new ClickEvent(Action.CHANGE_PAGE, entry.getTrimmedName()))
                    .setColor(Color.fromTextFormatting(TextFormatting.GREEN)));
            this.output.add(name);
        }
        final String ind = entry != null ? "  " : "";
        if (!biomes.isEmpty())
        {
            String biomeString = I18n.format("pokewatch.spawns.biomes") + "\n";
            for (final ITextComponent s : biomes)
                biomeString = biomeString + s.getString() + ",\n";
            biomeString = biomeString.substring(0, biomeString.length() - 2) + ".";
            for (final IFormattableTextComponent line : ListHelper.splitText(new StringTextComponent(biomeString), width
                    - fontRender.getStringWidth(ind), fontRender, true))
                this.output.add(new StringTextComponent(ind + line.getString()));
        }

        final List<String> types = Lists.newArrayList();
        if (value._validSubBiomes != null) for (final BiomeType t : value._validSubBiomes)
            types.add(I18n.format(t.readableName));
        if (!types.isEmpty())
        {
            String typeString = I18n.format("pokewatch.spawns.types") + " ";
            for (final String s : types)
                typeString = typeString + s + ", ";
            for (final IFormattableTextComponent line : ListHelper.splitText(new StringTextComponent(typeString), width
                    - fontRender.getStringWidth(ind), fontRender, false))
                this.output.add(new StringTextComponent(ind + line.getString()));
        }
        final boolean day = value.day;
        final boolean night = value.night;
        final boolean dusk = value.dusk;
        final boolean dawn = value.dawn;
        final boolean water = value.water;
        final boolean air = value.air;
        if (water) if (air) for (final IFormattableTextComponent line : ListHelper.splitText(new StringTextComponent(ind
                + I18n.format("pokewatch.spawns.water_optional")), width, fontRender, false))
            this.output.add(line);
        else for (final IFormattableTextComponent line : ListHelper.splitText(new StringTextComponent(ind + I18n.format(
                "pokewatch.spawns.water_only")), width, fontRender, false))
            this.output.add(line);
        String times = I18n.format("pokewatch.spawns.times");
        if (day) times = times + " " + I18n.format("pokewatch.spawns.day");
        if (night)
        {
            if (day) times = times + ", ";
            times = times + I18n.format("pokewatch.spawns.night");
        }
        if (dusk)
        {
            if (day || night) times = times + ", ";
            times = times + I18n.format("pokewatch.spawns.dusk");
        }
        if (dawn)
        {
            if (day || night || dawn) times = times + ", ";
            times = times + I18n.format("pokewatch.spawns.dawn");
        }
        for (final IFormattableTextComponent line : ListHelper.splitText(new StringTextComponent(times), width
                - fontRender.getStringWidth(ind), fontRender, false))
            this.output.add(new StringTextComponent(ind + line.getString()));
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

            if (val > 10e-4) val = (int) (val * 1000) / 10f;
            else if (val != 0)
            {
                float denom = 1000f;
                float numer = 100000f;
                float val2 = (int) (val * numer) / denom;
                while (val2 == 0)
                {
                    numer *= 100;
                    denom *= 100;
                    val2 = (int) (val * numer) / denom;
                }
                val = val2;
            }
            rate = I18n.format("pokewatch.spawns.rate_local", val + "%");
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
            rate = ind + I18n.format("pokewatch.spawns.rate_single", var);
        }
        if (!rate.isEmpty()) this.output.add(new StringTextComponent(ind + rate));
        this.output.add(new StringTextComponent(""));
    }

    public List<LineEntry> getLines(final ScrollGui<LineEntry> parent, final IClickListener listener)
    {
        final List<LineEntry> lines = Lists.newArrayList();
        final int textColour = GuiPokeWatch.nightMode ? 0xFFFFFF : 0x333333;
        for (final ITextComponent s : this.output)
            lines.add(new LineEntry(parent, 0, 0, this.fontRender, s, textColour).setClickListner(listener));
        return lines;
    }
}

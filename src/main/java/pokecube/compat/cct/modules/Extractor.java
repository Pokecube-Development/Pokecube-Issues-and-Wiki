package pokecube.compat.cct.modules;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import dan200.computercraft.api.lua.ArgumentHelper;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import pokecube.adventures.blocks.genetics.extractor.ExtractorTile;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector.SelectorValue;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.IMobGenetics;

public class Extractor extends BasePeripheral<ExtractorTile>
{
    IItemHandlerModifiable inventory = null;

    public Extractor(final ExtractorTile tile)
    {
        super(tile, "dnaExtractor", "source_info", "selector_info", "set_selector");
        this.inventory = (IItemHandlerModifiable) tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .orElse(null);
    }

    @Override
    public Object[] callMethod(final IComputerAccess computer, final ILuaContext context, final int method,
            final Object[] arguments) throws LuaException, InterruptedException
    {
        final List<String> values = Lists.newArrayList();
        ItemStack selector = this.inventory.getStackInSlot(1);
        if (selector.isEmpty()) selector = this.tile.override_selector;
        SelectorValue value = ClonerHelper.getSelectorValue(selector);
        final ItemStack source = this.inventory.getStackInSlot(2);
        final Set<Class<? extends Gene>> getSelectors = ClonerHelper.getGeneSelectors(selector);
        switch (method)
        {
        case 0:
            final IMobGenetics genes = ClonerHelper.getGenes(source);
            if (genes == null) throw new LuaException("No Genes found in source slot.");
            for (final ResourceLocation l : genes.getAlleles().keySet())
            {
                final Alleles a = genes.getAlleles().get(l);
                final Gene expressed = a.getExpressed();
                final Gene parent1 = a.getAlleles()[0];
                final Gene parent2 = a.getAlleles()[1];
                values.add(l.getNamespace());
                values.add(expressed.toString());
                values.add(parent1.toString());
                values.add(parent2.toString());
            }
            return values.toArray();
        case 1:
            if (getSelectors.isEmpty()) throw new LuaException("No Selector found.");
            for (final Class<? extends Gene> geneC : getSelectors)
                try
                {
                    final Gene gene = geneC.newInstance();
                    values.add(gene.getKey().getNamespace());
                }
                catch (InstantiationException | IllegalAccessException e)
                {
                }
            values.add(value.toString());
            return values.toArray();
        case 2:
            if (!getSelectors.isEmpty()) throw new LuaException(
                    "Cannot set custom selector when a valid one is in the slot.");
            for (int i = 0; i < arguments.length; i++)
                values.add(ArgumentHelper.getString(arguments, i));
            if (values.isEmpty()) throw new LuaException("You need to specify some genes");
            final ItemStack newSelector = new ItemStack(Items.WRITTEN_BOOK);
            newSelector.setTag(new CompoundNBT());
            final ListNBT pages = new ListNBT();
            for (final String s : values)
                pages.add(StringNBT.valueOf(String.format("{\"text\":\"%s\"}", s)));
            newSelector.getTag().put("pages", pages);
            value = RecipeSelector.getSelectorValue(this.tile.getStackInSlot(1));
            newSelector.getTag().put(ClonerHelper.SELECTORTAG, value.save());
            newSelector.setDisplayName(new StringTextComponent("Selector"));
            this.tile.override_selector = newSelector;
            return new Object[] { true };
        }
        return null;
    }

}

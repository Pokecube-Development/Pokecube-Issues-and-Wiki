package pokecube.compat.cct.modules;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector.SelectorValue;
import pokecube.adventures.blocks.genetics.splicer.SplicerTile;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.IMobGenetics;

public class Splicer extends BasePeripheral<SplicerTile>
{
    public static class Provider
    {
        private final SplicerTile            tile;
        private final IItemHandlerModifiable inventory;

        public Provider(final SplicerTile tile)
        {
            this.tile = tile;
            this.inventory = (IItemHandlerModifiable) tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                    .orElse(null);
        }

        public String[] getSourceInfo() throws LuaException
        {
            final List<String> values = Lists.newArrayList();
            final ItemStack source = this.inventory.getStackInSlot(2);
            final IMobGenetics genes = ClonerHelper.getGenes(source);
            if (genes == null) throw new LuaException("No Genes found in source slot.");
            for (final ResourceLocation l : genes.getAlleles().keySet())
            {
                final Alleles<?, ?> a = genes.getAlleles().get(l);
                final Gene<?> expressed = a.getExpressed();
                final Gene<?> parent1 = a.getAllele(0);
                final Gene<?> parent2 = a.getAllele(1);
                values.add(l.getNamespace());
                values.add(expressed.toString());
                values.add(parent1.toString());
                values.add(parent2.toString());
            }
            return values.toArray(new String[0]);
        }

        public String[] getDestinationInfo() throws LuaException
        {
            final List<String> values = Lists.newArrayList();
            final ItemStack dest = this.inventory.getStackInSlot(0);
            final IMobGenetics genes = ClonerHelper.getGenes(dest);
            if (genes == null) throw new LuaException("No Genes found in destination slot.");
            for (final ResourceLocation l : genes.getAlleles().keySet())
            {
                final Alleles<?, ?> a = genes.getAlleles().get(l);
                final Gene<?> expressed = a.getExpressed();
                final Gene<?> parent1 = a.getAllele(0);
                final Gene<?> parent2 = a.getAllele(1);
                values.add(l.getNamespace());
                values.add(expressed.toString());
                values.add(parent1.toString());
                values.add(parent2.toString());
            }
            return values.toArray(new String[0]);
        }

        public String[] getSelectorInfo() throws LuaException
        {
            ItemStack selector = this.inventory.getStackInSlot(1);
            if (selector.isEmpty()) selector = this.tile.override_selector;
            final List<String> values = Lists.newArrayList();
            final SelectorValue value = ClonerHelper.getSelectorValue(selector);
            final Set<Class<? extends Gene<?>>> getSelectors = ClonerHelper.getGeneSelectors(selector);
            if (getSelectors.isEmpty()) throw new LuaException("No Selector found.");
            for (final Class<? extends Gene<?>> geneC : getSelectors)
                try
                {
                    final Gene<?> gene = geneC.newInstance();
                    values.add(gene.getKey().getNamespace());
                }
                catch (InstantiationException | IllegalAccessException e)
                {
                }
            values.add(value.toString());
            return values.toArray(new String[0]);
        }

        public boolean setSelector(final String[] args) throws LuaException
        {
            ItemStack selector = this.inventory.getStackInSlot(1);
            if (selector.isEmpty()) selector = this.tile.override_selector;
            final List<String> values = Lists.newArrayList();
            SelectorValue value = ClonerHelper.getSelectorValue(selector);
            final Set<Class<? extends Gene<?>>> getSelectors = ClonerHelper.getGeneSelectors(selector);
            if (!getSelectors.isEmpty()) throw new LuaException(
                    "Cannot set custom selector when a valid one is in the slot.");
            for (final String s : args)
                values.add(s);
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
            return true;
        }
    }

    private final Provider provider;

    public Splicer(final SplicerTile tile)
    {
        super(tile, "dnaSplicer");
        this.provider = new Provider(tile);
    }

    @Override
    public Object getTarget()
    {
        return this.provider;
    }
}

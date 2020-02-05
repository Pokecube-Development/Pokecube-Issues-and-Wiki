package pokecube.core.inventory;

import com.mojang.datafixers.util.Pair;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TexturedSlot extends Slot
{
    final ResourceLocation texture;

    public TexturedSlot(final IInventory inventoryIn, final int index, final int xPosition, final int yPosition,
            final String texture)
    {
        super(inventoryIn, index, xPosition, yPosition);
        this.texture = new ResourceLocation(texture);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Pair<ResourceLocation, ResourceLocation> func_225517_c_()
    {
        return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, this.texture);
    }

    @Override
    public boolean isItemValid(final ItemStack stack)
    {
        return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
    }
}

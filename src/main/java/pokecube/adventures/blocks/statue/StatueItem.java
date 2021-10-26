package pokecube.adventures.blocks.statue;

import java.util.function.Consumer;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.IItemRenderProperties;

public class StatueItem extends BlockItem
{

    public StatueItem(final Block block, final Properties props)
    {
        super(block, props);
    }


    @Override
    public void initializeClient(final Consumer<IItemRenderProperties> consumer) {
        consumer.accept(new IItemRenderProperties() {

            private final BlockEntityWithoutLevelRenderer renderer = new pokecube.adventures.client.render.StatueItem();

            @Override
            public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
                return this.renderer;
            }
        });
    }
}

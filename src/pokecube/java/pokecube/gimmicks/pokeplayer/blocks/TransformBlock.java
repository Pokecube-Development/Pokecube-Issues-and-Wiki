package pokecube.gimmicks.pokeplayer.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeItems;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.gimmicks.pokeplayer.Pokeplayer;
import thut.api.ThutCaps;
import thut.wearables.inventory.PlayerWearables;

import java.util.List;
import java.util.Set;

public class TransformBlock extends Block {

    public TransformBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        if (!(player instanceof ServerPlayer)) return ItemInteractionResult.SUCCESS;

        var copy = ThutCaps.getCopyMob(player);
        boolean notTransformed = copy == null || copy.getCopiedID() == null;
        boolean isFilled = PokemobCaps.isFilled(stack);

        // Not transformed, and holding a filled cube, transform the player
        if (notTransformed && isFilled) {
            var pokemob = PokemobCaps.getPokemobIn(stack, level).pokemob();

            if (pokemob != null)
            {
                ItemStack heldA = pokemob.getHeldItem();
                ItemStack heldB = pokemob.getEntity().getOffhandItem();
                Set<ItemStack> wearables = pokemob.getEntity().getData(PlayerWearables.TYPE).getWearables();
                pokemob.setHeldItem(ItemStack.EMPTY);
                pokemob.setPokecube(stack);
                pokemob.getEntity().setData(PlayerWearables.TYPE, new PlayerWearables(pokemob.getEntity()));
                pokemob.getEntity().setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                int result = Pokeplayer.transformPlayer(pokemob, player);
                if(result < 0) return ItemInteractionResult.FAIL;
                ItemHandlerHelper.giveItemToPlayer(player, heldA); // Transfer pokemob held and offhand items to player
                ItemHandlerHelper.giveItemToPlayer(player, heldB);
                wearables.forEach(item -> ItemHandlerHelper.giveItemToPlayer(player, item)); // Transfer wearables to player
                player.setItemInHand(hand, ItemStack.EMPTY);
                return ItemInteractionResult.CONSUME;
            }
        }
        // If transformed, revert the player
        if (!notTransformed)
        {
            int result = Pokeplayer.transformPlayer(null, player);
            if (result < 0) return ItemInteractionResult.FAIL;
        }
        return ItemInteractionResult.SUCCESS;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag tooltipFlag)
    {
        tooltipComponents.clear();
        tooltipComponents.add(Component.translatable("block.pokecube.transform_block"));
        tooltipComponents.add(Component.translatable("block.pokecube.transform_block.desc"));
    }
}

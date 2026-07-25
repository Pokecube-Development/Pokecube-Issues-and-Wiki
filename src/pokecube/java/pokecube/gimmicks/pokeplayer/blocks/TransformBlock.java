package pokecube.gimmicks.pokeplayer.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeItems;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.gimmicks.pokeplayer.Pokeplayer;
import thut.api.ThutCaps;

public class TransformBlock extends Block {

    public TransformBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        if(!(player instanceof ServerPlayer)) return ItemInteractionResult.SUCCESS;
        var copy = ThutCaps.getCopyMob(player);
        boolean notTransformed = copy == null || copy.getCopiedID() == null;
        boolean isFilled = PokemobCaps.isFilled(stack);
        // Not transformed, and holding a filled cube, transform the player
        if(notTransformed && isFilled) {
            var pokemob = PokemobCaps.getPokemobIn(stack, level).pokemob();
            if(pokemob != null) pokemob.setPokecube(stack);
            Pokeplayer.transformPlayer(pokemob, player);
            player.setItemInHand(hand, ItemStack.EMPTY);
            return ItemInteractionResult.CONSUME;
        }
        // If transformed, revert the player
        if(!notTransformed) {
            var mob = copy.getCopiedMob();
            var pokemob = PokemobCaps.getPokemobFor(mob);
            var cube = new ItemStack(PokecubeItems.getEmptyCube(ResourceLocation.parse("pokecube:pokecube")));
            if(pokemob != null && !pokemob.getPokecube().isEmpty()) cube = pokemob.getPokecube();
            PokecubeManager.addToCube(cube, mob);
            if(!player.addItem(cube));// Should drop in here instead.
            Pokeplayer.transformPlayer(null, player);
        }
        return ItemInteractionResult.SUCCESS;
    }
}

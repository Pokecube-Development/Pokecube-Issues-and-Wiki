package pokecube.core.blocks.pokeplayer;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.blocks.InteractableTile;
import pokecube.gimmicks.pokeplayer.Pokeplayer;
import thut.api.ThutCaps;

public class TransformPRContainer extends InteractableTile implements ContainerListener {

    public TransformPRContainer(final BlockPos pos, final BlockState state)
    {
        super(BlockEntityType.BED, pos, state);

    }

    @Override
    public void containerChanged(Container container) {

    }

    @Override
    public void onWalkedOn(final Entity entityIn)
    {
        //Where the magic happens.
        if (entityIn instanceof Player)
        {
            Player player = (Player)entityIn;

            //Player is transformed, revert them
            if (ThutCaps.getCopyMob(player) != null && ThutCaps.getCopyMob(player).getCopiedID() != null)
            {
                try {
                    Pokeplayer.doPokeplayerCommand("none", player);
                }
                catch (CommandSyntaxException c) {
                    player.sendSystemMessage(Component.literal("CommandSyntaxException has been thrown. Player cannot be reverted."));
                }
            }
            //Player is not transformed, transform them
            else
            {
                ItemStack handItem = player.getMainHandItem();
                if (PokemobCaps.isFilled(handItem))
                {
                    try {
                        Pokeplayer.doPokeplayerCommand(PokemobCaps.getPokemobIn(handItem).pokemob().getDisplayName().getString(), entityIn);
                    }
                    catch (CommandSyntaxException c) {
                        player.sendSystemMessage(Component.literal("CommandSyntaxException has been thrown. Player cannot be transformed."));
                    }
                }
                else
                    player.sendSystemMessage(Component.literal("Transform cannot happen as player is not holding a filled pokecube."));
            }
        }
    }
}

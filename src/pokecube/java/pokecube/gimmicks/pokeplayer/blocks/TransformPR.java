package pokecube.gimmicks.pokeplayer.blocks;


import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.gimmicks.pokeplayer.Pokeplayer;
import thut.api.ThutCaps;

public class TransformPR extends BedBlock {

    public TransformPR(Properties properties) {
        super(DyeColor.PURPLE, properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity)
    {
        super.stepOn(level, pos, state, entity);
        //Where the magic happens.
        if (entity instanceof Player player)
        {
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
                    Pokeplayer.transformPlayer(PokemobCaps.getPokemobIn(handItem).pokemob(), player);
                }
                else
                    player.sendSystemMessage(Component.literal("Transform cannot happen as player is not holding a filled pokecube."));
            }
        }
    }
}

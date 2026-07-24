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
import net.minecraft.world.level.block.state.BlockState;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.gimmicks.pokeplayer.Pokeplayer;
import thut.api.ThutCaps;
import thut.api.Tracker;

public class TransformPR extends BedBlock {

    private final long transformWait = 30; // Number of ticks between two transforms

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
            long currentTick = Tracker.instance().getTick();
            long lastStep = player.getPersistentData().getLong("pokeplayer:last_transform_block_use");

            if (currentTick - lastStep > transformWait) {
                //Player is transformed
                if (ThutCaps.getCopyMob(player) != null && ThutCaps.getCopyMob(player).getCopiedID() != null) {
                    ItemStack handItem = player.getMainHandItem();
                    if (PokemobCaps.isFilled(handItem)) // Transform into different pokemob (re-rolls move as well)
                        Pokeplayer.transformPlayer(PokemobCaps.getPokemobIn(handItem).pokemob(), player);
                    else // Revert back into player
                    {
                        try {
                            Pokeplayer.doPokeplayerCommand("none", player);
                        } catch (CommandSyntaxException c) {
                            player.sendSystemMessage(Component.literal("CommandSyntaxException has been thrown. Player cannot be reverted."));
                        }
                    }
                }
                //Player is not transformed, transform them
                else {
                    ItemStack handItem = player.getMainHandItem();
                    if (PokemobCaps.isFilled(handItem))
                        Pokeplayer.transformPlayer(PokemobCaps.getPokemobIn(handItem).pokemob(), player);
                    else // Check if player is transformed and revert them if true.
                    {
                        if (ThutCaps.getCopyMob(player) != null && ThutCaps.getCopyMob(player).getCopiedID() != null) // Player is transformed
                        {
                            try {
                                Pokeplayer.doPokeplayerCommand("none", player);
                            } catch (CommandSyntaxException c) {
                                player.sendSystemMessage(Component.literal("CommandSyntaxException has been thrown. Player cannot be reverted."));
                            }
                        } else // Player is not transformed and not holding a pokecube.
                            player.sendSystemMessage(Component.literal("Transform cannot happen as player is not holding a filled pokecube."));
                    }
                }
            }
            player.getPersistentData().putLong("pokeplayer:last_transform_block_use", currentTick);
        }
    }
}

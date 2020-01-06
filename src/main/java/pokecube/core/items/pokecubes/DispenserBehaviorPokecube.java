package pokecube.core.items.pokecubes;

import net.minecraft.block.BlockState;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.IProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.FakePlayer;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;

public class DispenserBehaviorPokecube implements IDispenseItemBehavior
{

    @Override
    public ItemStack dispense(IBlockSource source, ItemStack stack)
    {
        Direction dir = null;
        final BlockState state = source.getBlockState();
        for (final IProperty<?> prop : state.getProperties())
            if (prop.getValueClass() == Direction.class)
            {
                dir = (Direction) state.get(prop);
                break;
            }
        if (dir == null) return stack;

        final FakePlayer player = PokecubeMod.getFakePlayer(source.getWorld());
        player.posX = source.getX();
        player.posY = source.getY() - player.getEyeHeight();
        player.posZ = source.getZ();

        // Defaults are for south.
        player.rotationPitch = 0;
        player.rotationYaw = 0;

        if (dir == Direction.EAST) player.rotationYaw = -90;
        else if (dir == Direction.WEST) player.rotationYaw = 90;
        else if (dir == Direction.NORTH) player.rotationYaw = 180;
        else if (dir == Direction.UP) player.rotationPitch = -90;
        else if (dir == Direction.DOWN) player.rotationPitch = 90;

        if (PokecubeItems.is(PokecubeItems.POKEMOBEGG, stack))
        {
            player.setHeldItem(Hand.MAIN_HAND, stack);
            final BlockRayTraceResult result = new BlockRayTraceResult(new Vec3d(0.5, 0.5, 0.5), Direction.UP, source
                    .getBlockPos().offset(dir), false);
            final ItemUseContext context = new ItemUseContext(player, Hand.MAIN_HAND, result);
            stack.onItemUse(context);
            player.inventory.clear();
        }
        else if (stack.getItem() instanceof IPokecube)
        {
            final IPokecube cube = (IPokecube) stack.getItem();
            final Vector3 direction = Vector3.getNewVector().set(dir);
            if (cube.throwPokecube(source.getWorld(), player, stack, direction, 0.25f)) stack.split(1);
        }
        return stack;
    }

}
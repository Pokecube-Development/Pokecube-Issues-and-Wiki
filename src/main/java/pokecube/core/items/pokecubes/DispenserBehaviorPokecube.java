package pokecube.core.items.pokecubes;

import net.minecraft.block.BlockState;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.Property;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.util.FakePlayer;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;

public class DispenserBehaviorPokecube implements IDispenseItemBehavior
{

    @Override
    public ItemStack dispense(final IBlockSource source, final ItemStack stack)
    {
        Direction dir = null;
        final BlockState state = source.getBlockState();
        for (final Property<?> prop : state.getProperties())
            if (prop.getValueClass() == Direction.class)
            {
                dir = (Direction) state.getValue(prop);
                break;
            }
        if (dir == null) return stack;

        final FakePlayer player = PokecubeMod.getFakePlayer(source.getLevel());
        player.setPos(source.x(), source.y(), source.z());

        // Defaults are for south.
        player.xRot = 0;
        player.yRot = 0;

        if (dir == Direction.EAST) player.yRot = -90;
        else if (dir == Direction.WEST) player.yRot = 90;
        else if (dir == Direction.NORTH) player.yRot = 180;
        else if (dir == Direction.UP) player.xRot = -90;
        else if (dir == Direction.DOWN) player.xRot = 90;

        if (ItemList.is(PokecubeItems.POKEMOBEGG, stack))
        {
            player.setItemInHand(Hand.MAIN_HAND, stack);
            final BlockRayTraceResult result = new BlockRayTraceResult(new Vector3d(0.5, 0.5, 0.5), Direction.UP, source
                    .getPos().relative(dir), false);
            final ItemUseContext context = new ItemUseContext(player, Hand.MAIN_HAND, result);
            stack.useOn(context);
            player.inventory.clearContent();
        }
        else if (stack.getItem() instanceof IPokecube)
        {
            final IPokecube cube = (IPokecube) stack.getItem();
            final Vector3 direction = Vector3.getNewVector().set(dir);
            final EntityPokecubeBase pokecube = cube.throwPokecube(source.getLevel(), player, stack, direction, 0.25f);
            if (pokecube != null)
            {
                stack.split(1);
                final Vector3 v = Vector3.getNewVector().set(source.x(), source.y(), source.z());
                v.addTo(direction);
                pokecube.setPos(v.x, v.y, v.z);
            }
        }
        return stack;
    }

}
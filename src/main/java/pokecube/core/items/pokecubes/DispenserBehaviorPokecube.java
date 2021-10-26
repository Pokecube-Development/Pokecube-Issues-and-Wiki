package pokecube.core.items.pokecubes;

import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;

public class DispenserBehaviorPokecube implements DispenseItemBehavior
{

    @Override
    public ItemStack dispense(final BlockSource source, final ItemStack stack)
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
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            final BlockHitResult result = new BlockHitResult(new Vec3(0.5, 0.5, 0.5), Direction.UP, source
                    .getPos().relative(dir), false);
            final UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND, result);
            stack.useOn(context);
            player.getInventory().clearContent();
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
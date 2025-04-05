package pokecube.core.items.pokecubes;

import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import pokecube.api.items.IPokecube;
import pokecube.core.PokecubeItems;
import pokecube.core.entity.pokecubes.EntityPokecubeBase;
import pokecube.core.impl.PokecubeMod;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;

public class DispenserBehaviorPokecube implements DispenseItemBehavior
{

    @Override
    public ItemStack dispense(final BlockSource source, final ItemStack stack)
    {
        Direction dir = null;
        final BlockState state = source.state();
        for (final Property<?> prop : state.getProperties()) if (prop.getValueClass() == Direction.class)
        {
            dir = (Direction) state.getValue(prop);
            break;
        }
        if (dir == null) return stack;

        final FakePlayer player = PokecubeMod.getFakePlayer(source.level());
        player.setPos(source.center());

        // Defaults are for south.
        float xRot = 0;
        float yRot = 0;

        if (dir == Direction.EAST) yRot = -90;
        else if (dir == Direction.WEST) yRot = 90;
        else if (dir == Direction.NORTH) yRot = 180;
        else if (dir == Direction.UP) xRot = -90;
        else if (dir == Direction.DOWN) xRot = 90;

        player.setXRot(xRot);
        player.setYRot(yRot);

        if (ItemList.is(PokecubeItems.POKEMOBEGG, stack))
        {
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            final BlockHitResult result = new BlockHitResult(new Vec3(0.5, 0.5, 0.5), Direction.UP,
                    source.pos().relative(dir), false);
            final UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND, result);
            stack.useOn(context);
            player.getInventory().clearContent();
        }
        else if (stack.getItem() instanceof IPokecube cube)
        {
            final Vector3 direction = new Vector3().set(dir);
            final EntityPokecubeBase pokecube = cube.throwPokecube(source.level(), player, stack, direction, 0.25f);
            if (pokecube != null)
            {
                stack.split(1);
                final Vector3 v = new Vector3().set(source.center());
                v.addTo(direction);
                pokecube.setPos(v.x, v.y, v.z);
            }
        }
        return stack;
    }

}
package pokecube.core.entity.pokemobs;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IProperty;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraftforge.common.util.FakePlayer;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;

public class DispenseBehaviourInteract implements IDispenseItemBehavior
{
    private static final ResourceLocation     DEFAULT     = new ResourceLocation("none");
    public static final Set<ResourceLocation> KNOWNSTACKS = Sets.newHashSet();

    // We make our own to try to ensure that any other added behaviour is kept.
    // Hopefully they registered their first, we do this in post init, so should
    // be.
    public static final DefaultedRegistry<IDispenseItemBehavior> DISPENSE_BEHAVIOR_REGISTRY = new DefaultedRegistry<>(
            DispenseBehaviourInteract.DEFAULT.toString());

    static
    {
        DispenseBehaviourInteract.DISPENSE_BEHAVIOR_REGISTRY.register(DispenseBehaviourInteract.DEFAULT,
                new DefaultDispenseItemBehavior());
    }

    public static void registerBehavior(ItemStack stack)
    {
        if (stack.isEmpty() || !DispenseBehaviourInteract.KNOWNSTACKS.add(stack.getItem().getRegistryName())) return;
        // TODO get any default behaviors incase they are not the default...
        // DISPENSE_BEHAVIOR_REGISTRY.putObject(stack.getItem().getRegistryName(),
        // DispenserBlock.DISPENSE_BEHAVIOR_REGISTRY.getObject(stack.getItem()));
        DispenserBlock.registerDispenseBehavior(() -> stack.getItem(), new DispenseBehaviourInteract());
    }

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
        if (dir == null) return DispenseBehaviourInteract.DISPENSE_BEHAVIOR_REGISTRY.getOrDefault(stack.getItem()
                .getRegistryName()).dispense(source, stack);
        final FakePlayer player = PokecubeMod.getFakePlayer(source.getWorld());
        player.posX = source.getX();
        player.posY = source.getY() - player.getEyeHeight();
        player.posZ = source.getZ();

        final Vector3 loc = Vector3.getNewVector().set(source.getBlockPos().offset(dir));
        final AxisAlignedBB box = loc.getAABB().grow(2);
        final List<MobEntity> mobs = source.getWorld().getEntitiesWithinAABB(MobEntity.class, box);
        Collections.shuffle(mobs);
        if (!mobs.isEmpty())
        {
            player.inventory.clear();
            player.setHeldItem(Hand.MAIN_HAND, stack);

            ActionResultType cancelResult = net.minecraftforge.common.ForgeHooks.onInteractEntityAt(player, mobs.get(0),
                    new Vec3d(0, 0, 0), Hand.MAIN_HAND);
            if (cancelResult == null) cancelResult = net.minecraftforge.common.ForgeHooks.onInteractEntity(player, mobs
                    .get(0), Hand.MAIN_HAND);

            final boolean interacted = cancelResult != null || mobs.get(0).processInitialInteract(player,
                    Hand.MAIN_HAND);
            boolean result = false;
            if (!interacted) result = stack.interactWithEntity(player, mobs.get(0), Hand.MAIN_HAND);
            for (final ItemStack stack3 : player.inventory.mainInventory)
                if (!stack3.isEmpty()) if (stack3 != stack)
                {
                    result = true;
                    // This should result in the object just being
                    // dropped.
                    DispenseBehaviourInteract.DISPENSE_BEHAVIOR_REGISTRY.getOrDefault(DispenseBehaviourInteract.DEFAULT)
                            .dispense(source, stack3);
                }

            player.inventory.clear();
            if (result) return stack;
        }
        return DispenseBehaviourInteract.DISPENSE_BEHAVIOR_REGISTRY.getOrDefault(stack.getItem().getRegistryName())
                .dispense(source, stack);
    }

}

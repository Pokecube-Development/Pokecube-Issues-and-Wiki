package pokecube.core.entity.pokemobs;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IProperty;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.FakePlayer;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;

public class DispenseBehaviourInteract implements IDispenseItemBehavior
{
    public static final Map<ResourceLocation, IDispenseItemBehavior> DEFAULTS = Maps.newHashMap();

    private static final IDispenseItemBehavior DEFAULT = new DefaultDispenseItemBehavior();

    public static void registerBehavior(final ItemStack stack)
    {
        if (DispenseBehaviourInteract.DEFAULTS.containsKey(stack.getItem().getRegistryName())) return;
        final IDispenseItemBehavior original = DispenserBlock.DISPENSE_BEHAVIOR_REGISTRY.get(stack.getItem());
        DispenseBehaviourInteract.DEFAULTS.put(stack.getItem().getRegistryName(), original);
        DispenserBlock.registerDispenseBehavior(() -> stack.getItem(), new DispenseBehaviourInteract());
    }

    public static void registerBehavior(final ResourceLocation tag)
    {
        for (final Item item : ItemTags.getCollection().getOrCreate(tag).getAllElements())
            DispenseBehaviourInteract.registerBehavior(new ItemStack(item));
    }

    @Override
    public ItemStack dispense(final IBlockSource source, final ItemStack stack)
    {
        Direction dir = null;
        final BlockState state = source.getBlockState();
        for (final IProperty<?> prop : state.getProperties())
            if (prop.getValueClass() == Direction.class)
            {
                dir = (Direction) state.get(prop);
                break;
            }
        if (dir == null) return DispenseBehaviourInteract.DEFAULTS.getOrDefault(stack.getItem().getRegistryName(),
                DispenseBehaviourInteract.DEFAULT).dispense(source, stack);

        final FakePlayer player = PokecubeMod.getFakePlayer(source.getWorld());
        player.setPosition(source.getX(), source.getY() - player.getEyeHeight(), source.getZ());

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
                    DispenseBehaviourInteract.DEFAULTS.getOrDefault(stack.getItem().getRegistryName(),
                            DispenseBehaviourInteract.DEFAULT).dispense(source, stack3);
                }
            player.inventory.clear();
            if (result) return stack;
        }
        return DispenseBehaviourInteract.DEFAULTS.getOrDefault(stack.getItem().getRegistryName(),
                DispenseBehaviourInteract.DEFAULT).dispense(source, stack);
    }

}

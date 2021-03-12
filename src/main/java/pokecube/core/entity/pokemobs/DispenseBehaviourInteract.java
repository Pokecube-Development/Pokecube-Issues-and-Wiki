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
import net.minecraft.state.Property;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
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
        final IDispenseItemBehavior original = DispenserBlock.DISPENSER_REGISTRY.get(stack.getItem());
        DispenseBehaviourInteract.DEFAULTS.put(stack.getItem().getRegistryName(), original);
        DispenserBlock.registerBehavior(() -> stack.getItem(), new DispenseBehaviourInteract());
    }

    public static void registerBehavior(final ResourceLocation tag)
    {
        for (final Item item : ItemTags.getAllTags().getTagOrEmpty(tag).getValues())
            DispenseBehaviourInteract.registerBehavior(new ItemStack(item));
    }

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
        if (dir == null) return DispenseBehaviourInteract.DEFAULTS.getOrDefault(stack.getItem().getRegistryName(),
                DispenseBehaviourInteract.DEFAULT).dispense(source, stack);

        final FakePlayer player = PokecubeMod.getFakePlayer(source.getLevel());
        player.setPos(source.x(), source.y() - player.getEyeHeight(), source.z());

        final Vector3 loc = Vector3.getNewVector().set(source.getPos().relative(dir));
        final AxisAlignedBB box = loc.getAABB().inflate(2);
        final List<MobEntity> mobs = source.getLevel().getEntitiesOfClass(MobEntity.class, box);
        Collections.shuffle(mobs);
        if (!mobs.isEmpty())
        {
            player.inventory.clearContent();
            player.setItemInHand(Hand.MAIN_HAND, stack);

            ActionResultType cancelResult = net.minecraftforge.common.ForgeHooks.onInteractEntityAt(player, mobs.get(0),
                    new Vector3d(0, 0, 0), Hand.MAIN_HAND);
            if (cancelResult == null) cancelResult = net.minecraftforge.common.ForgeHooks.onInteractEntity(player, mobs
                    .get(0), Hand.MAIN_HAND);

            final boolean interacted = cancelResult != null || mobs.get(0).interact(player,
                    Hand.MAIN_HAND) != ActionResultType.PASS;
            ActionResultType result = ActionResultType.PASS;
            if (!interacted) result = stack.interactLivingEntity(player, mobs.get(0), Hand.MAIN_HAND);
            for (final ItemStack stack3 : player.inventory.items)
                if (!stack3.isEmpty()) if (stack3 != stack)
                {
                    result = ActionResultType.SUCCESS;
                    // This should result in the object just being
                    // dropped.
                    DispenseBehaviourInteract.DEFAULTS.getOrDefault(stack.getItem().getRegistryName(),
                            DispenseBehaviourInteract.DEFAULT).dispense(source, stack3);
                }
            player.inventory.clearContent();
            if (result != ActionResultType.PASS) return stack;
        }
        return DispenseBehaviourInteract.DEFAULTS.getOrDefault(stack.getItem().getRegistryName(),
                DispenseBehaviourInteract.DEFAULT).dispense(source, stack);
    }

}

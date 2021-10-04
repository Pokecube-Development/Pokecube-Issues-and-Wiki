package pokecube.core.entity.pokemobs;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;

public class DispenseBehaviourInteract implements DispenseItemBehavior
{
    public static final Map<ResourceLocation, DispenseItemBehavior> DEFAULTS = Maps.newHashMap();

    private static final DispenseItemBehavior DEFAULT = new DefaultDispenseItemBehavior();

    public static void registerBehavior(final ItemStack stack)
    {
        if (DispenseBehaviourInteract.DEFAULTS.containsKey(stack.getItem().getRegistryName())) return;
        final DispenseItemBehavior original = DispenserBlock.DISPENSER_REGISTRY.get(stack.getItem());
        DispenseBehaviourInteract.DEFAULTS.put(stack.getItem().getRegistryName(), original);
        DispenserBlock.registerBehavior(() -> stack.getItem(), new DispenseBehaviourInteract());
    }

    public static void registerBehavior(final ResourceLocation tag)
    {
        for (final Item item : ItemTags.getAllTags().getTagOrEmpty(tag).getValues())
            DispenseBehaviourInteract.registerBehavior(new ItemStack(item));
    }

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
        if (dir == null) return DispenseBehaviourInteract.DEFAULTS.getOrDefault(stack.getItem().getRegistryName(),
                DispenseBehaviourInteract.DEFAULT).dispense(source, stack);

        final FakePlayer player = PokecubeMod.getFakePlayer(source.getLevel());
        player.setPos(source.x(), source.y() - player.getEyeHeight(), source.z());

        final Vector3 loc = Vector3.getNewVector().set(source.getPos().relative(dir));
        final AABB box = loc.getAABB().inflate(2);
        final List<Mob> mobs = source.getLevel().getEntitiesOfClass(Mob.class, box);
        Collections.shuffle(mobs);
        if (!mobs.isEmpty())
        {
            player.getInventory().clearContent();
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);

            InteractionResult cancelResult = net.minecraftforge.common.ForgeHooks.onInteractEntityAt(player, mobs.get(0),
                    new Vec3(0, 0, 0), InteractionHand.MAIN_HAND);
            if (cancelResult == null) cancelResult = net.minecraftforge.common.ForgeHooks.onInteractEntity(player, mobs
                    .get(0), InteractionHand.MAIN_HAND);

            final boolean interacted = cancelResult != null || mobs.get(0).interact(player,
                    InteractionHand.MAIN_HAND) != InteractionResult.PASS;
            InteractionResult result = InteractionResult.PASS;
            if (!interacted) result = stack.interactLivingEntity(player, mobs.get(0), InteractionHand.MAIN_HAND);
            for (final ItemStack stack3 : player.getInventory().items)
                if (!stack3.isEmpty()) if (stack3 != stack)
                {
                    result = InteractionResult.SUCCESS;
                    // This should result in the object just being
                    // dropped.
                    DispenseBehaviourInteract.DEFAULTS.getOrDefault(stack.getItem().getRegistryName(),
                            DispenseBehaviourInteract.DEFAULT).dispense(source, stack3);
                }
            player.getInventory().clearContent();
            if (result != InteractionResult.PASS) return stack;
        }
        return DispenseBehaviourInteract.DEFAULTS.getOrDefault(stack.getItem().getRegistryName(),
                DispenseBehaviourInteract.DEFAULT).dispense(source, stack);
    }

}

package pokecube.core.items;

import java.util.List;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import pokecube.core.entity.boats.GenericBoat;
import pokecube.core.entity.boats.GenericBoat.BoatType;

public class GenericBoatItem extends Item
{
    private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);
    private final BoatType type;

    public GenericBoatItem(BoatType type, Properties properties)
    {
        super(properties);
        this.type = type;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
    {
        ItemStack itemstack = player.getItemInHand(hand);
        HitResult hitresult = getPlayerPOVHitResult(world, player, ClipContext.Fluid.ANY);
        if (hitresult.getType() == HitResult.Type.MISS)
        {
            return InteractionResultHolder.pass(itemstack);
        }
        else
        {
            Vec3 vec3 = player.getViewVector(1.0F);
            double d0 = 5.0D;
            List<Entity> list = world.getEntities(player,
                    player.getBoundingBox().expandTowards(vec3.scale(d0)).inflate(1.0D), ENTITY_PREDICATE);
            if (!list.isEmpty())
            {
                Vec3 vec31 = player.getEyePosition();

                for (Entity entity : list)
                {
                    AABB aabb = entity.getBoundingBox().inflate(entity.getPickRadius());
                    if (aabb.contains(vec31))
                    {
                        return InteractionResultHolder.pass(itemstack);
                    }
                }
            }

            if (hitresult.getType() == HitResult.Type.BLOCK)
            {
                GenericBoat boat = new GenericBoat(world, hitresult.getLocation().x, hitresult.getLocation().y,
                        hitresult.getLocation().z);
                boat.setType(this.type);
                boat.setYRot(player.getYRot());
                if (!world.noCollision(boat, boat.getBoundingBox()))
                {
                    return InteractionResultHolder.fail(itemstack);
                }
                else
                {
                    if (!world.isClientSide)
                    {
                        world.addFreshEntity(boat);
                        world.gameEvent(player, GameEvent.ENTITY_PLACE, new BlockPos(hitresult.getLocation()));
                        if (!player.getAbilities().instabuild)
                        {
                            itemstack.shrink(1);
                        }
                    }

                    player.awardStat(Stats.ITEM_USED.get(this));
                    return InteractionResultHolder.sidedSuccess(itemstack, world.isClientSide());
                }
            }
            else
            {
                return InteractionResultHolder.pass(itemstack);
            }
        }
    }
}

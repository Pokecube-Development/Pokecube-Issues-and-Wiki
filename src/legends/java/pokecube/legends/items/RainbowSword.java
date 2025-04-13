package pokecube.legends.items;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RainbowSword extends SwordItem
{
    public final Tier tier;
    public RainbowSword(final Tier material)
    {
        super(material, new Item.Properties());
        this.tier = material;
    }

    @Override
    public boolean isValidRepairItem(ItemStack item, ItemStack repair) {
        return this.tier.getRepairIngredient().test(repair) || super.isValidRepairItem(item, repair);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRECHARGE_USE,
                SoundSource.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));

        double x = player.getX() + (double)(player.getDirection().getStepX() * 0.3F);
        double y = player.getY() + (double)(player.getDirection().getStepY() * 0.3F);
        double z = player.getZ() + (double)(player.getDirection().getStepZ() * 0.3F);

        // Get the player's orientation (yaw and pitch)
        float yaw = player.getYRot();
        float pitch = player.getXRot();

        // Calculate the direction vector
        double vx = -Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
        double vy = -Math.sin(Math.toRadians(pitch));
        double vz = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));

        if (!world.isClientSide)
        {
            SmallFireball fireball = new SmallFireball(world, x, y + 1.0, z, new Vec3(vx, vy, vz));
            fireball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            world.addFreshEntity(fireball);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild && world instanceof ServerLevel level) {
            stack.hurtAndBreak(3, level, player, (item) ->
            {
              player.onEquippedItemBroken(item, hand==InteractionHand.MAIN_HAND?EquipmentSlot.MAINHAND:EquipmentSlot.OFFHAND);
            });
        }

        player.getCooldowns().addCooldown(this, 20);
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
    }
}

package pokecube.legends.items;

import java.util.List;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.init.ItemInit;

public class ControllerUltraEffect extends ItemBase
{

    public ControllerUltraEffect(final String name, final CreativeModeTab tab, final int maxStackSize)
    {
        super(name, tab, maxStackSize);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isFoil(final ItemStack itemstack)
    {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(final ItemStack stack, final Level worldIn, final List<Component> tooltip,
            final TooltipFlag flagIn)
    {
        String message;
        if (Screen.hasShiftDown()) message = I18n.get("legends." + this.tooltipname + ".tooltip");
        else message = I18n.get("pokecube.tooltip.advanced");
        tooltip.add(new TranslatableComponent(message));
    }

    @Override
	public void inventoryTick(final ItemStack itemstack, final Level world, final Entity entity, final int slot, final boolean selected) {
		super.inventoryTick(itemstack, world, entity, slot, selected);
		{
			final java.util.HashMap<String, Object> $_dependencies = new java.util.HashMap<>();
			$_dependencies.put("entity", entity);
			ControllerUltraEffect.executeProcedure($_dependencies);
		}
	}

    public static void executeProcedure(final java.util.HashMap<String, Object> dependencies)
    {
		if (dependencies.get("entity") == null)
		{
			System.err.println("Failed!");
			return;
		}
		final Entity entity = (Entity) dependencies.get("entity");
		if (entity instanceof ServerPlayer) if ((entity instanceof ServerPlayer ? ((Player) entity).getInventory().armor.get(0) : ItemStack.EMPTY)
        		.getItem() == new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()) if (entity instanceof LivingEntity)
        		((LivingEntity) entity).removeAllEffects();
	}
}

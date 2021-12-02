package pokecube.compat.curios;

import java.util.Optional;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import thut.wearables.EnumWearable;
import thut.wearables.IWearableChecker;
import thut.wearables.ThutWearables;
import thut.wearables.events.WearablesLoadedEvent;
import thut.wearables.inventory.PlayerWearables;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.event.CurioChangeEvent;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class Impl
{
    public static class CuriosChecker implements IWearableChecker
    {
        public static final BiMap<String, EnumWearable> identMap = HashBiMap.create();

        static
        {
            // Standard set
            CuriosChecker.identMap.put("ring", EnumWearable.FINGER);
            CuriosChecker.identMap.put("back", EnumWearable.BACK);
            CuriosChecker.identMap.put("belt", EnumWearable.WAIST);
            CuriosChecker.identMap.put("bracelet", EnumWearable.WRIST);
            CuriosChecker.identMap.put("head", EnumWearable.HAT);
            CuriosChecker.identMap.put("necklace", EnumWearable.NECK);

            // Custom set
            CuriosChecker.identMap.put("ankle", EnumWearable.ANKLE);
            CuriosChecker.identMap.put("ear", EnumWearable.EAR);
            CuriosChecker.identMap.put("eyes", EnumWearable.EYE);
        }

        public static final CuriosChecker INSTANCE = new CuriosChecker();

        public CuriosChecker()
        {
            MinecraftForge.EVENT_BUS.addListener(this::onCuriosChange);
            // We do not handle the drop, as the change event handles it!
            // MinecraftForge.EVENT_BUS.addListener(this::onWearableDrop);
            MinecraftForge.EVENT_BUS.addListener(this::onWearablesLoad);
        }

        private ICuriosItemHandler getCurios(final Entity mob)
        {
            return mob.getCapability(CuriosCapability.INVENTORY).orElse(null);
        }

        @Override
        public boolean canRemove(final LivingEntity player, final ItemStack itemstack, final EnumWearable slot,
                final int subIndex)
        {
            return true;
        }

        @Override
        public void onInteract(final LivingEntity player, final ItemStack itemstack, final EnumWearable slot,
                final int subIndex, final UseOnContext context)
        {
            // Noop
        }

        @Override
        public void onPutOn(final LivingEntity player, final ItemStack itemstack, final EnumWearable slot,
                final int subIndex)
        {
            final ICuriosItemHandler curios = this.getCurios(player);
            final String ident = CuriosChecker.identMap.inverse().get(slot);
            if (ident != null && curios != null) try
            {
                final Optional<ICurioStacksHandler> opt = curios.getStacksHandler(ident);
                if (!opt.isPresent()) return;
                final IDynamicStackHandler handler = opt.get().getStacks();
                final ItemStack cur = handler.getStackInSlot(subIndex);
                if (cur == itemstack) return;
                handler.setStackInSlot(subIndex, itemstack);
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void onTakeOff(final LivingEntity player, final ItemStack itemstack, final EnumWearable slot,
                final int subIndex)
        {
            // We need to sync to curio if possible
            final ICuriosItemHandler curios = this.getCurios(player);
            final String ident = CuriosChecker.identMap.inverse().get(slot);
            if (ident != null && curios != null) try
            {
                final Optional<ICurioStacksHandler> opt = curios.getStacksHandler(ident);
                if (!opt.isPresent()) return;
                final IDynamicStackHandler handler = opt.get().getStacks();
                final ItemStack cur = handler.getStackInSlot(subIndex);
                if (cur.isEmpty()) return;
                handler.setStackInSlot(subIndex, ItemStack.EMPTY);
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpdate(final LivingEntity player, final ItemStack itemstack, final EnumWearable slot,
                final int subIndex)
        {
            // Noop
        }

        private void onCuriosChange(final CurioChangeEvent event)
        {
            final String identifier = event.getIdentifier();
            if (!CuriosChecker.identMap.containsKey(identifier)) return;
            final LivingEntity mob = event.getEntityLiving();
            final int index = event.getSlotIndex();
            final EnumWearable slot = CuriosChecker.identMap.get(identifier);
            final ItemStack to = event.getTo();
            final PlayerWearables wearables = ThutWearables.getWearables(mob);
            if (wearables != null && index < slot.slots)
            {
                wearables.setWearable(slot, to, index);
                ThutWearables.syncWearables(mob);
            }
        }

        private void onWearablesLoad(final WearablesLoadedEvent event)
        {
            final PlayerWearables wearables = event.loaded;
            final LivingEntity mob = event.wearer;
            final ICuriosItemHandler curios = this.getCurios(mob);
            if (curios == null) return;
            for (int i = 0; i < wearables.getSlots(); i++)
            {
                final int index = i;
                final int subIndex = EnumWearable.getSubIndex(index);
                final EnumWearable slot = EnumWearable.getWearable(index);
                final ItemStack stack = wearables.getStackInSlot(index);
                if (!stack.isEmpty()) this.onPutOn(mob, stack, slot, subIndex);
            }
        }
    }

    public static void register()
    {
        ThutWearables.config.noButton = true;
        EnumWearable.registerWearableChecker(CuriosChecker.INSTANCE);
    }

    public static void onIMC(final InterModEnqueueEvent event)
    {
        for (final String type : CuriosChecker.identMap.keySet())
        {
            final EnumWearable slot = CuriosChecker.identMap.get(type);
            InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder(type).size(
                    slot.slots).icon(new ResourceLocation(EnumWearable.getIcon(slot.index))).build());
        }
    }
}

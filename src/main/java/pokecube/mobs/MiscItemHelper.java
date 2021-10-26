package pokecube.mobs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.items.UsableItemEffects;
import pokecube.core.items.UsableItemEffects.BaseUseable;
import pokecube.core.items.UsableItemEffects.VitaminUsable.VitaminEffect;
import pokecube.core.items.vitamins.ItemVitamin;
import pokecube.core.utils.PokeType;
import thut.api.item.ItemList;

public class MiscItemHelper
{
    public static class CharcoalEffect extends BaseUseable
    {
        private static PokeType FIRE;

        public CharcoalEffect()
        {
            if (CharcoalEffect.FIRE == null) CharcoalEffect.FIRE = PokeType.getType("fire");
        }

        /**
         * @param pokemob
         * @param stack
         * @return
         */
        @Override
        public InteractionResultHolder<ItemStack> onMoveTick(final IPokemob pokemob, final ItemStack stack,
                final MovePacket moveuse)
        {
            if (pokemob == moveuse.attacker && moveuse.pre) if (moveuse.getMove().getType(
                    pokemob) == CharcoalEffect.FIRE)
            {
                moveuse.PWR *= 1.2;
                return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
            }
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }
    }

    public static final ResourceLocation USABLE = new ResourceLocation(PokecubeMobs.MODID, "usables");

    static ItemStack CHARCOALSTACK;

    private static InteractionResultHolder<ItemStack> applyEVs(final byte[] evs, final ItemStack stack, final IPokemob pokemob)
    {
        boolean fed = false;
        for (int i = 0; i < evs.length; i++)
        {
            final byte toAdd = evs[i];
            final byte ev = pokemob.getEVs()[i];

            if (toAdd > 0 && ev < Byte.MAX_VALUE)
            {
                fed = true;
                final byte[] evs2 = pokemob.getEVs();
                final int amt = ev + toAdd;
                evs2[i] = (byte) Math.min(Byte.MAX_VALUE, amt);
                pokemob.setEVs(evs2);
                break;
            }
        }
        return new InteractionResultHolder<>(fed ? InteractionResult.SUCCESS : InteractionResult.FAIL, stack);
    }

    private static InteractionResultHolder<ItemStack> feedVitamin(final ItemStack stack, final Entity entity)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
        if (pokemob != null)
        {
            if (ItemList.is(new ResourceLocation("pokecube:vit_hpup"), stack)) return MiscItemHelper.applyEVs(
                    new byte[] { 10, 0, 0, 0, 0, 0 }, stack, pokemob);
            if (ItemList.is(new ResourceLocation("pokecube:vit_protein"), stack)) return MiscItemHelper.applyEVs(
                    new byte[] { 0, 10, 0, 0, 0, 0 }, stack, pokemob);
            if (ItemList.is(new ResourceLocation("pokecube:vit_iron"), stack)) return MiscItemHelper.applyEVs(
                    new byte[] { 0, 0, 10, 0, 0, 0 }, stack, pokemob);
            if (ItemList.is(new ResourceLocation("pokecube:vit_calcium"), stack)) return MiscItemHelper.applyEVs(
                    new byte[] { 0, 0, 0, 10, 0, 0 }, stack, pokemob);
            if (ItemList.is(new ResourceLocation("pokecube:vit_zinc"), stack)) return MiscItemHelper.applyEVs(
                    new byte[] { 0, 10, 0, 0, 10, 0 }, stack, pokemob);
            if (ItemList.is(new ResourceLocation("pokecube:vit_carbos"), stack)) return MiscItemHelper.applyEVs(
                    new byte[] { 0, 0, 0, 0, 0, 10 }, stack, pokemob);
        }
        return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
    }

    public static void init()
    {
        ItemVitamin.vitamins.add("carbos");
        ItemVitamin.vitamins.add("zinc");
        ItemVitamin.vitamins.add("protein");
        ItemVitamin.vitamins.add("calcium");
        ItemVitamin.vitamins.add("hpup");
        ItemVitamin.vitamins.add("iron");

        final VitaminEffect value = new VitaminEffect()
        {
            @Override
            public InteractionResultHolder<ItemStack> onUse(final IPokemob pokemob, final ItemStack stack, final LivingEntity user)
            {
                final InteractionResultHolder<ItemStack> result = MiscItemHelper.feedVitamin(stack, pokemob.getEntity());
                // TODO decide on whether to send a message if it fails?
                return result;
            }
        };

        UsableItemEffects.VitaminUsable.effects.put("carbos", value);
        UsableItemEffects.VitaminUsable.effects.put("zinc", value);
        UsableItemEffects.VitaminUsable.effects.put("protein", value);
        UsableItemEffects.VitaminUsable.effects.put("calcium", value);
        UsableItemEffects.VitaminUsable.effects.put("hpup", value);
        UsableItemEffects.VitaminUsable.effects.put("iron", value);
    }

    @SubscribeEvent
    public static void registerCapabilities(final AttachCapabilitiesEvent<ItemStack> event)
    {
        // Already added the cap
        if (event.getCapabilities().containsKey(MiscItemHelper.USABLE)) return;
        if (MiscItemHelper.CHARCOALSTACK == null || MiscItemHelper.CHARCOALSTACK == event.getObject()) return;
        if (MiscItemHelper.CHARCOALSTACK.getItem() == event.getObject().getItem()) event.addCapability(
                MiscItemHelper.USABLE, new CharcoalEffect());
    }

}

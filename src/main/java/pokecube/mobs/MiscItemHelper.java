package pokecube.mobs;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.items.UsableItemEffects;
import pokecube.core.items.UsableItemEffects.BaseUseable;
import pokecube.core.items.UsableItemEffects.VitaminUsable.VitaminEffect;
import pokecube.core.items.vitamins.ItemVitamin;
import pokecube.core.utils.PokeType;

@Mod.EventBusSubscriber
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
        public ActionResult<ItemStack> onMoveTick(final IPokemob pokemob, final ItemStack stack,
                final MovePacket moveuse)
        {
            if (pokemob == moveuse.attacker && moveuse.pre) if (moveuse.getMove().getType(
                    pokemob) == CharcoalEffect.FIRE)
            {
                moveuse.PWR *= 1.2;
                return new ActionResult<>(ActionResultType.SUCCESS, stack);
            }
            return new ActionResult<>(ActionResultType.FAIL, stack);
        }
    }

    public static final ResourceLocation USABLE = new ResourceLocation(PokecubeMobs.MODID, "usables");

    static ItemStack CHARCOALSTACK;

    public static ActionResult<ItemStack> feedToPokemob(final ItemStack stack, final Entity entity)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
        if (pokemob != null)
        {
            if (PokecubeItems.is(new ResourceLocation("hpup"), stack))
            {
                pokemob.addEVs(new byte[] { 10, 0, 0, 0, 0, 0 });
                return new ActionResult<>(ActionResultType.SUCCESS, stack);
            }
            if (PokecubeItems.is(new ResourceLocation("protein"), stack))
            {
                pokemob.addEVs(new byte[] { 0, 10, 0, 0, 0, 0 });
                return new ActionResult<>(ActionResultType.SUCCESS, stack);
            }
            if (PokecubeItems.is(new ResourceLocation("iron"), stack))
            {
                pokemob.addEVs(new byte[] { 0, 0, 10, 0, 0, 0 });
                return new ActionResult<>(ActionResultType.SUCCESS, stack);
            }
            if (PokecubeItems.is(new ResourceLocation("calcium"), stack))
            {
                pokemob.addEVs(new byte[] { 0, 0, 0, 10, 0, 0 });
                return new ActionResult<>(ActionResultType.SUCCESS, stack);
            }
            if (PokecubeItems.is(new ResourceLocation("zinc"), stack))
            {
                pokemob.addEVs(new byte[] { 0, 0, 0, 0, 10, 0 });
                return new ActionResult<>(ActionResultType.SUCCESS, stack);
            }
            if (PokecubeItems.is(new ResourceLocation("carbos"), stack))
            {
                pokemob.addEVs(new byte[] { 0, 0, 0, 0, 0, 10 });
                return new ActionResult<>(ActionResultType.SUCCESS, stack);
            }
        }
        return new ActionResult<>(ActionResultType.FAIL, stack);
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
            public ActionResult<ItemStack> onUse(final IPokemob pokemob, final ItemStack stack, final LivingEntity user)
            {
                return MiscItemHelper.feedToPokemob(stack, pokemob.getEntity());
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
        if (event.getCapabilities().containsKey(MiscItemHelper.USABLE) || MiscItemHelper.CHARCOALSTACK == null
                || MiscItemHelper.CHARCOALSTACK == event.getObject()) return;
        if (MiscItemHelper.CHARCOALSTACK.getItem() == event.getObject().getItem()) event.addCapability(MiscItemHelper.USABLE,
                new CharcoalEffect());
    }

}

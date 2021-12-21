package pokecube.core.events.npc;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.event.entity.living.LivingEvent;

public class NpcTradesEvent extends LivingEvent
{
    public final MerchantOffers offers;

    public NpcTradesEvent(LivingEntity entity, MerchantOffers offers)
    {
        super(entity);
        this.offers = offers;
    }
}

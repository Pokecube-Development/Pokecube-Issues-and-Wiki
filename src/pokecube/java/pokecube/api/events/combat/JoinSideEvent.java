package pokecube.api.events.combat;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import pokecube.api.moves.Battle;

import java.util.List;

public class JoinSideEvent extends Event implements ICancellableEvent
{
    public final Battle battle;
    public final LivingEntity joining;
    public final List<LivingEntity> ourSide;
    public final List<LivingEntity> otherSide;

    public JoinSideEvent(Battle battle, LivingEntity joining, List<LivingEntity> ourSide, List<LivingEntity> otherSide)
    {
        this.battle = battle;
        this.joining = joining;
        this.ourSide = ImmutableList.copyOf(ourSide);
        this.otherSide = ImmutableList.copyOf(otherSide);
    }
}

package pokecube.core.interfaces;

import net.minecraft.entity.MobEntity;

public interface IInhabitable
{

    void onLeaveHabitat(MobEntity mob);

    boolean onEnterHabitat(MobEntity mob);

    boolean canEnterHabitat(MobEntity mob);
}

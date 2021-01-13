package pokecube.core.interfaces;

import net.minecraft.entity.MobEntity;

public interface IInhabitable
{

    void onExitHabitat(MobEntity mob);

    boolean onEnterHabitat(MobEntity mob);

    boolean canEnterHabitat(MobEntity mob);
}

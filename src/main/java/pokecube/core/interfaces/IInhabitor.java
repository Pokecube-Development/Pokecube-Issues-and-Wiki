package pokecube.core.interfaces;

import javax.annotation.Nullable;

import net.minecraft.core.GlobalPos;

public interface IInhabitor
{
    // We should locate the habitat ourself, so only a getter for this
    @Nullable
    GlobalPos getHome();

    // Called right after leaving the habitat
    void onExitHabitat();

    // Where we are currently trying to work
    @Nullable
    GlobalPos getWorkSite();

    // The habitat might decide our worksite for us, so make this have a setter
    void setWorldSite(@Nullable GlobalPos site);
}

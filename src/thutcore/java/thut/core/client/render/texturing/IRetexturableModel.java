package thut.core.client.render.texturing;

import thut.api.entity.animation.IAnimationChanger;

public interface IRetexturableModel
{
    default void setAnimationChanger(IAnimationChanger changer)
    {
        setAnimationChangerRaw(changer);
    }

    void setAnimationChangerRaw(IAnimationChanger changer);

    default void setTexturer(IPartTexturer texturer)
    {
        setTexturerRaw(texturer);
    }

    void setTexturerRaw(IPartTexturer texturer);
}

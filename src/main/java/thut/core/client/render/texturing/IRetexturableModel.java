package thut.core.client.render.texturing;

import thut.core.client.render.animation.IAnimationChanger;

public interface IRetexturableModel
{
    void setAnimationChanger(IAnimationChanger changer);

    void setTexturer(IPartTexturer texturer);
}

package thut.core.client.render.texturing;

import java.util.function.Supplier;

import thut.api.entity.animation.IAnimationChanger;

public interface IRetexturableModel
{
    public static class Holder<T> implements Supplier<T>
    {
        T value = null;

        @Override
        public T get()
        {
            return value;
        }

        public void set(T value)
        {
            this.value = value;
        }
    }

    Holder<IAnimationChanger> getAnimationChanger();

    void setAnimationChanger(Holder<IAnimationChanger> input);

    Holder<IPartTexturer> getTexturerChanger();

    void setTexturerChanger(Holder<IPartTexturer> input);
}

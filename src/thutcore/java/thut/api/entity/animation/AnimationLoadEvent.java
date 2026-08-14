package thut.api.entity.animation;

import net.neoforged.bus.api.Event;
import thut.api.ModelHolder;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelRenderer;

public class AnimationLoadEvent extends Event
{
    final ModelHolder holder;
    final IModel model;
    final IModelRenderer<?> renderer;

    public AnimationLoadEvent(ModelHolder holder, IModel model, IModelRenderer<?> renderer) {
        this.holder = holder;
        this.model = model;
        this.renderer = renderer;
    }

    public IModel getModel()
    {
        return model;
    }

    public ModelHolder getHolder()
    {
        return holder;
    }

    public IModelRenderer<?> getRenderer()
    {
        return renderer;
    }

    public static class Pre extends AnimationLoadEvent
    {
        public Pre(ModelHolder holder, IModel model, IModelRenderer<?> renderer)
        {
            super(holder, model, renderer);
        }
    }

    public static class Post extends AnimationLoadEvent
    {
        public Post(ModelHolder holder, IModel model, IModelRenderer<?> renderer)
        {
            super(holder, model, renderer);
        }
    }

    public static class Fail extends AnimationLoadEvent
    {
        public Fail(ModelHolder holder, IModel model, IModelRenderer<?> renderer)
        {
            super(holder, model, renderer);
        }
    }
}

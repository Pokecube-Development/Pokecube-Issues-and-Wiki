package thut.core.client.render.model;

public interface IModelCustom
{
    default void renderAll()
    {

    }

    default void renderAll(IModelRenderer<?> renderer)
    {
        this.renderAll();
    }

    default void renderAllExcept(IModelRenderer<?> renderer, String... excludedGroupNames)
    {
        this.renderAllExcept(excludedGroupNames);
    }

    default void renderAllExcept(String... excludedGroupNames)
    {

    }

    default void renderOnly(IModelRenderer<?> renderer, String... groupNames)
    {
        this.renderOnly(groupNames);
    }

    default void renderOnly(String... groupNames)
    {

    }

    default void renderPart(IModelRenderer<?> renderer, String partName)
    {
        this.renderPart(partName);
    }

    default void renderPart(String partName)
    {

    }
}

package thut.core.client.render.tabula.old;

import com.google.common.annotations.Beta;

@Beta
public interface IModel extends thut.core.client.render.model.IModel
{
    String getAuthor();

    int getCubeCount();

    String getName();
}

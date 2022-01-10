package thut.api.entity.multipart;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import thut.api.entity.multipart.GenericPartEntity.Factory;

public abstract class MultipartEntity<T extends GenericPartEntity<E>, E extends MultipartEntity<?, ?>> extends Entity
        implements IMultpart<T, E>
{
    protected GenericPartEntity.Factory<T, E> factory;

    private PartHolder<T> holder;

    public MultipartEntity(EntityType<?> type, Level level)
    {
        super(type, level);

        List<T> allParts = Lists.newArrayList();
        Map<String, T[]> partMap = Maps.newHashMap();

        this.holder = new PartHolder<T>(allParts, partMap, new Holder<T>());
    }

    // Functions for vanilla/forge stuff

    @Override
    public boolean isMultipartEntity()
    {
        if (this.getHolder().getParts() == null) initParts();
        return this.getHolder().getParts().length > 0;
    }

    @Override
    public T[] getParts()
    {
        this.checkUpdateParts();
        if (!this.isAddedToWorld())
        {
            return getHolder().makeAllParts(this.getPartClass());
        }
        return this.getHolder().getParts();
    }

    // Functions for storing things needed by the interface

    public void setFactory(GenericPartEntity.Factory<T, E> factory)
    {
        this.factory = factory;
    }

    @Override
    public Factory<T, E> getFactory()
    {
        return factory;
    }

    @Override
    public PartHolder<T> getHolder()
    {
        return holder;
    }
}

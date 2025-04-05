package pokecube.api.entity;

import net.minecraft.core.GlobalPos;
import pokecube.api.ai.IInhabitor;
import thut.api.data.HolderProvider;

public class CapabilityInhabitor
{
    public static class NotInhabitor implements IInhabitor
    {

        @Override
        public GlobalPos getHome()
        {
            return null;
        }

        @Override
        public void onExitHabitat()
        {}

        @Override
        public GlobalPos getWorkSite()
        {
            return null;
        }

        @Override
        public void setWorkSite(final GlobalPos site)
        {}
    }

    public static final HolderProvider<IInhabitor> _REGISTRY = new HolderProvider<>();
}

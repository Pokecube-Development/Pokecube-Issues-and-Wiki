package pokecube.world.gen.structures;

import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Unique;
import pokecube.api.events.StructureEvent;

public interface ITemplateExtended
{
    @Unique
    void $tryAddStructureEntity(StructureEvent.ReadTag event);

    @Unique
    boolean $hasAddedEntity(StructureTemplate.StructureBlockInfo info);
}

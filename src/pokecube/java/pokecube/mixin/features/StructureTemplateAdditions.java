package pokecube.mixin.features;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureEntityInfo;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import pokecube.api.PokecubeAPI;
import pokecube.api.events.StructureEvent;
import pokecube.world.gen.structures.ITemplateExtended;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(StructureTemplate.class)
public abstract class StructureTemplateAdditions implements ITemplateExtended
{
    @Unique
    private final Map<BlockPos, StructureEntityInfo> $customEntityInfos = new ConcurrentHashMap<>();

    @Final
    @Shadow
    public List<StructureEntityInfo> entityInfoList;

    @Unique
    @Override
    public void $tryAddStructureEntity(StructureEvent.ReadTag event)
    {
        if (event.nbt.contains("pokecube:structure_entity") && event.info != null)
        {
            if ($hasAddedEntity(event.info))
            {
                PokecubeAPI.logInfo("Not replacing info at {}", event.info.pos());
                return;
            }
            Vec3 v = event.info.pos().getBottomCenter();
            StructureEntityInfo info = new StructureEntityInfo(v, event.info.pos(), event.nbt);
            $customEntityInfos.put(event.info.pos(), info);
            entityInfoList.add(info);
            PokecubeAPI.logInfo("Added info {} at {}", event.nbt, event.info.pos());
        }
    }

    @Unique
    @Override
    public boolean $hasAddedEntity(StructureBlockInfo info)
    {
        return $customEntityInfos.containsKey(info.pos());
    }

}

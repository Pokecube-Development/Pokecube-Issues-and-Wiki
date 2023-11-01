package thut.api.level.structures;

import java.util.Locale;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import thut.api.ThutCaps;
import thut.api.util.PermNodes;
import thut.core.init.CommonInit;
import thut.core.init.CommonInit.ICustomStickHandler;
import thut.lib.TComponent;

public class StructureStickApplier implements ICustomStickHandler
{
    private static record StructInfo(String structure, String building, String key)
    {
    }

    private StructInfo getStructure(final ServerPlayer player, final ItemStack held)
    {
        if (!PermNodes.getBooleanPerm(player, CommonInit.SET_STRUCTURE)) return null;
        if (held.getHoverName().getString().toLowerCase(Locale.ROOT).startsWith("struct->"))
        {
            final String[] args = held.getHoverName().getString().split("->");
            if (args.length < 2) return null;
            if (args.length == 2) return new StructInfo(args[1], "unk_part", "create");
            if (args.length == 3) return new StructInfo(args[1], args[2], "create");
            return new StructInfo(args[1], args[2], args[3]);
        }
        return null;
    }

    @Override
    public boolean isItem(ServerPlayer player, ItemStack held)
    {
        return getStructure(player, held) != null;
    }

    @Override
    public void apply(ServerPlayer player, ServerLevel level, ItemStack held, BlockPos min, BlockPos max)
    {
        StructInfo info = this.getStructure(player, held);
        String key = info.key();
        String structure = info.structure();
        String building = info.building();
        final BoundingBox box = BoundingBox.fromCorners(min, max);

        if (key.equals("delete"))
        {
            StructureManager.remove(level.dimension(), box, s -> s.is(structure));
            final String message = "msg.structmake.removed";
            thut.lib.ChatHelper.sendSystemMessage(player, TComponent.translatable(message, structure, building));
        }
        else
        {
            CapabilityWorldStructures structs = ThutCaps.getWorldStructures(level);
            if (structs != null)
            {
                structs.addBuilding(structure, building, box);
                final String message = "msg.structmake.set";
                thut.lib.ChatHelper.sendSystemMessage(player, TComponent.translatable(message, structure, building));
            }
        }
    }

    @Override
    public String getCornerMessage()
    {
        return "msg.structmake.setcorner";
    }

}

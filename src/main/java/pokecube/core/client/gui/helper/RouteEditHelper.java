package pokecube.core.client.gui.helper;

import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.ai.routes.IGuardAICapability.IGuardTask;
import thut.lib.TComponent;

public class RouteEditHelper
{

    public static void getGuiList(final ScrollGui<GuardEntry> entries, final IGuardAICapability guard,
            final Function<CompoundTag, CompoundTag> function, final Entity entity, final Screen parent,
            final int width, final int dx, final int dy, final int height)
    {
        final Font fontRenderer = Minecraft.getInstance().font;
        int num = 0;
        final MutableComponent blank = TComponent.literal("");
        for (final IGuardTask task : guard.getTasks())
        {
            final ListEditBox location = new ListEditBox(fontRenderer, -200, 0, width, 10, blank);
            final ListEditBox time = new ListEditBox(fontRenderer, -200, 0, width, 10, blank);
            final ListEditBox dist = new ListEditBox(fontRenderer, -200, 0, width, 10, blank);

            location.registerPreFocus(parent);
            time.registerPreFocus(parent);
            dist.registerPreFocus(parent);

            location.setMaxLength(Short.MAX_VALUE);
            time.setMaxLength(Short.MAX_VALUE);
            dist.setMaxLength(Short.MAX_VALUE);
            if (task.getPos() != null)
                location.setValue(task.getPos().getX() + " " + task.getPos().getY() + " " + task.getPos().getZ());
            time.setValue(task.getActiveTime().startTick + " " + task.getActiveTime().endTick);
            dist.setValue(task.getRoamDistance() + "");
            location.moveCursor(-location.getCursorPosition());
            time.moveCursor(-time.getCursorPosition());
            dist.moveCursor(-dist.getCursorPosition());
            final GuardEntry entry = new GuardEntry(num++, guard, entity, parent, location, time, dist, function, dx,
                    dy, height);
            entries.addEntry(entry);
        }
        // Blank value.
        final ListEditBox location = new ListEditBox(fontRenderer, -200, 0, width, 10, blank);
        final ListEditBox time = new ListEditBox(fontRenderer, -200, 0, width, 10, blank);
        final ListEditBox dist = new ListEditBox(fontRenderer, -200, 0, width, 10, blank);

        location.registerPreFocus(parent);
        time.registerPreFocus(parent);
        dist.registerPreFocus(parent);

        location.setMaxLength(Short.MAX_VALUE);
        time.setMaxLength(Short.MAX_VALUE);
        dist.setMaxLength(Short.MAX_VALUE);
        final GuardEntry entry = new GuardEntry(num++, guard, entity, parent, location, time, dist, function, dx, dy,
                height);
        entries.addEntry(entry);
    }

}

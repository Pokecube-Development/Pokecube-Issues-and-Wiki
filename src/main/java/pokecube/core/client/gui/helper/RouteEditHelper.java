package pokecube.core.client.gui.helper;

import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.ai.routes.IGuardAICapability.IGuardTask;

public class RouteEditHelper
{

    public static void getGuiList(final ScrollGui<GuardEntry> entries, final IGuardAICapability guard,
            final Function<CompoundNBT, CompoundNBT> function, final Entity entity, final Screen parent,
            final int width, final int dx, final int dy, final int height)
    {
        final FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        int num = 0;
        for (final IGuardTask task : guard.getTasks())
        {
            final TextFieldWidget location = new TextFieldWidget(fontRenderer, 0, 0, width, 10, "");
            final TextFieldWidget time = new TextFieldWidget(fontRenderer, 0, 0, width, 10, "");
            final TextFieldWidget dist = new TextFieldWidget(fontRenderer, 0, 0, width, 10, "");
            location.setMaxStringLength(Short.MAX_VALUE);
            time.setMaxStringLength(Short.MAX_VALUE);
            dist.setMaxStringLength(Short.MAX_VALUE);
            if (task.getPos() != null) location.setText(task.getPos().getX() + " " + task.getPos().getY() + " " + task
                    .getPos().getZ());
            time.setText(task.getActiveTime().startTick + " " + task.getActiveTime().endTick);
            dist.setText(task.getRoamDistance() + "");
            location.moveCursorBy(-location.getCursorPosition());
            time.moveCursorBy(-time.getCursorPosition());
            dist.moveCursorBy(-dist.getCursorPosition());
            final GuardEntry entry = new GuardEntry(num++, guard, entity, parent, location, time, dist, function, dx,
                    dy, height);
            entries.addEntry(entry);
        }
        // Blank value.
        final TextFieldWidget location = new TextFieldWidget(fontRenderer, 0, 0, width, 10, "");
        final TextFieldWidget time = new TextFieldWidget(fontRenderer, 0, 0, width, 10, "");
        final TextFieldWidget dist = new TextFieldWidget(fontRenderer, 0, 0, width, 10, "");
        location.setMaxStringLength(Short.MAX_VALUE);
        time.setMaxStringLength(Short.MAX_VALUE);
        dist.setMaxStringLength(Short.MAX_VALUE);
        final GuardEntry entry = new GuardEntry(num++, guard, entity, parent, location, time, dist, function, dx, dy,
                height);
        entries.addEntry(entry);
    }

}

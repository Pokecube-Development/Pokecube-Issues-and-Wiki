package pokecube.adventures.client.gui.trainer.editor.pages;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.entity.LivingEntity;
import pokecube.adventures.client.gui.trainer.editor.EditorGui;
import pokecube.adventures.client.gui.trainer.editor.pages.util.Page;
import pokecube.adventures.network.PacketTrainer;
import pokecube.core.entity.npc.NpcType;
import thut.lib.TComponent;

public class Spawn extends Page
{
    EditBox type;
    EditBox level;

    LivingEntity mob = null;

    int index = 0;

    boolean stand  = false;
    String  gender = "male";

    public Spawn(final EditorGui parent)
    {
        super(TComponent.literal(""), parent);
    }

    @Override
    public void onPageOpened()
    {
        this.children.clear();
        this.renderables.clear();
        super.onPageOpened();
        int yOffset = this.height / 2;
        int xOffset = this.width / 2;
        this.type = new EditBox(this.font, xOffset - 45, yOffset, 100, 10, TComponent.literal(""));
        this.level = new EditBox(this.font, xOffset - 45, yOffset + 20, 100, 10, TComponent.literal(""));

        this.index = this.index % NpcType.typeMap.size();
        final List<String> types = Lists.newArrayList(NpcType.typeMap.keySet());
        types.sort((s1, s2) -> s1.compareTo(s2));

        this.type.setValue(types.get(this.index));
        this.level.setValue("5");

        this.stand = true;

        this.level.setFilter(s ->
        {
            if (s.isEmpty()) return true;
            try
            {
                final int i = Integer.parseInt(s);
                return i > 0 && i <= 100;
            }
            catch (final NumberFormatException e)
            {
                return false;
            }
        });

        this.type.setEditable(false);

        this.addRenderableWidget(this.level);
        this.addRenderableWidget(this.type);

        this.addRenderableWidget(new Button.Builder(TComponent.literal("Next"), (b) -> {
            this.index++;
            this.index = this.index % types.size();
            this.type.setValue(types.get(this.index));
        }).bounds(xOffset + 75 - 15, yOffset, 40, 20).build());

        this.addRenderableWidget(new Button.Builder(TComponent.literal("Prev"), (b) -> {
            this.index--;
            if (this.index < 0) this.index = types.size() - 1;
            this.type.setValue(types.get(this.index));
        }).bounds(xOffset - 75 - 15, yOffset, 40, 20).build());

        this.addRenderableWidget(new Button.Builder(TComponent.literal("Stands"), (b) -> {
            if (b.getMessage().getString().equals("wanders")) b.setMessage(TComponent.literal("stands"));
            else b.setMessage(TComponent.literal("wanders"));
            this.stand = b.getMessage().getString().equals("stands");
        }).bounds(xOffset - 5, yOffset + 40, 40, 20).build());

        this.addRenderableWidget(new Button.Builder(TComponent.literal("Random"), (b) -> {
            if (b.getMessage().getString().equals("male")) b.setMessage(TComponent.literal("female"));
            else if (b.getMessage().getString().equals("female")) b.setMessage(TComponent.literal("random"));
            else b.setMessage(TComponent.literal("male"));
            this.gender = b.getMessage().getString();
        }).bounds(xOffset - 45, yOffset + 40, 40, 20).build());

        xOffset -= 20;
        yOffset += 10;

        this.addRenderableWidget(new Button.Builder(TComponent.literal("Spawn NPC"), (b) -> {
            this.send("npc");
        }).bounds(xOffset - 100, yOffset - 80, 80, 20).build());

        this.addRenderableWidget(new Button.Builder(TComponent.literal("Spawn Trainer"), (b) -> {
            this.send("trainer");
        }).bounds(xOffset - 20, yOffset - 80, 80, 20).build());

        this.addRenderableWidget(new Button.Builder(TComponent.literal("Spawn Leader"), (b) -> {
            this.send("leader");
        }).bounds(xOffset + 60, yOffset - 80, 80, 20).build());
    }

    private void send(final String type)
    {
        final PacketTrainer message = new PacketTrainer(PacketTrainer.SPAWN);
        message.getTag().putString("__type__", this.type.getValue());
        final int value = this.level.getValue().isEmpty() ? 1 : Integer.parseInt(this.level.getValue());
        message.getTag().putInt("L", value);
        message.getTag().putString("_npc_", type);
        message.getTag().putString("_g_", this.gender);
        message.getTag().putBoolean("S", this.stand);
        PacketTrainer.ASSEMBLER.sendToServer(message);
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        super.render(graphics, mouseX, mouseY, partialTicks);
    }
}

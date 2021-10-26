package pokecube.adventures.client.gui.trainer.editor.pages;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.LivingEntity;
import pokecube.adventures.client.gui.trainer.editor.EditorGui;
import pokecube.adventures.client.gui.trainer.editor.pages.util.Page;
import pokecube.adventures.network.PacketTrainer;
import pokecube.core.entity.npc.NpcType;

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
        super(new TextComponent(""), parent);
    }

    @Override
    public void onPageOpened()
    {
        this.children.clear();
        this.renderables.clear();
        super.onPageOpened();
        int yOffset = this.height / 2;
        int xOffset = this.width / 2;
        this.type = new EditBox(this.font, xOffset - 45, yOffset, 100, 10, new TextComponent(""));
        this.level = new EditBox(this.font, xOffset - 45, yOffset + 20, 100, 10, new TextComponent(""));

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

        this.addRenderableWidget(new Button(xOffset + 75 - 15, yOffset, 40, 20, new TextComponent("next"), b ->
        {
            this.index++;
            this.index = this.index % types.size();
            this.type.setValue(types.get(this.index));

        }));
        this.addRenderableWidget(new Button(xOffset - 75 - 15, yOffset, 40, 20, new TextComponent("prev"), b ->
        {
            this.index--;
            if (this.index < 0) this.index = types.size() - 1;
            this.type.setValue(types.get(this.index));
        }));
        this.addRenderableWidget(new Button(xOffset - 5, yOffset + 40, 40, 20, new TextComponent("stands"), b ->
        {
            if (b.getMessage().getString().equals("wanders")) b.setMessage(new TextComponent("stands"));
            else b.setMessage(new TextComponent("wanders"));
            this.stand = b.getMessage().getString().equals("stands");
        }));
        this.addRenderableWidget(new Button(xOffset - 45, yOffset + 40, 40, 20, new TextComponent("random"), b ->
        {
            if (b.getMessage().getString().equals("male")) b.setMessage(new TextComponent("female"));
            else if (b.getMessage().getString().equals("female")) b.setMessage(new TextComponent("random"));
            else b.setMessage(new TextComponent("male"));
            this.gender = b.getMessage().getString();
        }));

        xOffset -= 20;
        yOffset += 10;

        this.addRenderableWidget(new Button(xOffset - 100, yOffset - 80, 80, 20, new TextComponent("Spawn NPC"), b -> this
                .send("npc")));
        this.addRenderableWidget(new Button(xOffset - 20, yOffset - 80, 80, 20, new TextComponent("Spawn Trainer"),
                b -> this.send("trainer")));
        this.addRenderableWidget(new Button(xOffset + 60, yOffset - 80, 80, 20, new TextComponent("Spawn Leader"), b -> this
                .send("leader")));
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
    public void render(final PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks)
    {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}

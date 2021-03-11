package pokecube.adventures.client.gui.trainer.editor.pages;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.StringTextComponent;
import pokecube.adventures.client.gui.trainer.editor.EditorGui;
import pokecube.adventures.client.gui.trainer.editor.pages.util.Page;
import pokecube.adventures.network.PacketTrainer;
import pokecube.core.entity.npc.NpcType;

public class Spawn extends Page
{
    TextFieldWidget type;
    TextFieldWidget level;

    LivingEntity mob = null;

    int index = 0;

    boolean stand  = false;
    String  gender = "male";

    public Spawn(final EditorGui parent)
    {
        super(new StringTextComponent(""), parent);
    }

    @Override
    public void onPageOpened()
    {
        this.children.clear();
        this.buttons.clear();
        super.onPageOpened();
        int yOffset = this.height / 2;
        int xOffset = this.width / 2;
        this.type = new TextFieldWidget(this.font, xOffset - 45, yOffset, 100, 10, new StringTextComponent(""));
        this.level = new TextFieldWidget(this.font, xOffset - 45, yOffset + 20, 100, 10, new StringTextComponent(""));

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

        this.addButton(this.level);
        this.addButton(this.type);

        this.addButton(new Button(xOffset + 75 - 15, yOffset, 40, 20, new StringTextComponent("next"), b ->
        {
            this.index++;
            this.index = this.index % types.size();
            this.type.setValue(types.get(this.index));

        }));
        this.addButton(new Button(xOffset - 75 - 15, yOffset, 40, 20, new StringTextComponent("prev"), b ->
        {
            this.index--;
            if (this.index < 0) this.index = types.size() - 1;
            this.type.setValue(types.get(this.index));
        }));
        this.addButton(new Button(xOffset - 5, yOffset + 40, 40, 20, new StringTextComponent("stands"), b ->
        {
            if (b.getMessage().getString().equals("wanders")) b.setMessage(new StringTextComponent("stands"));
            else b.setMessage(new StringTextComponent("wanders"));
            this.stand = b.getMessage().getString().equals("stands");
        }));
        this.addButton(new Button(xOffset - 45, yOffset + 40, 40, 20, new StringTextComponent("random"), b ->
        {
            if (b.getMessage().getString().equals("male")) b.setMessage(new StringTextComponent("female"));
            else if (b.getMessage().getString().equals("female")) b.setMessage(new StringTextComponent("random"));
            else b.setMessage(new StringTextComponent("male"));
            this.gender = b.getMessage().getString();
        }));

        xOffset -= 20;
        yOffset += 10;

        this.addButton(new Button(xOffset - 100, yOffset - 80, 80, 20, new StringTextComponent("Spawn NPC"), b -> this
                .send("npc")));
        this.addButton(new Button(xOffset - 20, yOffset - 80, 80, 20, new StringTextComponent("Spawn Trainer"),
                b -> this.send("trainer")));
        this.addButton(new Button(xOffset + 60, yOffset - 80, 80, 20, new StringTextComponent("Spawn Leader"), b -> this
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
    public void render(final MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks)
    {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}

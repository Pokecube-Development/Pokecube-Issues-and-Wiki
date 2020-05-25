package pokecube.adventures.client.gui.items.editor.pages;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.client.gui.items.editor.EditorGui;
import pokecube.adventures.client.gui.items.editor.pages.util.Page;
import pokecube.adventures.network.PacketTrainer;

public class Spawn extends Page
{
    TextFieldWidget type;
    TextFieldWidget level;

    int     index  = 0;
    boolean leader = false;
    boolean stand  = false;

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
        final int yOffset = this.height / 2;
        final int xOffset = this.width / 2;
        this.type = new TextFieldWidget(this.font, xOffset - 45, yOffset, 100, 10, "");
        this.level = new TextFieldWidget(this.font, xOffset - 45, yOffset + 20, 100, 10, "");

        this.index = this.index % TypeTrainer.typeMap.size();
        final List<String> types = Lists.newArrayList(TypeTrainer.typeMap.keySet());
        types.sort((s1, s2) -> s1.compareTo(s2));

        this.type.setText(types.get(this.index));
        this.level.setText("5");

        this.level.setValidator(s ->
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

        this.type.setEnabled(false);

        this.addButton(this.level);
        this.addButton(this.type);

        this.addButton(new Button(xOffset + 75 - 15, yOffset, 40, 20, "next", b ->
        {
            this.index++;
            this.index = this.index % types.size();
            this.type.setText(types.get(this.index));

        }));
        this.addButton(new Button(xOffset - 75 - 15, yOffset, 40, 20, "prev", b ->
        {
            this.index--;
            if (this.index < 0) this.index = types.size() - 1;
            this.type.setText(types.get(this.index));
        }));
        this.addButton(new Button(xOffset - 45, yOffset + 40, 40, 20, "trainer", b ->
        {
            if (b.getMessage().equals("trainer")) b.setMessage("leader");
            else b.setMessage("trainer");
            this.leader = b.getMessage().equals("leader");
        }));
        this.addButton(new Button(xOffset - 5, yOffset + 40, 40, 20, "stands", b ->
        {
            if (b.getMessage().equals("wanders")) b.setMessage("stands");
            else b.setMessage("wanders");
            this.stand = b.getMessage().equals("stands");
        }));
        this.addButton(new Button(xOffset + 35, yOffset + 40, 40, 20, "spawn", b ->
        {
            final PacketTrainer message = new PacketTrainer(PacketTrainer.SPAWN);
            message.data.putString("T", this.type.getText());
            final int value = this.level.getText().isEmpty() ? 1 : Integer.parseInt(this.level.getText());
            message.data.putInt("L", value);
            message.data.putBoolean("C", this.leader);
            message.data.putBoolean("S", this.stand);
            PokecubeAdv.packets.sendToServer(message);
        }));
    }

    @Override
    public void init()
    {
        super.init();
    }
}

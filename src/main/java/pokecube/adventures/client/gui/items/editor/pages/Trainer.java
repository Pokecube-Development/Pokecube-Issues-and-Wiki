package pokecube.adventures.client.gui.items.editor.pages;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.client.gui.items.editor.EditorGui;
import pokecube.adventures.client.gui.items.editor.pages.util.Page;
import pokecube.adventures.network.PacketTrainer;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.npc.NpcType;
import pokecube.core.items.pokecubes.PokecubeManager;

public class Trainer extends Page
{
    TextFieldWidget name;
    TextFieldWidget type;
    TextFieldWidget urlSkin;
    TextFieldWidget customTex;
    TextFieldWidget playerName;

    boolean male;

    int index = 0;

    public Trainer(final EditorGui parent)
    {
        super(new StringTextComponent(""), parent);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onPageOpened()
    {
        this.children.clear();
        this.buttons.clear();
        super.onPageOpened();
        final int yOffset = this.height / 2;
        final int xOffset = this.width / 2;
        this.type = new TextFieldWidget(this.font, xOffset - 45, yOffset, 100, 10, new StringTextComponent(""));
        this.name = new TextFieldWidget(this.font, xOffset - 45, yOffset - 60, 100, 10, new StringTextComponent(""));
        this.playerName = new TextFieldWidget(this.font, xOffset - 45, yOffset - 45, 100, 10, new StringTextComponent(
                ""));
        this.customTex = new TextFieldWidget(this.font, xOffset - 45, yOffset - 30, 100, 10, new StringTextComponent(
                ""));
        this.urlSkin = new TextFieldWidget(this.font, xOffset - 45, yOffset - 15, 100, 10, new StringTextComponent(""));

        this.urlSkin.maxStringLength = 255;

        this.index = this.index % NpcType.typeMap.size();
        final List<String> types = Lists.newArrayList(NpcType.typeMap.keySet());
        types.sort((s1, s2) -> s1.compareTo(s2));

        if (this.parent.trainer != null)
        {
            this.index = this.index % TypeTrainer.typeMap.size();
            types.clear();
            types.addAll(TypeTrainer.typeMap.keySet());
            types.sort((s1, s2) -> s1.compareTo(s2));
        }
        if (this.parent.entity instanceof NpcMob)
        {
            final NpcMob mob = (NpcMob) this.parent.entity;
            String name = mob.name;
            this.male = mob.isMale();
            if (name.contains(":") && name.split(":").length > 1) name = name.split(":")[1];
            this.name.setText(name);
            this.type.setText(mob.getNpcType().getName());
            for (int i = 0; i < types.size(); i++)
                if (NpcType.typeMap.get(types.get(0)) == mob.getNpcType())
                {
                    this.index = i;
                    break;
                }
            this.playerName.setText(mob.playerName);
            this.customTex.setText(mob.customTex);
            this.urlSkin.setText(mob.urlSkin);
        }
        this.addButton(this.name);
        this.addButton(this.type);
        this.addButton(this.playerName);
        this.addButton(this.customTex);
        // this.addButton(this.urlSkin);

        if (this.parent.trainer != null)
        {
            int index = 0;
            for (index = 0; index < EditorGui.PAGELIST.size(); index++)
                if (EditorGui.PAGELIST.get(index) == Pokemob.class) break;
            final int pokemobIndex = index;
            for (index = 0; index < EditorGui.PAGELIST.size(); index++)
                if (EditorGui.PAGELIST.get(index) == Trainer.class) break;
            final int ourIndex = index;

            for (int i = 0; i < this.parent.trainer.countPokemon(); i++)
            {
                final int i2 = i;
                this.addButton(new Button(xOffset + 120, yOffset + 20 * i, 50, 20, new StringTextComponent("mob " + i),
                        b ->
                        {
                            this.parent.changePage(pokemobIndex);
                            if (!(this.parent.current_page instanceof Pokemob)) return;
                            final Pokemob page = (Pokemob) this.parent.current_page;
                            page.pokemob = PokecubeManager.itemToPokemob(this.parent.trainer.getPokemob(i2),
                                    this.parent.entity.getEntityWorld());
                            page.index = i2;
                            page.deleteCallback = () ->
                            {
                                this.parent.trainer.setPokemob(0, ItemStack.EMPTY);
                                final PacketTrainer message = new PacketTrainer(PacketTrainer.UPDATEMOB);
                                message.getTag().putInt("I", this.parent.entity.getEntityId());
                                message.getTag().putInt("__trainers__", this.index);
                                PacketTrainer.ASSEMBLER.sendToServer(message);
                                this.parent.changePage(ourIndex);
                            };
                            // Re-call this to init the gui properly
                            page.onPageOpened();
                        }));
            }
            if (this.parent.trainer.countPokemon() < 6)
            {
                final int i2 = this.parent.trainer.countPokemon();
                this.addButton(new Button(xOffset + 120, yOffset + 20 * i2, 50, 20, new StringTextComponent("mob +"),
                        b ->
                        {
                            this.parent.changePage(pokemobIndex);
                            if (!(this.parent.current_page instanceof Pokemob)) return;
                            final Pokemob page = (Pokemob) this.parent.current_page;
                            page.index = i2;
                            page.deleteCallback = () ->
                            {
                                this.parent.trainer.setPokemob(0, ItemStack.EMPTY);
                                final PacketTrainer message = new PacketTrainer(PacketTrainer.UPDATEMOB);
                                message.getTag().putInt("I", this.parent.entity.getEntityId());
                                message.getTag().putInt("__trainers__", this.index);
                                PacketTrainer.ASSEMBLER.sendToServer(message);
                                this.parent.changePage(ourIndex);
                            };
                            // Re-call this to init the gui properly
                            page.onPageOpened();
                        }));
            }
        }

        this.addButton(new Button(xOffset + 75 - 15, yOffset, 40, 20, new StringTextComponent("next"), b ->
        {
            this.index++;
            this.index = this.index % types.size();
            this.type.setText(types.get(this.index));

        }));
        this.addButton(new Button(xOffset - 75 - 15, yOffset, 40, 20, new StringTextComponent("prev"), b ->
        {
            this.index--;
            if (this.index < 0) this.index = types.size() - 1;
            this.type.setText(types.get(this.index));
        }));

        this.addButton(new Button(xOffset - 45, yOffset + 40, 40, 20, new StringTextComponent("kill"), b ->
        {
            final PacketTrainer message = new PacketTrainer(PacketTrainer.KILLTRAINER);
            message.getTag().putInt("I", this.parent.entity.getEntityId());
            PacketTrainer.ASSEMBLER.sendToServer(message);
            this.closeScreen();
        }));
        this.addButton(new Button(xOffset - 5, yOffset + 40, 40, 20, new StringTextComponent(this.male ? "male"
                : "female"), b ->
                {
                    if (b.getMessage().getString().equals("female")) b.setMessage(new StringTextComponent("male"));
                    else b.setMessage(new StringTextComponent("female"));
                    this.male = b.getMessage().getString().equals("male");
                }));

        this.addButton(new Button(xOffset + 35, yOffset + 40, 40, 20, new StringTextComponent("Update"), b ->
        {
            final PacketTrainer message = new PacketTrainer(PacketTrainer.UPDATETRAINER);
            message.getTag().putInt("I", this.parent.entity.getEntityId());
            message.getTag().putString("T", this.type.getText());
            message.getTag().putString("N", this.name.getText());
            message.getTag().putString("pS", this.playerName.getText());
            message.getTag().putString("uS", this.urlSkin.getText());
            message.getTag().putString("cS", this.customTex.getText());
            message.getTag().putBoolean("G", this.male);
            PacketTrainer.ASSEMBLER.sendToServer(message);
        }));

        this.addButton(new Button(xOffset - 5, yOffset + 60, 40, 20, new StringTextComponent("rewards"), b ->
        {
            // Change to a rewards page
        }));
        this.addButton(new Button(xOffset - 45, yOffset + 60, 40, 20, new StringTextComponent("routes"), b ->
        {
            // Change to a routes page
        }));
        this.addButton(new Button(xOffset - 85, yOffset + 60, 40, 20, new StringTextComponent("messages"), b ->
        {
            // Change to a messages page
        }));
        this.addButton(new Button(xOffset + 35, yOffset + 60, 40, 20, new StringTextComponent("ai"), b ->
        {
            // Change to a ai page
        }));
        this.addButton(new Button(xOffset + 75, yOffset + 60, 40, 20, new StringTextComponent("trades"), b ->
        {
            // Change to a trades page
        }));
    }
}

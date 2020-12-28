package pokecube.adventures.client.gui.trainer.editor.pages;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import pokecube.adventures.client.gui.trainer.editor.EditorGui;
import pokecube.adventures.client.gui.trainer.editor.pages.util.Page;
import pokecube.adventures.network.PacketTrainer;
import pokecube.core.client.gui.pokemob.GuiPokemobBase;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.npc.NpcType;
import pokecube.core.items.pokecubes.PokecubeManager;

public class Trainer extends Page
{
    protected static int lastMobIndex = -1;

    TextFieldWidget name;
    TextFieldWidget type;
    TextFieldWidget urlSkin;
    TextFieldWidget customTex;
    TextFieldWidget playerName;

    TextFieldWidget tradeList;

    boolean male;
    boolean typename;

    int index = 0;

    public Trainer(final EditorGui parent)
    {
        super(new StringTextComponent(""), parent);
    }

    @Override
    public void onPageOpened()
    {
        this.children.clear();
        this.buttons.clear();
        super.onPageOpened();
        final int x = this.width / 2;
        final int y = this.height / 2;

        final int dy = -72;
        final int sy = 10;
        int i = 0;
        final int dx = -120;

        this.type = new TextFieldWidget(this.font, x + dx, y + dy + sy * i++, 100, 10, new StringTextComponent(""));
        this.name = new TextFieldWidget(this.font, x + dx, y + dy + sy * i++, 100, 10, new StringTextComponent(""));
        this.tradeList = new TextFieldWidget(this.font, x + dx, y + dy + sy * i++, 100, 10, new StringTextComponent(
                ""));
        this.playerName = new TextFieldWidget(this.font, x + dx, y + dy + sy * i++, 100, 10, new StringTextComponent(
                ""));
        this.customTex = new TextFieldWidget(this.font, x + dx, y + dy + sy * i++, 100, 10, new StringTextComponent(
                ""));
        this.urlSkin = new TextFieldWidget(this.font, x + dx, y + dy + sy * i++, 100, 10, new StringTextComponent(""));

        this.urlSkin.maxStringLength = 255;

        this.index = this.index % NpcType.typeMap.size();
        final List<String> types = Lists.newArrayList(NpcType.typeMap.keySet());
        types.sort((s1, s2) -> s1.compareTo(s2));

        if (this.parent.trainer != null)
        {
            this.index = this.index % NpcType.typeMap.size();
            types.clear();
            types.addAll(NpcType.typeMap.keySet());
            types.sort((s1, s2) -> s1.compareTo(s2));
        }
        if (this.parent.entity instanceof NpcMob)
        {
            final NpcMob mob = (NpcMob) this.parent.entity;
            String name = mob.name;
            this.male = mob.isMale();
            this.typename = false;
            if (name.contains(":") && name.split(":").length > 1)
            {
                name = name.split(":")[1];
                this.typename = true;
            }
            this.name.setText(name);
            this.type.setText(mob.getNpcType().getName());
            for (i = 0; i < types.size(); i++)
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
        this.addButton(this.tradeList);
        this.addButton(this.type);
        this.addButton(this.playerName);
        this.addButton(this.customTex);

        if (this.parent.entity instanceof NpcMob) this.tradeList.setText(((NpcMob) this.parent.entity).customTrades);
        else this.tradeList.setEnabled(false);

        // this.addButton(this.urlSkin);
        int index = 0;
        for (index = 0; index < EditorGui.PAGELIST.size(); index++)
            if (EditorGui.PAGELIST.get(index) == Trainer.class) break;
        final int ourIndex = index;
        if (this.parent.trainer != null)
        {
            for (index = 0; index < EditorGui.PAGELIST.size(); index++)
                if (EditorGui.PAGELIST.get(index) == Pokemob.class) break;
            final int pokemobIndex = index;

            if (Trainer.lastMobIndex != -1)
            {
                this.parent.changePage(pokemobIndex);
                if (!(this.parent.current_page instanceof Pokemob)) return;
                final Pokemob page = (Pokemob) this.parent.current_page;
                page.pokemob = PokecubeManager.itemToPokemob(this.parent.trainer.getPokemob(Trainer.lastMobIndex),
                        this.parent.entity.getEntityWorld());
                page.index = Trainer.lastMobIndex;
                page.deleteCallback = () ->
                {
                    final PacketTrainer message = new PacketTrainer(PacketTrainer.UPDATEMOB);
                    message.getTag().putInt("I", this.parent.entity.getEntityId());
                    message.getTag().putInt("__trainers__", page.index);
                    PacketTrainer.ASSEMBLER.sendToServer(message);
                    this.closeScreen();
                    // This is needed to update that the pokemobs
                    // have changed
                    PacketTrainer.requestEdit(this.parent.entity);
                };
                page.closeCallback = () ->
                {
                    this.parent.changePage(ourIndex);
                };
                // Re-call this to init the gui properly
                page.onPageOpened();
                Trainer.lastMobIndex = -1;
                return;
            }

            for (i = 0; i < this.parent.trainer.countPokemon(); i++)
            {
                final int i2 = i;
                this.addButton(new Button(x + 20 + 50 * (i / 3), y - 10 + 20 * (i % 3), 50, 20, new StringTextComponent(
                        "mob " + (i + 1)), b ->
                        {
                            this.parent.changePage(pokemobIndex);
                            if (!(this.parent.current_page instanceof Pokemob)) return;
                            final Pokemob page = (Pokemob) this.parent.current_page;
                            page.pokemob = PokecubeManager.itemToPokemob(this.parent.trainer.getPokemob(i2),
                                    this.parent.entity.getEntityWorld());
                            page.index = i2;
                            page.deleteCallback = () ->
                            {
                                final PacketTrainer message = new PacketTrainer(PacketTrainer.UPDATEMOB);
                                message.getTag().putInt("I", this.parent.entity.getEntityId());
                                message.getTag().putInt("__trainers__", page.index);
                                PacketTrainer.ASSEMBLER.sendToServer(message);
                                this.closeScreen();
                                // This is needed to update that the pokemobs
                                // have changed
                                PacketTrainer.requestEdit(this.parent.entity);
                            };
                            page.closeCallback = () ->
                            {
                                this.parent.changePage(ourIndex);
                            };
                            // Re-call this to init the gui properly
                            page.onPageOpened();
                        }));
            }
            if (this.parent.trainer.countPokemon() < 6)
            {
                final int i2 = this.parent.trainer.countPokemon();
                this.addButton(new Button(x + 20 + 50 * (i2 / 3), y - 10 + 20 * (i2 % 3), 50, 20,
                        new StringTextComponent("mob +"), b ->
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
                                message.getTag().putInt("__trainers__", page.index);
                                PacketTrainer.ASSEMBLER.sendToServer(message);
                                this.parent.changePage(ourIndex);
                            };
                            page.closeCallback = () ->
                            {
                                this.parent.changePage(ourIndex);
                            };
                            // Re-call this to init the gui properly
                            page.onPageOpened();
                        }));
            }
        }

        this.addButton(new Button(x - 9, y + dy - 1, 10, 10, new StringTextComponent(">"), b ->
        {
            this.index++;
            this.index = this.index % types.size();
            this.type.setText(types.get(this.index));
            this.onUpdated();

        }));
        this.addButton(new Button(x - 19, y + dy - 1, 10, 10, new StringTextComponent("<"), b ->
        {
            this.index--;
            if (this.index < 0) this.index = types.size() - 1;
            this.type.setText(types.get(this.index));
            this.onUpdated();
        }));
        this.addButton(new Button(x - 123, y + 55, 40, 20, new StringTextComponent("Delete"), b ->
        {
            final PacketTrainer message = new PacketTrainer(PacketTrainer.KILLTRAINER);
            message.getTag().putInt("I", this.parent.entity.getEntityId());
            PacketTrainer.ASSEMBLER.sendToServer(message);
            this.closeScreen();
        }));
        this.addButton(new Button(x - 19, y + dy + 9, 10, 10, new StringTextComponent(this.male ? "\u2642" : "\u2640"),
                b ->
                {
                    this.male = !this.male;
                    b.setMessage(new StringTextComponent(this.male ? "\u2642" : "\u2640"));
                    this.onUpdated();
                }));
        this.addButton(new Button(x + 60, y - 53, 60, 10, new StringTextComponent(this.typename ? "Name Prefix"
                : "No Prefix"), b ->
                {
                    this.typename = !this.typename;
                    b.setMessage(new StringTextComponent(this.typename ? "Name Prefix" : "No Prefix"));
                    this.onUpdated();
                }));
        this.addButton(new Button(x + 80, y - 73, 40, 20, new StringTextComponent("Apply"), b ->
        {
            this.onUpdated();
        }));
        this.addButton(new Button(x + 80, y + 55, 40, 20, new StringTextComponent("Exit"), b ->
        {
            this.onUpdated();
            this.closeScreen();
        }));

        for (index = 0; index < EditorGui.PAGELIST.size(); index++)
            if (EditorGui.PAGELIST.get(index) == Rewards.class) break;
        final int rewardIndex = index;
        this.addButton(new Button(x - 123, y + 00, 60, 20, new StringTextComponent("rewards"), b ->
        {
            // Change to a rewards page
            this.parent.changePage(rewardIndex);
            if (!(this.parent.current_page instanceof Rewards)) return;
            final Rewards page = (Rewards) this.parent.current_page;
            page.closeCallback = () ->
            {
                this.parent.changePage(ourIndex);
            };
            page.onPageOpened();
        }));
        for (index = 0; index < EditorGui.PAGELIST.size(); index++)
            if (EditorGui.PAGELIST.get(index) == Messages.class) break;
        final int messIndex = index;
        this.addButton(new Button(x - 123, y - 20, 60, 20, new StringTextComponent("messages"), b ->
        {
            // Change to a messages page
            // Change to a ai page
            this.parent.changePage(messIndex);
            if (!(this.parent.current_page instanceof Messages)) return;
            final Messages page = (Messages) this.parent.current_page;
            page.closeCallback = () ->
            {
                this.parent.changePage(ourIndex);
            };
            page.onPageOpened();
        }));
        for (index = 0; index < EditorGui.PAGELIST.size(); index++)
            if (EditorGui.PAGELIST.get(index) == AI.class) break;
        final int aiIndex = index;
        this.addButton(new Button(x - 123, y + 20, 60, 20, new StringTextComponent("ai"), b ->
        {
            // Change to a ai page
            this.parent.changePage(aiIndex);
            if (!(this.parent.current_page instanceof AI)) return;
            final AI page = (AI) this.parent.current_page;
            page.closeCallback = () ->
            {
                this.parent.changePage(ourIndex);
            };
            page.onPageOpened();
        }));
    }

    private void onUpdated()
    {
        final PacketTrainer message = new PacketTrainer(PacketTrainer.UPDATETRAINER);
        message.getTag().putInt("I", this.parent.entity.getEntityId());
        message.getTag().putString("__type__", this.type.getText());
        message.getTag().putString("N", this.name.getText());
        message.getTag().putString("pS", this.playerName.getText());
        message.getTag().putString("uS", this.urlSkin.getText());
        message.getTag().putString("cS", this.customTex.getText());
        message.getTag().putString("cT", this.tradeList.getText());
        message.getTag().putBoolean("rawName", !this.typename);
        message.getTag().putBoolean("G", this.male);
        PacketTrainer.ASSEMBLER.sendToServer(message);
    }

    @Override
    public void render(final MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks)
    {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        final int x = this.parent.width / 2 - 18;
        final int y = this.parent.height / 2 - 72;
        final int dy = 10;
        int i = 0;

        LivingEntity mob;
        if (this.parent.entity instanceof LivingEntity)
        {
            mob = (LivingEntity) this.parent.entity;
            final float yaw = Util.milliTime() / 40;
            GuiPokemobBase.renderMob(mob, x - 60, y + 70, 0, yaw, 0, yaw, 1f);
        }

        this.font.drawString(matrixStack, I18n.format("Trainer Type"), x + 20, y + dy * i++, 0xFFFFFFFF);
        this.font.drawString(matrixStack, I18n.format("Name"), x + 11, y + dy * i++, 0xFFFFFFFF);
        this.font.drawString(matrixStack, I18n.format("Trades List"), x, y + dy * i++, 0xFFFFFFFF);
        this.font.drawString(matrixStack, I18n.format("Player Texture"), x, y + dy * i++, 0xFFFFFFFF);
        this.font.drawString(matrixStack, I18n.format("Custom Texture"), x, y + dy * i++, 0xFFFFFFFF);
    }
}

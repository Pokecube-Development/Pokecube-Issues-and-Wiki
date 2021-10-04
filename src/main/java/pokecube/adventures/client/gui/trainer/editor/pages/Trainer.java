package pokecube.adventures.client.gui.trainer.editor.pages;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import pokecube.adventures.client.gui.trainer.editor.EditorGui;
import pokecube.adventures.client.gui.trainer.editor.pages.util.Page;
import pokecube.adventures.network.PacketTrainer;
import pokecube.core.client.gui.pokemob.GuiPokemobBase;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.npc.NpcType;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.entity.CopyCaps;
import thut.api.entity.ICopyMob;

public class Trainer extends Page
{
    protected static int lastMobIndex = -1;

    EditBox name;
    EditBox type;
    EditBox copyMob;
    EditBox customTex;
    EditBox playerName;

    EditBox tradeList;

    boolean male;
    boolean typename;

    int index = 0;

    public Trainer(final EditorGui parent)
    {
        super(new TextComponent(""), parent);
    }

    @Override
    public void onPageOpened()
    {
        this.children.clear();
        this.renderables.clear();
        super.onPageOpened();
        final int x = this.width / 2;
        final int y = this.height / 2;

        final int dy = -72;
        final int sy = 10;
        int i = 0;
        final int dx = -120;

        this.type = new EditBox(this.font, x + dx, y + dy + sy * i++, 100, 10, new TextComponent(""));
        this.name = new EditBox(this.font, x + dx, y + dy + sy * i++, 100, 10, new TextComponent(""));
        this.tradeList = new EditBox(this.font, x + dx, y + dy + sy * i++, 100, 10, new TextComponent(
                ""));
        this.playerName = new EditBox(this.font, x + dx, y + dy + sy * i++, 100, 10, new TextComponent(
                ""));
        this.customTex = new EditBox(this.font, x + dx, y + dy + sy * i++, 100, 10, new TextComponent(
                ""));
        this.copyMob = new EditBox(this.font, x + dx, y + dy + sy * i++, 100, 10, new TextComponent(""));

        this.copyMob.maxLength = 255;

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
            String name = mob.getNPCName();
            this.male = mob.isMale();
            this.typename = false;
            if (name.contains(":") && name.split(":").length > 1)
            {
                name = name.split(":")[1];
                this.typename = true;
            }
            this.name.setValue(name);
            this.type.setValue(mob.getNpcType().getName());
            for (i = 0; i < types.size(); i++)
                if (NpcType.typeMap.get(types.get(0)) == mob.getNpcType())
                {
                    this.index = i;
                    break;
                }
            this.playerName.setValue(mob.playerName);
            this.customTex.setValue(mob.customTex);
        }

        final ICopyMob copied = CopyCaps.get(this.parent.entity);
        if (copied != null && copied.getCopiedID() != null) this.copyMob.setValue(copied.getCopiedID().toString());

        this.addRenderableWidget(this.name);
        this.addRenderableWidget(this.tradeList);
        this.addRenderableWidget(this.type);
        this.addRenderableWidget(this.playerName);
        this.addRenderableWidget(this.customTex);
        this.addRenderableWidget(this.copyMob);

        if (this.parent.entity instanceof NpcMob) this.tradeList.setValue(((NpcMob) this.parent.entity).customTrades);
        else this.tradeList.setEditable(false);

        // this.addRenderableWidget(this.urlSkin);
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
                        this.parent.entity.getCommandSenderWorld());
                page.index = Trainer.lastMobIndex;
                page.deleteCallback = () ->
                {
                    final PacketTrainer message = new PacketTrainer(PacketTrainer.UPDATEMOB);
                    message.getTag().putInt("I", this.parent.entity.getId());
                    message.getTag().putInt("__trainers__", page.index);
                    PacketTrainer.ASSEMBLER.sendToServer(message);
                    this.onClose();
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
                this.addRenderableWidget(new Button(x + 20 + 50 * (i / 3), y - 10 + 20 * (i % 3), 50, 20, new TextComponent(
                        "mob " + (i + 1)), b ->
                        {
                            this.parent.changePage(pokemobIndex);
                            if (!(this.parent.current_page instanceof Pokemob)) return;
                            final Pokemob page = (Pokemob) this.parent.current_page;
                            page.pokemob = PokecubeManager.itemToPokemob(this.parent.trainer.getPokemob(i2),
                                    this.parent.entity.getCommandSenderWorld());
                            page.index = i2;
                            page.deleteCallback = () ->
                            {
                                final PacketTrainer message = new PacketTrainer(PacketTrainer.UPDATEMOB);
                                message.getTag().putInt("I", this.parent.entity.getId());
                                message.getTag().putInt("__trainers__", page.index);
                                PacketTrainer.ASSEMBLER.sendToServer(message);
                                this.onClose();
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
                this.addRenderableWidget(new Button(x + 20 + 50 * (i2 / 3), y - 10 + 20 * (i2 % 3), 50, 20,
                        new TextComponent("mob +"), b ->
                        {
                            this.parent.changePage(pokemobIndex);
                            if (!(this.parent.current_page instanceof Pokemob)) return;
                            final Pokemob page = (Pokemob) this.parent.current_page;
                            page.index = i2;
                            page.deleteCallback = () ->
                            {
                                this.parent.trainer.setPokemob(0, ItemStack.EMPTY);
                                final PacketTrainer message = new PacketTrainer(PacketTrainer.UPDATEMOB);
                                message.getTag().putInt("I", this.parent.entity.getId());
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

        this.addRenderableWidget(new Button(x - 9, y + dy - 1, 10, 10, new TextComponent(">"), b ->
        {
            this.index++;
            this.index = this.index % types.size();
            this.type.setValue(types.get(this.index));
            this.onUpdated();

        }));
        this.addRenderableWidget(new Button(x - 19, y + dy - 1, 10, 10, new TextComponent("<"), b ->
        {
            this.index--;
            if (this.index < 0) this.index = types.size() - 1;
            this.type.setValue(types.get(this.index));
            this.onUpdated();
        }));
        this.addRenderableWidget(new Button(x - 123, y + 55, 40, 20, new TextComponent("Delete"), b ->
        {
            final PacketTrainer message = new PacketTrainer(PacketTrainer.KILLTRAINER);
            message.getTag().putInt("I", this.parent.entity.getId());
            PacketTrainer.ASSEMBLER.sendToServer(message);
            this.onClose();
        }));
        this.addRenderableWidget(new Button(x - 19, y + dy + 9, 10, 10, new TextComponent(this.male ? "\u2642" : "\u2640"),
                b ->
                {
                    this.male = !this.male;
                    b.setMessage(new TextComponent(this.male ? "\u2642" : "\u2640"));
                    this.onUpdated();
                }));
        this.addRenderableWidget(new Button(x + 60, y - 53, 60, 10, new TextComponent(this.typename ? "Name Prefix"
                : "No Prefix"), b ->
                {
                    this.typename = !this.typename;
                    b.setMessage(new TextComponent(this.typename ? "Name Prefix" : "No Prefix"));
                    this.onUpdated();
                }));
        this.addRenderableWidget(new Button(x + 80, y - 73, 40, 20, new TextComponent("Apply"), b ->
        {
            this.onUpdated();
        }));
        this.addRenderableWidget(new Button(x + 80, y + 55, 40, 20, new TextComponent("Exit"), b ->
        {
            this.onUpdated();
            this.onClose();
        }));

        for (index = 0; index < EditorGui.PAGELIST.size(); index++)
            if (EditorGui.PAGELIST.get(index) == Rewards.class) break;
        final int rewardIndex = index;
        final int yOff = 15;
        this.addRenderableWidget(new Button(x - 123, y + yOff, 60, 20, new TextComponent("rewards"), b ->
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
        this.addRenderableWidget(new Button(x - 123, y + yOff - 20, 60, 20, new TextComponent("messages"), b ->
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
        this.addRenderableWidget(new Button(x - 123, y + yOff + 20, 60, 20, new TextComponent("ai"), b ->
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
        message.getTag().putInt("I", this.parent.entity.getId());
        message.getTag().putString("__type__", this.type.getValue());
        message.getTag().putString("N", this.name.getValue());
        message.getTag().putString("pS", this.playerName.getValue());
        message.getTag().putString("fM", this.copyMob.getValue());
        message.getTag().putString("cS", this.customTex.getValue());
        message.getTag().putString("cT", this.tradeList.getValue());
        message.getTag().putBoolean("rawName", !this.typename);
        message.getTag().putBoolean("G", this.male);
        PacketTrainer.ASSEMBLER.sendToServer(message);
    }

    @Override
    public void render(final PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks)
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
            final float yaw = Util.getMillis() / 40;
            GuiPokemobBase.renderMob(mob, x - 60, y + 80, 0, yaw, 0, yaw, 1f);
        }

        this.font.draw(matrixStack, I18n.get("Trainer Type"), x + 20, y + dy * i++, 0xFFFFFFFF);
        this.font.draw(matrixStack, I18n.get("Name"), x + 11, y + dy * i++, 0xFFFFFFFF);
        this.font.draw(matrixStack, I18n.get("Trades List"), x, y + dy * i++, 0xFFFFFFFF);
        this.font.draw(matrixStack, I18n.get("Player Texture"), x, y + dy * i++, 0xFFFFFFFF);
        this.font.draw(matrixStack, I18n.get("Custom Texture"), x, y + dy * i++, 0xFFFFFFFF);
        this.font.draw(matrixStack, I18n.get("Copied Mob"), x, y + dy * i++, 0xFFFFFFFF);
    }
}

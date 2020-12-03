package pokecube.adventures.client.gui.items.editor.pages;

import java.util.function.Predicate;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.adventures.client.gui.items.editor.EditorGui;
import pokecube.adventures.client.gui.items.editor.pages.util.Page;
import pokecube.adventures.network.PacketTrainer;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.pokemob.GuiPokemobBase;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Nature;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.Tools;

public class Pokemob extends Page
{
    TextFieldWidget name;
    TextFieldWidget type;

    TextFieldWidget nature;
    TextFieldWidget ability;

    TextFieldWidget level;

    TextFieldWidget size;

    TextFieldWidget[] moves = new TextFieldWidget[4];

    TextFieldWidget[] ivs = new TextFieldWidget[6];
    TextFieldWidget[] evs = new TextFieldWidget[6];

    boolean shiny;
    boolean male;

    public int index = -1;

    public IPokemob pokemob;

    public Runnable deleteCallback;

    public Pokemob(final EditorGui parent)
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

        this.type = new TextFieldWidget(this.font, xOffset - 120, yOffset - 55, 100, 10, new StringTextComponent(""));
        this.name = new TextFieldWidget(this.font, xOffset - 120, yOffset - 65, 100, 10, new StringTextComponent(""));
        // this.addButton(this.name);
        this.addButton(this.type);

        final Predicate<String> intValid = input ->
        {
            try
            {
                Integer.parseInt(input);
                return true;
            }
            catch (final NumberFormatException e)
            {
                return input.isEmpty();
            }
        };
        final Predicate<String> floatValid = input ->
        {
            try
            {
                Float.parseFloat(input);
                return true;
            }
            catch (final NumberFormatException e)
            {
                return input.isEmpty();
            }
        };

        for (int i = 0; i < 4; i++)
        {
            this.moves[i] = new TextFieldWidget(this.font, xOffset - 120, yOffset - 30 + i * 10, 70, 10,
                    new StringTextComponent(""));
            this.addButton(this.moves[i]);
        }
        final int evivshiftx = xOffset + 60;
        final int evivshifty = yOffset - 56;
        for (int i = 0; i < 6; i++)
        {
            this.ivs[i] = new TextFieldWidget(this.font, evivshiftx, evivshifty + i * 10, 20, 10,
                    new StringTextComponent(""));
            this.evs[i] = new TextFieldWidget(this.font, evivshiftx + 30, evivshifty + i * 10, 30, 10,
                    new StringTextComponent(""));
            this.ivs[i].setValidator(intValid);
            this.evs[i].setValidator(intValid);
            this.addButton(this.ivs[i]);
            this.addButton(this.evs[i]);
        }
        this.level = new TextFieldWidget(this.font, xOffset - 120, yOffset + 26, 27, 10, new StringTextComponent(""));
        this.level.setValidator(intValid);
        this.addButton(this.level);

        this.size = new TextFieldWidget(this.font, xOffset - 90, yOffset + 26, 50, 10, new StringTextComponent(""));
        this.size.setValidator(floatValid);
        this.addButton(this.size);

        this.ability = new TextFieldWidget(this.font, xOffset - 60, yOffset + 50, 90, 10, new StringTextComponent(""));
        this.nature = new TextFieldWidget(this.font, xOffset - 120, yOffset + 50, 50, 10, new StringTextComponent(""));
        this.addButton(this.ability);
        this.addButton(this.nature);
        this.nature.setEnabled(false);

        for (int i = 0; i < 4; i++)
        {
            String move = "";
            if (this.pokemob != null)
            {
                move = this.pokemob.getMove(i);
                if (move == null) move = "";
            }
            this.moves[i].setText(move);
            this.moves[i].moveCursorBy(-100);
        }
        for (int i = 0; i < 6; i++)
        {
            int value = 0;
            if (this.pokemob != null) value = this.pokemob.getIVs()[i];
            this.ivs[i].setText("" + value);
            value = 0;
            if (this.pokemob != null) value = this.pokemob.getEVs()[i] - Byte.MIN_VALUE;
            this.evs[i].setText("" + value);
        }

        int level = 1;
        float size = 1;
        final byte sexe;
        this.male = false;
        this.shiny = false;
        String nature = "";
        String ability = "";
        String name = "none";
        PokedexEntry entry = null;
        if (this.pokemob != null)
        {
            this.pokemob.onGenesChanged();
            entry = this.pokemob.getPokedexEntry();
            name = entry.getName();
            level = this.pokemob.getLevel();
            nature = this.pokemob.getNature() + "";
            if (this.pokemob.getAbility() != null) ability = this.pokemob.getAbility().toString();
            size = this.pokemob.getSize();
            sexe = this.pokemob.getSexe();
            this.shiny = this.pokemob.isShiny();
        }
        else sexe = 0;
        final String gender = sexe == IPokemob.MALE ? "\u2642" : sexe == IPokemob.FEMALE ? "\u2640" : "o";
        this.nature.setText("" + nature);
        this.ability.setText("" + ability);
        this.size.setText("" + size);
        this.level.setText("" + level);
        this.type.setText(name);

        // Now for the buttons

        final ITextComponent next = new StringTextComponent(">");
        final ITextComponent prev = new StringTextComponent("<");

        this.addButton(new Button(xOffset - 100, yOffset + 62, 12, 12, next, b ->
        {
            if (this.pokemob != null)
            {
                Nature here = this.pokemob.getNature();
                int index = here.ordinal();
                here = Nature.values()[++index % Nature.values().length];
                this.pokemob.setNature(here);
                this.nature.setText("" + here);
                this.nature.moveCursorBy(-100);
                this.onChanged();
            }
        }));
        this.addButton(new Button(xOffset - 112, yOffset + 62, 12, 12, prev, b ->
        {
            Nature here = this.pokemob.getNature();
            int index = here.ordinal();
            if (index == 0) index = Nature.values().length;
            here = Nature.values()[--index % Nature.values().length];
            this.pokemob.setNature(here);
            this.nature.setText("" + here);
            this.nature.moveCursorBy(-100);
            this.onChanged();
        }));

        this.addButton(new Button(xOffset - 40, yOffset + 62, 12, 12, next, b ->
        {
            if (this.pokemob != null)
            {
                int index = this.pokemob.getAbilityIndex() + 1;
                index %= 3;
                this.pokemob.setAbilityIndex(index);
                this.pokemob.setAbility(this.pokemob.getPokedexEntry().getAbility(index, this.pokemob));
                this.ability.setText("" + this.pokemob.getAbility());
                this.pokemob.onGenesChanged();
                this.ability.moveCursorBy(-100);
                this.onChanged();
            }
        }));
        this.addButton(new Button(xOffset - 52, yOffset + 62, 12, 12, prev, b ->
        {
            int index = this.pokemob.getAbilityIndex() - 1;
            if (index < 0) index = 2;
            index %= 3;
            this.pokemob.setAbilityIndex(index);
            this.pokemob.setAbility(this.pokemob.getPokedexEntry().getAbility(index, this.pokemob));
            this.pokemob.onGenesChanged();
            this.ability.setText("" + this.pokemob.getAbility());
            this.ability.moveCursorBy(-100);
            this.onChanged();
        }));

        this.addButton(new Button(xOffset + 25, yOffset + 64, 50, 12, new TranslationTextComponent(
                "traineredit.button.home"), b ->
                {
                    int index = 0;
                    for (index = 0; index < EditorGui.PAGELIST.size(); index++)
                        if (EditorGui.PAGELIST.get(index) == Trainer.class) break;
                    this.parent.changePage(index);
                }));
        this.addButton(new Button(xOffset + 73, yOffset + 64, 50, 12, new TranslationTextComponent(
                "traineredit.button.delete"), b ->
                {
                    this.deleteCallback.run();
                }));

        this.addButton(new Button(xOffset - 132, yOffset - 55, 10, 10, new StringTextComponent(gender), b ->
        {
            if (this.pokemob != null)
            {
                final byte old = this.pokemob.getSexe();
                if (old == IPokemob.MALE || old == IPokemob.FEMALE)
                {
                    final byte newSexe = old == IPokemob.MALE ? IPokemob.FEMALE : IPokemob.MALE;
                    this.pokemob.setSexe(newSexe);
                    final String newgender = newSexe == IPokemob.MALE ? "\u2642"
                            : sexe == IPokemob.FEMALE ? "\u2640" : "o";
                    b.setMessage(new StringTextComponent(newgender));
                    PokecubeCore.LOGGER.debug("Editing Gender");
                    this.onChanged();
                }
            }
        }));
        this.addButton(new Button(xOffset - 30, yOffset - 42, 10, 10, new StringTextComponent(this.shiny ? "Y" : "N"),
                b ->
                {
                    if (this.pokemob != null)
                    {
                        this.shiny = !this.pokemob.isShiny();
                        this.pokemob.setShiny(this.shiny);
                        b.setMessage(new StringTextComponent(this.shiny ? "Y" : "N"));
                        PokecubeCore.LOGGER.debug("Editing Shininess");
                        this.onChanged();
                    }
                }));

    }

    public void onChanged()
    {
        final CompoundNBT info = new CompoundNBT();
        for (int i = 0; i < 4; i++)
        {
            final String move = this.moves[i].getText();
            info.putString("m_" + i, move);
            this.pokemob.setMove(i, move);
        }
        for (int i = 0; i < 6; i++)
        {
            final byte iv = Byte.parseByte(this.ivs[i].getText());
            final byte[] ivs = this.pokemob.getIVs();
            ivs[i] = iv;
            info.putByte("iv_" + i, iv);
            this.pokemob.setIVs(ivs);
            final byte ev = (byte) (Integer.parseInt(this.evs[i].getText()) + Byte.MIN_VALUE);
            final byte[] evs = this.pokemob.getEVs();
            evs[i] = ev;
            info.putByte("ev_" + i, ev);
            this.pokemob.setEVs(evs);
        }
        if (!AbilityManager.abilityExists(this.ability.getText())) this.ability.setText("" + this.pokemob.getAbility());
        else
        {
            this.pokemob.setAbility(AbilityManager.getAbility(this.ability.getText()));
            info.putString("a", this.ability.getText());
        }

        this.pokemob.setExp(Tools.levelToXp(this.pokemob.getExperienceMode(), Integer.parseInt(this.level.getText())),
                false);
        info.putInt("l", Integer.parseInt(this.level.getText()));

        this.pokemob.setSize(Float.parseFloat(this.size.getText()));
        info.putFloat("s", Float.parseFloat(this.size.getText()));

        this.pokemob.setNature(Nature.valueOf(this.nature.getText()));
        info.putString("n", this.nature.getText());

        info.putBoolean("sh", this.shiny);
        info.putBoolean("g", this.male);

        // Case where we are editing a trainer
        if (this.index != -1)
        {
            this.parent.trainer.setPokemob(this.index, PokecubeManager.pokemobToItem(this.pokemob));
            final ItemStack stack = this.parent.trainer.getPokemob(this.index);
            final CompoundNBT tag = new CompoundNBT();
            stack.write(tag);
            final PacketTrainer message = new PacketTrainer(PacketTrainer.UPDATEMOB);
            message.getTag().putInt("I", this.parent.entity.getEntityId());
            message.getTag().putInt("__trainers__", this.index);
            message.getTag().put("__pokemob__", tag);
            PacketTrainer.ASSEMBLER.sendToServer(message);
        }
        else if (this.pokemob.getEntity().addedToChunk)
        {
            final PacketTrainer message = new PacketTrainer(PacketTrainer.UPDATEMOB);
            message.getTag().putInt("I", this.parent.entity.getEntityId());
            message.getTag().put("__pokemob__", info);
            PacketTrainer.ASSEMBLER.sendToServer(message);
        }
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers)
    {
        if (keyCode == GLFW.GLFW_KEY_ENTER && this.pokemob != null)
        {
            this.onChanged();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(final MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks)
    {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        final int x = this.parent.width / 2;
        final int y = this.parent.height / 2 - 70;
        this.font.drawString(matrixStack, I18n.format("traineredit.info.pokemob"), x - 120, y + 5, 0xFFFFFFFF);
        this.font.drawString(matrixStack, I18n.format("traineredit.info.moves"), x - 120, y + 30, 0xFFFFFFFF);
        this.font.drawString(matrixStack, I18n.format("traineredit.info.shiny"), x - 60, y + 30, 0xFFFFFFFF);
        this.font.drawString(matrixStack, I18n.format("traineredit.info.level"), x - 120, y + 85, 0xFFFFFFFF);
        this.font.drawString(matrixStack, I18n.format("traineredit.info.size"), x - 90, y + 85, 0xFFFFFFFF);
        this.font.drawString(matrixStack, I18n.format("traineredit.info.nature"), x - 120, y + 110, 0xFFFFFFFF);
        this.font.drawString(matrixStack, I18n.format("traineredit.info.ability"), x - 60, y + 110, 0xFFFFFFFF);
        this.font.drawString(matrixStack, I18n.format("traineredit.info.evs"), x + 90, y, 0xFFFFFFFF);
        this.font.drawString(matrixStack, I18n.format("traineredit.info.ivs"), x + 60, y, 0xFFFFFFFF);

        if (this.pokemob != null)
        {
            final float yaw = Util.milliTime() / 20;
            final int dx = -50;
            final int dy = +20;
            // Draw the actual pokemob
            GuiPokemobBase.renderMob(this.pokemob.getEntity(), x + dx, y + dy, 0, yaw, 0, yaw, 0.75f);
        }
    }
}

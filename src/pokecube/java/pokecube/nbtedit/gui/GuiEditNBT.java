package pokecube.nbtedit.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import pokecube.nbtedit.NBTStringHelper;
import pokecube.nbtedit.nbt.NamedNBT;
import pokecube.nbtedit.nbt.Node;
import pokecube.nbtedit.nbt.ParseHelper;

import java.util.function.Consumer;

public class GuiEditNBT extends AbstractWidget
{

    public static final ResourceLocation WINDOW_TEXTURE = ResourceLocation.fromNamespaceAndPath("nbtedit",
            "textures/gui/window.png");

    public static final int WIDTH = 178, HEIGHT = 93;

    private static String getValue(final Tag base)
    {
        switch (base.getId())
        {
        case 7:
            StringBuilder s = new StringBuilder();
            for (final byte b : ((ByteArrayTag) base).getAsByteArray())
                s.append(b).append(" ");
            return s.toString();
        case 9:
            return "TagList";
        case 10:
            return "TagCompound";
        case 11:
            StringBuilder i = new StringBuilder();
            for (final int a : ((IntArrayTag) base).getAsIntArray())
                i.append(a).append(" ");
            return i.toString();
        default:
            return NBTStringHelper.toString(base);
        }
    }

    private static void setValidValue(final Node<NamedNBT> node, final String value)
    {
        final NamedNBT named = node.getObject();
        final Tag base = named.getNBT();

        if (base instanceof ByteTag) named.setNBT(ByteTag.valueOf(ParseHelper.parseByte(value)));
        if (base instanceof ShortTag) named.setNBT(ShortTag.valueOf(ParseHelper.parseShort(value)));
        if (base instanceof IntTag) named.setNBT(IntTag.valueOf(ParseHelper.parseInt(value)));
        if (base instanceof LongTag) named.setNBT(LongTag.valueOf(ParseHelper.parseLong(value)));
        if (base instanceof FloatTag) named.setNBT(FloatTag.valueOf(ParseHelper.parseFloat(value)));
        if (base instanceof DoubleTag) named.setNBT(DoubleTag.valueOf(ParseHelper.parseDouble(value)));
        if (base instanceof ByteArrayTag) named.setNBT(new ByteArrayTag(ParseHelper.parseByteArray(value)));
        if (base instanceof IntArrayTag) named.setNBT(new IntArrayTag(ParseHelper.parseIntArray(value)));
        if (base instanceof StringTag) named.setNBT(StringTag.valueOf(value));
    }

    private static void validValue(final String value, final byte type) throws NumberFormatException
    {
        switch (type)
        {
        case 1:
            ParseHelper.parseByte(value);
            break;
        case 2:
            ParseHelper.parseShort(value);
            break;
        case 3:
            ParseHelper.parseInt(value);
            break;
        case 4:
            ParseHelper.parseLong(value);
            break;
        case 5:
            ParseHelper.parseFloat(value);
            break;
        case 6:
            ParseHelper.parseDouble(value);
            break;
        case 7:
            ParseHelper.parseByteArray(value);
            break;
        case 11:
            ParseHelper.parseIntArray(value);
            break;
        }
    }

    private final Minecraft mc = Minecraft.getInstance();
    private final Node<NamedNBT> node;

    private final Tag nbt;

    private final boolean canEditText, canEditValue;
    private final GuiNBTTree parent;

    private TextFieldWidget2 key, value;

    private Button save;
    private Button quit;

    private String kError, vError;

    private GuiCharacterButton newLine, section;

    public GuiEditNBT(final GuiNBTTree parent, final Node<NamedNBT> node, final boolean editText,
            final boolean editValue)
    {
        super((parent.width - GuiEditNBT.WIDTH) / 2, (parent.height - GuiEditNBT.HEIGHT) / 2, GuiEditNBT.WIDTH,
                GuiEditNBT.HEIGHT, Component.literal(""));
        this.parent = parent;
        this.node = node;
        this.nbt = node.getObject().getNBT();
        this.canEditText = editText;
        this.canEditValue = editValue;
    }

    @Override
    public boolean charTyped(final char c, final int i)
    {
        if (i == GLFW.GLFW_KEY_ESCAPE)
        {
            this.parent.closeWindow();
            return true;
        }
        else if (i == GLFW.GLFW_KEY_TAB) return true;
        else if (i == GLFW.GLFW_KEY_ENTER)
        {
            this.checkValidInput();
            if (this.save.active) this.saveAndQuit();
            return true;
        }
        return super.charTyped(c, i);
    }

    private void checkValidInput()
    {
        boolean valid = true;
        this.kError = null;
        this.vError = null;
        if (this.canEditText && !this.validName())
        {
            valid = false;
            this.kError = "Duplicate Tag Name";
        }
        try
        {
            GuiEditNBT.validValue(this.value.getValue(), this.nbt.getId());
        }
        catch (final NumberFormatException e)
        {
            this.vError = e.getMessage();
            valid = false;
        }
        this.save.active = valid;
    }

    public void initGUI(final int x, final int y)
    {
        this.setX(x);
        this.setY(y);

        this.section = this.parent.addTopWidget(
                new GuiCharacterButton((byte) 0, x + GuiEditNBT.WIDTH - 1, y + 34, b -> {
                    this.value.insertText("" + NBTStringHelper.SECTION_SIGN);
                    this.checkValidInput();
                }, supplier -> Component.literal("Section Sign")));
        this.newLine = this.parent.addTopWidget(
                new GuiCharacterButton((byte) 1, x + GuiEditNBT.WIDTH - 1, y + 50, b -> {
                    this.value.insertText("\n");
                    this.checkValidInput();
                }, supplier -> Component.literal("New Line")));
        final String sKey = this.node.getObject().getName();
        final String sValue = GuiEditNBT.getValue(this.nbt);
        this.key = this.parent.addTopWidget(new TextFieldWidget2(this.mc.font, x + 46, y + 18, 116, 15, false));
        this.value = this.parent.addTopWidget(new TextFieldWidget2(this.mc.font, x + 46, y + 44, 116, 15, true));

        this.key.setValue(sKey);
        this.key.setBordered(false);
        this.key.setEditable(this.canEditText);

        this.value.setEditable(this.canEditValue);
        this.value.setMaxLength(256);
        this.value.setValue(sValue);
        this.value.setBordered(false);

        if (!this.key.isFocused() && !this.value.isFocused()) if (this.canEditText) this.key.setFocused(true);
        else if (this.canEditValue) this.value.setFocused(true);

        this.save = this.parent.addTopWidget(
                new Button.Builder(Component.literal("Save"), (b) -> this.saveAndQuit()).bounds(x + 9, y + 62, 75, 20)
                        .build());

        this.quit = this.parent.addTopWidget(
                new Button.Builder(Component.literal("Cancel"), (b) -> this.parent.closeWindow()).bounds(x + 93,
                        y + 62, 75, 20).build());
    }

    @Override
    public boolean mouseClicked(final double x, final double y, final int b)
    {
        this.checkValidInput();
        return super.mouseClicked(x, y, b);
    }

    public void removeParts(Consumer<AbstractWidget> remover)
    {
        remover.accept(this);
        remover.accept(this.quit);
        remover.accept(this.save);
        remover.accept(this.section);
        remover.accept(this.newLine);
        remover.accept(this.value);
        remover.accept(this.key);
    }

    private void saveAndQuit()
    {
        if (this.canEditText) this.node.getObject().setName(this.key.getValue());
        GuiEditNBT.setValidValue(this.node, this.value.getValue());
        this.parent.nodeEdited(this.node);
        this.parent.closeWindow();
    }

    private boolean validName()
    {
        for (final Node<NamedNBT> node : this.node.getParent().getChildren())
        {
            final Tag base = node.getObject().getNBT();
            if (base != this.nbt && node.getObject().getName().equals(this.key.getValue())) return false;
        }
        return true;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int i, int i1, float v)
    {
        this.active = false;

        this.section.active = this.value.isFocused();
        this.newLine.active = this.value.isFocused();

        if (this.value.isFocused()) this.key.setFocused(false);

        graphics.blit(GuiEditNBT.WINDOW_TEXTURE, this.getX(), this.getY(), 0, 0, GuiEditNBT.WIDTH, GuiEditNBT.HEIGHT);

        if (!this.canEditText)
            graphics.fill(this.getX() + 42, this.getY() + 15, this.getX() + 169, this.getY() + 31, 0x80000000);
        if (!this.canEditValue)
            graphics.fill(this.getX() + 42, this.getY() + 41, this.getX() + 169, this.getY() + 57, 0x80000000);
        if (this.kError != null)
            graphics.drawCenteredString(this.mc.font, this.kError, this.getX() + GuiEditNBT.WIDTH / 2, this.getY() + 4,
                    0xFF0000);
        if (this.vError != null)
            graphics.drawCenteredString(this.mc.font, this.vError, this.getX() + GuiEditNBT.WIDTH / 2, this.getY() + 32,
                    0xFF0000);
    }

    @Override
    public void updateWidgetNarration(final NarrationElementOutput output)
    {
        // TODO Auto-generated method stub
    }

}

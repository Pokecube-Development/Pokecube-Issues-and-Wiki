package pokecube.nbtedit.gui;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.nbt.ShortNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import pokecube.nbtedit.NBTStringHelper;
import pokecube.nbtedit.nbt.NamedNBT;
import pokecube.nbtedit.nbt.Node;
import pokecube.nbtedit.nbt.ParseHelper;

public class GuiEditNBT extends Widget
{

    public static final ResourceLocation WINDOW_TEXTURE = new ResourceLocation("nbtedit", "textures/gui/window.png");

    public static final int              WIDTH          = 178, HEIGHT = 93;

    private static String getValue(final INBT base)
    {
        switch (base.getId())
        {
        case 7:
            String s = "";
            for (final byte b : ((ByteArrayNBT) base).getAsByteArray())
                s += b + " ";
            return s;
        case 9:
            return "TagList";
        case 10:
            return "TagCompound";
        case 11:
            String i = "";
            for (final int a : ((IntArrayNBT) base).getAsIntArray())
                i += a + " ";
            return i;
        default:
            return NBTStringHelper.toString(base);
        }
    }

    private static void setValidValue(final Node<NamedNBT> node, final String value)
    {
        final NamedNBT named = node.getObject();
        final INBT base = named.getNBT();

        if (base instanceof ByteNBT) named.setNBT(ByteNBT.valueOf(ParseHelper.parseByte(value)));
        if (base instanceof ShortNBT) named.setNBT(ShortNBT.valueOf(ParseHelper.parseShort(value)));
        if (base instanceof IntNBT) named.setNBT(IntNBT.valueOf(ParseHelper.parseInt(value)));
        if (base instanceof LongNBT) named.setNBT(LongNBT.valueOf(ParseHelper.parseLong(value)));
        if (base instanceof FloatNBT) named.setNBT(FloatNBT.valueOf(ParseHelper.parseFloat(value)));
        if (base instanceof DoubleNBT) named.setNBT(DoubleNBT.valueOf(ParseHelper.parseDouble(value)));
        if (base instanceof ByteArrayNBT) named.setNBT(new ByteArrayNBT(ParseHelper.parseByteArray(value)));
        if (base instanceof IntArrayNBT) named.setNBT(new IntArrayNBT(ParseHelper.parseIntArray(value)));
        if (base instanceof StringNBT) named.setNBT(StringNBT.valueOf(value));
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

    private final Minecraft      mc = Minecraft.getInstance();
    private final Node<NamedNBT> node;

    private final INBT           nbt;

    private final boolean        canEditText, canEditValue;
    private final GuiNBTTree     parent;

    private TextFieldWidget2     key, value;

    private Button               save;

    private String               kError, vError;

    private GuiCharacterButton   newLine, section;

    public GuiEditNBT(final GuiNBTTree parent, final Node<NamedNBT> node, final boolean editText,
            final boolean editValue)
    {
        super((parent.width - GuiEditNBT.WIDTH) / 2, (parent.height - GuiEditNBT.HEIGHT) / 2, GuiEditNBT.WIDTH,
                GuiEditNBT.HEIGHT, new StringTextComponent(""));
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
            valid &= true;
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
        this.x = x;
        this.y = y;
        this.parent.addButton(this.section = new GuiCharacterButton((byte) 0, x + GuiEditNBT.WIDTH - 1, y + 34, b ->
        {
            this.value.insertText("" + NBTStringHelper.SECTION_SIGN);
            this.checkValidInput();
        }));
        this.parent.addButton(this.newLine = new GuiCharacterButton((byte) 1, x + GuiEditNBT.WIDTH - 1, y + 50, b ->
        {
            this.value.insertText("\n");
            this.checkValidInput();
        }));
        final String sKey = this.node.getObject().getName();
        final String sValue = GuiEditNBT.getValue(this.nbt);
        this.parent.addButton(this.key = new TextFieldWidget2(this.mc.font, x + 46, y + 18, 116, 15, false));
        this.parent.addButton(this.value = new TextFieldWidget2(this.mc.font, x + 46, y + 44, 116, 15, true));

        this.key.setValue(sKey);
        this.key.setBordered(false);
        this.key.isEditable = this.canEditText;

        this.value.isEditable = this.canEditValue;
        this.value.setMaxLength(256);
        this.value.setValue(sValue);
        this.value.setBordered(false);

        if (!this.key.isFocused() && !this.value.isFocused()) if (this.canEditText) this.key.setFocused(true);
        else if (this.canEditValue) this.value.setFocused(true);

        this.parent.addButton(this.save = new Button(x + 9, y + 62, 75, 20, new StringTextComponent("Save"), b -> this.saveAndQuit()));
        this.parent.addButton(new Button(x + 93, y + 62, 75, 20, new StringTextComponent("Cancel"), b -> this.parent.closeWindow()));
    }

    @Override
    public boolean mouseClicked(final double p_mouseClicked_1_, final double p_mouseClicked_3_,
            final int p_mouseClicked_5_)
    {
        this.checkValidInput();
        return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
    }

    @Override
    public void render(final MatrixStack mat, final int mx, final int my, final float m)
    {
        this.active = false;

        this.section.active = this.value.isFocused();
        this.newLine.active = this.value.isFocused();
        this.mc.getTextureManager().bind(GuiEditNBT.WINDOW_TEXTURE);

        GL11.glColor4f(1, 1, 1, 1);
        this.blit(mat, this.x, this.y, 0, 0, GuiEditNBT.WIDTH, GuiEditNBT.HEIGHT);
        if (!this.canEditText) AbstractGui.fill(mat, this.x + 42, this.y + 15, this.x + 169, this.y + 31, 0x80000000);
        if (!this.canEditValue) AbstractGui.fill(mat, this.x + 42, this.y + 41, this.x + 169, this.y + 57, 0x80000000);

        if (this.kError != null) AbstractGui.drawCenteredString(mat,this.mc.font, this.kError,
                this.x + GuiEditNBT.WIDTH / 2, this.y + 4, 0xFF0000);
        if (this.vError != null) AbstractGui.drawCenteredString(mat, this.mc.font, this.vError,
                this.x + GuiEditNBT.WIDTH / 2, this.y + 32, 0xFF0000);
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
            final INBT base = node.getObject().getNBT();
            if (base != this.nbt && node.getObject().getName().equals(this.key.getValue())) return false;
        }
        return true;
    }

}

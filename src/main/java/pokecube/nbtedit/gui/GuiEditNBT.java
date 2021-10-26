package pokecube.nbtedit.gui;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
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
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import pokecube.nbtedit.NBTStringHelper;
import pokecube.nbtedit.nbt.NamedNBT;
import pokecube.nbtedit.nbt.Node;
import pokecube.nbtedit.nbt.ParseHelper;

public class GuiEditNBT extends AbstractWidget
{

    public static final ResourceLocation WINDOW_TEXTURE = new ResourceLocation("nbtedit", "textures/gui/window.png");

    public static final int              WIDTH          = 178, HEIGHT = 93;

    private static String getValue(final Tag base)
    {
        switch (base.getId())
        {
        case 7:
            String s = "";
            for (final byte b : ((ByteArrayTag) base).getAsByteArray())
                s += b + " ";
            return s;
        case 9:
            return "TagList";
        case 10:
            return "TagCompound";
        case 11:
            String i = "";
            for (final int a : ((IntArrayTag) base).getAsIntArray())
                i += a + " ";
            return i;
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

    private final Minecraft      mc = Minecraft.getInstance();
    private final Node<NamedNBT> node;

    private final Tag           nbt;

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
                GuiEditNBT.HEIGHT, new TextComponent(""));
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
        this.parent.addRenderableWidget(this.section = new GuiCharacterButton((byte) 0, x + GuiEditNBT.WIDTH - 1, y + 34, b ->
        {
            this.value.insertText("" + NBTStringHelper.SECTION_SIGN);
            this.checkValidInput();
        }));
        this.parent.addRenderableWidget(this.newLine = new GuiCharacterButton((byte) 1, x + GuiEditNBT.WIDTH - 1, y + 50, b ->
        {
            this.value.insertText("\n");
            this.checkValidInput();
        }));
        final String sKey = this.node.getObject().getName();
        final String sValue = GuiEditNBT.getValue(this.nbt);
        this.parent.addRenderableWidget(this.key = new TextFieldWidget2(this.mc.font, x + 46, y + 18, 116, 15, false));
        this.parent.addRenderableWidget(this.value = new TextFieldWidget2(this.mc.font, x + 46, y + 44, 116, 15, true));

        this.key.setValue(sKey);
        this.key.setBordered(false);
        this.key.isEditable = this.canEditText;

        this.value.isEditable = this.canEditValue;
        this.value.setMaxLength(256);
        this.value.setValue(sValue);
        this.value.setBordered(false);

        if (!this.key.isFocused() && !this.value.isFocused()) if (this.canEditText) this.key.setFocused(true);
        else if (this.canEditValue) this.value.setFocused(true);

        this.parent.addRenderableWidget(this.save = new Button(x + 9, y + 62, 75, 20, new TextComponent("Save"), b -> this.saveAndQuit()));
        this.parent.addRenderableWidget(new Button(x + 93, y + 62, 75, 20, new TextComponent("Cancel"), b -> this.parent.closeWindow()));
    }

    @Override
    public boolean mouseClicked(final double p_mouseClicked_1_, final double p_mouseClicked_3_,
            final int p_mouseClicked_5_)
    {
        this.checkValidInput();
        return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
    }

    @Override
    public void render(final PoseStack mat, final int mx, final int my, final float m)
    {
        this.active = false;

        this.section.active = this.value.isFocused();
        this.newLine.active = this.value.isFocused();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GuiEditNBT.WINDOW_TEXTURE);

        this.blit(mat, this.x, this.y, 0, 0, GuiEditNBT.WIDTH, GuiEditNBT.HEIGHT);
        if (!this.canEditText) GuiComponent.fill(mat, this.x + 42, this.y + 15, this.x + 169, this.y + 31, 0x80000000);
        if (!this.canEditValue) GuiComponent.fill(mat, this.x + 42, this.y + 41, this.x + 169, this.y + 57, 0x80000000);

        if (this.kError != null) GuiComponent.drawCenteredString(mat,this.mc.font, this.kError,
                this.x + GuiEditNBT.WIDTH / 2, this.y + 4, 0xFF0000);
        if (this.vError != null) GuiComponent.drawCenteredString(mat, this.mc.font, this.vError,
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
            final Tag base = node.getObject().getNBT();
            if (base != this.nbt && node.getObject().getName().equals(this.key.getValue())) return false;
        }
        return true;
    }

    @Override
    public void updateNarration(final NarrationElementOutput p_169152_)
    {
        // TODO Auto-generated method stub

    }

}

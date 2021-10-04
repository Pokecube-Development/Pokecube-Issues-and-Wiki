package pokecube.nbtedit.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import pokecube.nbtedit.NBTEdit;
import pokecube.nbtedit.NBTStringHelper;
import pokecube.nbtedit.nbt.NBTTree;
import pokecube.nbtedit.nbt.NamedNBT;
import pokecube.nbtedit.nbt.Node;
import pokecube.nbtedit.nbt.SaveStates;

/*
 * The main Gui class for NBTEdit. This implementation is messy, naive, and
 * unoptimized, but it works. This is from long before GuiLib (and is actually
 * my motivation for GuiLib), but sadly I do not have time to rewrite it.
 * Issues: - Not extensible - a separate tree GUI class for GuiLib would be
 * nice. - Naive/unoptimized - layout changes force an entire reload of the tree
 * - Messy, good luck. Some of the button IDs are hardcoded.
 */
public class GuiNBTTree extends Screen
{

    private final Minecraft           mc      = Minecraft.getInstance();

    private final NBTTree             tree;
    private final List<GuiNBTNode>    nodes;
    private final GuiSaveSlotButton[] saves;
    private final GuiNBTButton[]      nbtButtons;

    private final int                 X_GAP   = 10, START_X = 10;

    final int                         START_Y = 30;
    private final int                 Y_GAP   = Minecraft.getInstance().font.lineHeight + 2;

    private int                       y, yClick;

    int                               bottom;

    int                               width;

    int                               height;

    private int                       heightDiff;

    private int                       offset;

    private Node<NamedNBT>            focused;
    private int                       focusedSlotIndex;
    public boolean                    reInit  = false;

    private GuiEditNBT                window;

    public GuiNBTTree(final NBTTree tree)
    {
        super(null);
        this.tree = tree;
        this.yClick = -1;
        this.focusedSlotIndex = -1;
        this.nodes = new ArrayList<>();
        this.nbtButtons = new GuiNBTButton[16];
        this.saves = new GuiSaveSlotButton[7];
    }

    private void addButtons()
    {
        int x = 18, y = 4;

        for (byte i = 14; i < 17; ++i)
        {
            this.nbtButtons[i - 1] = new GuiNBTButton(i, x, y, b -> this.buttonClicked((GuiNBTButton) b));
            this.addRenderableWidget(this.nbtButtons[i - 1]);
            x += 15;
        }

        x += 30;
        for (byte i = 12; i < 14; ++i)
        {
            this.nbtButtons[i - 1] = new GuiNBTButton(i, x, y, b -> this.buttonClicked((GuiNBTButton) b));
            this.addRenderableWidget(this.nbtButtons[i - 1]);
            x += 15;
        }

        x = 18;
        y = 17;
        for (byte i = 1; i < 12; ++i)
        {
            this.nbtButtons[i - 1] = new GuiNBTButton(i, x, y, b -> this.buttonClicked((GuiNBTButton) b));
            this.addRenderableWidget(this.nbtButtons[i - 1]);
            x += 9;
        }
    }

    private void addNodes(final Node<NamedNBT> node, int x)
    {
        final int dx = node.hasChildren() ? 10 : 0;
        final GuiNBTNode nbtNode = new GuiNBTNode(this, node, x - dx, this.y);
        this.nodes.add(nbtNode);
        this.addRenderableWidget(nbtNode);
        x += this.X_GAP;
        this.y += this.Y_GAP;
        if (node.shouldDrawChildren()) for (final Node<NamedNBT> child : node.getChildren())
            this.addNodes(child, x);
    }

    private void addSaveSlotButtons()
    {
        final SaveStates saveStates = NBTEdit.getSaveStates();
        for (int i = 0; i < 7; ++i)
        {
            this.saves[i] = new GuiSaveSlotButton(saveStates.getSaveState(i), this.width - 24, 31 + i * 25, b ->
            {
                final GuiSaveSlotButton button = (GuiSaveSlotButton) b;
                button.reset();
                NBTEdit.getSaveStates().save();
                this.mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return;
            });
            this.addRenderableWidget(this.saves[i]);
        }
    }

    public boolean arrowKeyPressed(final boolean up)
    {
        if (this.focused == null) this.shift(up ? this.Y_GAP : -this.Y_GAP);
        else
        {
            this.shiftFocus(up);
            return true;
        }
        ;
        return false;
    }

    private void buttonClicked(final GuiNBTButton button)
    {
        if (this.window != null) return;
        final boolean hasFocus = this.focused != null;
        if (!hasFocus) return;
        if (button.getId() == 16) this.paste();
        else if (button.getId() == 15) this.cut();
        else if (button.getId() == 14) this.copy();
        else if (button.getId() == 13) this.deleteSelected();
        else if (button.getId() == 12) this.edit();
        else
        {
            this.focused.setDrawChildren(true);
            final List<Node<NamedNBT>> children = this.focused.getChildren();
            final String type = NBTStringHelper.getButtonName(button.getId());

            if (this.focused.getObject().getNBT() instanceof ListTag)
            {
                final Tag nbt = NBTStringHelper.newTag(button.getId());
                if (nbt != null)
                {
                    final Node<NamedNBT> newNode = new Node<>(this.focused, new NamedNBT("", nbt));
                    children.add(newNode);
                    this.setFocusedNode(newNode);
                }
            }
            else if (children.size() == 0) this.setFocusedNode(this.insert(type + "1", button.getId()));
            else for (int i = 1; i <= children.size() + 1; ++i)
            {
                final String name = type + i;
                if (this.validName(name, children))
                {
                    this.setFocusedNode(this.insert(name, button.getId()));
                    break;
                }
            }
            this.initGUI(true);
        }
    }

    private boolean canAddToParent(final Tag parent, final Tag child)
    {
        if (parent instanceof CompoundTag) return true;
        if (parent instanceof ListTag)
        {
            final ListTag list = (ListTag) parent;
            return list.size() == 0 || list.getElementType() == child.getId();
        }
        return false;
    }

    private boolean canPaste()
    {
        return NBTEdit.clipboard != null && this.focused != null
                && this.canAddToParent(this.focused.getObject().getNBT(), NBTEdit.clipboard.getNBT());
    }

    @Override
    public boolean charTyped(final char ch, final int key)
    {
        if (this.focusedSlotIndex != -1)
        {
            this.saves[this.focusedSlotIndex].charTyped(ch, key);
            return true;
        }
        else
        {
            if (key == GLFW.GLFW_KEY_C && Screen.hasControlDown()) return this.copy();
            if (key == GLFW.GLFW_KEY_V && Screen.hasControlDown() && this.canPaste()) return this.paste();
            if (key == GLFW.GLFW_KEY_X && Screen.hasControlDown()) return this.cut();

        }
        return super.charTyped(ch, key);
    }

    private boolean checkValidFocus(final Node<NamedNBT> fc)
    {
        for (final GuiNBTNode node : this.nodes)
            if (node.getNode() == fc)
            {
                this.setFocusedNode(fc);
                return true;
            }
        return fc.hasParent() && this.checkValidFocus(fc.getParent());
    }

    public void closeWindow()
    {
        this.children.remove(this.window);
        this.renderables.remove(this.window);
        this.window = null;
        this.initGUI();
    }

    private boolean copy()
    {
        if (this.focused != null)
        {
            final NamedNBT namedNBT = this.focused.getObject();
            if (namedNBT.getNBT() instanceof ListTag)
            {
                final ListTag list = new ListTag();
                this.tree.addChildrenToList(this.focused, list);
                NBTEdit.clipboard = new NamedNBT(namedNBT.getName(), list);
            }
            else if (namedNBT.getNBT() instanceof CompoundTag)
            {
                final CompoundTag compound = new CompoundTag();
                this.tree.addChildrenToTag(this.focused, compound);
                NBTEdit.clipboard = new NamedNBT(namedNBT.getName(), compound);
            }
            else NBTEdit.clipboard = this.focused.getObject().copy();
            this.setFocusedNode(this.focused);
            return true;
        }
        return false;
    }

    private boolean cut()
    {
        this.copy();
        this.deleteSelected();
        return true;
    }

    public boolean deleteSelected()
    {
        if (this.focused != null) if (this.tree.delete(this.focused))
        {
            final Node<NamedNBT> oldFocused = this.focused;
            this.shiftFocus(true);
            if (this.focused == oldFocused) this.setFocusedNode(null);
            this.initGUI();
            return true;
        }
        return false;
    }

    private void drawScrollBar(final PoseStack mat, final int mx, final int my)
    {
        if (this.heightDiff > 0)
        {

            if (Minecraft.getInstance().mouseHandler.isLeftPressed())
            {
                if (this.yClick == -1)
                {
                    if (mx >= this.width - 20 && mx < this.width && my >= this.START_Y - 1 && my < this.bottom)
                        this.yClick = my;
                }
                else
                {
                    float scrollMultiplier = 1.0F;
                    int height = this.getHeightDifference();

                    if (height < 1) height = 1;
                    int length = (this.bottom - (this.START_Y - 1)) * (this.bottom - (this.START_Y - 1))
                            / this.getContentHeight();
                    if (length < 32) length = 32;
                    if (length > this.bottom - (this.START_Y - 1) - 8) length = this.bottom - (this.START_Y - 1) - 8;

                    scrollMultiplier /= (float) (this.bottom - (this.START_Y - 1) - length) / (float) height;

                    this.shift((int) ((this.yClick - my) * scrollMultiplier));
                    this.yClick = my;
                }
            }
            else this.yClick = -1;

            GuiComponent.fill(mat, this.width - 20, this.START_Y - 1, this.width, this.bottom, Integer.MIN_VALUE);

            int length = (this.bottom - (this.START_Y - 1)) * (this.bottom - (this.START_Y - 1))
                    / this.getContentHeight();
            if (length < 32) length = 32;
            if (length > this.bottom - (this.START_Y - 1) - 8) length = this.bottom - (this.START_Y - 1) - 8;
            int y = -this.offset * (this.bottom - (this.START_Y - 1) - length) / this.heightDiff + this.START_Y - 1;

            if (y < this.START_Y - 1) y = this.START_Y - 1;

            this.fillGradient(mat, this.width - 20, y, this.width, y + length, 0x80ffffff, 0x80333333);
        }
    }

    private void edit()
    {
        final Tag base = this.focused.getObject().getNBT();
        final Tag parent = this.focused.getParent().getObject().getNBT();
        this.addRenderableWidget(this.window = new GuiEditNBT(this, this.focused, !(parent instanceof ListTag),
                !(base instanceof CompoundTag || base instanceof ListTag)));
        this.window.initGUI((this.width - GuiEditNBT.WIDTH) / 2, (this.height - GuiEditNBT.HEIGHT) / 2);
    }

    public boolean editSelected()
    {
        if (this.focused != null)
        {
            final Tag base = this.focused.getObject().getNBT();
            if (this.focused.hasChildren() && (base instanceof CompoundTag || base instanceof ListTag))
            {
                this.focused.setDrawChildren(!this.focused.shouldDrawChildren());
                int index;

                if (this.focused.shouldDrawChildren() && (index = this.indexOf(this.focused)) != -1)
                    this.offset = this.START_Y + 1 - this.nodes.get(index).y + this.offset;

                this.initGUI();
                return true;
            }
            else if (this.nbtButtons[11].active)
            {
                this.edit();
                return true;
            }
        }
        else if (this.focusedSlotIndex != -1)
        {
            this.stopEditingSlot();
            return true;
        }
        return false;
    }

    public Node<NamedNBT> focusedNode()
    {
        return this.focused;
    }

    private int getContentHeight()
    {
        return this.Y_GAP * this.nodes.size();
    }

    public GuiSaveSlotButton getFocusedSaveSlot()
    {
        return this.focusedSlotIndex != -1 ? this.saves[this.focusedSlotIndex] : null;
    }

    private int getHeightDifference()
    {
        return this.getContentHeight() - (this.bottom - this.START_Y + 2);
    }

    public NBTTree getNBTTree()
    {
        return this.tree;
    }

    public GuiEditNBT getWindow()
    {
        return this.window;
    }

    private int indexOf(final Node<NamedNBT> node)
    {
        for (int i = 0; i < this.nodes.size(); ++i)
            if (this.nodes.get(i).getNode() == node) return i;
        return -1;
    }

    public void initGUI()
    {
        this.initGUI(false);
    }

    public void initGUI(final boolean shiftToFocused)
    {
        this.children.clear();
        this.renderables.clear();
        this.y = this.START_Y;
        this.nodes.clear();

        if (this.window != null)
        {
            @SuppressWarnings("unchecked")
            final List<GuiEventListener> list = (List<GuiEventListener>) this.children();
            list.add(this.window);
        }

        this.addNodes(this.tree.getRoot(), this.START_X);
        this.addButtons();
        this.addSaveSlotButtons();
        if (this.focused != null) if (!this.checkValidFocus(this.focused)) this.setFocusedNode(null);
        if (this.focusedSlotIndex != -1) this.saves[this.focusedSlotIndex].startEditing();
        this.heightDiff = this.getHeightDifference();
        if (this.heightDiff <= 0) this.offset = 0;
        else
        {
            if (this.offset < -this.heightDiff) this.offset = -this.heightDiff;
            if (this.offset > 0) this.offset = 0;
            for (final GuiNBTNode node : this.nodes)
                node.shift(this.offset);
            if (shiftToFocused && this.focused != null) this.shiftTo(this.focused);
        }
    }

    public void initGUI(final int width, final int height, final int bottom)
    {
        this.width = width;
        this.height = height;
        this.bottom = bottom;
        this.yClick = -1;
        this.initGUI(false);
        if (this.window != null) this.window.initGUI((width - GuiEditNBT.WIDTH) / 2, (height - GuiEditNBT.HEIGHT) / 2);
    }

    private Node<NamedNBT> insert(final NamedNBT nbt)
    {
        final Node<NamedNBT> newNode = new Node<>(this.focused, nbt);

        if (this.focused.hasChildren())
        {
            final List<Node<NamedNBT>> children = this.focused.getChildren();

            boolean added = false;
            for (int i = 0; i < children.size(); ++i)
                if (NBTEdit.SORTER.compare(newNode, children.get(i)) < 0)
                {
                    children.add(i, newNode);
                    added = true;
                    break;
                }
            if (!added) children.add(newNode);
        }
        else this.focused.addChild(newNode);
        return newNode;
    }

    private Node<NamedNBT> insert(final String name, final byte type)
    {
        final Tag nbt = NBTStringHelper.newTag(type);
        if (nbt != null) return this.insert(new NamedNBT(name, nbt));
        return null;
    }

    public boolean isEditingSlot()
    {
        return this.focusedSlotIndex != -1;
    }

    @Override
    public boolean mouseClicked(final double x, final double y, final int m)
    {
        if (m == 1) return this.rightClick(x, y, m);
        final boolean superClick = super.mouseClicked(x, y, m);
        if (this.reInit)
        {
            this.initGUI();
            this.reInit = false;
            return true;
        }
        return superClick;
    }

    public void nodeClicked(final GuiNBTNode node)
    {
        if (this.window == null)
        {
            this.reInit = false;
            if (node.hideShowClicked())
            { // Check hide/show children buttons
                this.reInit = true;
                if (node.shouldDrawChildren()) this.offset = this.START_Y + 1 - node.y + this.offset;
            }
            if (!this.reInit)
            { // Check actual nodes, remove focus if nothing clicked
                Node<NamedNBT> newFocus = null;
                newFocus = node.getNode();
                if (this.focusedSlotIndex != -1) this.stopEditingSlot();
                this.setFocusedNode(newFocus);
            }
        }
    }

    public void nodeEdited(final Node<NamedNBT> node)
    {
        final Node<NamedNBT> parent = node.getParent();
        Collections.sort(parent.getChildren(), NBTEdit.SORTER);
        this.initGUI(true);
    }

    protected void overlayBackground(final int par1, final int par2, final int par3, final int par4)
    {
        final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder worldRenderer = tessellator.getBuilder();
        this.mc.getTextureManager().bindForSetup(GuiComponent.BACKGROUND_LOCATION);
        final float var6 = 32.0F;
        worldRenderer.begin(7, DefaultVertexFormat.POSITION_COLOR_TEX);
        final Color color = new Color(4210752);
        final int r = color.getRed();
        final int g = color.getRed();
        final int b = color.getRed();
        final int a = par4;
        worldRenderer.vertex(0.0D, par2, 0.0D).color(r, g, b, a).uv(0.0f, par2 / var6).endVertex();
        worldRenderer.vertex(this.width, par2, 0.0D).color(r, g, b, a).uv(this.width / var6, par2 / var6).endVertex();
        worldRenderer.vertex(this.width, par1, 0.0D).color(r, g, b, a).uv(this.width / var6, par1 / var6).endVertex();
        worldRenderer.vertex(0.0D, par1, 0.0D).color(r, g, b, a).uv(0.0f, par1 / var6).endVertex();
        tessellator.end();
    }

    private boolean paste()
    {
        if (NBTEdit.clipboard != null)
        {
            this.focused.setDrawChildren(true);

            final NamedNBT namedNBT = NBTEdit.clipboard.copy();
            if (this.focused.getObject().getNBT() instanceof ListTag)
            {
                namedNBT.setName("");
                final Node<NamedNBT> node = new Node<>(this.focused, namedNBT);
                this.focused.addChild(node);
                this.tree.addChildrenToTree(node);
                this.tree.sort(node);
                this.setFocusedNode(node);
            }
            else
            {
                final String name = namedNBT.getName();
                final List<Node<NamedNBT>> children = this.focused.getChildren();
                if (!this.validName(name, children)) for (int i = 1; i <= children.size() + 1; ++i)
                {
                    final String n = name + "(" + i + ")";
                    if (this.validName(n, children))
                    {
                        namedNBT.setName(n);
                        break;
                    }
                }
                final Node<NamedNBT> node = this.insert(namedNBT);
                this.tree.addChildrenToTree(node);
                this.tree.sort(node);
                this.setFocusedNode(node);
            }

            this.initGUI(true);
            return true;
        }
        return false;
    }

    @Override
    public void render(final PoseStack mat,final int mx, final int my, final float ticks)
    {
        int cmx = mx, cmy = my;
        if (this.window != null)
        {
            cmx = -1;
            cmy = -1;
        }
        this.overlayBackground(0, this.START_Y - 1, 255, 255);
        this.overlayBackground(this.bottom, this.height, 255, 255);
        super.render(mat,mx, my, ticks);
        // Render the tooltips after, so they don't get hidden by other buttond
        for (final GuiNBTButton button : this.nbtButtons)
            button.renderToolTip(mat,my, my);
        this.drawScrollBar(mat,cmx, cmy);
    }

    public boolean rightClick(final double x, final double y2, final int t)
    {
        for (int i = 0; i < 7; ++i)
            if (this.saves[i].isHovered())
            {
                this.setFocusedNode(null);
                if (this.focusedSlotIndex != -1) if (this.focusedSlotIndex != i)
                {
                    this.saves[this.focusedSlotIndex].stopEditing();
                    NBTEdit.getSaveStates().save();
                    return true;
                }
                else // Already editing the correct one!
                    return false;
                this.saves[i].startEditing();
                this.focusedSlotIndex = i;
                return true;
            }
        return false;
    }

    private void setFocusedNode(final Node<NamedNBT> toFocus)
    {
        if (toFocus == null) for (final GuiNBTButton b : this.nbtButtons)
            b.active = false;
        else if (toFocus.getObject().getNBT() instanceof CompoundTag)
        {
            for (final GuiNBTButton b : this.nbtButtons)
                b.active = true;
            this.nbtButtons[12].active = toFocus != this.tree.getRoot();
            this.nbtButtons[11].active = toFocus.hasParent()
                    && !(toFocus.getParent().getObject().getNBT() instanceof ListTag);
            this.nbtButtons[13].active = true;
            this.nbtButtons[14].active = toFocus != this.tree.getRoot();
            this.nbtButtons[15].active = NBTEdit.clipboard != null;
        }
        else if (toFocus.getObject().getNBT() instanceof ListTag)
        {
            if (toFocus.hasChildren())
            {
                final byte type = toFocus.getChildren().get(0).getObject().getNBT().getId();
                for (final GuiNBTButton b : this.nbtButtons)
                    b.active = false;
                this.nbtButtons[type - 1].active = true;
                this.nbtButtons[12].active = true;
                this.nbtButtons[11].active = !(toFocus.getParent().getObject().getNBT() instanceof ListTag);
                this.nbtButtons[13].active = true;
                this.nbtButtons[14].active = true;
                this.nbtButtons[15].active = NBTEdit.clipboard != null && NBTEdit.clipboard.getNBT().getId() == type;
            }
            else for (final GuiNBTButton b : this.nbtButtons)
                b.active = true;
            this.nbtButtons[11].active = !(toFocus.getParent().getObject().getNBT() instanceof ListTag);
            this.nbtButtons[13].active = true;
            this.nbtButtons[14].active = true;
            this.nbtButtons[15].active = NBTEdit.clipboard != null;
        }
        else
        {
            for (final GuiNBTButton b : this.nbtButtons)
                b.active = false;
            this.nbtButtons[12].active = true;
            this.nbtButtons[11].active = true;
            this.nbtButtons[13].active = true;
            this.nbtButtons[14].active = true;
            this.nbtButtons[15].active = false;
        }

        this.focused = toFocus;
        if (this.focused != null && this.focusedSlotIndex != -1) this.stopEditingSlot();
    }

    public void shift(final int i)
    {
        if (this.heightDiff <= 0 || this.window != null) return;
        int dif = this.offset + i;
        if (dif > 0) dif = 0;
        if (dif < -this.heightDiff) dif = -this.heightDiff;
        for (final GuiNBTNode node : this.nodes)
            node.shift(dif - this.offset);
        this.offset = dif;
    }

    private void shiftFocus(final boolean up)
    {
        int index = this.indexOf(this.focused);
        if (index != -1)
        {
            index += up ? -1 : 1;
            if (index >= 0 && index < this.nodes.size())
            {
                this.setFocusedNode(this.nodes.get(index).getNode());
                this.shift(up ? this.Y_GAP : -this.Y_GAP);
            }
        }
    }

    private void shiftTo(final Node<NamedNBT> node)
    {
        final int index = this.indexOf(node);
        if (index != -1)
        {
            final GuiNBTNode gui = this.nodes.get(index);
            this.shift((this.bottom + this.START_Y + 1) / 2 - (gui.y + gui.getHeight()));
        }
    }

    public boolean stopEditingSlot()
    {
        this.saves[this.focusedSlotIndex].stopEditing();
        NBTEdit.getSaveStates().save();
        this.focusedSlotIndex = -1;
        return true;
    }

    @Override
    public void tick()
    {
        if (this.focusedSlotIndex != -1) this.saves[this.focusedSlotIndex].update();
    }

    private boolean validName(final String name, final List<Node<NamedNBT>> list)
    {
        for (final Node<NamedNBT> node : list)
            if (node.getObject().getName().equals(name)) return false;
        return true;
    }

}

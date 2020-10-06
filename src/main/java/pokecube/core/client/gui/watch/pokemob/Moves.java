package pokecube.core.client.gui.watch.pokemob;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.PokemobInfoPage;
import pokecube.core.client.gui.watch.util.LineEntry;
import pokecube.core.client.gui.watch.util.LineEntry.IClickListener;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.moves.MovesUtils;

public class Moves extends ListPage<LineEntry>
{
    private int[][] moveOffsets;

    public Moves(final PokemobInfoPage parent)
    {
        super(parent, "moves");
    }

    @Override
    void drawInfo(final MatrixStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        final int x = (this.watch.width - 160) / 2 + 80;
        final int y = (this.watch.height - 160) / 2 + 8;
        if (this.watch.canEdit(this.parent.pokemob)) this.drawMoves(mat, x, y, mouseX, mouseY);
    }

    private void drawMoves(final MatrixStack mat, final int x, final int y, final int mouseX, final int mouseY)
    {
        final int dx = -30;
        final int dy = 20;
        int held = -1;
        final int mx = mouseX - (x + dx);
        final int my = mouseY - (y + dy);
        for (int i = 0; i < this.moveOffsets.length; i++)
        {
            final int[] offset = this.moveOffsets[i];
            if (offset[2] > 0)
            {
                held = i;
                continue;
            }
            final Move_Base move = MovesUtils.getMoveFromName(this.parent.pokemob.getMove(offset[3]));
            if (move != null)
            {
                AbstractGui.drawString(mat,this.font, MovesUtils.getMoveName(move.getName()).getString(), x + dx, y + dy
                        + offset[1] + offset[4], move.getType(this.parent.pokemob).colour);
                final int length = this.font.getStringWidth(MovesUtils.getMoveName(move.getName()).getString());
                if (mx > 0 && mx < length && my > offset[1] && my < offset[1] + this.font.FONT_HEIGHT)
                {
                    String text;
                    final int pwr = move.getPWR(this.parent.pokemob, this.watch.player);
                    if (pwr > 0) text = pwr + "";
                    else text = "-";
                    text = I18n.format("pokewatch.moves.pwr", text);
                    GlStateManager.disableDepthTest();
                    final int box = Math.max(10, this.font.getStringWidth(text) + 2);
                    final int mx1 = 75 - box;
                    final int my1 = offset[1] + 18;
                    final int dy1 = this.font.FONT_HEIGHT;
                    AbstractGui.fill(mat,x + mx1 - 1, y + my1 - 1, x + mx1 + box + 1, y + my1 + dy1 + 1, 0xFF78C850);
                    AbstractGui.fill(mat,x + mx1, y + my1, x + mx1 + box, y + my1 + dy1, 0xFF000000);
                    this.font.drawString(mat,text, x + mx1 + 1, y + my1, 0xFFFFFFFF);
                    GlStateManager.enableDepthTest();
                }
            }
        }
        if (held != -1)
        {
            final int[] offset = this.moveOffsets[held];
            final Move_Base move = MovesUtils.getMoveFromName(this.parent.pokemob.getMove(offset[3]));
            if (move != null) AbstractGui.drawString(mat,this.font, MovesUtils.getMoveName(move.getName()).getString(), x
                    + dx, y + dy + offset[1], move.getType(this.parent.pokemob).colour);
        }
    }

    @Override
    public void init()
    {
        super.init();
        //@formatter:off
        this.moveOffsets = new int[][]{
        // i = index, b = selected, dc = cursor offset
        //   dx  dy  b  i  dc
            {00, 00, 0, 0, 0},
            {00, 10, 0, 1, 0},
            {00, 20, 0, 2, 0},
            {00, 30, 0, 3, 0},
            {00, 42, 0, 4, 0}
        };
        //@formatter:on
    }

    @Override
    public void initList()
    {
        super.initList();
        int offsetX = (this.watch.width - 160) / 2 + 46;
        int offsetY = (this.watch.height - 160) / 2 + 82;
        final int height = this.font.FONT_HEIGHT * 6;
        int width = 111;

        final int colour = 0xFFFFFFFF;

        if (!this.watch.canEdit(this.parent.pokemob))
        {
            width = 111;
            final int dx = 0;
            final int dy = -60;
            offsetY += dy;
            offsetX += dx;
        }

        final Moves thisObj = this;
        final IClickListener listener = new IClickListener()
        {
            @Override
            public boolean handleClick(final ITextComponent component)
            {
                return thisObj.handleComponentClicked(component.getStyle());
            }

            @Override
            public void handleHovor(final ITextComponent component, final int x, final int y)
            {
                thisObj.renderComponentHoverEffect(component, x, y);
            }
        };

        this.list = new ScrollGui<>(this, this.minecraft, width, height, this.font.FONT_HEIGHT, offsetX, offsetY);
        final PokedexEntry entry = this.parent.pokemob.getPokedexEntry();
        final Set<String> added = Sets.newHashSet();
        for (int i = 0; i < 100; i++)
        {
            final List<String> moves = entry.getMovesForLevel(i, i - 1);
            for (final String s : moves)
            {
                added.add(s);
                final ITextComponent moveName = MovesUtils.getMoveName(s);
                moveName.setStyle(new Style());
                moveName.getStyle().setColor(TextFormatting.RED);
                final ITextComponent main = new TranslationTextComponent("pokewatch.moves.lvl", i, moveName);
                main.setStyle(new Style());
                main.getStyle().setColor(TextFormatting.GREEN);
                main.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, s));
                main.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(s)));
                this.list.addEntry(new LineEntry(this.list, 0, 0, this.font, main, colour).setClickListner(listener));
            }
        }
        for (final String s : entry.getMoves())
        {
            added.add(s);
            final ITextComponent moveName = MovesUtils.getMoveName(s);
            moveName.setStyle(new Style());
            moveName.getStyle().setColor(TextFormatting.RED);
            final ITextComponent main = new TranslationTextComponent("pokewatch.moves.tm", moveName);
            main.setStyle(new Style());
            main.getStyle().setColor(TextFormatting.GREEN);
            main.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, s));
            main.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(s)));
            this.list.addEntry(new LineEntry(this.list, 0, 0, this.font, main, colour).setClickListner(listener));
        }
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseButton)
    {
        if (mouseButton == 0)
        {
            for (final int[] moveOffset : this.moveOffsets)
                if (moveOffset[2] != 0) return true;
            final int x = (this.watch.width - 160) / 2 + 80;
            final int y = (this.watch.height - 160) / 2 + 8;
            final int dx = -30;
            final int dy = 20;
            final int x1 = (int) (mouseX - (x + dx));
            final int y1 = (int) (mouseY - (y + dy));
            final boolean inBox = x1 > 0 && y1 > 0 && x1 < 95 && y1 < 52;
            if (inBox)
            {
                int index = y1 / 10;
                index = Math.min(index, 4);
                this.moveOffsets[index][2] = 1;
                this.moveOffsets[index][4] = y1 - 10 * index;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseDragged(final double mouseX, final double mouseY, final int mouseButton, final double d2,
            final double d3)
    {
        if (mouseButton == 0)
        {
            int heldIndex = -1;
            for (int i = 0; i < this.moveOffsets.length; i++)
                if (this.moveOffsets[i][2] != 0)
                {
                    heldIndex = i;
                    break;
                }
            if (heldIndex != -1)
            {
                final int x = (this.watch.width - 160) / 2 + 80;
                final int y = (this.watch.height - 160) / 2 + 8;
                final int dx = -30;
                final int dy = 20;
                final int x1 = (int) (mouseX - (x + dx));
                final int y1 = (int) (mouseY - (y + dy));
                final boolean inBox = x1 > 0 && y1 > 0 && x1 < 95 && y1 < 52;
                if (inBox) this.moveOffsets[heldIndex][1] = y1 - this.moveOffsets[heldIndex][4];
                if (inBox) return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, mouseButton, d2, d3);
    }

    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int mouseButton)
    {
        if (mouseButton == 0)
        {
            int oldIndex = -1;
            for (int i = 0; i < this.moveOffsets.length; i++)
                if (this.moveOffsets[i][2] != 0)
                {
                    oldIndex = i;
                    this.moveOffsets[i][2] = 0;
                }
            if (oldIndex == -1) return false;
            final int x = (this.watch.width - 160) / 2 + 80;
            final int y = (this.watch.height - 160) / 2 + 8;
            final int dx = -30;
            final int dy = 20;
            final int x1 = (int) (mouseX - (x + dx));
            final int y1 = (int) (mouseY - (y + dy));
            final boolean inBox = x1 > 0 && y1 > 0 && x1 < 95 && y1 < 52;
            if (inBox)
            {
                int index = y1 / 10;
                index = Math.min(index, 4);
                index = Math.max(index, 0);
                this.parent.pokemob.exchangeMoves(oldIndex, index);
            }
            //@formatter:off
            this.moveOffsets = new int[][]{
                {00, 00, 0, 0, 0},
                {00, 10, 0, 1, 0},
                {00, 20, 0, 2, 0},
                {00, 30, 0, 3, 0},
                {00, 42, 0, 4, 0}
            };
            //@formatter:on
            if (inBox) return true;
        }
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void renderComponentHoverEffect(final MatrixStack mat, final ITextComponent component, final int x, final int y)
    {
        tooltip:
        if (component.getStyle().getHoverEvent() != null)
        {
            String text = component.getStyle().getHoverEvent().getValue().getUnformattedComponentText();
            final Move_Base move = MovesUtils.getMoveFromName(text);
            if (move == null) break tooltip;
            final int pwr = move.getPWR(this.parent.pokemob, this.watch.player);
            if (pwr > 0) text = pwr + "";
            else text = "-";
            text = I18n.format("pokewatch.moves.pwr", text);
            GlStateManager.disableDepthTest();
            final int box = Math.max(10, this.font.getStringWidth(text) + 2);
            final int mx = 100 - box;
            final int my = -10;
            final int dy = this.font.FONT_HEIGHT;
            AbstractGui.fill(x + mx - 1, y + my - 1, x + mx + box + 1, y + my + dy + 1, 0xFF78C850);
            AbstractGui.fill(x + mx, y + my, x + mx + box, y + my + dy, 0xFF000000);
            this.font.drawString(text, x + mx + 1, y + my, 0xFFFFFFFF);
            GlStateManager.enableDepthTest();
        }
        super.renderComponentHoverEffect(component, x, y);
    }

}

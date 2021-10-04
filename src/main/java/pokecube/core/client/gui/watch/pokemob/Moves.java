package pokecube.core.client.gui.watch.pokemob;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.PokemobInfoPage;
import pokecube.core.client.gui.watch.util.LineEntry;
import pokecube.core.client.gui.watch.util.LineEntry.IClickListener;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.zmoves.GZMoveManager;

public class Moves extends ListPage<LineEntry>
{
    public static final ResourceLocation TEX_DM = new ResourceLocation(PokecubeMod.ID,
            "textures/gui/pokewatchgui_moves.png");
    public static final ResourceLocation TEX_NM = new ResourceLocation(PokecubeMod.ID,
            "textures/gui/pokewatchgui_moves_nm.png");

    private int[][] moveOffsets;

    public Moves(final PokemobInfoPage parent)
    {
        super(parent, "moves", Moves.TEX_DM, Moves.TEX_NM);
    }

    @Override
    void drawInfo(final PoseStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 80;
        final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 8;
        if (this.watch.canEdit(this.parent.pokemob)) this.drawMoves(mat, x, y, mouseX, mouseY);
    }

    private void drawMoves(final PoseStack mat, final int x, final int y, final int mouseX, final int mouseY)
    {
        final int dx = 70; // -30
        final int dy = 30; // 20

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
                GuiComponent.drawString(mat, this.font, MovesUtils.getMoveName(move.getName()).getString(), x + dx, y
                        + dy + offset[1] + offset[4], move.getType(this.parent.pokemob).colour);
                final int length = this.font.width(MovesUtils.getMoveName(move.getName()).getString());
                if (mx > 0 && mx < length && my > offset[1] && my < offset[1] + this.font.lineHeight)
                {
                    String text;
                    final int pwr = move.getPWR(this.parent.pokemob, this.watch.player);
                    if (pwr > 0) text = pwr + "";
                    else text = "-";

                    if (GZMoveManager.isGZDMove(move.move.baseEntry) && offset[3] != this.parent.pokemob.getMoveIndex())
                        text = "???";

                    text = I18n.get("pokewatch.moves.pwr", text);
                    GlStateManager._disableDepthTest();
                    final int box = Math.max(10, this.font.width(text) + 2);
                    final int mx1 = 65 - box;
                    final int my1 = offset[1] + 30;
                    final int dy1 = this.font.lineHeight;
                    GuiComponent.fill(mat, x + mx1 - 1, y + my1 - 1, x + mx1 + box + 1, y + my1 + dy1 + 1, 0xFF78C850);
                    GuiComponent.fill(mat, x + mx1, y + my1, x + mx1 + box, y + my1 + dy1, 0xFF000000);
                    this.font.draw(mat, text, x + mx1 + 1, y + my1, 0xFFFFFFFF);
                    GlStateManager._enableDepthTest();
                }
            }
        }
        if (held != -1)
        {
            final int[] offset = this.moveOffsets[held];
            final Move_Base move = MovesUtils.getMoveFromName(this.parent.pokemob.getMove(offset[3]));
            if (move != null)
            {
                final int oy = 10;
                GuiComponent.drawString(mat, this.font, MovesUtils.getMoveName(move.getName()).getString(), x + dx, y
                        + dy + offset[1] + oy, move.getType(this.parent.pokemob).colour);
            }
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
            {-10, 10, 0, 0, 0},
            {-10, 20, 0, 1, 0},
            {-10, 30, 0, 2, 0},
            {-10, 40, 0, 3, 0},
            {-10, 58, 0, 4, 0}
        };
        //@formatter:on
    }

    @Override
    public void initList()
    {
        super.initList();
        int offsetX = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 90;
        int offsetY = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 30;
        final int height = this.font.lineHeight * 6;

        final int dx = 46;
        final int dy = 25;
        offsetY += dy;
        offsetX += dx;

        final int width = 111;

        final int colour = 0xFFFFFFFF;

        final Moves thisObj = this;
        final IClickListener listener = new IClickListener()
        {
            @Override
            public boolean handleClick(final Style component)
            {
                return thisObj.handleComponentClicked(component);
            }

            @Override
            public void handleHovor(final PoseStack mat, final Style component, final int x, final int y)
            {
                thisObj.renderComponentHoverEffect(mat, component, x, y);
            }
        };

        this.list = new ScrollGui<>(this, this.minecraft, width, height, this.font.lineHeight, offsetX, offsetY);
        final PokedexEntry entry = this.parent.pokemob.getPokedexEntry();
        final Set<String> added = Sets.newHashSet();
        if (!this.watch.canEdit(this.parent.pokemob))
        {
            for (int i = 0; i < 100; i++)
            {
                final List<String> moves = entry.getMovesForLevel(i, i - 1);
                for (final String s : moves)
                {
                    added.add(s);
                    final MutableComponent moveName = (MutableComponent) MovesUtils.getMoveName(s);
                    moveName.setStyle(moveName.getStyle().withColor(TextColor.fromLegacyFormat(ChatFormatting.RED)));
                    final MutableComponent main = new TranslatableComponent("pokewatch.moves.lvl", i,
                            moveName);
                    main.setStyle(main.getStyle().withColor(TextColor.fromLegacyFormat(ChatFormatting.GREEN))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, s)).withHoverEvent(
                                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(s))));
                    this.list.addEntry(new LineEntry(this.list, 0, 0, this.font, main, colour).setClickListner(
                            listener));
                }
            }
            for (final String s : entry.getMoves())
            {
                added.add(s);
                final MutableComponent moveName = (MutableComponent) MovesUtils.getMoveName(s);
                moveName.setStyle(moveName.getStyle().withColor(TextColor.fromLegacyFormat(ChatFormatting.RED)));
                final MutableComponent main = new TranslatableComponent("pokewatch.moves.tm", moveName);
                main.setStyle(main.getStyle().withColor(TextColor.fromLegacyFormat(ChatFormatting.GREEN)).withClickEvent(
                        new ClickEvent(ClickEvent.Action.CHANGE_PAGE, s)).withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT, new TextComponent(s))));
                this.list.addEntry(new LineEntry(this.list, 0, 0, this.font, main, colour).setClickListner(listener));
            }
        }
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseButton)
    {
        if (mouseButton == 0)
        {
            for (final int[] moveOffset : this.moveOffsets)
                if (moveOffset[2] != 0) return true;

            final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2;
            final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2;

            final int dx = 150;
            final int dy = 48;

            // The top left move corner should be (x + dx, y + dy)

            final int x1 = (int) (mouseX - (x + dx));
            final int y1 = (int) (mouseY - (y + dy));

            // If we are somewhere in here, we are probably clicking a move
            final boolean inBox = x1 > 0 && y1 > 0 && x1 < 95 && y1 < 58;

            if (inBox)
            {
                int index = y1 / 10;
                index = Math.min(index, 4);
                // This tells is how far down the move is, this is used as the
                // 5th slot is different spacing, otherwise could just use
                // index * 10
                final int indexShift = this.moveOffsets[index][1] - this.moveOffsets[0][1];

                // This marks the move as "selected"
                this.moveOffsets[index][2] = 1;
                // This records the original location of the mouse relative to
                // the move
                this.moveOffsets[index][4] = y1 - indexShift;

                // Apply the same shift as in dragged, to ensure we don't jitter
                // when clicked.
                this.moveOffsets[index][1] = y1 - this.moveOffsets[index][4];
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
                final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2;
                final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2;

                final int dx = 150;
                final int dy = 48;

                // The top left move corner should be (x + dx, y + dy)

                final int x1 = (int) (mouseX - (x + dx));
                final int y1 = (int) (mouseY - (y + dy));

                // If we are somewhere in here, we are probably clicking a move
                final boolean inBox = x1 > 0 && y1 > 0 && x1 < 95 && y1 < 58;

                if (inBox)
                {
                    // Offset the location of the move by how much we have moved
                    // the mouse since it was clicked.
                    this.moveOffsets[heldIndex][1] = y1 - this.moveOffsets[heldIndex][4];
                    return true;
                }
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

            final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2;
            final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2;

            final int dx = 150;
            final int dy = 48;

            // The top left move corner should be (x + dx, y + dy)

            final int x1 = (int) (mouseX - (x + dx));
            final int y1 = (int) (mouseY - (y + dy));

            // If we are somewhere in here, we are probably clicking a move
            final boolean inBox = x1 > 0 && y1 > 0 && x1 < 95 && y1 < 58;

            if (inBox)
            {
                int index = y1 / 10;
                index = Math.min(index, 4);
                index = Math.max(index, 0);
                this.parent.pokemob.exchangeMoves(oldIndex, index);
            }
            //@formatter:off
            this.moveOffsets = new int[][]{
                {-10, 10, 0, 0, 0},
                {-10, 20, 0, 1, 0},
                {-10, 30, 0, 2, 0},
                {-10, 40, 0, 3, 0},
                {-10, 58, 0, 4, 0}
            };
            //@formatter:on
            if (inBox) return true;
        }
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void renderComponentHoverEffect(final PoseStack mat, final Style component, final int x, final int y)
    {
        tooltip:
        if (component.getHoverEvent() != null)
        {
            final Object var = component.getHoverEvent().getValue(component.getHoverEvent().getAction());
            if (!(var instanceof Component)) break tooltip;
            String text = ((Component) var).getString();
            final Move_Base move = MovesUtils.getMoveFromName(text);
            if (move == null) break tooltip;
            final int pwr = move.getPWR(this.parent.pokemob, this.watch.player);
            if (pwr > 0) text = pwr + "";
            else text = "-";
            text = I18n.get("pokewatch.moves.pwr", text);
            GlStateManager._disableDepthTest();
            final int box = Math.max(10, this.font.width(text) + 2);
            final int mx = 100 - box;
            final int my = -0;
            final int dy = this.font.lineHeight;
            GuiComponent.fill(mat, x + mx - 1, y + my - 1, x + mx + box + 1, y + my + dy + 1, 0xFF78C850);
            GuiComponent.fill(mat, x + mx, y + my, x + mx + box, y + my + dy, 0xFF000000);
            this.font.draw(mat, text, x + mx + 1, y + my, 0xFFFFFFFF);
            GlStateManager._enableDepthTest();
        }
        super.renderComponentHoverEffect(mat, component, x, y);
    }

}

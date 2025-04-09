package pokecube.core.client.gui.watch.pokemob;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveConstants.AttackCategory;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.PokemobInfoPage;
import pokecube.core.client.gui.watch.util.LineEntry;
import pokecube.core.client.gui.watch.util.LineEntry.IClickListener;
import pokecube.core.moves.MovesUtils;
import thut.lib.TComponent;

import java.util.List;

public class Moves extends ListPage<LineEntry>
{
    public static final ResourceLocation TEX_DM = GuiPokeWatch.makeWatchTexture("pokewatchgui_pokedex_moves");
    public static final ResourceLocation TEX_NM = GuiPokeWatch.makeWatchTexture("pokewatchgui_pokedex_moves_nm");

    private int[][] moveOffsets;

    public Moves(final PokemobInfoPage parent)
    {
        super(parent, "moves", Moves.TEX_DM, Moves.TEX_NM);
    }

    @Override
    void drawInfo(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        int x = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 80;
        int y = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 8;
        if (this.watch.canEdit(this.parent.pokemob)) this.drawMoves(graphics, x, y, mouseX, mouseY);
    }

    private int getHovoredMove(double dx, double dy, double mouseX, double mouseY)
    {
        double x = (this.watch.width - GuiPokeWatch.GUIW) / 2f + 80;
        double y = (this.watch.height - GuiPokeWatch.GUIH) / 2f + 8;
        double mx = mouseX - (x + dx);
        double my = mouseY - (y + dy);
        IPokemob pokemob = this.parent.pokemob;
        for (int i = 0; i < this.moveOffsets.length; i++)
        {
            final int[] offset = this.moveOffsets[i];
            //this one is being dragged, so ignore it.
            if (offset[2] != 0) continue;
            final MoveEntry move = MovesUtils.getMove(pokemob.getMove(offset[3]));
            if (move != null)
            {
                Component moveName = MovesUtils.getMoveName(move.getName(), pokemob);
                int length = this.font.width(moveName);
                boolean mouseOver = mx > 0 && mx < length && my > offset[1] && my < offset[1] + this.font.lineHeight;
                if (mouseOver) return i;
            }
        }
        return -1;
    }

    private void drawMoves(final GuiGraphics graphics, final int x, final int y, final int mouseX, final int mouseY)
    {
        int dx = 53;
        int dy = 18;

        IPokemob pokemob = this.parent.pokemob;

        int held = -1;
        int hovored = getHovoredMove(dx, dy, mouseX, mouseY);

        for (int i = 0; i < this.moveOffsets.length; i++)
        {
            final int[] offset = this.moveOffsets[i];
            if (offset[2] > 0)
            {
                held = i;
                continue;
            }
            final MoveEntry move = MovesUtils.getMove(pokemob.getMove(offset[3]));
            if (move != null)
            {
                Component moveName = MovesUtils.getMoveName(move.getName(), pokemob);
                graphics.drawString(this.font, moveName, x + dx, y + dy + offset[1] + offset[4],
                        move.getType(pokemob).colour, false);
                if (hovored == i)
                {
                    Component value = TComponent.literal("-");
                    final int pwr = move.getPWR(this.parent.pokemob, this.watch.player);
                    Component stat = move.getCategory(pokemob) == AttackCategory.PHYSICAL ? TComponent.translatable(
                            "pokewatch.ATT", value) : TComponent.translatable("pokewatch.ATTSP", value);
                    if (pwr > 0) value = TComponent.translatable("pokewatch.moves.pwr.fmt", pwr, stat);
                    Component info = TComponent.translatable("pokewatch.moves.pwr", value);
                    final int box = Math.max(10, this.font.width(info) + 2);
                    final int mx1 = 170 - box;
                    final int my1 = offset[1] + 18;
                    final int dy1 = this.font.lineHeight;

                    graphics.pose().pushPose();
                    graphics.pose().translate(0, 0, 1);
                    graphics.fill(x + mx1 - 2, y + my1 - 2, x + mx1 + box + 2, y + my1 + dy1 + 2, 0xD92E0A65);
                    graphics.fill(x + mx1 - 1, y + my1 - 1, x + mx1 + box + 1, y + my1 + dy1 + 1, 0xD91E0F1E);
                    graphics.drawString(this.font, info, x + mx1 + 1, y + my1 + 1, 0xFFFFFFFF, false);

                    graphics.pose().popPose();
                }
            }
        }
        if (held != -1)
        {
            final int[] offset = this.moveOffsets[held];
            final MoveEntry move = MovesUtils.getMove(this.parent.pokemob.getMove(offset[3]));
            if (move != null && move.root_entry._implemented)
            {
                Component moveName = MovesUtils.getMoveName(move.getName(), pokemob);
                graphics.drawString(this.font, moveName, x + dx, y + dy + offset[1],
                        move.getType(this.parent.pokemob).colour);
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
        int offsetY = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 26;
        final int height = this.font.lineHeight * 11;

        final int dx = 41;
        final int dy = 8;
        offsetY += dy;
        offsetX += dx;

        final int width = 120;

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
            public void handleHovor(final GuiGraphics graphics, final Style component, final int x, final int y)
            {
                thisObj.renderComponentHoverEffect(graphics, component, x, y);
            }
        };
        IPokemob pokemob = this.parent.pokemob;

        this.list = new ScrollGui<>(this, this.minecraft, width, height, this.font.lineHeight, offsetX,
                offsetY);

        final PokedexEntry entry = pokemob.getPokedexEntry();

        if (!this.watch.canEdit(pokemob))
        {
            for (int i = 0; i < 100; i++)
            {
                final List<String> moves = entry.getMovesForLevel(i, i - 1);
                for (final String s : moves)
                {
                    MoveEntry m = MoveEntry.get(s);
                    if (m == null || !m.root_entry._implemented) continue;

                    final MutableComponent moveName = MovesUtils.getMoveName(s, pokemob);
                    final MutableComponent main = TComponent.translatable("pokewatch.moves.lvl", i, moveName);
                    main.setStyle(main.getStyle().withColor(TextColor.fromRgb(0x449944))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, s))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TComponent.literal(s))));
                    this.list.addEntry(new LineEntry(this.list, 0, 0, this.font, main.getVisualOrderText(),
                            colour).setClickListner(listener));
                }
            }
            for (final String s : entry.getMoves())
            {
                MoveEntry m = MoveEntry.get(s);
                if (m == null || !m.root_entry._implemented) continue;

                final MutableComponent moveName = MovesUtils.getMoveName(s, pokemob);
                final MutableComponent main = TComponent.translatable("pokewatch.moves.tm", moveName);
                main.setStyle(main.getStyle().withColor(TextColor.fromRgb(0x449944))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, s))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TComponent.literal(s))));
                this.list.addEntry(
                        new LineEntry(this.list, 0, 0, this.font, main.getVisualOrderText(), colour).setClickListner(
                                listener));
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
    {
        final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2;
        final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2;
        final int dx = 150;
        final int dy = 48;

        // The top left move corner should be (x + dx, y + dy)

        final int x1 = (int) (mouseX - (x + dx));
        final int y1 = (int) (mouseY - (y + dy));

        // If we are somewhere in here, we are probably clicking a move
        final boolean inBox = x1 > 0 && y1 > 48 && x1 < 95 && y1 < 58;
        if (inBox)
        {
            int i1 = 9;
            int i2 = 10;
            if (scrollY > 0) this.parent.pokemob.exchangeMoves(i1, i2);
            else if (scrollY < 0) this.parent.pokemob.exchangeMoves(i2, i1);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseButton)
    {
        IPokemob pokemob = this.parent.pokemob;
        if (mouseButton == 0 && pokemob != null)
        {
            for (final int[] moveOffset : this.moveOffsets) if (moveOffset[2] != 0) return true;

            int dx = 53; // -30
            int dy = 18; // 20
            int index = getHovoredMove(dx, dy, mouseX, mouseY);

            int y = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 8;
            double my = mouseY - (y + dy);

            if (index != -1)
            {
                int[] offset = this.moveOffsets[index];

                // This marks the move as "selected"
                offset[2] = 1;
                // This records the original location of the mouse relative to
                // the move
                offset[4] = (int) (my - offset[1]);

                // Apply the same shift as in dragged, to ensure we don't jitter
                // when clicked.
                offset[1] = (int) (my - offset[4]);
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
                int dy = 18;
                int y = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 8;
                double my = mouseY - (y + dy);
                // Offset the location of the move by how much we have moved
                // the mouse since it was clicked.
                this.moveOffsets[heldIndex][1] = (int) (my - this.moveOffsets[heldIndex][4]);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, mouseButton, d2, d3);
    }

    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int mouseButton)
    {
        IPokemob pokemob = this.parent.pokemob;
        if (mouseButton == 0 && pokemob != null)
        {
            int dx = 53; // -30
            int dy = 18; // 20
            int index = getHovoredMove(dx, dy, mouseX, mouseY);

            int oldIndex = -1;
            for (int i = 0; i < this.moveOffsets.length; i++)
                if (this.moveOffsets[i][2] != 0)
                {
                    oldIndex = i;
                    this.moveOffsets[i][2] = 0;
                }
            if (oldIndex == -1) return false;
            if (index != oldIndex && index != -1)
            {
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
            if (index != oldIndex) return true;
        }
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    protected void renderComponentHoverEffect(final GuiGraphics graphics, final Style component, final int x,
            final int y)
    {
        if (this.watch.canEdit(this.parent.pokemob)) return;
        tooltip:
        if (component.getHoverEvent() != null)
        {
            IPokemob pokemob = this.parent.pokemob;
            final Object var = component.getHoverEvent().getValue(component.getHoverEvent().getAction());
            if (!(var instanceof Component comp) || pokemob == null) break tooltip;
            final MoveEntry move = MovesUtils.getMove(comp.getString());
            if (move == null) break tooltip;
            Component value = TComponent.literal("-");
            final int pwr = move.getPWR(pokemob, this.watch.player);
            Component stat =
                    move.getCategory(pokemob) == AttackCategory.PHYSICAL ? TComponent.translatable("pokewatch.ATT",
                            value) : TComponent.translatable("pokewatch.ATTSP", value);
            if (pwr > 0) value = TComponent.translatable("pokewatch.moves.pwr.fmt", pwr, stat);
            Component info = TComponent.translatable("pokewatch.moves.pwr", value);
            final int box = Math.max(10, this.font.width(info) + 2);
            final int mx = 106 - box;
            final int my = 0;
            final int dy = this.font.lineHeight;

            graphics.pose().pushPose();
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 1);
            graphics.fill(x + mx - 2, y + my - 2, x + mx + box + 2, y + my + dy + 2, 0xD92E0A65);
            graphics.fill(x + mx - 1, y + my - 1, x + mx + box + 1, y + my + dy + 1, 0xD91E0F1E);
            graphics.drawString(this.font, info, x + mx + 1, y + my + 1, 0xFFFFFFFF);
            graphics.pose().popPose();
            graphics.pose().popPose();
        }
    }
}

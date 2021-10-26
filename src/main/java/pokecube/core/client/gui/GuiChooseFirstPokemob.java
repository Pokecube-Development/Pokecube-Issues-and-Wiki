package pokecube.core.client.gui;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.client.Resources;
import pokecube.core.client.gui.pokemob.GuiPokemobBase;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.network.packets.PacketChoose;
import pokecube.core.utils.PokeType;
import thut.api.entity.IMobColourable;

public class GuiChooseFirstPokemob extends Screen
{

    public final static float POKEDEX_RENDER = 1.5f;
    public static boolean     special        = false;
    public static boolean     pick           = false;

    public static PokedexEntry[] starters;

    int xSize = 150;
    int ySize = 150;

    private boolean gotSpecial = true;

    protected PlayerEntity player       = null;
    protected PokedexEntry pokedexEntry = null;
    int                    index        = 0;

    Button next;

    Button prev;
    Button choose;
    Button accept;

    Button deny;

    public GuiChooseFirstPokemob(PokedexEntry[] _starters)
    {
        super(new TranslationTextComponent("pokecube.starter.select"));
        if (_starters == null) _starters = Database.getStarters();
        GuiChooseFirstPokemob.starters = _starters;
        this.player = PokecubeCore.proxy.getPlayer();
    }

    @Override
    public boolean charTyped(final char par1, final int par2)
    {
        if (par2 == 1)
        {
            this.player.closeContainer();
            return true;
        }
        return super.charTyped(par1, par2);
    }

    private MobEntity getEntityToDisplay()
    {
        final IPokemob pokemob = EventsHandlerClient.getRenderMob(this.pokedexEntry, PokecubeCore.proxy.getWorld());
        if (pokemob == null) return null;
        return pokemob.getEntity();
    }

    @Override
    public void init()
    {
        super.init();
        final int xOffset = 0;
        final int yOffset = 110;
        if (GuiChooseFirstPokemob.starters.length > 0)
        {
            final ITextComponent next = new TranslationTextComponent("block.pc.next");
            this.addButton(this.next = new Button(this.width / 2 - xOffset + 65, this.height / 2 - yOffset, 50, 20,
                    next, b ->
                    {
                        this.index++;
                        if (this.index >= GuiChooseFirstPokemob.starters.length) this.index = 0;
                    }));
            final ITextComponent prev = new TranslationTextComponent("block.pc.previous");
            this.addButton(this.prev = new Button(this.width / 2 - xOffset - 115, this.height / 2 - yOffset, 50, 20,
                    prev, b ->
                    {
                        if (this.index > 0) this.index--;
                        else this.index = GuiChooseFirstPokemob.starters.length - 1;
                    }));
        }

        this.addButton(this.choose = new Button(this.width / 2 - xOffset - 25, this.height / 2 - yOffset + 160, 50, 20,
                new TranslationTextComponent("gui.pokemob.select"), b ->
                {
                    this.sendMessage(this.pokedexEntry);
                    this.player.closeContainer();
                }));

        this.addButton(this.accept = new Button(this.width / 2 - xOffset + 64, this.height / 2 - yOffset + 30, 50, 20,
                new TranslationTextComponent("gui.pokemob.accept"), b ->
                {
                    this.gotSpecial = true;

                    this.next.visible = true;
                    this.prev.visible = true;
                    this.choose.visible = true;
                    this.accept.visible = false;
                    this.deny.visible = false;
                    GuiChooseFirstPokemob.special = false;
                    if (!GuiChooseFirstPokemob.pick)
                    {
                        this.sendMessage((PokedexEntry) null);
                        this.player.closeContainer();
                    }
                }));
        this.addButton(this.deny = new Button(this.width / 2 - xOffset - 115, this.height / 2 - yOffset + 30, 50, 20,
                new TranslationTextComponent("gui.pokemob.deny"), b ->
                {
                    this.next.visible = true;
                    this.prev.visible = true;
                    this.choose.visible = true;
                    this.accept.visible = false;
                    this.deny.visible = false;
                    GuiChooseFirstPokemob.special = false;
                }));

        if (!GuiChooseFirstPokemob.special)
        {
            this.accept.visible = false;
            this.deny.visible = false;
        }
        else if (this.next != null && this.prev != null)
        {
            this.next.visible = false;
            this.prev.visible = false;
            this.choose.visible = false;
        }

    }

    @Override
    public void render(final MatrixStack mat, final int i, final int j, final float f)
    {
        this.renderBackground(mat);
        super.render(mat, i, j, f);

        if (GuiChooseFirstPokemob.special)
        {
            AbstractGui.drawCenteredString(mat, this.font, I18n.get("gui.pokemob.choose1st.override"), this.width
                    / 2, 17, 0xffffff);
            return;
        }
        if (GuiChooseFirstPokemob.starters == null || GuiChooseFirstPokemob.starters.length == 0)
            GuiChooseFirstPokemob.starters = new PokedexEntry[] { Pokedex.getInstance().getFirstEntry() };

        this.pokedexEntry = GuiChooseFirstPokemob.starters[this.index % GuiChooseFirstPokemob.starters.length];

        if (this.pokedexEntry == null) this.pokedexEntry = Pokedex.getInstance().getFirstEntry();
        if (this.pokedexEntry == null)
        {
            this.player.closeContainer();
            return;
        }

        GL11.glPushMatrix();

        AbstractGui.drawCenteredString(mat, this.font, I18n.get("gui.pokemob.choose1st"), this.width / 2, 17,
                0xffffff);

        AbstractGui.drawCenteredString(mat, this.font, I18n.get(this.pokedexEntry.getUnlocalizedName()), this.width
                / 2, 45, 0xffffff);

        int n = 0;
        int m = 0;
        n = (this.width - this.xSize) / 2;
        m = (this.height - this.ySize) / 2;
        final int l = 40;
        final int k = 150;

        if (this.pokedexEntry.getType2() == PokeType.unknown) AbstractGui.drawCenteredString(mat, this.font, PokeType
                .getTranslatedName(this.pokedexEntry.getType1()), this.width / 2, 65, this.pokedexEntry
                        .getType1().colour);
        else
        {
            AbstractGui.drawCenteredString(mat, this.font, PokeType.getTranslatedName(this.pokedexEntry.getType1()),
                    this.width / 2 - 20, 65, this.pokedexEntry.getType1().colour);
            AbstractGui.drawCenteredString(mat, this.font, PokeType.getTranslatedName(this.pokedexEntry.getType2()),
                    this.width / 2 + 20, 65, this.pokedexEntry.getType2().colour);
        }
        GL11.glPushMatrix();

        this.minecraft.getTextureManager().bind(Resources.GUI_POKEMOB);

        GL11.glColor4f(255f / 255f, 0f / 255f, 0f / 255f, 1.0F);
        this.blit(mat, n + k, m + l, 0, 0, this.pokedexEntry.getStatHP(), 13);

        GL11.glColor4f(234f / 255f, 125f / 255f, 46f / 255f, 1.0F);
        this.blit(mat, n + k, m + l + 13, 0, 0, this.pokedexEntry.getStatATT(), 13);

        GL11.glColor4f(242f / 255f, 203f / 255f, 46f / 255f, 1.0F);
        this.blit(mat, n + k, m + l + 26, 0, 0, this.pokedexEntry.getStatDEF(), 13);

        GL11.glColor4f(102f / 255f, 140f / 255f, 234f / 255f, 1.0F);
        this.blit(mat, n + k, m + l + 39, 0, 0, this.pokedexEntry.getStatATTSPE(), 13);

        GL11.glColor4f(118f / 255f, 198f / 255f, 78f / 255f, 1.0F);
        this.blit(mat, n + k, m + l + 52, 0, 0, this.pokedexEntry.getStatDEFSPE(), 13);

        GL11.glColor4f(243f / 255f, 86f / 255f, 132f / 255f, 1.0F);
        this.blit(mat, n + k, m + l + 65, 0, 0, this.pokedexEntry.getStatVIT(), 13);

        final String H = I18n.get("pokewatch.HP");
        final String A = I18n.get("pokewatch.ATT");
        final String D = I18n.get("pokewatch.DEF");
        final String AS = I18n.get("pokewatch.ATTSP");
        final String DS = I18n.get("pokewatch.DEFSP");
        final String S = I18n.get("pokewatch.VIT");

        AbstractGui.drawCenteredString(mat, this.font, H + ": ", n + k - 10, m + l + 3, 0x930000);
        AbstractGui.drawCenteredString(mat, this.font, A + ": ", n + k - 10, m + l + 17, 0xAD5D22);
        AbstractGui.drawCenteredString(mat, this.font, D + ": ", n + k - 10, m + l + 29, 0xB39622);
        AbstractGui.drawCenteredString(mat, this.font, AS + ": ", n + k - 18, m + l + 42, 0x4C68AD);
        AbstractGui.drawCenteredString(mat, this.font, DS + ": ", n + k - 18, m + l + 55, 0x57933A);
        AbstractGui.drawCenteredString(mat, this.font, S + ": ", n + k - 10, m + l + 67, 0xB44062);
        GL11.glPopMatrix();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderMob();
        this.renderItem(n + 00, m + 75, 40);

        GL11.glPopMatrix();
    }

    public void renderItem(final double x, final double y, final double z)
    {
        final ItemStack item = PokecubeItems.POKECUBE_CUBES;
        if (item.getItem() instanceof IPokecube)
        {
            final IBakedModel model = Minecraft.getInstance().getItemRenderer().getModel(item, null,
                    null);

            final MatrixStack matrixstack = new MatrixStack();
            final IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().renderBuffers()
                    .bufferSource();
            matrixstack.pushPose();
            matrixstack.translate((float) x + 8, (float) y + 8, 100.0F);
            matrixstack.scale(50.0F, -50.0F, 50.0F);
            final boolean flag = !model.usesBlockLight();
            if (flag) RenderHelper.setupForFlatItems();

            Minecraft.getInstance().getItemRenderer().render(item,
                    net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType.GUI, false, matrixstack,
                    irendertypebuffer$impl, 15728880, OverlayTexture.NO_OVERLAY, model);
            RenderSystem.enableDepthTest();
            if (flag) RenderHelper.setupFor3DItems();
            matrixstack.popPose();
            irendertypebuffer$impl.endBatch();
        }
    }

    private void renderMob()
    {
        try
        {
            final MobEntity entity = this.getEntityToDisplay();

            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
            pokemob.setShiny(false);
            pokemob.setSize(1);

            if (entity instanceof IMobColourable) ((IMobColourable) entity).setRGBA(255, 255, 255, 255);
            //@formatter:off
            final int dx =-50 + (this.width - this.xSize)/2;
            final int dy = 50 + (this.height - this.ySize)/2;
            final float size = 1.5f;
            final float yaw =  Util.getMillis() / 20;
            final float hx = 0;
            final float hy = yaw;
            //@formatter:on
            GL11.glPushMatrix();
            GL11.glTranslated(0, 0, 100);
            GuiPokemobBase.renderMob(entity, dx, dy, 0, yaw, hx, hy, size);
            GL11.glPopMatrix();
        }
        catch (final Throwable e)
        {
            e.printStackTrace();
        }
    }

    private void sendMessage(final PokedexEntry entry)
    {
        final PacketChoose packet = new PacketChoose(PacketChoose.CHOOSE);
        packet.data.putBoolean("S", this.gotSpecial);
        if (entry != null) packet.data.putString("N", entry.getTrimmedName());
        PokecubeCore.packets.sendToServer(packet);
    }

}

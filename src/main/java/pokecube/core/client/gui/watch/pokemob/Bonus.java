package pokecube.core.client.gui.watch.pokemob;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IBidiRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.PokemobInfoPage;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.Nature;
import pokecube.core.interfaces.PokecubeMod;

public class Bonus extends PokeInfoPage {

	int                   last = 0;
    final PokemobInfoPage parent;
    
	private IBidiRenderer splitRenderer = IBidiRenderer.field_243257_a;
    private final FontRenderer fontRender;
    
	public Bonus(final PokemobInfoPage parent)
    {
        super(parent, "extra");
        this.parent = parent;
        this.fontRender = Minecraft.getInstance().fontRenderer;
    }

	public static final ResourceLocation           TEXTURE_BASE  = new ResourceLocation(PokecubeMod.ID,
    		"textures/gui/pokewatchgui_battle.png");
	
	@Override
	public void renderBackground(MatrixStack mat)
	{
		this.minecraft.textureManager.bindTexture(Bonus.TEXTURE_BASE);
    	int offsetX = (this.watch.width - GuiPokeWatch.GUIW) / 2;
        int offsetY = (this.watch.height - GuiPokeWatch.GUIH) / 2;
    	this.blit(mat, offsetX, offsetY, 0, 0, GuiPokeWatch.GUIW, GuiPokeWatch.GUIH);
	}
	
	//Default
    private void drawBaseStats(final MatrixStack mat, final int x, final int y)
    {  	
        final int statYOffSet = y + 25; //0
        final int offsetX = x + 50; //-50
        int dx = 20 + offsetX;

        TextComponent message = new StringTextComponent("=)");
        //final String H = I18n.format("pokewatch.HP");

        AbstractGui.drawString(mat, this.fontRender, message, x + dx, statYOffSet + 18, 0xFF0000);
    }
    
	//Your Pokemob
    private void drawInfo(final MatrixStack mat, final int x, final int y)
    {
    	final int offsetX = x + 30; //-52
    	int dx = 20 + offsetX;
    	   	
    	// Draw ability, Happiness and Size
        final Ability ability = this.parent.pokemob.getAbility();
        final Nature nature = this.parent.pokemob.getNature();
        dx = x + 57; //55
        int dy = 40; //25
        // Draw ability
        if (ability != null)
        {
            final String abilityName = I18n.format(ability.getName());
            AbstractGui.drawString(mat,this.fontRender, I18n.format("pokewatch.ability", abilityName), x + dx, y + dy, 0xFFFFFF);
        }
        final int happiness = this.parent.pokemob.getHappiness();
        TextComponent message = new StringTextComponent("");

        // Draw size
        dy += 10; //50
        message = new TranslationTextComponent("pokewatch.size", this.parent.pokemob.getSize());
        this.splitRenderer = IBidiRenderer.func_243258_a(this.fontRender, message, 100);
        this.splitRenderer.func_241866_c(mat, x + dx, y + dy, 12, 0xFFFFFF);

        // Draw Nature
        dy += 11; //50
        if (nature != null)
        {
		    message = new TranslationTextComponent("pokewatch.nature", this.parent.pokemob.getNature());
		    this.splitRenderer = IBidiRenderer.func_243258_a(this.fontRender, message, 100);
		    this.splitRenderer.func_241866_c(mat, x + dx, y + dy, 12, 0xFFFFFF);
    	}
        
        if (happiness == 0) message = new TranslationTextComponent("pokemob.info.happy0");
        if (happiness > 0) message = new TranslationTextComponent("pokemob.info.happy1");
        if (happiness > 49) message = new TranslationTextComponent("pokemob.info.happy2");
        if (happiness > 99) message = new TranslationTextComponent("pokemob.info.happy3");
        if (happiness > 149) message = new TranslationTextComponent("pokemob.info.happy4");
        if (happiness > 199) message = new TranslationTextComponent("pokemob.info.happy5");
        if (happiness > 254) message = new TranslationTextComponent("pokemob.info.happy6");
        // Draw Happiness
        dy += 16; //50
        this.splitRenderer = IBidiRenderer.func_243258_a(this.fontRender, message, 100);
        this.splitRenderer.func_241866_c(mat, x + dx, y + dy, 12, 0xFFFFFF);
    }

    @Override
    void drawInfo(final MatrixStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2; // 2 + 80
        final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2; // 2 + 8
        if (this.watch.canEdit(this.parent.pokemob)) this.drawInfo(mat, x, y);
        else this.drawBaseStats(mat, x, y);
    }
}

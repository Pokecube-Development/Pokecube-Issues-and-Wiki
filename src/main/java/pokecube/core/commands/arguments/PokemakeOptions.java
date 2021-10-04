package pokecube.core.commands.arguments;

import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.FormeHolder;
import pokecube.core.interfaces.Nature;
import pokecube.core.utils.Tools;
import thut.api.entity.IMobColourable;
import thut.api.maths.Vector3;

public class PokemakeOptions
{
    private static final Map<String, PokemakeOptions.OptionHandler> REGISTRY = Maps.newHashMap();

    public static void register(final String id, final IFilter handler, final Predicate<PokemakeOptions> canHandle,
            final Component tooltip)
    {
        PokemakeOptions.REGISTRY.put(id, new PokemakeOptions.OptionHandler(handler, canHandle, tooltip));
    }

    static
    {
        PokemakeOptions.register("name", (options) ->
        {

        }, (options) ->
        {
            return !options.locks[PokemakeOptions.NAME];
        }, new TranslatableComponent("argument.entity.options.name.description"));
    }

    static final int COLOUR   = 0;
    static final int LEVEL    = 1;
    static final int MOVES    = 2;
    static final int WILD     = 3;
    static final int HELD     = 4;
    static final int SHINY    = 5;
    static final int FORM     = 6;
    static final int SEXE     = 7;
    static final int ABILITY  = 8;
    static final int LOCATION = 9;
    static final int SIZE     = 10;
    static final int NATURE   = 11;
    static final int NAME     = 12;
    static final int IVS      = 13;

    int red, green, blue, alpha;

    int       level       = 1;
    String[]  moves       = null;
    boolean   asWild      = false;
    ItemStack held        = ItemStack.EMPTY;
    boolean   shiny       = false;
    String    formeHolder = "";
    byte      gender      = -3;
    String    ability     = "";
    double    x, y, z;
    float     size        = -1;
    Nature    nature      = null;
    String    nickname    = "";

    byte[] ivs = null;

    boolean[] locks = new boolean[14];

    StringReader reader;

    public PokemakeOptions(final StringReader reader)
    {
        this.red = this.green = this.blue = this.alpha = 255;
        this.x = this.y = this.z = 0;
        this.reader = reader;
    }

    public IPokemob apply(IPokemob mob)
    {
        if (mob.getEntity() instanceof IMobColourable) ((IMobColourable) mob.getEntity()).setRGBA(this.red, this.green,
                this.blue, this.alpha);

        mob.setHealth(mob.getMaxHealth());

        if (!this.formeHolder.isEmpty())
        {
            final ResourceLocation formetag = PokecubeItems.toPokecubeResource(this.formeHolder);
            final FormeHolder holder = Database.formeHolders.get(formetag);
            mob.setCustomHolder(holder);
        }
        mob.setHeldItem(this.held);
        mob.setShiny(this.shiny);
        if (this.gender != -3) mob.setSexe(this.gender);
        if (this.size > 0) mob.setSize(this.size);
        if (!this.nickname.isEmpty()) mob.setPokemonNickname(this.nickname);
        if (AbilityManager.abilityExists(this.ability)) mob.setAbility(AbilityManager.getAbility(this.ability));
        if (this.nature != null) mob.setNature(this.nature);
        final int exp = Tools.levelToXp(mob.getExperienceMode(), this.level);
        mob = mob.setExp(exp, this.asWild);
        this.level = Tools.xpToLevel(mob.getPokedexEntry().getEvolutionMode(), exp);
        mob = mob.levelUp(this.level);
        if (this.ivs != null) mob.setIVs(this.ivs);

        // Set moves after calling lvl up, so they don't get over-written
        if (this.moves != null) for (int i = 0; i < this.moves.length; i++)
        {
            final String arg = this.moves[i];
            if (arg == null || arg.isEmpty()) continue;
            if (arg.equalsIgnoreCase("none")) mob.setMove(i, null);
            else mob.setMove(i, arg);
        }
        final Vector3 temp = Vector3.getNewVector();
        temp.set(mob.getEntity()).addTo(this.x, this.y, this.z).moveEntity(mob.getEntity());

        return mob;
    }

    public interface IFilter
    {
        void handle(PokemakeOptions options) throws CommandSyntaxException;
    }

    static class OptionHandler
    {
        public final IFilter                    handler;
        public final Predicate<PokemakeOptions> canHandle;
        public final Component             tooltip;

        private OptionHandler(final IFilter handlerIn, final Predicate<PokemakeOptions> canHandle,
                final Component tooltipIn)
        {
            this.handler = handlerIn;
            this.canHandle = canHandle;
            this.tooltip = tooltipIn;
        }
    }
}

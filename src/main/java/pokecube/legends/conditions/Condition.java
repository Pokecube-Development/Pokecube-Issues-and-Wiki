package pokecube.legends.conditions;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.database.stats.ISpecialCaptureCondition;
import pokecube.core.database.stats.ISpecialSpawnCondition;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.Tools;
import pokecube.legends.PokecubeLegends;
import thut.api.maths.Vector3;

public abstract class Condition implements ISpecialCaptureCondition, ISpecialSpawnCondition
{

    protected static boolean isBlock(final World world, final ArrayList<Vector3> blocks, final Block toTest)
    {
        for (final Vector3 v : blocks)
            if (v.getBlock(world) != toTest) return false;
        return true;
    }

    /**
     * @param world
     * @param blocks
     * @param material
     * @param bool
     *            if true, looks for matches, if false looks for anything that
     *            doesn't match.
     * @return
     */
    protected static boolean isMaterial(final World world, final ArrayList<Vector3> blocks, final Material material,
            final boolean bool)
    {
        final boolean ret = true;
        if (bool)
        {
            for (final Vector3 v : blocks)
                if (v.getBlockMaterial(world) != material) return false;
        }
        else for (final Vector3 v : blocks)
            if (v.getBlockMaterial(world) == material) return false;
        return ret;
    }

    public abstract PokedexEntry getEntry();

    @Override
    public boolean canCapture(final Entity trainer)
    {
        if (trainer == null) return false;
        if (CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(), this.getEntry()) > 0) return false;
        if (trainer instanceof ServerPlayerEntity && PokecubePlayerDataHandler.getCustomDataTag(
                (ServerPlayerEntity) trainer).getBoolean("capt:" + this.getEntry().getTrimmedName())) return false;
        return true;
    }

    @Override
    public void onSpawn(final IPokemob mob)
    {
    }

    @Override
    public boolean canSpawn(final Entity trainer)
    {
        if (trainer == null) return false;
        if (CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(), this.getEntry()) > 0) return true;
        // Also check if can capture, if not, no point in being able to spawn.
        if (trainer instanceof ServerPlayerEntity && PokecubePlayerDataHandler.getCustomDataTag(
                (ServerPlayerEntity) trainer).getBoolean("capt:" + this.getEntry().getTrimmedName())) return false;
        if (trainer instanceof ServerPlayerEntity)
        {
            final boolean prevSpawn = PokecubePlayerDataHandler.getCustomDataTag((ServerPlayerEntity) trainer)
                    .getBoolean("spwn:" + this.getEntry().getTrimmedName());
            if (!prevSpawn) return true;
            final MinecraftServer server = trainer.getServer();
            final long spwnDied = PokecubePlayerDataHandler.getCustomDataTag((ServerPlayerEntity) trainer).getLong(
                    "spwn_ded:" + this.getEntry().getTrimmedName());
            if (spwnDied > 0) return spwnDied + PokecubeLegends.config.respawnLegendDelay < server.getWorld(
                    DimensionType.OVERWORLD).getGameTime();
            return false;
        }
        return true;
    }

    @Override
    public boolean canSpawn(final Entity trainer, final Vector3 location, final boolean message)
    {
        if (!this.canSpawn(trainer)) return false;
        if (SpawnHandler.canSpawn(this.getEntry().getSpawnData(), location, trainer.getEntityWorld(), false))
        {
            final boolean here = Tools.countPokemon(location, trainer.getEntityWorld(), 32, this.getEntry()) > 0;
            return !here;
        }
        if (message) this.sendNoHere(trainer);
        return false;
    }

    public void sendNoTrust(final Entity trainer)
    {
        final String message = "msg.notrust.info";
        final ITextComponent component = new TranslationTextComponent(message, new TranslationTextComponent(this
                .getEntry().getUnlocalizedName()));
        trainer.sendMessage(component);
    }

    public void sendNoHere(final Entity trainer)
    {
        final String message = "msg.nohere.info";
        final ITextComponent component = new TranslationTextComponent(message, new TranslationTextComponent(this
                .getEntry().getUnlocalizedName()));
        trainer.sendMessage(component);
    }

    // Basic Legend
    public void sendLegend(final Entity trainer, String type, final int numA, final int numB)
    {
        final String message = "msg.infolegend.info";
        type = PokeType.getTranslatedName(PokeType.getType(type));
        trainer.sendMessage(new TranslationTextComponent(message, type, numA + 1, numB));
    }

    // Duo Type Legend
    public void sendLegendDuo(final Entity trainer, String type, String kill, final int numA, final int numB,
            final int killa, final int killb)
    {
        final String message = "msg.infolegendduo.info";
        type = PokeType.getTranslatedName(PokeType.getType(type));
        kill = PokeType.getTranslatedName(PokeType.getType(kill));
        trainer.sendMessage(new TranslationTextComponent(message, type, kill, numA + 1, numB, killa + 1, killb));
    }

    // Catch specific Legend
    public void sendLegendExtra(final Entity trainer, final String names)
    {
        final String message = "msg.infolegendextra.info";
        final String[] split = names.split(", ");
        ITextComponent namemes = null;
        for (final String s : split)
        {
            PokedexEntry entry = Database.getEntry(s);
            if (entry == null) entry = Database.missingno;
            if (namemes == null) namemes = new TranslationTextComponent(entry.getUnlocalizedName());
            else namemes = namemes.appendText(", ").appendSibling(new TranslationTextComponent(entry
                    .getUnlocalizedName()));
        }
        trainer.sendMessage(new TranslationTextComponent(message, namemes));
    }

    public void sendAngered(final Entity trainer)
    {
        final String message = "msg.angeredlegend.json";
        final ITextComponent component = new TranslationTextComponent(message, new TranslationTextComponent(this
                .getEntry().getUnlocalizedName()));
        trainer.sendMessage(component);
    }
}

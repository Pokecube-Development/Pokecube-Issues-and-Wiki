package pokecube.legends.conditions;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.database.stats.ISpecialCaptureCondition;
import pokecube.core.database.stats.ISpecialSpawnCondition;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.Tools;
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
        if (trainer instanceof ServerPlayerEntity && PokecubePlayerDataHandler.getCustomDataTag(
                (ServerPlayerEntity) trainer).getBoolean("spwn:" + this.getEntry().getTrimmedName())) return false;
        return true;
    }

    @Override
    public boolean canSpawn(final Entity trainer, final Vector3 location)
    {
        if (!this.canSpawn(trainer)) return false;
        if (SpawnHandler.canSpawn(this.getEntry().getSpawnData(), location, trainer.getEntityWorld(), false))
        {
            final boolean here = Tools.countPokemon(location, trainer.getEntityWorld(), 32, this.getEntry()) > 0;
            return !here;
        }
        this.sendNoHere(trainer);
        return false;
    }

    public void sendNoTrust(final Entity trainer)
    {
        final String message = "msg.notrust.txt";
        final ITextComponent component = new TranslationTextComponent(message, new TranslationTextComponent(this
                .getEntry().getUnlocalizedName()));
        trainer.sendMessage(component);
    }

    public void sendNoHere(final Entity trainer)
    {
        final String message = "msg.nohere.txt";
        final ITextComponent component = new TranslationTextComponent(message, new TranslationTextComponent(this
                .getEntry().getUnlocalizedName()));
        trainer.sendMessage(component);
    }

    // Basic Legend
    public void sendLegend(final Entity trainer, final String type, final float numA, final double numB)
    {
        final String message = "msg.infolegend.txt";
        final ITextComponent component = new TranslationTextComponent(message, type, numA, numB);
        trainer.sendMessage(component);
    }

    // Duo Type Legend
    public void sendLegendDuo(final Entity trainer, final String type, final String kill, final float numA,
            final double numB, final float killa, final double killb)
    {
        final String message = "msg.infolegendduo.txt";
        final ITextComponent component = new TranslationTextComponent(message, type, kill, numA, numB, killa, killb);
        trainer.sendMessage(component);
    }

    // Catch specific Legend
    public void sendLegendExtra(final Entity trainer, final String names)
    {
        final String message = "msg.infolegendextra.txt";
        final ITextComponent component = new TranslationTextComponent(message, names);
        trainer.sendMessage(component);
    }

    public void sendAngered(final Entity trainer)
    {
        final String message = "msg.angeredlegend.txt";
        final ITextComponent component = new TranslationTextComponent(message, new TranslationTextComponent(this
                .getEntry().getUnlocalizedName()));
        trainer.sendMessage(component);
    }
}

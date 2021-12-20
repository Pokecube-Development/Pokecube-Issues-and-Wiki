package thut.bot.entity.ai.modules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import thut.api.entity.CopyCaps;
import thut.api.entity.ICopyMob;
import thut.api.entity.event.CopyUpdateEvent;
import thut.bot.entity.BotPlayer;
import thut.bot.entity.ai.BotAI;
import thut.core.common.network.CapabilitySync;

@BotAI(key = "thutbot:mimic")
public class MimicBot extends AbstractBot
{
    public static final Pattern startPattern = Pattern.compile(START + SPACE + RSRC);

    public MimicBot(BotPlayer player)
    {
        super(player);
    }

    @Override
    public boolean init(String args)
    {
        Matcher match = startPattern.matcher(args);
        if (match.find())
        {
            try
            {
                ResourceLocation loc = new ResourceLocation(match.group(5));
                final EntityType<?> type = ForgeRegistries.ENTITIES.getValue(loc);
                if (type == null || !type.getRegistryName().equals(loc)) return false;
                final ICopyMob copy = CopyCaps.get(player);
                copy.setCopiedID(loc);
                CapabilitySync.sendUpdate(player);
                return true;
            }
            catch (Exception e)
            {
                return false;
            }
        }
        else return false;
    }

    @Override
    public void end(ServerPlayer commander)
    {
        final ICopyMob copy = CopyCaps.get(player);
        copy.setCopiedID(null);
    }

    @Override
    protected void preBotTick(ServerLevel world)
    {
        final ICopyMob copy = CopyCaps.get(player);
        if (copy.getCopiedMob() instanceof PathfinderMob mob)
        {
            this.mob = mob;
            this.mob.setOldPosAndRot();
            this.mob.tickCount = this.player.tickCount;
        }
        else super.preBotTick(world);
    }

    @Override
    public void botTick(ServerLevel world)
    {
        final ICopyMob copy = CopyCaps.get(player);
        copy.baseInit(world, player);
        final LivingEntity living = copy.getCopiedMob();

        // For when the mob gets saved, but doesn't load correctly.
        if (living == null) return;

        ICopyMob.copyEntityTransforms(living, player);
        ICopyMob.copyPositions(living, player);

        living.setId(-(player.getId() + 100));
        living.noPhysics = false;

        living.onAddedToWorld();
        living.tick();
        living.onRemovedFromWorld();

        final float eye = living.getEyeHeight(player.getPose(), player.getDimensions(player.getPose()));
        if (eye != player.getEyeHeight(player.getPose(), player.getDimensions(player.getPose())))
            player.refreshDimensions();

        living.setItemInHand(InteractionHand.MAIN_HAND, player.getItemInHand(InteractionHand.MAIN_HAND));
        living.setItemInHand(InteractionHand.OFF_HAND, player.getItemInHand(InteractionHand.OFF_HAND));

        living.level = player.level;

        if (!MinecraftForge.EVENT_BUS.post(new CopyUpdateEvent(living, player)))
        {
            living.setHealth(player.getHealth());
            living.setAirSupply(player.getAirSupply());
        }
    }

    @Override
    protected void postBotTick(ServerLevel world)
    {
        super.postBotTick(world);
    }

}

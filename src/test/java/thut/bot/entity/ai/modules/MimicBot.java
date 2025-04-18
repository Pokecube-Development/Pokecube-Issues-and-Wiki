package thut.bot.entity.ai.modules;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.neoforged.neoforge.event.EventHooks;
import thut.api.ThutCaps;
import thut.api.attachments.CopyMob;
import thut.api.entity.ICopyMob;
import thut.api.entity.event.CopyUpdateEvent;
import thut.bot.entity.BotPlayer;
import thut.bot.entity.ai.BotAI;
import thut.core.common.ThutCore;
import thut.core.common.network.SyncAttachments;
import thut.lib.RegHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                ResourceLocation loc = ResourceLocation.parse(match.group(5));
                final EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(loc);
                if (type == null || !RegHelper.getKey(type).equals(loc)) return false;
                final ICopyMob copy = ThutCaps.getCopyMob(player);
                copy.setCopiedID(loc);
                SyncAttachments.syncChange(CopyMob.TYPE_COPY, this.mob);
                this.getTag().putString("id", loc.toString());
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
        final ICopyMob copy = ThutCaps.getCopyMob(player);
        copy.setCopiedID(null);
        getTag().remove("id");
    }

    @Override
    protected void preBotTick(ServerLevel world)
    {
        final ICopyMob copy = ThutCaps.getCopyMob(player);
        if (copy.getCopiedMob() instanceof PathfinderMob mob)
        {
            this.mob = mob;
            this.mob.setOldPosAndRot();
            this.mob.tickCount = this.player.tickCount;
        }
        else super.preBotTick(world);
    }

    @Override
    public void tick()
    {
        final ICopyMob copy = ThutCaps.getCopyMob(player);
        ResourceLocation id = copy.getCopiedID();
        LivingEntity mob = copy.getCopiedMob();
        CompoundTag nbt = copy.getCopiedNBT();
        copy.setCopiedID(null);
        copy.setCopiedMob(null);
        copy.setCopiedNBT(new CompoundTag());

        if (EventHooks.fireEntityTickPre(this.player).isCanceled()) return;

        copy.setCopiedID(id);
        copy.setCopiedMob(mob);
        copy.setCopiedNBT(nbt);

        if (!(this.player.level instanceof final ServerLevel world)) return;

        preBotTick(world);
        botTick(world);
        postBotTick(world);
    }

    @Override
    public void botTick(ServerLevel world)
    {
        final ICopyMob copy = ThutCaps.getCopyMob(player);
        copy.baseInit(world, player);
        LivingEntity living = copy.getCopiedMob();

        ICopyMob.copyEntityTransforms(living, player);
        ICopyMob.copyPositions(living, player);

        living.setId(-(player.getId() + 100));
        living.noPhysics = false;

        living.onAddedToLevel();
        living.tick();
        living.onRemovedFromLevel();

        living.setItemInHand(InteractionHand.MAIN_HAND, player.getItemInHand(InteractionHand.MAIN_HAND));
        living.setItemInHand(InteractionHand.OFF_HAND, player.getItemInHand(InteractionHand.OFF_HAND));

        living.level = player.level;

        var event = new CopyUpdateEvent(living, player);
        ThutCore.FORGE_BUS.post(event);
        if (!event.isCanceled())
        {
            living.setHealth(player.getHealth());
            living.setAirSupply(player.getAirSupply());
        }
    }

    @Override
    protected void postBotTick(ServerLevel world)
    {
        ICopyMob.copyEntityTransforms(this.player, this.mob);
        ICopyMob.copyPositions(this.player, this.mob);
        ICopyMob.copyRotations(this.player, this.mob);
    }

}

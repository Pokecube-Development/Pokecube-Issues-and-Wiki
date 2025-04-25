package thut.bot.entity.ai.modules;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.neoforged.neoforge.event.EventHooks;
import thut.api.ThutCaps;
import thut.api.attachments.CopyMob;
import thut.api.entity.ICopyMob;
import thut.bot.entity.BotPlayer;
import thut.bot.entity.ai.BotAI;
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
                SyncAttachments.syncChange(CopyMob.TYPE_COPY, this.player);
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
        SyncAttachments.syncChange(CopyMob.TYPE_COPY, this.player);
        getTag().remove("id");
    }

    @Override
    protected void preBotTick(ServerLevel level)
    {
        final ICopyMob copy = ThutCaps.getCopyMob(player);
        if (copy.getCopiedMob() instanceof PathfinderMob mob)
        {
            this.mob = mob;
        }
        else super.preBotTick(level);
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

        EventHooks.fireEntityTickPost(this.player);

        if (!(this.player.level instanceof final ServerLevel level)) return;

        preBotTick(level);
        botTick(level);
        postBotTick(level);
    }

    @Override
    public void botTick(ServerLevel level)
    {
        final ICopyMob copy = ThutCaps.getCopyMob(player);
        copy.setFullTick(true);
    }

    @Override
    protected void postBotTick(ServerLevel level)
    {
        ICopyMob.copyEntityTransforms(this.player, this.mob);
        ICopyMob.copyPositions(this.player, this.mob);
        ICopyMob.copyRotations(this.player, this.mob);
    }

}

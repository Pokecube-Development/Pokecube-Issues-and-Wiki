package thut.bot.entity.ai.modules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraftforge.common.ForgeHooks;
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
        final ICopyMob copy = CopyCaps.get(player);
        copy.setCopiedID(null);
        getTag().remove("id");
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
    public void tick()
    {
        final ICopyMob copy = CopyCaps.get(player);
        ResourceLocation id = copy.getCopiedID();
        LivingEntity mob = copy.getCopiedMob();
        CompoundTag nbt = copy.getCopiedNBT();
        copy.setCopiedID(null);
        copy.setCopiedMob(null);
        copy.setCopiedNBT(new CompoundTag());

        if (ForgeHooks.onLivingUpdate(this.player)) return;

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
        final ICopyMob copy = CopyCaps.get(player);
        copy.baseInit(world, player);
        LivingEntity living = copy.getCopiedMob();

        // For when the mob gets saved, but doesn't load correctly.
        null_living:
        if (living == null)
        {
            if (getTag().contains("id"))
            {
                ResourceLocation loc = new ResourceLocation(getTag().getString("id"));
                copy.setCopiedID(loc);
                copy.baseInit(world, player);
                living = copy.getCopiedMob();
                if (living != null)
                {
                    living.deserializeNBT(getTag().getCompound("mimic"));
                    break null_living;
                }
            }
            return;
        }

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

        // TODO move this somewhere like on removed from world, etc
        if (player.tickCount % 100 == 0) this.getTag().put("mimic", living.serializeNBT());
    }

    @Override
    protected void postBotTick(ServerLevel world)
    {
        ICopyMob.copyEntityTransforms(this.player, this.mob);
        ICopyMob.copyPositions(this.player, this.mob);
        ICopyMob.copyRotations(this.player, this.mob);
    }

}

package pokecube.core.handlers;

import java.lang.reflect.Field;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.events.PokedexInspectEvent;
import pokecube.core.handlers.playerdata.PokecubePlayerCustomData;
import pokecube.core.utils.Tools;
import thut.core.common.handlers.PlayerDataHandler;

public class PokedexInspector
{
    public static interface IInspectReward
    {
        boolean inspect(PokecubePlayerCustomData data, Entity entity, boolean giveReward);
    }

    public static class InspectCapturesReward implements IInspectReward
    {
        final ItemStack reward;
        final Field     configField;
        final String    message;
        final String    tagString;

        public InspectCapturesReward(ItemStack reward, Field configField, String message, String tagString)
        {
            this.reward = reward;
            this.configField = configField;
            this.message = message;
            this.tagString = tagString;
        }

        private boolean check(Entity entity, String configArg, CompoundNBT tag, ItemStack reward, int num,
                boolean giveReward)
        {
            if (reward == null || tag.getBoolean(this.tagString)) return false;
            if (this.matches(num, configArg))
            {
                if (giveReward)
                {
                    tag.putBoolean(this.tagString, true);
                    entity.sendMessage(new TranslationTextComponent(this.message));
                    final PlayerEntity PlayerEntity = (PlayerEntity) entity;
                    Tools.giveItem(PlayerEntity, reward);
                    PokecubePlayerDataHandler.saveCustomData(entity.getCachedUniqueIdString());
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean inspect(PokecubePlayerCustomData data, Entity entity, boolean giveReward)
        {
            final int num = CaptureStats.getNumberUniqueCaughtBy(entity.getUniqueID());
            try
            {
                return this.check(entity, (String) this.configField.get(PokecubeCore.getConfig()), data.tag,
                        this.reward, num, giveReward);
            }
            catch (final IllegalArgumentException e)
            {
                e.printStackTrace();
            }
            catch (final IllegalAccessException e)
            {
                e.printStackTrace();
            }
            return false;
        }

        private boolean matches(int num, String arg)
        {
            int required = 0;
            if (arg.contains("%")) required = (int) (Double.parseDouble(arg.replace("%", "")) * Database.spawnables
                    .size() / 100d);
            else required = (int) Double.parseDouble(arg);
            return required <= num;
        }
    }

    public static Set<IInspectReward> rewards = Sets.newHashSet();

    public static void init()
    {
        Database.loadRewards();
    }

    public static boolean inspect(PlayerEntity player, boolean reward)
    {
        PokedexInspectEvent evt;
        MinecraftForge.EVENT_BUS.post(evt = new PokedexInspectEvent(player, reward));
        if (evt.isCanceled())
        {
            final String uuid = evt.getEntity().getCachedUniqueIdString();
            PlayerDataHandler.getInstance().save(uuid);
        }
        return evt.isCanceled();
    }

    public PokedexInspector()
    {
        MinecraftForge.EVENT_BUS.register(this);
        PokedexInspector.rewards.clear();
        PokedexInspector.init();
    }

    @SubscribeEvent(receiveCanceled = false, priority = EventPriority.LOWEST)
    public void inspectEvent(PokedexInspectEvent evt)
    {
        final String uuid = evt.getEntity().getCachedUniqueIdString();
        final PokecubePlayerCustomData data = PlayerDataHandler.getInstance().getPlayerData(uuid).getData(
                PokecubePlayerCustomData.class);
        boolean done = false;
        for (final IInspectReward reward : PokedexInspector.rewards)
        {
            final boolean has = reward.inspect(data, evt.getEntity(), evt.shouldReward);
            done = done || has;
            if (done && !evt.shouldReward) break;
        }
        if (done) evt.setCanceled(true);
    }
}

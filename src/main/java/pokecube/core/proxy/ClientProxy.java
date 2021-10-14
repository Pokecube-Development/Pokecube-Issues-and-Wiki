package pokecube.core.proxy;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;

import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent.NewRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.healer.HealerTile;
import pokecube.core.client.PokecenterSound;
import pokecube.nbtedit.NBTEdit;

@OnlyIn(value = Dist.CLIENT)
public class ClientProxy extends CommonProxy
{
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = PokecubeCore.MODID, value = Dist.CLIENT)
    public static class RegistryEvents
    {
        @SubscribeEvent
        public static void onStart(final NewRegistry event)
        {
            PokecubeCore.proxy = new ClientProxy();
            NBTEdit.proxy = new pokecube.nbtedit.forge.ClientProxy();
        }
    }

    public static final Map<BlockPos, PokecenterSound> pokecenter_sounds = Maps.newHashMap();

    private static Map<String, ResourceLocation> players  = Maps.newHashMap();
    private static Map<String, ResourceLocation> urlSkins = Maps.newHashMap();

    public ClientProxy()
    {
    }

    @Override
    public Player getPlayer(final UUID uuid)
    {
        // This is null on single player, so we have an integrated server
        if (Minecraft.getInstance().getCurrentServer() == null) return super.getPlayer(uuid);
        // Otherwise ask the world for the player.
        return this.getWorld().getPlayerByUUID(uuid);
    }

    @Override
    public Player getPlayer()
    {
        return Minecraft.getInstance().player;
    }

    @Override
    public Level getWorld()
    {
        return Minecraft.getInstance().level;
    }

    @Override
    public ResourceLocation getPlayerSkin(final String name)
    {
        if (ClientProxy.players.containsKey(name)) return ClientProxy.players.get(name);
        final Minecraft minecraft = Minecraft.getInstance();
        SkullBlockEntity.updateGameprofile(new GameProfile((UUID) null, name), (profile) ->
        {
            final Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager()
                    .getInsecureSkinInformation(profile);
            ResourceLocation resourcelocation;
            if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) resourcelocation = minecraft.getSkinManager()
                    .registerTexture(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
            else
            {
                final UUID uuid = Player.createPlayerUUID(profile);
                resourcelocation = DefaultPlayerSkin.getDefaultSkin(uuid);
            }
            ClientProxy.players.put(name, resourcelocation);
        });
        return DefaultPlayerSkin.getDefaultSkin();
    }

    @Override
    public ResourceLocation getUrlSkin(final String urlSkin)
    {
        if (ClientProxy.urlSkins.containsKey(urlSkin)) return ClientProxy.urlSkins.get(urlSkin);
        try
        {
            final TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
            final File file0 = new File(Minecraft.getInstance().gameDirectory, "assets/");
            final String s = Hashing.sha1().hashUnencodedChars(FilenameUtils.getBaseName(urlSkin)).toString();
            final ResourceLocation resourcelocation = new ResourceLocation("skins/" + s);
            final File file1 = new File(new File(file0, "skins"), s.length() > 2 ? s.substring(0, 2) : "xx");
            file1.mkdirs();
            final File file2 = new File(file1, s);
            final HttpTexture downloadingtexture = new HttpTexture(file2, urlSkin, DefaultPlayerSkin.getDefaultSkin(),
                    true, () ->
                    {
                    });
            texturemanager.register(resourcelocation, downloadingtexture);
            ClientProxy.urlSkins.put(urlSkin, resourcelocation);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            return DefaultPlayerSkin.getDefaultSkin();
        }
        return ClientProxy.urlSkins.get(urlSkin);
    }

    @Override
    public void pokecenterloop(final HealerTile tileIn, final boolean play)
    {
        if (play && !ClientProxy.pokecenter_sounds.containsKey(tileIn.getBlockPos()))
        {
            final PokecenterSound sound = new PokecenterSound(tileIn);
            Minecraft.getInstance().getSoundManager().play(sound);
            ClientProxy.pokecenter_sounds.put(tileIn.getBlockPos(), sound);
        }
        else if (!play && ClientProxy.pokecenter_sounds.containsKey(tileIn.getBlockPos()))
        {
            final PokecenterSound sound = ClientProxy.pokecenter_sounds.remove(tileIn.getBlockPos());
            sound.stopped = true;
            Minecraft.getInstance().getSoundManager().stop(sound);
        }
    }
}

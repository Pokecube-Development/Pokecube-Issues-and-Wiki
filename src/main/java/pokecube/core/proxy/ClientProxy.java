package pokecube.core.proxy;

import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DownloadingTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.FoliageColors;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColors;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import org.apache.commons.io.FilenameUtils;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.healer.HealerTile;
import pokecube.core.client.PokecenterSound;
import pokecube.core.database.PokedexEntry;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.utils.PokeType;

import java.io.File;
import java.util.Map;
import java.util.UUID;

public class ClientProxy extends CommonProxy
{
    private static final Map<BlockPos, PokecenterSound> pokecenter_sounds = Maps.newHashMap();

    private static Map<String, ResourceLocation> players  = Maps.newHashMap();
    private static Map<String, ResourceLocation> urlSkins = Maps.newHashMap();

    @SubscribeEvent
    public void colourBlocks(final ColorHandlerEvent.Block event)
    {
        final Block pechaLeaves = BerryManager.berryLeaves.get(3);
        final Block qualotLeaves = BerryManager.berryLeaves.get(23);
//      System.out.println(pechaLeaves);
//      System.out.println(qualotLeaves);
        event.getBlockColors().register((state, reader, pos, tintIndex) ->
        {
            return reader != null && pos != null ? BiomeColors.getAverageFoliageColor(reader, pos)
                    : FoliageColors.getDefaultColor();
        }, pechaLeaves, qualotLeaves);
    }

    @SubscribeEvent
    public void colourItems(final ColorHandlerEvent.Item event)
    {
        final Block pechaLeaves = BerryManager.berryLeaves.get(3);
        final Block qualotLeaves = BerryManager.berryLeaves.get(23);
        event.getItemColors().register((stack, tintIndex) ->
        {
            final BlockState blockstate = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
            return event.getBlockColors().getColor(blockstate, null, null, tintIndex);
        }, pechaLeaves, qualotLeaves);

        event.getItemColors().register((stack, tintIndex) ->
        {
            final PokeType type = PokeType.unknown;
            final PokedexEntry entry = ItemPokemobEgg.getEntry(stack);
            if (entry != null) return tintIndex == 0 ? entry.getType1().colour : entry.getType2().colour;
            return tintIndex == 0 ? type.colour : 0xFFFFFFFF;
        }, PokecubeItems.EGG.get());

    }

    @Override
    public PlayerEntity getPlayer(final UUID uuid)
    {
        // This is null on single player, so we have an integrated server
        if (Minecraft.getInstance().getCurrentServer() == null) return super.getPlayer(uuid);
        // Otherwise ask the world for the player.
        return this.getWorld().getPlayerByUUID(uuid);
    }

    @Override
    public PlayerEntity getPlayer()
    {
        return Minecraft.getInstance().player;
    }

    @Override
    public World getWorld()
    {
        return Minecraft.getInstance().level;
    }

    @Override
    public ResourceLocation getPlayerSkin(final String name)
    {
        if (ClientProxy.players.containsKey(name)) return ClientProxy.players.get(name);
        final Minecraft minecraft = Minecraft.getInstance();
        GameProfile profile = new GameProfile((UUID) null, name);
        profile = SkullTileEntity.updateGameprofile(profile);
        final Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager()
                .getInsecureSkinInformation(profile);
        ResourceLocation resourcelocation;
        if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) resourcelocation = minecraft.getSkinManager().registerTexture(
                map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
        else
        {
            final UUID uuid = PlayerEntity.createPlayerUUID(profile);
            resourcelocation = DefaultPlayerSkin.getDefaultSkin(uuid);
        }
        ClientProxy.players.put(name, resourcelocation);
        return resourcelocation;
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
            final DownloadingTexture downloadingtexture = new DownloadingTexture(file2, urlSkin, DefaultPlayerSkin
                    .getDefaultSkin(), true, () ->
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
    public void serverAboutToStart(final FMLServerAboutToStartEvent event)
    {
        ClientProxy.pokecenter_sounds.clear();
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

package pokecube.core.proxy;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.texture.DownloadingTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.FoliageColors;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColors;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.healer.HealerTile;
import pokecube.core.client.MoveSound;
import pokecube.core.client.PokecenterSound;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.ItemGenerator;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;

public class ClientProxy extends CommonProxy
{


    private static Map<String, ResourceLocation> players  = Maps.newHashMap();
    private static Map<String, ResourceLocation> urlSkins = Maps.newHashMap();

    @SubscribeEvent
    public void colourBlocks(final ColorHandlerEvent.Block event)
    {
        final Block[] leaves = ItemGenerator.leaves.values().toArray(new Block[0]);
        event.getBlockColors().register((state, reader, pos, tintIndex) ->
        {
            return reader != null && pos != null ? BiomeColors.getFoliageColor(reader, pos)
                    : FoliageColors.getDefault();
        }, leaves);
    }

    @SubscribeEvent
    public void colourItems(final ColorHandlerEvent.Item event)
    {
        final Block[] leaves = ItemGenerator.leaves.values().toArray(new Block[0]);
        event.getItemColors().register((stack, tintIndex) ->
        {
            final BlockState blockstate = ((BlockItem) stack.getItem()).getBlock().getDefaultState();
            return event.getBlockColors().getColor(blockstate, null, null, tintIndex);
        }, leaves);

        event.getItemColors().register((stack, tintIndex) ->
        {
            final PokeType type = PokeType.unknown;
            final PokedexEntry entry = ItemPokemobEgg.getEntry(stack);
            if (entry != null) return tintIndex == 0 ? entry.getType1().colour : entry.getType2().colour;
            return tintIndex == 0 ? type.colour : 0xFFFFFFFF;
        }, PokecubeItems.EGG);

    }

    @Override
    public PlayerEntity getPlayer()
    {
        return Minecraft.getInstance().player;
    }

    @Override
    public World getWorld()
    {
        return Minecraft.getInstance().world;
    }

    @Override
    public ResourceLocation getPlayerSkin(final String name)
    {
        if (ClientProxy.players.containsKey(name)) return ClientProxy.players.get(name);
        final Minecraft minecraft = Minecraft.getInstance();
        GameProfile profile = new GameProfile((UUID) null, name);
        profile = SkullTileEntity.updateGameProfile(profile);
        final Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager()
                .loadSkinFromCache(profile);
        ResourceLocation resourcelocation;
        if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) resourcelocation = minecraft.getSkinManager().loadSkin(
                map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
        else
        {
            final UUID uuid = PlayerEntity.getUUID(profile);
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
            final File file0 = new File(Minecraft.getInstance().gameDir, "assets/");
            final String s = Hashing.sha1().hashUnencodedChars(FilenameUtils.getBaseName(urlSkin)).toString();
            final ResourceLocation resourcelocation = new ResourceLocation("skins/" + s);
            final File file1 = new File(new File(file0, "skins"), s.length() > 2 ? s.substring(0, 2) : "xx");
            file1.mkdirs();
            final File file2 = new File(file1, s);
            final DownloadingTexture downloadingtexture = new DownloadingTexture(file2, urlSkin, DefaultPlayerSkin
                    .getDefaultSkinLegacy(), true, () ->
                    {
                    });
            texturemanager.loadTexture(resourcelocation, downloadingtexture);
            ClientProxy.urlSkins.put(urlSkin, resourcelocation);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            return DefaultPlayerSkin.getDefaultSkinLegacy();
        }
        return ClientProxy.urlSkins.get(urlSkin);
    }

    private final Map<BlockPos, PokecenterSound> pokecenter_sounds = Maps.newHashMap();
    private final Map<SoundEvent, Integer>       move_sounds       = Maps.newHashMap();
    private final Map<SoundEvent, Float>         move_volumes      = Maps.newHashMap();
    private final Map<SoundEvent, Vector3>       move_positions    = Maps.newHashMap();

    // @SubscribeEvent
    public void worldTick(final ClientTickEvent event)
    {
        final Set<SoundEvent> stale = Sets.newHashSet();
        for (final Map.Entry<SoundEvent, Integer> entry : this.move_sounds.entrySet())
        {
            final Integer tick = entry.getValue() - 1;
            if (tick < 0) stale.add(entry.getKey());
            entry.setValue(tick);
        }
        final PlayerEntity player = Minecraft.getInstance().player;
        final Vector3 pos2 = Vector3.getNewVector().set(player);
        for (final SoundEvent e : stale)
        {
            final Vector3 pos = this.move_positions.remove(e);
            this.move_sounds.remove(e);
            final float scale = this.move_volumes.remove(e);
            final float volume = MoveSound.getVolume(pos, pos2, scale);
            if (volume > 0) Minecraft.getInstance().getSoundHandler().play(new MoveSound(e, pos, scale));
        }
    }

    @Override
    public void serverAboutToStart(final FMLServerAboutToStartEvent event)
    {
        this.move_positions.clear();
        this.move_sounds.clear();
        this.pokecenter_sounds.clear();
    }

    @Override
    public void moveSound(final Vector3 pos, final SoundEvent event, final float volume)
    {
        final ClientPlayerEntity player = Minecraft.getInstance().player;
        final Vector3 pos1 = Vector3.getNewVector().set(player);
        final double dist = pos1.distanceTo(pos);
        // Implement a speed of sound delay to this.
        final int delay = (int) (dist * 20.0 / 340.0);
        this.move_sounds.put(event, delay);
        this.move_positions.put(event, pos.copy());
        this.move_volumes.put(event, volume);
    }

    @Override
    public void pokecenterloop(final HealerTile tileIn, final boolean play)
    {
        if (play && !this.pokecenter_sounds.containsKey(tileIn.getPos()))
        {
            final PokecenterSound sound = new PokecenterSound(tileIn);
            Minecraft.getInstance().getSoundHandler().play(sound);
            this.pokecenter_sounds.put(tileIn.getPos(), sound);
        }
        else if (!play && this.pokecenter_sounds.containsKey(tileIn.getPos()))
        {
            final PokecenterSound sound = this.pokecenter_sounds.remove(tileIn.getPos());
            sound.stopped = true;
            Minecraft.getInstance().getSoundHandler().stop(sound);
        }

    }
}

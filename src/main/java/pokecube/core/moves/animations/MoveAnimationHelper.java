package pokecube.core.moves.animations;

import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent.RenderFogEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.moves.PokemobTerrainEffects;
import thut.api.maths.Vector3;
import thut.api.terrain.CapabilityTerrain.ITerrainProvider;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;
import thut.lib.CompatParser.ClassFinder;

public class MoveAnimationHelper
{
    static Map<String, Class<? extends MoveAnimationBase>> presets = Maps.newHashMap();

    static
    {
        List<Class<?>> foundClasses;
        try
        {
            foundClasses = ClassFinder.find(MoveAnimationHelper.class.getPackage().getName());
            for (final Class<?> candidateClass : foundClasses)
            {
                final AnimPreset preset = candidateClass.getAnnotation(AnimPreset.class);
                if (preset != null && MoveAnimationBase.class.isAssignableFrom(candidateClass))
                {
                    @SuppressWarnings("unchecked")
                    final Class<? extends MoveAnimationBase> presetClass = (Class<? extends MoveAnimationBase>) candidateClass;
                    MoveAnimationHelper.presets.put(preset.getPreset(), presetClass);
                }
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    private static MoveAnimationHelper instance;

    public static IMoveAnimation getAnimationPreset(String anim)
    {
        IMoveAnimation animation = null;
        if (anim == null || anim.isEmpty()) return animation;
        final String preset = anim.split(":")[0];
        final Class<? extends MoveAnimationBase> presetClass = MoveAnimationHelper.presets.get(preset);
        if (presetClass != null) try
        {
            animation = presetClass.newInstance();
            ((MoveAnimationBase) animation).init(anim);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        return animation;
    }

    public static MoveAnimationHelper Instance()
    {
        if (MoveAnimationHelper.instance == null)
        {
            MoveAnimationHelper.instance = new MoveAnimationHelper();
            MinecraftForge.EVENT_BUS.register(MoveAnimationHelper.instance);
        }
        return MoveAnimationHelper.instance;
    }

    final Vector3                 source     = Vector3.getNewVector();
    final Vector3                 target     = Vector3.getNewVector();
    final int                     index;

    private int                   effects    = 0;

    Map<BlockPos, TerrainSegment> terrainMap = Maps.newHashMap();

    public MoveAnimationHelper()
    {
        final TerrainSegment dummy = new TerrainSegment(0, 0, 0);
        int found = -1;
        for (int i = 0; i < dummy.effectArr.length; i++)
            if (dummy.effectArr[i] instanceof PokemobTerrainEffects)
            {
                found = i;
                break;
            }
        this.index = found;
    }

    public void addEffect()
    {
        this.effects++;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void chunkUnload(ChunkEvent.Unload evt)
    {
        if (!evt.getWorld().isRemote()) return;
        for (int i = 0; i < 16; i++)
            this.terrainMap.remove(new BlockPos(evt.getChunk().getPos().x, i, evt.getChunk().getPos().z));
    }

    public void clear()
    {
        this.effects = 0;
    }

    public void clearEffect()
    {
        this.effects = Math.max(0, this.effects - 1);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onCapabilityAttach(AttachCapabilitiesEvent<Chunk> event)
    {
        if (!event.getObject().getWorld().isRemote) return;
        if (event.getCapabilities().containsKey(TerrainManager.TERRAINCAP))
        {
            final ITerrainProvider provider = (ITerrainProvider) event.getCapabilities().get(TerrainManager.TERRAINCAP);
            for (int i = 0; i < 16; i++)
                this.terrainMap.put(new BlockPos(event.getObject().getPos().x, i, event.getObject().getPos().z),
                        provider.getTerrainSegment(i));
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRenderWorldPost(RenderFogEvent event)
    {
        if (this.effects == 0) return;
        int num = 0;
        try
        {
            if (this.index == -1) return;
            final PlayerEntity player = Minecraft.getInstance().player;
            this.source.set(player);
            final int range = 4;
            final BlockPos.Mutable pos = new BlockPos.Mutable();
            for (int i = -range; i <= range; i++)
                for (int j = -range; j <= range; j++)
                    for (int k = -range; k <= range; k++)
                    {
                        this.source.set(player);
                        pos.setPos(player.chunkCoordX + i, player.chunkCoordY + j, player.chunkCoordZ + k);
                        final TerrainSegment segment = this.terrainMap.get(pos);
                        if (segment == null) continue;
                        final PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.effectArr[this.index];
                        if (teffect == null || !teffect.hasEffects()) continue;
                        this.target.set(segment.getCentre());
                        this.source.set(this.target.subtractFrom(this.source));
                        // Clear out the jitteryness from rendering
                        final double d0 = (-player.posX + player.lastTickPosX) * event.getRenderPartialTicks();
                        final double d1 = (-player.posY + player.lastTickPosY) * event.getRenderPartialTicks();
                        final double d2 = (-player.posZ + player.lastTickPosZ) * event.getRenderPartialTicks();
                        this.source.addTo(d0, d1, d2);
                        GL11.glPushMatrix();
                        GL11.glTranslated(this.source.x, this.source.y, this.source.z);
                        teffect.renderTerrainEffects(event);
                        GL11.glPopMatrix();
                        num++;
                    }
        }
        catch (final Throwable e)
        {
            e.printStackTrace();
        }
        this.effects = num;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void worldLoad(WorldEvent.Load evt)
    {
        if (!evt.getWorld().isRemote()) return;
        this.terrainMap.clear();
    }
}

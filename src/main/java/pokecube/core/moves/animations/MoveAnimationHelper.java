package pokecube.core.moves.animations;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
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

    public static IMoveAnimation getAnimationPreset(final String anim)
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

    final Vector3 source = Vector3.getNewVector();
    final Vector3 target = Vector3.getNewVector();
    final int     index;

    private int effects = 0;

    public Map<BlockPos, TerrainSegment> terrainMap = Maps.newHashMap();

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
    public void chunkUnload(final ChunkEvent.Unload evt)
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
    public void onCapabilityAttach(final AttachCapabilitiesEvent<Chunk> event)
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
    public void onRenderWorldPost(final RenderWorldLastEvent event)
    {
        if (this.effects == 0) return;
        int num = 0;
        try
        {
            if (this.index == -1) return;
            final PlayerEntity player = Minecraft.getInstance().player;
            this.source.set(player);
            final int range = 4;

            final Minecraft mc = Minecraft.getInstance();
            final Vec3d projectedView = mc.gameRenderer.getActiveRenderInfo().getProjectedView();
            final MatrixStack mat = event.getMatrixStack();
            mat.push();
            mat.translate(-projectedView.x, -projectedView.y, -projectedView.z);

            final BlockPos.Mutable pos = new BlockPos.Mutable();
            for (int i = -range; i <= range; i++)
                for (int j = -range; j <= range; j++)
                    for (int k = -range; k <= range; k++)
                    {
                        pos.setPos(player.chunkCoordX + i, player.chunkCoordY + j, player.chunkCoordZ + k);
                        final TerrainSegment segment = this.terrainMap.get(pos);
                        if (segment == null) continue;
                        final PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.effectArr[this.index];
                        if (teffect == null || !teffect.hasEffects()) continue;
                        this.target.set(segment.getCentre());
                        this.target.add(-8, -8, -8);
                        mat.push();
                        teffect.renderTerrainEffects(event, target);
                        mat.pop();
                        num++;
                    }
            mat.pop();
        }
        catch (final Throwable e)
        {
            e.printStackTrace();
        }
        this.effects = num;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void worldLoad(final WorldEvent.Load evt)
    {
        if (!evt.getWorld().isRemote()) return;
        this.terrainMap.clear();
    }
}

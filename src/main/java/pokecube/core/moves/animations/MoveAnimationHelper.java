package pokecube.core.moves.animations;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;

import org.objectweb.asm.Type;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.moves.PokemobTerrainEffects;
import thut.api.maths.Vector3;
import thut.api.terrain.CapabilityTerrain.ITerrainProvider;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;
import thut.lib.CompatParser.ClassFinder;

public class MoveAnimationHelper
{
    private static final Type PRESETANNOTATION = Type.getType("Lpokecube/core/moves/animations/AnimPreset;");

    static Map<String, Class<? extends MoveAnimationBase>> presets = Maps.newHashMap();

    private static final BiFunction<ModFile, String, Boolean> validClass = (file, name) ->
    {
        for (final AnnotationData a : file.getScanResult().getAnnotations())
            if (name.equals(a.clazz().getClassName()) && a.annotationType().equals(
                    MoveAnimationHelper.PRESETANNOTATION)) return true;
        return false;
    };

    static
    {
        Collection<Class<?>> foundClasses;
        try
        {
            foundClasses = ClassFinder.find(MoveAnimationHelper.class.getPackage().getName(),
                    MoveAnimationHelper.validClass);
            for (final Class<?> candidateClass : foundClasses)
            {
                if (!MoveAnimationBase.class.isAssignableFrom(candidateClass)) continue;
                if (candidateClass.getAnnotations().length == 0) continue;
                final AnimPreset preset = candidateClass.getAnnotation(AnimPreset.class);
                if (preset != null)
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
            animation = presetClass.getConstructor().newInstance();
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
        if (!evt.getWorld().isClientSide()) return;
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
    public void onCapabilityAttach(final AttachCapabilitiesEvent<LevelChunk> event)
    {
        if (!event.getObject().getLevel().isClientSide) return;
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
    public void onRenderWorldPost(final RenderLevelLastEvent event)
    {
        if (this.effects == 0) return;
        int num = 0;
        try
        {
            if (this.index == -1) return;
            final Player player = Minecraft.getInstance().player;
            this.source.set(player);
            final int range = 4;

            final Minecraft mc = Minecraft.getInstance();
            final Vec3 projectedView = mc.gameRenderer.getMainCamera().getPosition();
            final PoseStack mat = event.getPoseStack();
            mat.pushPose();
            mat.translate(-projectedView.x, -projectedView.y, -projectedView.z);
            final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

            final int x = player.getBlockX() >> 4;
            final int y = player.getBlockY() >> 4;
            final int z = player.getBlockZ() >> 4;

            for (int i = -range; i <= range; i++)
                for (int j = -range; j <= range; j++)
                    for (int k = -range; k <= range; k++)
                    {
                        pos.set(x + i, y + j, z + k);
                        final TerrainSegment segment = this.terrainMap.get(pos.immutable());
                        if (segment == null) continue;
                        final PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.effectArr[this.index];
                        if (teffect == null || !teffect.hasEffects()) continue;
                        this.target.set(segment.getCentre());
                        this.target.add(-8, -8, -8);
                        mat.pushPose();
                        teffect.renderTerrainEffects(event, this.target);
                        mat.popPose();
                        num++;
                    }
            mat.popPose();
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
        if (!evt.getWorld().isClientSide()) return;
        this.terrainMap.clear();
    }
}

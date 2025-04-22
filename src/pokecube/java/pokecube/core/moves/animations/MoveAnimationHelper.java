package pokecube.core.moves.animations;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.moddiscovery.ModFile;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import net.neoforged.neoforgespi.language.ModFileScanData.AnnotationData;
import org.objectweb.asm.Type;
import pokecube.api.moves.utils.IMoveAnimation;
import pokecube.core.moves.PokemobTerrainEffects;
import thut.api.level.terrain.TerrainSegment;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;
import thut.lib.CompatParser.ClassFinder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;

public class MoveAnimationHelper
{
    private static final Type PRESETANNOTATION = Type.getType("Lpokecube/core/moves/animations/AnimPreset;");

    static Map<String, Class<? extends MoveAnimationBase>> presets = Maps.newHashMap();

    private static final BiFunction<ModFile, String, Boolean> validClass = (file, name) -> {
        for (final AnnotationData a : file.getScanResult().getAnnotations())
            if (name.equals(a.clazz().getClassName()) && a.annotationType()
                    .equals(MoveAnimationHelper.PRESETANNOTATION)) return true;
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

    public static IMoveAnimation getAnimationPreset(final String preset, JsonObject values)
    {
        IMoveAnimation animation = null;
        if (preset == null || preset.isEmpty()) return animation;
        final Class<? extends MoveAnimationBase> presetClass = MoveAnimationHelper.presets.get(preset);
        if (presetClass != null) try
        {
            animation = presetClass.getConstructor().newInstance();
            ((MoveAnimationBase) animation).init(values);
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
            ThutCore.FORGE_BUS.register(MoveAnimationHelper.instance);
        }
        return MoveAnimationHelper.instance;
    }

    final Vector3 source = new Vector3();
    final Vector3 target = new Vector3();
    final int index;
    Lock mutex = new ReentrantLock();
    Set<PokemobTerrainEffects> effects = new HashSet<>();

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

    public void addForRender(PokemobTerrainEffects effect)
    {
        mutex.lock();
        effects.add(effect);
        mutex.unlock();
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRenderWorldPost(final RenderLevelStageEvent event)
    {
        if (event.getStage() != Stage.AFTER_SOLID_BLOCKS) return;
        if (this.index == -1) return;
        mutex.lock();
        Set<PokemobTerrainEffects> run = new HashSet<>(effects);
        mutex.unlock();

        final Player player = Minecraft.getInstance().player;
        this.source.set(player);

        final Minecraft mc = Minecraft.getInstance();
        final Vec3 projectedView = mc.gameRenderer.getMainCamera().getPosition();
        final PoseStack mat = event.getPoseStack();
        mat.pushPose();
        mat.translate(-projectedView.x, -projectedView.y, -projectedView.z);

        run.removeIf(teffect -> {
            if (!teffect.hasEffects()) return false;
            try
            {
                this.target.set(teffect.segment.getCentre());
                this.target.add(-8, -8, -8);
                mat.pushPose();
                teffect.renderTerrainEffects(event, this.target);
                mat.popPose();
            }
            catch (final Throwable e)
            {
                e.printStackTrace();
            }
            return true;
        });
        mat.popPose();

        mutex.lock();
        effects.removeAll(run);
        mutex.unlock();
    }
}

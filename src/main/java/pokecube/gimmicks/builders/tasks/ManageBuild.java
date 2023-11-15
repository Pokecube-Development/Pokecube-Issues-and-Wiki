package pokecube.gimmicks.builders.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.common.base.Predicates;
import com.google.gson.JsonObject;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.TeamManager;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.core.ai.tasks.utility.StoreTask;
import pokecube.core.ai.tasks.utility.UtilTask;
import pokecube.gimmicks.builders.BuilderTasks;
import pokecube.gimmicks.builders.builders.JigsawBuilder;
import pokecube.gimmicks.builders.builders.StructureBuilder;
import pokecube.gimmicks.builders.builders.StructureBuilder.BoMRecord;
import pokecube.world.gen.structures.configs.ExpandedJigsawConfiguration;
import pokecube.world.gen.structures.configs.ExpandedJigsawConfiguration.AvoidanceSettings;
import pokecube.world.gen.structures.configs.ExpandedJigsawConfiguration.ClearanceSettings;
import pokecube.world.gen.structures.configs.ExpandedJigsawConfiguration.YSettings;
import pokecube.world.gen.structures.pieces.ExpandedPoolElementStructurePiece;
import pokecube.world.gen.structures.utils.ExpandedJigsawPacement;
import thut.api.util.JsonUtil;

/**
 * This IAIRunnable assigns building tasks based on the instructions in a book
 * in the offhand slot.<br>
 * <br>
 * Format for the instructions is as follows:<br>
 * First page of a writable book should say:<br>
 * first line: t:key where key is jigsaw or building<br>
 * second line: resource location to look up<br>
 * following lines (optional):<br>
 * p: x y z - coordinates to shift by r: ROTATION - rotates the structure,
 * ROTATION is NONE, CLOCKWISE_90, CLOCKWISE_180 or COUNTERCLOCKWISE_90 <br>
 * <br>
 * Example book:<br>
 * t:jigsaw<br>
 * pokecube_legends:temples/surface/sky_pillar<br>
 * p: 0 1 0<br>
 * r: CLOCKWISE_90<br>
 * m: NONE<br>
 */
public class ManageBuild extends UtilTask implements INBTSerializable<CompoundTag>
{
    public static final String KEY = "builder_manager";

    final StoreTask storage;

    boolean hasInstructions = false;
    boolean loadedBuild = false;
    ItemStack last = ItemStack.EMPTY;
    StructureBuilder builder;
    JigsawBuilder jigsaw;
    int timer = 0;
    List<IPokemob> minions = new ArrayList<>();

    public ManageBuild(IPokemob pokemob, StoreTask storage)
    {
        super(pokemob);
        this.storage = storage;
    }

    @Override
    public void reset()
    {
        hasInstructions = false;
        jigsaw = null;
        builder = null;
    }

    public void setBuilder(StructureBuilder builder, ServerLevel level, List<IPokemob> pokemobs)
    {
        var pair = storage.getInventory(level, storage.storageLoc, Direction.UP);
        if (pair == null) return;
        // Jigsaw sets the key provider based on the loaded jigsaw, otherwise we
        // need to set it based on our offhand item
        if (jigsaw == null)
        {
            final IItemHandlerModifiable keySource = new InvWrapper(pokemob.getInventory());
            builder.keyProvider = StructureBuilder.makeForSource(builder, keySource, keySource.getSlots() - 1);
        }
        else builder.keyProvider = StructureBuilder.loadFromTemplate(builder);

        this.builder = builder;

        for (var pokemob : pokemobs)
        {
            var task = pokemob.getNamedTaskes().get(DoBuild.KEY);
            if (!(task instanceof DoBuild build) || build.builder != null) continue;

            build.storage.storageLoc = storage.storageLoc;
            build.storage.storageFace = storage.storageFace;

            final IItemHandlerModifiable itemSource = new InvWrapper(pokemob.getInventory());
            builder.itemSource = itemSource;
            if (!builder.workers.contains(pokemob.getEntity()))
            {
                if (pokemob != this.pokemob)
                {
                    builder.addBoMRecord(new BoMRecord(() -> pokemob.getEntity().getOffhandItem(),
                            _book -> pokemob.getEntity().setItemInHand(InteractionHand.OFF_HAND, _book)));
                }
                else
                {
                    builder.addBoMRecord(new BoMRecord(() -> pair.getFirst().getStackInSlot(0),
                            _book -> pair.getFirst().setStackInSlot(0, _book)));
                }
                builder.workers.add(pokemob.getEntity());
            }
            if (!minions.contains(pokemob)) minions.add(pokemob);
            build.setBuilder(builder, level);
        }
    }

    @Override
    public void run()
    {
        var storeLoc = storage.storageLoc;
        // Only run if we actually have storage (and are server side)
        if (storeLoc == null || !(entity.level instanceof ServerLevel level)) return;

        // Only run this every 2.5 seconds or so
        if (entity.tickCount - timer < 50) return;
        timer = entity.tickCount;

        // Initialise the level, this ensures that it loads properly from nbt if
        // saved. This also calls an initial init for all of the builders
        if (jigsaw != null && jigsaw.level == null) jigsaw.setLevel(level);

        // Debug prints for jigsaws
//        System.out.println(this.jigsaw + " " + (this.jigsaw != null ? this.jigsaw.builders.size() : 0));
//        if (jigsaw != null && jigsaw.builders.size() > 0) System.out.println(this.jigsaw.builders.get(0).workers);

        // Building managers sit down near their storage.
        if (storeLoc.distManhattan(entity.getOnPos()) > 3)
        {
            // Path to it if too far.
            setWalkTo(storeLoc, 1, 1);
        }
        else
        {
            // Otherwise just sit down.
            pokemob.setLogicState(LogicStates.SITTING, true);
        }

        // Here we find a set of minions to make do the work
        List<IPokemob> pokemobs = new ArrayList<>();
        if (this.entity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES))
        {
            Iterable<LivingEntity> visible = this.entity.getBrain()
                    .getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().findAll(e ->
                    {
                        IPokemob p = PokemobCaps.getPokemobFor(e);
                        if (p == null) return false;
                        if (!TeamManager.sameTeam(e, entity)) return false;
                        return p.getGeneralState(GeneralStates.STAYING) && p.isRoutineEnabled(BuilderTasks.BUILD)
                                && p.getNamedTaskes().containsKey(DoBuild.KEY);
                    });
            // Only allow valid guard targets.
            for (var o : visible) pokemobs.add(PokemobCaps.getPokemobFor(o));
        }

        // If no minions found, I guess we do it ourselves.
        if (pokemobs.isEmpty() && pokemob.isRoutineEnabled(BuilderTasks.BUILD) && minions.isEmpty())
        {
            pokemobs.add(pokemob);
        }

        // If we had found minions before, re-add them now.
        if (!minions.isEmpty())
        {
            for (var m : minions) if (!pokemobs.contains(m) && m.getEntity().isAddedToWorld()) pokemobs.add(m);
        }

        // No workers?? (maybe we are not allowed to build via AI button)
        if (pokemobs.isEmpty()) return;

        // If we have a builder, check the done status and reset if needed.
        if (builder != null)
        {
            if (builder.done)
            {
                if (builder.passes++ > 3)
                {
                    if (jigsaw != null) jigsaw.builders.remove(0);
                    for (var m : minions)
                    {
                        var task = m.getNamedTaskes().get(DoBuild.KEY);
                        if (!(task instanceof DoBuild build) || build.builder != builder) continue;
                        build.builder = null;
                    }
                    builder = null;
                }
                else
                {
                    builder._template = null;
                }
            }
        }

        // If we don't have a builder, reset the minions and then cycle the
        // builder from jigsaw if possible, otherwise reset.
        if (builder == null)
        {
            for (var m : minions)
            {
                var task = m.getNamedTaskes().get(DoBuild.KEY);
                if (!(task instanceof DoBuild build)) continue;
                build.builder = null;
            }
            minions.clear();
            if (jigsaw != null)
            {
                while (!jigsaw.builders.isEmpty())
                {
                    if (!jigsaw.builders.get(0).done || jigsaw.builders.get(0).passes < 3)
                    {
                        break;
                    }
                    jigsaw.builders.remove(0);
                }
            }

            if (jigsaw == null || jigsaw.builders.isEmpty())
            {

                // Swap held items in this case. This prevents us immediately
                // trying to make a jigsaw again.
                ItemStack main = entity.getMainHandItem();
                ItemStack off = entity.getOffhandItem();
                entity.setItemInHand(InteractionHand.MAIN_HAND, off);
                entity.setItemInHand(InteractionHand.OFF_HAND, main);

                reset();
                return;
            }

            this.setBuilder(jigsaw.builders.get(0), level, pokemobs);
        }
        // Otherwise order minions to work on our builder.
        else this.setBuilder(builder, level, pokemobs);
    }

    @Override
    public boolean shouldRun()
    {
        if (loadedBuild)
        {
            loadedBuild = false;
            last = pokemob.getEntity().getOffhandItem();
            hasInstructions = true;
        }

        if (last != pokemob.getEntity().getOffhandItem() && entity.level instanceof ServerLevel level)
        {
            boolean hadInstructions = hasInstructions;
            last = pokemob.getEntity().getOffhandItem();
            hasInstructions = false;

            var pair = storage.getInventory(level, storage.storageLoc, Direction.UP);
            if (pair == null) return false;

            BlockPos origin = pokemob.getHome();
            check:
            if (last.hasTag() && last.getOrCreateTag().get("pages") instanceof ListTag list && !list.isEmpty()
                    && list.get(0) instanceof StringTag entry)
            {
                try
                {
                    ResourceLocation toMake = null;
                    String string = entry.getAsString();
                    BlockPos shift = new BlockPos(0, 0, 0);
                    if (!string.startsWith("{")) string = "{\"text\":\"" + string + "\"}";
                    var parsed = JsonUtil.gson.fromJson(string, JsonObject.class);
                    String[] lines = parsed.get("text").getAsString().strip().split("\n");

                    String type = lines[0].replace("t:", "").strip();

                    if (!(type.equals("jigsaw") || type.equals("building"))) break check;

                    if (lines.length >= 2) toMake = new ResourceLocation(lines[1]);

                    String rotation = "NONE";
                    String offset = "0 0 0";
                    String mirror = "NONE";
                    int jigsawDepth = 4;

                    for (int i = 2; i < lines.length; i++)
                    {
                        String line = lines[i];
                        if (line.startsWith("p:")) offset = line.replace("p:", "").strip();
                        if (line.startsWith("r:")) rotation = line.replace("r:", "").strip();
                        if (line.startsWith("m:")) rotation = line.replace("m:", "").strip();
                        if (line.startsWith("d:")) jigsawDepth = Integer.parseInt(line.replace("d:", "").strip());
                    }

                    Rotation rot = Rotation.NONE;
                    try
                    {
                        rot = Rotation.valueOf(rotation.toUpperCase(Locale.ROOT));
                    }
                    catch (Exception e)
                    {
                        PokecubeAPI.LOGGER.error(e);
                    }

                    Mirror mir = Mirror.NONE;
                    try
                    {
                        mir = Mirror.valueOf(mirror.toUpperCase(Locale.ROOT));
                    }
                    catch (Exception e)
                    {
                        PokecubeAPI.LOGGER.error(e);
                    }

                    var args = offset.contains(",") ? offset.split(",") : offset.split(" ");
                    int dx = 0;
                    int dy = 0;
                    int dz = 0;

                    if (args.length == 1)
                    {
                        dy = Integer.parseInt(args[0]);
                    }
                    else if (args.length == 3)
                    {
                        dx = Integer.parseInt(args[0]);
                        dy = Integer.parseInt(args[1]);
                        dz = Integer.parseInt(args[2]);
                    }
                    shift = new BlockPos(dx, dy, dz);

                    if (toMake != null)
                    {
                        final IItemHandlerModifiable itemSource = new InvWrapper(this.pokemob.getInventory());
                        if (type.equals("building"))
                        {
                            builder = new StructureBuilder(origin.offset(shift), rot, mir, itemSource);
                            builder.toMake = toMake;
                            hasInstructions = true;
                        }
                        else
                        {
                            var poolHolder = level.registryAccess().registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY)
                                    .getHolderOrThrow(ResourceKey.create(Registry.TEMPLATE_POOL_REGISTRY, toMake));
                            ExpandedJigsawConfiguration config = new ExpandedJigsawConfiguration(poolHolder,
                                    jigsawDepth, YSettings.DEFAULT, ClearanceSettings.DEFAULT, new ArrayList<>(), "",
                                    "", "none", Heightmap.Types.WORLD_SURFACE_WG, new ArrayList<>(), 0, 0,
                                    AvoidanceSettings.DEFAULT);
                            var context = new PieceGeneratorSupplier.Context<ExpandedJigsawConfiguration>(
                                    level.getChunkSource().getGenerator(),
                                    level.getChunkSource().getGenerator().getBiomeSource(), level.getSeed(),
                                    new ChunkPos(origin), config, level, Predicates.alwaysTrue(),
                                    level.getStructureManager(), level.registryAccess());
                            WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
                            worldgenrandom.setLargeFeatureSeed(context.seed(), context.chunkPos().x,
                                    context.chunkPos().z);
                            var make = ExpandedJigsawPacement.addPieces(context, ExpandedPoolElementStructurePiece::new,
                                    origin, false, false, worldgenrandom, rot);
                            if (make.isPresent())
                            {
                                StructurePiecesBuilder structurepiecesbuilder = new StructurePiecesBuilder();
                                worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
                                worldgenrandom.setLargeFeatureSeed(context.seed(), context.chunkPos().x,
                                        context.chunkPos().z);
                                var buildContext = new PieceGenerator.Context<ExpandedJigsawConfiguration>(config,
                                        level.getChunkSource().getGenerator(), level.getStructureManager(),
                                        new ChunkPos(origin), level, worldgenrandom, level.getSeed());
                                make.get().generatePieces(structurepiecesbuilder, buildContext);

                                this.jigsaw = new JigsawBuilder(structurepiecesbuilder, shift, itemSource, level);
                                hasInstructions = true;
                                System.out.println("New Jigsaw!");
                            }
                            else
                            {
                                System.out.println("Jigsaw failed to generate!");
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    PokecubeAPI.LOGGER.error("Error loading building instructions", e);
                }
            }

            if (hadInstructions && !hasInstructions)
            {
                for (var m : minions)
                {
                    var task = m.getNamedTaskes().get(DoBuild.KEY);
                    if (!(task instanceof DoBuild build) || build.builder == null) continue;
                    build.reset();
                }
            }
            timer = entity.tickCount;
        }
        return hasInstructions;
    }

    @Override
    public String getIdentifier()
    {
        return KEY;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag nbt = new CompoundTag();
        if (this.jigsaw != null)
        {
            nbt.put("jigsaw", this.jigsaw.serializeNBT());
        }
        else if (this.builder != null)
        {
            nbt.put("builder", this.builder.serializeNBT());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        if (nbt.contains("jigsaw", Tag.TAG_COMPOUND))
        {
            this.jigsaw = new JigsawBuilder();
            this.jigsaw.deserializeNBT(nbt.getCompound("jigsaw"));
            hasInstructions = true;
            loadedBuild = true;
        }
        else if (nbt.contains("builder", Tag.TAG_COMPOUND))
        {
            this.builder = new StructureBuilder();
            this.builder.deserializeNBT(nbt.getCompound("builder"));
            hasInstructions = true;
            loadedBuild = true;
        }
    }

}

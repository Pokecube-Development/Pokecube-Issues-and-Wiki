package pokecube.core.ai.tasks.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.google.common.base.Predicates;
import com.google.gson.JsonObject;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.ai.tasks.utility.builders.JigsawBuilder;
import pokecube.core.ai.tasks.utility.builders.StructureBuilder;
import pokecube.core.ai.tasks.utility.builders.StructureBuilder.BoMRecord;
import pokecube.core.ai.tasks.utility.builders.StructureBuilder.PlaceInfo;
import pokecube.world.gen.structures.configs.ExpandedJigsawConfiguration;
import pokecube.world.gen.structures.configs.ExpandedJigsawConfiguration.AvoidanceSettings;
import pokecube.world.gen.structures.configs.ExpandedJigsawConfiguration.ClearanceSettings;
import pokecube.world.gen.structures.configs.ExpandedJigsawConfiguration.YSettings;
import pokecube.world.gen.structures.pieces.ExpandedPoolElementStructurePiece;
import pokecube.world.gen.structures.utils.ExpandedJigsawPacement;
import thut.api.maths.Vector3;
import thut.api.util.JsonUtil;
import thut.core.common.ThutCore;
import thut.lib.ItemStackTools;

/**
 * This IAIRunnable gets the mob to build a structure as defined in a book given
 * to it in the offhand slot.
 */
public class BuildStructureTask extends UtilTask
{
    final StoreTask storage;

    boolean hasInstructions = false;
    ItemStack last = ItemStack.EMPTY;
    StructureBuilder builder;
    JigsawBuilder jigsaw;

    boolean findingSpot = false;
    boolean gettingPart = false;

    boolean makeBorder = false;

    PlaceInfo nextPlace = null;
    int pathTimeout = -1;

    Vector3 seeking = new Vector3();

    Vector3 v = new Vector3();
    Vector3 v1 = new Vector3();

    List<Integer> ys = null;

    public BuildStructureTask(IPokemob pokemob, StoreTask storage)
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
        ys = null;
        makeBorder = true;
    }

    private boolean checkValid(ItemStack stack, List<ItemStack> requested)
    {
        for (ItemStack v : requested) if (ItemStack.isSame(v, stack)) return true;
        return false;
    }

    public void setBuilder(StructureBuilder builder, ServerLevel level)
    {
        var pair = storage.getInventory(level, storage.storageLoc, Direction.UP);
        if (pair == null)
        {
            reset();
            return;
        }
        builder.BoM = new BoMRecord(() -> pair.getFirst().getStackInSlot(0),
                _book -> pair.getFirst().setStackInSlot(0, _book));
        builder.checkBlueprint(level);
        builder.provideBoM();
        ys = new ArrayList<>(builder.removeOrder.keySet());
        this.builder = builder;
        hasInstructions = true;
        makeBorder = true;
        builder.creative = pokemob.getOwner() instanceof ServerPlayer player && player.isCreative();

        if (builder.done && jigsaw == null) reset();
    }

    @Override
    public void run()
    {
        var storeLoc = storage.storageLoc;
        if (storeLoc == null || !(entity.level instanceof ServerLevel level)) return;

        if (builder == null)
        {
            if (jigsaw == null || jigsaw.builders.isEmpty())
            {
                reset();
                return;
            }
            this.setBuilder(jigsaw.builders.get(0), level);
        }
        if (builder == null || builder.done || builder._template == null)
        {
            if (jigsaw != null) jigsaw.builders.remove(0);
            builder = null;
            if (jigsaw != null && !jigsaw.builders.isEmpty()) this.setBuilder(jigsaw.builders.get(0), level);
            return;
        }

        if (entity.tickCount % 40 == 0) builder.checkBoM();

        IItemHandlerModifiable itemhandler = builder.itemSource;

        var clearSpot = builder.nextRemoval(ys, level);
        boolean isClear = clearSpot == null;
        pathTimeout--;

        if (!isClear)
        {
            double diff = 5;
            diff = Math.max(diff, this.entity.getBbWidth());
            if (entity.getOnPos().distSqr(clearSpot) > diff)
            {
                this.setWalkTo(clearSpot, 1, 0);
                if (pathTimeout < 0) pathTimeout = 40;
            }
            if (pathTimeout < 20 || builder.creative)
            {
                if (!storage.canBreak(level, clearSpot))
                {
                    // Notify that we can't actually break this.
                    double size = pokemob.getMobSizes().mag();
                    double x = this.entity.getX();
                    double y = this.entity.getY();
                    double z = this.entity.getZ();

                    Random r = ThutCore.newRandom();
                    for (int l = 0; l < 2; l++)
                    {
                        double i = r.nextGaussian() * size;
                        double j = r.nextGaussian() * size;
                        double k = r.nextGaussian() * size;
                        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, x + i, y + j, z + k, 1, 0, 0, 0, 0);
                    }
                }
                else
                {
                    BlockState state = level.getBlockState(clearSpot);
                    final List<ItemStack> list = Block.getDrops(state, level, clearSpot,
                            level.getBlockEntity(clearSpot));
                    list.removeIf(stack -> ItemStackTools.addItemStackToInventory(stack, builder.itemSource, 1));
                    list.forEach(c -> {
                        int x = clearSpot.getX();
                        int z = clearSpot.getZ();
                        ItemEntity item = new ItemEntity(level, x + 0.5, clearSpot.getY() + 0.5, z + 0.5, c);
                        level.addFreshEntity(item);
                    });
                    level.destroyBlock(clearSpot, false);

                    // TODO ensure this walks to storage first, and only when
                    // nearly full.
                    storage.doStorageCheck(itemhandler);
                }
            }
        }
        else if (makeBorder)
        {
            var origin = builder.origin;
            var size = builder._template.getSize();

            // Mark corners
            BlockPos p1 = origin.offset(-1, 0, -1);
            BlockPos p2 = origin.offset(size.getX() + 1, 0, size.getZ() + 1);
            BlockPos p3 = origin.offset(size.getX() + 1, 0, -1);
            BlockPos p4 = origin.offset(-1, 0, size.getZ() + 1);

            level.setBlockAndUpdate(level.getHeightmapPos(Types.MOTION_BLOCKING, p1), Blocks.TORCH.defaultBlockState());
            level.setBlockAndUpdate(level.getHeightmapPos(Types.MOTION_BLOCKING, p2), Blocks.TORCH.defaultBlockState());
            level.setBlockAndUpdate(level.getHeightmapPos(Types.MOTION_BLOCKING, p3), Blocks.TORCH.defaultBlockState());
            level.setBlockAndUpdate(level.getHeightmapPos(Types.MOTION_BLOCKING, p4), Blocks.TORCH.defaultBlockState());

            makeBorder = false;
        }
        else if (findingSpot)
        {
            if (nextPlace == null)
            {
                nextPlace = builder.getNextPlacement(level);
            }
            if (nextPlace != null)
            {
                var pos = nextPlace.info().pos;
                double diff = 5;
                diff = Math.max(diff, this.entity.getBbWidth());
                if (entity.getOnPos().distSqr(pos) > diff)
                {
                    this.setWalkTo(pos, 1, 0);
                    if (pathTimeout < 0) pathTimeout = 40;
                }
                if (pathTimeout < 20 || builder.creative)
                {
                    builder.tryPlace(level);

                    gettingPart = true;
                    findingSpot = false;

                    // Check if we still have item for next one, if so, then
                    // immediately continue
                    List<ItemStack> requested = new ArrayList<>();
                    int n = 0;
                    needed_check:
                    for (int i = 0, max = builder.placeOrder.size(); i < max; i++)
                    {
                        ItemStack needed = builder.neededItems.get(builder.placeOrder.get(i).pos);
                        if (needed == null || needed.isEmpty()) continue;
                        if (++n > 3) break;
                        for (var stack : requested) if (ItemStack.isSame(stack, needed)) continue needed_check;
                        needed = needed.copy();
                        needed.setCount(Math.min(5, needed.getCount()));
                        requested.add(needed);
                    }

                    for (int i = 2; i < itemhandler.getSlots(); i++)
                    {
                        ItemStack stack = itemhandler.getStackInSlot(i);
                        for (var stack2 : requested) if (ItemStack.isSame(stack, stack2))
                        {
                            requested.remove(stack2);
                            break;
                        }
                    }

                    if (requested.isEmpty())
                    {
                        gettingPart = false;
                        findingSpot = true;
                    }
                }
            }
        }
        else if (gettingPart)
        {
            if (builder.creative)
            {
                gettingPart = false;
                findingSpot = true;
                return;
            }

            double diff = 1;
            diff = Math.max(diff, this.entity.getBbWidth());
            if (entity.getOnPos().distSqr(storeLoc) > diff)
            {
                this.setWalkTo(storeLoc, 1, 0);
                if (pathTimeout < 0) pathTimeout = 40;
            }
            if (pathTimeout < 20)
            {
                var pair = storage.getInventory(level, storage.storageLoc, Direction.UP);
                storage.doStorageCheck(itemhandler);
                var container = pair.getFirst();

                List<ItemStack> requested = new ArrayList<>();
                int n = 0;
                needed_check:
                for (int i = 0, max = builder.placeOrder.size(); i < max; i++)
                {
                    ItemStack needed = builder.neededItems.get(builder.placeOrder.get(i).pos);
                    if (needed == null || needed.isEmpty()) continue;
                    if (++n > 3) break;
                    for (var stack : requested) if (ItemStack.isSame(stack, needed)) continue needed_check;
                    needed = needed.copy();
                    needed.setCount(Math.min(5, needed.getCount()));
                    requested.add(needed);
                }

                if (container != null) for (int i = 0; i < container.getSlots(); i++)
                {
                    ItemStack stack = container.getStackInSlot(i);
                    if (checkValid(stack, requested))
                    {
                        for (var stack2 : requested) if (ItemStack.isSame(stack, stack2))
                        {
                            requested.remove(stack2);
                            break;
                        }
                        container.setStackInSlot(i, ItemStack.EMPTY);
                        ItemStackTools.addItemStackToInventory(stack, itemhandler, 2);
                    }
                }
                if (requested.size() > 0)
                {
                    // need item, request it.
                    builder.provideBoM();

                    double size = pokemob.getMobSizes().mag();
                    double x = this.entity.getX();
                    double y = this.entity.getY();
                    double z = this.entity.getZ();

                    Random r = ThutCore.newRandom();
                    for (int l = 0; l < 2; l++)
                    {
                        double i = r.nextGaussian() * size;
                        double j = r.nextGaussian() * size;
                        double k = r.nextGaussian() * size;
                        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, x + i, y + j, z + k, 1, 0, 0, 0, 0);
                    }
                }
                else
                {
                    gettingPart = false;
                    findingSpot = true;
                }
            }
        }
        else
        {
            gettingPart = true;
        }
    }

    @Override
    public boolean shouldRun()
    {
        if (last != pokemob.getEntity().getOffhandItem() && entity.level instanceof ServerLevel level)
        {
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

                    for (int i = 2; i < lines.length; i++)
                    {
                        String line = lines[i];
                        if (line.startsWith("p:")) offset = line.replace("p:", "").strip();
                        if (line.startsWith("r:")) rotation = line.replace("r:", "").strip();
                        if (line.startsWith("m:")) rotation = line.replace("m:", "").strip();

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
                            var builder = new StructureBuilder(origin.offset(shift), rot, mir, itemSource);
                            builder.toMake = toMake;
                            builder.keyProvider = StructureBuilder.makeForSource(builder, itemSource,
                                    itemSource.getSlots() - 1);
                            this.setBuilder(builder, level);
                        }
                        else
                        {
                            var poolHolder = level.registryAccess().registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY)
                                    .getHolderOrThrow(ResourceKey.create(Registry.TEMPLATE_POOL_REGISTRY, toMake));
                            ExpandedJigsawConfiguration config = new ExpandedJigsawConfiguration(poolHolder, 4,
                                    YSettings.DEFAULT, ClearanceSettings.DEFAULT, new ArrayList<>(), "", "", "none",
                                    Heightmap.Types.WORLD_SURFACE_WG, new ArrayList<>(), 0, 0,
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
                    PokecubeAPI.LOGGER.error(e);
                }
            }
        }
        return hasInstructions;
    }

}

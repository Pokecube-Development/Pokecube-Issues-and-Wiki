/**
 *
 */
package pokecube.core.items;

import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;
import pokecube.api.data.Pokedex;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.commandhandlers.TeleportHandler;
import pokecube.api.events.pokemobs.SpawnEvent.SpawnContext;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.healer.HealerBlock;
import pokecube.core.database.Database;
import pokecube.core.eventhandlers.SpawnHandler;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.network.packets.PacketDataSync;
import pokecube.core.network.packets.PacketPokedex;
import thut.api.level.structures.NamedVolumes.INamedStructure;
import thut.api.level.structures.StructureManager;
import thut.api.maths.Vector3;
import thut.core.common.commands.CommandTools;
import thut.core.common.handlers.PlayerDataHandler;
import thut.core.common.network.TerrainUpdate;
import thut.lib.TComponent;

/** @author Manchou */
public class ItemPokedex extends Item implements DyeableLeatherItem
{
    public final boolean watch;

    public ItemPokedex(final Properties props, final boolean watch)
    {
        super(props);
        this.watch = watch;
    }

    @Override
    public InteractionResult interactLivingEntity(final ItemStack stack, final Player playerIn,
            final LivingEntity target, final InteractionHand hand)
    {
        interact:
        if (playerIn instanceof ServerPlayer)
        {
            final Entity entityHit = target;
            final IPokemob pokemob = PokemobCaps.getPokemobFor(entityHit);

            // Not a pokemob, or not a stock pokemob, only the watch will do
            // anything on right click, pokedex is for accessing the mob.
            final boolean doInteract = target instanceof ServerPlayer
                    || pokemob != null && pokemob.getPokedexEntry().stock && this.watch;

            if (!doInteract) break interact;
            this.showGui(playerIn, target, pokemob);
            return InteractionResult.SUCCESS;
        }
        return super.interactLivingEntity(stack, playerIn, target, hand);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level world, final Player player, final InteractionHand hand)
    {
        final ItemStack itemstack = player.getItemInHand(hand);
        if (!world.isClientSide) SpawnHandler.refreshTerrain(new Vector3().set(player), player.getLevel(), true);
        if (!player.isCrouching())
        {
            final Entity entityHit = Tools.getPointedEntity(player, 16, 0.5);
            final IPokemob pokemob = PokemobCaps.getPokemobFor(entityHit);
            this.showGui(player, entityHit, pokemob);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemstack);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemstack);
    }

    @Override
    public InteractionResult useOn(final UseOnContext context)
    {
        final Level worldIn = context.getLevel();
        final Player playerIn = context.getPlayer();
        final BlockPos pos = context.getClickedPos();
        final Vector3 hit = new Vector3().set(pos);
        final Block block = hit.getBlockState(worldIn).getBlock();
        if (!worldIn.isClientSide && playerIn instanceof ServerPlayer player && worldIn instanceof ServerLevel level)
        {
            SpawnHandler.refreshTerrain(new Vector3().set(player), player.getLevel(), true);

            // Debug option to see if structures are in an area, for testing
            // datapacks/configs.
            if (PokecubeCore.getConfig().debug_misc)
            {
                final Set<INamedStructure> infos = StructureManager.getFor(level.dimension(), pos, false);
                for (final INamedStructure i : infos)
                {
                    thut.lib.ChatHelper.sendSystemMessage(player, TComponent.literal(i.getName()));
                    BlockPos.betweenClosedStream(i.getTotalBounds()).forEach(p -> {
                        Packet<?> packet = new ClientboundLevelParticlesPacket(ParticleTypes.HAPPY_VILLAGER, true,
                                p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 0, 0, 0, 0, 1);
                        player.connection.send(packet);
                    });
                }
            }

            // Debug option for checking what can spawn in an area, for testing
            // datapacks/configs.
            if (PokecubeCore.getConfig().debug_spawning)
            {
                Vector3 v = new Vector3().set(pos);
                SpawnCheck checker = new SpawnCheck(v, level);
                for (final PokedexEntry e : Database.spawnables)
                    if (e.getSpawnData().getMatcher(new SpawnContext(player, e), checker, false) != null)
                {
                    thut.lib.ChatHelper.sendSystemMessage(player, TComponent.literal(e.getTrimmedName()));
                }
            }
        }

        // Assign as a teleport location when used on a pokecenter.
        if (block instanceof HealerBlock)
        {
            final GlobalPos loc = GlobalPos.of(worldIn.dimension(), playerIn.blockPosition());
            TeleportHandler.setTeleport(loc, playerIn.getStringUUID());
            if (!worldIn.isClientSide)
            {
                CommandTools.sendMessage(playerIn, "pokedex.setteleport");
                PacketDataSync.syncData(playerIn, "pokecube-data");
            }
            return InteractionResult.SUCCESS;
        }

        if (playerIn.isCrouching() && !worldIn.isClientSide)
        {
            Component message = CommandTools.makeTranslatedMessage("pokedex.locationinfo1", "green",
                    Database.spawnables.size());
            thut.lib.ChatHelper.sendSystemMessage(playerIn, message);
            message = CommandTools.makeTranslatedMessage("pokedex.locationinfo2", "green",
                    Pokedex.getInstance().getEntries().size());
            thut.lib.ChatHelper.sendSystemMessage(playerIn, message);
            message = CommandTools.makeTranslatedMessage("pokedex.locationinfo3", "green",
                    Pokedex.getInstance().getRegisteredEntries().size());
            thut.lib.ChatHelper.sendSystemMessage(playerIn, message);
        }

        if (!playerIn.isCrouching())
        {
            final Entity entityHit = Tools.getPointedEntity(playerIn, 16, 0.5);
            final IPokemob pokemob = PokemobCaps.getPokemobFor(entityHit);
            this.showGui(playerIn, entityHit, pokemob);
        }
        return InteractionResult.FAIL;
    }

    private void showGui(final Player player, final Entity mob, final IPokemob pokemob)
    {
        if (player instanceof ServerPlayer splayer)
        {
            final ChunkAccess chunk = player.getLevel().getChunk(player.blockPosition());
            TerrainUpdate.sendTerrainToClient(new ChunkPos(chunk.getPos().x, chunk.getPos().z), splayer);
            PacketDataSync.syncData(player, "pokecube-stats");
            PacketPokedex.sendSecretBaseInfoPacket(splayer, this.watch);
            PacketPokedex.sendLocalSpawnsPacket(splayer);
            if (pokemob != null) PlayerDataHandler.getInstance().getPlayerData(player)
                    .getData(PokecubePlayerStats.class).inspect(player, pokemob);
            PacketPokedex.sendOpenPacket(splayer, mob, this.watch);
        }
    }

    @Override
    public int getColor(ItemStack stack)
    {
        CompoundTag compoundtag = stack.getTagElement("display");
        return compoundtag != null && compoundtag.contains("color", 99) ? compoundtag.getInt("color") : 0xFFB02E26;
    }
}

/**
 *
 */
package pokecube.core.items;

import java.util.Set;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.healer.HealerBlock;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.commandhandlers.TeleportHandler;
import pokecube.core.network.packets.PacketDataSync;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;
import thut.api.terrain.StructureManager;
import thut.api.terrain.StructureManager.StructureInfo;
import thut.core.common.commands.CommandTools;
import thut.core.common.handlers.PlayerDataHandler;
import thut.core.common.network.TerrainUpdate;

/** @author Manchou */
public class ItemPokedex extends Item
{
    public final boolean watch;

    public ItemPokedex(final Properties props, final boolean watch)
    {
        super(props);
        this.watch = watch;
        if (PokecubeItems.POKECUBE_ITEMS.isEmpty()) PokecubeItems.POKECUBE_ITEMS = new ItemStack(this);
    }

    @Override
    public InteractionResult interactLivingEntity(final ItemStack stack, final Player playerIn,
            final LivingEntity target, final InteractionHand hand)
    {
        interact:
        if (playerIn instanceof ServerPlayer)
        {
            final Entity entityHit = target;
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entityHit);

            // Not a pokemob, or not a stock pokemob, only the watch will do
            // anything on right click, pokedex is for accessing the mob.
            final boolean doInteract = target instanceof ServerPlayer || pokemob != null && pokemob
                    .getPokedexEntry().stock && this.watch;

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
        if (!world.isClientSide) SpawnHandler.refreshTerrain(Vector3.getNewVector().set(player), player.getCommandSenderWorld(),
                true);
        if (!player.isCrouching())
        {
            final Entity entityHit = Tools.getPointedEntity(player, 16);
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entityHit);
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
        final Vector3 hit = Vector3.getNewVector().set(pos);
        final Block block = hit.getBlockState(worldIn).getBlock();
        if (!worldIn.isClientSide)
        {
            SpawnHandler.refreshTerrain(Vector3.getNewVector().set(playerIn), playerIn.getCommandSenderWorld(), true);
            if (PokecubeMod.debug)
            {
                final Set<StructureInfo> infos = StructureManager.getFor(worldIn.dimension(), pos);
                for (final StructureInfo i : infos)
                    playerIn.sendMessage(new TextComponent(i.name), Util.NIL_UUID);
            }
        }
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
            playerIn.sendMessage(message, Util.NIL_UUID);
            message = CommandTools.makeTranslatedMessage("pokedex.locationinfo2", "green", Pokedex.getInstance()
                    .getEntries().size());
            playerIn.sendMessage(message, Util.NIL_UUID);
            message = CommandTools.makeTranslatedMessage("pokedex.locationinfo3", "green", Pokedex.getInstance()
                    .getRegisteredEntries().size());
            playerIn.sendMessage(message, Util.NIL_UUID);
        }

        if (!playerIn.isCrouching())
        {
            final Entity entityHit = Tools.getPointedEntity(playerIn, 16);
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entityHit);
            this.showGui(playerIn, entityHit, pokemob);
        }
        return InteractionResult.FAIL;
    }

    private void showGui(final Player player, final Entity mob, final IPokemob pokemob)
    {
        if (player instanceof ServerPlayer)
        {
            final ChunkAccess chunk = player.getCommandSenderWorld().getChunk(player.blockPosition());
            TerrainUpdate.sendTerrainToClient(new ChunkPos(chunk.getPos().x, chunk.getPos().z),
                    (ServerPlayer) player);
            PacketDataSync.syncData(player, "pokecube-stats");
            PacketPokedex.sendSecretBaseInfoPacket((ServerPlayer) player, this.watch);
            if (pokemob != null) PlayerDataHandler.getInstance().getPlayerData(player).getData(
                    PokecubePlayerStats.class).inspect(player, pokemob);
            PacketPokedex.sendOpenPacket((ServerPlayer) player, mob, this.watch);
        }
    }

}

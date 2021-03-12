/**
 *
 */
package pokecube.core.items;

import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
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
    public ActionResultType interactLivingEntity(final ItemStack stack, final PlayerEntity playerIn,
            final LivingEntity target, final Hand hand)
    {
        interact:
        if (playerIn instanceof ServerPlayerEntity)
        {
            final Entity entityHit = target;
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entityHit);

            // Not a pokemob, or not a stock pokemob, only the watch will do
            // anything on right click, pokedex is for accessing the mob.
            final boolean doInteract = target instanceof ServerPlayerEntity || pokemob != null && pokemob
                    .getPokedexEntry().stock && this.watch;

            if (!doInteract) break interact;
            this.showGui(playerIn, target, pokemob);
            return ActionResultType.SUCCESS;
        }
        return super.interactLivingEntity(stack, playerIn, target, hand);
    }

    @Override
    public ActionResult<ItemStack> use(final World world, final PlayerEntity player, final Hand hand)
    {
        final ItemStack itemstack = player.getItemInHand(hand);
        if (!world.isClientSide) SpawnHandler.refreshTerrain(Vector3.getNewVector().set(player), player.getCommandSenderWorld(),
                true);
        if (!player.isCrouching())
        {
            final Entity entityHit = Tools.getPointedEntity(player, 16);
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entityHit);
            this.showGui(player, entityHit, pokemob);
            return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
        }
        return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
    }

    @Override
    public ActionResultType useOn(final ItemUseContext context)
    {
        final World worldIn = context.getLevel();
        final PlayerEntity playerIn = context.getPlayer();
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
                    playerIn.sendMessage(new StringTextComponent(i.name), Util.NIL_UUID);
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
            return ActionResultType.SUCCESS;
        }

        if (playerIn.isCrouching() && !worldIn.isClientSide)
        {
            ITextComponent message = CommandTools.makeTranslatedMessage("pokedex.locationinfo1", "green",
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
        return ActionResultType.FAIL;
    }

    private void showGui(final PlayerEntity player, final Entity mob, final IPokemob pokemob)
    {
        if (player instanceof ServerPlayerEntity)
        {
            final IChunk chunk = player.getCommandSenderWorld().getChunk(player.blockPosition());
            TerrainUpdate.sendTerrainToClient(new ChunkPos(chunk.getPos().x, chunk.getPos().z),
                    (ServerPlayerEntity) player);
            PacketDataSync.syncData(player, "pokecube-stats");
            PacketPokedex.sendSecretBaseInfoPacket((ServerPlayerEntity) player, this.watch);
            if (pokemob != null) PlayerDataHandler.getInstance().getPlayerData(player).getData(
                    PokecubePlayerStats.class).inspect(player, pokemob);
            PacketPokedex.sendOpenPacket((ServerPlayerEntity) player, mob, this.watch);
        }
    }

}

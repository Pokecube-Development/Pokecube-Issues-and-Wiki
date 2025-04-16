package pokecube.adventures.items;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import pokecube.adventures.PokecubeAdv;
import pokecube.api.entity.trainers.TrainerCaps;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.utils.CapHolders;
import thut.api.ThutCaps;
import thut.api.attachments.IOwnable;
import thut.api.attachments.Linkable;
import thut.api.attachments.Linkable.ILinkStorage;
import thut.api.attachments.Linkable.LinkHolder;
import thut.api.attachments.Linkable.LinkStorage;
import thut.api.util.PermNodes;
import thut.api.util.PermNodes.DefaultPermissionLevel;
import thut.lib.TComponent;

import java.util.UUID;

public class Linker extends Item
{
    private static final ResourceLocation LINKSTOREKEY = ResourceLocation.fromNamespaceAndPath(PokecubeAdv.MODID,
            "linker");

    static
    {
        Linkable.LINK_STORAGES.put(LINKSTOREKEY, (tag, context) -> {
            var store = new LinkStore();
            store.deserializeNBT(context, tag);
            return store;
        });
    }

    private static class LinkStore extends LinkStorage
    {
        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider provider)
        {
            var tag = super.serializeNBT(provider);
            tag.putString("key", LINKSTOREKEY.toString());
            return tag;
        }

        @Override
        public boolean setLinkedMob(final UUID mobid, final Entity user)
        {
            // we do not link mobs;
            return false;
        }

        @Override
        public boolean setLinkedPos(final GlobalPos pos, final Entity user)
        {
            // Null when loading
            if (user == null)
            {
                super.setLinkedPos(pos, user);
                return false;
            }
            if (pos == null || user.isCrouching())
            {
                super.setLinkedPos(null, user);
                if (!user.level().isClientSide)
                {
                    if (user instanceof Player player)
                    {
                        player.displayClientMessage(TComponent.translatable("item.pokecube_adventures.linker.unset"),
                                true);
                        player.swing(player.getUsedItemHand());
                    }
                }
            }
            else
            {
                super.setLinkedPos(pos, user);
                if (!user.level().isClientSide)
                {
                    if (user instanceof Player player)
                    {
                        player.displayClientMessage(TComponent.translatable("item.pokecube_adventures.linker.set"),
                                true);
                        player.swing(player.getUsedItemHand());
                    }
                }
                if (user.level().isClientSide) try
                {
                    final String loc = String.format("%d %d %d", pos.pos().getX(), pos.pos().getY(), pos.pos().getZ());
                    Minecraft.getInstance().keyboardHandler.setClipboard(loc);
                    if (user instanceof Player player)
                    {
                        player.displayClientMessage(TComponent.translatable("item.pokecube_adventures.linker.set"),
                                true);
                        player.swing(player.getUsedItemHand());
                    }
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                }

            }
            return true;
        }
    }

    public static String PERMLINKTRAINER = "linker.link_npc";
    public static String PERMLINKPET = "linker.link_pet";

    private static final CompoundTag DEFAULT = new CompoundTag();

    static
    {
        DEFAULT.putString("key", LINKSTOREKEY.toString());
    }

    public Linker(final Properties properties)
    {
        super(properties.component(Linkable.LINK_STORE, new LinkHolder(DEFAULT)));
        PermNodes.registerBooleanNode(PokecubeAdv.MODID, Linker.PERMLINKTRAINER, DefaultPermissionLevel.OP,
                "Is the player allowed to use the linker item to set a trainer's stationary location");
        PermNodes.registerBooleanNode(PokecubeAdv.MODID, Linker.PERMLINKPET, DefaultPermissionLevel.ALL,
                "Is the player allowed to use the linker item to set their pokemob's stationary location");
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        var store = context.getItemInHand().get(Linkable.LINK_STORE);
        if (store == null || !(store.link() instanceof LinkStore))
        {
            store = LinkHolder.withStorage(context.getLevel().registryAccess(), new LinkStore());
            context.getItemInHand().set(Linkable.LINK_STORE, store);
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target,
            InteractionHand usedHand)
    {
        var _store = stack.get(Linkable.LINK_STORE);
        if (_store == null || !(_store.link() instanceof LinkStore))
        {
            _store = LinkHolder.withStorage(target.level().registryAccess(), new LinkStore());
            stack.set(Linkable.LINK_STORE, _store);
        }
        final IGuardAICapability ai = CapHolders.getGuardAI(target);
        LinkHolder storage = ThutCaps.getLinkStorage(stack);
        if (storage == null || storage.tag().isEmpty()) return InteractionResult.PASS;
        if (storage.link() == null)
        {
            storage = storage.withContext(player.registryAccess());
            stack.set(Linkable.LINK_STORE, storage);
        }
        ILinkStorage store = storage.link();
        final GlobalPos pos = store.getLinkedPos(player);
        if (ai != null && pos != null && pos.dimension() == target.level().dimension())
        {
            final IOwnable ownable = ThutCaps.getOwnable(target);
            boolean valid = player.level().isClientSide();
            if (player instanceof ServerPlayer serverPlayer)
            {
                if (ownable != null && ownable.getOwnerId() != null)
                    valid = player.getUUID().equals(ownable.getOwnerId()) && PermNodes.getBooleanPerm(serverPlayer,
                            Linker.PERMLINKPET);
                else if (TrainerCaps.getHasPokemobs(target) != null)
                    valid = PermNodes.getBooleanPerm(serverPlayer, Linker.PERMLINKTRAINER);
                if (valid)
                {
                    final BlockPos bpos = pos.pos().above();
                    player.displayClientMessage(
                            TComponent.translatable("item.pokecube_adventures.linked.mob", target.getDisplayName(),
                                    bpos.getX(), bpos.getY(), bpos.getZ()), true);
                }
            }
            if (valid)
            {
                ai.getPrimaryTask().setPos(pos.pos().above());
                player.swing(player.getUsedItemHand());
                return InteractionResult.sidedSuccess(player.level().isClientSide());
            }
            else player.displayClientMessage(TComponent.translatable("item.pokecube_adventures.linked.mob.fail"), true);
        }
        return super.interactLivingEntity(stack, player, target, usedHand);
    }
}

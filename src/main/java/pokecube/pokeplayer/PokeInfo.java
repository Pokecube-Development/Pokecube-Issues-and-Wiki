package pokecube.pokeplayer;

import net.minecraft.block.BlockState;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.packets.PacketDataSync;
import pokecube.core.utils.EntityTools;
import pokecube.core.utils.PokeType;
import pokecube.pokeplayer.inventory.InventoryPlayerPokemob;
import pokecube.pokeplayer.network.DataSyncWrapper;
import pokecube.pokeplayer.network.PacketTransform;
import thut.api.world.mobs.data.Data;
import thut.api.world.mobs.data.DataSync;
import thut.core.common.handlers.PlayerDataHandler;
import thut.core.common.handlers.PlayerDataHandler.PlayerData;
import thut.core.common.world.mobs.data.SyncHandler;

public class PokeInfo extends PlayerData
{
    private ItemStack             stack = ItemStack.EMPTY;
    private IPokemob              pokemob;
    private boolean 			  attached = false;
    
    public DamageSource           lastDamage = null;
    public InventoryPlayerPokemob pokeInventory;
    public float                  originalHeight;
    public float                  originalWidth;
    public float                  originalHP;

    public PokeInfo()
    {
    }
    
    public void set(IPokemob pokemob, PlayerEntity player)
    {
        if (this.pokemob != null || pokemob == null) resetPlayer(player);
        if (pokemob == null || this.pokemob == pokemob) return;
        if (this.attached) return;
        if (PokecubeManager.isFilled(this.stack)) {
	        this.pokemob = pokemob;
	        this.pokeInventory = new InventoryPlayerPokemob(this, player.world);
	        this.originalHeight = player.getHeight();
	        this.originalWidth = player.getWidth();
	        this.originalHP = player.getMaxHealth();
	        pokemob.getEntity().setWorld(player.getEntityWorld());
	        pokemob.getEntity().getPersistentData().putBoolean("is_a_player", true);
	        pokemob.getEntity().getPersistentData().putString("playerID", player.getUniqueID().toString());
	        pokemob.getEntity().getPersistentData().putString("oldName", pokemob.getPokemonNickname());
	        pokemob.setPokemonNickname(player.getDisplayName().toString());
	        pokemob.setOwner(player);
	        pokemob.initAI();
	        final DataSync sync = SyncHandler.getData(player);
	        if (sync instanceof DataSyncWrapper) ((DataSyncWrapper) sync).wrapped = this.pokemob.dataSync();
	        if (player instanceof ServerPlayerEntity) PacketDataSync.sendInitPacket((PlayerEntity) player, this
	                .getIdentifier());
        }
        save(player);
    }

    public void resetPlayer(PlayerEntity player)
    {
        DataSync sync = SyncHandler.getData(player);
        if (sync instanceof DataSyncWrapper)
        {
            ((DataSyncWrapper) sync).wrapped = sync;
        }
        if (pokemob == null && !player.getEntityWorld().isRemote) return;
        player.getEyeHeight();
        //player.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(originalHP);
        float height = originalHeight;
        float width = originalWidth;
        if (player.getHeight() != height)
        {
        	player.canUpdate(true);
            player.size.scale(width, height);
            player.canUpdate(false);
        }
        setFlying(player, false);
        pokemob = null;
        stack = null;
        pokeInventory = null;
        save(player);
        if (!player.getEntityWorld().isRemote)
        {
            EventsHandler.sendUpdate(player);
        }
    }

    public void setPlayer(PlayerEntity player)
    {
        if (pokemob == null) return;
        DataSync sync = SyncHandler.getData(player);
        if (sync instanceof DataSyncWrapper)
        {
            ((DataSyncWrapper) sync).wrapped = pokemob.dataSync();
        }
        pokemob.setSize((float) (pokemob.getSize() / PokecubeCore.getConfig().scalefactor));

//        pokemob.getAI().aiTasks.removeIf(new Predicate<IAIRunnable>()
//        {
//            @Override
//            public boolean test(IAIRunnable t)
//            {
//                boolean allowed = t instanceof AIHungry;
//                allowed = allowed || t instanceof AIMate;
//                return !allowed;
//            }
//        });

        float height = pokemob.getSize() * pokemob.getPokedexEntry().height;
        float width = pokemob.getSize() * pokemob.getPokedexEntry().width;
        player.stepHeight = pokemob.getEntity().getEyeHeight();
        width = Math.min(player.size.width, width);
        if (player.size.height != height || player.size.width != width)
        {
            player.canUpdate(true);
            player.size.scale(width, height);
            player.canUpdate(false);
        }
        setFlying(player, true);
        save(player);
        if (!player.getEntityWorld().isRemote)
        {
            EventsHandler.sendUpdate(player);
            ((ServerPlayerEntity) player).sendAllContents(player.container,
                    player.container.inventoryItemStacks);
            // // Fixes the inventories appearing to vanish
            player.getPersistentData().putLong("_pokeplayer_evolved_", player.getEntityWorld().getGameTime() + 50);
        }
    }

    public void postPlayerTick(PlayerEntity player)
    {
        if (pokemob == null) return;
        float height = pokemob.getSize() * pokemob.getPokedexEntry().height;
        float width = pokemob.getSize() * pokemob.getPokedexEntry().width;
        player.stepHeight = pokemob.getEntity().getEyeHeight();
        width = Math.min(player.size.width, width);
        if (player.size.height != height || player.size.width != width)
        {
            player.canUpdate(true);
            player.size.scale(width, height);
            player.canUpdate(false);
        }
    }

    public void onUpdate(PlayerEntity player, final World world)
    {
        if (getPokemob(world) == null && stack != null)
        {
            resetPlayer(player);
        }
        if (pokemob == null) return;
        MobEntity poke = pokemob.getEntity();

//        // Fixes pokemob sometimes targetting self.
//        if (poke.getAttackTarget() == player || poke.getAttackTarget() == poke)
//        {
//            boolean old = AIFindTarget.handleDamagedTargets;
//            AIFindTarget.handleDamagedTargets = false;
//            poke.setAttackTarget(null);
//            pokemob.setTargetID(-1);
//            AIFindTarget.handleDamagedTargets = old;
//        }

        // Flag the data sync dirty every so often to ensure things stay synced.
        if (poke.ticksExisted % 20 == 0)
        {
            for (Data<?> d : pokemob.dataSync().getAll())
                d.setDirty(true);
        }

        // Ensure it is tamed.
        pokemob.setGeneralState(GeneralStates.TAMED, true);
        // No Stay mode for pokeplayers.
        pokemob.setGeneralState(GeneralStates.STAYING, false);
        // Update the mob.
        // Ensure the mob has correct world.
        poke.setWorld(player.getEntityWorld());
        poke.addedToChunk = true;
        // No clip to prevent collision effects from the mob itself.
        poke.noClip = true;

        poke.canUpdate();

        // Update location
        poke.distanceWalkedModified = Integer.MAX_VALUE;
        EntityTools.copyEntityTransforms(poke, player);

        // Deal with health
        if (player.abilities.isCreativeMode)
        {
            poke.setHealth(poke.getMaxHealth());
            pokemob.setHungerTime(-PokecubeCore.getConfig().pokemobLifeSpan / 4);
        }
        //player.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(poke.getMaxHealth());

        float health = poke.getHealth();
        /** do not manage hp for creative mode players. */
        if (!player.abilities.isCreativeMode) if (player instanceof ServerPlayerEntity && player.addedToChunk)
        {
            float playerHealth = player.getHealth();

            /** Player has healed somehow, this is fine. */
            if (playerHealth > health && lastDamage == null && health > 0 && playerHealth <= poke.getMaxHealth())
            {
                if (poke.getAttackTarget() == null) health = playerHealth;
                else playerHealth = health;
            }

            /** If this is going to kill the player, do it with an attack, as
             * this will properly kill the player. */
            if (health < playerHealth)
            {
                DamageSource source = lastDamage == null ? DamageSource.GENERIC : lastDamage;
                float amount = playerHealth - health;
                source.setDamageBypassesArmor().setDamageIsAbsolute();
                player.attackEntityFrom(source, amount);
            }
            else player.setHealth(health);

            // Sync pokehealth to player health.
            playerHealth = player.getHealth();
            poke.setHealth(playerHealth);

            lastDamage = null;

            health = playerHealth;

            PacketTransform packet = new PacketTransform();
            packet.id = player.getEntityId();
            packet.data.putBoolean("U", true);
            packet.data.putFloat("H", health);
            packet.data.putFloat("M", poke.getMaxHealth());
            //PokecubeCore.packets.sendTo(packet, (ServerPlayerEntity) player);

            // Fixes the inventories appearing to vanish
            if (player.getPersistentData().contains("_pokeplayer_evolved_") && player.getPersistentData()
                    .getLong("_pokeplayer_evolved_") > player.getEntityWorld().getGameTime())
            {
                ((ServerPlayerEntity) player).sendAllContents(player.container,
                        player.container.inventoryItemStacks);
            }
            else player.getPersistentData().remove("_pokeplayer_evolved_");
        }
        if (player.getHealth() > 0) player.deathTime = -1;
        poke.deathTime = player.deathTime;

        int num = pokemob.getHungerTime();
        int max = PokecubeCore.getConfig().pokemobLifeSpan;
        num = Math.round(((max - num) * 20) / (float) max);
        if (player.isCreative()) num = 20;
        player.getFoodStats().setFoodLevel(num);

        updateFloating(player);
        updateFlying(player);
        updateSwimming(player);

        // Synchronize the hitbox locations
        poke.setPosition(player.getPosX(), player.getPosY(), player.getPosZ());
    }

    public void clear()
    {
        pokemob = null;
        pokeInventory = null;
        stack = null;
    }

    public void save(PlayerEntity player)
    {
        if (!player.getEntityWorld().isRemote)
            PlayerDataHandler.getInstance().save(player.getCachedUniqueIdString(), getIdentifier());
    }

    private void setFlying(PlayerEntity player, boolean set)
    {
        if (pokemob == null) return;
        boolean fly = pokemob.floats() || pokemob.flys() || !set;
        boolean check = set ? !player.abilities.allowFlying : player.abilities.allowFlying;
        if (fly && check && player.getEntityWorld().isRemote && !player.abilities.isCreativeMode)
        {
            player.abilities.allowFlying = set;
            player.sendPlayerAbilities();
        }
    }

    private void updateFlying(PlayerEntity player)
    {
        if (pokemob == null) return;
        if (pokemob.floats() || pokemob.flys())
        {
            player.fallDistance = 0;
            if (player instanceof ServerPlayerEntity) ((ServerPlayerEntity) player).connection.floatingTickCount = 0;
        }
    }

    private void updateFloating(PlayerEntity player)
    {
        if (pokemob == null) return;
        if (!player.isSneaking() && pokemob.floats() && !player.isElytraFlying())
        {
            double h = pokemob.getPokedexEntry().preferedHeight;
            Vector3d start = new Vector3d(player.getPosX(), player.getPosY(), player.getPosZ());
            Vector3d end = new Vector3d(player.getPosX(), player.getPosY() - h, player.getPosZ());
            VoxelShape shape = null;
            BlockState state = null;
            BlockPos pos = null;
            		
            RayTraceResult position = player.getEntityWorld().rayTraceBlocks(start, end, pos, shape, state);
            boolean noFloat = pokemob.getLogicState(LogicStates.SITTING) || pokemob.getLogicState(LogicStates.SLEEPING)
                    || pokemob.isGrounded()
                    || (pokemob.getStatus() & (IMoveConstants.STATUS_SLP + IMoveConstants.STATUS_FRZ)) > 0;

            if (position != null && !noFloat)
            {
                double d = position.getHitVec().subtract(start).length();
                if (d < 0.9 * h) player.prevPosY += 0.1;
                else player.prevPosY = 0;
            }
            else if (player.prevPosY < 0 && !noFloat)
            {
                player.prevPosY *= 0.6;
            }
        }
    }

    private void updateSwimming(PlayerEntity player)
    {
        if (pokemob == null) return;
        if (pokemob.getPokedexEntry().swims() || pokemob.isType(PokeType.getType("water"))) player.setAir(300);
    }

    public ItemStack detach()
    {
        this.attached = false;
        if (this.pokemob == null) return ItemStack.EMPTY;
        this.pokemob.getEntity().getPersistentData().remove("is_a_player");
        return PokecubeManager.pokemobToItem(this.pokemob);
    }
    
    public void setStack(final ItemStack stack)
    {
        this.stack = stack;
    }
    
    @Override
    public String dataFileName()
    {
        return "PokePlayer";
    }

    @Override
    public String getIdentifier()
    {
        return "pokeplayer-data";
    }

    @Override
    public boolean shouldSync()
    {
        return false;
    }

//    @Override
//    public void writeToNBT(final CompoundNBT tag)
//    {
//        if (this.pokemob != null) this.stack = PokecubeManager.pokemobToItem(this.pokemob);
//        this.stack.write(tag);
//    }
    
    @Override
    public void writeToNBT(CompoundNBT tag)
    {
    	if (this.pokemob != null) {
    		this.stack = PokecubeManager.pokemobToItem(this.pokemob);
   		this.stack.write(tag);
  	}
        else if (stack != null)
        {
            stack.write(tag);
        }
        tag.putFloat("h", originalHeight);
        tag.putFloat("w", originalWidth);
        tag.putFloat("hp", originalHP);
    }

//    @Override
//    public void readFromNBT(final CompoundNBT tag)
//    {
//        this.stack = ItemStack.read(tag);
//    }
    
    @Override
    public void readFromNBT(CompoundNBT tag)
    {
    	this.stack = ItemStack.read(tag);
        originalHeight = tag.getFloat("h");
        originalWidth = tag.getFloat("w");
        originalHP = tag.getFloat("hp");
        if (originalHP <= 1) originalHP = 20;
    }

    public IPokemob getPokemob(World world)
    {
        if (pokemob == null && stack != null)
        {
            pokemob = PokecubeManager.itemToPokemob(stack, world);
            if (pokemob == null) stack = null;
        }
        return pokemob;
    }
}

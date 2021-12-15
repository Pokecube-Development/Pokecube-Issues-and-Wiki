package pokecube.core.interfaces.capabilities.impl;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.utils.TagNames;
import thut.api.entity.ai.IAIRunnable;

public abstract class PokemobSaves extends PokemobOwned implements TagNames
{
    private void cleanLoadedAIStates()
    {
        // First clear out any non-persistant ai states from logic states
        for (final LogicStates state : LogicStates.values()) if (!state.persists()) this.setLogicState(state, false);
        // Then clean up general states
        for (final GeneralStates state : GeneralStates.values())
            if (!state.persists()) this.setGeneralState(state, false);
        // Finally cleanup combat states
        for (final CombatStates state : CombatStates.values()) if (!state.persists()) this.setCombatState(state, false);
    }

    @Override
    public void read(final CompoundTag tag)
    {
        final CompoundTag ownerShipTag = tag.getCompound(TagNames.OWNERSHIPTAG);
        final CompoundTag statsTag = tag.getCompound(TagNames.STATSTAG);
        final CompoundTag movesTag = tag.getCompound(TagNames.MOVESTAG);
        final CompoundTag inventoryTag = tag.getCompound(TagNames.INVENTORYTAG);
        final CompoundTag breedingTag = tag.getCompound(TagNames.BREEDINGTAG);
        final CompoundTag visualsTag = tag.getCompound(TagNames.VISUALSTAG);
        final CompoundTag aiTag = tag.getCompound(TagNames.AITAG);
        final CompoundTag miscTag = tag.getCompound(TagNames.MISCTAG);
        // Read Ownership Tag
        if (!ownerShipTag.isEmpty())
        {
            this.setPokemobTeam(ownerShipTag.getString(TagNames.TEAM));
            this.setPokemonNickname(ownerShipTag.getString(TagNames.NICKNAME));
            if (ownerShipTag.contains(TagNames.OT))
                this.setOriginalOwnerUUID(UUID.fromString(ownerShipTag.getString(TagNames.OT)));
        }
        // Read stats tag
        if (!statsTag.isEmpty())
        {
            this.setExp(statsTag.getInt(TagNames.EXP), false);
            this.setStatus(statsTag.getByte(TagNames.STATUS));
            this.addHappiness(statsTag.getInt(TagNames.HAPPY));
            this.setDynamaxFactor(statsTag.getFloat(TagNames.DYNAPOWER));
        }
        // Read moves tag
        if (!movesTag.isEmpty())
        {
            this.getMoveStats().newMoves.clear();
            if (movesTag.contains(TagNames.NEWMOVES))
            {
                final ListTag newMoves = (ListTag) movesTag.get(TagNames.NEWMOVES);
                for (int i = 0; i < newMoves.size(); i++)
                    if (!this.getMoveStats().newMoves.contains(newMoves.getString(i)))
                        this.getMoveStats().newMoves.add(newMoves.getString(i));
            }
            this.setMoveIndex(movesTag.getInt(TagNames.MOVEINDEX));
            this.setAttackCooldown(movesTag.getInt(TagNames.COOLDOWN));
            final int[] disables = movesTag.getIntArray(TagNames.DISABLED);
            if (disables.length == 4) for (int i = 0; i < 4; i++) this.setDisableTimer(i, disables[i]);
        }
        // Read Inventory tag
        if (!inventoryTag.isEmpty())
        {
            final ListTag ListNBT = inventoryTag.getList(TagNames.ITEMS, 10);
            for (int i = 0; i < ListNBT.size(); ++i)
            {
                final CompoundTag CompoundNBT1 = ListNBT.getCompound(i);
                final int j = CompoundNBT1.getByte("Slot") & 255;
                if (j < this.getInventory().getContainerSize())
                    this.getInventory().setItem(j, ItemStack.of(CompoundNBT1));
                this.setHeldItem(this.getInventory().getItem(1));
            }
        }
        // Read Breeding tag
        if (!breedingTag.isEmpty()) this.loveTimer = breedingTag.getInt(TagNames.SEXETIME);
        // Read visuals tag
        if (!visualsTag.isEmpty())
        {
            this.dataSync().set(this.params.DYECOLOUR, visualsTag.getInt(TagNames.SPECIALTAG));
            final int[] flavourAmounts = visualsTag.getIntArray(TagNames.FLAVOURSTAG);
            if (flavourAmounts.length == 5)
                for (int i = 0; i < flavourAmounts.length; i++) this.setFlavourAmount(i, flavourAmounts[i]);
            if (visualsTag.contains(TagNames.POKECUBE))
            {
                final CompoundTag pokecubeTag = visualsTag.getCompound(TagNames.POKECUBE);
                this.setPokecube(ItemStack.of(pokecubeTag));
            }
            if (visualsTag.contains(TagNames.MODELHOLDER))
                this.setCustomHolder(FormeHolder.load(visualsTag.getCompound(TagNames.MODELHOLDER)));
        }

        // Read AI
        if (!aiTag.isEmpty())
        {
            this.setTotalCombatState(aiTag.getInt(TagNames.COMBATSTATE));
            this.setTotalGeneralState(aiTag.getInt(TagNames.GENERALSTATE));
            this.setTotalLogicState(aiTag.getInt(TagNames.LOGICSTATE));
            this.timeSinceCombat = aiTag.getInt(TagNames.COMBATTIME);
            this.cleanLoadedAIStates();

            this.setHungerTime(aiTag.getInt(TagNames.HUNGER));
            final CompoundTag routines = aiTag.getCompound(TagNames.AIROUTINES);
            for (final String s : routines.getAllKeys())
            {
                final AIRoutine routine = AIRoutine.valueOf(s);
                this.setRoutineState(routine, routines.getBoolean(s));
            }
            this.loadedTasks = aiTag.getCompound(TagNames.AITAG);
        }
        // Read Misc other
        if (!miscTag.isEmpty())
        {
            this.setRNGValue(miscTag.getInt(TagNames.RNGVAL));
            this.uid = miscTag.getInt(TagNames.UID);
            this.wasShadow = miscTag.getBoolean(TagNames.WASSHADOW);
        }
    }

    @Override
    public CompoundTag write()
    {
        final CompoundTag pokemobTag = new CompoundTag();
        pokemobTag.putInt(TagNames.VERSION, 1);
        // Write Ownership tag
        final CompoundTag ownerShipTag = new CompoundTag();
        // This is still written for pokecubes to read from. Actual number is
        // stored in genes.
        ownerShipTag.putInt(TagNames.POKEDEXNB, this.getPokedexNb());
        // Write the owner here, this is only for use for lookups, the holder
        // actually saves it.
        if (this.getOwnerId() != null) ownerShipTag.putUUID(TagNames.OWNER, this.getOwnerId());
        ownerShipTag.putString(TagNames.NICKNAME, this.getPokemonNickname());
        ownerShipTag.putString(TagNames.TEAM, this.getPokemobTeam());
        if (this.getOriginalOwnerUUID() != null)
            ownerShipTag.putString(TagNames.OT, this.getOriginalOwnerUUID().toString());

        // Write stats tag
        final CompoundTag statsTag = new CompoundTag();
        statsTag.putInt(TagNames.EXP, this.getExp());
        statsTag.putByte(TagNames.STATUS, this.getStatus());
        statsTag.putInt(TagNames.HAPPY, this.bonusHappiness);
        statsTag.putFloat(TagNames.DYNAPOWER, this.getDynamaxFactor());

        // Write moves tag
        final CompoundTag movesTag = new CompoundTag();
        movesTag.putInt(TagNames.MOVEINDEX, this.getMoveIndex());
        if (!this.getMoveStats().newMoves.isEmpty())
        {
            final ListTag newMoves = new ListTag();
            for (final String s : this.getMoveStats().newMoves) newMoves.add(StringTag.valueOf(s));
            movesTag.put(TagNames.NEWMOVES, newMoves);
        }
        movesTag.putInt(TagNames.COOLDOWN, this.getAttackCooldown());
        final int[] disables = new int[4];
        boolean tag = false;
        for (int i = 0; i < 4; i++)
        {
            disables[i] = this.getDisableTimer(i);
            tag = tag || disables[i] > 0;
        }
        if (tag) movesTag.putIntArray(TagNames.DISABLED, disables);

        // Write Inventory tag
        final CompoundTag inventoryTag = new CompoundTag();
        final ListTag ListNBT = new ListTag();
        this.getInventory().setItem(1, this.getHeldItem());
        for (int i = 0; i < this.getInventory().getContainerSize(); ++i)
        {
            final ItemStack itemstack = this.getInventory().getItem(i);
            if (!itemstack.isEmpty())
            {
                final CompoundTag CompoundNBT1 = new CompoundTag();
                CompoundNBT1.putByte("Slot", (byte) i);
                itemstack.save(CompoundNBT1);
                ListNBT.add(CompoundNBT1);
            }
        }
        inventoryTag.put(TagNames.ITEMS, ListNBT);

        // Write Breeding tag
        final CompoundTag breedingTag = new CompoundTag();
        breedingTag.putInt(TagNames.SEXETIME, this.loveTimer);

        // Write visuals tag
        final CompoundTag visualsTag = new CompoundTag();

        // This is still written for pokecubes to read from. Actual form is
        // stored in genes.
        visualsTag.putString(TagNames.FORME, this.getPokedexEntry().getTrimmedName());
        if (this.forme_holder != null) visualsTag.put(TagNames.MODELHOLDER, this.forme_holder.save());
        visualsTag.putInt(TagNames.SPECIALTAG, this.dataSync().get(this.params.DYECOLOUR));
        final int[] flavourAmounts = new int[5];
        for (int i = 0; i < flavourAmounts.length; i++) flavourAmounts[i] = this.getFlavourAmount(i);
        visualsTag.putIntArray(TagNames.FLAVOURSTAG, flavourAmounts);
        if (!this.getPokecube().isEmpty())
        {
            final CompoundTag pokecubeTag = this.getPokecube().save(new CompoundTag());
            visualsTag.put(TagNames.POKECUBE, pokecubeTag);
        }
        // Misc AI
        final CompoundTag aiTag = new CompoundTag();

        aiTag.putInt(TagNames.GENERALSTATE, this.getTotalGeneralState());
        aiTag.putInt(TagNames.LOGICSTATE, this.getTotalLogicState());
        aiTag.putInt(TagNames.COMBATSTATE, this.getTotalCombatState());
        aiTag.putInt(TagNames.COMBATTIME, this.timeSinceCombat);

        aiTag.putInt(TagNames.HUNGER, this.getHungerTime());
        final CompoundTag aiRoutineTag = new CompoundTag();
        for (final AIRoutine routine : AIRoutine.values())
            // Only save it if it was allowed anyway!
            if (routine.isAllowed(this)) aiRoutineTag.putBoolean(routine.toString(), this.isRoutineEnabled(routine));
        aiTag.put(TagNames.AIROUTINES, aiRoutineTag);
        final CompoundTag taskTag = new CompoundTag();
        for (final IAIRunnable task : this.getTasks()) if (task instanceof INBTSerializable)
            taskTag.put(task.getIdentifier(), ((INBTSerializable<?>) task).serializeNBT());
        aiTag.put(TagNames.AITAG, taskTag);

        // Misc other
        final CompoundTag miscTag = new CompoundTag();
        miscTag.putInt(TagNames.RNGVAL, this.getRNGValue());
        miscTag.putInt(TagNames.UID, this.getPokemonUID());
        miscTag.putBoolean(TagNames.WASSHADOW, this.wasShadow);

        // Set tags to the pokemob tag.
        pokemobTag.put(TagNames.OWNERSHIPTAG, ownerShipTag);
        pokemobTag.put(TagNames.STATSTAG, statsTag);
        pokemobTag.put(TagNames.MOVESTAG, movesTag);
        pokemobTag.put(TagNames.INVENTORYTAG, inventoryTag);
        pokemobTag.put(TagNames.BREEDINGTAG, breedingTag);
        pokemobTag.put(TagNames.VISUALSTAG, visualsTag);
        pokemobTag.put(TagNames.AITAG, aiTag);
        pokemobTag.put(TagNames.MISCTAG, miscTag);
        return pokemobTag;
    }

}

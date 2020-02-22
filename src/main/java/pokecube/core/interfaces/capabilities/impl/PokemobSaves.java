package pokecube.core.interfaces.capabilities.impl;

import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
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
        for (final LogicStates state : LogicStates.values())
            if (!state.persists()) this.setLogicState(state, false);
        // Then clean up general states
        for (final GeneralStates state : GeneralStates.values())
            if (!state.persists()) this.setGeneralState(state, false);
        // Finally cleanup combat states
        for (final CombatStates state : CombatStates.values())
            if (!state.persists()) this.setCombatState(state, false);
    }

    @Override
    public void read(final CompoundNBT tag)
    {
        final CompoundNBT ownerShipTag = tag.getCompound(TagNames.OWNERSHIPTAG);
        final CompoundNBT statsTag = tag.getCompound(TagNames.STATSTAG);
        final CompoundNBT movesTag = tag.getCompound(TagNames.MOVESTAG);
        final CompoundNBT inventoryTag = tag.getCompound(TagNames.INVENTORYTAG);
        final CompoundNBT breedingTag = tag.getCompound(TagNames.BREEDINGTAG);
        final CompoundNBT visualsTag = tag.getCompound(TagNames.VISUALSTAG);
        final CompoundNBT aiTag = tag.getCompound(TagNames.AITAG);
        final CompoundNBT miscTag = tag.getCompound(TagNames.MISCTAG);
        // Read Ownership Tag
        if (!ownerShipTag.isEmpty())
        {
            this.setPokemobTeam(ownerShipTag.getString(TagNames.TEAM));
            this.setPokemonNickname(ownerShipTag.getString(TagNames.NICKNAME));
            try
            {
                if (ownerShipTag.contains(TagNames.OT)) this.setOriginalOwnerUUID(UUID.fromString(ownerShipTag
                        .getString(TagNames.OT)));
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
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
            if (movesTag.contains(TagNames.NEWMOVES)) try
            {
                final ListNBT newMoves = (ListNBT) movesTag.get(TagNames.NEWMOVES);
                for (int i = 0; i < newMoves.size(); i++)
                    if (!this.getMoveStats().newMoves.contains(newMoves.getString(i))) this.getMoveStats().newMoves.add(
                            newMoves.getString(i));
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error loading new moves for " + this.getEntity().getName(), e);
            }
            this.setMoveIndex(movesTag.getInt(TagNames.MOVEINDEX));
            this.setAttackCooldown(movesTag.getInt(TagNames.COOLDOWN));
            final int[] disables = movesTag.getIntArray(TagNames.DISABLED);
            if (disables.length == 4) for (int i = 0; i < 4; i++)
                this.setDisableTimer(i, disables[i]);
        }
        // Read Inventory tag
        if (!inventoryTag.isEmpty())
        {
            final ListNBT ListNBT = inventoryTag.getList(TagNames.ITEMS, 10);
            for (int i = 0; i < ListNBT.size(); ++i)
            {
                final CompoundNBT CompoundNBT1 = ListNBT.getCompound(i);
                final int j = CompoundNBT1.getByte("Slot") & 255;
                if (j < this.getInventory().getSizeInventory()) this.getInventory().setInventorySlotContents(j,
                        ItemStack.read(CompoundNBT1));
                this.setHeldItem(this.getInventory().getStackInSlot(1));
            }
        }
        // Read Breeding tag
        if (!breedingTag.isEmpty()) this.loveTimer = breedingTag.getInt(TagNames.SEXETIME);
        // Read visuals tag
        if (!visualsTag.isEmpty())
        {
            this.dataSync().set(this.params.DYECOLOUR, visualsTag.getInt(TagNames.SPECIALTAG));
            final int[] flavourAmounts = visualsTag.getIntArray(TagNames.FLAVOURSTAG);
            if (flavourAmounts.length == 5) for (int i = 0; i < flavourAmounts.length; i++)
                this.setFlavourAmount(i, flavourAmounts[i]);
            if (visualsTag.contains(TagNames.POKECUBE))
            {
                final CompoundNBT pokecubeTag = visualsTag.getCompound(TagNames.POKECUBE);
                this.setPokecube(ItemStack.read(pokecubeTag));
            }
            this.setCustomTexDetails(visualsTag.getString(TagNames.TEX));
            if (visualsTag.contains(TagNames.MODEL)) this.setCustomModel(new ResourceLocation(visualsTag.getString(
                    TagNames.MODEL)));
            if (visualsTag.contains(TagNames.ANIM)) this.setCustomAnims(new ResourceLocation(visualsTag.getString(
                    TagNames.ANIM)));
        }

        // Read AI
        if (!aiTag.isEmpty())
        {
            this.setTotalCombatState(aiTag.getInt(TagNames.COMBATSTATE));
            this.setTotalGeneralState(aiTag.getInt(TagNames.GENERALSTATE));
            this.setTotalLogicState(aiTag.getInt(TagNames.LOGICSTATE));
            this.cleanLoadedAIStates();

            this.setHungerTime(aiTag.getInt(TagNames.HUNGER));
            final CompoundNBT routines = aiTag.getCompound(TagNames.AIROUTINES);
            for (final String s : routines.keySet())
                // try/catch block incase addons add more routines to the enum.
                try
                {
                    final AIRoutine routine = AIRoutine.valueOf(s);
                    this.setRoutineState(routine, routines.getBoolean(s));
                }
                catch (final Exception e)
                {

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
    public CompoundNBT write()
    {
        final CompoundNBT pokemobTag = new CompoundNBT();
        pokemobTag.putInt(TagNames.VERSION, 1);
        // Write Ownership tag
        final CompoundNBT ownerShipTag = new CompoundNBT();
        // This is still written for pokecubes to read from. Actual number is
        // stored in genes.
        ownerShipTag.putInt(TagNames.POKEDEXNB, this.getPokedexNb());
        // Write the owner here, this is only for use for lookups, the holder
        // actually saves it.
        if (this.getOwnerId() != null) ownerShipTag.putString(TagNames.OWNER, this.getOwnerId().toString());
        ownerShipTag.putString(TagNames.NICKNAME, this.getPokemonNickname());
        ownerShipTag.putString(TagNames.TEAM, this.getPokemobTeam());
        if (this.getOriginalOwnerUUID() != null) ownerShipTag.putString(TagNames.OT, this.getOriginalOwnerUUID()
                .toString());

        // Write stats tag
        final CompoundNBT statsTag = new CompoundNBT();
        statsTag.putInt(TagNames.EXP, this.getExp());
        statsTag.putByte(TagNames.STATUS, this.getStatus());
        statsTag.putInt(TagNames.HAPPY, this.bonusHappiness);
        statsTag.putFloat(TagNames.DYNAPOWER, this.getDynamaxFactor());

        // Write moves tag
        final CompoundNBT movesTag = new CompoundNBT();
        movesTag.putInt(TagNames.MOVEINDEX, this.getMoveIndex());
        if (!this.getMoveStats().newMoves.isEmpty())
        {
            final ListNBT newMoves = new ListNBT();
            for (final String s : this.getMoveStats().newMoves)
                newMoves.add(new StringNBT(s));
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
        final CompoundNBT inventoryTag = new CompoundNBT();
        final ListNBT ListNBT = new ListNBT();
        this.getInventory().setInventorySlotContents(1, this.getHeldItem());
        for (int i = 0; i < this.getInventory().getSizeInventory(); ++i)
        {
            final ItemStack itemstack = this.getInventory().getStackInSlot(i);
            if (!itemstack.isEmpty())
            {
                final CompoundNBT CompoundNBT1 = new CompoundNBT();
                CompoundNBT1.putByte("Slot", (byte) i);
                itemstack.write(CompoundNBT1);
                ListNBT.add(CompoundNBT1);
            }
        }
        inventoryTag.put(TagNames.ITEMS, ListNBT);

        // Write Breeding tag
        final CompoundNBT breedingTag = new CompoundNBT();
        breedingTag.putInt(TagNames.SEXETIME, this.loveTimer);

        // Write visuals tag
        final CompoundNBT visualsTag = new CompoundNBT();

        // This is still written for pokecubes to read from. Actual form is
        // stored in genes.
        visualsTag.putString(TagNames.FORME, this.getPokedexEntry().getTrimmedName());
        visualsTag.putString(TagNames.TEX, this.getCustomTex());
        if (this.getCustomModel() != null) visualsTag.putString(TagNames.MODEL, this.getCustomModel().toString());
        if (this.getCustomAnims() != null) visualsTag.putString(TagNames.ANIM, this.getCustomAnims().toString());
        visualsTag.putInt(TagNames.SPECIALTAG, this.dataSync().get(this.params.DYECOLOUR));
        final int[] flavourAmounts = new int[5];
        for (int i = 0; i < flavourAmounts.length; i++)
            flavourAmounts[i] = this.getFlavourAmount(i);
        visualsTag.putIntArray(TagNames.FLAVOURSTAG, flavourAmounts);
        if (!this.getPokecube().isEmpty())
        {
            final CompoundNBT pokecubeTag = this.getPokecube().write(new CompoundNBT());
            visualsTag.put(TagNames.POKECUBE, pokecubeTag);
        }
        // Misc AI
        final CompoundNBT aiTag = new CompoundNBT();

        aiTag.putInt(TagNames.GENERALSTATE, this.getTotalGeneralState());
        aiTag.putInt(TagNames.LOGICSTATE, this.getTotalLogicState());
        aiTag.putInt(TagNames.COMBATSTATE, this.getTotalCombatState());

        aiTag.putInt(TagNames.HUNGER, this.getHungerTime());
        final CompoundNBT aiRoutineTag = new CompoundNBT();
        for (final AIRoutine routine : AIRoutine.values())
            aiRoutineTag.putBoolean(routine.toString(), this.isRoutineEnabled(routine));
        aiTag.put(TagNames.AIROUTINES, aiRoutineTag);
        final CompoundNBT taskTag = new CompoundNBT();
        for (final IAIRunnable task : this.getTasks())
            if (task instanceof INBTSerializable) taskTag.put(task.getIdentifier(), ((INBTSerializable<?>) task)
                    .serializeNBT());
        aiTag.put(TagNames.AITAG, taskTag);

        // Misc other
        final CompoundNBT miscTag = new CompoundNBT();
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

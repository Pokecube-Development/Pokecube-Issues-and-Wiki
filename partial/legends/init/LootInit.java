package pokecube.legends.init;

import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LootInit 
{	
	public static LootEntry shard = new LootEntryItem(
			ItemInit.CRYSTAL_SHARD, 100, 2, new LootFunction[0], new LootCondition[0], "crystal_shard");
	public static LootEntry prison = new LootEntryItem(
			ItemInit.PRISION_BOTTLE, 100, 1, new LootFunction[0], new LootCondition[0], "prisonbottle");
	public static LootEntry glass = new LootEntryItem(
			ItemInit.REVEAL_GLASS, 100, 1, new LootFunction[0], new LootCondition[0], "revealglass");
	public static LootEntry floer = new LootEntryItem(
			ItemInit.REVEAL_GLASS, 100, 1, new LootFunction[0], new LootCondition[0], "gracidea");
	public static LootEntry dnaa = new LootEntryItem(
			ItemInit.DNA_SPLICERA, 100, 1, new LootFunction[0], new LootCondition[0], "dna_splicera");
	public static LootEntry dnab = new LootEntryItem(
			ItemInit.DNA_SPLICERB, 100, 1, new LootFunction[0], new LootCondition[0], "dna_splicerb");
	public static LootEntry rainbowwing = new LootEntryItem(
			ItemInit.RAINBOW_WING, 100, 1, new LootFunction[0], new LootCondition[0], "rainbow_wing");
	public static LootEntry silverwing = new LootEntryItem(
			ItemInit.SILVER_WING, 100, 1, new LootFunction[0], new LootCondition[0], "silver_wing");
	
	@SubscribeEvent
	public void onLootTableLoad(final LootTableLoadEvent event) {
		
        if(event.getName().equals(LootTableList.CHESTS_SIMPLE_DUNGEON) ||
        		event.getName().equals(LootTableList.CHESTS_ABANDONED_MINESHAFT) ||
        				event.getName().equals(LootTableList.CHESTS_DESERT_PYRAMID)) {
    	
            event.getTable().getPool("main").addEntry(shard);
            event.getTable().getPool("main").addEntry(prison);
            event.getTable().getPool("main").addEntry(glass);
            event.getTable().getPool("main").addEntry(floer);
            event.getTable().getPool("main").addEntry(dnaa);
            event.getTable().getPool("main").addEntry(dnab);
            event.getTable().getPool("main").addEntry(silverwing);
            event.getTable().getPool("main").addEntry(rainbowwing);
        }
	}
}

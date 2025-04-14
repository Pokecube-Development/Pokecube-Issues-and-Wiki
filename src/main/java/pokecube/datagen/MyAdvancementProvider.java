package pokecube.datagen;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MyAdvancementProvider extends AdvancementProvider
{ // Parameters can be obtained from GatherDataEvent.
    public MyAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        // Add an instance of our generator to the list parameter. This can be done as many times as you want.
        // Having multiple generators is purely for organization, all functionality can be achieved with a single generator.
        super(output, lookupProvider, existingFileHelper, List.of(new MyAdvancementGenerator()));
    }

    private static final class MyAdvancementGenerator implements AdvancementProvider.AdvancementGenerator {
        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {
            // Generate your advancements here.
            // All methods follow the builder pattern, meaning that chaining is possible and encouraged.
            // For better readability of the explanations, chaining will not be done here.

            // Create an advancement builder using the static #advancement() method.
            // Using #advancement() automatically enables telemetry events. If you do not want this,
            // #recipeAdvancement() can be used instead, there are no other functional differences.
            Advancement.Builder builder = Advancement.Builder.advancement();

            // Sets the parent of the advancement. You can use another advancement you have already generated,
            // or create a placeholder advancement using the static AdvancementSubProvider#createPlaceholder method.
            builder.parent(AdvancementSubProvider.createPlaceholder("pokecube_mobs:test/root"));

            // Sets the display properties of the advancement. This can either be a DisplayInfo object,
            // or pass in the values directly. If values are passed in directly, a DisplayInfo object will be created for you.
            builder.display(
                    // The advancement icon. Can be an ItemStack or an ItemLike.
                    new ItemStack(Items.GRASS_BLOCK),
                    // The advancement title and description. Don't forget to add translations for these!
                    Component.translatable("advancements.examplemod.example_advancement.title"),
                    Component.translatable("advancements.examplemod.example_advancement.description"),
                    // The background texture. Use null if you don't want a background texture (for non-root advancements).
                    null,
                    // The frame type. Valid values are AdvancementType.TASK, CHALLENGE, or GOAL.
                    AdvancementType.GOAL,
                    // Whether to show the advancement toast or not.
                    true,
                    // Whether to announce the advancement into chat or not.
                    true,
                    // Whether the advancement should be hidden or not.
                    false
            );

            // An advancement reward builder. Can be created with any of the four reward types, and further rewards
            // can be added using the methods prefixed with add. This can also be built beforehand,
            // and the resulting AdvancementRewards can then be reused across multiple advancement builders.
            builder.rewards(
                    // Alternatively, use addExperience() to add to an existing builder.
                    AdvancementRewards.Builder.experience(100)
                            // Alternatively, use loot() to create a new builder.
                            .addLootTable(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("minecraft", "chests/igloo")))
                            // Alternatively, use recipe() to create a new builder.
                            .addRecipe(ResourceLocation.fromNamespaceAndPath("minecraft", "iron_ingot"))
                            // Alternatively, use function() to create a new builder.
                            .runs(ResourceLocation.fromNamespaceAndPath("examplemod", "example_function"))
            );

            // Adds a criterion with the given name to the advancement. Use the corresponding trigger instance's static method.
            builder.addCriterion("pickup_dirt", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIRT));

            // Adds a requirements handler. Minecraft natively provides allOf() and anyOf(), more complex requirements
            // must be implemented manually. Only has an effect with two or more criteria.
            builder.requirements(AdvancementRequirements.allOf(List.of("pickup_dirt")));

            // Save the advancement to disk, using the given resource location. This returns an AdvancementHolder,
            // which may be stored in a variable and used as a parent by other advancement builders.
            builder.save(saver, ResourceLocation.fromNamespaceAndPath("examplemod", "example_advancement"), existingFileHelper);
        }
    }
}

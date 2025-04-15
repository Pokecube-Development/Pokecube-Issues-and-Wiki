package pokecube.datagen;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.advancements.Triggers;
import pokecube.adventures.advancements.triggers.BeatLeaderTrigger;
import pokecube.adventures.advancements.triggers.BeatTrainerTrigger;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeItems;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class PokecubeAdvAdvancements extends AdvancementProvider
{ // Parameters can be obtained from GatherDataEvent.
    public PokecubeAdvAdvancements(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
            ExistingFileHelper existingFileHelper)
    {
        // Add an instance of our generator to the list parameter. This can be done as many times as you want.
        // Having multiple generators is purely for organization, all functionality can be achieved with a single generator.
        super(output, lookupProvider, existingFileHelper, List.of(new MyAdvancementGenerator()));
    }

    private static final class MyAdvancementGenerator implements AdvancementGenerator
    {
        private void generateBadges(PokeType type, HolderLookup.Provider registries, Consumer<AdvancementHolder> saver,
                ExistingFileHelper existingFileHelper)
        {
            Advancement.Builder builder = Advancement.Builder.recipeAdvancement();
            Item badge = PokecubeAdv.BADGES.get(type);
            var type_name = type.name;
            if(type_name.equals("???")) type_name = "unknown";

            // Sets the parent of the advancement. You can use another advancement you have already generated,
            // or create a placeholder advancement using the static AdvancementSubProvider#createPlaceholder method.
//            builder.parent(AdvancementSubProvider.createPlaceholder("pokecube_adventures:trainers/root"));
            builder.parent(AdvancementSubProvider.createPlaceholder("pokecube_mobs:capture/root"));

            var TYPE = AdvancementType.CHALLENGE;
            // Sets the display properties of the advancement. This can either be a DisplayInfo object,
            // or pass in the values directly. If values are passed in directly, a DisplayInfo object will be created for you.
            builder.display(
                    // The advancement icon. Can be an ItemStack or an ItemLike.
                    badge,
                    // The advancement title and description. Don't forget to add translations for these!
                    Component.translatable("achievement.pokeadv.get.badge" + type_name),
                    Component.translatable("achievement.pokeadv.get.badge" + type_name + ".desc"),
                    // The background texture. Use null if you don't want a background texture (for non-root advancements).
                    null,
                    // The frame type. Valid values are AdvancementType.TASK, CHALLENGE, or GOAL.
                    TYPE,
                    // Whether to show the advancement toast or not.
                    true,
                    // Whether to announce the advancement into chat or not.
                    true,
                    // Whether the advancement should be hidden or not.
                    false);

            var reward = AdvancementRewards.Builder.experience(100);
            // An advancement reward builder. Can be created with any of the four reward types, and further rewards
            // can be added using the methods prefixed with add. This can also be built beforehand,
            // and the resulting AdvancementRewards can then be reused across multiple advancement builders.
            builder.rewards(reward);

            var name = "get_" + type_name + "_badge";
            builder.addCriterion(name, InventoryChangeTrigger.TriggerInstance.hasItems(badge));
            // Adds a requirements handler. Minecraft natively provides allOf() and anyOf(), more complex requirements
            // must be implemented manually. Only has an effect with two or more criteria.
            builder.requirements(AdvancementRequirements.allOf(List.of(name)));

            // Save the advancement to disk, using the given resource location. This returns an AdvancementHolder,
            // which may be stored in a variable and used as a parent by other advancement builders.
            builder.save(saver, ResourceLocation.fromNamespaceAndPath("pokecube_adventures", "trainers/" + name),
                    existingFileHelper);
        }

        private void generateNPC(String type, HolderLookup.Provider registries, Consumer<AdvancementHolder> saver,
                ExistingFileHelper existingFileHelper)
        {
            Advancement.Builder builder = Advancement.Builder.recipeAdvancement();

            // Sets the parent of the advancement. You can use another advancement you have already generated,
            // or create a placeholder advancement using the static AdvancementSubProvider#createPlaceholder method.
//            builder.parent(AdvancementSubProvider.createPlaceholder("pokecube_adventures:trainers/root"));
            builder.parent(AdvancementSubProvider.createPlaceholder("pokecube_mobs:capture/root"));

            var TYPE = type.equals("trainer") ? AdvancementType.CHALLENGE : AdvancementType.GOAL;
            // Sets the display properties of the advancement. This can either be a DisplayInfo object,
            // or pass in the values directly. If values are passed in directly, a DisplayInfo object will be created for you.
            builder.display(
                    // The advancement icon. Can be an ItemStack or an ItemLike.
                    PokecubeItems.getStack("pokecube"),
                    // The advancement title and description. Don't forget to add translations for these!
                    Component.translatable("achievement.pokeadv.defeat." + type),
                    Component.translatable("achievement.pokeadv.defeat." + type + ".desc"),
                    // The background texture. Use null if you don't want a background texture (for non-root advancements).
                    null,
                    // The frame type. Valid values are AdvancementType.TASK, CHALLENGE, or GOAL.
                    TYPE,
                    // Whether to show the advancement toast or not.
                    true,
                    // Whether to announce the advancement into chat or not.
                    true,
                    // Whether the advancement should be hidden or not.
                    false);

            var reward = AdvancementRewards.Builder.experience(100);
            // An advancement reward builder. Can be created with any of the four reward types, and further rewards
            // can be added using the methods prefixed with add. This can also be built beforehand,
            // and the resulting AdvancementRewards can then be reused across multiple advancement builders.
            builder.rewards(reward);

            var name = "beat_" + type;
            if (type.equals("trainer"))
            {
                builder.addCriterion(name, Triggers.BEATTRAINER.get()
                        .createCriterion(new BeatTrainerTrigger.TriggerInstance(Optional.empty())));
            }
            else if (type.equals("leader"))
            {
                builder.addCriterion(name, Triggers.BEATLEADER.get()
                        .createCriterion(new BeatLeaderTrigger.TriggerInstance(Optional.empty())));
            }
            // Adds a requirements handler. Minecraft natively provides allOf() and anyOf(), more complex requirements
            // must be implemented manually. Only has an effect with two or more criteria.
            builder.requirements(AdvancementRequirements.allOf(List.of(name)));

            // Save the advancement to disk, using the given resource location. This returns an AdvancementHolder,
            // which may be stored in a variable and used as a parent by other advancement builders.
            builder.save(saver, ResourceLocation.fromNamespaceAndPath("pokecube_adventures", "trainers/" + name),
                    existingFileHelper);
        }

        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver,
                ExistingFileHelper existingFileHelper)
        {
            for (PokeType type : PokeType.values()) generateBadges(type, registries, saver, existingFileHelper);
            generateNPC("trainer", registries, saver, existingFileHelper);
            generateNPC("leader", registries, saver, existingFileHelper);
        }
    }
}

package pokecube.datagen;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import pokecube.api.data.PokedexEntry;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.handlers.playerdata.advancements.triggers.CatchPokemobTrigger;
import pokecube.core.handlers.playerdata.advancements.triggers.FirstPokemobTrigger;
import pokecube.core.handlers.playerdata.advancements.triggers.HatchPokemobTrigger;
import pokecube.core.handlers.playerdata.advancements.triggers.KillPokemobTrigger;

import java.util.function.Consumer;

public class PokecubeMobsAdvancements extends AdvancementProvider
{
    public PokecubeMobsAdvancements(DataGenerator generatorIn, ExistingFileHelper fileHelperIn)
    {
        super(generatorIn, fileHelperIn);
    }

    @Override
    protected void registerAdvancements(Consumer<Advancement> consumer, ExistingFileHelper fileHelper)
    {
        new CaptureAdvancements().accept(consumer);
        new KillAdvancements().accept(consumer);
        new HatchAdvancements().accept(consumer);
    }

    public static class CaptureAdvancements implements Consumer<Consumer<Advancement>>
    {
        @Override
        public void accept(Consumer<Advancement> advancementConsumer)
        {
            Advancement root = Advancement.Builder.advancement().display(PokecubeItems.getStack("pokecube"),
                            new TranslatableComponent("achievement.pokecube.catch.root"),
                            new TranslatableComponent("achievement.pokecube.catch.root.desc"),
                            new ResourceLocation("minecraft:textures/gui/advancements/backgrounds/adventure.png"),
                            FrameType.TASK, false, false, false)
                    .addCriterion("get_a_pokemob", new FirstPokemobTrigger.Instance(EntityPredicate.Composite.ANY))
                    .save(advancementConsumer, "pokecube_mobs:capture/root");
            Advancement getFirst = Advancement.Builder.advancement().parent(root)
                    .display(PokecubeItems.getStack("pokecube"),
                            new TranslatableComponent("achievement.pokecube.get1st"),
                            new TranslatableComponent("achievement.pokecube.get1st.desc"), null, FrameType.TASK, true,
                            true, false)
                    .addCriterion("get_a_pokemob", new FirstPokemobTrigger.Instance(EntityPredicate.Composite.ANY))
                    .save(advancementConsumer, "pokecube_mobs:capture/get_first_pokemob");

            for (PokedexEntry entry : Database.getSortedFormes())
            {
                var name = entry.getTrimmedName();
                Advancement.Builder.advancement().parent(getFirst).display(PokecubeItems.getStack("pokecube"),
                                new TranslatableComponent("achievement.pokecube.catch",
                                        new TranslatableComponent("entity.pokecube." + name)),
                                new TranslatableComponent("achievement.pokecube.catch.desc",
                                        new TranslatableComponent("entity.pokecube." + name)), null, FrameType.TASK,
                                true, true, false).addCriterion("catch_" + name,
                                new CatchPokemobTrigger.Instance(EntityPredicate.Composite.ANY, entry, false, -1, 0))
                        .save(advancementConsumer, "pokecube_mobs:capture/catch_" + name);
            }
        }
    }

    public static class KillAdvancements implements Consumer<Consumer<Advancement>>
    {
        @Override
        public void accept(Consumer<Advancement> advancementConsumer)
        {
            Advancement root = Advancement.Builder.advancement().display(PokecubeItems.getStack("pokecube"),
                            new TranslatableComponent("achievement.pokecube.kill.root"),
                            new TranslatableComponent("achievement.pokecube.kill.root.desc"),
                            new ResourceLocation("minecraft:textures/gui/advancements/backgrounds/adventure.png"),
                            FrameType.TASK, false, false, false)
                    .addCriterion("get_a_pokemob", new FirstPokemobTrigger.Instance(EntityPredicate.Composite.ANY))
                    .save(advancementConsumer, "pokecube_mobs:kill/root");

            for (PokedexEntry entry : Database.getSortedFormes())
            {
                var name = entry.getTrimmedName();
                Advancement.Builder.advancement().parent(root).display(PokecubeItems.getStack("pokecube"),
                                new TranslatableComponent("achievement.pokecube.kill",
                                        new TranslatableComponent("entity.pokecube." + name)),
                                new TranslatableComponent("achievement.pokecube.kill.desc",
                                        new TranslatableComponent("entity.pokecube." + name)), null, FrameType.TASK, true,
                                true, false).addCriterion("kill_" + name,
                                new KillPokemobTrigger.Instance(EntityPredicate.Composite.ANY, entry))
                        .save(advancementConsumer, "pokecube_mobs:kill/kill_" + name);
            }
        }
    }

    public static class HatchAdvancements implements Consumer<Consumer<Advancement>>
    {
        @Override
        public void accept(Consumer<Advancement> advancementConsumer)
        {
            Advancement root = Advancement.Builder.advancement().display(PokecubeItems.getStack("pokecube"),
                            new TranslatableComponent("achievement.pokecube.hatch.root"),
                            new TranslatableComponent("achievement.pokecube.hatch.root.desc"),
                            new ResourceLocation("minecraft:textures/gui/advancements/backgrounds/adventure.png"),
                            FrameType.TASK, false, false, false)
                    .addCriterion("get_a_pokemob", new FirstPokemobTrigger.Instance(EntityPredicate.Composite.ANY))
                    .save(advancementConsumer, "pokecube_mobs:hatch/root");

            for (PokedexEntry entry : Database.getSortedFormes())
            {
                var name = entry.getTrimmedName();
                Advancement.Builder.advancement().parent(root).display(PokecubeItems.getStack("pokecube"),
                                new TranslatableComponent("achievement.pokecube.hatch",
                                        new TranslatableComponent("entity.pokecube." + name)),
                                new TranslatableComponent("achievement.pokecube.hatch.desc",
                                        new TranslatableComponent("entity.pokecube." + name)), null, FrameType.TASK,
                                true, true, false).addCriterion("hatch_" + name,
                                new HatchPokemobTrigger.Instance(EntityPredicate.Composite.ANY, entry))
                        .save(advancementConsumer, "pokecube_mobs:hatch/hatch_" + name);
            }
        }
    }
}

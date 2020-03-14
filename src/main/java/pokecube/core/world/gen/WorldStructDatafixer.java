package pokecube.core.world.gen;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiFunction;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerUpper;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.functions.PointFreeRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;

import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.NamespacedSchema;
import net.minecraft.util.datafix.TypeReferences;
import pokecube.core.database.worldgen.WorldgenHandler;

public class WorldStructDatafixer extends DataFix
{
    protected static final PointFreeRule OPTIMIZATION_RULE = DataFixUtils.make(() ->
    {
        final PointFreeRule opSimple = PointFreeRule.orElse(PointFreeRule.orElse(PointFreeRule.CataFuseSame.INSTANCE,
                PointFreeRule.orElse(PointFreeRule.CataFuseDifferent.INSTANCE, PointFreeRule.LensAppId.INSTANCE)),
                PointFreeRule.orElse(PointFreeRule.LensComp.INSTANCE, PointFreeRule.orElse(
                        PointFreeRule.AppNest.INSTANCE, PointFreeRule.LensCompFunc.INSTANCE)));
        final PointFreeRule opLeft = PointFreeRule.many(PointFreeRule.once(PointFreeRule.orElse(opSimple,
                PointFreeRule.CompAssocLeft.INSTANCE)));
        final PointFreeRule opComp = PointFreeRule.many(PointFreeRule.once(PointFreeRule.orElse(
                PointFreeRule.SortInj.INSTANCE, PointFreeRule.SortProj.INSTANCE)));
        final PointFreeRule opRight = PointFreeRule.many(PointFreeRule.once(PointFreeRule.orElse(opSimple,
                PointFreeRule.CompAssocRight.INSTANCE)));
        return PointFreeRule.seq(ImmutableList.of(() -> opLeft, () -> opComp, () -> opRight, () -> opLeft,
                () -> opRight));
    });

    protected static int getLowestSchemaSameVersion(final Int2ObjectSortedMap<Schema> schemas, final int versionKey)
    {
        if (versionKey < schemas.firstIntKey()) // can't have a data type before
                                                // anything else
            return schemas.firstIntKey();
        return schemas.subMap(0, versionKey + 1).lastIntKey();
    }

    private static int getLowestFixSameVersion(final IntSortedSet fixerVersions, final int versionKey)
    {
        if (versionKey < fixerVersions.firstInt()) // can have a version before
                                                   // everything else
            return fixerVersions.firstInt() - 1;
        return fixerVersions.subSet(0, versionKey + 1).lastInt();
    }

    protected static TypeRewriteRule getRule(final Long2ObjectMap<TypeRewriteRule> rules,
            final List<DataFix> globalList, final IntSortedSet fixerVersions, final int version, final int dataVersion)
    {
        if (version >= dataVersion) return TypeRewriteRule.nop();
        final int expandedVersion = WorldStructDatafixer.getLowestFixSameVersion(fixerVersions, DataFixUtils.makeKey(
                version));
        final int expandedDataVersion = DataFixUtils.makeKey(dataVersion);
        final long key = (long) expandedVersion << 32 | expandedDataVersion;
        return rules.computeIfAbsent(key, k ->
        {
            final List<TypeRewriteRule> rules2 = Lists.newArrayList();
            for (final DataFix fix : globalList)
            {
                final int fixVersion = fix.getVersionKey();
                if (fixVersion > expandedVersion && fixVersion <= expandedDataVersion)
                {
                    final TypeRewriteRule fixRule = fix.getRule();
                    if (fixRule == TypeRewriteRule.nop()) continue;
                    rules2.add(fixRule);
                }
            }
            return TypeRewriteRule.seq(rules2);
        });
    }

    @SuppressWarnings("unchecked")
    public static void insertFixer()
    {
        try
        {
            final Field schemasF = DataFixerUpper.class.getDeclaredField("schemas");
            final Field globalListF = DataFixerUpper.class.getDeclaredField("globalList");
            final Field fixerVersionsF = DataFixerUpper.class.getDeclaredField("fixerVersions");
            final Field rulesF = DataFixerUpper.class.getDeclaredField("rules");

            schemasF.setAccessible(true);
            globalListF.setAccessible(true);
            fixerVersionsF.setAccessible(true);
            rulesF.setAccessible(true);
            final DataFixer FIXER = DataFixesManager.getDataFixer();
            final Int2ObjectSortedMap<Schema> schemas = (Int2ObjectSortedMap<Schema>) schemasF.get(FIXER);
            final List<DataFix> globalList = (List<DataFix>) globalListF.get(FIXER);
            final IntSortedSet fixerVersions = (IntSortedSet) fixerVersionsF.get(FIXER);
            final Long2ObjectMap<TypeRewriteRule> rules = (Long2ObjectMap<TypeRewriteRule>) rulesF.get(FIXER);

            final BiFunction<Integer, Schema, Schema> NAMESPACED_SCHEMA_FACTORY = NamespacedSchema::new;

            final int key = DataFixUtils.makeKey(8888, 0);
            final Schema parent = schemas.isEmpty() ? null
                    : schemas.get(WorldStructDatafixer.getLowestSchemaSameVersion(schemas, key - 1));
            final Schema schema = NAMESPACED_SCHEMA_FACTORY.apply(DataFixUtils.makeKey(8888, 0), parent);
            schemas.put(schema.getVersionKey(), schema);

            final DataFix fixer = new WorldStructDatafixer(schema, false);
            globalList.add(fixer);
            fixerVersions.add(fixer.getVersionKey());

            final Type<?> type = schema.getType(TypeReferences.STRUCTURE_FEATURE);
            final TypeRewriteRule rule = fixer.getRule();
            final long key2 = (long) key << 32 | key;
            rules.put(key2, rule);
            type.rewrite(rule, WorldStructDatafixer.OPTIMIZATION_RULE);
        }
        catch (final Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public WorldStructDatafixer(final Schema outputSchema, final boolean changesType)
    {
        super(outputSchema, changesType);
    }

    @Override
    public TypeRewriteRule makeRule()
    {
        final Type<?> type = this.getInputSchema().getType(TypeReferences.STRUCTURE_FEATURE);
        final OpticFinder<?> opticfinder = type.findField("Children");
        return this.fixTypeEverywhereTyped("WorldStructDatafixer", type, (type2) ->
        {
            return type2.updateTyped(opticfinder, (type3) ->
            {
                return type3.update(DSL.remainderFinder(), (dynamic) ->
                {
                    return this.fixTag(type2.get(DSL.remainderFinder()), dynamic);
                });
            });
        });
    }

    private Dynamic<?> fixTag(final Dynamic<?> in, Dynamic<?> out)
    {
        final String s = in.get("id").asString("");
        if (s.startsWith("pokecube") && !WorldgenHandler.structs.containsKey(s)) out = out.set("id", out.createString(
                "INVALID"));
        return out;
    }

}

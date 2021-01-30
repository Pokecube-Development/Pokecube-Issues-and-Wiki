package pokecube.core.database.genes;

import java.util.List;

import com.google.common.collect.Lists;

public class Mutations
{
    public static class Mutation
    {
        public String result;
        public float  weight = 1;

        float _cutoff = 0;

        @Override
        public boolean equals(final Object obj)
        {
            if (obj == this) return true;
            if (obj instanceof Mutation) return ((Mutation) obj).result.equals(this.result);
            return false;
        }

        @Override
        public int hashCode()
        {
            return this.result.hashCode();
        }
    }

    public static class MutationHolder
    {
        public boolean replace = false;

        public String geneType;

        public String output;

        public String geneA;
        public String geneB;

        public List<Mutation> options = Lists.newArrayList();

        float _total = 0;

        public List<Mutation> _sorted = Lists.newArrayList();

        @Override
        public String toString()
        {
            String key = this.output;
            if (this.geneA != null || this.geneB != null) key = this.geneA + "+" + this.geneB + "->" + key;
            return key;
        }

        public Mutation getFor(final float value)
        {
            if (this._total == 0) throw new IllegalStateException("Cannot have no mutations!");
            Mutation mutation = this._sorted.get(0);
            for (int i = 1; i < this._sorted.size(); i++)
            {
                mutation = this._sorted.get(i - 1);
                final float prev = mutation._cutoff;
                final float here = this._sorted.get(i)._cutoff;
                if (prev < value && here >= value) return this._sorted.get(i);
            }
            return mutation;
        }

        public void postProcess()
        {
            this._sorted.clear();
            this._total = 0;

            for (final Mutation m : this.options)
            {
                this._total += m.weight;
                m._cutoff = this._total;
                this._sorted.add(m);
            }
            for (final Mutation m : this.options)
                m._cutoff /= this._total;
        }
    }

    public boolean replace = false;

    public List<MutationHolder> mutations = Lists.newArrayList();

}

package ml.kanfa.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Ibrahim Maïga.
 */
public class Generators {

    /**
     * suppresses default constructor, no-instance outer class
     * static methods are used to return generator {@link Generator} instances
     */
    private Generators(){}

    @SafeVarargs
    @SuppressWarnings("UnusedDeclaration")
    public static <T> Generator<T> newCombination(final T... args) {
        return new Combination<>(args);
    }

    @SafeVarargs
    @SuppressWarnings("UnusedDeclaration")
    public static <T> Generator<T> newPermutation(final T... args) {
        return new Permutation<>(args);
    }

    private static List<List<Integer>> iterativeIndexGenerate(int n, int p) throws Exception {
        List<List<Integer>> lists = createFirst(n);
        if (p > 0 && p <= n) {
            while (p > 1) {
                List<List<Integer>> arrayLists = new ArrayList<>();
                for (List<Integer> list : lists) {
                    for (int i = list.get(list.size() - 1) + 1; i <= n; i++) {
                        ArrayList<Integer> array = new ArrayList<>();
                        array.addAll(list);
                        array.add(i);
                        arrayLists.add(array);
                    }
                }
                lists = arrayLists;
                p--;
            }

        } else {
           throwException(n, p);
        }
        return lists;
    }

    private static void throwException(int n, int p) throws Exception{
        String message =   (p > n) ?  "p > n" : ( (p == 0) ? "p = 0" : "p < 0");
        throw new Exception(message);
    }

    private static List<List<Integer>> createFirst(int n) {
        final List<List<Integer>> lists = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            final ArrayList<Integer> array = new ArrayList<>();
            array.add(i);
            lists.add(array);
        }
        return lists;
    }

    /**
     * Inner Class Permutation
     * @param <T>
     */
    private static class Permutation<T> extends AbstractGenerator<T> {
        @SafeVarargs
        Permutation(T... values) {
            super(values);
        }

        protected List<List<Integer>> generateIndex(int p) throws Exception{
            List<List<Integer>> ars = iterativeIndexGenerate(this.n, p);
            final List<List<Integer>> arrays = new ArrayList<>();
            Objects.requireNonNull(ars);
            for (List<Integer> array : ars) {
                arrays.addAll(generateIndex(array, p >= 1 ? (p - 1) : 0));
            }

            return arrays;
        }

        private List<List<Integer>> generateIndex(final List<Integer> initial, int pos) {
            final List<List<Integer>> arrays = new ArrayList<>();
            if (pos == 0) {
                arrays.add(initial);
                return arrays;
            } else if (pos == 1)
                return this.circularGeneration(initial, pos);

            else {
                final List<List<Integer>> generatedIndex = generateIndex(initial, pos - 1);
                for (List<Integer> array : generatedIndex) {
                    arrays.addAll(circularGeneration(array, pos));
                }
            }
            return arrays;
        }

        private List<List<Integer>> circularGeneration(final List<Integer> initial, int pos) {
            final List<List<Integer>> arrays = new ArrayList<>();
            List<Integer> array = initial;

            do {
                arrays.add(array);
                array = new ArrayList<>(arrays.get(arrays.size() - 1));
                int first = array.get(pos - 1);

                for (int i = pos - 1; i < array.size() - 1; i++)
                    array.set(i, array.get(i + 1));

                array.set(array.size() - 1, first);

            } while (!array.equals(initial));

            return arrays;
        }
    }

    /**
     * Inner Class Combination
     * @param <T>
     */
    private static class Combination<T> extends AbstractGenerator<T> {
        @SafeVarargs
        Combination(final T... values) {
            super(values);
        }

        @Override
        protected List<List<Integer>> generateIndex(int p) throws Exception {
            return iterativeIndexGenerate(this.n, p);
        }
    }
}

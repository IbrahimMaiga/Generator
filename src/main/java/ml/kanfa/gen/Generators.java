package ml.kanfa.gen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

/**
 * @author Ibrahim Maïga.
 */
public class Generators {

    /**
     * Suppresses default constructor to prevent instantiation, no-instance outer class
     * static methods are used to return {@link Generator} instances
     */
    private Generators() {
    }

    /**
     * Returns new {@link Generator} implementation instance
     *<p> A method static is used to access the instance,
     * preferred solution to the access via constructor,
     * this approach gives more possibility because one
     * can work on the instance before returning it,
     * and name is also much more explicit.</p>
     * @param args list of values
     * @param <T> generics type
     * @return new {@link Combination} instance
     */
    @SafeVarargs
    @SuppressWarnings("UnusedDeclaration")
    public static <T> Generator<T> newCombination(final T... args) {
        return new Combination<>(args);
    }

    /**
     * Returns new {@link Generator} implementation instance
     *<p> A method static is used to access the instance,
     * preferred solution to the access via constructor,
     * this approach gives more possibility because one
     * can work on the instance before returning it,
     * and name is also much more explicit.</p>
     * @param args list of values
     * @param <T> generics type
     * @return new {@link Permutation} instance
     */
    @SafeVarargs
    @SuppressWarnings("UnusedDeclaration")
    public static <T> Generator<T> newPermutation(final T... args) {
        return new Permutation<>(args);
    }

    private static List<List<Integer>> iterativeIndexGenerate(int n, int p) {
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
            throwIllegalArgumentException(n, p);
        }
        return lists;
    }

    /**
     * Throws {@link java.lang.IllegalArgumentException}
     * @param n elements size
     * @param p generation length
     */
    private static void throwIllegalArgumentException(int n, int p) {
        String message = (p > n) ? "p > n" : ((p == 0) ? "p = 0" : "p < 0");
        throw new IllegalArgumentException(message);
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

        protected List<List<Integer>> generateIndex(int p) {
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
        protected List<List<Integer>> generateIndex(int p) {
            return iterativeIndexGenerate(this.n, p);
        }
    }

    /**
     * Inner Class AbstractGenerator
     * @param <T>
     */
    static abstract class AbstractGenerator<T> implements Generator<T> {

        private static final String IDENTITY = "";
        private final List<T> tArray;
        protected int n;

        @SafeVarargs
        @SuppressWarnings("varargs")
        AbstractGenerator(final T... values) {
            Objects.requireNonNull(values);
            this.tArray = Arrays.asList(values);
            this.n = values.length;
        }

        protected abstract List<List<Integer>> generateIndex(int p);

        @Override
        @SuppressWarnings("UnusedDeclaration")
        public List<String> generateToWord(int p, char separator) {
            final List<String> lWords = new ArrayList<>();
            final List<List<Integer>> lIndex = generateIndex(p);
            lWords.addAll(lIndex.stream().map(integers -> toWord(integers, separator)).collect(Collectors.toList()));
            return lWords;
        }

        @Override
        @SuppressWarnings("UnusedDeclaration")
        public List<List<T>> generate(int p) {
            return this.generate(this.generateIndex(p));
        }

        private String toWord(final List<Integer> lIndex, char separator) {
            Objects.requireNonNull(lIndex);
            String s = (separator + "").trim();
            return lIndex
                    .stream()
                    .map(integer -> tArray.get((integer - 1)).toString())
                    .collect(Collectors.toList())
                    .stream()
                    .reduce(IDENTITY, (a, b) -> (a + (!a.equals(IDENTITY) ? s : IDENTITY) + b));
        }

        private List<List<T>> generate(final List<List<Integer>> listIndex) {
            Objects.requireNonNull(listIndex);
            int parallelism = Runtime.getRuntime().availableProcessors();
            final ForkJoinPool pool = new ForkJoinPool(parallelism);
            final GeneratorRecursiveTask<T> task = new GeneratorRecursiveTask<>(this.tArray, listIndex);
            return pool.invoke(task);
        }

        /**
         * Inner Class GeneratorRecursiveTask
         * @param <T>
         */
        private static class GeneratorRecursiveTask<T> extends RecursiveTask<List<List<T>>> {

            private final List<List<Integer>> lists;
            private final List<T> tArray;
            private static final int LIMIT = 200;

            GeneratorRecursiveTask(final List<T> tArray, final List<List<Integer>> lists) {
                this.tArray = tArray;
                this.lists = lists;
            }

            @Override
            protected List<List<T>> compute() {
                final List<List<T>> arrays;
                final List<ForkJoinTask<List<List<T>>>> subTasks = new ArrayList<>();
                int size = this.lists.size();
                if (size > LIMIT) {
                    arrays = new ArrayList<>();
                    int mod = size % LIMIT;
                    int rs = size / LIMIT;
                    for (int i = 0; i < rs; i++) {
                        final List<List<Integer>> l = this.lists.subList((LIMIT * i), (LIMIT) * (i + 1));
                        subTasks.add(new GeneratorRecursiveTask<>(this.tArray, l).fork());
                    }
                    if (mod != 0)
                        subTasks.add(new GeneratorRecursiveTask<>(this.tArray, this.lists.subList(size - mod, size)).fork());
                } else {
                    arrays = getAllIndexValues(this.lists);
                }

                for (ForkJoinTask<List<List<T>>> subTask : subTasks) {
                    arrays.addAll(subTask.join());
                }

                return arrays;
            }

            private List<List<T>> getAllIndexValues(final List<List<Integer>> lists) {
                return lists.stream()
                        .map(this::getIndexValues)
                        .collect(Collectors.toList());
            }

            private List<T> getIndexValues(final List<Integer> integers) {
                return integers.stream()
                        .map(integer -> this.tArray.get(integer - 1))
                        .collect(Collectors.toList());
            }
        }
    }
}
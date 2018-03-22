package ml.kanfa.gen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class Generators
 *
 * @author Ibrahim Maïga <maiga.ibrm@gmail.com>.
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
     * <p> A method static is used to access the instance,
     * preferred solution to the access via constructor,
     * this approach gives more possibility because one
     * can work on the instance before returning it,
     * and name is also much more explicit.</p>
     *
     * @param args list of values
     * @param <T>  generics type
     * @return new {@link Combination} instance
     */
    @SafeVarargs
    @SuppressWarnings("UnusedDeclaration")
    public static <T> Generator<T> newCombination(final T... args) {
        return new Combination<>(args);
    }

    /**
     * Returns new {@link Generator} implementation instance
     * <p> A method static is used to access the instance,
     * preferred solution to the access via constructor,
     * this approach gives more possibility because one
     * can work on the instance before returning it,
     * and name is also much more explicit.</p>
     *
     * @param args list of values
     * @param <T>  generics type
     * @return new {@link Permutation} instance
     */
    @SafeVarargs
    @SuppressWarnings("UnusedDeclaration")
    public static <T> Generator<T> newPermutation(final T... args) {
        return new Permutation<>(args);
    }

    private static List<List<Integer>> createIndex(int n, int p) {
        List<List<Integer>> lists = createFirst(n);
        if (p > 0 && p <= n) {
            while (p > 1) {
                List<List<Integer>> arrayLists = new ArrayList<>();
                lists.forEach(list -> {
                            for (int i = list.get(list.size() - 1) + 1; i <= n; i++) {
                                ArrayList<Integer> array = new ArrayList<>(list);
                                array.add(i);
                                arrayLists.add(array);
                            }
                        }
                );
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
     *
     * @param n elements size
     * @param p generation length
     */
    private static void throwIllegalArgumentException(int n, int p) {
        String message = (p > n) ? "p > n" : ((p == 0) ? "p = 0" : "p < 0");
        throw new IllegalArgumentException(message);
    }

    /**
     * Returns a list consisting of numbers less than or equal
     * to the value <code>n</code>, all generations are based on this
     *
     * @param n elements size
     * @return a list consisting of numbers less than or equal
     * to the value <code>n</code>, all generations are based on this
     */
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
     *
     * @param <T> generic parameter
     */
    private static class Permutation<T> extends AbstractGenerator<T> {
        @SafeVarargs
        Permutation(T... values) {
            super(values);
        }

        protected List<List<Integer>> generateIndex(int p) {
            final List<List<Integer>> indexes = createIndex(this.n, p);
//            int parallelism = Runtime.getRuntime().availableProcessors();
//            final ForkJoinPool pool = new ForkJoinPool(parallelism);
//            final PermutationRecursiveTask task = new PermutationRecursiveTask(indexes, p);
//            return pool.invoke(task);
            return collect(indexes.stream().map(array -> generateIndex(array, p >= 1 ? (p - 1) : 0)));
        }

        /**
         * Returns list of list the all combination corresponding to initial start in pos
         *
         * @param initial the initial value
         * @param pos     the generation start position
         * @return list of list the all permutation corresponding to initial start in pos
         */
        private List<List<Integer>> generateIndex(final List<Integer> initial, int pos) {
            Objects.requireNonNull(initial);
            if (pos == 0) {
                final List<List<Integer>> arrays = new ArrayList<>();
                arrays.add(initial);
                return arrays;
            } else if (pos == 1) {
                return this.circularGeneration(initial, pos);
            } else {
                return this.collect(generateIndex(initial, pos - 1), pos);
            }
        }

        private List<List<Integer>> collect(Stream<List<List<Integer>>> stream) {
            final List<List<Integer>> arrays = new ArrayList<>();
            stream.forEach(arrays::addAll);
            return arrays;
        }

        private List<List<Integer>> collect(List<List<Integer>> integers, int pos) {
            return this.collect(integers.stream().map(integer -> circularGeneration(integer, pos)));
        }

        /**
         * @param initial initial list
         * @param pos     the positions where the generation begins
         * @return list of elements
         */
        private List<List<Integer>> circularGeneration(final List<Integer> initial, int pos) {
            Objects.requireNonNull(initial);
            final List<List<Integer>> arrays = new ArrayList<>();
            List<Integer> array = initial;
            do {
                arrays.add(array);
                array = new ArrayList<>(arrays.get(arrays.size() - 1));
                this.permute(pos, array);
            } while (!array.equals(initial));
            return arrays;
        }

        private void permute(int pos, List<Integer> array) {
            int first = array.get(pos - 1);
            for (int i = pos - 1; i < array.size() - 1; i++) {
                array.set(i, array.get(i + 1));
            }
            array.set(array.size() - 1, first);
        }

        private class PermutationRecursiveTask extends RecursiveTask<List<List<Integer>>> {

            private final List<List<Integer>> indexes;
            private int position;
            private static final int LIMIT = 20;

            PermutationRecursiveTask(List<List<Integer>> indexes, int position) {
                this.indexes = Objects.requireNonNull(indexes);
                this.position = position;
            }

            @Override
            protected List<List<Integer>> compute() {
                final List<ForkJoinTask<List<List<Integer>>>> subTasks = new ArrayList<>();
                final List<List<Integer>> arrays;
                int size = indexes.size();
                if (size > LIMIT) {
                    arrays = new ArrayList<>();
                    int mod = size % LIMIT;
                    int rs = size / LIMIT;
                    for (int i = 0; i < rs; i++) {
                        final List<List<Integer>> indexList = this.indexes.subList((LIMIT * i), (LIMIT) * (i + 1));
                        subTasks.add(new PermutationRecursiveTask(indexList, this.position).fork());
                    }
                    if (mod != 0) {
                        subTasks.add(new PermutationRecursiveTask(this.indexes.subList(size - mod, size), this.position).fork());
                    }
                    subTasks.forEach(subTask -> arrays.addAll(subTask.join()));
                } else {
                    arrays = collect(this.indexes.stream().map(array -> generateIndex(array, this.position >= 1 ? (this.position - 1) : 0)));
                }
                return arrays;
            }
        }
    }

    /**
     * Inner Class Combination
     *
     * @param <T>
     */
    private static class Combination<T> extends AbstractGenerator<T> {
        @SafeVarargs
        Combination(final T... values) {
            super(values);
        }

        @Override
        protected List<List<Integer>> generateIndex(int p) {
            return createIndex(this.n, p);
        }
    }

    /**
     * Inner Class AbstractGenerator
     *
     * @param <T>
     */
    private static abstract class AbstractGenerator<T> implements Generator<T> {

        private static final String IDENTITY = "";
        private final List<T> tArray;
        int n;

        @SafeVarargs
        @SuppressWarnings("varargs")
        AbstractGenerator(final T... values) {
            Objects.requireNonNull(values);
            this.tArray = Arrays.asList(values);
            this.n = values.length;
        }

        /**
         * @param p generation length
         * @return list of index
         */
        protected abstract List<List<Integer>> generateIndex(int p);

        @Override
        @SuppressWarnings("UnusedDeclaration")
        public List<String> generateToWord(int p, char separator) {
            final List<List<Integer>> lIndex = generateIndex(p);
            return lIndex.stream().map(integers -> toWord(integers, separator)).collect(Collectors.toList());
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

        /**
         * @param listIndex index of elements to generate
         * @return a list of element list, each list contains
         * a set of objects, which are in fact the desired generation.
         */
        private List<List<T>> generate(final List<List<Integer>> listIndex) {
            Objects.requireNonNull(listIndex);
            int parallelism = Runtime.getRuntime().availableProcessors();
            final ForkJoinPool pool = new ForkJoinPool(parallelism);
            final GeneratorRecursiveTask<T> task = new GeneratorRecursiveTask<>(this.tArray, listIndex);
            return pool.invoke(task);
        }

        /**
         * Inner Class GeneratorRecursiveTask
         *
         * @param <T>
         */
        private static class GeneratorRecursiveTask<T> extends RecursiveTask<List<List<T>>> {

            private final List<List<Integer>> lists;
            private final List<T> tArray;
            private static final int LIMIT = 200;

            GeneratorRecursiveTask(final List<T> tArray, final List<List<Integer>> lists) {
                this.tArray = Objects.requireNonNull(tArray);
                this.lists = Objects.requireNonNull(lists);
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
                    if (mod != 0) {
                        subTasks.add(new GeneratorRecursiveTask<>(this.tArray, this.lists.subList(size - mod, size)).fork());
                    }
                    subTasks.forEach(subTask -> arrays.addAll(subTask.join()));
                } else {
                    arrays = getAllIndexValues(this.lists);
                }
                return arrays;
            }

            private List<List<T>> getAllIndexValues(final List<List<Integer>> lists) {
                Objects.requireNonNull(lists);
                return lists.stream()
                        .map(this::getIndexValues)
                        .collect(Collectors.toList());
            }

            private List<T> getIndexValues(final List<Integer> integers) {
                Objects.requireNonNull(integers);
                return integers.stream()
                        .map(integer -> this.tArray.get(integer - 1))
                        .collect(Collectors.toList());
            }
        }
    }
}
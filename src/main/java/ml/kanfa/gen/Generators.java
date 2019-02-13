package ml.kanfa.gen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Class Generators
 *
 * @author Ibrahim Maïga <maiga.ibrm@gmail.com>.
 */
public class Generators {

    @SuppressWarnings("UnusedDeclaration")
    private static Logger LOGGER = Logger.getLogger(Generators.class.getName());

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

    private static class LazyPool {
        private static int parallelism = Runtime.getRuntime().availableProcessors();
        private static ForkJoinPool pool = ForkJoinPool.commonPool();
    }

    @SuppressWarnings("UnusedDeclaration")
    private static Supplier<ForkJoinPool> pool(final int parallelism) {
        return () -> new ForkJoinPool(parallelism);
    }

    private static Supplier<ForkJoinPool> pool() {
        return () -> LazyPool.pool;
    }

    private static List<List<Integer>> createIndex(int n, int p) {
        List<List<Integer>> lists = createFirst(n);
        if (p > 0 && p <= n) {
            while (p > 1) {
                List<List<Integer>> arrayLists = new ArrayList<>();
                lists.forEach(list -> IntStream
                        .rangeClosed(list.get(list.size() - 1) + 1, n)
                        .forEach(i -> {
                            final List<Integer> array = new ArrayList<>(list);
                            array.add(i);
                            arrayLists.add(array);
                        }));
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
        final String message = (p > n) ? "p > n" : ((p == 0) ? "p = 0" : "p < 0");
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
        return IntStream.rangeClosed(1, n)
                .mapToObj(Arrays::asList)
                .collect(Collectors.toList());
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
            List<List<Integer>> indexes = createIndex(this.n, p);
            return runTask(indexes, p);
        }

        /**
         * Returns list of list the all combination corresponding to initial start in pos
         *
         * @param initial the initial value
         * @param pos     the generation start position
         * @return list of list the all permutation corresponding to initial start in position pos
         */
        private List<List<Integer>> generateIndex(final List<Integer> initial, int pos) {
            if (pos == 0) {
                return Arrays.asList(initial);
            } else if (pos == 1) {
                return circularGeneration(initial, 1);
            } else {
                return this.currentGeneration(generateIndex(initial, pos - 1), pos--);
            }
        }

        private List<List<Integer>> currentGeneration(List<List<Integer>> indexes, int pos) {
            return indexes.stream()
                    .map(integers -> circularGeneration(integers, pos))
                    .reduce(new ArrayList<>(), (lists, lists2) -> {
                        lists.addAll(lists2);
                        return lists;
                    });
        }

        @SuppressWarnings("UnusedDeclaration")
        private List<List<Integer>> runTask(List<List<Integer>> indexes, int position) {
            LOGGER.info("Index generation start");
            final PermutationRecursiveTask task = new PermutationRecursiveTask(indexes, position,
                    indexes.size() / LazyPool.parallelism);
            return pool().get().invoke(task);
        }

        /**
         * Recursive version
         * Returns list of list the all combination corresponding to initial start in pos
         *
         * @param initial the initial value
         * @param pos     the generation start position
         * @return list of list the all permutation corresponding to initial start in position pod
         */
        @SuppressWarnings("UnusedDeclaration")
        private List<List<Integer>> generateIndexRecursive(final List<Integer> initial, int pos) {
            Objects.requireNonNull(initial);
            if (pos == 0) {
                return Arrays.asList(initial);
            } else if (pos == 1) {
                return circularGeneration(initial, pos);
            } else {
                return collect(generateIndexRecursive(initial, pos - 1), pos);
            }
        }

        private List<List<Integer>> collect(final Stream<List<List<Integer>>> stream) {
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
                this.permute(array, pos);
            } while (!array.equals(initial));
            return arrays;
        }

        private void permute(final List<Integer> array, int pos) {
            int first = array.get(pos - 1);
            for (int i = pos - 1; i < array.size() - 1; i++) {
                array.set(i, array.get(i + 1));
            }
            array.set(array.size() - 1, first);
        }

        @SuppressWarnings("UnusedDeclaration")
        private class PermutationRecursiveTask extends RecursiveTask<List<List<Integer>>> {

            private final List<List<Integer>> indexes;
            private int position;
            private int limit;

            PermutationRecursiveTask(List<List<Integer>> indexes, int position, int limit) {
                this.indexes = Objects.requireNonNull(indexes);
                this.position = position;
                this.limit = limit;
                LOGGER.info(Thread.currentThread() + " " + indexes.size());
            }

            @Override
            protected List<List<Integer>> compute() {
                int size = indexes.size();
                if (this.limit > 0 && size > this.limit) {
                    return ForkJoinTask
                            .invokeAll(createPermutationTasks(size))
                            .stream()
                            .map(ForkJoinTask::join)
                            .flatMap(List::stream)
                            .collect(Collectors.toList());
                } else {
                    return collect(this.indexes
                            .stream()
                            .map(array -> generateIndex(array, this.position >= 1 ? (this.position - 1) : 0)));
                }
            }

            private List<PermutationRecursiveTask> createPermutationTasks(int size) {
                final List<PermutationRecursiveTask> tasks = new ArrayList<>();
                int mod = size % this.limit;
                int rs = size / this.limit;
                IntStream
                        .range(0, rs)
                        .filter(i -> i < rs)
                        .forEach(
                                i -> tasks.add(new PermutationRecursiveTask(this.indexes.subList((this.limit * i),
                                        (this.limit) * (i + 1)), this.position, this.limit))
                        );
                if (mod != 0) {
                    tasks.add(new PermutationRecursiveTask(this.indexes.subList(size - mod, size),
                            this.position, this.limit));
                }
                return tasks;
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
            final String separatorInString = separator == ' ' ? "" : String.valueOf(separator);
            return lIndex
                    .stream()
                    .map(integer -> tArray.get((integer - 1)).toString())
                    .collect(Collectors.toList())
                    .stream()
                    .reduce(IDENTITY, (a, b) -> (a + (!a.equals(IDENTITY) ? separatorInString : IDENTITY) + b));
        }

        /**
         * @param listIndex index of elements to generate
         * @return a list of element list, each list contains
         * a set of objects, which are in fact the desired generation.
         */
        private List<List<T>> generate(final List<List<Integer>> listIndex) {
            LOGGER.info("Values generation start");
            final GeneratorRecursiveTask<T> task = new GeneratorRecursiveTask<>(this.tArray, listIndex, 1000);
            return pool().get().invoke(task);
        }

        /**
         * Inner Class GeneratorRecursiveTask
         *
         * @param <T>
         */
        private static class GeneratorRecursiveTask<T> extends RecursiveTask<List<List<T>>> {

            private final List<List<Integer>> lists;
            private final List<T> tArray;
            private int limit;

            GeneratorRecursiveTask(final List<T> tArray, final List<List<Integer>> lists, int limit) {
                this.tArray = Objects.requireNonNull(tArray);
                this.lists = Objects.requireNonNull(lists);
                this.limit = limit;
            }

            @Override
            protected List<List<T>> compute() {
                int size = this.lists.size();
                if (this.limit > 0 && size > this.limit) {
                    return ForkJoinTask
                            .invokeAll(createGeneratorTask(size))
                            .stream()
                            .map(ForkJoinTask::join)
                            .flatMap(List::stream)
                            .collect(Collectors.toList());
                } else {
                    return getAllIndexValues(this.lists);
                }
            }

            private List<GeneratorRecursiveTask<T>> createGeneratorTask(int size) {
                List<GeneratorRecursiveTask<T>> tasks = new ArrayList<>();
                int mod = size % this.limit;
                int rs = size / this.limit;
                IntStream
                        .range(0, rs)
                        .forEach(
                                i -> tasks.add(new GeneratorRecursiveTask<>(this.tArray,
                                        this.lists.subList((this.limit * i), (this.limit) * (i + 1)), this.limit))
                        );
                if (mod != 0) {
                    tasks.add(new GeneratorRecursiveTask<>(this.tArray, this.lists.subList(size - mod, size),
                            this.limit));
                }
                return tasks;
            }

            private List<List<T>> getAllIndexValues(final List<List<Integer>> lists) {
                return lists.stream()
                        .map(integers -> getIndexValues(integers).collect(Collectors.toList()))
                        .collect(Collectors.toList());
            }

            private Stream<T> getIndexValues(final List<Integer> integers) {
                return integers.stream().map(integer -> this.tArray.get(integer - 1));
            }
        }
    }
}

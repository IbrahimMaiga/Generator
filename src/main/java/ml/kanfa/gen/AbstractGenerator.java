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
public abstract class AbstractGenerator<T> implements Generator<T> {

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
    public List<String> generateToWord(int p, char separator){
        final List<String> lWords = new ArrayList<>();
        final List<List<Integer>> lIndex = generateIndex(p);
        lWords.addAll(lIndex.stream().map(integers -> toWord(integers, separator)).collect(Collectors.toList()));
        return lWords;
    }

    @Override
    @SuppressWarnings("UnusedDeclaration")
    public List<List<T>> generate(int p){
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
        final GeneratorRecursiveTask task = new GeneratorRecursiveTask(this.tArray, listIndex);
        return pool.invoke(task);
    }

    private class GeneratorRecursiveTask extends RecursiveTask<List<List<T>>> {

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
                    subTasks.add(new GeneratorRecursiveTask(this.tArray, l).fork());
                }
                if (mod != 0)
                    subTasks.add(new GeneratorRecursiveTask(this.tArray, this.lists.subList(size - mod, size)).fork());
            } else {
                arrays = getAllIndexValues(this.lists);
            }

            for (ForkJoinTask<List<List<T>>> subTask : subTasks){
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
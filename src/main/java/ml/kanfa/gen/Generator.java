package ml.kanfa.gen;

import java.util.List;

/**
 * @author Ibrahim Maïga.
 */
public interface Generator<T> {
    /**
     *
     * @param p
     * @return
     */
    @SuppressWarnings("UnusedDeclaration")
    List<List<T>> generate(int p);

    /**
     *
     * @param p
     * @param separator
     * @return
     */
    @SuppressWarnings("UnusedDeclaration")
    List<String> generateToWord(int p, char separator);

    /**
     *
     * @param p
     * @return
     */
    @SuppressWarnings("UnusedDeclaration")
    default List<String> generateToWord(int p) {
        return generateToWord(p, ' ');
    }
}

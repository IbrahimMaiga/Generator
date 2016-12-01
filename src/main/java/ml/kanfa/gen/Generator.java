package ml.kanfa.gen;

import java.util.List;

/**
 * @author Ibrahim Maïga.
 */
public interface Generator<T> {
    @SuppressWarnings("UnusedDeclaration")
    List<List<T>> generate(int p) throws Exception;

    @SuppressWarnings("UnusedDeclaration")
    List<String> generateToWord(int p, char separator) throws Exception;

    @SuppressWarnings("UnusedDeclaration")
    default List<String> generateToWord(int p) throws Exception {
        return generateToWord(p, ' ');
    }
}

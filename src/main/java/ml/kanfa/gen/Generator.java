package ml.kanfa.gen;

import java.util.List;

/**
 * @author Ibrahim Maïga.
 */
public interface Generator<T> {
    @SuppressWarnings("UnusedDeclaration")
    List<List<T>> generate(int p);

    @SuppressWarnings("UnusedDeclaration")
    List<String> generateToWord(int p, char separator);

    @SuppressWarnings("UnusedDeclaration")
    default List<String> generateToWord(int p){
        return generateToWord(p, ' ');
    }
}

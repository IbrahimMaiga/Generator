package ml.kanfa.gen;

import java.util.List;

/**
 * Interface Generator
 * Contains the set of methods specific to a generation
 *
 * @author Ibrahim Maïga.
 */
public interface Generator<T> {

    /**
     * @param p generation length
     * @return a list of element list, each list contains
     * a set of objects, which are in fact the desired generation.
     */
    @SuppressWarnings("UnusedDeclaration")
    List<List<T>> generate(int p);

    /**
     * @param p generation length
     * @param separator values separator
     * @return string representation of list values
     */
    @SuppressWarnings("UnusedDeclaration")
    List<String> generateToWord(int p, char separator);

    /**
     * @param p generation lengths
     * @return string representation of list values without separator
     */
    @SuppressWarnings("UnusedDeclaration")
    default List<String> generateToWord(int p) {
        return generateToWord(p, ' ');
    }
}
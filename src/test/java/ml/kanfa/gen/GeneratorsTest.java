package ml.kanfa.gen;

import junit.framework.TestCase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Class GeneratorsTest.
 * Contains all {@link Generators} tests.
 *
 * @author Ibrahim Maïga <maiga.ibrm@gmail.com>.
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class GeneratorsTest extends TestCase {

    private Generator<String> combination = Generators.newCombination("A", "B", "C");
    private Generator<String> permutation = Generators.newPermutation("A", "B");
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void generateToWordPermutation() {
        List<String> words = Arrays.asList("AB", "BA");
        assertEquals(words, this.permutation.generateToWord(2));
    }

    @Test
    public void generatePermutation() {
        List<List<String>> generateList = getLists();
        assertEquals(generateList, getPermutation(3));
    }

    @Test
    public void generatePermutationWithLengthEqualsOne() {
        final List<String> var1 = Collections.singletonList("A");
        final List<String> var2 = Collections.singletonList("B");
        final List<String> var3 = Collections.singletonList("C");
        final List<List<String>> generateList = Arrays.asList(var1, var2, var3);
        assertEquals(generateList, getPermutation(1));
    }

    @Test
    public void generateToWordCombination() {
        List<String> words = Arrays.asList("AB", "AC", "BC");
        assertEquals(words, this.combination.generateToWord(2));
    }

    @Test
    public void generateCombination() {
        List<String> var1 = Arrays.asList("A", "B");
        List<String> var2 = Arrays.asList("A", "C");
        List<String> var3 = Arrays.asList("B", "C");
        List<List<String>> generateList = Arrays.asList(var1, var2, var3);
        assertEquals(generateList, this.combination.generate(2));
    }

    @Test
    public void noReversePatternInCombination() {
        List<String> words = this.combination.generateToWord(2);
        String reverseWord = reverse(words.get(0));
        assertFalse(words.contains(reverseWord));
    }

    @Test
    public void greaterValueException() {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("p > n");
        this.permutation.generate(10);
    }

    @Test
    public void negativeValueException() {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("p < 0");
        this.permutation.generate(-10);
    }

    @Test
    public void zeroAsParamException() {
        this.exception.expect(Exception.class);
        this.exception.expectMessage("p = 0");
        this.permutation.generate(0);
    }

    @Test
    public void reversePatternInPermutation() {
        List<String> words = this.permutation.generateToWord(2);
        String reverseWord = reverse(words.get(0));
        assertTrue(words.contains(reverseWord));
    }

    @Test
    public void permutationLength() {
        Generator<String> permutation = Generators.newPermutation("A", "B", "C", "D", "E", "F");
        List<List<String>> words = permutation.generate(4);
        assertEquals(words.size(), this.getPermutationLength(6, 4));
    }

    @Test
    public void combinationLength() {
        List<String> words = this.combination.generateToWord(2);
        assertEquals(words.size(), this.getCombinationLength(3, 2));
    }

    private List<List<String>> getPermutation(int p) {
        Generator<String> per = Generators.newPermutation("A", "B", "C");
        return per.generate(p);
    }

    private List<List<String>> getLists() {
        final List<String> var1 = Arrays.asList("A", "B", "C");
        final List<String> var2 = Arrays.asList("A", "C", "B");
        final List<String> var3 = Arrays.asList("B", "C", "A");
        final List<String> var4 = Arrays.asList("B", "A", "C");
        final List<String> var5 = Arrays.asList("C", "A", "B");
        final List<String> var6 = Arrays.asList("C", "B", "A");
        return Arrays.asList(var1, var2, var3, var4, var5, var6);
    }

    private int getPermutationLength(int n, int p) {
        return factorial(n) / factorial((n - p));
    }


    private int getCombinationLength(int n, int p) {
        return getPermutationLength(n, p) / factorial(p);
    }

    /**
     * Returns n factorial
     * @param n {@code int} method parameter
     * @return {@code int} n factorial
     */
    private int factorial(int n) {
        return IntStream.range(1, n + 1).reduce(1, (a, b) -> a * b);
    }

    /**
     * Returns reversed {@code String}
     * @param str String to reverse
     * @return {@code String} reversed string
     */
    private String reverse(String str) {
        final char[] chars = str.toCharArray();
        int length = chars.length;
        char[] reversedChars = new char[length];
        for (int i = length - 1; i >= 0; i--) {
            reversedChars[length - (i + 1)] = chars[i];
        }
        return new String(reversedChars);
    }

    /**
     * Other way to make reverse method
     * @param str {@code String} to reverse
     * @return {@code String} reversed string
     */
    @SuppressWarnings("UnusedDeclaration")
    private String reverseString(String str) {
        return new StringBuilder(str).reverse().toString();
    }
}
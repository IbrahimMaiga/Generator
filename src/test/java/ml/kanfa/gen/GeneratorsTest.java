package ml.kanfa.gen;

import junit.framework.TestCase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class GeneratorsTest.
 * Contains all {@link Generators} tests.
 *
 * @author Ibrahim Maïga.
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class GeneratorsTest extends TestCase {

    private Generator combination = Generators.newCombination("A", "B", "C");
    private Generator permutation = Generators.newPermutation("A", "B");
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void generateToWordPermutation() throws Exception {
        List<String> words = new ArrayList<>();
        words.add("AB");
        words.add("BA");
        assertEquals(words, this.permutation.generateToWord(2));
    }

    @Test
    public void generatePermutation() throws Exception {
        List<List<String>> generateList = getLists();
        assertEquals(generateList, getPermutation(3));
    }

    @Test
    public void generatePermutationWithLengthEqualsOne() throws Exception {
        List<String> var1 = Arrays.asList("A");
        List<String> var2 = Arrays.asList("B");
        List<String> var3 = Arrays.asList("C");
        List<List<String>> generateList = Arrays.asList(var1, var2, var3);
        assertEquals(generateList, getPermutation(1));
    }

    private List<List<String>> getPermutation(int p) throws Exception {
        Generator<String> per = Generators.newPermutation("A", "B", "C");
        return per.generate(p);
    }

    private List<List<String>> getLists() {
        List<String> var1 = Arrays.asList("A", "B", "C");
        List<String> var2 = Arrays.asList("A", "C", "B");
        List<String> var3 = Arrays.asList("B", "C", "A");
        List<String> var4 = Arrays.asList("B", "A", "C");
        List<String> var5 = Arrays.asList("C", "A", "B");
        List<String> var6 = Arrays.asList("C", "B", "A");
        return Arrays.asList(var1, var2, var3, var4, var5, var6);
    }

    @Test
    public void generateToWordCombination() throws Exception {
        List<String> words = new ArrayList<>();
        words.add("AB");
        words.add("AC");
        words.add("BC");
        assertEquals(words, this.combination.generateToWord(2));
    }

    @Test
    public void generateCombination() throws Exception {
        List<List<String>> generateList = new ArrayList<>();
        List<String> var1 = new ArrayList<>();
        var1.add("A");
        var1.add("B");
        List<String> var2 = new ArrayList<>();
        var2.add("A");
        var2.add("C");
        List<String> var3 = new ArrayList<>();
        var3.add("B");
        var3.add("C");
        generateList.add(var1);
        generateList.add(var2);
        generateList.add(var3);
        assertEquals(generateList, this.combination.generate(2));
    }

    @Test
    public void noReversePatternInCombination() throws Exception {
        List<String> words = this.combination.generateToWord(2);
        String reverse_word = reverse(words.get(0));
        assertFalse(words.contains(reverse_word));
    }

    @Test
    public void greatherValueException() throws Exception {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("p > n");
        this.permutation.generate(10);
    }

    @Test
    public void negativeValueException() throws Exception {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("p < 0");
        this.permutation.generate(-10);
    }

    @Test
    public void zeroAsParamException() throws Exception {
        this.exception.expect(Exception.class);
        this.exception.expectMessage("p = 0");
        this.permutation.generate(0);
    }

    @Test
    public void reversePatternInPermutation() throws Exception {
        List<String> words = this.permutation.generateToWord(2);
        String reverse_word = reverse(words.get(0));
        assertTrue(words.contains(reverse_word));
    }

    @Test
    public void permutationLength() throws Exception {
        Generator<String> permutation = Generators.newPermutation("A", "B", "C", "D", "E", "F", "G", "H");
        List<List<String>> words = permutation.generate(7);
        assertEquals(words.size(), this.getPermutationLength(8, 7));
    }

    @Test
    public void combinationLength() throws Exception {
        List<String> words = this.combination.generateToWord(2);
        assertEquals(words.size(), this.getCombinationLength(3, 2));
    }

    private int getPermutationLength(int n, int p) {
        return factorial(n) / factorial((n - p));
    }

    private int getCombinationLength(int n, int p) {
        return getPermutationLength(n, p) / factorial(p);
    }

    private int factorial(int n) {
        if (n == 0 || n == 1) return 1;
        else return n * factorial(n - 1);
    }

    private String reverse(String str) {
        final char[] chars = str.toCharArray();
        int length = chars.length;
        char[] reverse_chars = new char[length];
        for (int i = length - 1; i >= 0; i--) {
            reverse_chars[length - (i + 1)] = chars[i];
        }
        return new String(reverse_chars);
    }
}
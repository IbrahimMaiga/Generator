# Generator
Generator is a module which generates combinations and permutations

# How to use

It's very easy !!
Generator instances are created that are either Combinations or Permutations using statics methods in Generators class, newCombination or newPermutation as follows:
```sh
Genetator<Integer> combination = Generators.newCombination(1, 2, 3);
final List<List<Integer>> listOfCombinationGenerateToList = combination.generate(2);
```
The above code creates combinations of 2 elements from the set {1, 2, 3}, and returns the result as a list.

```sh
Genetator<String> permutation = Generators.newPermutation("A", "B", "C", "A");
final List<String> listOfPermutationGenerateToWord = combination.generateToWord(3);
```
The above code creates permutations of 3 elements from the set {"A", "B", "C", "D"}, and returns the result as a list of String.

You can use generateToWord by specifying a letter separator as follows.
```sh
final List<String> listOfPermutationGenerateToWord = combination.generateToWord(3, '_');
```
The separator is of type char, for a list L = [A, B, C], we will have the following output A_B_C.


License
----

MIT
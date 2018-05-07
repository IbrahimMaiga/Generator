package ml.kanfa.gen;


import java.util.List;

/**
 * @author Ibrahim Maïga <maiga.ibrm@gmail.com>
 */
public interface Main {

    static void main(String ...args) {
        Generator<String> permutation = Generators.newPermutation("A,B,G,D,T,X,K,E,R,1,2,3".split(","));
        Generator<String> combination = Generators.newCombination("A,B,G,D,T,X,K,E,R,1,2,3".split(","));
        List<List<String>> permutationList = permutation.generate(5);
        List<List<String>> combinationList = combination.generate(5);
        System.out.println("List of combination");
        System.out.println(combinationList);
        System.out.println("Permutation size");
        System.out.println(permutationList.size());
    }
}
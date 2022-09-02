import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.base.Predicate;


public class CreateLTLConstraints {
    private static Predicate<List<String>> predicate = new Predicate<List<String>>() {
        @Override
        public boolean apply(List<String> input) {
            return !input.get(0).equals(input.get(1));
        }
        };

    public static void main(String[] args) throws Exception {
        Set<String> lines = new HashSet();
        Scanner scanner =  new Scanner(new File(args[0]));
        while(scanner.hasNextLine()) {
            lines.add(scanner.nextLine());
        }
        Set<List<String>> product = Sets.cartesianProduct(ImmutableList.of(ImmutableSet.copyOf(lines),ImmutableSet.copyOf(lines)));
        Set<List<String>> filtered = Sets.filter(product, predicate);
        // System.out.println(product);
        // System.out.println(filtered);
        for(List<String> pair : filtered) {
            printConstraints(pair);
        }
        // System.out.println("All (" + filtered.size() + " x 6) constraints have been generated.");
    }

    public static void printConstraints(List<String> pair) {
        // if either of the pairs is EPSILON, we shouldn't make constraints over it because they aren't actual transitions
        if (pair.get(0).equals("EPSILON") || pair.get(1).equals("EPSILON")) {
            return;
        }
        String pairName = pair.get(0) + "_" + pair.get(1);
        /* Constraint 1: a is always followed by b */
        System.out.println("ltl " + pairName + "_1 { [] ((event == " + pair.get(0) + ") -> X <> (event == " + pair.get(1) + ")) }");
        /* Constraint 2: a is never followed by b */
        System.out.println("ltl " + pairName + "_2 { [] ((event == " + pair.get(0) + ") -> X [] (event != " + pair.get(1) + ")) }");
        /* Constraint 3: a is always preceded by b */
        System.out.println("ltl " + pairName + "_3 { ((event != " + pair.get(0) + ") U (event == " + pair.get(1) + ")) || [](event != " + pair.get(0) + ") }");
        /* BROKEN!!! Constraint 4: a is always immediately followed by b */
        System.out.println("ltl " + pairName + "_4 { [] ((event == " + pair.get(0) + ") -> X (event == " + pair.get(1) + ")) }");
        /* Constraint 5: a is never immediately followed by b */
        System.out.println("ltl " + pairName + "_5 { [] ((event == " + pair.get(0) + ") -> X (event != " + pair.get(1) + ")) }");
        /* Constraint 6: a is always immediately preceded by b */
        System.out.println("ltl " + pairName + "_6 { <>(event == " + pair.get(0) + ") -> ((event != " + pair.get(0) + ") U ((event == " + pair.get(1) + ") && X(event == " + pair.get(0) + "))) }");
    }
}

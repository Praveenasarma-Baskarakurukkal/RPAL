
import java.util.ArrayList;

public class myrpal {
    String error;

    public static void main(String[] args) {

        String content = "";
        String fileName;

        try {
            if (args.length == 0) {
                String error = "You should give the AST file name as an argument...";
                throw new Exception(error);
            } else {

                fileName = args[0];
                Parser p = new Parser(fileName);
                content = p.startParsing();

                // Check if the '-ast' switch is present
                boolean printAST = false;
                for (String arg : args) {
                    if (arg.equals("-ast")) {
                        printAST = true;
                        break;
                    }
                }

                if (printAST) {
                    // Print AST tree if '-ast' switch is present
                    System.out.println("Abstract Syntax Tree (AST):");
                    System.out.println(content);
                } else {
                    // Print only the output without the AST tree
                    Vertex root = CreateTree.nodeFromFile(content);
                    ASTtoST.astToSt(root);
                    ArrayList<Stack<EleValue>> controls = ElementParser.generateCS(root);
                    CSEMachine cseMachine = new CSEMachine(controls);
                    System.out.println("Output of the above program is:");
                    cseMachine.evaluateTree();
                }
            }

        } catch (ExceptionHandlerOfAST exception) {
            System.out.println("Error occurred while standardizing AST:");
            System.out.println(exception.getMessage());
        } catch (ExceptionHandlerOfCSE exception) {
            System.out.println("Error occurred while evaluating CSE:");
            System.out.println(exception.getMessage());
        } catch (RuntimeException exception) {
            System.out.println("Runtime Exception:");
            System.out.println(exception.getMessage());
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }

}


import java.util.ArrayList;

/**
 * CSE machine to evaluate the traversed tree
 */

public class CSEMachine {
    private final Stack<EleValue> eleValues;
    private final Stack<EleValueOrTuple> eleValueOrTuples;
    private final OperationHandler operationHandler;
    private final ArrayList<Environment> environments;
    private final ArrayList<Stack<EleValue>> CS;

    public CSEMachine(ArrayList<Stack<EleValue>> controlStructures) {
        this.CS = controlStructures;
        this.eleValueOrTuples = new Stack<>();
        this.operationHandler = new OperationHandler();

        eleValues = new Stack<>();
        eleValues.push(new EleValue("environment", "0"));
        eleValues.push(new EleValue("delta", "0"));
        eleValueOrTuples.push(new EleValue("environment", "0"));
        environments = new ArrayList<>();
        environments.add(new Environment());
    }

    @Override
    public String toString() {
        return eleValues + "\n" + eleValueOrTuples + "\n" + currentEnvironment() + "\n";
    }

    /**
     * Get current environment index by peeking the stack and fining the closest
     * environment element.
     * 
     */
    private int currentEnvironmentIndex() {
        int closestEnvironment = 0;
        for (EleValueOrTuple element : eleValues) {
            if (element instanceof EleValue && element.isLabel("environment")) {
                String closestEnvironmentStr = ((EleValue) element).getValue();
                closestEnvironment = Integer.parseInt(closestEnvironmentStr);
            }
        }
        return closestEnvironment;
    }

    /**
     * Get current environment
     */
    private Environment currentEnvironment() {
        return environments.get(currentEnvironmentIndex());
    }

    /**
     * Start processing the control stack to evaluate result.
     */
    public void evaluateTree() {
        while (!eleValues.isEmpty()) {
            EleValue currentElement = eleValues.pop();

            if (currentElement.isLabel("gamma")) {
                EleValueOrTuple firstElem = eleValueOrTuples.pop();
                EleValueOrTuple secondElem = eleValueOrTuples.pop();
                if (firstElem.isLabel("yStar")) {
                    Rule12(secondElem);
                } else if (firstElem.isLabel("eta")) {
                    eleValueOrTuples.push(secondElem);
                    Rule13(currentElement, firstElem);
                } else if (firstElem.isLabel("lambda")) {
                    EleValue firstValue = (EleValue) firstElem;
                    if (firstValue.getValue().contains(",")) {
                        Rule11(firstElem, secondElem);
                    } else {
                        Rule4(firstElem, secondElem);
                    }
                } else if (firstElem.isLabel("tau")) {
                    Rule10(firstElem, secondElem);
                } else {
                    Rule3(firstElem, secondElem);
                }
            } else if (currentElement.isLabel("delta")) {
                int controlIndex = Integer.parseInt(currentElement.getValue());
                extractDelta(controlIndex);
            } else if (currentElement.isLabel("id")) {
                Rule1(currentElement);
            } else if (currentElement.isLabel("lambda")) {
                Rule2(currentElement);
            } else if (currentElement.isLabel("environment")) {
                Rule5(currentElement);
            } else if (currentElement.isLabel("beta")) {
                Rule8();
            } else if (currentElement.isLabel("tau")) {
                Rule9(currentElement);
            } else if (!Rule6_7(currentElement)) {
                eleValueOrTuples.push(currentElement);
            }
        }
    }

    /**
     * Extract elements from the control structure specified.
     */
    private void extractDelta(int controlIndex) {
        Stack<EleValue> control = CS.get(controlIndex);
        for (EleValue controlElem : control) {
            this.eleValues.push(controlElem);
        }
    }

    /**
     * rule 1
     */
    private void Rule1(EleValue name) {
        String id = name.getValue();
        EleValueOrTuple value = currentEnvironment().lookup(id);
        if (value == null) {
            value = new EleValue(id);
        }
        eleValueOrTuples.push(value);
    }

    /**
     * rule 2
     */
    private void Rule2(EleValue lambda) {
        String[] kAndX = lambda.getValue().split(" ");
        String c = Integer.toString(currentEnvironmentIndex());
        String[] newValues = { kAndX[0], kAndX[1], c };
        EleValueOrTuple newLambda = new EleValue("lambda", String.join(" ", newValues));
        eleValueOrTuples.push(newLambda);
    }

    /**
     * rule13
     */
    private void Rule3(EleValueOrTuple rator, EleValueOrTuple rand) {
        EleValueOrTuple result = operationHandler.apply(rator, rand);
        eleValueOrTuples.push(result);
    }

    /**
     * rule4
     */
    private void Rule4(EleValueOrTuple lambda, EleValueOrTuple rand) {
        if (lambda instanceof EleValue && lambda.isLabel("lambda")) {
            String[] kAndXAndC = ((EleValue) lambda).getValue().split(" ");
            String k = kAndXAndC[0];
            String x = kAndXAndC[1];
            String c = kAndXAndC[2];
            Environment envC = environments.get(Integer.parseInt(c));

            Environment newEnvironment = new Environment(envC, x, rand);
            String newEnvIndex = Integer.toString(environments.size());
            environments.add(newEnvironment);
            eleValues.push(new EleValue("environment", newEnvIndex));
            eleValues.push(new EleValue("delta", k));
            eleValueOrTuples.push(new EleValue("environment", newEnvIndex));
            return;
        }
        throw new ExceptionHandlerOfCSE("Expected lambda element but found: " + lambda);
    }

    /**
     * rule 5
     */
    private void Rule5(EleValue env) {
        EleValueOrTuple value = eleValueOrTuples.pop();
        EleValueOrTuple envS = eleValueOrTuples.pop();
        if (envS instanceof EleValue && envS.isLabel("environment")) {
            if (env.equals(envS)) {
                eleValueOrTuples.push(value);
                return;
            }
            throw new ExceptionHandlerOfCSE(String.format("Environment element mismatch: %s and %s", env, envS));
        }
        throw new ExceptionHandlerOfCSE("Expected environment element but found: " + envS);
    }

    /**
     * rule7
     */
    private boolean Rule6_7(EleValue element) {
        if (operationHandler.checkMathematicalOperation(element)) {
            EleValueOrTuple rator = eleValueOrTuples.pop();
            EleValueOrTuple rand = eleValueOrTuples.pop();
            EleValueOrTuple result = operationHandler.applyOperations(element, rator, rand);
            eleValueOrTuples.push(result);
        } else if (operationHandler.checkArrayOperation(element)) {
            EleValueOrTuple rand = eleValueOrTuples.pop();
            EleValueOrTuple result = operationHandler.apply(element, rand);
            eleValueOrTuples.push(result);
        } else {
            return false;
        }
        return true;
    }

    /**
     * rule8
     */
    private void Rule8() {
        EleValue deltaElse = eleValues.pop();
        EleValue deltaThen = eleValues.pop();
        EleValueOrTuple condition = eleValueOrTuples.pop();

        if (deltaElse.isLabel("delta") && deltaThen.isLabel("delta")) {
            if (condition.isLabel("true")) {
                eleValues.push(deltaThen);
                return;
            } else if (condition.isLabel("false")) {
                eleValues.push(deltaElse);
                return;
            }
            throw new RuntimeException("If condition must evaluate to a truth value.");
        }
        throw new ExceptionHandlerOfCSE("Expected delta elements.");
    }

    /**
     * rule 9
     */
    private void Rule9(EleValue tau) {
        int elements = Integer.parseInt(tau.getValue());
        EleValueOrTuple[] tupleElements = new EleValueOrTuple[elements];
        for (int i = 0; i < elements; i++) {
            tupleElements[i] = eleValueOrTuples.pop();
        }
        EleValueOrTuple tuple = new EleTuple(tupleElements);
        eleValueOrTuples.push(tuple);
    }

    /**
     * rule10
     */
    private void Rule10(EleValueOrTuple tuple, EleValueOrTuple index) {
        if (tuple instanceof EleTuple) {
            if (index instanceof EleValue && index.isLabel("int")) {
                int ind = Integer.parseInt(((EleValue) index).getValue());
                EleValueOrTuple value = ((EleTuple) tuple).getValue()[ind];
                eleValueOrTuples.push(value);
                return;
            }
            throw new ExceptionHandlerOfCSE("Expected integer index but found: " + index);
        }
        throw new ExceptionHandlerOfCSE("Expected tuple but found: " + tuple);
    }

    /**
     * rule 11
     */
    private void Rule11(EleValueOrTuple lambda, EleValueOrTuple rand) {
        if (lambda instanceof EleValue && lambda.isLabel("lambda")) {
            if (rand instanceof EleTuple) {
                String[] kAndVAndC = ((EleValue) lambda).getValue().split(" ");
                String k = kAndVAndC[0];
                String[] v = kAndVAndC[1].split(",");
                String c = kAndVAndC[2];
                Environment envC = environments.get(Integer.parseInt(c));

                Environment newEnvironment = new Environment(envC);
                for (int i = 0; i < v.length; i++) {
                    newEnvironment.remember(v[i], ((EleTuple) rand).getValue()[i]);
                }
                String newEnvIndex = Integer.toString(environments.size());
                environments.add(newEnvironment);
                eleValues.push(new EleValue("environment", newEnvIndex));
                eleValues.push(new EleValue("delta", k));
                eleValueOrTuples.push(new EleValue("environment", newEnvIndex));
                return;
            }
            throw new ExceptionHandlerOfCSE("Expected tuple but found: " + rand);
        }
        throw new ExceptionHandlerOfCSE("Expected lambda element but found: " + lambda);
    }

    /**
     * rule12
     */
    private void Rule12(EleValueOrTuple lambda) {
        if (lambda instanceof EleValue && lambda.isLabel("lambda")) {
            String iAndVAndC = ((EleValue) lambda).getValue();
            EleValueOrTuple etaElement = new EleValue("eta", iAndVAndC);
            eleValueOrTuples.push(etaElement);
            return;
        }
        throw new ExceptionHandlerOfCSE("Expected lambda element but found: " + lambda);
    }

    /**
     * rule 13
     */
    private void Rule13(EleValue gamma, EleValueOrTuple eta) {
        if (eta instanceof EleValue && eta.isLabel("eta")) {
            String iAndVAndC = ((EleValue) eta).getValue();
            EleValue lambda = new EleValue("lambda", iAndVAndC);
            EleValue newGamma = new EleValue("gamma");

            eleValueOrTuples.push(eta);
            eleValueOrTuples.push(lambda);

            eleValues.push(gamma);
            eleValues.push(newGamma);
            return;
        }
        throw new ExceptionHandlerOfCSE("Expected eta element but found: " + eta);
    }
}

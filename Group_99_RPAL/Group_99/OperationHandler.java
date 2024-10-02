
interface operationHandlerBoolean {
    boolean operation(boolean value1, boolean value2);
}

interface operationHandlerString {
    String operation(String value);
}

interface operationHandlerNumerical {
    int operation(int value1, int value2);
}

/**
 * Applies functions and operators.
 */
public class OperationHandler {

    /**
     * @return ith element in the tuple(1 indexed); operation[operand]
     */
    private EleValueOrTuple extract(EleTuple operation, EleValueOrTuple operand) {
        if (operand instanceof EleValue && operand.isLabel("int")) {
            int index = Integer.parseInt(((EleValue) operand).getValue());
            return operation.getValue()[index - 1];
        }
        throw new RuntimeException("Index of the tuple must be integer.");
    }

    /**
     * Checks if binary operation is applicable
     */
    public boolean checkMathematicalOperation(EleValueOrTuple op) {
        String[] operationsMathematical = {
                "+",
                "-",
                "/",
                "*",
                "**",
                "eq",
                "ne",
                "gr",
                "ge",
                "le",
                ">",
                "<",
                ">=",
                "<=",
                "or",
                "&",
                "aug",
                "ls" };
        for (String binaryOp : operationsMathematical) {
            if (op.isLabel(binaryOp))
                return true;
        }
        return false;
    }

    /**
     * Checks if un-ary operation is applicable
     */
    public boolean checkArrayOperation(EleValueOrTuple op) {
        String[] operationsArray = {
                "Print",
                "Isstring",
                "Isinteger",
                "Istruthvalue",
                "Isfunction",
                "Null",
                "Istuple",
                "Order",
                "Stern",
                "Stem",
                "ItoS",
                "neg",
                "not",
                "$ConcPartial" };

        for (String unaryOp : operationsArray) {
            if (op.isLabel(unaryOp))
                return true;
        }
        return false;
    }

    /**
     * Applies binary operators. VAL1 OP VAL2
     */
    public EleValueOrTuple applyOperations(EleValueOrTuple operation, EleValueOrTuple operand1,
            EleValueOrTuple operand2) {
        switch (operation.getLabel()) {
            case "+":
                return numericalOperator(operand1, operand2, Integer::sum);
            case "-":
                return numericalOperator(operand1, operand2, (a, b) -> a - b);
            case "*":
                return numericalOperator(operand1, operand2, (a, b) -> a * b);
            case "**":
                return numericalOperator(operand1, operand2, (a, b) -> (int) Math.pow(a, b));
            case "/":
                return numericalOperator(operand1, operand2, (a, b) -> a / b);
            case "or":
                return binaryBooleanOperator(operand1, operand2, (a, b) -> a || b);
            case "&":
                return binaryBooleanOperator(operand1, operand2, (a, b) -> a && b);
            case "eq":
                return booleanCondition(operand1.equals(operand2));
            case "ne":
                return not(booleanCondition(operand1.equals(operand2)));
            case ">":
            case "gr":
                return greater(operand1, operand2);
            case "<":
            case "ls":
                return not(greater(operand1, operand2));
            case ">=":
            case "ge":
                return or(greater(operand1, operand2), booleanCondition(operand1.equals(operand2)));
            case "<=":
            case "le":
                return or(not(greater(operand1, operand2)), booleanCondition(operand1.equals(operand2)));
            case "aug":
                return aug(operand1, operand2);
            default:
                throw new ExceptionHandlerOfCSE("Unknown operator: " + operation);
        }
    }

    /**
     * Applies un-ary functions and operators. OP VAL
     */
    public EleValueOrTuple apply(EleValueOrTuple operation, EleValueOrTuple operand) {
        switch (operation.getLabel()) {
            case "Print":
                System.out.println(covertToString(operand));
                return new EleValue("dummy");
            case "Isstring":
                return booleanCondition(operand.isLabel("str"));
            case "Isinteger":
                return booleanCondition(operand.isLabel("int"));
            case "Istruthvalue":
                return booleanCondition(operand.isLabel("true") || operand.isLabel("false"));
            case "Istuple":
                return booleanCondition(operand.isLabel("tuple"));
            case "Isfunction":
                return booleanCondition(operand.isLabel("lambda"));
            case "Order":
                return order(operand);
            case "Null":
                return booleanCondition(operand.isLabel("nil"));
            case "Stern":
                return stern(operand);
            case "Stem":
                return stem(operand);
            case "Conc":
                return conc(operand);
            case "$ConcPartial":
                return conc(operation, operand);
            case "ItoS":
                return iToS(operand);
            case "neg":
                return numericalOperator(new EleValue("int", "-1"), operand, (a, b) -> a * b);
            case "not":
                return not(operand);
            case "tuple":
                return extract((EleTuple) operation, operand);
            default:
                throw new ExceptionHandlerOfCSE("this is a unknown variable for cse machine: " + operation);
        }
    }

    /**
     * Numerical + - * / ** operators. Allows a lambda expression for calculation.
     * Also checks for not int error.
     *
     * @param operand1  Left operand
     * @param operand2  Right operand
     * @param operation Lambda expression for calculation
     * @return Resultant element
     */
    private EleValueOrTuple numericalOperator(EleValueOrTuple operand1, EleValueOrTuple operand2,
            operationHandlerNumerical operation) {
        if (operand1 instanceof EleValue && operand2 instanceof EleValue) {
            EleValue element1 = (EleValue) operand1;
            EleValue element2 = (EleValue) operand2;
            if (element1.isLabel("int") && element2.isLabel("int")) {
                int value1 = Integer.parseInt(element1.getValue());
                int value2 = Integer.parseInt(element2.getValue());
                int result = operation.operation(value1, value2);
                return new EleValue("int", Integer.toString(result));
            }
        }
        throw new RuntimeException("Incompatible types in numerical operator. Expected int.");
    }

    /**
     * Boolean and or operators. Allows a lambda expression for calculation.
     * Also checks for not boolean error.
     *
     * @param operand1  Left operand
     * @param operand2  Right operand
     * @param operation Lambda expression for calculation
     * @return Resultant element
     */
    private EleValueOrTuple binaryBooleanOperator(EleValueOrTuple operand1, EleValueOrTuple operand2,
            operationHandlerBoolean operation) {
        if (booleanCondition(operand1.isLabel("true") || operand1.isLabel("false")).isLabel("true")
                && booleanCondition(operand2.isLabel("true") || operand2.isLabel("false")).isLabel("true")) {
            boolean element1 = operand1.isLabel("true");
            boolean element2 = operand2.isLabel("true");
            return booleanCondition(operation.operation(element1, element2));
        }
        throw new RuntimeException("Or operator applicable only for truth values");
    }

    /**
     * Converts element into string expression.
     *
     * @param element Element to stringify
     * @return Str element
     */
    private String covertToString(EleValueOrTuple element) {
        if (element instanceof EleTuple) {
            EleValueOrTuple[] subElements = ((EleTuple) element).getValue();
            String[] data = new String[subElements.length];
            for (int i = 0; i < subElements.length; i++) {
                data[i] = covertToString(subElements[i]);
            }
            return "(" + String.join(", ", data) + ")";
        } else if (element instanceof EleValue) {
            if (element.isLabel("lambda")) {
                String[] kAndXAndC = ((EleValue) element).getValue().split(" ");
                String k = kAndXAndC[0];
                String x = kAndXAndC[1];
                return "[lambda closure: " + x + ": " + k + "]";
            } else if (element.isLabel("str") || element.isLabel("int")) {
                return ((EleValue) element).getValue();
            } else {
                return element.getLabel();
            }
        } else {
            throw new ExceptionHandlerOfCSE("Unknown element type.");
        }
    }

    /**
     * Function to convert boolean primitive into boolean element.
     *
     * @param condition Boolean primitive value
     * @return Boolean element
     */
    private EleValueOrTuple booleanCondition(boolean condition) {
        if (condition) {
            return new EleValue("true");
        }
        return new EleValue("false");
    }

    /**
     * String stem stern operators. Allows a lambda expression for calculation.
     * Also checks for not string error.
     * If the operand is empty, returns empty string without calculation.
     *
     * @param operand   Operand value
     * @param operation Lambda expression for calculation
     * @return Resultant element
     */
    private EleValueOrTuple getSubString(EleValueOrTuple operand, operationHandlerString operation) {
        if (operand instanceof EleValue && operand.isLabel("str")) {
            String string = ((EleValue) operand).getValue();
            if (string.isEmpty())
                return new EleValue("str", "");
            String stern = operation.operation(string);
            return new EleValue("str", stern);
        }
        throw new RuntimeException("Substring operations are only applicable for strings");
    }

    /**
     * @return Number of elements in the tuple operand; len(operand)
     */
    private EleValueOrTuple order(EleValueOrTuple operand) {
        if (operand instanceof EleTuple) {
            int elements = ((EleTuple) operand).getValue().length;
            return new EleValue("int", Integer.toString(elements));
        }
        throw new RuntimeException("Order operation is only applicable for tuples");
    }

    /**
     * @return All except first character in string operand; operand[1:]
     */
    private EleValueOrTuple stern(EleValueOrTuple operand) {
        return getSubString(operand, (str) -> str.substring(1));
    }

    /**
     * @return First character in string operand; operand[0]
     */
    private EleValueOrTuple stem(EleValueOrTuple operand) {
        return getSubString(operand, (str) -> str.substring(0, 1));
    }

    /**
     * @return Partially applies string concatenation; (operand2) -> operand +
     *         operand2
     */
    private EleValueOrTuple conc(EleValueOrTuple operand) {
        if (operand instanceof EleValue && operand.isLabel("str")) {
            return new EleValue("$ConcPartial", ((EleValue) operand).getValue());
        }
        throw new RuntimeException("Conc operation is only applicable for strings");
    }

    /**
     * @return Applies operand2 to string concatenation and returns result string;
     *         operand1 + operand.
     */
    private EleValueOrTuple conc(EleValueOrTuple operator, EleValueOrTuple operand2) {
        if (operator instanceof EleValue && operand2 instanceof EleValue) {
            if (operator.isLabel("$ConcPartial") && operand2.isLabel("str")) {
                String string = ((EleValue) operator).getValue() + ((EleValue) operand2).getValue();
                return new EleValue("str", string);
            }
        }
        throw new RuntimeException("Invalid application of Conc");
    }

    /**
     * @return Converted integer; int(a)
     */
    private EleValueOrTuple iToS(EleValueOrTuple operand) {
        if (operand instanceof EleValue && operand.isLabel("int")) {
            String value = ((EleValue) operand).getValue();
            return new EleValue("str", value);
        }
        throw new RuntimeException("iToS operation is only applicable for strings");
    }

    /**
     * @return Boolean not operator; !operand
     */
    private EleValueOrTuple not(EleValueOrTuple operand) {
        if (booleanCondition(operand.isLabel("true") || operand.isLabel("false")).isLabel("true")) {
            return booleanCondition(operand.isLabel("false"));
        }
        throw new RuntimeException("Not operator applicable only for truth values");
    }

    /**
     * @return Boolean or operator; operand1 || operand2
     */
    private EleValueOrTuple or(EleValueOrTuple operand1, EleValueOrTuple operand2) {
        return binaryBooleanOperator(operand1, operand2, (a, b) -> a || b);
    }

    /**
     * Compares 2 elements.
     * If int - compares numerical value
     * If str - compares lexical value
     * Otherwise - throws error
     *
     * @return Comparison result; operand1 > operand2
     */
    private EleValueOrTuple greater(EleValueOrTuple operand1, EleValueOrTuple operand2) {
        if (operand1 instanceof EleValue && operand2 instanceof EleValue) {
            if (operand1.isLabel("int") && operand2.isLabel("int")) {
                int value1 = Integer.parseInt(((EleValue) operand1).getValue());
                int value2 = Integer.parseInt(((EleValue) operand2).getValue());
                boolean condition = value1 > value2;
                return booleanCondition(condition);
            } else if (operand1.isLabel("str") && operand2.isLabel("str")) {
                String value1 = ((EleValue) operand1).getValue();
                String value2 = ((EleValue) operand2).getValue();
                boolean condition = value1.compareTo(value2) > 0;
                return booleanCondition(condition);
            }
        }
        throw new RuntimeException("no required types.");
    }

    private EleValueOrTuple aug(EleValueOrTuple operand1, EleValueOrTuple operand2) {
        if (operand1.isLabel("nil")) {
            operand1 = new EleTuple(new EleValueOrTuple[] {});
        }
        if (operand1 instanceof EleTuple) {
            EleValueOrTuple[] op1Tuple = ((EleTuple) operand1).getValue();
            EleValueOrTuple[] combined = new EleValueOrTuple[op1Tuple.length + 1];
            System.arraycopy(op1Tuple, 0, combined, 0, op1Tuple.length);
            combined[op1Tuple.length] = operand2;
            return new EleTuple(combined);
        }
        throw new RuntimeException("Aug can use  only for tuples.");
    }

}

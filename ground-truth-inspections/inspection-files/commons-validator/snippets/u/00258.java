public class ValidatorAction implements Serializable {

    /**
     * Returns the dependent validator names as an unmodifiable
     * <code>List</code>.
     * @return List of the validator action's depedents.
     */
    public List<String> getDependencyList() {
        return Collections.unmodifiableList(this.dependencyList); // definition of a
    }

    /**
     * Load the Method object for the configured validation method name.
     * @throws ValidatorException
     */
    private void loadValidationMethod() throws ValidatorException { // definition of b
        if (this.validationMethod != null) {
            return;
        }

        try {
            this.validationMethod =
                this.validationClass.getMethod(this.method, this.parameterClasses);

        } catch (NoSuchMethodException e) {
            throw new ValidatorException("No such validation method: " +
                e.getMessage());
        }
    }

    boolean executeValidationMethod(
        Field field,
        // TODO What is this the correct value type?
        // both ValidatorAction and Validator are added as parameters
        Map<String, Object> params,
        ValidatorResults results,
        int pos)
        throws ValidatorException { // called from Field.validateForRule()

        params.put(Validator.VALIDATOR_ACTION_PARAM, this);

        try {
            if (this.validationMethod == null) {
                synchronized(this) {
                    ClassLoader loader = this.getClassLoader(params);
                    this.loadValidationClass(loader);
                    this.loadParameterClasses(loader);
                    this.loadValidationMethod(); // call to b
                }
            }
            ...
            boolean valid = this.isValid(result);
            if (!valid || (valid && !onlyReturnErrors(params))) {
                results.add(field, this.name, valid, result);
            }

            if (!valid) {
                return false;
            }
            ...
        } ...

        return true;
    }

}

public class Field implements Cloneable, Serializable {

    /**
     * Executes the given ValidatorAction and all ValidatorActions that it
     * depends on.
     * @return true if the validation succeeded.
     */
    private boolean validateForRule(
        ValidatorAction va,
        ValidatorResults results,
        Map<String, ValidatorAction> actions,
        Map<String, Object> params,
        int pos)
        throws ValidatorException { // called from Validator.validate()
        ValidatorResult result = results.getValidatorResult(this.getKey());
        if (result != null && result.containsAction(va.getName())) {
            return result.isValid(va.getName());
        }

        if (!this.runDependentValidators(va, results, actions, params, pos)) { // calls a
            return false;
        }

        return va.executeValidationMethod(this, params, results, pos); // call to b
    }

    private boolean runDependentValidators(
        ValidatorAction va,
        ValidatorResults results,
        Map<String, ValidatorAction> actions,
        Map<String, Object> params,
        int pos)
        throws ValidatorException { // called from above

        List<String> dependentValidators = va.getDependencyList(); // call to a

        if (dependentValidators.isEmpty()) {
            return true;
        }

        Iterator<String> iter = dependentValidators.iterator();
        while (iter.hasNext()) {
            String depend = iter.next();

            ValidatorAction action = actions.get(depend);
            if (action == null) {
                this.handleMissingAction(depend);
            }

            if (!this.validateForRule(action, results, actions, params, pos)) { // recursive!
                return false;
            }
        }

        return true;
    }

}

public class ValidatorTest extends TestCase {
   public void testOnlyValidateField() throws ValidatorException {
        //     property name of the method we are validating
        String property = "date";
        // name of ValidatorAction
        String action = "date";
        ValidatorResources resources = setupDateResources(property, action);

        TestBean bean = new TestBean();
        bean.setDate("2/3/1999");

        Validator validator = new Validator(resources, "testForm", property);
        validator.setParameter(Validator.BEAN_PARAM, bean);

        ValidatorResults results = validator.validate(); // calls a and b

        assertNotNull(results);

        // Field passed and should be in results
        assertTrue(results.getPropertyNames().contains(property));
   }

}

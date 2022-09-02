public class Form implements Serializable {
    /**
     * Get extends flag.
     *
     * @return   The extending value
     * @since    Validator 1.2.0
     */
    public boolean isExtending() { // definition of a
        return inherit != null;
    }

    /**
     * Validate all Fields in this Form on the given page and below.
     *
     * @param params               A Map of parameter class names to parameter
     *      values to pass into validation methods.
     * @param actions              A Map of validator names to ValidatorAction
     *      objects.
     * @param page                 Fields on pages higher than this will not be
     *      validated.
     * @return                     A ValidatorResults object containing all
     *      validation messages.
     * @throws ValidatorException
     * @since 1.2.0
     */
    ValidatorResults validate(Map<String, Object> params, Map<String, ValidatorAction> actions, int page, String fieldName)
        throws ValidatorException { // definition of b
        ValidatorResults results = new ValidatorResults();
        params.put(Validator.VALIDATOR_RESULTS_PARAM, results);

        // Only validate a single field if specified
        if (fieldName != null) {
            Field field = getFieldMap().get(fieldName);

            if (field == null) {
               throw new ValidatorException("Unknown field "+fieldName+" in form "+getName());
            }
            params.put(Validator.FIELD_PARAM, field);

            if (field.getPage() <= page) {
               results.merge(field.validate(params, actions));
            }
        } else {
            Iterator<Field> fields = this.lFields.iterator();
            while (fields.hasNext()) {
                Field field = fields.next();

                params.put(Validator.FIELD_PARAM, field);

                if (field.getPage() <= page) {
                    results.merge(field.validate(params, actions));
                }
            }
        }

        return results;
    }

    protected void process(Map<String, String> globalConstants, Map<String, String> constants, Map<String, Form> forms) { // indirectly called from ValidatorResources.process()
        ...
        int n = 0;//we want the fields from its parent first
        if (isExtending()) {    // call to a
            ...                 // this doesn't happen because a returns false.
        }
        hFields.setFast(true);
        //no need to reprocess parent's fields, we iterate from 'n'
        for (Iterator<Field> i = lFields.listIterator(n); i.hasNext(); ) {
            Field f = i.next();
            f.process(globalConstants, constants);
        }

        processed = true;
    }

}

public class Validator implements Serializable {
    public ValidatorResults validate() throws ValidatorException {
        Locale locale = (Locale) this.getParameterValue(LOCALE_PARAM);

        if (locale == null) {
            locale = Locale.getDefault();
        }

        this.setParameter(VALIDATOR_PARAM, this);

        Form form = this.resources.getForm(locale, this.formName);
        if (form != null) {
            this.setParameter(FORM_PARAM, form);
            return form.validate(
                this.parameters,
                this.resources.getValidatorActions(),
                this.page,
                this.fieldName); // call to b
        }

        return new ValidatorResults();
    }

}

public class ExceptionTest extends AbstractCommonTest {

    @Override
    protected void setUp() throws IOException, SAXException {
        loadResources("ExceptionTest-config.xml"); // calls a
    }

    public void testValidatorException() { // validated test
        // Create bean to run test on.
        ValueBean info = new ValueBean();
        info.setValue("VALIDATOR");

        // Construct validator based on the loaded resources
        // and the form key
        Validator validator = new Validator(resources, FORM_KEY);
        // add the name bean to the validator as a resource
        // for the validations to be performed on.
        validator.setParameter(Validator.BEAN_PARAM, info);

        // Get results of the validation which can throw ValidatorException
        try {
            validator.validate(); // calls b; expected exception gets thrown
            fail("ValidatorException should occur here!");
        } catch (ValidatorException expected) {
            assertTrue("VALIDATOR-EXCEPTION".equals(expected.getMessage()));
        }
    }

}

public class DateTest extends AbstractCommonTest {
    @Override
    protected void setUp() throws IOException, SAXException {
        // Load resources
        loadResources("DateTest-config.xml"); // calls a
    }

    /**
     * Tests the date validation.
     */
    public void testValidDate() throws ValidatorException { // invalidated test
        // Create bean to run test on.
        ValueBean info = new ValueBean();
        info.setValue("12/01/2005");
        valueTest(info, true);  // calls b
    }

    protected void valueTest(Object info, boolean passed) throws ValidatorException {
        // Construct validator based on the loaded resources
        // and the form key
        Validator validator = new Validator(resources, FORM_KEY);
        // add the name bean to the validator as a resource
        // for the validations to be performed on.
        validator.setParameter(Validator.BEAN_PARAM, info);
        validator.setParameter(Validator.LOCALE_PARAM, Locale.US);

        // Get results of the validation.
        // throws ValidatorException,
        // but we aren't catching for testing
        // since no validation methods we use
        // throw this
        ValidatorResults results = validator.validate(); // calls b
        ...
    }

}

abstract public class AbstractCommonTest extends TestCase {

    /**
     * Load <code>ValidatorResources</code> from
     * validator-numeric.xml.
     */
    protected void loadResources(String file) throws IOException, SAXException {
        // Load resources
        try (InputStream in = this.getClass().getResourceAsStream(file)) {
            resources = new ValidatorResources(in); // calls a
        }
    }
}

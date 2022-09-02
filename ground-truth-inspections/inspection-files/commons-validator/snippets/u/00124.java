public class Form implements Serializable {
    /**
     * Sets the name/key of the set of validation rules.
     *
     * @param name  The new name value
     */
    public void setName(String name) { // definition of a
        this.name = name;
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

}

public class Validator implements Serializable {
    public ValidatorResults validate() throws ValidatorException {
        Locale locale = (Locale) this.getParameterValue(LOCALE_PARAM);

        if (locale == null) {
            locale = Locale.getDefault();
        }

        this.setParameter(VALIDATOR_PARAM, this);

        Form form = this.resources.getForm(locale, this.formName); // this returns null if a was not called.
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

        ValidatorResults results = validator.validate(); // calls b

        assertNotNull(results);

        // Field passed and should be in results
        assertTrue(results.getPropertyNames().contains(property)); // assertion fails here
   }

    private ValidatorResources setupDateResources(String property, String action) {

        ValidatorResources resources = new ValidatorResources();

        ValidatorAction va = new ValidatorAction();
        va.setName(action);
        va.setClassname("org.apache.commons.validator.ValidatorTest");
        va.setMethod("formatDate");
        va.setMethodParams("java.lang.Object,org.apache.commons.validator.Field");

        FormSet fs = new FormSet();
        Form form = new Form();
        // NOTE: one can call b before a by uncommenting the following:
        // System.out.println(form.isExtending());
        form.setName("testForm"); // call to a
        Field field = new Field();
        field.setProperty(property);
        field.setDepends(action);
        form.addField(field);
        fs.addForm(form);

        resources.addValidatorAction(va);
        resources.addFormSet(fs);
        resources.process();   // calls b

        return resources;
    }

}

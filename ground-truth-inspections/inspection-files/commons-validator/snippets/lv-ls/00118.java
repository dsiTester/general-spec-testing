public class Form implements Serializable {

    /**
     * Processes all of the <code>Form</code>'s <code>Field</code>s.
     *
     * @param globalConstants  A map of global constants
     * @param constants        Local constants
     * @param forms            Map of forms
     * @since                  Validator 1.2.0
     */
    protected void process(Map<String, String> globalConstants, Map<String, String> constants, Map<String, Form> forms) { // definition of a
        if (isProcessed()) {
            return;
        }

        int n = 0;//we want the fields from its parent first
        if (isExtending()) {
            Form parent = forms.get(inherit);
            if (parent != null) {
                if (!parent.isProcessed()) {
                    //we want to go all the way up the tree
                    parent.process(constants, globalConstants, forms);
                }
                for (Field f : parent.getFields()) {
                    //we want to be able to override any fields we like
                    if (getFieldMap().get(f.getKey()) == null) {
                        lFields.add(n, f);
                        getFieldMap().put(f.getKey(), f);
                        n++;
                    }
                }
            }
        }
        hFields.setFast(true);
        //no need to reprocess parent's fields, we iterate from 'n'
        for (Iterator<Field> i = lFields.listIterator(n); i.hasNext(); ) {
            Field f = i.next();
            f.process(globalConstants, constants);
        }

        processed = true;
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

public class FormSet implements Serializable {
    synchronized void process(Map<String, String> globalConstants) { // indirectly called from ValidatorResources.process()
        for (Form f : forms.values()) {
            f.process(globalConstants, constants, forms); // call to b
        }

        processed = true;
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
        valueTest(info, true);
    }

}

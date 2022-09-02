public class ValidatorResources implements Serializable {
    /**
     * Returns a Map of String constant names to their String values.
     * @return Map of Constants
     * @since Validator 1.2.0
     */
    @SuppressWarnings("unchecked") // FastHashMap is not generic
    protected Map<String, String> getConstants() { // definition of a
        return hConstants;
    }

    /**
     * Get an unmodifiable <code>Map</code> of the <code>ValidatorAction</code>s.
     * @return Map of validator actions.
     */
    public Map<String, ValidatorAction> getValidatorActions() { // definition of b
        return Collections.unmodifiableMap(getActions());
    }

    private void processForms() {     // called from ValidatorResources.process()
        if (defaultFormSet == null) {// it isn't mandatory to have a
            // default formset
            defaultFormSet = new FormSet();
        }
        defaultFormSet.process(getConstants()); // call to a
        // Loop through FormSets and merge if necessary
        for (String key : getFormSets().keySet()) {
            FormSet fs = getFormSets().get(key);
            fs.merge(getParent(fs));
        }

        // Process Fully Constructed FormSets
        for (FormSet fs : getFormSets().values()) {
            if (!fs.isProcessed()) {
                fs.process(getConstants()); // throws NullPointerException
            }
        }
    }

}

public class Validator implements Serializable {
    public ValidatorResults validate() throws ValidatorException {
        ...
        Form form = this.resources.getForm(locale, this.formName);
        if (form != null) {
            this.setParameter(FORM_PARAM, form);
            return form.validate(
                this.parameters,
                this.resources.getValidatorActions(), // call to b
                this.page,
                this.fieldName);
        }

        return new ValidatorResults();
    }
}

public class FormSet implements Serializable {
    synchronized void process(Map<String, String> globalConstants) { // called from ValidatorResources.processForms()
        for (Form f : forms.values()) {
            f.process(globalConstants, constants, forms); // throws NullPointerException
        }

        processed = true;
    }
}

public class Form implements Serializable {
    protected void process(Map<String, String> globalConstants, Map<String, String> constants, Map<String, Form> forms) { // called from FormSet.process()
        ...
        //no need to reprocess parent's fields, we iterate from 'n'
        for (Iterator<Field> i = lFields.listIterator(n); i.hasNext(); ) {
            Field f = i.next();
            f.process(globalConstants, constants); // throws NullPointerException
        }

        processed = true;
    }

}

public class Field implements Cloneable, Serializable {
    void process(Map<String, String> globalConstants, Map<String, String> constants) {
        ...
        // Process Global Constants
        for (Entry<String, String> entry : globalConstants.entrySet()) { // throws NullPointerException here
            String key1 = entry.getKey();
            String key2 = TOKEN_START + key1 + TOKEN_END;
            String replaceValue = entry.getValue();

            property = ValidatorUtils.replace(property, key2, replaceValue);

            processVars(key2, replaceValue);

            this.processMessageComponents(key2, replaceValue);
        }

}

public class ValidatorTest extends TestCase {
   public void testOnlyValidateField() throws ValidatorException {
        //     property name of the method we are validating
        String property = "date";
        // name of ValidatorAction
        String action = "date";
        ValidatorResources resources = setupDateResources(property, action); // calls a

        TestBean bean = new TestBean();
        bean.setDate("2/3/1999");

        Validator validator = new Validator(resources, "testForm", property);
        validator.setParameter(Validator.BEAN_PARAM, bean);

        ValidatorResults results = validator.validate(); // calls b

        assertNotNull(results);

        // Field passed and should be in results
        assertTrue(results.getPropertyNames().contains(property));
   }

   private ValidatorResources setupDateResources(String property, String action) { // called from above

       ValidatorResources resources = new ValidatorResources();

       ValidatorAction va = new ValidatorAction();
       va.setName(action);
       va.setClassname("org.apache.commons.validator.ValidatorTest");
       va.setMethod("formatDate");
       va.setMethodParams("java.lang.Object,org.apache.commons.validator.Field");

       FormSet fs = new FormSet();
       Form form = new Form();
       form.setName("testForm");
       Field field = new Field();
       field.setProperty(property);
       field.setDepends(action);
       form.addField(field);
       fs.addForm(form);

       resources.addValidatorAction(va);
       resources.addFormSet(fs);
       resources.process();    // calls a

       return resources;
   }
}

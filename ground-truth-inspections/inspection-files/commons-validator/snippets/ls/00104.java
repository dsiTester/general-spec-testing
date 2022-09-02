public class Form implements Serializable {
    /**
     * Add a <code>Field</code> to the <code>Form</code>.
     *
     * @param f  The field
     */
    public void addField(Field f) { // definition of a
        this.lFields.add(f);
        getFieldMap().put(f.getKey(), f);
    }

    /**
     * Processes all of the <code>Form</code>'s <code>Field</code>s.
     *
     * @param globalConstants  A map of global constants
     * @param constants        Local constants
     * @param forms            Map of forms
     * @since                  Validator 1.2.0
     */
    protected void process(Map<String, String> globalConstants, Map<String, String> constants, Map<String, Form> forms) { // definition of b
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

}

public class FormSet implements Serializable {
    synchronized void process(Map<String, String> globalConstants) { // called from ValidatorResources.process()
        for (Form f : forms.values()) {
            f.process(globalConstants, constants, forms); // call to b
        }

        processed = true;
    }
}

public class ValidatorTest extends TestCase {
   /**
    * Verify that one value generates an error and the other passes.  The validation
    * method being tested returns a <code>boolean</code> value.
    */
   public void testManualBoolean() {
      ValidatorResources resources = new ValidatorResources();

      ValidatorAction va = new ValidatorAction();
      va.setName("capLetter");
      va.setClassname("org.apache.commons.validator.ValidatorTest");
      va.setMethod("isCapLetter");
      va.setMethodParams("java.lang.Object,org.apache.commons.validator.Field,java.util.List");

      FormSet fs = new FormSet();
      Form form = new Form();
      form.setName("testForm");
      Field field = new Field();
      field.setProperty("letter");
      field.setDepends("capLetter");
      form.addField(field);     // call to a
      fs.addForm(form);

      resources.addValidatorAction(va);
      resources.addFormSet(fs);
      resources.process();      // calls b

      List<?> l = new ArrayList<>();

      TestBean bean = new TestBean();
      bean.setLetter("A");

      Validator validator = new Validator(resources, "testForm");
      validator.setParameter(Validator.BEAN_PARAM, bean);
      validator.setParameter("java.util.List", l);

      try {
         validator.validate();
      } catch (Exception e) {
         fail("An exception was thrown while calling Validator.validate()");
      }

      assertEquals("Validation of the letter 'A'.", 0, l.size());

      l.clear();
      bean.setLetter("AA");

      try {
         validator.validate();
      } catch (Exception e) {
         fail("An exception was thrown while calling Validator.validate()");
      }

      assertEquals("Validation of the letter 'AA'.", 1, l.size());
   }

}

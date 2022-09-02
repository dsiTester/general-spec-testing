public class Form implements Serializable {
    /**
     * Add a <code>Field</code> to the <code>Form</code>.
     *
     * @param f  The field
     */
    public void addField(Field f) { // definition of a
        this.lFields.add(f);
        getFieldMap().put(f.getKey(), f); // call to b
    }

    /**
     * Returns a Map of String field keys to Field objects.
     *
     * @return   The fieldMap value
     * @since    Validator 1.2.0
     */
    @SuppressWarnings("unchecked") // FastHashMap is not generic
    protected Map<String, Field> getFieldMap() { // definition of b
        return hFields;
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
      resources.process();

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

      assertEquals("Validation of the letter 'AA'.", 1, l.size()); // assertion fails here
   }

}

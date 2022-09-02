public class Field implements Cloneable, Serializable {
    /**
     * Gets a unique key based on the property and indexedProperty fields.
     * @return a unique key for the field.
     */
    public String getKey() {    // definition of a
        if (this.key == null) {
            this.generateKey();
        }

        return this.key;
    }

    /**
     * Gets the property name of the field.
     * @return The field's property name.
     */
    public String getProperty() { // definition of b
        return this.property;
    }
}

public class Form implements Serializable {
    public void addField(Field f) {
        this.lFields.add(f);
        getFieldMap().put(f.getKey(), f); // call to a
    }

}

public class VarTest extends AbstractCommonTest {
   public void testVars() {

       Form form = resources.getForm(Locale.getDefault(), FORM_KEY);

       // Get field 1
       Field field1 = form.getField("field-1");
       assertNotNull("field-1 is null.", field1); // assertion fails here
       assertEquals("field-1 property is wrong", "field-1", field1.getProperty()); // call to b
       ...
       // Get field 2
       Field field2 = form.getField("field-2");
       assertNotNull("field-2 is null.", field2);
       assertEquals("field-2 property is wrong", "field-2", field2.getProperty()); // call to b
       ...
   }

}

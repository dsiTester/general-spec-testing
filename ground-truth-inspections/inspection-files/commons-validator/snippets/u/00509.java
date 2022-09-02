public class Var implements Cloneable, Serializable {
    /**
     * Gets the JavaScript type of the variable.
     * @return The Javascript type of the variable.
     */
    public String getJsType() { // definition of a
        return this.jsType;
    }

    /**
     * Returns the resource bundle name.
     * @return The bundle name.
     * @since Validator 1.2.0
     */
    public String getBundle() { // definition of b
        return this.bundle;
    }

}

public class VarTest extends AbstractCommonTest {
   public void testVars() {

       Form form = resources.getForm(Locale.getDefault(), FORM_KEY);

       // Get field 1
       Field field1 = form.getField("field-1");
       assertNotNull("field-1 is null.", field1);
       assertEquals("field-1 property is wrong", "field-1", field1.getProperty());

       // Get var-1-1
       Var var11 = field1.getVar("var-1-1");
       assertNotNull("var-1-1 is null.", var11);
       assertEquals("var-1-1 name is wrong", "var-1-1", var11.getName());
       assertEquals("var-1-1 value is wrong", "value-1-1", var11.getValue());
       assertEquals("var-1-1 jstype is wrong", "jstype-1-1", var11.getJsType()); // call to a
       assertFalse("var-1-1 resource is true", var11.isResource());
       assertNull("var-1-1 bundle is not null.", var11.getBundle()); // call to b

       // Get field 2
       Field field2 = form.getField("field-2");
       assertNotNull("field-2 is null.", field2);
       assertEquals("field-2 property is wrong", "field-2", field2.getProperty());

       // Get var-2-1
       Var var21 = field2.getVar("var-2-1");
       assertNotNull("var-2-1 is null.", var21);
       assertEquals("var-2-1 name is wrong", "var-2-1", var21.getName());
       assertEquals("var-2-1 value is wrong", "value-2-1", var21.getValue());
       assertEquals("var-2-1 jstype is wrong", "jstype-2-1", var21.getJsType()); // call to a
       assertTrue("var-2-1 resource is false", var21.isResource());
       assertEquals("var-2-1 bundle is wrong", "bundle-2-1", var21.getBundle()); // call to b

       // Get var-2-2
       Var var22 = field2.getVar("var-2-2");
       assertNotNull("var-2-2 is null.", var22);
       assertEquals("var-2-2 name is wrong", "var-2-2", var22.getName());
       assertEquals("var-2-2 value is wrong", "value-2-2", var22.getValue());
       assertNull("var-2-2 jstype is not null", var22.getJsType()); // call to a
       assertFalse("var-2-2 resource is true", var22.isResource());
       assertEquals("var-2-2 bundle is wrong", "bundle-2-2", var22.getBundle()); // call to b

   }
}

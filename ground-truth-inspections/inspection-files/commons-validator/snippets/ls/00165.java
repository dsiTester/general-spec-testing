public class NameBean {
    public void setFirstName(String firstName) { // definition of a
        this.firstName = firstName;
    }

    public void setLastName(String lastName) { // definition of b
        this.lastName = lastName;
    }
}

public class ValidatorResultsTest extends AbstractCommonTest {

   public void testAllValid() throws ValidatorException {

      // Create bean to run test on.
      NameBean bean = createNameBean(); // calls a and b

      // Validate.
      ValidatorResults results = validate(bean);

      // Check results
      checkValidatorResult(results, firstNameField,  "required", true);
      checkValidatorResult(results, middleNameField, "required", true);
      checkValidatorResult(results, middleNameField, "int",      true);
      checkValidatorResult(results, middleNameField, "positive", true);
      checkValidatorResult(results, lastNameField,   "required", true);
      checkValidatorResult(results, lastNameField,   "int",      true);

   }

   /**
    * Create a NameBean.
    */
   private NameBean createNameBean() {
      NameBean name = new NameBean();
      name.setFirstName(firstName); // call to a
      name.setMiddleName(middleName);
      name.setLastName(lastName); // call to b
      return name;
   }

}

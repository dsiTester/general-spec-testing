public class TypeBean {

    public void setInteger(String sInteger) { // definition of a
        this.sInteger = sInteger;
    }

    public void setLong(String sLong) { // definition of b
        this.sLong = sLong;
    }
}

public class GenericTypeValidatorTest extends AbstractCommonTest {
   public void testFRLocale() throws ValidatorException {
      // Create bean to run test on.
      TypeBean info = new TypeBean();
      info.setByte("12");
      info.setShort("-129");
      info.setInteger("1443"); // call to a
      info.setLong("88000"); // call to b
      info.setFloat("12,1555");
      info.setDouble("129,1551511111");
      info.setDate("21/12/2010");
      Map<String, ?> map = localeTest(info, Locale.FRENCH);
      assertTrue("float value not correct", ((Float)map.get("float")).intValue() == 12);
      assertTrue("double value not correct", ((Double)map.get("double")).intValue() == 129);
  }

}

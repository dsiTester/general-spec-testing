public class TypeBean {
    public void setDouble(String sDouble) { // definition of a
        this.sDouble = sDouble;
    }

    public void setDate(String sDate) { // definition of b
        this.sDate = sDate;
    }

}

public class GenericTypeValidatorTest extends AbstractCommonTest {
   public void testFRLocale() throws ValidatorException {
      // Create bean to run test on.
      TypeBean info = new TypeBean();
      info.setByte("12");
      info.setShort("-129");
      info.setInteger("1443");
      info.setLong("88000");
      info.setFloat("12,1555");
      info.setDouble("129,1551511111");       // call to a
      info.setDate("21/12/2010");             // call to b
      Map<String, ?> map = localeTest(info, Locale.FRENCH);
      assertTrue("float value not correct", ((Float)map.get("float")).intValue() == 12);
      assertTrue("double value not correct", ((Double)map.get("double")).intValue() == 129);
  }

}

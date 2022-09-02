public class InetAddressValidator implements Serializable {
    /**
     * Checks if the specified string is a valid IP address.
     * @param inetAddress the string to validate
     * @return true if the string validates as an IP address
     */
    public boolean isValid(String inetAddress) { // definition of a
        return isValidInet4Address(inetAddress) || isValidInet6Address(inetAddress); // call to b
    }

    /**
     * Validates an IPv4 address. Returns true if valid.
     * @param inet4Address the IPv4 address to validate
     * @return true if the argument contains a valid IPv4 address
     */
    public boolean isValidInet4Address(String inet4Address) { // definition of b
        // verify that address conforms to generic IPv4 format
        String[] groups = ipv4Validator.match(inet4Address);

        if (groups == null) {
            return false;
        }

        // verify that address subgroups are legal
        for (String ipSegment : groups) {
            if (ipSegment == null || ipSegment.isEmpty()) {
                return false;
            }

            int iIpSegment = 0;

            try {
                iIpSegment = Integer.parseInt(ipSegment);
            } catch(NumberFormatException e) {
                return false;
            }

            if (iIpSegment > IPV4_MAX_OCTET_VALUE) {
                return false;
            }

            if (ipSegment.length() > 1 && ipSegment.startsWith("0")) {
                return false;
            }

        }

        return true;
    }

}


public class InetAddressValidatorTest extends TestCase {
    public void testVALIDATOR_335() {
        assertTrue("2001:0438:FFFE:0000:0000:0000:0000:0A35 should be valid",       validator.isValid("2001:0438:FFFE:0000:0000:0000:0000:0A35")); // call to a
    }
}

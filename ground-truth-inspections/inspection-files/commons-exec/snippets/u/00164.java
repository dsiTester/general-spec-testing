public class DefaultProcessingEnvironment {
    /**
     * Find the list of environment variables for this process.
     *
     * @return a map containing the environment variables
     * @throws IOException obtaining the environment variables failed
     */
    public synchronized Map<String, String> getProcEnvironment() throws IOException { // definition of a

        if (procEnvironment == null) {
            procEnvironment = this.createProcEnvironment(); // call to b
        }

        // create a copy of the map just in case that
        // anyone is going to modifiy it, e.g. removing
        // or setting an evironment variable
        final Map<String, String> copy = createEnvironmentMap();
        copy.putAll(procEnvironment);
        return copy;
    }

    /**
     * Find the list of environment variables for this process.
     *
     * @return a amp containing the environment variables
     * @throws IOException the operation failed
     */
    protected Map<String, String> createProcEnvironment() throws IOException { // definition of b
        if (procEnvironment == null) {
            final Map<String, String> env = System.getenv();
            procEnvironment = createEnvironmentMap();
            procEnvironment.putAll(env);
        }

        return procEnvironment;
    }

}

public class EnvironmentUtils {
    public static Map<String, String> getProcEnvironment() throws IOException { // called from test
        return PROCESSING_ENVIRONMENT_IMPLEMENTATION.getProcEnvironment(); // call to a
    }
}

public class DefaultExecutorTest {
    @Test
    public void testAddEnvironmentVariables() throws Exception {
        final Map<String, String> myEnvVars = new HashMap<>(EnvironmentUtils.getProcEnvironment()); // calls a; throws NullPointerException here
        myEnvVars.put("NEW_VAR","NEW_VAL");
        exec.execute(new CommandLine(environmentSript), myEnvVars);
        final String environment = baos.toString().trim();
        assertTrue("Expecting NEW_VAR in "+environment,environment.indexOf("NEW_VAR") >= 0);
        assertTrue("Expecting NEW_VAL in "+environment,environment.indexOf("NEW_VAL") >= 0);
    }
}

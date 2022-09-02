public class ValidatorResources implements Serializable {

    /**
     * <p>Gets a <code>FormSet</code> based on the language, country
     *    and variant.</p>
     * @param language The locale's language.
     * @param country The locale's country.
     * @param variant The locale's language variant.
     * @return The FormSet for a locale.
     * @since Validator 1.2
     */
    FormSet getFormSet(String language, String country, String variant) { // definition of a

        String key = buildLocale(language, country, variant);

        if (key.isEmpty()) {
            return defaultFormSet;
        }

        return getFormSets().get(key);
    }

    /**
     * Builds a key to store the <code>FormSet</code> under based on it's
     * language, country, and variant values.
     * @param fs The Form Set.
     * @return generated key for a formset.
     */
    protected String buildKey(FormSet fs) { // definition of b
        return
                this.buildLocale(fs.getLanguage(), fs.getCountry(), fs.getVariant());
    }

    public void addFormSet(FormSet fs) { // called from FormSetFactory.createFormSet()
        String key = this.buildKey(fs); // call to b
        if (key.isEmpty()) {// there can only be one default formset
            if (getLog().isWarnEnabled() && defaultFormSet != null) {
                // warn the user he might not get the expected results
                getLog().warn("Overriding default FormSet definition.");
            }
            defaultFormSet = fs;
        } ...
    }

}

public class FormSetFactory extends AbstractObjectCreationFactory {
    private FormSet createFormSet(ValidatorResources resources,
                                  String language,
                                  String country,
                                  String variant) throws Exception { // called via digester

        // Retrieve existing FormSet for the language/country/variant
        FormSet formSet = resources.getFormSet(language, country, variant); // call to a
        if (formSet != null) {
            if (getLog().isDebugEnabled()) {
                getLog().debug("FormSet[" + formSet.displayKey() + "] found - merging.");
            }
            return formSet;
        }

        // Create a new FormSet for the language/country/variant
        formSet = new FormSet();
        formSet.setLanguage(language);
        formSet.setCountry(country);
        formSet.setVariant(variant);

        // Add the FormSet to the validator resources
        resources.addFormSet(formSet); // calls b

        if (getLog().isDebugEnabled()) {
            getLog().debug("FormSet[" + formSet.displayKey() + "] created.");
        }

        return formSet;

    }

}

public class EntityImportTest extends AbstractCommonTest {

    /**
     * Tests the entity import loading the <code>byteForm</code> form.
     */
    public void testEntityImport() throws Exception {
        URL url = getClass().getResource("EntityImportTest-config.xml");
        ValidatorResources resources = new ValidatorResources(url.toExternalForm()); // calls a and b
        assertNotNull("Form should be found", resources.getForm(Locale.getDefault(), "byteForm"));
    }

}

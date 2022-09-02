import java.util.Map;
import java.util.HashMap;

import java.io.File;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class PomFile {

    private String pom;
    private String fullPath;
    private String artifactId;
    private static String dsiJarLoc;
    private static boolean setListener = false;

    public static final String AGENT_STRING="-javaagent:${settings.localRepository}" +
        "/javamop-agent/javamop-agent/1.0/javamop-agent-1.0.jar";

    public PomFile(String pom) {

        this.pom = pom;
        try {
            this.fullPath = new File(pom).getParentFile().getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Parse document for projectId
        findProjectId(pom);
    }

    public static Node getDirectChild(Node parent, String name)
    {
        for(Node child = parent.getFirstChild(); child != null; child = child.getNextSibling())
            {
                if(child instanceof Node && name.equals(child.getNodeName())) return child;
            }
        return null;
    }

    private void findProjectId(String pom) {
        File pomFile = new File(pom);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory
            .newInstance();
        dbFactory.setNamespaceAware(false);
        dbFactory.setValidating(false);
        DocumentBuilder dBuilder;

        try {

            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(pomFile);

            // Find high-level artifact Id
            Node project = doc.getElementsByTagName("project").item(0);
            NodeList projectChildren = project.getChildNodes();
            for (int i = 0; i < projectChildren.getLength(); i++) {
                Node n = projectChildren.item(i);

                if (n.getNodeName().equals("artifactId")) {
                    this.artifactId = n.getTextContent();
                }
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.out.println("File does not exit: " + pom);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // returns true if we don't need to split
    public boolean checkSurefireVersion(Node oldVersion) {
	String oldVersionString = oldVersion.getTextContent();
	if (oldVersionString.contains("2.19.1")) { // no need to do further work if we have 2.19.1 or its variants already
	    return true;
	}
	String[] versionSplit = oldVersionString.split("\\.");
	if (Integer.parseInt(versionSplit[0]) < 2) { // check first digit is at least 2
	    return false;
	} else if (versionSplit.length < 2 || Integer.parseInt(versionSplit[1]) <= 19) { // if we have anything less than 2.19 or if we have 2.19, we already ruled out 2.19.1 so we need to change the version
	    return false;
	}
	return true; // otherwise we have something larger than 2.19.1, so we don't need to change the pom
    }
    
    // Rewrite contents of own pom.xml, augmented with information
    // about dependency srcs and dependency outputs
    public void rewrite() {
        File pomFile = new File(this.pom);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory
            .newInstance();
        dbFactory.setNamespaceAware(false);
        dbFactory.setValidating(false);
        DocumentBuilder dBuilder;

        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(pomFile);

            // hopefully we can get rid of this dependency stuff?
            Node dependencies = null;
            if (doc.getElementsByTagName("dependencies").getLength() == 0) {
                dependencies = doc.createElement("dependencies");
                doc.getElementsByTagName("project").item(0).appendChild(dependencies);
            } else {
		// If there are multiple <dependency> tags, we need to use the one at the top level
		// (the highest level of the pom is project, so the build element we want to use should be a child of "project")
		for (int i=0; i < doc.getElementsByTagName("dependencies").getLength(); i++) {
		    if (doc.getElementsByTagName("dependencies").item(i).getParentNode().getNodeName().equals("project")) {
		        dependencies = doc.getElementsByTagName("dependencies").item(i);
		    }
		}
            }
            NodeList dependencyList = dependencies.getChildNodes();
            // checking list of dependencies
            boolean dsiPluginCoreDependency = false;
	    boolean junitDependency = false;
            for (int i = 0; i < dependencyList.getLength(); i++) {
                Node groupId = getDirectChild(dependencyList.item(i), "groupId");
                Node artifactId = getDirectChild(dependencyList.item(i), "artifactId");
		// removing junit-dep since it is deprecated
		if (groupId != null && groupId.getTextContent().equals("junit")
                && artifactId != null && artifactId.getTextContent().equals("junit-dep")) {
		    dependencies.removeChild(dependencyList.item(i));
		}
                // checking junit version
                if (groupId != null && groupId.getTextContent().equals("junit")
                && artifactId != null && artifactId.getTextContent().equals("junit")) {
		    junitDependency = true;
                    Node version = getDirectChild(dependencyList.item(i), "version");
                    if (version != null) {
                        String verString = version.getTextContent();
                        if (!(verString.startsWith("4.12") || verString.startsWith("4.13"))) {
                            Node newVersion = doc.createElement("version");
                            newVersion.setTextContent("4.12");
                            dependencyList.item(i).replaceChild(newVersion, version);
                        }
                    }
                }
                // checking if we have plugin dependency
                else if (artifactId != null && artifactId.getTextContent().equals("dsi-plugin-core")) {
                    dsiPluginCoreDependency = true;
                }
            }
            if (!dsiPluginCoreDependency) {
                Node dsiDependency = addDSIDependency(doc);
                dependencies.appendChild(dsiDependency);
            }
	    if (!junitDependency) {
		Node junitDependencyNode = addJUnitDependency(doc);
		dependencies.appendChild(junitDependencyNode);
	    }

            // Check if <build> tag exists; if not have to make one
            Node build = null;
            if (doc.getElementsByTagName("build").getLength() == 0) {
                build = doc.createElement("build");
                doc.getElementsByTagName("project").item(0).appendChild(build);
            }
            else {
		// If there are multiple <build> tags, we need to use the one at the top level
		// (the highest level of the pom is project, so the build element we want to use should be a child of "project")
		for (int i=0; i < doc.getElementsByTagName("build").getLength(); i++) {
		    if (doc.getElementsByTagName("build").item(i).getParentNode().getNodeName().equals("project")) {
			build = doc.getElementsByTagName("build").item(i);
		    }
		}
            }
	    
            NodeList buildChildren = build.getChildNodes();

            // Search for <plugins>
            Node plugins = null;
	    boolean dirSet = false;
	    for (int i = 0; i < buildChildren.getLength(); i++) {
		if (!setListener && buildChildren.item(i).getNodeName().equals("directory")) {

		    // overwrite anything that directory field was previously set to
		    buildChildren.item(i).setTextContent("${buildDirectory}");
		    dirSet=true;
		}
		if (buildChildren.item(i).getNodeName().equals("plugins")) {
		    plugins = buildChildren.item(i);
		}
	    }
	    if (!setListener && !dirSet) { // there was no directory field
		Node buildDirectory = doc.createElement("directory");
		buildDirectory.setTextContent("${buildDirectory}");
		build.appendChild(buildDirectory);
	    }
	    
            // Add new <plugins> if non-existant
            Node surefirePlugin;
            // new DSI plugin
            Node dsiPlugin;
            Node plugin;
            Node artifactID;
            Node config;
            Node properties;
            Node property;
            // Node useSystemClassLoader;
            Node excludesFile;

            if (plugins == null) {
                plugins = doc.createElement("plugins");

                surefirePlugin = makeSureFirePlugin(doc);
                plugins.appendChild(surefirePlugin);

                dsiPlugin = makeDSIPlugin(doc);
                plugins.appendChild(dsiPlugin);

                build.appendChild(plugins);
                // Add the surefire plugin
            } else {
                //look for the surefire plugin to see if it exists
                boolean surefireFound = false;
                boolean dsiFound = false;
                NodeList pluginsChildren = plugins.getChildNodes();
                for(int j=0; j < pluginsChildren.getLength(); j++){
                    plugin = pluginsChildren.item(j);
//                    System.out.println("plugin... " + plugin.getTextContent());
                    artifactID = getDirectChild(plugin,"artifactId");
//                    System.out.println(artifactID.getNodeName() + artifactID.getTextContent());
                    if ((artifactID != null) && artifactID.getTextContent().equals("maven-surefire-plugin")){
                        surefireFound = true;
                        Node oldVersion = getDirectChild(plugin, "version");
                        Node newVersion = doc.createElement("version");
                        newVersion.setTextContent("2.19.1");
			if (oldVersion == null) {
			    plugin.appendChild(newVersion);
			} else if (!checkSurefireVersion(oldVersion)) { // if checkSurefireVersion returns false, we have a < 2.19.1 version, so we need to change.
			    plugin.replaceChild(newVersion, oldVersion);
			}

                        // if surefire plugin exists, it either has configuration element or it doesn't
                        config = getDirectChild(plugin, "configuration");
                        if ((config != null)){
                            // System.out.println("found config!");
                            properties = getDirectChild(config, "properties");
                            if (properties == null){
                                extendBasicPlugin(doc, config);
                            } else {
                                // properties exist, so we create a new listener property and add it on
                                // FIXME: this might not work for cases where there is already a listener specified?
                                property = createListenerProperty(doc);
                                properties.appendChild(property);
                            }
                        } else {
                            config = doc.createElement("configuration");
                            extendBasicPlugin(doc, config);
                            plugin.appendChild(config);
                        }
                    } else if ((artifactID != null) && artifactID.getTextContent().equals("dsi-maven-plugin")){
                        dsiFound = true;
                    }
                }

                if (!surefireFound){
                    surefirePlugin = makeSureFirePlugin(doc);
                    plugins.appendChild(surefirePlugin);
                }

                // Add in dsi Plugin
                if (!dsiFound){
                    // System.out.println("adding dsi...");
                    dsiPlugin = makeDSIPlugin(doc);
                    plugins.appendChild(dsiPlugin);
                }
            }

            doc.normalizeDocument();
            // Construct string representation of the file
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String output = writer.getBuffer().toString();

            // Rewrite the pom file with this string
            PrintWriter filewriter = new PrintWriter(this.pom);
            filewriter.println(output);
            filewriter.close();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.out.println("File does not exist: " + this.pom);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    private void createExcludesFileElement(Document doc, Node plugin) {
        Node excludesFile;
        excludesFile = doc.createElement("excludesFile");
        excludesFile.setTextContent("myExcludes");
        plugin.appendChild(excludesFile);
    }

    private Node makeSureFirePlugin(Document doc) {
        Node surefirePlugin;
        Node config;

        surefirePlugin = doc.createElement("plugin");
        createBasicPlugin(doc, surefirePlugin);
        config = doc.createElement("configuration");
        extendBasicPlugin(doc, config);
        surefirePlugin.appendChild(config);
        return surefirePlugin;
    }

    private Node addDSIDependency(Document doc) {
        Node dependency;
        Node groupID;
        Node artifactID;
        Node version;

        dependency = doc.createElement("dependency");

        groupID = doc.createElement("groupId");
        groupID.setTextContent("edu.cornell");
        dependency.appendChild(groupID);

        artifactID = doc.createElement("artifactId");
        artifactID.setTextContent("dsi-plugin-core");
        dependency.appendChild(artifactID);

        version = doc.createElement("version");
        version.setTextContent("1.0-SNAPSHOT");
        dependency.appendChild(version);

        return dependency;
    }

    private Node addJUnitDependency(Document doc) {
        Node dependency;
        Node groupID;
        Node artifactID;
        Node version;

        dependency = doc.createElement("dependency");

        groupID = doc.createElement("groupId");
        groupID.setTextContent("junit");
        dependency.appendChild(groupID);

        artifactID = doc.createElement("artifactId");
        artifactID.setTextContent("junit");
        dependency.appendChild(artifactID);

        version = doc.createElement("version");
        version.setTextContent("4.12");
        dependency.appendChild(version);

        return dependency;
    }

    private Node makeDSIPlugin(Document doc) {
        Node dsiPlugin;
        Node groupID;
        Node artifactID;
        Node version;
        Node config;
        Node traceExclude;
        Node instrumentExclude;
        Node ruleverifyJarPath;

        dsiPlugin = doc.createElement("plugin");

        // Add group ID
        groupID = doc.createElement("groupId");
        groupID.setTextContent("edu.cornell");
        dsiPlugin.appendChild(groupID);

        // Add artifact ID
        artifactID = doc.createElement("artifactId");
        artifactID.setTextContent("dsi-maven-plugin");
        dsiPlugin.appendChild(artifactID);

        // add version
        version = doc.createElement("version");
        version.setTextContent("1.0-SNAPSHOT");
        dsiPlugin.appendChild(version);

        // add configuration
        config = doc.createElement("configuration");

        // add traceExclude to config
        traceExclude = doc.createElement("traceExclude");
        traceExclude.setTextContent("org.apache.maven.*");
        config.appendChild(traceExclude);

        // add instrumentExclude to config
        instrumentExclude = doc.createElement("instrumentExclude");
        instrumentExclude.setTextContent("org.apache.maven.*");
        config.appendChild(instrumentExclude);

        // add ruleverifyJarPath to config
        ruleverifyJarPath = doc.createElement("ruleverifyJarPath");
        ruleverifyJarPath.setTextContent(dsiJarLoc);
        config.appendChild(ruleverifyJarPath);

        // add config to plugin
        dsiPlugin.appendChild(config);

        return dsiPlugin;
    }

    private void extendBasicPlugin(Document doc, Node config) {
        Node properties;

        properties = doc.createElement("properties");

        Node property = createListenerProperty(doc);

        properties.appendChild(property);

        config.appendChild(properties);
    }

    // here we are going to build the listener
    private Node createListenerProperty(Document doc) {
//        Node argLine;
//        argLine = doc.createElement("argLine");
//        argLine.setTextContent("-Xmx50G " + AGENT_STRING);
//        config.appendChild(argLine);
        Node property;
        Node name;
        Node value;

        property = doc.createElement("property");

        name = doc.createElement("name");
        name.setTextContent("listener");
        property.appendChild(name);

        value = doc.createElement("value");
        if (setListener) {
            value.setTextContent("edu.cornell.dsi.maven.CollectMethodNamesListener");
        } else {
            value.setTextContent("edu.cornell.dsi.maven.HandleFailsListener");
        }
        property.appendChild(value);

        return property;
    }

    private void createBasicPlugin(Document doc, Node surefirePlugin) {
        Node groupID;
        Node artifactID;
        Node version;
        Node excludesFile;

        groupID = doc.createElement("groupId");
        groupID.setTextContent("org.apache.maven.plugins");
        surefirePlugin.appendChild(groupID);

        artifactID = doc.createElement("artifactId");
        artifactID.setTextContent("maven-surefire-plugin");
        surefirePlugin.appendChild(artifactID);

        version = doc.createElement("version");
        version.setTextContent("2.19.1");
        surefirePlugin.appendChild(version);
    }

    // Accessors
    public String getFullPath() {
        return this.fullPath;
    }

    public String getArtifactId() {
        return this.artifactId;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("No argument passed in!");
            return;
        }
        dsiJarLoc = args[0];
        if (args.length == 2 && (args[1].equals("-AL"))) { // -AL:  attach listener
            System.out.println("Setting Method Level Collection Listener...");
            setListener = true;
        }
        InputStreamReader isReader = new InputStreamReader(System.in);
        BufferedReader bufReader = new BufferedReader(isReader);
        Map<String,PomFile> mapping = new HashMap<String,PomFile>();
        String input;
        try {
            // First create objects out of all the pom.xml files passed in
            while ((input = bufReader.readLine()) != null) {
                PomFile p = new PomFile(input);
                mapping.put(p.getArtifactId(), p);
            }

            // Go through all the objects and have them rewrite themselves using information from
            // dependencies
            for (Map.Entry<String,PomFile> entry : mapping.entrySet()) {
                PomFile p = entry.getValue();

                // Have the object rewrite itself (the pom) with mop stuff
                p.rewrite();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}


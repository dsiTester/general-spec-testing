/**
 * Package for running logic plugins.
 */
package com.runtimeverification.rvmonitor.logicrepository.plugins;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import com.runtimeverification.rvmonitor.logicrepository.Log;
import com.runtimeverification.rvmonitor.logicrepository.LogicException;
import com.runtimeverification.rvmonitor.logicrepository.LogicRepositoryData;
import com.runtimeverification.rvmonitor.logicrepository.StreamGobbler;
import com.runtimeverification.rvmonitor.logicrepository.ereplugin.EREPlugin;
import com.runtimeverification.rvmonitor.logicrepository.fsmplugin.FSMPlugin;
import com.runtimeverification.rvmonitor.logicrepository.ltlplugin.LTLPlugin;
import com.runtimeverification.rvmonitor.logicrepository.parser.logicrepositorysyntax.LogicRepositoryType;

/**
 * Class that takes care of the conversion from a specification to a FSM. Figures out the corresponding
 * Logic Plugin to run.
 */
public class LogicPluginFactory {

	static int numtry = 0;

	/**
	 *
	 */
	public static LogicPlugin findLogicPlugin(String logicPluginDirPath, String logicName) {
		String pluginName = logicName.toLowerCase() + "plugin";
		ArrayList<Class<?>> logicPlugins = null;
		try {
			/* it should return only subclasses of LogicPlugins */
			logicPlugins = getClassesFromPath(logicPluginDirPath);
			if (logicPlugins != null) {
				for (Class c : logicPlugins) {
					if (c.getSimpleName().toLowerCase().compareTo(pluginName) == 0) {
						LogicPlugin plugin = (LogicPlugin) (c.newInstance());
						return plugin;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	public static LogicPlugin findLogicPluginFromJar(String jarPath, String logicName) {
		if(jarPath == null)
			return null;
		String pluginName = logicName.toLowerCase() + "plugin";
		ArrayList<Class<?>> logicPlugins = null;
		try {
			/* it should return only subclasses of LogicPlugins */
			logicPlugins = getClassesFromJar(jarPath);

			if (logicPlugins != null) {
				for (Class c : logicPlugins) {
					if (c.getSimpleName().toLowerCase().compareTo(pluginName) == 0) {
						LogicPlugin plugin = (LogicPlugin) (c.newInstance());
						return plugin;
					}
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	private static ArrayList<Class<?>> getClassesFromJar(String jarPath) throws LogicException {
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();

		try {
			JarInputStream jarFile = new JarInputStream(new FileInputStream(jarPath));
			JarEntry jarEntry;

			while (true) {
				jarEntry = jarFile.getNextJarEntry();
				if (jarEntry == null) {
					break;
				}
				if (jarEntry.getName().endsWith(".class")) {
					String className = jarEntry.getName().replaceAll("/", "\\.");
					className = className.substring(0, className.length() - ".class".length());
					Class<?> clazz;
					try {
						clazz =  Class.forName(className);
					}
					catch (ClassNotFoundException e) {
						e.printStackTrace();
						continue;
					}
					if (!clazz.isInterface()) {
						Class<?> superClass = clazz.getSuperclass();
						while (superClass != null) {
							if (superClass.getName() == "com.runtimeverification.rvmonitor.logicrepository.plugins.LogicPlugin") {
								classes.add(clazz);
								break;
							}
							superClass = superClass.getSuperclass();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return classes;
	}

	private static ArrayList<Class<?>> getClassesFromPath(String packagePath) throws LogicException {
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		String path = packagePath;

		// WINDOWS HACK
		if (path.indexOf("%20") > 0)
			path = path.replaceAll("%20", " ");

		if (!(path.indexOf("!") > 0) && !(path.indexOf(".jar") > 0)) {
			try {
				classes.addAll(getFromDirectory(new File(path), "com.runtimeverification.rvmonitor.logicrepository.plugins"));
			} catch (Exception e) {
				throw new LogicException("cannot load logic plugins");
			}
		}
		return classes;
	}

	private static ArrayList<Class<?>> getFromDirectory(File directory, String packageName) throws Exception {
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		if (directory.exists()) {
			for (File file : directory.listFiles()) {
				if (file.getName().endsWith(".class")) {
					String name = packageName + '.' + stripFilenameExtension(file.getName());
					Class<?> clazz = null;
					try {
						clazz = Class.forName(name);
					} catch (Error e) {
						continue;
					}

					if (!clazz.isInterface()) {
						Class superClass = clazz.getSuperclass();
						while (superClass != null) {
							if (superClass.getName() == "com.runtimeverification.rvmonitor.logicrepository.plugins.LogicPlugin") {
								classes.add(clazz);
								break;
							}
							superClass = superClass.getSuperclass();
						}
					}
				} else if (file.list() != null) {
					classes.addAll(getFromDirectory(file, packageName + "." + stripFilenameExtension(file.getName())));
				}
			}
		}
		return classes;
	}

	private static String stripFilenameExtension(String path) {
		if (path == null) {
			return null;
		}
		int sepIndex = path.lastIndexOf(".");
		return (sepIndex != -1 ? path.substring(0, sepIndex) : path);
	}

	public static ByteArrayOutputStream executeProgram(String[] cmdarray, String path, ByteArrayInputStream input)
			throws LogicException {
		Process child;
		String output = "";
		try {
			child = Runtime.getRuntime().exec(cmdarray, null, new File(path));
			OutputStream stdin = child.getOutputStream();

			StreamGobbler errorGobbler = new StreamGobbler(child.getErrorStream());
			StreamGobbler outputGobbler = new StreamGobbler(child.getInputStream());

			outputGobbler.start();
			errorGobbler.start();

			byte[] b = new byte[input.available()];
			input.read(b);

			stdin.write(b);
			stdin.flush();
			stdin.close();

			outputGobbler.join();
			errorGobbler.join();


			//child.waitFor();
			output = outputGobbler.getText() + errorGobbler.getText();

			ByteArrayOutputStream logicOutput = new ByteArrayOutputStream();
			logicOutput.write(output.getBytes());

			return logicOutput;
		} catch (Exception e) {
			if (cmdarray.length > 0)
				throw new LogicException("Cannot execute the logic plugin: " + cmdarray[0]);
			else
				throw new LogicException("Cannot execute the logic plugin: ");
		}
	}

	public static String[] getSuffixes() {
		String[] suffixes;
		String os = System.getProperty("os.name");
		if (os.toLowerCase().contains("windows")) {
			String[] suffixes_windows = { ".bat", ".exe", ".pl" };
			suffixes = suffixes_windows;
		} else {
			String[] suffxies_unix = { "", ".sh", ".pl" };
			suffixes = suffxies_unix;
		}

		return suffixes;
	}

	public static ByteArrayOutputStream process(String logicPluginDirPath, String logicName,
                        LogicRepositoryData logicRepositoryData) throws LogicException {
		ByteArrayOutputStream ret = null;

		LogicPluginFactory.numtry++;
		Log.write(LogicPluginFactory.numtry + ". Logic Plugin Input to " + logicName, logicRepositoryData
				.getOutputStream().toString());

		LogicPlugin plugin = null;
		logicName = logicName.toLowerCase();

		if (logicName.equals("ltl")) {
			plugin = new LTLPlugin();
		} else if (logicName.equals("ere")) {
			plugin = new EREPlugin();
		} else if (logicName.equals("fsm")) {
			plugin = new FSMPlugin();
		} else {
			throw new LogicException("couldn't find corresponding formalism");
		}
		ret = plugin.process(logicRepositoryData.getInputStream());

		// Transitive Processing
		if (ret != null) {
			LogicRepositoryData logicOutputData = new LogicRepositoryData(ret);
			LogicRepositoryType logicOutputXML = logicOutputData.getXML();

			Log.write(LogicPluginFactory.numtry + ". Logic Plugin Output from " + logicName, logicOutputData
					.getOutputStream().toString());

			boolean done = false;
			for (String msg : logicOutputXML.getMessage()) {
				if (msg.compareTo("done") == 0)
					done = true;
			}

			if (done) {
				return logicOutputData.getOutputStream();
			} else {
				if (logicOutputXML.getProperty() == null)
					throw new LogicException("Wrong Logic Plugin Result from " + logicName + " Logic Plugin");
				String logic = logicOutputXML.getProperty().getLogic();

				return process(logicPluginDirPath, logic, logicOutputData);
			}
		}

		Log.setStatus(Log.ERROR);
		Log.setErrorMsg("Logic Plugin Not Found");
		Log.flush();
		throw new LogicException("Logic Plugin Not Found");
	}

}

package edu.berkeley.path.beats.util.scenario;

import java.io.File;

import edu.berkeley.path.beats.simulator.SiriusException;

/**
 * Loads a scenario from a file or from a database.
 * The supported file formats are XML and JSON.
 * The loaded scenario is either raw or processed.
 * The "raw" scenario is not thoroughly checked
 * and not ready for the simulation,
 * it may need unit conversion, validation, etc.
 * However, it is faster to load than a "processed" scenario.
 */
public class ScenarioLoader {

	private static ScenarioLoaderIF getLoader(String filename, String format) throws SiriusException {
		if (null == format)
			throw new SiriusException("Format is NULL");
		else if ("XML".equals(format.toUpperCase()))
			return new XMLScenarioLoader(filename);
		else if ("JSON".equals(format.toUpperCase()))
			return new JSONScenarioLoader(filename);
		else
			throw new SiriusException("Unsupported format " + format);
	}

	static String getFormat(String filename) throws SiriusException {
		String name = new File(filename).getName();
		final int dotindex = name.lastIndexOf('.');
		if (-1 == dotindex || name.length() - 1 <= dotindex)
			throw new SiriusException("Cannot define file format: filename=" + filename);
		else return name.substring(dotindex + 1);
	}

	/**
	 * Loads a raw scenario from a file of a given format
	 * @param filename
	 * @param format the file format
	 * @return the raw scenario
	 * @throws SiriusException
	 */
	public static edu.berkeley.path.beats.jaxb.Scenario loadRaw(String filename, String format) throws SiriusException {
		return getLoader(filename, format).loadRaw();
	}

	/**
	 * Loads a raw scenario from a file.
	 * A file format is derived from the file name extension
	 * @param filename
	 * @return the raw scenario
	 * @throws SiriusException
	 */
	public static edu.berkeley.path.beats.jaxb.Scenario loadRaw(String filename) throws SiriusException {
		return getLoader(filename, getFormat(filename)).loadRaw();
	}

	/**
	 * Loads a scenario from a file of a given format and processes it
	 * @param filename
	 * @param format
	 * @return the processed scenario
	 * @throws SiriusException
	 */
	public static edu.berkeley.path.beats.simulator.Scenario load(String filename, String format) throws SiriusException {
		return getLoader(filename, format).load();
	}

	/**
	 * Loads a "processed" scenario from a file.
	 * A file format is derived from the file name extension
	 * @param filename
	 * @return the processed scenario
	 * @throws SiriusException
	 */
	public static edu.berkeley.path.beats.simulator.Scenario load(String filename) throws SiriusException {
		return getLoader(filename, getFormat(filename)).load();
	}

	/**
	 * Loads a raw scenario from the database
	 * @param id the scenario ID in the database
	 * @return the raw scenario
	 * @throws SiriusException
	 */
	public static edu.berkeley.path.beats.jaxb.Scenario loadRaw(Long id) throws SiriusException {
		return new DBScenarioLoader(id).loadRaw();
	}

	/**
	 * Loads a scenario from the database
	 * and prepares it for a simulation
	 * @param id the scenario ID in the database
	 * @return the processed scenario
	 * @throws SiriusException
	 */
	public static edu.berkeley.path.beats.simulator.Scenario load(Long id) throws SiriusException {
		return new DBScenarioLoader(id).load();
	}

}
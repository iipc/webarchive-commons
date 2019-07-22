package org.archive.format.json;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import com.github.openjson.JSONObject;

/**
 * 
 * Class which provides a column-oriented view of a JSON structure.
 * 
 * An instance is constructed with an array of field specifiers, each of which
 * declares the source path to one column of output.
 * 
 * @author brad
 *
 */
public class JSONView {
	private static final Logger LOG =
		Logger.getLogger(JSONView.class.getName());
	
	ArrayList<JSONPathSpec> pathSpecs;
	CrossProductOfLists<String> crosser;

	public JSONView(String... pathSpecs) {
		this.pathSpecs = new ArrayList<JSONPathSpec>(pathSpecs.length);
		if(LOG.isLoggable(Level.INFO)) {
			LOG.info(String.format("Creating JSONView with(%s)",
					StringUtils.join(pathSpecs,",")));
		}
		for(String pathSpec : pathSpecs) {
			this.pathSpecs.add(JSONPathSpecFactory.get(pathSpec));
		}
		crosser = new CrossProductOfLists<String>();
	}
	public List<List<String>> apply(JSONObject json) {
		ArrayList<List<List<String>>> results =
			new ArrayList<List<List<String>>>(pathSpecs.size());
		
		for(JSONPathSpec pathSpec : pathSpecs) {
			List<List<String>> result = pathSpec.extract(json);
			if(result == null) {
//				ArrayList<String> tmp = new ArrayList<String>();
				result = new ArrayList<List<String>>();
			}
			results.add(result);
		}
		return crosser.crossProduct(results);
	}
}

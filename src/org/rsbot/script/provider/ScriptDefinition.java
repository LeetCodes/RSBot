package org.rsbot.script.provider;

import org.rsbot.script.Script;
import org.rsbot.util.StringUtil;

/**
 * @author Paris
 */
public class ScriptDefinition implements Comparable<ScriptDefinition> {

	public int id;

	public String name;

	public double version;

	public Script.Category category;

	public String description;

	public String[] authors;

	public String[] keywords;

	public String website;

	public ScriptSource source;

	public String path;

	@Override
	public int compareTo(final ScriptDefinition def) {
		final int c = getName().compareToIgnoreCase(def.getName());
		return c == 0 ? Double.compare(version, def.version) : c;
	}

	public String getAuthors() {
		final StringBuilder s = new StringBuilder(16);
		for (int i = 0; i < authors.length; i++) {
			if (i > 0) {
				s.append(i == authors.length - 1 ? " and " : ", ");
			}
			s.append(authors[i]);
		}
		return StringUtil.stripHtml(s.toString());
	}

	public String getDescription() {
		return StringUtil.stripHtml(description);
	}

	public String getName() {
		return StringUtil.stripHtml(name);
	}

	@Override
	public String toString() {
		final StringBuilder s = new StringBuilder(46);
		s.append(getName());
		s.append(" v");
		s.append(version);
		s.append(" by ");
		s.append(getAuthors());
		return s.toString();
	}
}

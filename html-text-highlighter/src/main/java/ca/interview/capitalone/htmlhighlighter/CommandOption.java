package ca.interview.capitalone.htmlhighlighter;

/**
 * The Enum CommandOption.
 */
public enum CommandOption 
{
	HTML_ARG("html"),
	HTMLPATH_ARG("hpath"),
	TXT_ARG("txt"),
	TXTPATH_ARG("tpath"),
	HELP_ARG("help")
	;
	
	public String val()
	{
		return s;
	}
	
	private CommandOption(String s)
	{
		this.s = s;
	}
	
	private final String s;
}

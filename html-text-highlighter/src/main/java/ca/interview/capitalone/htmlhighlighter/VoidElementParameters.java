package ca.interview.capitalone.htmlhighlighter;

public enum VoidElementParameters
{
	DOCTYPE("!doctype"),
	COMMENT("!--"),
	AREA("area"),
	BASE("base"),
	BR("br"),
	COL("col"),
	COMMAND("command"),
	EMBED("embed"),
	HR("hr"),
	IMG("img"),
	INPUT("input"),
	KEYGEN("keygen"),
	LINK("link"),
	MENUITEM("menuitem"),
	META("meta"),
	PARAM("param"),
	SOURCE("source"),
	TRACK("track"),
	WBR("wbr")
	;
	public String val()
	{
		return s;
	}
	
	private VoidElementParameters(String s)
	{
		this.s = s;
	}
	
	private final String s;
}

package ca.interview.capitalone.htmlhighlighter;

public enum HtmlElementParameters 
{
	HTML("html"),
	HEAD("head"),
	TITLE("title"),
	H1("h1"),
	H2("h2"),
	H3("h3"),
	BODY("body"),
	P("p"),
	A("a")
	;
	
	public String val()
	{
		return s;
	}
	
	private HtmlElementParameters(String s)
	{
		this.s = s;
	}
	
	private final String s;
}

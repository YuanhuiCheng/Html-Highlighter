package ca.interview.capitalone.htmlhighlighter;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * HTML Highlighter used to generate an output file with corresponding color tags added 
 * to the original html file (with escape codes for the colors applied)
 * 
 * Using for command line:
 * The directory of the html file to be highlighted (not required, if don't specify, it would
 * use input.html under the project as the default input html file)
 * - 
 * The directory of the txt file to be generated (not required, if don't specify, it would 
 * generate a file that uses the same name as the input file except the file format in the 
 * same directory of the input file)
 * - 
 * @author yuanhuicheng
 * @version 1.8
 * @since 2018-2-24
 */
public class Builder 
{
    public static void main(String[] args) throws IOException
    {
    	CommandParser parser = new CommandParser(args);
    	
    	HtmlHighlighter highlighter = new HtmlHighlighter();
    	try 
    	{
			highlighter.highlightHtml(parser.getHtmlPath(), parser.getTxtPath());
		} 
        catch (CommandParserException e) 
    	{
			if (e.getLocalizedMessage() == null)
			{
				logger.error("The directory of the HTML file or TXT file is wrong");
			}
			else
			{
				logger.error(e.getLocalizedMessage());
			}
			
			System.exit(-1);
		}
    }
    
    /**
	 * Instantiates a new builder.  Other programs would not call the object.
	 */
    private Builder()
    {
    	//Do nothing
    }
    
    private static final Logger logger = LogManager.getLogger(Builder.class);
}


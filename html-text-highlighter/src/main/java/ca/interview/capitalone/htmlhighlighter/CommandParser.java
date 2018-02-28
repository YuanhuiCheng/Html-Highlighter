package ca.interview.capitalone.htmlhighlighter;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * The Class CommandParser.
 */
public class CommandParser 
{
	public CommandParser(String[] args)
	{
		if (args == null)
		{
			this.args = new String[0];
		}
		else
		{
			this.args = Arrays.copyOf(args, args.length);
		}
	}
	
	/**
	 * Instantiates the path of input html file 
	 * @throws CommandParserException
	 */
	private void initHtmlPath() throws CommandParserException
	{
		try 
		{
			CommandLine cmdline = parseCommands(args);
			
			if (cmdline.hasOption(CommandOption.HTMLPATH_ARG.val()))
			{
				htmlPath = cmdline.getOptionValue(CommandOption.HTMLPATH_ARG.val());
			}
			
			
			if (htmlPath == null)
			{
				htmlPath = "input.html";
			}
			
			File file = new File(htmlPath);
			if (!file.canRead())
			{
				throw new CommandParserException(htmlPath + " not readable, possibly the directory is wrong.");
			}
		} 
		catch (ParseException e)
		{
			printHelp();
			throw new CommandParserException("The arguments are invalid", e);
		}
	}
	
	/**
	 * Instantiates the path of output txt file 
	 * @throws CommandParserException 
	 */
	private void initTxtPath() throws CommandParserException
	{
		try 
		{
			CommandLine cmdline = parseCommands(args);
			
			if (cmdline.hasOption(CommandOption.TXTPATH_ARG.val()))
			{
				txtPath = cmdline.getOptionValue(CommandOption.TXTPATH_ARG.val());
			}
			
			if (txtPath == null)
			{
				txtPath = getHtmlPath().replace(getHtmlPath().substring(getHtmlPath().lastIndexOf('.')), ".txt");
			}
			
			if (!txtPath.endsWith(TXT_FORMAT))
			{
				txtPath += TXT_FORMAT;
			}
		} 
		catch (ParseException e) 
		{
			printHelp();
			throw new CommandParserException("The arguments are invalid", e);
		}
	}
	
	/**
	 * Parser the commands input by user
	 * @param args the args input by user
	 * @return the command line
	 * @throws ParseException
	 */
	private CommandLine parseCommands(final String args[]) throws ParseException
	{
		CommandLineParser parser = new DefaultParser();
		Options option = createOptions();
		return parser.parse(option, args);
	}
	
	private Options createOptions()
	{
		Options options = new Options();
		options.addOption(CommandOption.HTML_ARG.val(), CommandOption.HTMLPATH_ARG.val(), true, "The path of the html file is nor required");
		options.addOption(CommandOption.TXT_ARG.val(), CommandOption.TXTPATH_ARG.val(), true, "The path of the txt file is not required");
	
		return options;
	}
	
	/**
	 * Print the help
	 */
	private void printHelp()
	{
		Options option = createOptions();
		new HelpFormatter().printHelp("Html Highlighter", option);
	}
	
	/**
	 * Gets the path of input html file
	 * @return the html path
	 * @throws CommandParserException
	 */
	public String getHtmlPath() throws CommandParserException
	{
		if (htmlPath == null)
		{
			initHtmlPath();
		}
		
		return htmlPath;
	}
	
	/**
	 * Gets the path of output html file
	 * @return the txt path
	 * @throws CommandParserException
	 */
	public String getTxtPath() throws CommandParserException
	{
		if (txtPath == null)
		{
			initTxtPath();
		}
		
		return txtPath;
	}
	
	private String htmlPath;
	private String txtPath;
	
	private static final String TXT_FORMAT = ".txt";
	private String[] args = null;
}

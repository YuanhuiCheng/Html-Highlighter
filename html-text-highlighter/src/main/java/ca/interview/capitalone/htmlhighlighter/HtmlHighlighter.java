package ca.interview.capitalone.htmlhighlighter;

import java.awt.Color;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HtmlHighlighter 
{
	/**
	 * The method to be called for parsing an input html file and writing to an output text file
	 * @param htmlPath the directory of the input html file
	 * @param txtPath the directory of the output text file
	 * @throws IOException
	 */
	public void highlightHtml(final String htmlPath, final String txtPath) throws IOException
	{
		final List<String> htmlLines = getHtmlLines(htmlPath);
		final String txtResults = parseHtmlLines(htmlLines);
		
		TxtWriter tw = new TxtWriter(txtPath);
		tw.writeString(txtResults);
		tw.closeBufferedWriter();
		
		final String txtName = txtPath.substring(txtPath.lastIndexOf(SLASH)+1);
		if (txtPath.equals(txtName))
		{
			logger.info(txtName + " has been produced in the directory of " + htmlPath);
		}
		else
		{
			logger.info(txtName + " has been produced in the directory of " + txtPath);
		}
		
	}
	
	/**
	 * Parse html lines to add corresponding color tags
	 * @param htmlLines
	 * @return the parsed html lines as a string
	 */
	private String parseHtmlLines(final List<String> htmlLines)
	{

		StringBuffer sb = new StringBuffer(); //used to append each parsed each line
		StringBuilder sbLine; //used to insert color tag to each line
		Stack<String> htmlElementStack = new Stack<String>(); //stack used to store html elements
		
		//if ifPreviousTagIsClosed == true, the previous tag which has been checked is a closing tag,
		//so if the color tag should be added before the current tag should be determined
		boolean ifPreviousTagIsClosed = false;
		String previousOpeningOrSelfClosingTag = "";
		for (String htmlLine : htmlLines)
		{
			htmlLine = htmlLine.trim(); //get rid of leading and tailing space and comments
			if (htmlLine.isEmpty()) continue;
			sbLine = new StringBuilder(htmlLine);
			int addedIndex = 0;
			
			//new line doesn't start with '<', which means the part is html text enclosed by some tags rather than tags themselves
			if (parseLineStartWithHtmlText(sbLine, htmlElementStack, ifPreviousTagIsClosed))
			{
				addedIndex += COLOR_TAG_LENGTH;
				ifPreviousTagIsClosed = false; //referring to next tag to be checked, the previous tag is not closing tag anymore but color tag
			}
			
			Pattern pa = Pattern.compile(BRACKETS_PATTERN_REGEX); //pattern matching the string stored in angle-shaped brackets, <>
		    Matcher ma = pa.matcher(htmlLine);
		    
		    int previousClosingTagIndexInLine = 0; //the index of previous closing tag within the current line
		    while (ma.find())
		    {
		    	final String rawHtmlTag = ma.group(); //e.g. <a href="http://tech.capitalone.ca">
		    	int startIndex = ma.start() + addedIndex;
		    	int endIndex = ma.end() + addedIndex;
		    	
		    	//the previous tag within the line is a closing tag and next within the line is an opening tag
		    	if (parseTextBetweenCloseAndStartTag(sbLine, htmlElementStack, ifPreviousTagIsClosed, previousOpeningOrSelfClosingTag,
		    						previousClosingTagIndexInLine, startIndex))
		    	{
		    		addedIndex += COLOR_TAG_LENGTH;
    				startIndex += COLOR_TAG_LENGTH;
	    			ifPreviousTagIsClosed = false; //referring to next tag to be checked, the previous tag is not closing tag anymore but color tag
	    			previousOpeningOrSelfClosingTag = htmlElementStack.peek();
		    	}
		    
		    	if (rawHtmlTag.startsWith(CLOSING_LEFT_BRACKET)) //a closing tag, e.g. </p>
		    	{
		    		//get the html tag enclosed by the brackets e.g. </p> -> p
		    		final String htmlElement = getNameOfClosingTag(rawHtmlTag);
		    		
		    		if (parseClosingTagAfterClosingTag(sbLine, htmlElementStack, ifPreviousTagIsClosed,
		    				htmlElement, previousOpeningOrSelfClosingTag, startIndex))
		    		{
		    			addedIndex += COLOR_TAG_LENGTH;
		    		}
		    		
		    		previousClosingTagIndexInLine = endIndex;
		    		ifPreviousTagIsClosed = true;
		    		previousOpeningOrSelfClosingTag = htmlElement;
		    	}
		    	else //opening tag or self-closing tag
		    	{
		    		final String htmlElement = getNameOfOpeningOrVoidTag(rawHtmlTag);
		    		
		    		if (!parseSelfClosingTag(sbLine, htmlElement, previousOpeningOrSelfClosingTag, startIndex)) //opening tag
		    		{		
		    			if (parseOpeningTag(sbLine, htmlElementStack, ifPreviousTagIsClosed, htmlElement, previousOpeningOrSelfClosingTag, startIndex))
		    			{
		    				addedIndex += COLOR_TAG_LENGTH;
		    			}
		    			
		    			ifPreviousTagIsClosed = false;
		    		}
		    		else
		    		{
		    			addedIndex += COLOR_TAG_LENGTH;
		    		}
		    		previousOpeningOrSelfClosingTag = htmlElement;
		    	}
		    }
		    sb.append(sbLine).append("\n");   
		}
		
		return sb.toString();
	}
	
	/**
	 * New line starts with text rather than tags. Requires defining which elements enclose the new line
	 * and adding the corresponding color tag.
	 *e.g.
	 *  <body>
	 *  <p>This is a paragraph.</p>
	 *  There is more text in the body after the paragraph.
	 *  </body>
	 *  
	 * The third line should be converted to '\color[40E0D0]There is more text in the body after the paragraph.'
	 * refers to case1.html, line 10
	 * 
	 * @param sbLine the line to be changed
	 * @param htmlElementStack
	 * @param ifPreviousTagIsClosed
	 * @return true if the passed line starts with text rather than tag and the previous tag is a closing tag,
	 * 		   otherwise false
	 */
	private boolean parseLineStartWithHtmlText(final StringBuilder sbLine, final Stack<String> htmlElementStack, final boolean ifPreviousTagIsClosed)
	{
		if (!htmlElementStack.isEmpty() && !sbLine.toString().startsWith(LEFT_BRACKET) && ifPreviousTagIsClosed)
		{
			sbLine.insert(0, formatColorTag(elementColorMap.get(htmlElementStack.peek())));
			return true;
		}
		return false;
	}
	
	/**
	 * If there is text between a closing tag and a opening tag (two nested elements), 
	 * the text is within the outer scope.  Require defining which elements enclose the 
	 * text and inserting the corresponding color tag at a correct index.
	 * 
	 *e.g. 
	 *	<body>
	 *	This is text in the body.
	 *	<p>This is the first paragraph.</p> There is some text between the heading and paragraph. <p>This is the second paragraph.</p>
	 *  There is more text in the body after the paragraph.
	 *	</body>
	 *
	 * The third line should be converted to 
	 * '\color[A9A9A9]<p>This is the first paragraph.</p>\color[40E0D0] There is some text between the heading and paragraph. \color[A9A9A9]<p>This is the second paragraph.</p>'
	 *  refers to case1.html, line 9
	 *  
	 * @param sbLine the line to be changed
	 * @param htmlElementStack
	 * @param ifPreviousTagIsClosed
	 * @param previousClosingTagIndexInLine the end index of the previous closing tag
	 * @param startIndex the start index of the next opening tag
	 * @return true if there is text between two nested elements 
	 */
	private boolean parseTextBetweenCloseAndStartTag(final StringBuilder sbLine, final Stack<String> htmlElementStack,
					final boolean ifPreviousTagIsClosed, final String previousOpeningOrSelfClosingTag,
					final int previousClosingTagIndexInLine, final int startIndex)
	{
		//e.g. <h1>This is a Heading</h1> some text <p>This is a paragraph.</p>, 
		//the middle tag should be 'some text', then the condition should be true
		final String middleText = sbLine.substring(previousClosingTagIndexInLine, startIndex);
		if (ifPreviousTagIsClosed && !middleText.trim().isEmpty() && !htmlElementStack.isEmpty()
			&& !htmlElementStack.peek().equals(previousOpeningOrSelfClosingTag))
		{
			sbLine.insert(previousClosingTagIndexInLine, formatColorTag(elementColorMap.get(htmlElementStack.peek())));
			return true;
		}
		return false;
	}
	
	/**
	 * If the previous tag of the closing tag is a closing tag as well, the corresponding color tag
	 * should be inserted before the closing tag
	 * 
	 * e.g.
	 * 	<BODY>
	 * 	<H1>Welcome to Capital One</H1>
	 * 	<P><A HREF="http://tech.capitalone.ca">Visit Our Blog</A></P>
	 * 	</BODY>
	 * 
	 * The last line should be converted to 
	 * '\color[40E0D0]</BODY>'
	 * refers to case2.html, line 5
	 * 
	 * @param sbLine the line to be changed
	 * @param htmlElementStack
	 * @param ifPreviousTagIsClosed
	 * @param htmlElement
	 * @param startIndex the start index of the current tag
	 * @return true if current tag and previous tag are both closing tags,
	 * 		so colorTag should be added before the current tag
	 */
	private boolean parseClosingTagAfterClosingTag(final StringBuilder sbLine, final Stack<String> htmlElementStack,
				final boolean ifPreviousTagIsClosed, final String htmlElement, final String previousOpeningOrSelfClosingTag,
				final int startIndex)
	{
		if (!htmlElementStack.isEmpty() && htmlElementStack.peek().equals(htmlElement)) //meets the corresponding closing tag
		{
			htmlElementStack.pop(); //pop the tag in the stack when meeting the corresponding closing tag
		
			/**
			 * The following condition applies when ifPreviousTagIsClosed == false
			 * e.g.
			 * <body>
			 * This is text in the body.
			 * <h1>This is a heading</h1> 
			 * There is more text in the body after the paragraph.
			 * </body>
			 * no need to add color tag before </body> since the previous element is also nested by <body>, so 
			 * corresponding color tag has already added before the previous element, which indicates ifPreviousTagIsClosed == false
			 * 
			 * The following condition applies when ifPreviousTagIsClosed == true
			 * e.g.
			 * <body>
			 * <h1>This is a heading</h1>
			 * </body>
			 * required adding color tag before </body> since the previous element is nested by some other html tag
			 */
			if (ifPreviousTagIsClosed && !htmlElement.equals(previousOpeningOrSelfClosingTag))
    		{
				sbLine.insert(startIndex, formatColorTag(elementColorMap.get(htmlElement)));
				return true;
    		}
		}
		else //corresponding close tag not found, there is something wrong with input html format
		{
			logger.warning("The previous opening tag is " + LEFT_BRACKET + htmlElementStack.peek() + RIGHT_BRACKET +
							"; however, the closing tag is " + LEFT_BRACKET + htmlElement + RIGHT_BRACKET + ", which does not match");
		}
		return false;
	}
	
	/**
	 * If it is found that voidElementColorMap contains the html tag name as a key, the tag is a self-closing tag
	 * (void element), so the corresponding color tag must be added before the tag.
	 * @param sbLine the line to be changed
	 * @param htmlElement 
	 * @param startIndex the position to insert the colorTag, which is the start index of the self-closing tag
	 * @return true if the tag is a self-closing tag, otherwise false
	 */
	private boolean parseSelfClosingTag(final StringBuilder sbLine, final String htmlElement, 
								final String previousOpeningOrSelfClosingTag, final int startIndex)
	{
		if (voidElementColorMap.containsKey(htmlElement)) //self-closing tag
		{
			if (!htmlElement.equals(previousOpeningOrSelfClosingTag))
			{
				sbLine.insert(startIndex, formatColorTag(voidElementColorMap.get(htmlElement)));
			}
			
			return true;
		}
		return false;
	}
	
	/**
	 * Parse the opening tag.  If it is found an opening tag, the color tag must be added before it.
	 * If no corresponding html tag as a key is found in elementColorMap, using generateColor() to 
	 * generate a color which has not existed in elementColorMap and voidElementColorMap, then 
	 * push the html tag as a key, and color as a value to elemtnColorMap
	 * @param sbLine the line to be changed
	 * @param htmlElementStack
	 * @param htmlElement
	 * @param startIndex the start index of the opening tag
	 */
	private boolean parseOpeningTag(final StringBuilder sbLine, final Stack<String> htmlElementStack,
			final boolean ifPreviousTagIsClosed, final String htmlElement, final String previousOpeningOrSelfClosingTag, int startIndex)
	{
		htmlElementStack.push(htmlElement); //push the name of opening tag to htmlElementStack
		if (!elementColorMap.containsKey(htmlElement)) //if the map does not contain the found html tag, add the tag and newly generated corresponding color tag to the map
		{
			String newColor = "";
			do
			{
				newColor = generateColor();
			}
			while(elementColorMap.containsValue(newColor) || voidElementColorMap.containsValue(newColor));
			
			elementColorMap.put(htmlElement, newColor);
		}
		if (!htmlElement.equals(previousOpeningOrSelfClosingTag))
		{
			sbLine.insert(startIndex, formatColorTag(elementColorMap.get(htmlElement)));
			return true;
		}
		return false;
	}
	
	/**
	 * get the name of a closing tag
	 * @param rawHtmlTag, e.g. <p>
	 * @return the name of the html tag, e.g. p
	 */
	private String getNameOfClosingTag(final String rawHtmlTag)
	{
		return rawHtmlTag.substring(rawHtmlTag.indexOf(LEFT_BRACKET)+2, rawHtmlTag.indexOf(RIGHT_BRACKET)).trim().toLowerCase();
	}
	
	/**
	 * get the name of a opening tag or a self-closing tag
	 * @param rawHtmlTag, e.g. <a href="http://tech.capitalone.ca">, </br>
	 * @return the name of the html tag except the attributes e.g. a, br
	 */
	private String getNameOfOpeningOrVoidTag(final String rawHtmlTag)
	{
		String htmlElement = rawHtmlTag.substring(rawHtmlTag.indexOf(LEFT_BRACKET)+1, rawHtmlTag.indexOf(RIGHT_BRACKET)).trim().toLowerCase().replace(SLASH, "");
		return htmlElement.contains(" ")? htmlElement.substring(0, htmlElement.indexOf(" ")) : htmlElement; //get rid of the attributes specified in the tag
	}
	
	/**
	 * generate color for those html elements that are not defined in elementColorMap
	 * @return hex color string
	 */
	private String generateColor()
	{
		Random rand = new Random();
		float r = rand.nextFloat();
		float g = rand.nextFloat();
		float b = rand.nextFloat();
		Color randomColor = new Color(r, g, b);
		randomColor = randomColor.brighter();
		return String.format("%02X%02X%02X", randomColor.getRed(), randomColor.getGreen(), randomColor.getBlue());
	}
	
	/**
	 * get a list of lines generated from the html file to be converted 
	 * @param htmlPath	the path of the input html file
	 * @return	a list of lines of the html file
	 * @throws IOException
	 */
	private List<String> getHtmlLines(final String htmlPath) throws IOException
	{
		List<String> htmlLines = new ArrayList<String>();
		try(Stream<String> stream = Files.lines(Paths.get(htmlPath), StandardCharsets.UTF_8))
		{
			htmlLines = stream.collect(Collectors.toList());
		}
		catch (IOException e) 
		{
			throw new IOException(htmlPath + " cannot be properly read");
		}
		
		return htmlLines;
	}
	
	/**
	 * create formatted color tag, e.g.\color[FF0000], \color[FFFF00]
	 * @param color
	 * @return formatted color tag
	 */
	private static String formatColorTag(final String color)
	{
		return String.format(COLOR_TAG_PATTERN_REGEX, color);
	}
	
	private static final int COLOR_TAG_LENGTH = "\\color[]".length() + 6;
	private static final String LEFT_BRACKET = "<";
	private static final String RIGHT_BRACKET = ">";
	private static final String CLOSING_LEFT_BRACKET = "</";
	private static final String SLASH = "/";
	private static final String BRACKETS_PATTERN_REGEX = "\\<(.*?)\\>";
	private static final String COLOR_TAG_PATTERN_REGEX = "\\color[%s]";
	private static final Logger logger = Logger.getLogger(HtmlHighlighter.class.getName());

	/**
	 * store key -> in elementColorMap, commonly-used html elements, 
	 * 				in voidElementColorMap, commonly-used self-closing elements
	 * 		 value -> corresponding color tag in maps
	 */
	static Map<String, String> elementColorMap = new HashMap<String, String>();
	static Map<String, String> voidElementColorMap = new HashMap<String, String>();
	static																	          
	{																		    		
		elementColorMap.put(HtmlElementParameters.HTML.val(), "FF0000");
		elementColorMap.put(HtmlElementParameters.HEAD.val(), "FFFF00");
		elementColorMap.put(HtmlElementParameters.TITLE.val(), "008000");
		elementColorMap.put(HtmlElementParameters.H1.val(), "006400");
		elementColorMap.put(HtmlElementParameters.H2.val(), "3CB371");
		elementColorMap.put(HtmlElementParameters.H3.val(), "2E8B57");
		elementColorMap.put(HtmlElementParameters.BODY.val(), "40E0D0");
		elementColorMap.put(HtmlElementParameters.P.val(), "A9A9A9");
		elementColorMap.put(HtmlElementParameters.A.val(), "0000FF");
		
		voidElementColorMap.put(VoidElementParameters.DOCTYPE.val(), "B0C4DE");
		voidElementColorMap.put(VoidElementParameters.COMMENT.val(), "FFFFFF");
		voidElementColorMap.put(VoidElementParameters.AREA.val(), "FFB6C1");
		voidElementColorMap.put(VoidElementParameters.BASE.val(), "FF69B4");
		voidElementColorMap.put(VoidElementParameters.BR.val(), "FFC0CB");
		voidElementColorMap.put(VoidElementParameters.COL.val(), "FF1493");
		voidElementColorMap.put(VoidElementParameters.COMMAND.val(), "DB7093");
		voidElementColorMap.put(VoidElementParameters.EMBED.val(), "C71585");
		voidElementColorMap.put(VoidElementParameters.HR.val(), "FF4500");
		voidElementColorMap.put(VoidElementParameters.IMG.val(), "00BFFF");
		voidElementColorMap.put(VoidElementParameters.INPUT.val(), "1E90FF");
		voidElementColorMap.put(VoidElementParameters.KEYGEN.val(), "7FFFD4");
		voidElementColorMap.put(VoidElementParameters.LINK.val(), "008080");
		voidElementColorMap.put(VoidElementParameters.MENUITEM.val(), "B8860B");
		voidElementColorMap.put(VoidElementParameters.META.val(), "A52A2A");
		voidElementColorMap.put(VoidElementParameters.PARAM.val(), "BDB76B");
		voidElementColorMap.put(VoidElementParameters.SOURCE.val(), "DCDCDC");
		voidElementColorMap.put(VoidElementParameters.TRACK.val(), "FAEBD7");
		voidElementColorMap.put(VoidElementParameters.WBR.val(), "FFFACD");
	}
}

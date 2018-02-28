package ca.interview.capitalone.htmlhighlighter;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

public class HtmlHighlighterTest 
{
   @Before
   public void initialize() throws IOException
   {
	   highlighter = new HtmlHighlighter();
   }
   
   @Test
   public void isCaseOneWork() throws IOException
   {
	   final String htmlFile1 = relativePath + "case1.html";
	   final String txtFile1 = relativePath + "actualCase1.txt";
	   final String expectedTxtFile1 = relativePath + "expectedCase1.txt";
	   
	   assertEqualsAfterHighlight(htmlFile1, txtFile1, expectedTxtFile1);
   }
   
   @Test
   public void isCaseTwoWork() throws IOException
   {
	   
	   final String htmlFile2 = relativePath + "case2.html";
	   final String txtFile2 = relativePath + "actualCase2.txt";
	   final String expectedTxtFile2 = relativePath + "expectedCase2.txt";
	   
	   assertEqualsAfterHighlight(htmlFile2, txtFile2, expectedTxtFile2);
   }
   
   private void assertEqualsAfterHighlight(final String htmlFile, final String txtFile,
		   							  final String expectedTxtFile) throws IOException
   {
	   highlighter.highlightHtml(htmlFile, txtFile);
	   assertEquals("The files differ!", 
			   new String(Files.readAllBytes(Paths.get(expectedTxtFile))), 
			   new String(Files.readAllBytes(Paths.get(txtFile))));
   }
   
   
   private static final String relativePath = "src/test/resources/";
   
   private HtmlHighlighter highlighter = null;
}

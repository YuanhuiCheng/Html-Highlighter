package ca.interview.capitalone.htmlhighlighter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TxtWriter
{
	public TxtWriter(final String txtPath) throws IOException
	{
		File file = new File(txtPath);
		if (!file.exists()) //if the output file does not exist, creating a new one
		{
			try
			{
				file.createNewFile();
			}
			catch (IOException e)
			{
				throw new IOException("The .txt file cannot be properly generated");
			}
		}
		
		try
		{
			Path path = Paths.get(txtPath);
			bw = Files.newBufferedWriter(path);
		}
		catch (IOException e)
		{
			throw new IOException("The instance of .txt file can not be properly generated.",e);
		}
	}
	
	public void writeString(final String textString) throws IOException
	{
		try
		{
			bw.write(textString);
		}
		catch(IOException e)
		{
			throw new IOException("String are not properly wrote to the txt file.", e);
		}
	}
	
	/**
	 * Close buffered writer.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void closeBufferedWriter() throws IOException
	{
		try
		{
			bw.flush();
			bw.close();
		} 
		catch (IOException e) 
		{
			throw new IOException("The buffered writer cannot be properly closed.", e);
		}
	}
	
	private BufferedWriter bw;
}

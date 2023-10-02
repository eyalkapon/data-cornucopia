package uk.co.devworx.chars;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CharsetDetectorTest
{
	@Test
	public void testCharset() throws Exception
	{
		Path filePath = Paths.get("src/test/resources/Scenario-16-Windows-Charset.csv");
		// Create a metadata object to hold the detected metadata
		Metadata metadata = new Metadata();
		CharsetDetector detector = new CharsetDetector();
		detector.setText(Files.readAllBytes(filePath));
		CharsetMatch[] allCharsets = detector.detectAll();
		for (CharsetMatch c : allCharsets)
		{
			System.out.println(c);
		}
	}

}

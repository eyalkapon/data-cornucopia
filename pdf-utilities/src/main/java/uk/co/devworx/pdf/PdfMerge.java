package uk.co.devworx.pdf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * A simple class that merges a number of PDF documents into a single
 * document
 */
public class PdfMerge
{
	private static final Logger logger = LogManager.getLogger(PdfMerge.class);

	/**
	 * Merges all the PDF files in the given directory
	 *
	 * @param outputPdf
	 */
	public static void MergePDFFilesInDirectory(Path outputPdf, Path directory) throws IOException
	{
		Objects.requireNonNull(outputPdf, "You have to specify a valid output PDF");
		Objects.requireNonNull(directory, "You have to specify a directory");

		if(Files.isDirectory(directory) == false) throw new IllegalArgumentException("You have to specify a directory - " + directory.toAbsolutePath() + " - is not a directory;");

		final List<Path> dirs = new ArrayList<>();
		Files.walkFileTree(directory, new SimpleFileVisitor<Path>()
		{
			@Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
			{
				if(file.getFileName().toString().endsWith(".pdf"))
				{
					dirs.add(file);
				}
				return FileVisitResult.CONTINUE;
			}
		});

		logger.info("Found a total of " + dirs.size() + " pdf files in : " + directory.toAbsolutePath());

		MergePDFFiles(outputPdf, dirs);
	}


	/**
	 * Merge the given input PDF documents to produce the given output.
	 *
	 * @param outputPdf
	 * @param inputPaths
	 */
	public static void MergePDFFiles(Path outputPdf, Path...  inputPaths) throws IOException
	{
		MergePDFFiles(outputPdf, Arrays.asList(inputPaths));
	}
	/**
	 * Merge the given input PDF documents to produce the given output.
	 *
	 * @param outputPdf
	 * @param inputPaths
	 */
	public static void MergePDFFiles(Path outputPdf, Collection<Path>  inputPaths) throws IOException
	{
		Objects.requireNonNull(outputPdf, "You have to specify a valid output PDF");
		Objects.requireNonNull(inputPaths, "You have to specify a valid number of input");
		if(inputPaths.size() == 0)
		{
			throw new IllegalArgumentException("You need to specify at least 1 input path");
		}

		logger.info("Converting " + inputPaths.size() + " PDF documents to one output : " + outputPdf.toAbsolutePath());
		final List<PDDocument> docsToClose = new ArrayList<>();
		try(final PDDocument output = new PDDocument())
		{
			for (Path p : inputPaths)
			{
				logger.info("Merging in : " + p.getFileName());
				PDDocument document = PDDocument.load(p.toFile());

				int numberOfPages = document.getNumberOfPages();
				for (int j = 0; j < numberOfPages; j++)
				{
					output.addPage(document.getPage(j));
				}
				docsToClose.add(document);
			}

			logger.info("Saving  : " + outputPdf.toAbsolutePath() + " | Total Pages : " + output.getNumberOfPages());
			output.save(outputPdf.toFile());
		}
		finally
		{
			for(PDDocument doc : docsToClose)
			{
				try
				{
					doc.close();
				}
				catch(Exception e)
				{
					logger.warn("unable to close the document : " + e + " | Nothing to do, so carrying on.");
				}
			}
		}
	}
	

	public static void main(String... args) throws Exception
	{
		if(args.length < 2)
		{
			throw new IllegalArgumentException("You must specify 2 arguments: \n" +
											   "1) The Output PDF to produce. \n" +
											   "2) The Directory of PDF files to merge.");
		}

		Path p1 = Paths.get(args[0]);
		Path p2 = Paths.get(args[1]);

		PdfMerge.MergePDFFilesInDirectory(p1, p2);

		System.exit(0);
	}


	
}


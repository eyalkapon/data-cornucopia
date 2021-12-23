package uk.co.devworx.etcetera;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.*;

/**
 * A class that will watch a directory (recursively) and will copy any new file that appears over to another directory.
 * Useful for debugging processes that create files on the system.
 */
public class RecursiveFileWatcherCopier
{
	private static final Logger logger = LogManager.getLogger(RecursiveFileWatcherCopier.class);

	public static void main(String... args) throws Exception
	{
		if(args.length != 2)
		{
			throw new RuntimeException("You need to specify at least two paths. \n : " +
									   "1) The directory / path that needs to be watched. \n"  +
									   "2) The directory / path which to place the deltas for how things update.");
		}

		final Path pathToWatch = Paths.get(args[0]);
		final Path outputPath = Paths.get(args[1]);

		logger.info("Path to Watch : " + pathToWatch);
		logger.info("Output Path : " + outputPath);

		RecursiveFileWatcherCopier copier = new RecursiveFileWatcherCopier(pathToWatch, outputPath);
		copier.startWatchCopy();

	}

	private final Path pathToWatch;
	private final Path outputPath;
	private final FileSystem fileSystem;
	private final IdentityHashMap<WatchService, Path> watchServicesToPath;

	public RecursiveFileWatcherCopier(Path pathToWatch, Path outputPath)
	{
		this.pathToWatch = pathToWatch;
		this.outputPath = outputPath;
		fileSystem = pathToWatch.getFileSystem();


		if(Files.exists(pathToWatch) == false || Files.isDirectory(pathToWatch) == false)
		{
			throw new RuntimeException("The path to watch you have specified - " + pathToWatch.toAbsolutePath() + " - does not exist.");
		}

		if(Files.exists(outputPath) == true && Files.isDirectory(outputPath) == false)
		{
			throw new RuntimeException("The output path you have specified - " + outputPath.toAbsolutePath() + " - exists, but is not a directory.");
		}

		if(Files.exists(outputPath) == false)
		{
			try
			{
				Files.createDirectories(outputPath);
			}
			catch(Exception e)
			{
				throw new RuntimeException("Could not create the output path directory : " + outputPath.toAbsolutePath() + " - exception was : " + e);
			}
		}

		watchServicesToPath = new IdentityHashMap<>();
	}

	private void startWatchCopy()
	{
		logger.info("Starting the Watcher.");

		try
		{
			//Bootstrap
			final WatchService w1 = fileSystem.newWatchService();
			watchServicesToPath.put(w1, pathToWatch);
			pathToWatch.register(w1,  StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);

			while(true)
			{
				final Set<Map.Entry<WatchService, Path>> entries = watchServicesToPath.entrySet();
				for(Map.Entry<WatchService, Path> e : entries)
				{
					final WatchService ws = e.getKey();
					final Path path = e.getValue();

					final WatchKey key = ws.poll();
					if(key == null) continue;

					final List<WatchEvent<?>> watchEvents = key.pollEvents();
					logger.info("Processing - " + watchEvents.size() + " events");
					Set<Path> pathsToCopy = new LinkedHashSet<>();

					for (WatchEvent<?> event : watchEvents )
					{
						final WatchEvent.Kind<?> kind = event.kind();
						final Object context = event.context();
						if(context instanceof Path == false)
						{
							logger.warn("Unknown event Context - " + context + " - skipping");
							continue;
						}

						Path modifiedPath = (Path)(context);
						modifiedPath = path.resolve(modifiedPath);

						if(kind.equals(StandardWatchEventKinds.ENTRY_CREATE) && Files.isDirectory(modifiedPath))
						{
							logger.info("New Directory Created : " + modifiedPath + " - Will start watching that also");
							final WatchService w2 = fileSystem.newWatchService();
							watchServicesToPath.put(w2, modifiedPath);
							modifiedPath.register(w2, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
						}

						logger.info("Modified Path - " + modifiedPath.toAbsolutePath() + " - Kind : " + kind);
						pathsToCopy.add(modifiedPath);
					}

					logger.info("Create Copies of " + pathsToCopy.size() + " paths");

					createPathCopies(pathsToCopy);

					boolean valid = key.reset();
					logger.info("Done Processing - " + watchEvents.size() + " events - Key has been reset: " + valid);

				}

				Thread.yield();
				Thread.sleep(0);
			}

		}
		catch(IOException | InterruptedException e)
		{
			throw new RuntimeException("Could not execute the runner service : " + e, e);
		}
	}

	private void createPathCopies(Set<Path> pathsToCopy) throws IOException, InterruptedException
	{
		String fullOrigin = pathToWatch.toAbsolutePath().toString();

		for(Path p : pathsToCopy)
		{
			String subjectFull = p.toAbsolutePath().toString();

			if(Files.exists(p) == false)
			{
				logger.info("Skipping the file - as this no longer exists : " + subjectFull);
				continue;
			}

			String subPathName = subjectFull.substring(fullOrigin.length() + 1);

			String newNamePrefix = getInstantPrefix();
			Path newPath = outputPath.resolve(subPathName);
			String fileName = newNamePrefix + newPath.getFileName();

			newPath = newPath.getParent().resolve(fileName);

			logger.info("New Path Full : " + newPath);

			final boolean originIsDir = Files.isDirectory(Paths.get(fullOrigin));
			logger.info("Origin is Directory : " + originIsDir);

			if(originIsDir == true)
			{
				copyRecursive(p, newPath);
			}
			else
			{
				copySingular(p, newPath);
			}

			Thread.sleep(1); //So we don't have clashes !
		}

	}

	private void copySingular(Path source, Path target) throws IOException
	{
		if(Files.exists(source) == false)
		{
			logger.info("Skipping the file - as this no longer exists : " + source);
			return;
		}

		final Path parentDir = target.getParent();
		if(Files.exists(parentDir) == false)
		{
			Files.createDirectories(parentDir);
		}

		Files.copy(source, target);
	}

	private void copyRecursive(Path source, Path targetDir) throws IOException
	{
		if(Files.exists(targetDir) == false)
		{
			Files.createDirectories(targetDir);
		}

		Files.walkFileTree(source, new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
			{
				String subPath = source.relativize(file).toString();
				Path target = targetDir.resolve(  subPath );
				if(Files.exists(target.getParent()) == false )
				{
					Files.createDirectories(target.getParent());
				}
				Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
			{
				return FileVisitResult.CONTINUE;
			}

		});

	}

	private String getInstantPrefix()
	{
		String prefix = Instant.now().toString();
		prefix = prefix.replace(":", "_");
		prefix = prefix.replace(" ", "_");
		prefix = prefix.replace(".", "_");
		return prefix + "_";
	}


}





















/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2021 - 2022 The Institut Pasteur.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package fiji.plugin.trackmate.ctc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.scijava.Context;

import com.opencsv.CSVWriter;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.action.CTCExporter;
import fiji.plugin.trackmate.action.CTCExporter.ExportType;
import fiji.plugin.trackmate.performance.spt.MetricsRunner;

public class CTCMetricsRunner extends MetricsRunner
{

	/**
	 * Logger to supervise the batch.
	 */
	private final Logger batchLogger = Logger.DEFAULT_LOGGER;

	/**
	 * Logger to pass to TrackMate instances.
	 */
	private final Logger trackmateLogger = Logger.VOID_LOGGER;

	/**
	 * CTC processor instance.
	 */
	private final CTCMetricsProcessor ctc;

	/**
	 * Path to ground truth folder.
	 */
	private final String gtPath;

	public CTCMetricsRunner( final String gtPath, final Context context )
	{
		super( Paths.get( gtPath ).getParent(), "SPTMetrics" );
		this.gtPath = gtPath;
		final int logLevel = 0; // silence CTC logging.
		this.ctc = new CTCMetricsProcessor( context, logLevel );
	}

	@Override
	public void performMetricsMeasurements( final TrackMate trackmate, final double detectionTiming, final double trackingTiming )
	{
		batchLogger.log( "Exporting as CTC results.\n" );
		final Settings settings = trackmate.getSettings();
		final File csvFile = findSuitableCSVFile( settings );
		final String[] csvHeader1 = toCSVHeader( settings );

		final int id = CTCExporter.getAvailableDatasetID( resultsRootPath.toString() );
		final String resultsFolder = CTCExporter.getExportTrackingDataPath( resultsRootPath.toString(), id, ExportType.RESULTS, trackmate );
		try
		{
			// Export to CTC files.
			CTCExporter.exportTrackingData( resultsRootPath.toString(), id, ExportType.RESULTS, trackmate, trackmateLogger );

			// Perform CTC measurements.
			batchLogger.log( "Performing CTC metrics measurements.\n" );
			final CTCMetrics m = ctc.process( gtPath, resultsFolder );
			// Add timing measurements.
			final CTCMetrics metrics = m.copyEdit()
					.detectionTime( detectionTiming )
					.trackingTime( trackingTiming )
					.tim( detectionTiming + trackingTiming )
					.get();
			batchLogger.log( "CTC metrics:\n" );
			batchLogger.log( metrics.toString() + '\n' );

			// Write to CSV.
			final String[] line1 = toCSVLine( settings, csvHeader1 );
			final String[] line = metrics.concatWithCSVLine( line1 );

			try (CSVWriter csvWriter = new CSVWriter( new FileWriter( csvFile, true ),
					CSVWriter.DEFAULT_SEPARATOR,
					CSVWriter.NO_QUOTE_CHARACTER,
					CSVWriter.DEFAULT_ESCAPE_CHARACTER,
					CSVWriter.DEFAULT_LINE_END ))
			{
				csvWriter.writeNext( line );
			}

		}
		catch ( final IOException | IllegalArgumentException e )
		{
			batchLogger.error( "Could not export tracking data to CTC files:\n" + e.getMessage() + '\n' );
			// Write default values to CSV.
			final String[] line1 = toCSVLine( settings, csvHeader1 );
			final CTCMetrics metrics = CTCMetrics.create()
					.seg( Double.NaN )
					.tra( Double.NaN )
					.det( Double.NaN )
					.ct( Double.NaN )
					.tf( Double.NaN )
					.bci( Double.NaN )
					.cca( Double.NaN )
					.tim( Double.NaN )
					.detectionTime( Double.NaN )
					.trackingTime( Double.NaN )
					.get();
			final String[] line = metrics.concatWithCSVLine( line1 );
			try (CSVWriter csvWriter = new CSVWriter( new FileWriter( csvFile, true ),
					CSVWriter.DEFAULT_SEPARATOR,
					CSVWriter.NO_QUOTE_CHARACTER,
					CSVWriter.DEFAULT_ESCAPE_CHARACTER,
					CSVWriter.DEFAULT_LINE_END ))
			{
				csvWriter.writeNext( line );
			}
			catch ( final IOException e1 )
			{
				batchLogger.error( "Could not write failed results to CSV file:\n" + e1.getMessage() + '\n' );
				e1.printStackTrace();
			}
		}
		finally
		{
			try
			{
				// Delete CTC export folder.
				deleteFolder( resultsFolder );
			}
			catch ( final RuntimeException e )
			{
				batchLogger.error( "Failed to delete CTC export folder: " + resultsFolder + "\n"
						+ "Please delete it manually later.\n" );
			}
		}
	}

	private static final void deleteFolder( final String folder )
	{
		final Path path = Paths.get( folder );
		try
		{
			Files.walkFileTree( path, new SimpleFileVisitor< Path >()
			{
				@Override
				public FileVisitResult visitFile( final Path file, final BasicFileAttributes attrs ) throws IOException
				{
					Files.delete( file );
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory( final Path dir, final IOException e ) throws IOException
				{
					if ( e == null )
					{
						Files.delete( dir );
						return FileVisitResult.CONTINUE;
					}
					throw e;
				}
			} );
		}
		catch ( final IOException e )
		{
			throw new RuntimeException( "Failed to delete " + path, e );
		}
	}
}

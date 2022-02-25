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
package fiji.plugin.trackmate.batcher.ui;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BatcherModelIO
{

	private static File defaultSaveFile = new File( new File( System.getProperty( "user.home" ), ".trackmate" ), "trackmatebatchersettings.json" );

	public static void saveTo( final File modelFile, final BatcherModel model )
	{
		final String str = toJson( model );

		if ( !modelFile.exists() )
			modelFile.getParentFile().mkdirs();

		try (FileWriter writer = new FileWriter( modelFile ))
		{
			writer.append( str );
		}
		catch ( final IOException e )
		{
			System.err.println( "Could not write the settings to " + modelFile );
			e.printStackTrace();
		}
	}

	public static void saveToDefault( final BatcherModel model )
	{
		saveTo( defaultSaveFile, model );
	}

	public static BatcherModel readFrom( final File modelFile )
	{
		if ( !modelFile.exists() )
		{
			final BatcherModel model = new BatcherModel();
			saveTo( modelFile, model );
			return model;
		}

		try (FileReader reader = new FileReader( modelFile ))
		{
			final String str = Files.lines( Paths.get( modelFile.getAbsolutePath() ) )
					.collect( Collectors.joining( System.lineSeparator() ) );

			return fromJson( str );
		}
		catch ( final IOException e )
		{}
		return new BatcherModel();
	}

	public static BatcherModel readFromDefault()
	{
		return readFrom( defaultSaveFile );
	}

	private static Gson getGson()
	{
		final GsonBuilder builder = new GsonBuilder();
		return builder.setPrettyPrinting().create();
	}

	public static String toJson( final BatcherModel model )
	{
		return getGson().toJson( model );
	}

	public static BatcherModel fromJson( final String str )
	{
		final BatcherModel model = getGson().fromJson( str, BatcherModel.class );
		return model;
	}
}
package fiji.plugin.trackmate.ctc;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import fiji.plugin.trackmate.ctc.ui.ParameterSweepController;
import fiji.plugin.trackmate.gui.Fonts;
import fiji.plugin.trackmate.gui.Icons;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;

public class TrackMateParameterSweepPlugin implements PlugIn
{

	private ParameterSweepController controller;

	@Override
	public void run( final String arg )
	{
		if ( arg != null && !arg.isEmpty() )
		{
			final List< String > args = Arrays.asList( arg.split( "," ) )
					.stream()
					.map( String::trim )
					.collect( Collectors.toList() );
			if ( args.size() > 1 )
			{
				final ImagePlus imp = IJ.openImage( args.get( 0 ) );
				imp.show();
				final String gtPath = args.get( 1 );
				controller = new ParameterSweepController( imp, gtPath );
				controller.show();
				return;
			}
		}

		final GenericDialogPlus dialog = new GenericDialogPlus( "TrackMate parameter sweep startup" );
		dialog.setIconImage( Icons.TRACKMATE_ICON.getImage() );

		dialog.addMessage( "TrackMate parameter sweep.", Fonts.BIG_FONT );
		dialog.addMessage( "Please select an image to run tracking on." );
		dialog.addImageChoice( "Source image", "" );

		dialog.addMessage( "Please browse to the folder in which the tracking ground-truth is stored." );
		dialog.addMessage( "The results will be written in the parent of this folder." );
		dialog.addMessage( "The ground truth must follow the cell-tracking challenge format. See\n"
				+ "Ulman, et al (2017). An objective comparison of cell-tracking algorithms.\n"
				+ "Nature Methods, 14(12), 1141–1152. https://doi.org/10.1038/nmeth.4473\n"
				+ "for details." );
		dialog.addMessage( "Path to tracking ground-truth for source image" );
		dialog.addDirectoryField( "", System.getProperty( "user.home" ), 50 );

		dialog.showDialog();
		if ( dialog.wasCanceled() )
			return;

		final ImagePlus imp = dialog.getNextImage();
		final String gtPath = dialog.getNextString();

		controller = new ParameterSweepController( imp, gtPath );
		controller.show();
	}

	public ParameterSweepController getController()
	{
		return controller;
	}
}

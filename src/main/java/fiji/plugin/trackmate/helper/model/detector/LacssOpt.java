/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2023 TrackMate developers.
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
package fiji.plugin.trackmate.helper.model.detector;

import java.util.LinkedHashMap;
import java.util.Map;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.detector.lacss.LacssDetectorFactory;
import fiji.plugin.trackmate.detector.lacss.LacssDetectorConfigurationPanel.PretrainedModel;
import fiji.plugin.trackmate.detector.lacss.LacssDetector;
import fiji.plugin.trackmate.detection.DetectorKeys;
import fiji.plugin.trackmate.detection.SpotDetectorFactoryBase;
import fiji.plugin.trackmate.detection.ThresholdDetectorFactory;
import fiji.plugin.trackmate.helper.model.parameter.AbstractParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.BooleanParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.DoubleParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.EnumParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.IntParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.StringRangeParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.NumberParamSweepModel.RangeType;

public class LacssOpt
{

	private LacssOpt()
	{}

	static Map< String, AbstractParamSweepModel< ? > > createModels()
	{
		final EnumParamSweepModel< PretrainedModel > LacssModel = new EnumParamSweepModel<>( PretrainedModel.class )
				.paramName( "Lacss Models" )
				.rangeType( fiji.plugin.trackmate.helper.model.parameter.ArrayParamSweepModel.RangeType.FIXED )
				.addValue( PretrainedModel.Default )
				.addValue( PretrainedModel.CUSTOM)
				.fixedValue( PretrainedModel.Default );
		final StringRangeParamSweepModel lacssCustomModelPath = new StringRangeParamSweepModel()
				.paramName( "Lacss custom model path" )
				.isFile( true )
				.add( System.getProperty( "user.home" ) );	
		final DoubleParamSweepModel min_cell_area = new DoubleParamSweepModel()
				.paramName( "Min Cell Area" )
				.dimension( Dimension.LENGTH )
				.rangeType( RangeType.FIXED )
				.min( 0. )
				.max( 150. );
		//final BooleanParamSweepModel return_label = new BooleanParamSweepModel()
		//		.paramName( "return_label" )
		//		.rangeType( fiji.plugin.trackmate.helper.model.parameter.BooleanParamSweepModel.RangeType.FIXED )
		//		.fixedValue( true );		
		final BooleanParamSweepModel remove_OOB = new BooleanParamSweepModel()
				.paramName( "remove out of bounds" )
				.rangeType( fiji.plugin.trackmate.helper.model.parameter.BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( false );		
		final BooleanParamSweepModel multi_channel = new BooleanParamSweepModel()
				.paramName( "MultiChannel" )
				.rangeType( fiji.plugin.trackmate.helper.model.parameter.BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( true );	
		final DoubleParamSweepModel scaling = new DoubleParamSweepModel()
				.paramName( "scaling factor" )
				.dimension( Dimension.LENGTH )
				.rangeType( RangeType.FIXED )
				.min( 0. )
				.max( 50. );
		final DoubleParamSweepModel NMS_IOU = new DoubleParamSweepModel()
				.paramName( "NMS IOU" )
				.dimension( Dimension.LENGTH )
				.rangeType( RangeType.FIXED )
				.min( 0. )
				.max( 100. );
		final DoubleParamSweepModel segmentation_threshold = new DoubleParamSweepModel()
				.paramName( "segmentation threshold" )
				.dimension( Dimension.LENGTH )
				.rangeType( RangeType.FIXED )
				.min( 0. )
				.max( 1. );
		final DoubleParamSweepModel detection_threshold = new DoubleParamSweepModel()
				.paramName( "detection threshold" )
				.dimension( Dimension.LENGTH )
				.rangeType( RangeType.FIXED )
				.min( 0. )
				.max( 1. );
		final IntParamSweepModel channel1 = new IntParamSweepModel()
				.paramName( "Channel to segment" )
				.rangeType( RangeType.FIXED )
				.min( 0 )
				.max( 4 );

		final Map< String, AbstractParamSweepModel< ? > > models = new LinkedHashMap<>();
		models.put( LacssDetectorFactory.KEY_LACSS_MODEL, LacssModel );
		models.put( LacssDetectorFactory.KEY_LACSS_CUSTOM_MODEL_FILEPATH, lacssCustomModelPath );
		//models.put( LacssDetectorFactory.KEY_RETURN_LABEL, return_label );
		models.put( LacssDetectorFactory.KEY_MIN_CELL_AREA, min_cell_area );
		models.put( LacssDetectorFactory.KEY_MULTI_CHANNEL, multi_channel); 
		models.put( DetectorKeys.KEY_TARGET_CHANNEL, channel1 );
		models.put( LacssDetectorFactory.KEY_SCALING, scaling );
		models.put( LacssDetectorFactory.KEY_NMS_IOU, NMS_IOU);
		models.put( LacssDetectorFactory.KEY_SEGMENTATION_THRESHOLD, segmentation_threshold );
		models.put( LacssDetectorFactory.KEY_DETECTION_THRESHOLD, detection_threshold );
		models.put (LacssDetectorFactory.KEY_REMOVE_OUT_OF_BOUNDS, remove_OOB);
		return models;
	}

	public static SpotDetectorFactoryBase< ? > createFactory()
	{
		return new LacssDetectorFactory<>();
	}

	public static Object castPretrainedModel( final String str )
	{
		for ( final PretrainedModel e : PretrainedModel.values() )
			if ( e.toString().equals( str ) )
				return e;

		return null;
	}
}

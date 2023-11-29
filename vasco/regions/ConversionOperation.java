package vasco.regions;

public abstract class ConversionOperation {
	public abstract Node convertToQuadTree(ConvertVector cv);

	public abstract ChainCode convertToChainCode(ConvertVector cv);

	public abstract BinaryArray convertToBinaryArray(ConvertVector cv);

	public abstract Raster convertToRaster(ConvertVector cv);
}

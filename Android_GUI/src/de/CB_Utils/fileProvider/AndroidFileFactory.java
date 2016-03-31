package de.CB_Utils.fileProvider;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import CB_Utils.Util.FileIO;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by Longri on 17.02.2016.
 */
public class AndroidFileFactory extends FileFactory {
	@Override
	protected File createPlatformFile(String path) {
	return new AndroidFile(path);
	}

	@Override
	protected File createPlatformFile(File parent) {
	return new AndroidFile(parent);
	}

	@Override
	protected File createPlatformFile(File parent, String child) {
	return new AndroidFile(parent, child);
	}

	@Override
	protected File createPlatformFile(String parent, String child) {
	return new AndroidFile(parent, child);
	}

	@Override
	protected String createPlatformThumb(String Path, int scaledWidth, String thumbPrefix) {

	String storePath = FileIO.GetDirectoryName(Path) + "/";
	String storeName = FileIO.GetFileNameWithoutExtension(Path);
	String storeExt = FileIO.GetFileExtension(Path).toLowerCase();
	String ThumbPath = storePath + thumbPrefix + THUMB + storeName + "." + storeExt;

	java.io.File ThumbFile = new java.io.File(ThumbPath);

	if (ThumbFile.exists())
		return ThumbPath;

	BitmapFactory.Options options = new BitmapFactory.Options();
	options.inJustDecodeBounds = true;

	BitmapFactory.decodeFile(Path, options);

	int oriWidth = options.outWidth;
	int oriHeight = options.outHeight;
	float scalefactor = (float) scaledWidth / (float) oriWidth;

	if (scalefactor >= 1)
		return Path; // don't need a thumb, return original path

	int newHeight = (int) (oriHeight * scalefactor);
	int newWidth = (int) (oriWidth * scalefactor);

	final int REQUIRED_WIDTH = newWidth;
	final int REQUIRED_HIGHT = newHeight;
	//Find the correct scale value. It should be the power of 2.
	int scale = 1;
	while (oriWidth / scale / 2 >= REQUIRED_WIDTH && oriHeight / scale / 2 >= REQUIRED_HIGHT)
		scale *= 2;

	BitmapFactory.Options o2 = new BitmapFactory.Options();
	o2.inSampleSize = scale;
	Bitmap resized = null;
	try {
		resized = BitmapFactory.decodeStream(new FileInputStream(Path), null, o2);
	} catch (FileNotFoundException e1) {

		e1.printStackTrace();
	}

	FileOutputStream out = null;
	try {
		out = new FileOutputStream(ThumbPath);

		Bitmap.CompressFormat format = Bitmap.CompressFormat.PNG;

		if (storeExt.equals("jpg"))
		format = Bitmap.CompressFormat.JPEG;

		resized.compress(format, 80, out);

		resized.recycle();

		return ThumbPath;
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		try {
		if (out != null) {
			out.close();
		}
		} catch (IOException e) {
		e.printStackTrace();
		}
	}

	return null;
	}
}
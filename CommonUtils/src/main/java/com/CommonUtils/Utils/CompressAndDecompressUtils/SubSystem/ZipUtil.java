package com.CommonUtils.Utils.CompressAndDecompressUtils.SubSystem;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;

import com.CommonUtils.Utils.ArrayUtils.ArrayUtil;
import com.CommonUtils.Utils.IOUtils.FileUtil;
import com.CommonUtils.Utils.IOUtils.IOUtil;
import com.CommonUtils.Utils.StringUtils.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ZipUtil 
{		
	private static void writeFile(final File sourceFile, 
			 					  final ZipArchiveOutputStream zaos, 
			 					  FileInputStream fis,
			 					  BufferedInputStream bis,
			 					  final String targetName) throws IOException
	{
		zaos.putArchiveEntry(new ZipArchiveEntry(targetName));
		
		fis = new FileInputStream(sourceFile);
		bis = new BufferedInputStream(fis);
		byte[] buffer = new byte[1024];
		int len = -1;
		while ((len = bis.read(buffer)) != -1)
		{ zaos.write(buffer, 0, len); }
		zaos.flush();
		
		zaos.closeArchiveEntry();
		IOUtil.closeQuietly(fis, bis);
	}
		
	private static void writeDirectory(final ZipArchiveOutputStream zaos, 
			  						   final String targetName) throws IOException
	{
		zaos.putArchiveEntry(new ZipArchiveEntry(targetName));
		zaos.closeArchiveEntry();
	}
	
	private static void compress(final File sourceFile, 
								 final String destName,
								 final ZipArchiveOutputStream zaos,
								 FileInputStream fis,
			 					 BufferedInputStream bis) throws IOException
	{
		//类型为目录
		if (sourceFile.isDirectory())
		{
			writeDirectory(zaos, destName + File.separator);
			File[] files = sourceFile.listFiles();
			for (File file : files)
			{
				String name = destName + File.separator + file.getName();
				compress(file, name, zaos, fis, bis);
			}
		}
		//类型为文件
		else
		{ writeFile(sourceFile, zaos, fis, bis, destName); }
	}
	
	public static void compress(final Path zipPath, final Path ... sourcePaths)
	{
		if (ArrayUtil.isArrayEmpty(sourcePaths))
		{ return; }
		
		Collection<File> sourceFiles = new ArrayList<>();
		for (Path sourcePath : sourcePaths)
		{ sourceFiles.add(sourcePath.toFile()); }
		
		compress(zipPath.toFile(), sourceFiles.toArray(new File[sourceFiles.size()]));
	}
	
	public static void compress(final String zipFilePath, final String ... sourceFilePaths)
	{
		if (ArrayUtil.isArrayEmpty(sourceFilePaths))
		{ return; }
		
		Collection<File> sourceFiles = new ArrayList<>();
		for (String sourceFilePath : sourceFilePaths)
		{ sourceFiles.add(new File(sourceFilePath)); }
		
		compress(new File(zipFilePath), sourceFiles.toArray(new File[sourceFiles.size()]));
	}
	
	public static void compress(final File zipFile, final File ... sourceFiles)
	{
		if (ArrayUtil.isArrayEmpty(sourceFiles))
		{ return; }
		
		ZipArchiveOutputStream zaos = null;
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		try
		{
			zaos = new ZipArchiveOutputStream(zipFile);
			zaos.setUseZip64(Zip64Mode.AsNeeded);
			for (File sourceFile : sourceFiles)
			{ compress(sourceFile, sourceFile.getName(), zaos, fis, bis); }
			zaos.finish();
		}
		catch (Exception ex)
		{ log.error("压缩为zip文件出现异常，异常原因为：", ex); }
		finally
		{ IOUtil.closeQuietly(zaos, fis, bis); }
	}
	
	public static void decompress(final Path srcPath, final Path saveFileDirectoryPath, final String charsetName)
	{ decompress(srcPath.toFile(), saveFileDirectoryPath.toFile(), charsetName); }
	
	public static void decompress(final String srcFilePath, final String saveFileDirectoryPath, final String charsetName)
	{ decompress(new File(srcFilePath), new File(saveFileDirectoryPath), charsetName); }
	
	public static void decompress(final File srcFile, final File saveFileDirectory, final String charsetName)
	{
		ZipFile zipFile = null;
		try
		{
			if (!IOUtil.isFileEmpty(saveFileDirectory))
			{
				if (!(saveFileDirectory.mkdir()))
				{ throw new Exception("解压zip文件失败，因无法创建目录，目录名称为：" + saveFileDirectory.getAbsolutePath()); }
			}
			
			zipFile = new ZipFile(srcFile, charsetName);
			Enumeration<ZipArchiveEntry> files = zipFile.getEntries();
			while (files.hasMoreElements())
			{
				ZipArchiveEntry zipArchiveEntry = files.nextElement();
				String entryFileName = zipArchiveEntry.getName();
				String entryFilePath = saveFileDirectory.getAbsolutePath() + File.separator + entryFileName;
				if (zipArchiveEntry.isDirectory())
				{
					if (!(new File(entryFilePath).mkdir()))
					{
						IOUtil.closeQuietly(zipFile);
						throw new Exception("解压zip文件失败，因无法创建目录，目录名称为：" + saveFileDirectory.getAbsolutePath());
					}
				}
				else
				{ FileUtil.copyFileBio(zipFile.getInputStream(zipArchiveEntry), new File(entryFilePath)); }
			}
		}
		catch (Exception ex)
		{ log.error("解压缩Zip文件异常，异常原因为：", ex); }
		finally
		{ IOUtil.closeQuietly(zipFile); }
	}
	
	public static boolean isEndsWithZip(final String fileName)
	{
		boolean result = false;
		if (!StringUtil.isStrEmpty(fileName))
		{
			if (fileName.endsWith(".ZIP") || fileName.endsWith(".zip"))
			{ result = true; }
		}
		return result;
	}
	
	public static boolean isEndsWithZip(final File file)
	{
		boolean result = false;
		if (!IOUtil.isFileEmpty(file)) { result = isEndsWithZip(file.getAbsolutePath()); }
		return result;
	}
	
	public static boolean isEndsWithZip(final Path path)
	{
		boolean result = false;
		if (null != path) { result = isEndsWithZip(path.toFile()); }
		return result;
	}
}
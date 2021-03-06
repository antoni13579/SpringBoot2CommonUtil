package com.CommonUtils.Utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import com.CommonUtils.Utils.DataTypeUtils.CollectionUtils.JavaCollectionsUtil;
import com.CommonUtils.Utils.DataTypeUtils.StringUtils.StringUtil;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IoUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * hutool工具类可以替换这个
 * @deprecated
 * */
@Deprecated(since="hutool工具类可以替换这个")
@Slf4j
public final class FileUtil 
{
	private FileUtil() {}
	
	/**
	 * 判断指定的路径是否为文件类型，文件类型返回true，不是则返回false，建议使用cn.hutool.core.io.FileUtil.isFile
	 * */
	public static boolean isFile(final String path)
	{
		File file = new File(path);
		return file.exists() && file.isFile() && !StringUtil.isStrEmpty(path);
	}
	
	/**
	 * 判断指定的路径是否为文件类型，文件类型返回true，不是则返回false，建议使用cn.hutool.core.io.FileUtil.isFile
	 * */
	public static boolean isFile(final File file)
	{ return null != file && file.exists() && file.isFile(); }
	
	/**
	 * 判断指定的路径是否为文件类型，文件类型返回true，不是则返回false，建议使用cn.hutool.core.io.FileUtil.isFile
	 * */
	public static boolean isFile(final Path path)
	{
		if (null == path)
		{ return false; }
		else
		{ return isFile(path.toFile()); }
	}
	
	/**建议使用cn.hutool.core.io.IoUtil.write或cn.hutool.core.io.FileUtil相关write方法*/
	public static boolean writeInfo(final File file, final boolean append, final String encode, final String info)
	{
		boolean result = false;
		try
		(
				FileOutputStream fos = new FileOutputStream(file, append);
				OutputStreamWriter osw = new OutputStreamWriter(fos, encode);
				BufferedWriter bw = new BufferedWriter(osw);
		)
		{
			bw.write(info);
			bw.flush();
			result = true;
		}
		catch (Exception ex)
		{
			log.error("写入数据到文件异常，异常原因为：", ex);
			result = false;
		}
		
		return result;
	}
	
	/**
	 * 创建文件（仅仅没有的时候才创建），并返回对应的File对象列表，建议使用cn.hutool.core.io.FileUtil
	 * */
	public static Collection<File> createFiles(final File ... files)
	{
		if (!com.CommonUtils.Utils.DataTypeUtils.ArrayUtils.ArrayUtil.isArrayEmpty(files))
		{
			List<File> result = new ArrayList<>();
			com.CommonUtils.Utils.DataTypeUtils.ArrayUtils.ArrayUtil.arrayProcessor
			(
					files, 
					(final File file, final int indx, final int length) -> 
					{
						try
						{
							if (!Files.exists(file.toPath()))
							{ file.createNewFile(); }
							
							result.add(file);
						}
						catch (Exception ex)
						{ log.error("创建文件异常，异常原因为：", ex); }
					}
			);
			return result;
		}
		else
		{ return Collections.emptyList(); }
	}
	
	/**
	 * 创建文件（仅仅没有的时候才创建），并返回对应的File对象列表，建议使用cn.hutool.core.io.FileUtil
	 * */
	public static Collection<File> createFiles(final String ... filePaths)
	{
		if (!com.CommonUtils.Utils.DataTypeUtils.ArrayUtils.ArrayUtil.isArrayEmpty(filePaths))
		{
			List<File> files = new ArrayList<>();
			com.CommonUtils.Utils.DataTypeUtils.ArrayUtils.ArrayUtil.arrayProcessor
			(
					filePaths, 
					(final String filePath, final int indx, final int length) -> files.add(new File(filePath))
			);
			
			return createFiles(files.toArray(new File[filePaths.length]));
		}
		else
		{ return Collections.emptyList(); }
	}
	
	/**
	 * 创建文件（仅仅没有的时候才创建），并返回对应的File对象列表，建议使用cn.hutool.core.io.FileUtil
	 * */
	public static Collection<File> createFiles(final Path ... paths)
	{
		if (!com.CommonUtils.Utils.DataTypeUtils.ArrayUtils.ArrayUtil.isArrayEmpty(paths))
		{
			List<File> files = new ArrayList<>();
			com.CommonUtils.Utils.DataTypeUtils.ArrayUtils.ArrayUtil.arrayProcessor
			(
					paths, 
					(final Path path, final int indx, final int length) -> files.add(path.toFile())
			);
			
			return createFiles(files.toArray(new File[paths.length]));
		}
		else
		{ return Collections.emptyList(); }
	}
	
	/**
	 * 采用Nio复制文件，建议使用cn.hutool.core.io.IoUtil.copy或cn.hutool.core.io.FileUtil.copy相关方法
	 * */
	public static String copyFileNio(final File srcFile, final File destFile)
	{
		try
		(
				FileInputStream fis = new FileInputStream(srcFile);
				FileChannel readChannel = fis.getChannel();
				FileOutputStream fos = new FileOutputStream(destFile);
				FileChannel writeChannel = fos.getChannel();
		)
		{
			long startTime = System.currentTimeMillis();			
			readChannel.transferTo(0, readChannel.size(), writeChannel);
			writeChannel.force(true);				//强制将内存中数据刷新到硬盘，boolean代表是否刷新属性
			double seconds = (System.currentTimeMillis() - startTime) / 1000D;
			log.info("采用Nio复制文件，源文件={}，目标文件={}，耗时{}秒", srcFile.getPath(), destFile.getPath(), seconds);
			return "SUCCESSED";
		}
		catch (Exception e)
		{ 
			log.error("采用Nio复制文件出现异常，源文件={}，目标文件={}，异常原因为：", srcFile.getPath(), destFile.getPath(), e);
			return "FAILED";
		}
	}
	
	/**
	 * 采用Nio复制文件，建议使用cn.hutool.core.io.IoUtil.copy或cn.hutool.core.io.FileUtil.copy相关方法
	 * */
	public static String copyFileNio(final String srcFilePath, final String destFilePath)
	{ return copyFileNio(new File(srcFilePath), new File(destFilePath)); }
	
	/**
	 * 采用Nio复制文件，建议使用cn.hutool.core.io.IoUtil.copy或cn.hutool.core.io.FileUtil.copy相关方法
	 * */
	public static String copyFileNio(final Path srcPath, final Path destPath)
	{ return copyFileNio(srcPath.toFile(), destPath.toFile()); }
	
	/**
	 * 采用Bio复制文件，建议使用cn.hutool.core.io.IoUtil.copy或cn.hutool.core.io.FileUtil.copy相关方法
	 * */
	public static void copyFileBio(final InputStream srcIs, final Path destPath)
	{ copyFileBio(srcIs, destPath.toFile()); }
	
	/**
	 * 采用Bio复制文件，建议使用cn.hutool.core.io.IoUtil.copy或cn.hutool.core.io.FileUtil.copy相关方法
	 * */
	public static void copyFileBio(final InputStream srcIs, final String destFilePath)
	{ copyFileBio(srcIs, new File(destFilePath)); }
	
	/**
	 * 采用Bio复制文件，建议使用cn.hutool.core.io.IoUtil.copy或cn.hutool.core.io.FileUtil.copy相关方法
	 * */
	public static void copyFileBio(final InputStream srcIs, final File destFile)
	{
		try
		(
				InputStream bis = new BufferedInputStream(srcIs);
				OutputStream fos = new FileOutputStream(destFile);
				OutputStream bos = new BufferedOutputStream(fos);
		)
		{
			byte[] buffer = new byte[1024];
	        int read = -1;
	        while((read = bis.read(buffer)) != -1)
	        { bos.write(buffer, 0, read); }
	        
	        bos.flush();
		}
		catch (Exception ex)
		{ log.error("采用Bio复制文件出现异常，目标文件={}，异常原因为：", destFile.getPath(), ex); }
		finally
		{ IoUtil.close(srcIs); }
	}
	
	/**
	 * 采用Bio复制文件，建议使用cn.hutool.core.io.IoUtil.copy或cn.hutool.core.io.FileUtil.copy相关方法
	 * */
	public static String copyFileBio(final File srcFile, final File destFile)
	{
		try
		(
				InputStream fis = new FileInputStream(srcFile);
				InputStream bis = new BufferedInputStream(fis);
				OutputStream fos = new FileOutputStream(destFile);
				OutputStream bos = new BufferedOutputStream(fos);
		)
		{
			long startTime = System.currentTimeMillis();
			
			byte[] buffer = new byte[1024];
	        int read = -1;
	        while((read = bis.read(buffer)) != -1)
	        { bos.write(buffer, 0, read); }
	        
	        bos.flush();
	        double seconds = (System.currentTimeMillis() - startTime) / 1000D;
			log.info("采用Bio复制文件，源文件={}，目标文件={}，耗时{}秒", srcFile.getPath(), destFile.getPath(), seconds);
			return "SUCCESSED";
		}
		catch (Exception e)
		{
			log.error("采用Bio复制文件出现异常，源文件={}，目标文件={}，异常原因为：", srcFile.getPath(), destFile.getPath(), e);
			return "FAILED";
		}
	}
	
	/**
	 * 采用Bio复制文件，建议使用cn.hutool.core.io.IoUtil.copy或cn.hutool.core.io.FileUtil.copy相关方法
	 * */
	public static String copyFileBio(final String srcFilePath, final String destFilePath)
	{ return copyFileBio(new File(srcFilePath), new File(destFilePath)); }
	
	/**
	 * 采用Bio复制文件，建议使用cn.hutool.core.io.IoUtil.copy或cn.hutool.core.io.FileUtil.copy相关方法
	 * */
	public static String copyFileBio(final Path srcPath, final Path destPath)
	{ return copyFileBio(srcPath.toFile(), destPath.toFile()); }
	
	/**建议使用cn.hutool.core.io.IoUtil.readHex28Upper*/
	public static String getFileHeader(final String filePath, final boolean toUpper)
	{ return getFileHeader(new File(filePath), toUpper); }
	
	/**建议使用cn.hutool.core.io.IoUtil.readHex28Upper*/
	public static String getFileHeader(final Path path, final boolean toUpper)
	{ return getFileHeader(path.toFile(), toUpper); }
	
	/**建议使用cn.hutool.core.io.IoUtil.readHex28Upper*/
	public static String getFileHeader(final File file, final boolean toUpper)
	{
		String result = null;
		
		try(InputStream fis = new FileInputStream(file);)
		{
			byte[] b = new byte[100];
			fis.read(b, 0, b.length);
			result = Convert.toHex(b);
		}
		catch (Exception ex)
		{ log.error("获取文件头出现异常，异常原因为：{}", ex); }
		
		return toUpper ? Optional.ofNullable(result).orElse("").toUpperCase() : result;
	}
	
	/**建议使用cn.hutool.core.io.FileTypeUtil.getType*/
	public static Collection<String> getFileType(final Path path)
	{ return getFileType(path.toFile()); }
	
	/**建议使用cn.hutool.core.io.FileTypeUtil.getType*/
	public static Collection<String> getFileType(final String filePath)
	{ return getFileType(new File(filePath)); }
	
	/**建议使用cn.hutool.core.io.FileTypeUtil.getType*/
	public static Collection<String> getFileType(final File file)
	{
		String fileHeader = getFileHeader(file, true);
		Collection<String> result = new HashSet<>();
		JavaCollectionsUtil.mapProcessor
		(
				IOContants.FILE_TYPE, 
				(String key, String value, int indx) -> 
				{
					if (fileHeader.startsWith(value))
					{ result.add(key); }
				}
		);
		return result;
	}
	
	/**建议使用cn.hutool.core.collection.LineIter自行实现*/
	public static long getFileTotalRows(final Path path, final String encode)
	{ return getFileTotalRows(path.toFile(), encode); }
	
	/**建议使用cn.hutool.core.collection.LineIter自行实现*/
	public static long getFileTotalRows(final String filePath, final String encode)
	{ return getFileTotalRows(new File(filePath), encode); }
	
	/**建议使用cn.hutool.core.collection.LineIter自行实现*/
	public static long getFileTotalRows(final File file, final String encode)
	{
		long result = 0;
		
		try
		(
				InputStream fis = new FileInputStream(file);
				Reader isr = new InputStreamReader(fis, encode);
				BufferedReader br = new BufferedReader(isr);
		)
		{
			String line;
			while (null != (line = br.readLine()))
			{
				result++;
				log.debug(line);
			}
		}
		catch (Exception ex)
		{ log.error("获取文件总行数异常，异常原因为：", ex); }
		
		return result;
	}
	
	/**
	 * 建议使用cn.hutool.core.io.FileUtil.size
	 * */
	public static long getFileSize(final Path path)
	{ return getFileSize(path.toFile()); }
	
	/**
	 * 建议使用cn.hutool.core.io.FileUtil.size
	 * */
	public static long getFileSize(final String filePath)
	{ return getFileSize(new File(filePath)); }
	
	/**
	 * 建议使用cn.hutool.core.io.FileUtil.size
	 * */
	public static long getFileSize(final File file)
	{ return file.length(); }
	
	/**
	 * 建议使用hutool相关工具，代码例子如下
	 * 1、cn.hutool.extra.servlet.getMultipart获取MultipartFormData对象
	 * 2、MultipartFormData对象中，通过getFileMap获取需上传文件的相关信息
	 * 3、调用UploadFile里面的write写入到本地
	 * */
	public static boolean uploadFilesLocal(final Path directoryPath, final MultipartFile ... multipartFiles)
	{ return uploadFilesLocal(directoryPath.toFile(), multipartFiles); }
	
	/**
	 * 建议使用hutool相关工具，代码例子如下
	 * 1、cn.hutool.extra.servlet.getMultipart获取MultipartFormData对象
	 * 2、MultipartFormData对象中，通过getFileMap获取需上传文件的相关信息
	 * 3、调用UploadFile里面的write写入到本地
	 * */
	public static boolean uploadFilesLocal(final String directoryStr, final MultipartFile ... multipartFiles)
	{ return uploadFilesLocal(new File(directoryStr), multipartFiles); }
	
	/**
	 * 建议使用hutool相关工具，代码例子如下
	 * 1、cn.hutool.extra.servlet.getMultipart获取MultipartFormData对象
	 * 2、MultipartFormData对象中，通过getFileMap获取需上传文件的相关信息
	 * 3、调用UploadFile里面的write写入到本地
	 * */
	public static boolean uploadFilesLocal(final File directory, final MultipartFile ... multipartFiles)
	{
		if (!cn.hutool.core.util.ArrayUtil.isEmpty(multipartFiles))
		{
			Set<Boolean> executeRecords = new HashSet<>();
			JavaCollectionsUtil.collectionProcessor
			(
					CollUtil.newArrayList(multipartFiles), 
					(multipartFile, indx, length) -> 
					{
						if (!directory.exists())
						{ directory.mkdir(); }
						
						String filePath = directory.getAbsolutePath() + File.separator + multipartFile.getOriginalFilename();
						try
						{
							multipartFile.transferTo(new File(filePath));
							executeRecords.add(true);
						}
						catch (Exception ex)
						{
							log.error("上传文件异常，需上传的文件名称为{}，异常原因为：", multipartFile.getOriginalFilename(), ex);
							executeRecords.add(false);
						}
					}
			);
			return JavaCollectionsUtil.getOperationFlowResult(executeRecords);
		}
		else
		{ return false; }
	}
	
	/**建议使用cn.hutool.core.io.IoUtil.readBytes或cn.hutool.core.io.FileUtil.readBytes*/
	public static Optional<byte[]> toBytes(final File file)
	{
		byte[] data = null;
        try
        (
        		FileInputStream input = new FileInputStream(file);
        		BufferedInputStream bufferInput = new BufferedInputStream(input);
        		ByteArrayOutputStream output = new ByteArrayOutputStream();
        )
        {
        	byte[] buf = new byte[1024];
        	int numBytesRead = 0;
        	
        	while ((numBytesRead = bufferInput.read(buf, 0, 1024)) != -1) 
        	{ output.write(buf, 0, numBytesRead); }
        	
        	data = output.toByteArray();
        }  
        catch (Exception ex) 
        { log.error("文件转换字节数组出现异常，异常原因为：", ex); }
        
        return Optional.ofNullable(data);
	}
	
	/**建议使用cn.hutool.core.io.IoUtil.readBytes或cn.hutool.core.io.FileUtil.readBytes*/
	public static Optional<byte[]> toBytes(final Path path)
	{ return toBytes(path.toFile()); }
	
	/**建议使用cn.hutool.core.io.IoUtil.readBytes或cn.hutool.core.io.FileUtil.readBytes*/
	public static Optional<byte[]> toBytes(final String filePath)
	{ return toBytes(new File(filePath)); }
}
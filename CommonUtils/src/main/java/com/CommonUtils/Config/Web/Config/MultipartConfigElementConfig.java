package com.CommonUtils.Config.Web.Config;

import javax.servlet.MultipartConfigElement;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.util.unit.DataSize;

import cn.hutool.core.util.StrUtil;

/**在使用这个配置类的时候，需要进行create bean操作*/
public final class MultipartConfigElementConfig 
{
	private MultipartConfigElementConfig() {}
	
	public static MultipartConfigElement getInstance(final String location, final DataSize maxFileSize, final DataSize maxRequestSize)
	{
		MultipartConfigFactory factory = new MultipartConfigFactory();
		
		if (!StrUtil.isEmptyIfStr(location))
		{ factory.setLocation(location); }
		
		//设置文件大小限制 ,超了，页面会抛出异常信息，这时候就需要进行异常信息的处理了
        factory.setMaxFileSize(maxFileSize);
        factory.setMaxRequestSize(maxRequestSize);		 //设置总上传数据总大小
        return factory.createMultipartConfig();
	}
}
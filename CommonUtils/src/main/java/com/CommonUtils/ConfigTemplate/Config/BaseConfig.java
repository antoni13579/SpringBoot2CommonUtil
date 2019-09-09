package com.CommonUtils.ConfigTemplate.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.CommonUtils.Config.ThreadPool.ThreadPoolTaskExecutorConfig;

@Configuration
public class BaseConfig 
{
	@Bean(name = "commonThreadPool")
	public ThreadPoolTaskExecutor commonThreadPool()
	{ return ThreadPoolTaskExecutorConfig.getThreadPoolTaskExecutor(false, 15, 30); }
}
package com.CommonUtils.ConfigTemplate.RestController;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.WebAsyncTask;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.CommonUtils.ConfigTemplate.Bean.ExcelBean;
import com.CommonUtils.ConfigTemplate.Config.SpringIntegrationConfig.MainGateWay;
import com.CommonUtils.Utils.FrameworkUtils.SecurityUtils.SpringSecurityUtil;
import com.CommonUtils.Utils.NetworkUtils.HttpUtils.Bean.RegisterInfo;
import com.CommonUtils.Utils.NetworkUtils.HttpUtils.Bean.SimpleResponse;
import com.CommonUtils.Utils.TreeUtils.Bean.TreeNode;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.PatternPool;
import cn.hutool.core.util.ReUtil;
import cn.hutool.poi.excel.ExcelReader;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@RestController
@RequestMapping("/homeRestController")
@Slf4j
@Api(value = "核心RestController")
public class HomeRestController 
{
	private String userModulePath = "SystemConfigInfo/UserModule.xls";
	private String menusOfModulePath = "SystemConfigInfo/MenusOfModule.xls";
	
	@Resource
	private MainGateWay mainGateWay;
	
	@Resource
	private ThreadPoolTaskExecutor commonThreadPool;
	
	@RequestMapping("/register")
	public SimpleResponse register(@ModelAttribute RegisterInfo dto)
	{
		String email = dto.getRegisterEmail();
		SimpleResponse result = new SimpleResponse();
		
		//邮箱地址验证不通过
		if (!ReUtil.isMatch(PatternPool.EMAIL, email))
		{
			return result.setStatus(HttpStatus.BAD_REQUEST.value())
						 .setStatusDesc("邮箱格式验证不通过，无法注册");
		}
		else
		{
			return result.setStatus(HttpStatus.OK.value())
					 	 .setStatusDesc("注册成功");
		}
	}
	
	/**
	 * WebAsyncTask是作为返回值，同时可以直接把执行任务嵌套其中，异步执行
	 * */
	@RequestMapping("/getUserModule")
	public WebAsyncTask<List<Map<String, Object>>> getUserModule()
	{
		URL url = Thread.currentThread().getContextClassLoader().getResource(this.userModulePath);		
		WebAsyncTask<List<Map<String, Object>>> result = new WebAsyncTask<>
		(
				4000L, 
				this.commonThreadPool, 
				() -> 
				{
					ExcelReader reader = null;
					List<Map<String, Object>> excelResult = null;
					try
					{
						reader = cn.hutool.poi.excel.ExcelUtil.getReader(new File(url.toURI()));
						excelResult = reader.readAll();
					}
					catch (Exception ex)
					{ excelResult = Collections.emptyList(); }
					finally
					{ IoUtil.close(reader); }
					
					return excelResult;
				}
		);
		result.onError
		(
				() -> 
				{
					log.error("获取菜单主模块出现异常");
					return Collections.emptyList();
				}
		);
		result.onTimeout
		(
				() -> 
				{
					log.warn("获取菜单主模块超时");
					return Collections.emptyList();
				}
		);
		
		return result;
	}
	
	@RequestMapping("/getCurrentUserName")
	@ApiOperation(value = "获取当前登录用户",notes = "获取当前登录用户")
	@ApiImplicitParams
	(
			{ @ApiImplicitParam(name = "authentication", value = "SpringSecurity用户信息", required = true, dataTypeClass = Authentication.class) }
	)
	public Mono<User> getCurrentUserName(Authentication authentication)
	{
		UserDetails user = SpringSecurityUtil.getUser(authentication);
		return Mono.just((User)user);
	}
	
	/**
	 * DeferredResult为返回值，需要先执行第三方任务，把最终结果给到DeferredResult，再返回DeferredResult
	 * */
	@RequestMapping("/getMenusOfModule")
	public DeferredResult<Collection<TreeNode<Map<String, Object>>>> getMenusOfModule(String menuType)
	{
		DeferredResult<Collection<TreeNode<Map<String, Object>>>> result = new DeferredResult<>(4000L, Collections.emptyList());
		result.onTimeout(() -> log.warn("获取子菜单超时"));
		
		this.commonThreadPool.submit
		(
				() -> 
				{
					URL url = Thread.currentThread().getContextClassLoader().getResource(this.menusOfModulePath);
					ExcelReader reader = null;
					List<Map<String, Object>> excelResult = null;
					try
					{
						reader = cn.hutool.poi.excel.ExcelUtil.getReader(new File(url.toURI()));
						reader.setSheet(menuType);
						excelResult = reader.readAll();
					}
					catch (Exception ex)
					{ excelResult = Collections.emptyList(); }
					finally
					{ IoUtil.close(reader); }
					
					ExcelBean excelBean = new ExcelBean()
							.setExcelDatas(excelResult)
							.setEmpty(CollUtil.isEmpty(excelResult))
							.setDeferredResult(result);
					this.mainGateWay.startToHandleExcelData(excelBean);
				}
		);
		
		return result;
	}
	
	/**
	 * 一边异步处理请求一边生成HTTP响应的方式为将一个HTTP响应分割成多个事件返回，这种方式是基于HTTP/1.1的分块传输编码（Chunked transfer encoding）。
	 * */
	@GetMapping("/responseBodyEmitterTest")
	public ResponseBodyEmitter streaming()
	{
		ResponseBodyEmitter emitter = new ResponseBodyEmitter(4000L);
		this.commonThreadPool.submit
		(
				() -> 
				{
					for (int i = 1; i <= 10; i++)
					{
						try 
						{ Thread.sleep(10); } 
						catch (InterruptedException e) 
						{
							log.error("睡眠异常，异常原因为：", e);
							Thread.currentThread().interrupt();
						}
						
						try 
						{ emitter.send(i, MediaType.APPLICATION_JSON); } 
						catch (IOException e) 
						{ log.error("IO异常，异常原因为：", e); }
					}
					
					emitter.complete();
				}
		);
		
		return emitter;
	}
	
	/**
	 * 所谓的Sse其实就是Server-Sent Events，即服务器推送事件，属于HTML5的一项新功能，常用于服务器主动通知客户端有相关信息的更新。
	 * 其他替代方法一般有WebSocket和客户端定时轮询，前者过于复杂，后者又过于低效而笨拙。SseEmitter属于ResponseBodyEmitter的子类，可以生成text/event-stream格式的信息。
	 * */
	@GetMapping("/sseEmitterTest")
	//@GetMapping(value="/sseEmitterTest",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter sse()
	{
		SseEmitter emitter = new SseEmitter(4000L);
		this.commonThreadPool.submit
		(
				() -> 
				{
					for (int i = 1; i <= 10; i++)
					{
						try 
						{ Thread.sleep(10); } 
						catch (InterruptedException e) 
						{
							log.error("睡眠异常，异常原因为：", e);
							Thread.currentThread().interrupt();
						}
						
						try 
						{ emitter.send(SseEmitter.event().comment("This is test event").id(UUID.randomUUID().toString()).name("onlog").reconnectTime(3000).data(i)); } 
						catch (IOException e) 
						{ log.error("IO异常，异常原因为：", e); }
					}
					
					emitter.complete();
				}
		);
		return emitter;
	}
	
	@GetMapping(value="/countDown",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<ServerSentEvent<Object>> countDown() 
	{
		return Flux.interval(Duration.ofSeconds(1))
				   .map(seq -> Tuples.of(seq, getCountDownSec()))
				   .map(data -> ServerSentEvent.<Object>builder()
						   					   .event("countDown")
						   					   .id(Long.toString(data.getT1()))
						   					   .data(data.getT2())
						   					   .build());
	}
	
	private String getCountDownSec() 
	{
		int countDownSec=3*60*60;
		if (countDownSec>0) 
		{
			int h = countDownSec/(60*60);
			int m = (countDownSec%(60*60))/60;
			int s = (countDownSec%(60*60))%60;
			return "活动倒计时："+h+" 小时 "+m+" 分钟 "+s+" 秒";
		}
		return "活动倒计时：0 小时 0 分钟 0 秒";
	}
}
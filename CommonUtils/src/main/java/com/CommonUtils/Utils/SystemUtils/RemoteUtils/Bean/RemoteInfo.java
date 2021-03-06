package com.CommonUtils.Utils.SystemUtils.RemoteUtils.Bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * hutool已封装了对应的工具类
 * @deprecated
 * */
@Deprecated(since="hutool已封装了对应的工具类")
@Getter
@Setter
@ToString
public final class RemoteInfo 
{
	private String host;
	private String username;
	private String password;
	private int port;
	private String encode;

	public RemoteInfo(final String host, final String username, final String password, final int port, final String encode)
	{ init(host, username, password, port, encode); }
	
	public RemoteInfo(final String host, final String username, final String password, final int port)
	{ init(host, username, password, port, "gbk"); }
	
	public RemoteInfo(final String host, final String username, final String password)
	{ init(host, username, password, 22, "gbk"); }
	
	private void init(final String host, final String username, final String password, final int port, final String encode)
	{
		this.host = host;
		this.username = username;
		this.password = password;
		this.port = port;
		this.encode = encode;
	}
}
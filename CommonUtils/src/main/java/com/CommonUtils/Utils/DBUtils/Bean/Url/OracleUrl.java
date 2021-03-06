package com.CommonUtils.Utils.DBUtils.Bean.Url;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
@ToString
public final class OracleUrl 
{
	private String tnsname;
	private String hostIp;
	private int port;
	private String instanceName;
}
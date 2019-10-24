package com.CommonUtils.Config.Jdbc.DataSources;

import java.sql.SQLException;

import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;

import com.CommonUtils.Utils.DBUtils.DBContants;
import com.CommonUtils.Utils.DBUtils.Bean.DBBaseInfo.DBInfo;
import com.CommonUtils.Utils.DataTypeUtils.CollectionUtils.CustomCollections.Properties;

/**
 * Atomikos的分布式事务仅仅适用于单个JAVA应用连接不同的数据库，也有缺点的，具体是XA协议两阶段提交方案锁定资源时间长，对性能影响很大，基本不适合解决微服务事务问题。架构不复杂可以用
 * 但是不适用如下场景：
 * 订单服务调用订单数据库，库存服务调用库存数据库，业务应用要调用【订单服务】与【库存服务】完成业务操作
 * */
public final class AtomikosDataSourceConfig 
{
	private AtomikosDataSourceConfig() {}
	
	public static DataSource getDataSourceForMySql(final String dataSourceName, 
										   		   final String validationQuery,
										   		   final DBInfo dbInfo) throws SQLException
	{
		AtomikosDataSourceBean result = generateAtomikosDataSourceBean(dataSourceName, validationQuery);
		result.setXaDataSourceClassName(DBContants.MySqlJdbcDriver.MYSQL_XA.getJdbcDriver());
		result.setXaProperties
		(
				new Properties()
					.setProperty("url", dbInfo.getJdbcUrl())
					.setProperty("user", dbInfo.getJdbcUserName())
					.setProperty("password", dbInfo.getJdbcPassWord())
					.setProperty("pinGlobalTxToPhysicalConnection", String.valueOf(true))
					.getProperties()
		);
		return result;
	}
	
	/**建议使用数据库提供的XADataSource，别自己实现或引用第三方的XADataSource，如Druid*/
	@Deprecated
	public static DataSource getDataSource(final String dataSourceName, 
	   		   							   final String validationQuery,
	   		   							   final XADataSource xaDataSource) throws SQLException
	{
		AtomikosDataSourceBean result = generateAtomikosDataSourceBean(dataSourceName, validationQuery);
		result.setXaDataSource(xaDataSource);
		return result;
	}
	
	private static AtomikosDataSourceBean generateAtomikosDataSourceBean(final String dataSourceName, 
	   		   															 final String validationQuery) throws SQLException
	{
		AtomikosDataSourceBean result = new AtomikosDataSourceBean();
		result.setBeanName(dataSourceName);
		result.setBorrowConnectionTimeout(30);
		result.setConcurrentConnectionValidation(true);
		//result.setDefaultIsolationLevel(defaultIsolationLevel);
		result.setLoginTimeout(30);
		//result.setLogWriter(out);
		result.setMaintenanceInterval(60);
		result.setMaxIdleTime(6000);
		result.setMaxLifetime(30000);
		result.setMaxPoolSize(40);
		result.setMinPoolSize(2);
		result.setPoolSize(40);
		//result.setReapTimeout(reapTimeout);
		result.setTestQuery(validationQuery);
		result.setUniqueResourceName(dataSourceName);
		

		//result.close();
		//result.destroy();
		//result.init();
		//result.setXaDataSource(xaDataSource);
		//result.setXaDataSourceClassName(xaDataSourceClassName);
		//result.setXaProperties(xaProperties);
		return result;
	}
}
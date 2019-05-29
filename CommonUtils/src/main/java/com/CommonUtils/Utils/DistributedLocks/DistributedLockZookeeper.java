package com.CommonUtils.Utils.DistributedLocks;

import java.util.Collections;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooDefs.Perms;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DistributedLockZookeeper 
{
	private DistributedLockZookeeper() {}
	
	public synchronized static boolean lock(final ZooKeeper zooKeeperClient, final String path)
	{
		try
		{
			Stat stat = getStat(zooKeeperClient, path);
			if (null != stat)
			{ 
				log.warn("【{}】Zookeeper分布式锁已存在，无法进行获取", path);
				return false;
			}
			else
			{
				zooKeeperClient.create(path, path.getBytes(), Collections.singletonList(new ACL(Perms.ALL,Ids.ANYONE_ID_UNSAFE)) , CreateMode.EPHEMERAL);
				return true;
			}
		}
		catch(Exception e)
		{
			log.error("【{}】Zookeeper分布式锁获取失败，异常原因为：{}", path, e);
			return false;
		}
	}
	
	public synchronized static void unlock(final ZooKeeper zooKeeperClient, final String path)
	{
		try
		{
			Stat stat = getStat(zooKeeperClient, path);
			if (null != stat) 
			{ zooKeeperClient.delete(path, 0); }
			else
			{ log.warn("【{}】Zookeeper分布式锁不存在，无法进行释放操作", path); }
		}
		catch(Exception e)
		{ log.error("【{}】Zookeeper分布式锁释放失败，异常原因为：{}", path, e); }
	}
	
	private static Stat getStat(final ZooKeeper zooKeeper, final String path) throws KeeperException, InterruptedException
	{ return zooKeeper.exists(path, true); }
}
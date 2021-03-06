package com.CommonUtils.Utils.FrameworkUtils.SecurityUtils.EncryptionAndDecryptionUtils;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

/**建议使用hutool相关加解密工具
 * @deprecated
 * */
@Deprecated(since="建议使用hutool相关加解密工具")
public final class JasyptUtil 
{
	private JasyptUtil() {}
	
	public static String decrypt(final String key, final String encryptedStr)
	{
		StandardPBEStringEncryptor instance = new StandardPBEStringEncryptor();
		instance.setPassword(key);
		return instance.decrypt(encryptedStr);
	}
	
	public static String encrypt(final String key, final String needEncryptStr)
	{
		StandardPBEStringEncryptor instance = new StandardPBEStringEncryptor();
		instance.setPassword(key);
		return instance.encrypt(needEncryptStr);
	}
}
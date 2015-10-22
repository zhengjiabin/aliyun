package OSSClient;

import java.io.IOException;
import java.util.Properties;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;

public class OSSClientUtil {

	/** OSS客户端 */
	private volatile static OSSClient OSSClient;

	private OSSClientUtil() {

	}

	/**
	 * 初始化OSSClient
	 * 
	 * @return
	 */
	public static OSSClient getInstance() {
		if (OSSClient == null) {
			synchronized (OSSClientUtil.class) {
				if (OSSClient == null) {
					OSSClient = initOSSClient();
				}
			}
		}
		return OSSClient;
	}

	/**
	 * 初始化OSS对象
	 * 
	 * @return
	 */
	private static OSSClient initOSSClient() {
		// 初始化属性对象
		Properties prop = initProperties();
		// 初始化配置对象
		ClientConfiguration config = initOSSClientConfiguration(prop);
		// 初始化域名对象
		String endpoint = getEndpoint(prop);
		// 获取登录对象
		DefaultCredentialProvider credsProvider = getCredentials(config, prop);

		OSSClient = new OSSClient(endpoint, credsProvider, config);
		return OSSClient;
	}

	/**
	 * 获取登录信息
	 * 
	 * @param config
	 * @param prop
	 */
	private static DefaultCredentialProvider getCredentials(ClientConfiguration config, Properties prop) {
		/** 用户的Access Key ID */
		String ACCESS_KEY_ID = prop.getProperty("ACCESS_KEY_ID");
		/** 用户的Secret Access Key */
		String SECRET_ACCESS_KEY = prop.getProperty("SECRET_ACCESS_KEY");

		DefaultCredentialProvider credentialProvider = new DefaultCredentialProvider(ACCESS_KEY_ID, SECRET_ACCESS_KEY);
		return credentialProvider;
	}

	/**
	 * 获取域名信息
	 * 
	 * @param prop
	 */
	private static String getEndpoint(Properties prop) {
		String setEndpoint = prop.getProperty("setEndpoint");
		if (!Boolean.parseBoolean(setEndpoint)) {
			return null;
		}

		/** 用户自己指定的域名 */
		String ENDPOINT = prop.getProperty("ENDPOINT");
		return ENDPOINT;
	}

	/**
	 * 设置OSS配置对象
	 * 
	 * @param properties
	 * @return
	 */
	private static ClientConfiguration initOSSClientConfiguration(Properties prop) {
		ClientConfiguration config = new ClientConfiguration();

		// 设置代理
		setProxy(config, prop);
		// 设置需要用户验证的代理
		setProxyByValidate(config, prop);
		// 设置网络参数
		setNetwork(config, prop);

		return config;
	}

	/**
	 * 获取OSS配置属性
	 * 
	 * @return
	 */
	private static Properties initProperties() {
		Properties prop = new Properties();
		try {
			prop.load(OSSClientUtil.class.getResourceAsStream("/aliyunOSS.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return prop;
	}

	/**
	 * 设置网络参数
	 * 
	 * @param config
	 * @param prop
	 */
	private static void setNetwork(ClientConfiguration config, Properties prop) {
		String setNetwork = prop.getProperty("setNetwork");
		if (!Boolean.parseBoolean(setNetwork)) {
			return;
		}

		// 设置HTTP最大连接数为10
		String maxConnections = prop.getProperty("maxConnections");
		config.setMaxConnections(Integer.parseInt(maxConnections));

		// 设置最大的重试次数为3
		String maxErrorRetry = prop.getProperty("maxErrorRetry");
		config.setMaxErrorRetry(Integer.parseInt(maxErrorRetry));

		// 设置TCP连接超时为5000毫秒
		String connectionTimeoutInMillis = prop.getProperty("connectionTimeoutInMillis");
		config.setConnectionTimeout(Integer.parseInt(connectionTimeoutInMillis));

		// 设置Socket传输数据超时的时间为2000毫秒
		String socketTimeoutInMillis = prop.getProperty("socketTimeoutInMillis");
		config.setSocketTimeout(Integer.parseInt(socketTimeoutInMillis));
	}

	/**
	 * 设置需要用户验证的代理
	 * 
	 * @param config
	 * @param prop
	 */
	private static void setProxyByValidate(ClientConfiguration config, Properties prop) {
		String setProxyByValidate = prop.getProperty("setProxyByValidate");
		if (!Boolean.parseBoolean(setProxyByValidate)) {
			return;
		}

		String proxyUsername = prop.getProperty("proxyUsername");
		config.setProxyUsername(proxyUsername);

		String proxyPassword = prop.getProperty("proxyPassword");
		config.setProxyPassword(proxyPassword);
	}

	/**
	 * 设置代理
	 * 
	 * @param config
	 * @param prop
	 */
	private static void setProxy(ClientConfiguration config, Properties prop) {
		String setProxy = prop.getProperty("setProxy");
		if (!Boolean.parseBoolean(setProxy)) {
			return;
		}

		String proxyHost = prop.getProperty("proxyHost");
		config.setProxyHost(proxyHost);

		String proxyPort = prop.getProperty("proxyPort");
		config.setProxyPort(Integer.parseInt(proxyPort));
	}
}

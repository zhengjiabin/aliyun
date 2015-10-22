package OSSClient;

import java.io.IOException;
import java.util.Properties;

import org.springframework.util.StringUtils;

import com.aliyun.oss.internal.OSSConstants;

public class OSSDirectoryUtil {
	private static final String defaultEndpoint = OSSConstants.DEFAULT_OSS_ENDPOINT;

	private static Properties prop;

	static {
		prop = new Properties();
		try {
			prop.load(OSSDirectoryUtil.class.getResourceAsStream("/aliyunOSS.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取bucket下的路径
	 * 
	 * @param bucketName
	 * @param filePath
	 * @return
	 */
	public static String getDirectoryUnderBucket(String bucketName, String filePath) {
		if (StringUtils.isEmpty(filePath)) {
			return null;
		}
		if (StringUtils.isEmpty(bucketName)) {
			return filePath;
		}

		int index = filePath.indexOf(bucketName);
		String directoryUnderBucket = filePath.substring(index + bucketName.length() + 1, filePath.length());
		return directoryUnderBucket;
	}

	/**
	 * 获取文件名后缀
	 * 
	 * @param fileAbsolutePath
	 */
	public static String getFileSuffix(String fileAbsolutePath) {
		if (StringUtils.isEmpty(fileAbsolutePath)) {
			return null;
		}

		int lastIndexOf = fileAbsolutePath.lastIndexOf(".");
		String suffix = fileAbsolutePath.substring(lastIndexOf, fileAbsolutePath.length());
		return suffix;
	}

	/**
	 * 文件路径规范化
	 * 
	 * @param directory
	 * @return
	 */
	public static String normalDirectory(String directory) {
		if (StringUtils.isEmpty(directory)) {
			return null;
		}

		String newDirectory = directory.replaceAll("\\\\", "/");
		return newDirectory;
	}

	/**
	 * <pre>
	 * 获取上传路径
	 * 若未绑定域名：则访问路径：http://bj.bcebos.com/v1/binbinpictures/photo/test.png
	 * （bucketName：binbinpictures,directoryUnderBucket：photo/test.png）
	 * 
	 * 若绑定域名：则访问路径：http://binbinpictures.bj.bcebos.com/photo/test.png
	 * </pre>
	 */
	public static String getDirectory(String bucketName, String directoryUnderBucket) {
		if (StringUtils.isEmpty(bucketName) || StringUtils.isEmpty(directoryUnderBucket)) {
			return null;
		}

		String setEndpoint = prop.getProperty("setEndpoint");

		if (Boolean.parseBoolean(setEndpoint)) {
			return getDirectoryByEndpoint(bucketName, directoryUnderBucket);
		} else {
			return getDirectoryByDefalutEndpoint(bucketName, directoryUnderBucket);
		}
	}

	private static String getDirectoryByEndpoint(String bucketName, String directoryUnderBucket) {
		String ENDPOINT = prop.getProperty("ENDPOINT");
		if (defaultEndpoint.equals(ENDPOINT)) {
			return getDirectoryByDefalutEndpoint(bucketName, directoryUnderBucket);
		} else {
			return getDirectoryBySpecialEndpoint(bucketName, directoryUnderBucket);
		}
	}

	/**
	 * 获取指定域名的访问路径
	 * 
	 * @param bucketName
	 * @param directoryUnderBucket
	 * @return
	 */
	private static String getDirectoryBySpecialEndpoint(String bucketName, String directoryUnderBucket) {
		String ENDPOINT = prop.getProperty("ENDPOINT");
		StringBuffer url = new StringBuffer(ENDPOINT);
		url.append("/");
		url.append(bucketName);
		url.append("/");
		url.append(directoryUnderBucket);

		return url.toString();
	}

	/**
	 * 获取默认的访问路径
	 * 
	 * @param bucketName
	 * @param directoryUnderBucket
	 * @return
	 */
	private static String getDirectoryByDefalutEndpoint(String bucketName, String directoryUnderBucket) {
		StringBuffer url = new StringBuffer(defaultEndpoint);
		url.append("/");
		url.append(bucketName);
		url.append("/");
		url.append(directoryUnderBucket);

		return url.toString();
	}
}

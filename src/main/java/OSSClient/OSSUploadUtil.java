package OSSClient;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import javax.ws.rs.core.MediaType;

import org.springframework.util.StringUtils;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.AppendObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;

/**
 * <pre>
 * 上传Object
 * 若未绑定域名：则访问路径：http://bj.bcebos.com/v1/binbinpictures/photo/test.png
 * （bucketName：binbinpictures,directoryUnderBucket：photo/test.png）
 * 
 * 若绑定域名：则访问路径：http://binbinpictures.bj.bcebos.com/photo/test.png
 * </pre>
 * 
 * @author Administrator
 * 
 */
public class OSSUploadUtil {
	private static OSSClient OSSClient = OSSClientUtil.getInstance();

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
	 * <pre>
	 * 文件路径形式上传文件
	 * 1、putObject函数支持不超过5GB的Object上传
	 * 2、BOS会在Header中返回Object的ETag作为文件标识
	 * </pre>
	 * 
	 * @throws IOException
	 */
	public static String putObject(String bucketName, String directoryUnderBucket, String filePath) throws IOException {
		if (StringUtils.isEmpty(bucketName) || StringUtils.isEmpty(directoryUnderBucket)
				|| StringUtils.isEmpty(filePath)) {
			return null;
		}

		if (!OSSBucketUtil.doesBucketExist(bucketName)) {
			OSSBucketUtil.createBucket(bucketName);
		}

		// 以文件形式上传Object
		File file = new File(filePath);
		OSSClient.putObject(bucketName, directoryUnderBucket, file);

		String directory = OSSDirectoryUtil.getDirectory(bucketName, directoryUnderBucket);
		return directory;
	}

	/**
	 * <pre>
	 * 文件路径形式上传文件
	 * 1、putObject函数支持不超过5GB的Object上传
	 * 2、BOS会在Header中返回Object的ETag作为文件标识
	 * </pre>
	 * 
	 * @throws IOException
	 */
	public static String putObject(String bucketName, String directoryUnderBucket, File file) throws IOException {
		if (StringUtils.isEmpty(bucketName) || StringUtils.isEmpty(directoryUnderBucket) || file == null) {
			return null;
		}

		if (!OSSBucketUtil.doesBucketExist(bucketName)) {
			OSSBucketUtil.createBucket(bucketName);
		}

		OSSClient.putObject(bucketName, directoryUnderBucket, file);

		String directory = OSSDirectoryUtil.getDirectory(bucketName, directoryUnderBucket);
		return directory;
	}

	/**
	 * 二进制形式上传文件
	 * 
	 * @param bucketName
	 * @param directoryUnderBucket
	 * @param b
	 */
	public static String pubObject(String bucketName, String directoryUnderBucket, byte[] b) {
		if (StringUtils.isEmpty(bucketName) || StringUtils.isEmpty(directoryUnderBucket) || b == null) {
			return null;
		}

		if (!OSSBucketUtil.doesBucketExist(bucketName)) {
			OSSBucketUtil.createBucket(bucketName);
		}

		InputStream content = new ByteArrayInputStream(b);
		// 创建上传Object的Metadata
		ObjectMetadata meta = new ObjectMetadata();
		// 必须设置ContentLength
		meta.setContentLength(b.length);
		// 设置一小时后过期
		Date expirationTime = new Date(new Date().getTime() + 3600 * 1000);
		meta.setExpirationTime(expirationTime);

		// 上传Object.
		OSSClient.putObject(bucketName, directoryUnderBucket, content, meta);

		String directory = OSSDirectoryUtil.getDirectory(bucketName, directoryUnderBucket);
		return directory;
	}

	/**
	 * 追加上传
	 * 
	 * @return
	 */
	public static String putAppendObject(String bucketName, String directoryUnderBucket, File file) {
		if (StringUtils.isEmpty(bucketName) || StringUtils.isEmpty(directoryUnderBucket) || file == null) {
			return null;
		}

		if (!OSSBucketUtil.doesBucketExist(bucketName)) {
			OSSBucketUtil.createBucket(bucketName);
		}

		AppendObjectRequest appendObjectRequest = new AppendObjectRequest(bucketName, directoryUnderBucket, file);

		// 设置追加位置,发送追加Object请求
		long length = getAppendNextPosition(bucketName, directoryUnderBucket);
		appendObjectRequest.setPosition(length);

		ObjectMetadata meta = getAppendObjectMetadata();
		appendObjectRequest.setMetadata(meta);

		OSSClient.appendObject(appendObjectRequest);

		String directory = OSSDirectoryUtil.getDirectory(bucketName, directoryUnderBucket);
		return directory;
	}

	/**
	 * 获取追加对象流
	 * 
	 * @return
	 */
	private static ObjectMetadata getAppendObjectMetadata() {
		// 设置content-type，注意设置object meta只能在使用Append创建Object设置
		ObjectMetadata meta = new ObjectMetadata();
		meta.setContentType("image/jpeg");
		return meta;
	}

	/**
	 * 获取追加文件的下一个位置
	 * 
	 * @param bucketName
	 * @param directoryUnderBucket
	 * @return
	 */
	private static long getAppendNextPosition(String bucketName, String directoryUnderBucket) {
		OSSObject object = null;
		try {
			object = OSSClient.getObject(bucketName, directoryUnderBucket);
		} catch (OSSException e) {
			e.printStackTrace();
		} catch (ClientException e) {
			e.printStackTrace();
		}

		long length = 0L;
		if (object != null) {
			ObjectMetadata objectMetadata = object.getObjectMetadata();
			length = objectMetadata.getContentLength();
		}
		return length;
	}

	/**
	 * 文件路径形式上传文件（高级）
	 * 
	 * @param bucketName
	 * @param directoryUnderBucket
	 * @param filePath
	 * @throws FileNotFoundException
	 */
	public static String putObjectAdvanced(String bucketName, String directoryUnderBucket, String filePath)
			throws FileNotFoundException {
		if (StringUtils.isEmpty(bucketName) || StringUtils.isEmpty(directoryUnderBucket)
				|| StringUtils.isEmpty(filePath)) {
			return null;
		}

		if (!OSSBucketUtil.doesBucketExist(bucketName)) {
			OSSBucketUtil.createBucket(bucketName);
		}

		File file = new File(filePath);
		if (file == null || file.isDirectory()) {
			return null;
		}

		InputStream input = new FileInputStream(file);
		ObjectMetadata metadata = initObjectMetadata();
		OSSClient.putObject(bucketName, directoryUnderBucket, input, metadata);

		String directory = OSSDirectoryUtil.getDirectory(bucketName, directoryUnderBucket);
		return directory;
	}

	/**
	 * 初始化上传输入流
	 * 
	 * @return
	 */
	private static ObjectMetadata initObjectMetadata() {
		// 初始化上传输入流
		ObjectMetadata metadata = new ObjectMetadata();

		// 设置ContentLength大小
		metadata.setContentLength(1000);
		// 设置自定义元数据name的值为my-data
		metadata.addUserMetadata("name", "my-data");
		// 设置ContentType
		metadata.setContentType(MediaType.APPLICATION_JSON);

		return metadata;
	}
}

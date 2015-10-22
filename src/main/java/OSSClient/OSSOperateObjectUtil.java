package OSSClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.core.MediaType;

import org.springframework.util.StringUtils;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.CopyObjectRequest;
import com.aliyun.oss.model.CopyObjectResult;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import com.aliyun.oss.model.ObjectMetadata;

/**
 * 操作Object工具
 * 
 * @author Administrator
 * 
 */
public class OSSOperateObjectUtil {
	private static OSSClient OSSClient = OSSClientUtil.getInstance();

	private static Properties prop;

	static {
		prop = new Properties();
		try {
			prop.load(OSSOperateObjectUtil.class.getResourceAsStream("/aliyunOSS.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <pre>
	 * 查看bucket中的Object列表
	 * 1、默认情况下，如果Bucket中的Object数量大于100，则只会返回100个Object，并且返回结果中IsTruncated值为True，并返回NextMarker做为下次读取的起点
	 * 2、若想增大返回Object的数目，可以使用Marker参数分次读取
	 * </pre>
	 */
	public static List<OSSObjectSummary> listObjects(String bucketName) {
		if (StringUtils.isEmpty(bucketName)) {
			return new ArrayList<OSSObjectSummary>();
		}

		// 获取指定Bucket下的所有Object信息
		ObjectListing listObjectsResponse = OSSClient.listObjects(bucketName);
		if (listObjectsResponse == null) {
			return new ArrayList<OSSObjectSummary>();
		}

		List<OSSObjectSummary> contents = listObjectsResponse.getObjectSummaries();
		return contents;
	}

	/**
	 * 查看bucket中的Object列表(高级)
	 */
	public static List<OSSObjectSummary> listObjectsAdvanced(String bucketName) {
		if (StringUtils.isEmpty(bucketName)) {
			return new ArrayList<OSSObjectSummary>();
		}

		// 构造ListObjectsRequest请求
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest(bucketName);

		// 设置参数
		listObjectsRequest.setDelimiter("/");
		listObjectsRequest.setMarker("123");
		listObjectsRequest.setPrefix("xx");
		listObjectsRequest.setMaxKeys(1000);

		ObjectListing listObjectsResponse = OSSClient.listObjects(listObjectsRequest);
		if (listObjectsResponse == null) {
			return new ArrayList<OSSObjectSummary>();
		}

		List<OSSObjectSummary> contents = listObjectsResponse.getObjectSummaries();
		return contents;
	}

	/**
	 * 删除Object
	 */
	public static void deleteObject(String bucketName, String objectKey) {
		if (StringUtils.isEmpty(bucketName) || StringUtils.isEmpty(objectKey)) {
			return;
		}

		try {
			OSSClient.deleteObject(bucketName, objectKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 拷贝Object
	 */
	public static String copyObject(String srcBucketName, String srcKey, String destBucketName, String destKey) {
		if (StringUtils.isEmpty(srcBucketName) || StringUtils.isEmpty(srcKey) || StringUtils.isEmpty(destBucketName)
				|| StringUtils.isEmpty(destKey)) {
			return null;
		}

		// 拷贝Object
		CopyObjectResult copyObjectResponse = OSSClient.copyObject(srcBucketName, srcKey, destBucketName, destKey);
		if (copyObjectResponse == null) {
			return null;
		}

		String eTag = copyObjectResponse.getETag();

		return eTag;
	}

	/**
	 * 拷贝Object(高级)
	 */
	public static String copyObjectAdvanced(String srcBucketName, String srcKey, String destBucketName, String destKey) {
		if (StringUtils.isEmpty(srcBucketName) || StringUtils.isEmpty(srcKey) || StringUtils.isEmpty(destBucketName)
				|| StringUtils.isEmpty(destKey)) {
			return null;
		}

		// 创建CopyObjectRequest对象
		CopyObjectRequest copyObjectRequest = new CopyObjectRequest(srcBucketName, srcKey, destBucketName, destKey);

		// 设置新的Metadata
		ObjectMetadata metadata = new ObjectMetadata();
		// 设置ContentLength大小
		metadata.setContentLength(1000);
		// 设置自定义元数据name的值为my-data
		metadata.addUserMetadata("name", "my-data");
		// 设置ContentType
		metadata.setContentType(MediaType.APPLICATION_JSON);

		copyObjectRequest.setNewObjectMetadata(metadata);

		// 复制Object
		CopyObjectResult copyObjectResponse = OSSClient.copyObject(copyObjectRequest);
		if (copyObjectResponse == null) {
			return null;
		}

		String eTag = copyObjectResponse.getETag();

		return eTag;
	}
}

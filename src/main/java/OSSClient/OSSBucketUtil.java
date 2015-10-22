package OSSClient;

import java.util.List;

import org.springframework.util.StringUtils;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.AccessControlList;
import com.aliyun.oss.model.Bucket;
import com.aliyun.oss.model.CannedAccessControlList;

/**
 * bucket操作工具
 * 
 * @author Administrator
 * 
 */
public class OSSBucketUtil {
	private static OSSClient OSSClient = OSSClientUtil.getInstance();

	/**
	 * 创建bucket
	 */
	public static void createBucket(String bucketName) {
		OSSClient.createBucket(bucketName);
	}

	/**
	 * 获取所有buckets
	 */
	public static List<Bucket> getBuckets() {
		List<Bucket> buckets = OSSClient.listBuckets();
		return buckets;
	}

	/**
	 * 判断bucket是否存在
	 */
	public static boolean doesBucketExist(String bucketName) {
		if (StringUtils.isEmpty(bucketName)) {
			return false;
		}

		boolean exists = OSSClient.doesBucketExist(bucketName);
		return exists;
	}

	/**
	 * 删除bucket
	 * 
	 * @param client
	 * @param bucketName
	 */
	public static void deleteBucket(String bucketName) {
		if (!doesBucketExist(bucketName)) {
			return;
		}

		OSSClient.deleteBucket(bucketName);
	}

	/**
	 * 设置bucket访问权限
	 * 
	 * @param client
	 * @param bucketName
	 */
	public static void setBucketPrivate(String bucketName, CannedAccessControlList acl) {
		if (!doesBucketExist(bucketName)) {
			return;
		}

		OSSClient.setBucketAcl(bucketName, acl);
	}

	/**
	 * 获取bucket访问权限
	 * 
	 * @param bucketName
	 * @return
	 */
	public static AccessControlList getBucketPrivate(String bucketName) {
		if (!doesBucketExist(bucketName)) {
			return null;
		}

		AccessControlList accessControlList = OSSClient.getBucketAcl(bucketName);
		return accessControlList;
	}

	/**
	 * 获取bucket地址
	 * 
	 * @param bucketName
	 * @return
	 */
	public static String getBucketLocation(String bucketName) {
		if (!doesBucketExist(bucketName)) {
			return null;
		}

		String location = OSSClient.getBucketLocation(bucketName);
		return location;
	}
}

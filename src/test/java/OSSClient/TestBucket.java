package OSSClient;

import org.junit.Test;

/**
 * 测试bucket
 * 
 * @author Administrator
 *
 */
public class TestBucket {

	@Test
	public void createBucket() {
		String bucketName = "cndwine";
		OSSBucketUtil.createBucket(bucketName);
	}

	@Test
	public void getBuckets() {
		OSSBucketUtil.getBuckets();
	}

}

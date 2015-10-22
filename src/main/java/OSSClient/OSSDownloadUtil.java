package OSSClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;

/**
 * 下载Object
 * 
 * @author Administrator
 * 
 */
public class OSSDownloadUtil {
	private static OSSClient OSSClient = OSSClientUtil.getInstance();

	/**
	 * 获取bucket中的object
	 * 
	 * @throws IOException
	 */
	public static void getObject(String bucketName, String objectKey) throws IOException {
		// 获取Object，返回结果为OSSObject对象
		OSSObject object = OSSClient.getObject(bucketName, objectKey);
		// 获取ObjectMeta
		ObjectMetadata meta = object.getObjectMetadata();
		System.out.println(meta.getContentLength());

		// 获取Object的输入流
		InputStream objectContent = object.getObjectContent();
		System.out.println(objectContent.read());

		// 只获取ObjectMetadata
		ObjectMetadata objectMetadata = OSSClient.getObjectMetadata(bucketName, objectKey);
		System.out.println(objectMetadata.getContentType());

		// 关闭流
		objectContent.close();
	}

	/**
	 * 获取bucket中的object(高级)
	 * 
	 * @throws IOException
	 */
	public static void getObjectAdvanced(String bucketName, String objectKey) throws IOException {
		// 新建GetObjectRequest
		GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, objectKey);
		// 获取0~100字节范围内的数据
		getObjectRequest.setRange(0, 100);

		// 获取Object，返回结果为OSSObject对象
		OSSObject object = OSSClient.getObject(getObjectRequest);
		// 获取ObjectMeta
		ObjectMetadata meta = object.getObjectMetadata();
		System.out.println(meta.getETag());
		// 获取Object的输入流
		InputStream objectContent = object.getObjectContent();
		System.out.println(objectContent.read());

		// 获取Object至文件中
		File file = new File("/path/to/file");
		OSSClient.getObject(getObjectRequest, file);

		// 关闭流
		objectContent.close();
	}
}

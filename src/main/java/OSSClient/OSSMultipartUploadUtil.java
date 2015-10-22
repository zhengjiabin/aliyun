package OSSClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.AbortMultipartUploadRequest;
import com.aliyun.oss.model.CompleteMultipartUploadRequest;
import com.aliyun.oss.model.CompleteMultipartUploadResult;
import com.aliyun.oss.model.InitiateMultipartUploadRequest;
import com.aliyun.oss.model.InitiateMultipartUploadResult;
import com.aliyun.oss.model.ListMultipartUploadsRequest;
import com.aliyun.oss.model.ListPartsRequest;
import com.aliyun.oss.model.MultipartUpload;
import com.aliyun.oss.model.MultipartUploadListing;
import com.aliyun.oss.model.PartETag;
import com.aliyun.oss.model.PartListing;
import com.aliyun.oss.model.PartSummary;
import com.aliyun.oss.model.UploadPartRequest;
import com.aliyun.oss.model.UploadPartResult;

/**
 * 分块上传工具
 * 
 * @author Administrator
 * 
 */
public class OSSMultipartUploadUtil {
	private static OSSClient OSSClient = OSSClientUtil.getInstance();

	/**
	 * <pre>
	 * 分块上传Object
	 * 应用场景如下：
	 * 1、需要支持断点上传。
	 * 2、上传超过5GB大小的文件。
	 * 3、网络条件较差，和BOS的服务器之间的连接经常断开。
	 * 4、需要流式地上传文件。
	 * 5、上传文件之前，无法确定上传文件的大小。
	 * </pre>
	 * 
	 * @throws IOException
	 */
	public static void multipartUpload(String bucketName, String objectKey) throws IOException {
		// 初始化分块上传组件
		InitiateMultipartUploadResult result = initiateMultipartUploadResult(bucketName, objectKey);

		// 开始上传
		List<PartETag> partETags = startMultipartUpload(result);

		// 完成分块上传
		completeMultipartUpload(result, partETags);
	}

	/**
	 * 初始化分块上传组件
	 */
	private static InitiateMultipartUploadResult initiateMultipartUploadResult(String bucketName, String objectKey) {
		// 初始化multipart Upload
		InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectKey);
		InitiateMultipartUploadResult result = OSSClient.initiateMultipartUpload(request);

		// 打印UploadId
		System.out.println("UploadId: " + result.getUploadId());

		return result;
	}

	/**
	 * <pre>
	 * 测试分块上传Object
	 * 
	 * 注意点：
	 * 1、UploadPart 方法要求除最后一个Part以外，其他的Part大小都要大于5MB。
	 * 2、但是Upload Part接口并不会立即校验上传Part的大小；只有当Complete Multipart Upload的时候才会校验。
	 * 3、为了保证数据在网络传输过程中不出现错误，建议用户在收到BOS的返回请求后，用UploadPart返回的Content-MD5值验证上传数据的正确性。
	 * 4、Part号码的范围是1~10000。如果超出这个范围，BOS将返回InvalidArgument的错误码。
	 * 5、每次上传Part时都要把流定位到此次上传块开头所对应的位置。
	 * 6、每次上传Part之后，BOS的返回结果会包含一个 PartETag 对象，它是上传块的ETag与块编号（PartNumber）的组合，在后续完成分块上传的步骤中会用到它，因此需要将其保存起来。
	 * 7、一般来讲这些 PartETag 对象将被保存到List中
	 * </pre>
	 * 
	 * @param result
	 *            分块上传配置组件
	 * @throws IOException
	 */
	private static List<PartETag> startMultipartUpload(InitiateMultipartUploadResult result) throws IOException {
		// 设置每块为 5MB
		final long partSize = 1024 * 1024 * 5L;
		File partFile = new File("/path/to/file.zip");
		int partCount = getPartCount(partFile, partSize);

		// 新建一个List保存每个分块上传后的ETag和PartNumber
		List<PartETag> partETags = new ArrayList<PartETag>();

		UploadPartResult uploadPartResult = null;
		for (int i = 0; i < partCount; i++) {
			uploadPartResult = uploadPart(partFile, partSize, i, result);

			// 将返回的PartETag保存到List中。
			partETags.add(uploadPartResult.getPartETag());
		}

		return partETags;
	}

	/**
	 * 每个分块的上传
	 * 
	 * @param partFile
	 * @param partSize
	 * @param currentPart
	 * @param InitiateMultipartUploadResult
	 * @return
	 * @throws IOException
	 */
	private static UploadPartResult uploadPart(File partFile, long partSize, int currentPart,
			InitiateMultipartUploadResult result) throws IOException {
		// 跳到每个分块的开头
		long skipBytes = partSize * currentPart;
		// 计算每个分块的大小
		long size = partSize < partFile.length() - skipBytes ? partSize : partFile.length() - skipBytes;

		// 获取文件流
		FileInputStream fis = new FileInputStream(partFile);
		fis.skip(skipBytes);

		// 创建UploadPartRequest，上传分块
		UploadPartRequest uploadPartRequest = new UploadPartRequest();
		uploadPartRequest.setBucketName(result.getBucketName());
		uploadPartRequest.setKey(result.getKey());
		uploadPartRequest.setUploadId(result.getUploadId());
		uploadPartRequest.setInputStream(fis);
		uploadPartRequest.setPartSize(size);
		uploadPartRequest.setPartNumber(currentPart + 1);
		UploadPartResult uploadPartResult = OSSClient.uploadPart(uploadPartRequest);

		// 关闭文件
		fis.close();

		return uploadPartResult;
	}

	/**
	 * 获取分块数目
	 * 
	 * @param partFile
	 * @return
	 */
	private static int getPartCount(File partFile, long partSize) {
		int partCount = (int) (partFile.length() / partSize);
		if (partFile.length() % partSize != 0) {
			partCount++;
		}

		return partCount;
	}

	/**
	 * 完成分块上传
	 * 
	 * @throws IOException
	 */
	private static void completeMultipartUpload(InitiateMultipartUploadResult result, List<PartETag> partETags)
			throws IOException {
		CompleteMultipartUploadRequest request = new CompleteMultipartUploadRequest(result.getBucketName(),
				result.getKey(), result.getUploadId(), partETags);

		// 完成分块上传
		CompleteMultipartUploadResult completeResult = OSSClient.completeMultipartUpload(request);

		// 打印Object的ETag
		System.out.println(completeResult.getETag());
	}

	/**
	 * 取消分块上传
	 */
	public static void abortMultipartUpload(String bucketName, String objectKey) {
		// 初始化分块上传组件
		InitiateMultipartUploadResult result = initiateMultipartUploadResult(bucketName, objectKey);
		AbortMultipartUploadRequest request = new AbortMultipartUploadRequest(bucketName, objectKey,
				result.getUploadId());

		// 取消分块上传
		OSSClient.abortMultipartUpload(request);
	}

	/**
	 * 获取所有的 Multipart上传事件
	 * 
	 * @param bucketName
	 */
	public static void listMultipartUploads(String bucketName) {
		ListMultipartUploadsRequest request = new ListMultipartUploadsRequest(bucketName);

		// 获取Bucket内所有上传事件
		MultipartUploadListing listing = OSSClient.listMultipartUploads(request);

		// 遍历所有上传事件
		for (MultipartUpload multipartUpload : listing.getMultipartUploads()) {
			System.out.println("Key: " + multipartUpload.getKey() + " UploadId: " + multipartUpload.getUploadId());
		}
	}

	/**
	 * 获取所有已上传的块信息
	 * 
	 * @param bucketName
	 * @param objectKey
	 */
	public static void listParts(String bucketName, String objectKey) {
		// 初始化分块上传组件
		InitiateMultipartUploadResult result = initiateMultipartUploadResult(bucketName, objectKey);

		ListPartsRequest listPartsRequest = new ListPartsRequest(bucketName, objectKey, result.getUploadId());

		// 获取上传的所有Part信息
		PartListing partListing = OSSClient.listParts(listPartsRequest);

		// 遍历所有Part
		for (PartSummary part : partListing.getParts()) {
			System.out.println("PartNumber: " + part.getPartNumber() + " ETag: " + part.getETag());
		}
	}
}

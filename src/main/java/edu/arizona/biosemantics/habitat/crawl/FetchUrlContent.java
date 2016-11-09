package edu.arizona.biosemantics.habitat.crawl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

/**
 * Obtain the content from Url
 */
public class FetchUrlContent {

	private static int retryTime = 0;

	/**
	 * 获取指定URL的内容
	 */
	public static String getContent(String url, Hashtable<String, String> headers, String encoding) {
		// 查询结果
		DefaultHttpClient httpclient = null;
		StringBuffer sb = new StringBuffer();
		String finalString = null;
		try {
			// 创建client实例
			httpclient = new DefaultHttpClient();
			
			HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 60 * 1000);//链接超时设置为1分钟
			HttpConnectionParams.setSoTimeout(httpclient.getParams(), 60 * 1000);//页面读取超时设置为1分钟

		    //在执行过程中发现可修复的异常时，选择合适的异常处理方式
			HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {

			    public boolean retryRequest(
			            IOException exception, 
			            int executionCount,
			            HttpContext context) {
			        if (executionCount >= 5) {
			            // Do not retry if over max retry count
			            return false;
			        }
			        if (exception instanceof NoHttpResponseException) {
			            // Retry if the server dropped connection on us
			            return true;
			        }
			        if (exception instanceof SSLHandshakeException) {
			            // Do not retry on SSL handshake exception
			            return false;
			        }
			        HttpRequest request = (HttpRequest) context.getAttribute(
			                ExecutionContext.HTTP_REQUEST);
			        boolean idempotent = !(request instanceof HttpEntityEnclosingRequest); 
			        if (idempotent) {
			            // Retry if the request is considered idempotent 
			            return true;
			        }
			        return false;
			    }

			};
			httpclient.setHttpRequestRetryHandler(retryHandler);
			
			// 通过HttpGet来获取
			HttpGet httpGet = new HttpGet(url);
			//httpGet.setURI(UriBuilder.fromPath(url).build(url));
			if (headers == null) {// 默认设置
				httpGet.addHeader(
						"Accept",
						"application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
				httpGet.addHeader("Cache-Control", "max-age=0");
				httpGet.addHeader(
						"User-Agent",
						"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/534.13 (KHTML, like Gecko) Chrome/9.0.597.98 Safari/534.13");
				// httpGet.addHeader("Accept-Encoding", "gzip,deflate,sdch");
				httpGet.addHeader("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.3");
			} else {
				Set<String> headSet = headers.keySet();
				Iterator<String> headSetIter = headSet.iterator();
				while (headSetIter.hasNext()) {
					String key = (String) headSetIter.next();
					httpGet.addHeader(key, (String) headers.get(key));
				}
			}
			
			HttpResponse response = httpclient.execute(httpGet);// 执行请求
			HttpEntity entity = response.getEntity();// 获取响应实体
			String reEncode = null;
			if(entity!=null&&entity.getContentType()!=null){
				reEncode = entity.getContentType().getValue();
			}
			if(reEncode!=null&&!"".equals(reEncode)){
				reEncode=reEncode.replaceAll(".+?charset=","");
			}
			if(reEncode!=null&&!"".equals(reEncode)&&reEncode.indexOf("/")==-1){
				encoding = reEncode;
			}
			if(encoding==null){
				//encoding = "UTF-8";//没有指定，同时也没有获取到
			}
			if (entity.isStreaming()) {
				if(encoding==null){
					// 读取内容
					Integer contentLength = new Long(entity.getContentLength()).intValue();
					if(contentLength>0){
			            byte[] responseBody = new byte[contentLength];
			            entity.getContent().read(responseBody);  
			  
			            // 处理内容  
			            String firstWebHtml = new String(responseBody, "gbk");  
			              
			            String charsetStr = getContentCharset(firstWebHtml);  
			            //System.out.println("真正的编码是：" + charsetStr);  
			            finalString = new String(responseBody, charsetStr); 
			          
					}else{
						InputStream txtis = entity.getContent();
					        byte b[] = new byte[409600];  
					        int len = 0;  
					        int temp=0;          //所有读取的内容都使用temp接收  
					        while((temp=txtis.read())!=-1){    //当没有读取完时，继续读取  
					            b[len]=(byte)temp;  
					            len++;  
					        } 
					        byte f[] =new byte[len];
					        System.arraycopy(b, 0, f, 0, len);
					        //System.out.println(new String(b,0,len)); 
					        txtis.close();
					        
					        String charsetStr = getContentCharset(new String(f));  
				           // System.out.println("真正的编码是：" + charsetStr);  
				            finalString = new String(f, charsetStr); 
					}
		            
				}else{
					InputStream is = entity.getContent();
					BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding));
					String line = null;
					// 获取二次请求的地址，用于下载列表
					while ((line = br.readLine()) != null) {
						sb.append(line);
						sb.append("\n");
					}
					is.close();
					finalString=sb.toString();
				}
			}
		} catch (ClientProtocolException e) {
			System.err.println("协议不正确");
		} catch (IOException e) {
			//e.printStackTrace();
			System.err.println("IO出错");
			retryTime++;
			if (retryTime <= 10)
				return getContent(url, headers, encoding);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally {
			// 关闭连接
			if (httpclient != null) {
				httpclient.getConnectionManager().shutdown();
			}
			return finalString;
		}

		
	}

/**
 *获取Response内容字符集
 * 
 * @param response
 * @return
 */
public static String getContentCharset(String headerHtml) {
	String charset = "ISO_8859-1";
	if (headerHtml != null) {
		if (matcher(headerHtml, "(charset)\\s?=\\s?(utf-?8)")) {
			charset = "utf-8";
		} else if (matcher(headerHtml, "(charset)\\s?=\\s?(gbk)")) {
			charset = "gbk";
		} else if (matcher(headerHtml, "(charset)\\s?=\\s?(gb2312)")) {
			charset = "gb2312";// http://ks.examda.com
		}
	}
	return charset;
}

/**
 *正则匹配
 * 
 * @param s
 * @param pattern
 * @return
 */
public static boolean matcher(String s, String pattern) {
	Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE);
	Matcher matcher = p.matcher(s);
	if (matcher.find()) {
		return true;
	} else {
		return false;
	}
}

	
	/**
	 * 获取指定URL的内容
	 */
	public static String getContent(DefaultHttpClient httpclient, String url, Hashtable<String, String> headers, String encoding) {
		StringBuffer sb = new StringBuffer();
		try {
			// 通过HttpGet来获取
			HttpGet httpGet = new HttpGet(url);
			if (headers == null) {// 默认设置
				httpGet.addHeader("Accept","application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
				httpGet.addHeader("Cache-Control", "max-age=0");
				httpGet.addHeader("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/534.13 (KHTML, like Gecko) Chrome/9.0.597.98 Safari/534.13");
				httpGet.addHeader("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.3");
			} else {
				Set<String> headSet = headers.keySet();
				Iterator<String> headSetIter = headSet.iterator();
				while (headSetIter.hasNext()) {
					String key = (String) headSetIter.next();
					httpGet.addHeader(key, (String) headers.get(key));
				}
			}

			HttpResponse response = httpclient.execute(httpGet);// 执行请求
			HttpEntity entity = response.getEntity();// 获取响应实体
			String reEncode = entity.getContentType().getValue();
			if(reEncode!=null&&!"".equals(reEncode)){
				reEncode=reEncode.replaceAll(".+?charset=","");
			}
			if(reEncode!=null&&!"".equals(reEncode)&&reEncode.indexOf("/")==-1){
				encoding = reEncode;
			}
			if(encoding==null){
				encoding = "UTF-8";//没有指定，同时也没有获取到
			}
			
			if (entity.isStreaming()) {
				InputStream is = entity.getContent();
				BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding));
				String line = null;
				// 获取二次请求的地址，用于下载列表
				while ((line = br.readLine()) != null) {
					sb.append(line);
					sb.append("\n");
				}
				is.close();
			}
		} catch (ClientProtocolException e) {
			System.err.println("协议不正确");
		} catch (IOException e) {
			System.err.println("IO出错");
		}
	  return sb.toString();
	}
	
	
	
	/**
	 * 获取指定URL的内容
	 */
	public String postContent(String url, Hashtable<String, String> headers,
			List<NameValuePair> postParams, String encoding) {
		// 查询结果
		DefaultHttpClient httpclient = null;
		StringBuffer sb = new StringBuffer();
		try {
			// 创建client实例
			httpclient = new DefaultHttpClient();
			
			HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 60 * 1000);//链接超时设置为1分钟
			HttpConnectionParams.setSoTimeout(httpclient.getParams(), 60 * 1000);//页面读取超时设置为1分钟

		    //在执行过程中发现可修复的异常时，选择合适的异常处理方式
			HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {

			    public boolean retryRequest(
			            IOException exception, 
			            int executionCount,
			            HttpContext context) {
			        if (executionCount >= 5) {
			            // Do not retry if over max retry count
			            return false;
			        }
			        if (exception instanceof NoHttpResponseException) {
			            // Retry if the server dropped connection on us
			            return true;
			        }
			        if (exception instanceof SSLHandshakeException) {
			            // Do not retry on SSL handshake exception
			            return false;
			        }
			        HttpRequest request = (HttpRequest) context.getAttribute(
			                ExecutionContext.HTTP_REQUEST);
			        boolean idempotent = !(request instanceof HttpEntityEnclosingRequest); 
			        if (idempotent) {
			            // Retry if the request is considered idempotent 
			            return true;
			        }
			        return false;
			    }

			};

			httpclient.setHttpRequestRetryHandler(retryHandler);

			// 通过HttpPost来获取
			HttpPost httpPost = new HttpPost(url);

			if (headers == null) {// 默认设置
				httpPost.addHeader(
						"Accept",
						"application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
				httpPost.addHeader("Cache-Control", "max-age=0");
				httpPost.addHeader(
						"User-Agent",
						"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/534.13 (KHTML, like Gecko) Chrome/9.0.597.98 Safari/534.13");
				// httpGet.addHeader("Accept-Encoding", "gzip,deflate,sdch");
				httpPost.addHeader("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.3");
			} else {
				Set<String> headSet = headers.keySet();
				Iterator<String> headSetIter = headSet.iterator();
				while (headSetIter.hasNext()) {
					String key = (String) headSetIter.next();
					httpPost.addHeader(key, (String) headers.get(key));
				}
			}

			if (postParams != null) {
				httpPost.setEntity(new UrlEncodedFormEntity(postParams));
				HttpParams params = httpPost.getParams();
				//System.out.println(params.getParameter("KeyWord"));
			}

			HttpResponse response = httpclient.execute(httpPost);// 执行请求

			// response.setEntity(new
			// GzipDecompressingEntity(response.getEntity()));
			HttpEntity entity = response.getEntity();// 获取响应实体

			if (entity.isStreaming()) {
				InputStream is = entity.getContent();
				BufferedReader br = new BufferedReader(new InputStreamReader(
						is, encoding));
				String line = null;
				// 获取二次请求的地址，用于下载列表
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				is.close();
			}
		} catch (ClientProtocolException e) {
			System.err.println("协议不正确");
		} catch (IOException e) {
			System.err.println("IO出错");
			retryTime++;
			if (retryTime <= 10)
				return this.postContent(url, headers,postParams, encoding);
		} finally {
			// 关闭连接
			if (httpclient != null) {
				httpclient.getConnectionManager().shutdown();
			}
			return sb.toString();
		}

		
	}

	public static void main(String[] args) {
		FetchUrlContent fuc = new FetchUrlContent();
		String url ="http://www.baidu.com/link?url=b5848d7eec693340437dea3bf3bca880c6e6dcd60610adf83cd68499a49b48202860a4f779deb89a39b3469431fcc60b5fb30355fe258a86b24cecff3d364b48923172c0efedab98fe1973b1f0e0ce19641d0d723ad79671b1794605b6a1807f8c4d1d7e5a5bdc96e419dff1d20330ce39a16eb0b9e48c7f150e48cd09e550f6e757a01f3f901e38b40abcbc7c82b9dcb5ceab5caf496496d262745b6d6eba19bf578a3280b405654adb0afb9ae7a0c9388d5d5194ec3a6697a665f44bf375b08e963b0829d003f312bf8a0a16873b27455108b3efb59c96840ad23f744690e3f4ca74b27a82d00d6f139ae04af729db1edb98c67467be9e3d344a95ca1bea9a8f3842bdfed7c4f2f9719b5cb708dc6c2ce48f15e4f0bae252a569f9680e0121f8fe33d78a5bded2866642c9aee01e129a339dd2e1a3a9e6c010d2bc1f99736e6c6064110c696a52b82a1b38774148964511840436d2f29bf62081258f2aebac5573358cc3c0659257bfcf547b398ac9eff77129fc130c6603cd60b31f6a7ca731ff64ab6197a201f156e5416cada010838e2aa087425d23664c075ff6d8df3cd5";
		
		Hashtable<String, String> headers = new Hashtable<String, String>();
		headers.put(
				"Accept",
				"application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
		headers.put("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.3");
		headers.put("Accept-Language", "zh-CN,zh;q=0.8");
		headers.put("Cache-Control", "max-age=0");
		headers.put("Connection", "keep-alive");
		headers.put(
				"Cookie",
				"BAIDUID=F253FBFA2FE498C4A24D85C21EC8D371:FG=1; BAIDU_WISE_UID=bd_1337764956_388; BDUT=4u2g7A2E437F12C360D53A78D9D26BE6CB8D13819c747fd4");
		headers.put("Host", "passport.baidu.com");
		headers.put(
				"User-Agent",
				"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/534.13 (KHTML, like Gecko) Chrome/9.0.597.98 Safari/534.13");

		
		String content = fuc
				.getContent(
						url,
						null, "utf-8");
		///System.out.println(content);
		
		
		//System.out.println("url="+fuc.getRedirectUrl(url, headers,  "utf-8"));
	}
	
	
	/**
	 * obtain the redirect URL
	 */
	public static String getRedirectUrl(String url, Hashtable<String, String> headers,String encoding){
		// 查询结果
		DefaultHttpClient httpclient = new DefaultHttpClient();
		String newUri = null;
		try {
			// 创建client实例
			HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 60 * 1000);//链接超时设置为1分钟
			HttpConnectionParams.setSoTimeout(httpclient.getParams(), 60 * 1000);//页面读取超时设置为1分钟
			HttpParams params = httpclient.getParams();  
			params.setParameter(ClientPNames.HANDLE_REDIRECTS, false);  
			// 通过HttpGet来获取
			HttpGet httpGet = new HttpGet(url);

			if (headers == null) {// 默认设置
				httpGet.addHeader(
						"Accept",
						"application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
				httpGet.addHeader("Cache-Control", "max-age=0");
				httpGet.addHeader(
						"User-Agent",
						"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/534.13 (KHTML, like Gecko) Chrome/9.0.597.98 Safari/534.13");
				// httpGet.addHeader("Accept-Encoding", "gzip,deflate,sdch");
				httpGet.addHeader("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.3");
			} else {
				Set<String> headSet = headers.keySet();
				Iterator<String> headSetIter = headSet.iterator();
				while (headSetIter.hasNext()) {
					String key = (String) headSetIter.next();
					httpGet.addHeader(key, (String) headers.get(key));
				}
			}

			HttpResponse response = httpclient.execute(httpGet);// 执行请求
			  // 判断页面返回状态判断是否进行转向抓取新链接
	        int statusCode = response.getStatusLine().getStatusCode();
	        if ((statusCode == HttpStatus.SC_MOVED_PERMANENTLY) ||
	                (statusCode == HttpStatus.SC_MOVED_TEMPORARILY) ||
	                (statusCode == HttpStatus.SC_SEE_OTHER) ||
	                (statusCode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
	            // 此处重定向处理  此处还未验证
	            newUri = response.getLastHeader("Location").getValue();
	        }
		} catch (ClientProtocolException e) {
			System.err.println("协议不正确");
		} catch (IOException e) {
			System.err.println("IO出错");
			retryTime++;
			if (retryTime <= 10)
				return getRedirectUrl(url, headers, encoding);
		} finally {
			// 关闭连接
			if (httpclient != null) {
				httpclient.getConnectionManager().shutdown();
			}
			return newUri;
		}
	}
}
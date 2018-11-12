package jason.ppcr;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Hello world!
 *
 */
public class App {

	public static void main(String[] args) {
		App crawler = new App();
		List<String> name = new ArrayList();
		name.add("000001");
		name.add("000002");
		name.add("000003");
		name.add("000004");
		List<String> link = new ArrayList();
		link.add("https://www.baidu.com");
		link.add("https://www.qq.com");
		link.add("https://www.163.com");
		link.add("https://weixin.qq.com/");
		for (int i = 0; i < name.size(); i++) {
			crawler.getHtmlTextByPath(name.get(i),link.get(i));
		}
	}

	// 根据url从网络获取网页文本
	public Document getHtmlTextByUrl(String url) {
		Document doc = null;
		try {
			// doc = Jsoup.connect(url).timeout(5000000).get();
			int i = (int) (Math.random() * 1000); // 做一个随机延时，防止网站屏蔽
			while (i != 0) {
				i--;
			}
			doc = Jsoup.connect(url).data("query", "Java").userAgent("Mozilla").cookie("auth", "token").timeout(300000).post();
		} catch (IOException e) {
			e.printStackTrace();
			try {
				doc = Jsoup.connect(url).timeout(5000000).get();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		return doc;
	}

	// 根据本地路径获取网页文本，如果不存在就通过url从网络获取并保存

	public Document getHtmlTextByPath(String name, String url) {
		String path = "E:/Html/" + name + ".html";
		Document doc = null;
		File input = new File(path);
		String urlcat = url;
		try {
			doc = Jsoup.parse(input, "GBK");
			if (!doc.children().isEmpty()) {
				System.out.println("已经存在");
			}
		} catch (IOException e) {
			System.out.println("文件未找到，正在从网络获取.......");
			doc = this.getHtmlTextByUrl(url);
			// 并且保存到本地
			this.Save_Html(url, name);
		}
		return doc;
	} // 此处为保存网页的函数

	// 将网页保存在本地（通过url,和保存的名字）
	public void Save_Html(String url, String name) {
		try {
			name = name + ".html";
			// System.out.print(name);
			File dest = new File("E:/Html/" + name);// D:\Html
			// 接收字节输入流
			InputStream is;
			// 字节输出流
			FileOutputStream fos = new FileOutputStream(dest);

			URL temp = new URL(url);
			is = temp.openStream();

			// 为字节输入流加缓冲
			BufferedInputStream bis = new BufferedInputStream(is);
			// 为字节输出流加缓冲
			BufferedOutputStream bos = new BufferedOutputStream(fos);

			int length;

			byte[] bytes = new byte[1024 * 20];
			while ((length = bis.read(bytes, 0, bytes.length)) != -1) {
				fos.write(bytes, 0, length);
			}

			bos.close();
			fos.close();
			bis.close();
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 根据元素属性获取某个元素内的elements列表
	public Elements getEleByClass(Document doc, String className) {
		Elements elements = null;
		elements = doc.select(className);// 这里把我们获取到的html文本doc，和工具class名，注意<tr class="provincetr">
		return elements; // 此处返回的就是所有的tr集合
	}

	// 获取省 、市 、县等的信息
	public ArrayList getProvince(String name, String url, String type) {
		ArrayList result = new ArrayList();
		// "tr.provincetr"
		String classtype = "tr." + type + "tr";
		// 从网络上获取网页
		// Document doc = this.getHtmlTextByUrl(url);
		// 从本地获取网页,如果没有则从网络获取
		Document doc2 = this.getHtmlTextByPath(name, url);
		System.out.println(name);
		if (doc2 != null) {
			Elements es = this.getEleByClass(doc2, classtype); // tr的集合
			for (Element e : es) // 依次循环每个元素，也就是一个tr
			{
				if (e != null) {
					for (Element ec : e.children()) // 一个tr的子元素td，td内包含a标签
					{
						String[] prv = new String[4]; // 身份的信息： 原来的url（当前url） 名称（北京） 现在url（也就是北京的url） 类型（prv）省
						if (ec.children().first() != null) {
							// 原来的url
							prv[0] = url; // 就是参数url
							// 身份名称
							System.out.println(ec.children().first().ownText());
							prv[1] = ec.children().first().ownText(); // a标签文本 如:北京
							// 身份url地址
							// System.out.println(ec.children().first().attr("href"));
							// http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2015/index.html
							String ownurl = ec.children().first().attr("abs:href"); // 北京的url
							// 因为如果从本地取得话，会成为本地url，所以保留第一次从网络上的url，保证url不为空
							if (ownurl.length() < 10) {
//								connectOrcl c = new connectOrcl();
//								ownurl = c.selectOne(prv[1]); // 从数据库中取，这是另一个调用数据库函数，根据名称取url
							}
							prv[2] = ownurl; // 如：北京自己的url
							System.out.println(prv[2]);
							// 级别
							prv[3] = type; // 就是刚刚传的类型，后面会有city 、county等
							// 将所有身份加入list中
							result.add(prv);
						}
					}
				}
			}
		}
		return result; // 反回所有的省份信息集合，存数据库，字段类型为： baseurl name ownurl levelname（type） updatetime
	}
}

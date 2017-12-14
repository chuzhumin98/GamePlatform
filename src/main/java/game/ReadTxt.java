import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.Vector;

public class ReadTxt {
	public static Vector<String> readTxtFile(String filePath) {
		Vector<String> vv = new Vector<String>();
		try {
			String encoding = "GBK";
			File file = new File(filePath);
			if (file.isFile() && file.exists()) { // �ж��ļ��Ƿ����
				InputStreamReader read = new InputStreamReader(
						new FileInputStream(file), encoding);// ���ǵ������ʽ
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					vv.add(lineTxt);
				}
				read.close();
			} else {
				System.out.println("�Ҳ���ָ�����ļ�");
			}
		} catch (Exception e) {
			System.out.println("��ȡ�ļ����ݳ���");
			e.printStackTrace();
		}
		return vv;
	}

	public static String getRandomConent(String filePath) {
		Vector<String> vv = readTxtFile(filePath);
		int len = vv.size();
		Random r = new Random();
		int now = r.nextInt(len);
		String content = vv.get(now);
		return content;
	}

	public static void main(String argv[]) {
		String filePath = "E:\\myPro\\UserMan\\src\\lucene\\dictionary.txt";
		System.out.println(readTxtFile(filePath));
		System.out.println(getRandomConent(filePath));
	}
}

package PreAnTable;

import java.util.HashMap;
import java.util.Map;

public class CommProcess {
	public static class TokenProcesser {
		private int start;
		private String pr;

		public TokenProcesser(String pr) {
			this.pr = pr;
			start = 0;
		}

		public boolean hasNextToken() {
			return start < pr.length();
		}
		/**
		 * ��ȡ��һ���ַ�
		 * @return û����һ���ַ������ַ�������Ч��Χ���򷵻�null
		 */
		public String nextToken() {
			if (!hasNextToken())
				return null;
			char a = pr.charAt(start);
			start++;
			if ((a >= '!' && a <= '~') || a == '��') {//��Ч��Χ��
				if (a >= 'A' && a <= 'Z') {// ����Ǵ�д��ĸ����Ҫ����A'��
					if (start<pr.length() && pr.charAt(start ) == '\'') {// ����A'��Ϊ����
						start++;
						return a+"\'";
					}
				}
				return Character.toString(a);
			} else {
				return null;
			}
		}
		
		/**
		 * ��ȡ��ǰ��ƫ��
		 * @return
		 */
		public int Start(){
			return start;
		}
		/**
		 * ʣ�����е��ַ�
		 * @return
		 */
		public String RemainString(){
			return hasNextToken()?pr.substring(start):null;
		}
	}

	public static Map<String, String[]> Prod(String[] ps) {
		Map<String, String[]> pros = new HashMap<>();
		for (String s : ps) {
			String[] pr = s.split("->");
			String[] rs = pr[1].split("[|]");
			for (int i = 0; i < rs.length; i++) {
				rs[i] = rs[i].trim();
			}
			pr[0] = pr[0].trim();
			pros.put(pr[0], rs);
		}
		return pros;
	}

	/**
	 * ��ȡ��һ������
	 * 
	 * @param pr
	 * @param start
	 * @return
	 */
	public static String getNextToken(String pr, int start) {
		if (start >= pr.length())
			return null;
		char a = pr.charAt(start);
		if ((a >= '!' && a <= '~') || a == '��') {
			if (a >= 'A' && a <= 'Z') {// ����Ǵ�д��ĸ����Ҫ����A'��
				if (pr.length() > start + 1 && pr.charAt(start + 1) == '\'') {// ����A'��Ϊ����
					return pr.substring(start, start + 2);
				}
			}
			return pr.substring(start, start + 1);
		} else {
			return null;
		}
	}

	/**
	 * �ж��Ƿ����ս��
	 * 
	 * @param t
	 * @return
	 */
	public static boolean IsTerminal(String t) {
		char a = t.charAt(0);
		return !(a >= 'A' && a <= 'Z');
	}
}

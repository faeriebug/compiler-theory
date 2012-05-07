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
		 * 获取下一个字符
		 * @return 没有下一个字符或者字符不在有效范围内则返回null
		 */
		public String nextToken() {
			if (!hasNextToken())
				return null;
			char a = pr.charAt(start);
			start++;
			if ((a >= '!' && a <= '~') || a == 'ε') {//有效范围内
				if (a >= 'A' && a <= 'Z') {// 如果是大写字母，则要考虑A'。
					if (start<pr.length() && pr.charAt(start ) == '\'') {// 类似A'看为整体
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
		 * 获取当前的偏移
		 * @return
		 */
		public int Start(){
			return start;
		}
		/**
		 * 剩下所有的字符
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
	 * 获取下一个符号
	 * 
	 * @param pr
	 * @param start
	 * @return
	 */
	public static String getNextToken(String pr, int start) {
		if (start >= pr.length())
			return null;
		char a = pr.charAt(start);
		if ((a >= '!' && a <= '~') || a == 'ε') {
			if (a >= 'A' && a <= 'Z') {// 如果是大写字母，则要考虑A'。
				if (pr.length() > start + 1 && pr.charAt(start + 1) == '\'') {// 类似A'看为整体
					return pr.substring(start, start + 2);
				}
			}
			return pr.substring(start, start + 1);
		} else {
			return null;
		}
	}

	/**
	 * 判断是否是终结符
	 * 
	 * @param t
	 * @return
	 */
	public static boolean IsTerminal(String t) {
		char a = t.charAt(0);
		return !(a >= 'A' && a <= 'Z');
	}
}

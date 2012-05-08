package PreAnTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * <p>
 * 文法输入的预处理
 * <p>
 * 1. 输入的文法中"<>"内部表示的是非终结符
 * <p>
 * 2. "->"表示推导
 * <p>
 * 3. 空格分隔的其他符号视为整体的一个终结符，如“begin”视为一个终结符，“b e g i n”视为5个终结符。
 * <p>
 * 4. 空格在分析的过程中会被过滤掉，如果要输入空格本身，则需要加"\"，如“\ ”。
 * <p>
 * 5. 同理，如果要输入“<,>,->,|”中的任何一个都需要加“\”，如“\<”,“\>”，“\->”，“\|”
 * 
 * @author WuyaMony
 * 
 */
public class CommProcess {
	public static final String NT = "NT";
	public static final String T = "T";
	public static final String INFER = "INFER";
	public static final String OR = "OR";
	public static final int Epsilon = 1;
	public static final int Sign = 2;

	public static class Grammar {
		public int StartNT;
		public Integer[][][] prod;
		public String[] NTs;
		public String[] Ts;

		public Grammar(int startNT, Integer[][][] prod, String[] nTs,
				String[] ts) {
			this.StartNT = startNT;
			this.prod = prod;
			NTs = nTs;
			Ts = ts;
		}
	}

	private static class token {
		String str, type;

		public token() {
		}

		public token(String str, String type) {
			this.str = str;
			this.type = type;
		}
	}

	private static class TokenProcesser {
		private int start;
		private String pr;

		public TokenProcesser(String pr) {
			this.pr = pr;
			start = 0;
		}

		public boolean endOfString() {
			return start >= pr.length();
		}

		/**
		 * 下一个非终结符
		 * 
		 * @param t
		 * @return
		 */
		private token nextNT() {
			token t = new token();
			char b = 0;
			StringBuilder sb = new StringBuilder();
			while (!endOfString() && (b = pr.charAt(start++)) != '>') {// 注意此部分，即使输入的是空格也不会过滤！
				sb.append(b);
			}
			if (b == '>' && sb.length() != 0) {// 正常结束
				t.str = sb.toString();
				t.type = NT;
				return t;
			} else {// 异常结束
				return new token(null, null);
			}
		}

		/**
		 * 下一个转义字符
		 */
		private String nextSlash() {
			if (endOfString())
				return null;
			switch (pr.charAt(start++)) {
			case '\\':
				return "\\";
			case '>':
				return ">";
			case '<':
				return "<";
			case '|':
				return "|";
			case ' ':
				return " ";
			case '-':
				if (endOfString()) {
					return "-";
				} else {
					if (pr.charAt(start + 1) == '>'){
						start++;
						return "->";
					}else
						return "-";
				}
			default:
				return null;
			}
		}

		private token nextT() {
			start--;
			char b = 0;
			StringBuilder sb = new StringBuilder();
			while (!endOfString()) {
				b = pr.charAt(start++);
				if (b == ' ' || b == '<') {
					break;
				}
				if (b == '\\') {
					String s = nextSlash();
					if (s != null) {
						sb.append(s);
					} else {// 出错
						return new token(null, null);
					}
				} else {
					sb.append(b);
				}
			}
			if (b == '<') {
				start--;
			}
			return new token(sb.toString(), T);
		}

		/**
		 * 获取下一个字符
		 * 
		 * @return 没有下一个字符或者字符不在有效范围内则返回null
		 */
		public token nextToken() {
			if (endOfString())
				return null;// 到达字符串末尾
			switch (pr.charAt(start++)) {
			case ' ':
				return nextToken();
			case '<': // 尝试获取非终结符
				return nextNT();
			case '-':
				if (!endOfString() && pr.charAt(start++) == '>') // 推出符号"->"
					return new token("->", INFER);
				else
					// 出错
					return new token(null, null);
			case '|':
				return new token("|", OR);
			default:
				return nextT();
			}
		}
	}

	/**
	 * 生成产生式，并自动编号，为非终结符和终结符编号
	 * 
	 * @param ps
	 * @return
	 */
	public static Grammar ProcessProd(String[] ps, boolean Expend) {
		Map<String, Integer> nts = new HashMap<>();
		Map<String, Integer> ts = new HashMap<>();
		Integer[][][] prod;
		ArrayList<ArrayList<Integer[]>> prodHelp = new ArrayList<>();
		ts.put("ε", Epsilon);
		ts.put("#", Sign);
		TokenProcesser tp;
		token k, t;
		boolean hasStart = false;
		int startNT = 0;
		String longest = "";
		int line = 0;
		outer: for (String s : ps) {
			line++;
			tp = new TokenProcesser(s);
			k = tp.nextToken();
			if (k == null)// 到达字符串末尾
				continue;
			if (k.type == null || !k.type.equals(NT)) {// 出错，第一个符号必须是非终结符
				System.out.println("Error: line " + line + " 第一个符号必须是非终结符\n\t"
						+ s);
				return null;
			} else {
				if (!nts.containsKey(k.str)) {
					nts.put(k.str, prodHelp.size());
					if (k.str.length() > longest.length())
						longest = k.str;
					prodHelp.add(new ArrayList<Integer[]>());
				}
				if (!hasStart) {
					hasStart = true;
					startNT = nts.get(k.str);
				}
				t = tp.nextToken();
				if (t == null || t.type == null || !t.type.equals(INFER)) {// 出错，第二个符号必须是推出符号
					System.out.println("Error: line " + line
							+ " 第二个符号必须是推出符号\n\t" + s);
					return null;
				} else {
					boolean must = true;
					ArrayList<Integer> tmp = new ArrayList<>();
					while (true) {
						t = tp.nextToken();
						if (must) {
							if (t == null || t.type == null
									|| !(t.type.equals(NT) || t.type.equals(T))) {// 出错，第三个符号必须是非终结符或者终结符
								System.out
										.println("Error: line "
												+ line
												+ " :第三个符号必须是非终结符或者终结符或者|之后必须有非终结符或终结符\n\t"
												+ s);
								return null;
							}
						} else {
							if (t == null) {// 到达字符串末尾
								prodHelp.get(nts.get(k.str)).add(
										tmp.toArray(new Integer[tmp.size()]));
								continue outer;
							} else if (t.type == null || t.type.equals(INFER)) {// 出错
								if (t.type == null)
									System.out.println("Error: line " + line
											+ " :符号不合法\n\t" + s);
								else
									System.out.println("Error: line " + line
											+ " :不应该是推出符号\n\t" + s);
								return null;
							} else if (t.type.equals(OR)) {// 后继必须有其他符号
								must = true;
								prodHelp.get(nts.get(k.str)).add(
										tmp.toArray(new Integer[tmp.size()]));
								tmp = new ArrayList<>();
								continue;
							}
						}
						must = false;
						// 终结符或者非终结符，加入
						if (t.type.equals(NT)) {
							if (!nts.containsKey(t.str)) {
								if (t.str.length() > longest.length())
									longest = t.str;
								nts.put(t.str, prodHelp.size());
								prodHelp.add(new ArrayList<Integer[]>());
							}
							tmp.add(nts.get(t.str));
						} else {
							if (!ts.containsKey(t.str)) {
								ts.put(t.str, ts.size() + 1);
							}
							tmp.add(-ts.get(t.str));// 终结符为负号
						}
					}
				}
			}
		}
		if (Expend) {// 拓广文法
			String s_ = longest + "'";
			nts.put(s_, prodHelp.size());
			ArrayList<Integer[]> a = new ArrayList<Integer[]>();
			a.add(new Integer[] { startNT });
			prodHelp.add(a);
			startNT = nts.get(s_);
		}
		prod = new Integer[prodHelp.size()][][];
		for (int i = 0; i < prodHelp.size(); i++) {
			prod[i] = prodHelp.get(i).toArray(
					new Integer[prodHelp.get(i).size()][]);
		}
		String[] ntss = new String[nts.size()];
		for (Entry<String, Integer> s : nts.entrySet()) {
			ntss[s.getValue()] = s.getKey();
		}
		String[] tss = new String[ts.size() + 1];
		for (Entry<String, Integer> s : ts.entrySet()) {
			tss[s.getValue()] = s.getKey();
		}
		return new Grammar(startNT, prod, ntss, tss);
	}

	public static void main(String[] args) {
		String[] ps = new String[] { "<E>-><T><E'>", "<E'>->+<T><E'>| ε",
				"<T>-><F><T'>", "<T'>->*<F><T'>| ε", "<F>->(<E>) | i" };
		Grammar p = CommProcess.ProcessProd(ps, true);
		System.out.println("StartNT=" + p.NTs[p.StartNT]);
		for (int i = 0; i < p.prod.length; i++) {
			System.out.print("<" + p.NTs[i] + ">->");
			for (Integer[] j : p.prod[i]) {
				for (Integer k : j) {
					if (k < 0) {// 终结符
						System.out.print(" " + p.Ts[-k]);
					} else {
						System.out.print(" <" + p.NTs[k] + ">");
					}
				}
				System.out.print("|");
			}
			System.out.println();
		}
	}
}

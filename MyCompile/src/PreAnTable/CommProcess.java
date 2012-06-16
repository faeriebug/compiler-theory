package PreAnTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * <p>
 * �ķ������Ԥ����
 * <p>
 * 1. ������ķ���"<>"�ڲ���ʾ���Ƿ��ս��
 * <p>
 * 2. "��"��ʾ�Ƶ�
 * <p>
 * 3. �ո�ָ�����������Ϊ�����һ���ս���硰begin����Ϊһ���ս��b e g i n����Ϊ5���ս��
 * <p>
 * 4. �ո��ڷ����Ĺ���лᱻ���˵������Ҫ����ո��?����Ҫ��"\"���硰\ ����
 * <p>
 * 5. ͬ�?���Ҫ���롰<,>,��,|���е��κ�һ������Ҫ�ӡ�\�����硰\<��,��\>������\����\|��
 * 6.���ս����0,1,2...�ս��-1,-2,-3...
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
		 * ��һ�����ս��
		 * 
		 * @param t
		 * @return
		 */
		private token nextNT() {
			token t = new token();
			char b = 0;
			StringBuilder sb = new StringBuilder();
			while (!endOfString() && (b = pr.charAt(start++)) != '>') {// ע��˲��֣���ʹ������ǿո�Ҳ������ˣ�
				sb.append(b);
			}
			if (b == '>' && sb.length() != 0) {// �����
				t.str = sb.toString();
				t.type = NT;
				return t;
			} else {// �쳣����
				return new token(null, null);
			}
		}

		/**
		 * ��һ��ת���ַ�
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
			case '→':
				return "→";
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
					} else {// ����
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
		 * ��ȡ��һ���ַ�
		 * 
		 * @return û����һ���ַ�����ַ�����Ч��Χ���򷵻�null
		 */
		public token nextToken() {
			if (endOfString())
				return null;// �����ַ�ĩβ
			switch (pr.charAt(start++)) {
			case ' ':
				return nextToken();
			case '<': // ���Ի�ȡ���ս��
				return nextNT();
			case '→':
				// �Ƴ����"->"
				return new token("→", INFER);
			case '|':
				return new token("|", OR);
			default:
				return nextT();
			}
		}
	}

	/**
	 * ��ɲ���ʽ�����Զ���ţ�Ϊ���ս����ս����
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
			if (k == null)// �����ַ�ĩβ
				continue;
			if (k.type == null || !k.type.equals(NT)) {// ���?��һ����ű����Ƿ��ս��
				System.out.println("Error: line " + line + " ��һ����ű����Ƿ��ս��\n\t"
						+ s);
				return null;
			}
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
			if (t == null || t.type == null || !t.type.equals(INFER)) {// ���?�ڶ�����ű������Ƴ����
				System.out.println("Error: line " + line + " �ڶ�����ű������Ƴ����\n\t"
						+ s);
				return null;
			}
			boolean must = true;
			ArrayList<Integer> tmp = new ArrayList<>();
			while (true) {
				t = tp.nextToken();
				if (must) {
					if (t == null || t.type == null
							|| !(t.type.equals(NT) || t.type.equals(T))) {// ���?�������ű����Ƿ��ս������ս��
						System.out
								.println("Error: line "
										+ line
										+ " :�������ű����Ƿ��ս������ս�����|֮������з��ս����ս��\n\t"
										+ s);
						return null;
					}
				} else {
					if (t == null) {// �����ַ�ĩβ
						prodHelp.get(nts.get(k.str)).add(
								tmp.toArray(new Integer[tmp.size()]));
						continue outer;
					} else if (t.type == null || t.type.equals(INFER)) {// ����
						if (t.type == null)
							System.out.println("Error: line " + line
									+ " :��Ų��Ϸ�\n\t" + s);
						else
							System.out.println("Error: line " + line
									+ " :��Ӧ�����Ƴ����\n\t" + s);
						return null;
					} else if (t.type.equals(OR)) {// ��̱�����������
						must = true;
						prodHelp.get(nts.get(k.str)).add(
								tmp.toArray(new Integer[tmp.size()]));
						tmp = new ArrayList<>();
						continue;
					}
				}
				must = false;
				// �ս����߷��ս�����
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
					tmp.add(-ts.get(t.str));// �ս��Ϊ����
				}
			}
		}
		if (Expend) {// �ع��ķ�
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
		String[] ps = new String[] { "<E>→<T><E'>", "<E'>→+<T><E'>| ε",
				"<T>→<F><T'>", "<T'>→*<F><T'>| ε", "<F>→(<E>) | i" };
		Grammar p = CommProcess.ProcessProd(ps, true);
		System.out.println("StartNT=" + p.NTs[p.StartNT]);
		for (int i = 0; i < p.prod.length; i++) {
			System.out.print("<" + p.NTs[i] + ">→");
			for (Integer[] j : p.prod[i]) {
				for (Integer k : j) {
					if (k < 0) {// �ս��
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

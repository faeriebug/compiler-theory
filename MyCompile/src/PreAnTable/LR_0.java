package PreAnTable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import PreAnTable.CommProcess.Grammar;

public class LR_0 {
	/** 项目 */
	private class item {
		// int itemnumber;
		Integer unterm;// 此项目关联的非终结符
		int proID;// 对应的产生式编号
		int start;// 制定点好后面对应的字符

		public item(Integer unterm, int pro, int start) {
			this.unterm = unterm;
			this.proID = pro;
			this.start = start;
		}
	}

	/** 项目集 */
	private class Itemset {
		int id;
		HashSet<Integer> core = new HashSet<>();
		HashSet<Integer> items = new HashSet<>();

		@Override
		public int hashCode() {
			// System.out.println("super.hashcode="+super.hashCode());
			return core.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof Itemset && ((Itemset) o).core.equals(this.core);
		}
	}

	/** 所有项目，映射 */
	private item[] allitems;
	/** 项目对应的闭包 */
	private Map<Integer, HashSet<Integer>> ntClosure;
	/** 非终结符对应每个第一产生式的项目编号 */
	private Map<Integer, Integer[]> NTBeginItem;
	/** go函数 */
	private HashMap<Itemset, HashMap<Integer, Itemset>> go;
	/** 项目集规范簇 */
	private HashMap<Itemset, Itemset> ccsi;
	/** 开始产生式的编号 */
	private int startProNumber, endProNumber;
	Grammar GG;

	public static void main(String arg[]) {
		// String[] ps = new String[] { "<E>→<E>+<T>|<T>",
		// "<T>→<T>*<F>|<F>","<F>→(<E>) | i" };
		// String[] ps = new String[] {
		// "<S>→<A><B>","<B>→c<B>d | c d","<A>→a<A>b | a b" };
		String[] ps = null;
		try (BufferedReader br = new BufferedReader(new FileReader("grammar"))) {
			String line;
			ArrayList<String> pss = new ArrayList<String>();
			while ((line = br.readLine()) != null) {
				if (!line.trim().isEmpty()) {
					// System.out.println(line.trim());
					pss.add(line.trim());
				}
			}
			ps = new String[pss.size()];
			pss.toArray(ps);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// String[] ps = new String[] { "<程序>→<分程序>",
		// "<分程序>→begin<说明语句表>;<执行语句表>end",
		// "<说明语句表>→<说明语句>|<说明语句表>;<说明语句>",
		// "<说明语句>→<变量说明>|<函数说明>",
		// "<变量说明>→<变量类型> <变量>",
		// "<变量类型>→double | integer",
		// "<变量>→<标识符>",
		// "<标识符>→i",
		// "<函数说明>→<变量类型> function <标识符>(<参数列表>);<函数体>",
		// "<参数列表>→<参数>|<参数列表>,<参数>",
		// "<参数>→<变量说明>",
		// "<函数体>→begin<执行语句表>end",
		// "<执行语句表>→<执行语句>|<执行语句表>;<执行语句>",
		// "<执行语句>→<读语句>|<写语句>|<赋值语句>|<条件语句>|<循环语句>",
		// "<读语句>→read(<变量>)",
		// "<写语句>→write(<变量>)",
		// "<赋值语句>→<变量>=<算术表达式>",
		// "<算术表达式>→<算术表达式>+<项>|<算术表达式>-<项>|<项>",
		// "<项>→<项>*<因子>|<因子>",
		// "<因子>→<变量>|<常数>|<函数调用>",
		// "<常数>→const",
		// "<函数调用>→<标识符>(<算术表达式列表>)",
		// "<算术表达式列表>→<算术表达式>|<算术表达式列表>,<算术表达式>",
		// "<条件语句>→if<条件表达式>then<执行语句>else<执行语句>",
		// "<条件表达式>→<算术表达式><关系运算符><算术表达式>",
		// "<关系运算符>→ op",
		// "<循环语句>→while<条件表达式>begin<执行语句表>end", };
		Grammar p = CommProcess.ProcessProd(ps, false);
		LR_0 l = new LR_0();
		l.SetGrammar(p);
		// HashMap<Itemset, HashMap<Integer, Itemset>> go = l.getGo();
		// for (Entry<Itemset, HashMap<Integer, Itemset>> en : go.entrySet()) {
		// for (Entry<Integer, Itemset> ss : en.getValue().entrySet()) {
		// System.out.println("go(I"
		// + en.getKey().id
		// + ","
		// + (ss.getKey() < 0 ? p.Ts[-ss.getKey()] : p.NTs[ss
		// .getKey()]) + ")=I" + ss.getValue().id);
		// }
		// }
		String[][] tbl = l.constructSLRTable();
		int base = p.Ts.length - 1;
		// for (int i = 0; i < p.Ts.length; i++) {
		//
		// }
		for (int i = 0; i < tbl.length; i++) {
			for (int j = 0; j < tbl[i].length; j++) {
				if(tbl[i][j]==null)
					continue;
				System.out.print("(" + i + ",");
				System.out.print((j - base < 0 ? p.Ts[-j + base] : p.NTs[j
						- base]));
				System.out.println(")=" + tbl[i][j]);
			}
		}
	}

	public HashMap<Itemset, HashMap<Integer, Itemset>> getGo() {
		return go;
	}

	public HashMap<Itemset, Itemset> getCcsi() {
		return ccsi;
	}

	public String[][] constructSLRTable() {
		Follow foll = new Follow();
		foll.SetGrammar(GG);
		String[][] tbl = new String[ccsi.size()][GG.Ts.length + GG.NTs.length
				- 1];
//		for (int i = 0; i < tbl.length; i++) {
//			for (int j = 0; j < tbl[i].length; j++) {
//				tbl[i][j]="";
//			}
//		}
		int base = GG.Ts.length - 1;
		for (Itemset Ii : ccsi.keySet()) {
			for (Integer p : Ii.items) {
				if (p == endProNumber) {// S'→S·
					tbl[Ii.id][-CommProcess.Sign + base] = "acc";
					continue;
				}
				item i = allitems[p];
				Integer[] pr = GG.prod[i.unterm][i.proID];
				if (i.start < pr.length) {// A→α·aβ ,A→α·Bβ
					if (pr[i.start] < 0) {// 终结符
						tbl[Ii.id][pr[i.start] + base] = "s"
								+ go.get(Ii).get(pr[i.start]).id;
					} else {// 非终结符
						tbl[Ii.id][pr[i.start] + base] = Integer.toString(go
								.get(Ii).get(pr[i.start]).id);
					}
				} else {// A→α·
					for (Integer b : foll.follow_set.get(i.unterm)) {
						tbl[Ii.id][b + base] = "r<" + GG.NTs[i.unterm] + ","
								+ i.proID + ">";
					}
				}
			}
		}

		return tbl;
	}

	public void SetGrammar(Grammar G) {
		this.GG = G;
		go = new HashMap<>();
		ccsi = new HashMap<>();
		NTBeginItem = new HashMap<Integer, Integer[]>();
		ntClosure = new HashMap<Integer, HashSet<Integer>>();
		Fill_allitems();
		Fill_go();
	}

	/**
	 * 生成所有项目并编号
	 */
	void Fill_allitems() {
		ArrayList<item> array_allitems = new ArrayList<>();
		for (int prod = 0; prod < GG.prod.length; prod++) {// 遍历每一个非终结符
			ArrayList<Integer> beginItems = new ArrayList<Integer>();
			Integer[][] pp = GG.prod[prod];
			for (int i = 0; i < pp.length; i++) {
				array_allitems.add(new item(prod, i, 0));// 圆点位于起始端
				beginItems.add(array_allitems.size() - 1);
				if (prod == GG.StartNT) {
					startProNumber = array_allitems.size() - 1;
					endProNumber = startProNumber + pp[i].length;
				}
				for (int j = 1; j <= pp[i].length; j++) {
					array_allitems.add(new item(prod, i, j));
				}
			}
			NTBeginItem.put(prod,
					beginItems.toArray(new Integer[beginItems.size()]));
		}
		allitems = array_allitems.toArray(new item[array_allitems.size()]);
	}

	void Fill_go() {
		Itemset I0 = new Itemset();
		int count = 1;
		I0.id = 0;
		I0.core.add(startProNumber);
		I0.items.addAll(closure(startProNumber));
		ccsi.put(I0, I0);
		LinkedList<Itemset> checkSet = new LinkedList<>();
		checkSet.addLast(I0);
		while (!checkSet.isEmpty()) {
			Itemset I = checkSet.poll();
			HashMap<Integer, Itemset> goTmp = new HashMap<>();
			for (Integer i : I.items) {
				item ii = allitems[i];
				Integer[] pr = GG.prod[ii.unterm][ii.proID];
				if (ii.start < pr.length) {
					if (!goTmp.containsKey(pr[ii.start])) {
						goTmp.put(pr[ii.start], new Itemset());
					}
					Itemset itmp = goTmp.get(pr[ii.start]);
					itmp.core.add(i + 1);
				}
			}
			for (Integer k : goTmp.keySet()) {
				Itemset kk = goTmp.get(k);
				if (ccsi.containsKey(kk)) {
					goTmp.put(k, ccsi.get(kk));
				} else {
					for (Integer d : kk.core) {
						kk.items.addAll(closure(d));
					}
					kk.id = count++;
					ccsi.put(kk, kk);
					checkSet.addLast(kk);
				}
			}
			go.put(I, goTmp);
		}
	}

	/**
	 * 求一个项目的闭包
	 * 
	 * @param i
	 * @return
	 */
	HashSet<Integer> closure(int itemnumber) {
		HashSet<Integer> c = new HashSet<>();
		c.add(itemnumber);// i→closure(i);
		item i = allitems[itemnumber];
		Integer[] pr = GG.prod[i.unterm][i.proID];
		if (i.start < pr.length && pr[i.start] >= 0) {// 非终结符
			c.addAll(NTClosure(pr[i.start], null));
		}
		return c;
	}

	HashSet<Integer> NTClosure(int NT, HashSet<Integer> has) {
		if (has != null) {
			has.add(NT);
		}
		if (ntClosure.containsKey(NT)) {
			return ntClosure.get(NT);
		} else {
			HashSet<Integer> ui = new HashSet<Integer>();
			if (has == null) {
				has = new HashSet<Integer>();
				has.add(NT);
			}
			for (Integer ii : NTBeginItem.get(NT)) {
				ui.add(ii);
				item i = allitems[ii];
				Integer[] pr = GG.prod[i.unterm][i.proID];
				if (i.start < pr.length && pr[i.start] >= 0
						&& !has.contains(pr[i.start])) {// 未考察过非终结符
					ui.addAll(NTClosure(pr[i.start], has));
				}
			}
			ntClosure.put(NT, ui);
		}
		return ntClosure.get(NT);
	}
}

package PreAnTable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import PreAnTable.CommProcess.Grammar;

public class PreAnTableConstruct {
	/** follow集合 */
	Map<Integer, Set<Integer>> follow_set;
	/** M */
	Map<String, String> M;
	First first = new First();
	Follow follow = new Follow();
	Grammar G;

	public void SetGrammar(Grammar G) {
		this.G = G;
		first.SetGrammar(G);
		follow.SetGrammar(G);
		follow_set = follow.getFollowSet();
		analyse();
	}

	public Map<String, String> getM() {
		return M;
	}

	void analyse() {
		M = new HashMap<>();
		for (int prod = 0;prod < G.prod.length; prod++) {// 遍历每一个非终结符
			for (Integer[] pr : G.prod[prod]) {// 遍历对应的每一个产生式
				String tmp = "";
				for (Integer tt : pr) {
					tmp += (tt < 0 ? G.Ts[-tt] : G.NTs[tt]);
				}
				for (Integer a : first.getStringFirstSet(pr, 0)) {
					if (a.equals(CommProcess.Epsilon)) {
						for (Integer b : follow_set.get(prod)) {

							M.put(G.NTs[prod] + "," + G.Ts[b],
									G.NTs[prod] + "->" + tmp);
						}
					} else {
						M.put(G.NTs[prod] + "," + G.Ts[a], G.NTs[prod] + "->" + tmp);
					}
				}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[] input = { "<E>-><T><E'>", "<E'>->+<T><E'>| ε", "<T>-><F><T'>",
				"<T'>->*<F><T'>| ε", "<F>->(<E>) | i" };
		Grammar G = CommProcess.ProcessProd(input,false);
		PreAnTableConstruct p = new PreAnTableConstruct();
		p.SetGrammar(G);
		Map<String, String> M = p.getM();
		for (String s : M.keySet()) {
			System.out.println("M[" + s + "]=" + M.get(s));
		}
	}

}

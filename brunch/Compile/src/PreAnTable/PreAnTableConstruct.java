package PreAnTable;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class PreAnTableConstruct {
	/** follow集合 */
	Map<String, Set<String>> follow_set;
	/** M */
	Map<String, String> M;
	/** 产生式 */
	Map<String, String[]> pros;
	First first = new First();
	Follow follow = new Follow();

	public void SetGrammar(String[] ps, String G) {
		pros = PreProcess.Process(ps);
		first.SetGrammar(pros);
		follow.SetGrammar(pros, G);
		follow_set=follow.getFollowSet();
		analyse();
	}
	public Map<String, String> getM(){
		return M;
	}
	void analyse() {
		M=new HashMap<>();
		for (Entry<String, String[]> pr : pros.entrySet()) {
			for (String p : pr.getValue()) {// 遍历对应的每一个产生式
				for (String a : first.getStringFirstSet(p)) {
					if (a.equals("ε")) {
						for (String b : follow_set.get(pr.getKey())) {
							M.put(pr.getKey() + "," + b, pr.getKey() + "->" + p);
						}
					} else {
						M.put(pr.getKey() + "," + a, pr.getKey() + "->" + p);
					}
				}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[] input = { "E->TE'", "E'->+TE'|ε", "T->FT'", "T'->*FT'|ε",
		"F->(E)|i" };
		PreAnTableConstruct p=new PreAnTableConstruct();
		p.SetGrammar(input, "E");
		Map<String, String> M=p.getM();
		for (String s : M.keySet()) {
			System.out.println("M["+s+"]="+M.get(s));
		}
	}

}

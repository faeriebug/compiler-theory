package PreAnTable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Follow {
	/** 非终结符之间的依赖关系 */
	Map<String, Set<String>> rela;
	/** first集合 */
	Map<String, Set<String>> follow_set;
	/** 产生式 */
	Map<String, String[]> pros;
	First first = new First();

	public void SetGrammar(String[] ps,String G) {
		first.SetGrammar(ps);
		rela = new HashMap<>();
		follow_set = new HashMap<>();
		pros = new HashMap<>();
		for (String s : ps) {
			String[] pr = s.split("->");
			String[] rs = pr[1].split("[|]");
			pros.put(pr[0], rs);
			follow_set.put(pr[0], new HashSet<String>());
		}
		follow_set.get(G).add("#");
		analyse();
	}

	public Map<String, Set<String>> getFollowSet(){
		return follow_set;
	}
	
	private void analyse() {
		for (Entry<String, String[]> prod : pros.entrySet()) {// 遍历每一个非终结符
			for (String p : prod.getValue()) {// 遍历对应的每一个产生式
				int index = 0;
				while (index < p.length()) {// 遍历产生式的每一个符号
					String t = First.getNextToken(p, index);
					index += t.length();
					if (!First.IsTerminal(t)) {// 非终结符
						// first分析
						firstAna(p, t, index);
						// follow分析
						followAna(p, t, prod.getKey(), index);
					}
				}
			}
		}
		fillSetByDependency();
	}

	private void fillSetByDependency() {
		LinkedList<String> depend_list = First.SetOpSort(rela);
		String it = null;
		while ((it = depend_list.pollLast()) != null) {
			Set<String> its = follow_set.get(it);
			for (String s : rela.get(it)) {// 依赖于node的节点
				// first(it)-{ε}<-first(s)
				follow_set.get(s).addAll(its);// 将it集合的元素合并到s中
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Follow f = new Follow();
		String[] input = { "E->TE'", "E'->+TE'|ε", "T->FT'", "T'->*FT'|ε",
				"F->(E)|i" };
		f.SetGrammar(input,"E");
		f.analyse();
	}

	/**
	 * 非终结符t在产生式p中分析,直接添加到t中 first(b)-ε<-fist(t)
	 * 
	 * @param p
	 * @param A
	 * @param start
	 */
	void firstAna(String p, String A, int start) {
		if (start < p.length()) {
			Set<String> re = first.getStringFirstSet(p.substring(start));
			First.UnionWithoutEpsilon(re, follow_set.get(A));
		}
	}

	/**
	 * 非终结符t在产生式p中分析,依赖follow分析
	 * 
	 * @param p
	 * @param A
	 * @param B
	 * @param start
	 */
	void followAna(String p, String A, String B, int start) {
		if(A.equals(B))return;//自己就不用和自己合并了
		if (start < p.length()) {
			Set<String> re = first.getStringFirstSet(p.substring(start));
			if (!re.contains("ε"))
				return;
		}
		if (!rela.containsKey(B)) {
			rela.put(B, new HashSet<String>());
		}
		rela.get(B).add(A);// B被A依赖
	}



}

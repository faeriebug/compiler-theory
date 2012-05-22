package PreAnTable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import PreAnTable.CommProcess.Grammar;

public class Follow {
	/** 非终结符之间的依赖关系 */
	Map<Integer, Set<Integer>> rela;
	/** follow集合 */
	Map<Integer, Set<Integer>> follow_set;
	Grammar G;
	First first = new First();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Follow f = new Follow();
		String[] input = { "<E>→<T><E'>", "<E'>→+<T><E'>| ε",
				"<T>→<F><T'>","<T'>→*<F><T'>| ε","<F>→(<E>) | i"};
		Grammar G=CommProcess.ProcessProd(input,false);
		f.SetGrammar(G);
		f.analyse();
		Map<Integer, Set<Integer>> Follow_set = f.getFollowSet();
		for (Entry<Integer, Set<Integer>> s : Follow_set.entrySet()) {// 遍历每一个非终结符
			System.out.print("Follow(<" + G.NTs[s.getKey()] + ">)={");
			Integer ss;
			for (Iterator<Integer> iterator = s.getValue().iterator(); iterator
					.hasNext();) {
				ss = iterator.next();
				System.out.print(G.Ts[-ss]);
				if (iterator.hasNext()) {
					System.out.print(",");
				} else {
					System.out.println("}");
				}
			}
		}
	}
	
	public void SetGrammar(Grammar G) {
		this.G=G;
		first.SetGrammar(G);
		rela = new HashMap<>();
		follow_set = new HashMap<>();
		for (int i = 0; i < G.NTs.length; i++) {
			follow_set.put(i, new HashSet<Integer>());
		}
		follow_set.get(G.StartNT).add(-CommProcess.Sign);
		analyse();
	}

	public Map<Integer, Set<Integer>> getFollowSet() {
		return follow_set;
	}

	private void analyse() {
		for (int prod = 0;prod < G.prod.length; prod++) {// 遍历每一个非终结符
			for (Integer[] p : G.prod[prod]) {// 遍历对应的每一个产生式
				for (int i=0;i<p.length;i++) {
					if(p[i]>=0){
						// first分析
						firstAna(p[i],p, i+1);
						// follow分析
						followAna(p[i],prod, p,i+1);
					}
				}
			}
		}
		fillSetByDependency();
	}

	private void fillSetByDependency() {
		LinkedList<Integer> depend_list = First.SetOpSort(rela);
		for (Integer it : depend_list) {
			Set<Integer> its = follow_set.get(it);
			for (Integer s : rela.get(it)) {// 依赖于node的节点
				// first(it)-{ε}<-first(s)
				follow_set.get(s).addAll(its);// 将it集合的元素合并到s中
			}
		}
	}



	/**
	 * 非终结符t在产生式p中分析,直接添加到t中 first(b)-ε<-fist(t)
	 */
	void firstAna(Integer A, Integer[] p,int start) {
		if (start<p.length) {
			Set<Integer> re = first.getStringFirstSet(p,start);
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
	void followAna(Integer A, Integer B, Integer[] beta,Integer start) {
		if (A.equals(B))
			return;// 自己就不用和自己合并了
		if (start<beta.length) {
			Set<Integer> re = first.getStringFirstSet(beta,start);
			if (!re.contains(CommProcess.Epsilon))
				return;
		}
		if (!rela.containsKey(B)) {
			rela.put(B, new HashSet<Integer>());
		}
		rela.get(B).add(A);// B被A依赖
	}

}

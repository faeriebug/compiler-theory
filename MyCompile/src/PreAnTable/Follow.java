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
	/** ���ս��֮���������ϵ */
	Map<Integer, Set<Integer>> rela;
	/** follow���� */
	Map<Integer, Set<Integer>> follow_set;
	Grammar G;
	First first = new First();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Follow f = new Follow();
		String[] input = { "<E>��<T><E'>", "<E'>��+<T><E'>| ��",
				"<T>��<F><T'>","<T'>��*<F><T'>| ��","<F>��(<E>) | i"};
		Grammar G=CommProcess.ProcessProd(input,false);
		f.SetGrammar(G);
		f.analyse();
		Map<Integer, Set<Integer>> Follow_set = f.getFollowSet();
		for (Entry<Integer, Set<Integer>> s : Follow_set.entrySet()) {// ����ÿһ�����ս��
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
		for (int prod = 0;prod < G.prod.length; prod++) {// ����ÿһ�����ս��
			for (Integer[] p : G.prod[prod]) {// ������Ӧ��ÿһ������ʽ
				for (int i=0;i<p.length;i++) {
					if(p[i]>=0){
						// first����
						firstAna(p[i],p, i+1);
						// follow����
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
			for (Integer s : rela.get(it)) {// ������node�Ľڵ�
				// first(it)-{��}<-first(s)
				follow_set.get(s).addAll(its);// ��it���ϵ�Ԫ�غϲ���s��
			}
		}
	}



	/**
	 * ���ս��t�ڲ���ʽp�з���,ֱ����ӵ�t�� first(b)-��<-fist(t)
	 */
	void firstAna(Integer A, Integer[] p,int start) {
		if (start<p.length) {
			Set<Integer> re = first.getStringFirstSet(p,start);
			First.UnionWithoutEpsilon(re, follow_set.get(A));
		}
	}

	/**
	 * ���ս��t�ڲ���ʽp�з���,����follow����
	 * 
	 * @param p
	 * @param A
	 * @param B
	 * @param start
	 */
	void followAna(Integer A, Integer B, Integer[] beta,Integer start) {
		if (A.equals(B))
			return;// �Լ��Ͳ��ú��Լ��ϲ���
		if (start<beta.length) {
			Set<Integer> re = first.getStringFirstSet(beta,start);
			if (!re.contains(CommProcess.Epsilon))
				return;
		}
		if (!rela.containsKey(B)) {
			rela.put(B, new HashSet<Integer>());
		}
		rela.get(B).add(A);// B��A����
	}

}

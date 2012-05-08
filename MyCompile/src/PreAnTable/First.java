package PreAnTable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import PreAnTable.CommProcess.Grammar;

/**
 * ��first��
 * <p>
 * ���������˹��������Ӻ���ݹ���ķ�
 * 
 * @author WuyaMony
 * 
 */
public class First {
	/** ���ս��֮���������ϵ */
	Map<Integer, Set<Integer>> rela;
	/** first���� */
	Map<Integer, Set<Integer>> first_set;
	/** ��¼ĳ�����ս���Ƿ�Ϊ�� */
	Map<Integer, Boolean> isEpsilon;
	Grammar G;
	public static void main(String[] args) {
		First f = new First();
		String[] input = { "<E>-><T><E'>", "<E'>->+<T><E'>| ��",
				"<T>-><F><T'>","<T'>->*<F><T'>| ��","<F>->(<E>) | i" };
		Grammar G=CommProcess.ProcessProd(input,false);
		f.SetGrammar(G);
		f.analyse();
		Map<Integer, Set<Integer>> first_set = f.getUnTermFirstSet();
		for (Entry<Integer, Set<Integer>> s : first_set.entrySet()) {// ����ÿһ�����ս��
			System.out.print("First(<" + G.NTs[s.getKey()] + ">)={");
			Integer ss;
			for (Iterator<Integer> iterator = s.getValue().iterator(); iterator
					.hasNext();) {
				ss = iterator.next();
				System.out.print(G.Ts[ss]);
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
		rela = new HashMap<>();
		first_set = new HashMap<>();
		isEpsilon = new HashMap<>();
		for (int i = 0; i < G.NTs.length; i++) {
			first_set.put(i, new HashSet<Integer>());
			isEpsilon.put(i, null);
		}
		analyse();
	}

	/**
	 * ��ȡfirst��
	 * 
	 * @return
	 */
	public Map<Integer, Set<Integer>> getUnTermFirstSet() {
		return first_set;
	}

	/**
	 * ��ȡ�ִ���first��
	 * 
	 * @param pr
	 * @return
	 */
	public Set<Integer> getStringFirstSet(Integer[] pr,int start) {
		Set<Integer> fi = new HashSet<>();
		Set<Integer> dep = new HashSet<>();
		for (int t=start;t<pr.length;t++) {
			if(pr[t]<0){// �ս��
				fi.add(-pr[t]);// ��first���������Ԫ��
				return fi;
			}else{// ���ս��
				// ���������
				dep.add(pr[t]);// ��������t
				if (!inferUnTermEpsilon(pr[t])) {// �˷��ս��������Ϊ�ţ�����������������
					break;
				} else if (t==pr.length-1) {// �����һ�����ս�����ҿ���Ϊ��
					fi.add(CommProcess.Epsilon);
				}
			}
		}
		for (Integer d : dep) {
			Set<Integer> its = first_set.get(d);
			UnionWithoutEpsilon(its, fi);
		}
		return fi;
	}

	public static void UnionWithoutEpsilon(Set<Integer> src, Set<Integer> des) {
		boolean epsilon = src.contains(CommProcess.Epsilon);
		if (epsilon)
			src.remove(CommProcess.Epsilon);// ��ʱ�Ƴ���
		des.addAll(src);
		if (epsilon)
			src.add(CommProcess.Epsilon);
	}

	/**
	 * �����first��
	 * 
	 * @return
	 */
	private void analyse() {
		for (int prod = 0;prod < G.prod.length; prod++) {// ����ÿһ�����ս��
			for (Integer[] p : G.prod[prod]) {// ������Ӧ��ÿһ������ʽ
				for (int i=0;i<p.length;i++) {
					if(p[i]<0){// �ս��
						if (p[i]==CommProcess.Epsilon && i!=p.length-1) {// tΪ�ţ����������һ���ս�������Բ����롣
							continue;
						}
						first_set.get(prod).add(-p[i]);// ��first���������Ԫ��
						break;
					}else{// ���ս����һ���ǵ�һ�����ս��������֮ǰ�ķ��ս��������Ϊ��
						// ���������
						if (!rela.containsKey(p[i])) {
							rela.put(p[i], new HashSet<Integer>());
						}
						rela.get(p[i]).add(prod);// ��������t
						if (!_inferUnTermEpsilon(p[i])) {// �˷��ս��������Ϊ�ţ�����������������
							break;
						} else if (i==p.length-1) {// �����һ�����ս�����ҿ���Ϊ��
							first_set.get(prod).add(CommProcess.Epsilon);
						}
					}
				}
			}
		}
		fillSetByDependency();// ��������������first��
	}

	/**
	 * ��������������first��
	 */
	private void fillSetByDependency() {
		LinkedList<Integer> depend_list = SetOpSort(rela);
		for (Integer it : depend_list) {
			Set<Integer> its = first_set.get(it);
			boolean epsilon = its.contains(CommProcess.Epsilon);
			if (epsilon)
				its.remove(CommProcess.Epsilon);// ��ʱ�Ƴ���
			for (Integer s : rela.get(it)) {// ������node�Ľڵ�
				// first(it)-{��}<-first(s)
				first_set.get(s).addAll(its);// ��it���ϵ�Ԫ�غϲ���s�У���Ȼ����Ҫ�����Ƿ������
			}
			if (epsilon)
				its.add(CommProcess.Epsilon);
		}
	}

	/**
	 * �������� ������node�ڵ�������ڵ�
	 * ���㷨����������һ�����������˳�򣬻ش�������⣺���ĸ��������ȼ������������У��ĸ�����ӣ��Ų��������©��
	 * 
	 * @param rela
	 * @return
	 */
	public static LinkedList<Integer> SetOpSort(Map<Integer, Set<Integer>> rela) {
		LinkedList<Integer> depend_list = new LinkedList<>();
		Map<Integer, Boolean> his = new HashMap<Integer, Boolean>();
		for (Integer s : rela.keySet()) {
			his.put(s, false);
		}
		for (Integer s : rela.keySet()) {
			dependby(s, his, rela, depend_list);
		}
		return depend_list;
	}

	/**
	 * ��������
	 * 
	 * @param depend_list
	 * @param his
	 * @param node
	 */
	private static void dependby(Integer node, Map<Integer, Boolean> his,
			Map<Integer, Set<Integer>> rela, LinkedList<Integer> depend_list) {
		if (!his.containsKey(node) || his.get(node))// �����ʹ�������
			return;
		his.put(node, true);
		for (Integer s : rela.get(node)) {// ������node�Ľڵ�
			dependby(s, his, rela, depend_list);
		}
		// ��������е��������ܹ����
		depend_list.addFirst(node);
	}

	/**
	 * �ƶ�ָ���ķ��ս���Ƿ����Ϊ��
	 * 
	 * @param T
	 * @return
	 */
	public boolean inferUnTermEpsilon(int T) {
		return first_set.get(T).contains(CommProcess.Epsilon);
	}

	/**
	 * �ƶ�ָ���ķ��ս���Ƿ����Ϊ��
	 * 
	 * @param pros
	 * @param T
	 * @return
	 */
	private boolean _inferUnTermEpsilon(int T) {
		if (isEpsilon.get(T) != null) {
			return isEpsilon.get(T);
		}
		if (first_set.get(T).contains("��")) {
			isEpsilon.put(T, true);
			return true;
		}
		Integer[][] pro = G.prod[T];
		outer: for (Integer[] p : pro) {
			for (Integer t : p) {
				if(t<0){// �ս��
					if (t!=CommProcess.Epsilon) {// �Ǧ��ս�����򲻿���Ϊ��
						continue outer;
					}
				}else{// ���ս��
					if (!_inferUnTermEpsilon(t)) {// ����һ�����ս��������Ϊ���򲻿���Ϊ��
						continue outer;
					}
				}
			}
			first_set.get(T).add(CommProcess.Epsilon);
			isEpsilon.put(T, true);
			return true;// ��������ܣ���һ����ת���´�ѭ�����в���
		}
		isEpsilon.put(T, false);
		return false;// ������ܣ���һ����ǰ��Ĳ������ҵ������ˡ�
	}

}

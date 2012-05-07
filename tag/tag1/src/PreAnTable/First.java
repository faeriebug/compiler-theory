package PreAnTable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import PreAnTable.CommProcess.TokenProcesser;

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
	Map<String, Set<String>> rela;
	/** first���� */
	Map<String, Set<String>> first_set;
	/** ����ʽ */
	Map<String, String[]> pros;
	/** ��¼ĳ�����ս���Ƿ�Ϊ�� */
	Map<String, Boolean> isEpsilon;

	public static void main(String[] args) {
		First f = new First();
		String[] input = { "E->TE'", "E'->+TE'|��", "T->FT'", "T'->*FT'|��",
				"F->(E)|i" };
		f.SetGrammar(input);
		f.analyse();
		Map<String, Set<String>> first_set = f.getUnTermFirstSet();
		for (Entry<String, Set<String>> s : first_set.entrySet()) {// ����ÿһ�����ս��
			System.out.print("First(" + s.getKey() + ")={");
			String ss;
			for (Iterator<String> iterator = s.getValue().iterator(); iterator
					.hasNext();) {
				ss = iterator.next();
				System.out.print(ss);
				if (iterator.hasNext()) {
					System.out.print(",");
				} else {
					System.out.println("}");
				}
			}
		}
	}

	public void SetGrammar(String[] ps) {
		SetGrammar(CommProcess.Prod(ps));
	}

	public void SetGrammar(Map<String, String[]> pros) {
		rela = new HashMap<>();
		first_set = new HashMap<>();
		this.pros = pros;
		isEpsilon = new HashMap<>();
		for (String s : pros.keySet()) {
			first_set.put(s, new HashSet<String>());
			isEpsilon.put(s, null);
		}
		analyse();
	}

	/**
	 * ��ȡfirst��
	 * 
	 * @return
	 */
	public Map<String, Set<String>> getUnTermFirstSet() {
		return first_set;
	}

	/**
	 * ��ȡ�ִ���first��
	 * 
	 * @param pr
	 * @return
	 */
	public Set<String> getStringFirstSet(String pr) {
		Set<String> fi = new HashSet<>();
		Set<String> dep = new HashSet<>();
		TokenProcesser tr = new TokenProcesser(pr);
		while (tr.hasNextToken()) {
			String t = tr.nextToken();
			if (CommProcess.IsTerminal(t)) {// �ս��
				fi.add(t);// ��first���������Ԫ��
				return fi;
			} else {// ���ս��
				// ���������
				dep.add(t);// ��������t
				if (!inferUnTermEpsilon(t)) {// �˷��ս��������Ϊ�ţ�����������������
					break;
				} else if (!tr.hasNextToken()) {// �����һ�����ս�����ҿ���Ϊ��
					fi.add("��");
				}
			}
		}
		for (String d : dep) {
			Set<String> its = first_set.get(d);
			UnionWithoutEpsilon(its, fi);
		}
		return fi;
	}

	public static void UnionWithoutEpsilon(Set<String> src, Set<String> des) {
		boolean epsilon = src.contains("��");
		if (epsilon)
			src.remove("��");// ��ʱ�Ƴ���
		des.addAll(src);
		if (epsilon)
			src.add("��");
	}

	/**
	 * �����first��
	 * 
	 * @return
	 */
	private void analyse() {
		for (Entry<String, String[]> prod : pros.entrySet()) {// ����ÿһ�����ս��
			for (String p : prod.getValue()) {// ������Ӧ��ÿһ������ʽ
				TokenProcesser tr = new TokenProcesser(
						p);
				while (tr.hasNextToken()) {
					String t = tr.nextToken();
					if (CommProcess.IsTerminal(t)) {// �ս��
						if (t.equals("��") && tr.hasNextToken()) {// tΪ�ţ����������һ���ս�������Բ����롣
							continue;
						}
						first_set.get(prod.getKey()).add(t);// ��first���������Ԫ��
						break;
					} else {// ���ս����һ���ǵ�һ�����ս��������֮ǰ�ķ��ս��������Ϊ��
						// ���������
						if (!rela.containsKey(t)) {
							rela.put(t, new HashSet<String>());
						}
						rela.get(t).add(prod.getKey());// ��������t
						if (!_inferUnTermEpsilon(t)) {// �˷��ս��������Ϊ�ţ�����������������
							break;
						} else if (!tr.hasNextToken()) {// �����һ�����ս�����ҿ���Ϊ��
							first_set.get(prod.getKey()).add("��");
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
		LinkedList<String> depend_list = SetOpSort(rela);
		for (String it : depend_list) {
			Set<String> its = first_set.get(it);
			boolean epsilon = its.contains("��");
			if (epsilon)
				its.remove("��");// ��ʱ�Ƴ���
			for (String s : rela.get(it)) {// ������node�Ľڵ�
				// first(it)-{��}<-first(s)
				first_set.get(s).addAll(its);// ��it���ϵ�Ԫ�غϲ���s�У���Ȼ����Ҫ�����Ƿ������
			}
			if (epsilon)
				its.add("��");
		}
	}

	/**
	 * �������� ������node�ڵ�������ڵ�
	 * ���㷨����������һ�����������˳�򣬻ش�������⣺���ĸ��������ȼ������������У��ĸ�����ӣ��Ų��������©��
	 * 
	 * @param rela
	 * @return
	 */
	public static LinkedList<String> SetOpSort(Map<String, Set<String>> rela) {
		LinkedList<String> depend_list = new LinkedList<>();
		Map<String, Boolean> his = new HashMap<String, Boolean>();
		for (String s : rela.keySet()) {
			his.put(s, false);
		}
		for (String s : rela.keySet()) {
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
	private static void dependby(String node, Map<String, Boolean> his,
			Map<String, Set<String>> rela, LinkedList<String> depend_list) {
		if (!his.containsKey(node) || his.get(node))// �����ʹ�������
			return;
		his.put(node, true);
		for (String s : rela.get(node)) {// ������node�Ľڵ�
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
	public boolean inferUnTermEpsilon(String T) {
		return first_set.get(T).contains("��");
	}

	/**
	 * �ƶ�ָ���ķ��ս���Ƿ����Ϊ��
	 * 
	 * @param pros
	 * @param T
	 * @return
	 */
	private boolean _inferUnTermEpsilon(String T) {
		if (isEpsilon.get(T) != null) {
			return isEpsilon.get(T);
		}
		if (first_set.get(T).contains("��")) {
			isEpsilon.put(T, true);
			return true;
		}
		String[] pro = pros.get(T);
		outer: for (String p : pro) {
			TokenProcesser tr = new TokenProcesser(p);
			while (tr.hasNextToken()) {
				String t = tr.nextToken();
				if (!CommProcess.IsTerminal(t)) {// ���ս��
					if (!_inferUnTermEpsilon(t)) {// ����һ�����ս��������Ϊ���򲻿���Ϊ��
						continue outer;
					}
				} else {// �ս��
					if (!t.equals("��")) {// �Ǧ��ս�����򲻿���Ϊ��
						continue outer;
					}
				}
			}
			first_set.get(T).add("��");
			isEpsilon.put(T, true);
			return true;// ��������ܣ���һ����ת���´�ѭ�����в���
		}
		isEpsilon.put(T, false);
		return false;// ������ܣ���һ����ǰ��Ĳ������ҵ������ˡ�
	}

	// /**
	// * �ƶ��ִ��Ƿ�Ϊ��
	// *
	// * @param T
	// * @return
	// */
	// boolean inferStringEpsilon(String B) {
	// int index = 0;
	// while (index < B.length()) {
	// String t = getNextToken(B, index);
	// index += t.length();
	// if (!IsTerminal(t)) {// ���ս��
	// if (!_inferUnTermEpsilon(t)) {// ����һ�����ս��������Ϊ���򲻿���Ϊ��
	// return false;
	// }
	// } else {// �ս��
	// if (!t.equals("��")) {// �Ǧ��ս�����򲻿���Ϊ��
	// return false;
	// }
	// }
	// }
	// return true;// ��������ܣ���һ����ת���´�ѭ�����в���
	// }

}

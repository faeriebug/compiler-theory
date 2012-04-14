package PreAnTable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
	
	Map<String,Boolean> isEpsilon;

	public static void main(String[] args) {
		First f = new First();
		String[] input = { "E->TE'", "E'->+TE'|��", "T->FT'", "T'->*FT'|��",
				"F->(E)|i" };
		f.analyse(input);
	}

	public Map<String, Set<String>> analyse(String[] ps) {
		rela = new HashMap<>();
		first_set = new HashMap<>();
		pros = new HashMap<>();
		for (String s : ps) {
			String[] pr = s.split("->");
			String[] rs = pr[1].split("[|]");
			pros.put(pr[0], rs);
			first_set.put(pr[0], new HashSet<String>());
		}

		for (Entry<String, String[]> prod : pros.entrySet()) {// ����ÿһ�����ս��
			for (String p : prod.getValue()) {// ������Ӧ��ÿһ������ʽ
				int index = 0;
				while (index < p.length()) {// ��������ʽ��ÿһ������
					String t = getNextToken(p, index);
					index += t.length();
					if (IsTerminal(t)) {// �ս��
						first_set.get(prod.getKey()).add(t);// ��first���������Ԫ��
						break;
					} else {// ���ս��
						// ���������
						if (!rela.containsKey(t)) {
							rela.put(t, new HashSet<String>());
						}
						rela.get(t).add(prod.getKey());// ��������t
						if (!inferEpsilon(t)) {// �˷��ս��������Ϊ�ţ�����������������
							break;
						}
					}
				}
			}
		}
		fillSetByDependency();
		return first_set;
	}

	/**
	 * ����c��first���仯��������������ϵı仯 ������Ԫ���зǦŵ�Ҫ���
	 * 
	 * @param c
	 */
	void fillSetByDependency() {
		Map<String, Boolean> his = new HashMap<String, Boolean>();
		LinkedList<String> depend_list = new LinkedList<>();
		for (String s : rela.keySet()) {
			his.put(s, false);
		}
		for (String s : rela.keySet()) {
			dependby(depend_list, his, s);
		}
		String it = null;
		while ((it = depend_list.pollLast()) != null) {
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
	 * ������node�ڵ�������ڵ� ���㷨����������һ�����������˳�򣬻ش�������⣺���ĸ��������ȼ������������У��ĸ�����ӣ��Ų��������©����
	 * 
	 * @param depend_list
	 * @param his
	 * @param node
	 */
	void dependby(List<String> depend_list, Map<String, Boolean> his,
			String node) {
		if (!his.containsKey(node) || his.get(node))// �����ʹ�������
			return;
		his.put(node, true);
		for (String s : rela.get(node)) {// ������node�Ľڵ�
			dependby(depend_list, his, s);
		}
		// ��������е��������ܹ����
		depend_list.add(node);
	}

	/**
	 * �ƶ��ƶ��ķ��ս���Ƿ����Ϊ��
	 * 
	 * @param pros
	 * @param T
	 * @return
	 */
	boolean inferEpsilon(String T) {
		if (first_set.get(T).contains("��")) {
			return true;
		}
		String[] pro = pros.get(T);
		outer: for (String p : pro) {
			int index = 0;
			while (index < p.length()) {
				String t = getNextToken(p, index);
				index += t.length();
				if (!IsTerminal(t)) {// ���ս��
					if (!inferEpsilon(t)) {// ����һ�����ս��������Ϊ���򲻿���Ϊ��
						continue outer;
					}
				} else {// �ս��
					if (!t.equals("��")) {// �Ǧ��ս�����򲻿���Ϊ��
						continue outer;
					}
				}
			}
			first_set.get(T).add("��");
			return true;// ��������ܣ���һ����ת���´�ѭ�����в���
		}
		return false;// ������ܣ���һ����ǰ��Ĳ������ҵ������ˡ�
	}

	/**
	 * ��ȡ��һ������
	 * 
	 * @param pr
	 * @param start
	 * @return
	 */
	String getNextToken(String pr, int start) {
		char a = pr.charAt(start);
		if ((a >= '!' && a <= '~') || a == '��') {
			if (a >= 'A' && a <= 'Z') {// ����Ǵ�д��ĸ����Ҫ����A'��
				if (pr.length() > start + 1 && pr.charAt(start + 1) == '\'') {// ����A'��Ϊ����
					return pr.substring(start, start + 2);
				}
			}
			return pr.substring(start, start + 1);
		} else {
			return null;
		}
	}

	/**
	 * �ж��Ƿ����ս��
	 * 
	 * @param t
	 * @return
	 */
	boolean IsTerminal(String t) {
		char a = t.charAt(0);
		return !(a >= 'A' && a <= 'Z');
	}

}

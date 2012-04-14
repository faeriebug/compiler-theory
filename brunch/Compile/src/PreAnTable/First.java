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

	Map<String, Boolean> isEpsilon;

	public static void main(String[] args) {
		First f = new First();
		String[] input = { "E->TE'", "E'->+TE'|��", "T->FT'", "T'->*FT'|��",
				"F->(E)|i" };
		f.SetGrammar(input);
		f.analyse();
	}

	public void SetGrammar(String[] ps) {
		rela = new HashMap<>();
		first_set = new HashMap<>();
		pros = new HashMap<>();
		isEpsilon=new HashMap<>();
		for (String s : ps) {
			String[] pr = s.split("->");
			String[] rs = pr[1].split("[|]");
			pros.put(pr[0], rs);
			first_set.put(pr[0], new HashSet<String>());
			isEpsilon.put(pr[0], null);
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
	 * @param pr
	 * @return
	 */
	public Set<String> getStringFirstSet(String pr){
		Set<String> fi=new HashSet<>();
		Set<String> dep=new HashSet<>();
		int index = 0;
		while (index < pr.length()) {// ��������ʽ��ÿһ������
			String t = getNextToken(pr, index);
			index += t.length();
			if (IsTerminal(t)) {// �ս��
				fi.add(t);// ��first���������Ԫ��
				return fi;
			} else {// ���ս��
				// ���������
				dep.add(t);// ��������t
				if (!inferUnTermEpsilon(t)) {// �˷��ս��������Ϊ�ţ�����������������
					break;
				}else if(index>=pr.length()){//�����һ�����ս�����ҿ���Ϊ��
					fi.add("��");
				}
			}
		}
		for (String d : dep) {
			Set<String> its = first_set.get(d);
			UnionWithoutEpsilon(its,fi);
		}
		return fi;
	}

	public static void UnionWithoutEpsilon(Set<String> src,Set<String> des){
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
						if (!_inferUnTermEpsilon(t)) {// �˷��ս��������Ϊ�ţ�����������������
							break;
						}else if(index>=p.length()){//�����һ�����ս�����ҿ���Ϊ��
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
	 * ������node�ڵ�������ڵ� ���㷨����������һ�����������˳�򣬻ش�������⣺���ĸ��������ȼ������������У��ĸ�����ӣ��Ų��������©��
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
			Map<String, Set<String>> rela, List<String> depend_list) {
		if (!his.containsKey(node) || his.get(node))// �����ʹ�������
			return;
		his.put(node, true);
		for (String s : rela.get(node)) {// ������node�Ľڵ�
			dependby(s, his, rela, depend_list);
		}
		// ��������е��������ܹ����
		depend_list.add(node);
	}
	
	/**
	 * �ƶ�ָ���ķ��ս���Ƿ����Ϊ��
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
			int index = 0;
			while (index < p.length()) {
				String t = getNextToken(p, index);
				index += t.length();
				if (!IsTerminal(t)) {// ���ս��
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

	/**
	 * �ƶ��ִ��Ƿ�Ϊ��
	 * 
	 * @param T
	 * @return
	 */
	boolean inferStringEpsilon(String B) {
		int index = 0;
		while (index < B.length()) {
			String t = getNextToken(B, index);
			index += t.length();
			if (!IsTerminal(t)) {// ���ս��
				if (!_inferUnTermEpsilon(t)) {// ����һ�����ս��������Ϊ���򲻿���Ϊ��
					return false;
				}
			} else {// �ս��
				if (!t.equals("��")) {// �Ǧ��ս�����򲻿���Ϊ��
					return false;
				}
			}
		}
		return true;// ��������ܣ���һ����ת���´�ѭ�����в���
	}

	/**
	 * ��ȡ��һ������
	 * 
	 * @param pr
	 * @param start
	 * @return
	 */
	public static String getNextToken(String pr, int start) {
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
	public static boolean IsTerminal(String t) {
		char a = t.charAt(0);
		return !(a >= 'A' && a <= 'Z');
	}

}

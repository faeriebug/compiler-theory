package PreAnTable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Follow {
	/** ���ս��֮���������ϵ */
	Map<String, Set<String>> rela;
	/** first���� */
	Map<String, Set<String>> follow_set;
	/** ����ʽ */
	Map<String, String[]> pros;
	First first = new First();
	
	public void SetGrammar(Map<String, String[]> pros,String G){
		this.pros=pros;
		first.SetGrammar(pros);
		rela = new HashMap<>();
		follow_set = new HashMap<>();
		for (String s : pros.keySet()) {
			follow_set.put(s, new HashSet<String>());
		}
		follow_set.get(G).add("#");
		analyse();
	}
	
	public void SetGrammar(String[] ps,String G) {
		SetGrammar(PreProcess.Process(ps), G);
	}

	public Map<String, Set<String>> getFollowSet(){
		return follow_set;
	}
	
	private void analyse() {
		for (Entry<String, String[]> prod : pros.entrySet()) {// ����ÿһ�����ս��
			for (String p : prod.getValue()) {// ������Ӧ��ÿһ������ʽ
				int index = 0;
				while (index < p.length()) {// ��������ʽ��ÿһ������
					String t = First.getNextToken(p, index);
					index += t.length();
					if (!First.IsTerminal(t)) {// ���ս��
						// first����
						firstAna(p, t, index);
						// follow����
						followAna(p, t, prod.getKey(), index);
					}
				}
			}
		}
		fillSetByDependency();
	}

	private void fillSetByDependency() {
		LinkedList<String> depend_list = First.SetOpSort(rela);
		for (String it : depend_list) {
			Set<String> its = follow_set.get(it);
			for (String s : rela.get(it)) {// ������node�Ľڵ�
				// first(it)-{��}<-first(s)
				follow_set.get(s).addAll(its);// ��it���ϵ�Ԫ�غϲ���s��
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Follow f = new Follow();
		String[] input = { "E->TE'", "E'->+TE'|��", "T->FT'", "T'->*FT'|��",
				"F->(E)|i" };
		f.SetGrammar(input,"E");
		f.analyse();
		Map<String, Set<String>> first_set=f.getFollowSet();
		for (Entry<String, Set<String>> s : first_set.entrySet()) {// ����ÿһ�����ս��
			System.out.print("First("+s.getKey()+")={");
			String ss;
			for (Iterator<String> iterator = s.getValue().iterator(); iterator.hasNext();) {
				ss=iterator.next();
				System.out.print(ss);
				if(iterator.hasNext()){
					System.out.print(",");
				}else{
					System.out.println("}");
				}
			}
		}
	}

	/**
	 * ���ս��t�ڲ���ʽp�з���,ֱ����ӵ�t�� first(b)-��<-fist(t)
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
	 * ���ս��t�ڲ���ʽp�з���,����follow����
	 * 
	 * @param p
	 * @param A
	 * @param B
	 * @param start
	 */
	void followAna(String p, String A, String B, int start) {
		if(A.equals(B))return;//�Լ��Ͳ��ú��Լ��ϲ���
		if (start < p.length()) {
			Set<String> re = first.getStringFirstSet(p.substring(start));
			if (!re.contains("��"))
				return;
		}
		if (!rela.containsKey(B)) {
			rela.put(B, new HashSet<String>());
		}
		rela.get(B).add(A);// B��A����
	}



}

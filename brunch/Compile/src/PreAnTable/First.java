package PreAnTable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 求first集
 * <p>
 * 基于消除了公共左因子和左递归的文法
 * 
 * @author WuyaMony
 * 
 */
public class First {
	/** 非终结符之间的依赖关系 */
	Map<String, Set<String>> rela;
	/** first集合 */
	Map<String, Set<String>> first_set;
	/** 产生式 */
	Map<String, String[]> pros;
	
	Map<String,Boolean> isEpsilon;

	public static void main(String[] args) {
		First f = new First();
		String[] input = { "E->TE'", "E'->+TE'|ε", "T->FT'", "T'->*FT'|ε",
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

		for (Entry<String, String[]> prod : pros.entrySet()) {// 遍历每一个非终结符
			for (String p : prod.getValue()) {// 遍历对应的每一个产生式
				int index = 0;
				while (index < p.length()) {// 遍历产生式的每一个符号
					String t = getNextToken(p, index);
					index += t.length();
					if (IsTerminal(t)) {// 终结符
						first_set.get(prod.getKey()).add(t);// 向first集合中添加元素
						break;
					} else {// 非终结符
						// 添加依赖项
						if (!rela.containsKey(t)) {
							rela.put(t, new HashSet<String>());
						}
						rela.get(t).add(prod.getKey());// 此依赖于t
						if (!inferEpsilon(t)) {// 此非终结符不可能为ε，不必再向下搜索了
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
	 * 集合c的first集变化，引起的依赖集合的变化 新增的元素中非ε的要添加
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
			boolean epsilon = its.contains("ε");
			if (epsilon)
				its.remove("ε");// 暂时移除ε
			for (String s : rela.get(it)) {// 依赖于node的节点
				// first(it)-{ε}<-first(s)
				first_set.get(s).addAll(its);// 将it集合的元素合并到s中，当然这里要考虑是否包含ε
			}
			if (epsilon)
				its.add("ε");
		}
	}

	/**
	 * 依赖于node节点的其他节点 本算法就是搜索出一个集合运算的顺序，回答这个问题：”哪个个集合先加入依赖集合中，哪个个后加，才不会出现遗漏“。
	 * 
	 * @param depend_list
	 * @param his
	 * @param node
	 */
	void dependby(List<String> depend_list, Map<String, Boolean> his,
			String node) {
		if (!his.containsKey(node) || his.get(node))// 被访问过，回溯
			return;
		his.put(node, true);
		for (String s : rela.get(node)) {// 依赖于node的节点
			dependby(depend_list, his, s);
		}
		// 完成了所有的依赖才能够添加
		depend_list.add(node);
	}

	/**
	 * 推断制定的非终结符是否可能为ε
	 * 
	 * @param pros
	 * @param T
	 * @return
	 */
	boolean inferEpsilon(String T) {
		if (first_set.get(T).contains("ε")) {
			return true;
		}
		String[] pro = pros.get(T);
		outer: for (String p : pro) {
			int index = 0;
			while (index < p.length()) {
				String t = getNextToken(p, index);
				index += t.length();
				if (!IsTerminal(t)) {// 非终结符
					if (!inferEpsilon(t)) {// 其中一个非终结符不可能为空则不可能为空
						continue outer;
					}
				} else {// 终结符
					if (!t.equals("ε")) {// 非ε终结符，则不可能为空
						continue outer;
					}
				}
			}
			first_set.get(T).add("ε");
			return true;// 如果不可能，则一定跳转到下次循环进行查找
		}
		return false;// 如果可能，则一定在前面的查找中找到返回了。
	}

	/**
	 * 获取下一个符号
	 * 
	 * @param pr
	 * @param start
	 * @return
	 */
	String getNextToken(String pr, int start) {
		char a = pr.charAt(start);
		if ((a >= '!' && a <= '~') || a == 'ε') {
			if (a >= 'A' && a <= 'Z') {// 如果是大写字母，则要考虑A'。
				if (pr.length() > start + 1 && pr.charAt(start + 1) == '\'') {// 类似A'看为整体
					return pr.substring(start, start + 2);
				}
			}
			return pr.substring(start, start + 1);
		} else {
			return null;
		}
	}

	/**
	 * 判断是否是终结符
	 * 
	 * @param t
	 * @return
	 */
	boolean IsTerminal(String t) {
		char a = t.charAt(0);
		return !(a >= 'A' && a <= 'Z');
	}

}

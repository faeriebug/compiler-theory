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
	/** 记录某个非终结符是否为ε */
	Map<String, Boolean> isEpsilon;

	public static void main(String[] args) {
		First f = new First();
		String[] input = { "E->TE'", "E'->+TE'|ε", "T->FT'", "T'->*FT'|ε",
				"F->(E)|i" };
		f.SetGrammar(input);
		f.analyse();
		Map<String, Set<String>> first_set = f.getUnTermFirstSet();
		for (Entry<String, Set<String>> s : first_set.entrySet()) {// 遍历每一个非终结符
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
	 * 获取first集
	 * 
	 * @return
	 */
	public Map<String, Set<String>> getUnTermFirstSet() {
		return first_set;
	}

	/**
	 * 获取字串的first集
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
			if (CommProcess.IsTerminal(t)) {// 终结符
				fi.add(t);// 向first集合中添加元素
				return fi;
			} else {// 非终结符
				// 添加依赖项
				dep.add(t);// 此依赖于t
				if (!inferUnTermEpsilon(t)) {// 此非终结符不可能为ε，不必再向下搜索了
					break;
				} else if (!tr.hasNextToken()) {// 即最后一个非终结符，且可能为空
					fi.add("ε");
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
		boolean epsilon = src.contains("ε");
		if (epsilon)
			src.remove("ε");// 暂时移除ε
		des.addAll(src);
		if (epsilon)
			src.add("ε");
	}

	/**
	 * 计算出first集
	 * 
	 * @return
	 */
	private void analyse() {
		for (Entry<String, String[]> prod : pros.entrySet()) {// 遍历每一个非终结符
			for (String p : prod.getValue()) {// 遍历对应的每一个产生式
				TokenProcesser tr = new TokenProcesser(
						p);
				while (tr.hasNextToken()) {
					String t = tr.nextToken();
					if (CommProcess.IsTerminal(t)) {// 终结符
						if (t.equals("ε") && tr.hasNextToken()) {// t为ε，但不是最后一个终结符，所以不加入。
							continue;
						}
						first_set.get(prod.getKey()).add(t);// 向first集合中添加元素
						break;
					} else {// 非终结符，一般是第一个非终结符，或者之前的非终结符都可能为ε
						// 添加依赖项
						if (!rela.containsKey(t)) {
							rela.put(t, new HashSet<String>());
						}
						rela.get(t).add(prod.getKey());// 此依赖于t
						if (!_inferUnTermEpsilon(t)) {// 此非终结符不可能为ε，不必再向下搜索了
							break;
						} else if (!tr.hasNextToken()) {// 即最后一个非终结符，且可能为空
							first_set.get(prod.getKey()).add("ε");
						}
					}
				}
			}
		}
		fillSetByDependency();// 根据依赖项填充各first集
	}

	/**
	 * 根据依赖项填充各first集
	 */
	private void fillSetByDependency() {
		LinkedList<String> depend_list = SetOpSort(rela);
		for (String it : depend_list) {
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
	 * 拓扑排序 依赖于node节点的其他节点
	 * 本算法就是搜索出一个集合运算的顺序，回答这个问题：”哪个个集合先加入依赖集合中，哪个个后加，才不会出现遗漏“
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
	 * 依赖排序
	 * 
	 * @param depend_list
	 * @param his
	 * @param node
	 */
	private static void dependby(String node, Map<String, Boolean> his,
			Map<String, Set<String>> rela, LinkedList<String> depend_list) {
		if (!his.containsKey(node) || his.get(node))// 被访问过，回溯
			return;
		his.put(node, true);
		for (String s : rela.get(node)) {// 依赖于node的节点
			dependby(s, his, rela, depend_list);
		}
		// 完成了所有的依赖才能够添加
		depend_list.addFirst(node);
	}

	/**
	 * 推断指定的非终结符是否可能为ε
	 * 
	 * @param T
	 * @return
	 */
	public boolean inferUnTermEpsilon(String T) {
		return first_set.get(T).contains("ε");
	}

	/**
	 * 推断指定的非终结符是否可能为ε
	 * 
	 * @param pros
	 * @param T
	 * @return
	 */
	private boolean _inferUnTermEpsilon(String T) {
		if (isEpsilon.get(T) != null) {
			return isEpsilon.get(T);
		}
		if (first_set.get(T).contains("ε")) {
			isEpsilon.put(T, true);
			return true;
		}
		String[] pro = pros.get(T);
		outer: for (String p : pro) {
			TokenProcesser tr = new TokenProcesser(p);
			while (tr.hasNextToken()) {
				String t = tr.nextToken();
				if (!CommProcess.IsTerminal(t)) {// 非终结符
					if (!_inferUnTermEpsilon(t)) {// 其中一个非终结符不可能为空则不可能为空
						continue outer;
					}
				} else {// 终结符
					if (!t.equals("ε")) {// 非ε终结符，则不可能为空
						continue outer;
					}
				}
			}
			first_set.get(T).add("ε");
			isEpsilon.put(T, true);
			return true;// 如果不可能，则一定跳转到下次循环进行查找
		}
		isEpsilon.put(T, false);
		return false;// 如果可能，则一定在前面的查找中找到返回了。
	}

	// /**
	// * 推断字串是否为空
	// *
	// * @param T
	// * @return
	// */
	// boolean inferStringEpsilon(String B) {
	// int index = 0;
	// while (index < B.length()) {
	// String t = getNextToken(B, index);
	// index += t.length();
	// if (!IsTerminal(t)) {// 非终结符
	// if (!_inferUnTermEpsilon(t)) {// 其中一个非终结符不可能为空则不可能为空
	// return false;
	// }
	// } else {// 终结符
	// if (!t.equals("ε")) {// 非ε终结符，则不可能为空
	// return false;
	// }
	// }
	// }
	// return true;// 如果不可能，则一定跳转到下次循环进行查找
	// }

}

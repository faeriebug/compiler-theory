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
 * 求first集
 * <p>
 * 基于消除了公共左因子和左递归的文法
 * 
 * @author WuyaMony
 * 
 */
public class First {
	/** 非终结符之间的依赖关系 */
	Map<Integer, Set<Integer>> rela;
	/** first集合 */
	Map<Integer, Set<Integer>> first_set;
	/** 记录某个非终结符是否为ε */
	Map<Integer, Boolean> isEpsilon;
	Grammar G;
	public static void main(String[] args) {
		First f = new First();
		String[] input = { "<E>-><T><E'>", "<E'>->+<T><E'>| ε",
				"<T>-><F><T'>","<T'>->*<F><T'>| ε","<F>->(<E>) | i" };
		Grammar G=CommProcess.ProcessProd(input,false);
		f.SetGrammar(G);
		f.analyse();
		Map<Integer, Set<Integer>> first_set = f.getUnTermFirstSet();
		for (Entry<Integer, Set<Integer>> s : first_set.entrySet()) {// 遍历每一个非终结符
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
	 * 获取first集
	 * 
	 * @return
	 */
	public Map<Integer, Set<Integer>> getUnTermFirstSet() {
		return first_set;
	}

	/**
	 * 获取字串的first集
	 * 
	 * @param pr
	 * @return
	 */
	public Set<Integer> getStringFirstSet(Integer[] pr,int start) {
		Set<Integer> fi = new HashSet<>();
		Set<Integer> dep = new HashSet<>();
		for (int t=start;t<pr.length;t++) {
			if(pr[t]<0){// 终结符
				fi.add(-pr[t]);// 向first集合中添加元素
				return fi;
			}else{// 非终结符
				// 添加依赖项
				dep.add(pr[t]);// 此依赖于t
				if (!inferUnTermEpsilon(pr[t])) {// 此非终结符不可能为ε，不必再向下搜索了
					break;
				} else if (t==pr.length-1) {// 即最后一个非终结符，且可能为空
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
			src.remove(CommProcess.Epsilon);// 暂时移除ε
		des.addAll(src);
		if (epsilon)
			src.add(CommProcess.Epsilon);
	}

	/**
	 * 计算出first集
	 * 
	 * @return
	 */
	private void analyse() {
		for (int prod = 0;prod < G.prod.length; prod++) {// 遍历每一个非终结符
			for (Integer[] p : G.prod[prod]) {// 遍历对应的每一个产生式
				for (int i=0;i<p.length;i++) {
					if(p[i]<0){// 终结符
						if (p[i]==CommProcess.Epsilon && i!=p.length-1) {// t为ε，但不是最后一个终结符，所以不加入。
							continue;
						}
						first_set.get(prod).add(-p[i]);// 向first集合中添加元素
						break;
					}else{// 非终结符，一般是第一个非终结符，或者之前的非终结符都可能为ε
						// 添加依赖项
						if (!rela.containsKey(p[i])) {
							rela.put(p[i], new HashSet<Integer>());
						}
						rela.get(p[i]).add(prod);// 此依赖于t
						if (!_inferUnTermEpsilon(p[i])) {// 此非终结符不可能为ε，不必再向下搜索了
							break;
						} else if (i==p.length-1) {// 即最后一个非终结符，且可能为空
							first_set.get(prod).add(CommProcess.Epsilon);
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
		LinkedList<Integer> depend_list = SetOpSort(rela);
		for (Integer it : depend_list) {
			Set<Integer> its = first_set.get(it);
			boolean epsilon = its.contains(CommProcess.Epsilon);
			if (epsilon)
				its.remove(CommProcess.Epsilon);// 暂时移除ε
			for (Integer s : rela.get(it)) {// 依赖于node的节点
				// first(it)-{ε}<-first(s)
				first_set.get(s).addAll(its);// 将it集合的元素合并到s中，当然这里要考虑是否包含ε
			}
			if (epsilon)
				its.add(CommProcess.Epsilon);
		}
	}

	/**
	 * 拓扑排序 依赖于node节点的其他节点
	 * 本算法就是搜索出一个集合运算的顺序，回答这个问题：”哪个个集合先加入依赖集合中，哪个个后加，才不会出现遗漏“
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
	 * 依赖排序
	 * 
	 * @param depend_list
	 * @param his
	 * @param node
	 */
	private static void dependby(Integer node, Map<Integer, Boolean> his,
			Map<Integer, Set<Integer>> rela, LinkedList<Integer> depend_list) {
		if (!his.containsKey(node) || his.get(node))// 被访问过，回溯
			return;
		his.put(node, true);
		for (Integer s : rela.get(node)) {// 依赖于node的节点
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
	public boolean inferUnTermEpsilon(int T) {
		return first_set.get(T).contains(CommProcess.Epsilon);
	}

	/**
	 * 推断指定的非终结符是否可能为ε
	 * 
	 * @param pros
	 * @param T
	 * @return
	 */
	private boolean _inferUnTermEpsilon(int T) {
		if (isEpsilon.get(T) != null) {
			return isEpsilon.get(T);
		}
		if (first_set.get(T).contains("ε")) {
			isEpsilon.put(T, true);
			return true;
		}
		Integer[][] pro = G.prod[T];
		outer: for (Integer[] p : pro) {
			for (Integer t : p) {
				if(t<0){// 终结符
					if (t!=CommProcess.Epsilon) {// 非ε终结符，则不可能为空
						continue outer;
					}
				}else{// 非终结符
					if (!_inferUnTermEpsilon(t)) {// 其中一个非终结符不可能为空则不可能为空
						continue outer;
					}
				}
			}
			first_set.get(T).add(CommProcess.Epsilon);
			isEpsilon.put(T, true);
			return true;// 如果不可能，则一定跳转到下次循环进行查找
		}
		isEpsilon.put(T, false);
		return false;// 如果可能，则一定在前面的查找中找到返回了。
	}

}

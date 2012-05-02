package PreAnTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import PreAnTable.CommProcess.TokenProcesser;

public class LR_0 {
	/**��Ŀ*/
	private class item {
		// int itemnumber;
		String unterm;
		int proID;
		int start;

		public item(String unterm, int pro, int start) {
			this.unterm = unterm;
			this.proID = pro;
			this.start = start;
		}
	}

	/** ��Ŀ�� */
	private class Itemset {
		HashSet<Integer> core = new HashSet<>();
		HashSet<Integer> items = new HashSet<>();
		
		@Override
		public int hashCode() {
//			System.out.println("super.hashcode="+super.hashCode());
			return core.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof Itemset && ((Itemset) o).core.equals(this.core);
		}

	}

	/** ������Ŀ��ӳ�� */
	private item[] allitems;
	/** ��Ŀ��Ӧ�ıհ� */
	private Map<Integer, HashSet<Integer>> itemClosure;
	/** ���ս����Ӧÿ����һ����ʽ����Ŀ��� */
	private Map<String, Integer[]> untermBeginItem;
	/** ����ʽ */
	private Map<String, String[]> pros;
	/** go���� */
	private HashMap<Itemset, HashMap<String, Itemset>> go;
	/** ��Ŀ���淶�� */
	private HashMap<Itemset, Itemset> ccsi;
	/** ��ʼ����ʽ�ı�� */
	private int startProNumber;
	/** ��ʼ����ʽ */
	private String G;

	
	public static void main(String arg[]){
		
	}
	
	public void SetGrammar(String[] ps, String G) {
		pros = CommProcess.Prod(ps);
		go = new HashMap<>();
		ccsi = new HashMap<>();
		this.G = G;
		Fill_allitems();
		Fill_go();
	}

	void analyse() {

	}

	void Fill_go() {
		Itemset I0 = new Itemset();
		I0.core.add(startProNumber);
		I0.items.addAll(closure(startProNumber));
		ccsi.put(I0, I0);
		LinkedList<Itemset> checkSet = new LinkedList<>();
		checkSet.addLast(I0);
		while (!checkSet.isEmpty()) {
			Itemset I = checkSet.poll();
			HashMap<String, Itemset> goTmp = new HashMap<>();
			for (Integer i : I.items) {
				item ii = allitems[i];
				TokenProcesser t = new TokenProcesser(
						pros.get(ii.unterm)[ii.proID].substring(ii.start));
				if (t.hasNextToken()) {
					String tt = t.nextToken();
					if (!goTmp.containsKey(tt)) {
						goTmp.put(tt, new Itemset());
					}
					Itemset itmp = goTmp.get(tt);
					itmp.core.add(i + 1);
				}
			}
			for (String k : goTmp.keySet()) {
				Itemset kk = goTmp.get(k);
				if (ccsi.containsKey(kk)) {
					goTmp.put(k, ccsi.get(kk));
				} else {
					for(Integer d:kk.core){
						kk.items.addAll(closure(d));
					}
					ccsi.put(kk, kk);
					checkSet.addLast(kk);
				}
			}
		}
	}

	/**
	 * ����������Ŀ�����
	 */
	void Fill_allitems() {
		ArrayList<item> array_allitems = new ArrayList<>();
		for (Entry<String, String[]> pro : pros.entrySet()) {
			ArrayList<Integer> beginItems = new ArrayList<Integer>();
			String[] pp = pro.getValue();
			for (int i = 0; i < pp.length; i++) {
				TokenProcesser tr = new TokenProcesser(pp[i]);
				array_allitems.add(new item(pro.getKey(), i,0));//Բ��λ����ʼ��
				beginItems.add(array_allitems.size() - 1);
				if (pro.getKey().equals(G))
					startProNumber = array_allitems.size() - 1;
				while (tr.hasNextToken()) {
					tr.nextToken();
					array_allitems.add(new item(pro.getKey(), i, tr.Start()));
				}
			}
			Integer[] a = new Integer[beginItems.size()];
			untermBeginItem.put(pro.getKey(), beginItems.toArray(a));
		}
		allitems = new item[array_allitems.size()];
		array_allitems.toArray(allitems);
	}


	/**
	 * ��һ����Ŀ�ıհ�
	 * 
	 * @param i
	 * @return
	 */
	HashSet<Integer> closure(int itemnumber) {
		if (itemClosure.containsKey(itemnumber)) {
			return itemClosure.get(itemnumber);
		}
		HashSet<Integer> c = new HashSet<>();
		c.add(itemnumber);// i->closure(i);
		item i = allitems[itemnumber];
		TokenProcesser tr = new TokenProcesser(
				pros.get(i.unterm)[i.proID].substring(i.start));
		String t;
		if (tr.hasNextToken()) {
			t = tr.nextToken();
			if (!CommProcess.IsTerminal(t)) {
				for (Integer ii : untermBeginItem.get(t)) {
					c.addAll(closure(ii));
				}
			}
		}
		itemClosure.put(itemnumber, c);
		return c;
	}
}

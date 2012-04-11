package ComLF;
import java.util.ArrayList;

public class Main {
	private ArrayList<String> pro = new ArrayList<>();

	public void ExComLF(String p) {
		String[] pr = p.split("->");
		String[] rs = pr[1].split("[|]");
		Node tree = new Node();
		for (String r : rs) {
			insert(r, tree);
		}
		reduce(tree);
		print(tree, "");
		generate(tree, 0);
		for(String s:pro){
			System.out.println(s);
		}
	}

	void insert(String r, Node tree) {
		if (tree == null)
			return;
		for (Node i = tree.child; i != null; i = i.nexBro) {
			if(i.value.isEmpty())
				continue;
			else if (i.value.charAt(0) == r.charAt(0)) {
				if(i.child==null){// 此节点没有子树，构建子树
					Node ch = new Node();
					ch.value = i.value.substring(1);
					i.child = ch;
					i.value = Character.toString(i.value.charAt(0));
				}
				// 插入到此节点的子树中
				insert(r.substring(1), i);
				return;
			}
		}
		Node ch = new Node();
		ch.value = r;
		ch.nexBro = tree.child;
		tree.child = ch;
	}

	void reduce(Node tree) {
		if (tree.child != null && tree.child.nexBro == null && !tree.value.isEmpty()) {// 即子树只有一个分支，可以精简
			tree.value += tree.child.value;
			tree.child = tree.child.child;
			reduce(tree);
		} else {
			for (Node i = tree.child; i != null; i = i.nexBro) {
				reduce(i);
			}
		}
	}

	void print(Node tree, String prefix) {
		System.out.println(prefix + "|-:" + tree.value);
		for (Node i = tree.child; i != null; i = i.nexBro) {
			print(i, prefix + "   ");
		}
	}

	void generate(Node tree, int num) {
		if (tree.child == null)
			return;
		pro.add(Character.toString((char)('A' + num)) + "'->");
		for (Node i = tree.child; i != null; i = i.nexBro) {
			// System.out.print(i.value);
			if (i.child != null) {
				pro.set(num, pro.get(num)+i.value+Character.toString((char)('A' + pro.size()))+"'"+((i.nexBro != null)?"|":""));
				generate(i, pro.size());
			} else {
				pro.set(num, pro.get(num)+(i.value.isEmpty()?"ε":i.value)+((i.nexBro != null)?"|":""));
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Main a = new Main();
		a.ExComLF("A->a|ab|abcd|abef");
	}

}

package LRecursive;

/**
 * Ïû³ý×óµÝ¹é
 * @author WuyaMony
 *
 */
public class Main {
	void elim_dir(String p){
		String[] pr = p.split("->");
		String[] rs = pr[1].split("[|]");
		String a,b;
		a=pr[0]+"->";
		b=pr[0]+"'->";
		for(String s:rs){
			if(s.startsWith(pr[0])){
				b+=s.substring(pr[0].length())+pr[0]+"'|";
			}else{
				a+=s+pr[0]+"'|";
			}
		}
		b+="¦Å";
		System.out.println(a);
		System.out.println(b);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Main a=new Main();
		a.elim_dir("A->Aa|Ab|Ac|d|e");
	}

}

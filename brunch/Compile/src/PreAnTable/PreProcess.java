package PreAnTable;

import java.util.HashMap;
import java.util.Map;

public class PreProcess {
	public static Map<String, String[]> Process(String[] ps){
		Map<String, String[]> pros = new HashMap<>();
		for (String s : ps) {
			String[] pr = s.split("->");
			String[] rs = pr[1].split("[|]");
			for (int i = 0; i < rs.length; i++) {
				rs[i]=rs[i].trim();
			}
			pr[0]=pr[0].trim();
			pros.put(pr[0], rs);
		}
		return pros;
	}
}



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class Tokenize1 {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static Map<String, Integer> tokenizeFun(String args) throws IOException 
    {
        HashMap<String, Integer> tokens = new HashMap<>();
        List<String> stopWords = new ArrayList<>();
        boolean[] stopWordFlag;
        String con = new Scanner(new File("C:\\Users\\Bomule\\Desktop/stopwords.txt")).useDelimiter("\\Z").next();
        String[] stop = con.split("[\n]");
        for ( String sw : stop)
        {
            stopWords.add(sw);
        }
		String content = args;
		String[] arr = content.split("[^A-Za-z]");
		for ( String ss : arr) {
			if(!ss.equals("") && !ss.equals("\n"))
			{
				ss = ss.toLowerCase();
				if(tokens.get(ss) == null)
				{
				   tokens.put(ss, 1);
				}
				else
				{
					tokens.put(ss, tokens.get(ss) + 1);
				}
			}
		}

        int unique = 0;
        int tokens1 = 0;
        Map<String, Integer> tokensMap = new TreeMap<>(tokens);
        for (String key : tokensMap.keySet()) {
            unique++;
            tokens1 = tokens1 + tokensMap.get(key);
        }
        Map<String, Integer> sortedMap = sortByValues(tokens);

        stopWordFlag = new boolean[unique+1];
        int i = 1;
        for(String sw:sortedMap.keySet())
        {
            if(stopWords.contains(sw))
                stopWordFlag[i] = true;
            i++;
        }
        int count = printMap(sortedMap, 15,tokens1);
        Porter p = new Porter();
        HashMap<String, Integer> portstem = new HashMap<>();
        for (Map.Entry<String, Integer> entry : sortedMap.entrySet())
        {
            String str = entry.getKey();
            if(!stopWords.contains(str))
            {
                int a=entry.getValue();
                String amv = p.portingFun(str);
                if(portstem.get(amv) == null)
                    portstem.put(amv, a);
                else
                {
                    int ab = portstem.get(amv);
                    portstem.put(amv, a+ab);
                }
            }
        }
        Map<String, Integer> sortportstem = sortByValues(portstem);
        int uni=0;
        int cou = 0;
        for (Map.Entry<String, Integer> entry : sortportstem.entrySet())
        {
            uni++;
            cou += entry.getValue();
        }
        return sortportstem;
    }

    public static <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
        Comparator<K> valueComparator =  new Comparator<K>() {
            public int compare(K k1, K k2) {
                int compare = map.get(k2).compareTo(map.get(k1));
                if (compare == 0) return 1;
                else return compare;
            }
        };
        Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
    }

    public static int printMap(Map<String, Integer> map, float n, int words) {
        double p = 0;
        double s = 0;
        double p1;
        int k=0;
        DecimalFormat df = new DecimalFormat("#0.00"); 
        for (Map.Entry<String, Integer> entry : map.entrySet()) {

            s = s+(double)entry.getValue();
            p = s/(double)words * 100.00;
            p1 = entry.getValue()/(double)words * 100.00;
            k++;
            if(p>=n)
                return k;
        }
        return 0;
    }
}
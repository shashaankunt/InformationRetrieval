
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

class wordProp {

    double tf;
    double tf_idf;
}

class tfidf {

    public int termFreq;
    public int maxTf;
    public String Title;
    public double mag;
    public Map<String, Integer> sortportstem = new TreeMap<>();
    public Map<String, wordProp> tf_idf = new TreeMap<>();
}

class query {

    public int maxTf;
    public double mag;
    public Map<String, Integer> queFreq = new TreeMap<>();
    public Map<String, wordProp> queTf_Idf = new TreeMap<>();
    public Map<Integer, Double> cosSim = new TreeMap<>();
    public List<Integer> relevancy = new ArrayList<>();
}

public class VectRetrieval {

    public static printMap main(String args) throws IOException {
        String abc;
        abc = args;
        int k = 0;
        HashMap<Integer, String> docName = new HashMap<>();
        HashMap<Integer, tfidf> lis = new HashMap<>();
        HashMap<String, Integer> df = new HashMap<>();
        HashMap<Integer, query> queryMap = new HashMap<>();
        printMap oh = new printMap();
        DecimalFormat decF = new DecimalFormat("#0.0000");
        int N = 0;
        Tokenize1 obj1 = new Tokenize1();
        String docPath = "C:\\Users\\Bomule\\Desktop\\IRS/cranfieldDoc";
        File folder = new File(docPath);
        File[] listOfFiles = folder.listFiles();

        String content;
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                N++;
                tfidf a = new tfidf();
                Map<String, Integer> portstem = new TreeMap<>();
                content = new Scanner(new File(docPath + "/" + listOfFile.getName())).useDelimiter("\\Z").next();
                content = content.replace("\n", " ");
                final Pattern titlePattern = Pattern.compile("<TITLE>(.+?)</TITLE>");
                final Pattern docnoPattern = Pattern.compile("<DOCNO>(.+?)</DOCNO>");
                final Pattern text = Pattern.compile("<TEXT>(.+?)</TEXT>");
                final Matcher matchTitle = titlePattern.matcher(content);
                final Matcher matchDocno = docnoPattern.matcher(content);
                final Matcher text1 = text.matcher(content);
                matchTitle.find();
                matchDocno.find();
                text1.find();
                int docNo = Integer.parseInt(matchDocno.group(1).replace(" ", ""));
                docName.put(docNo, matchTitle.group(1));

                portstem = obj1.tokenizeFun(text1.group(1) + matchTitle.group(1));

                a.Title = matchTitle.group(1).replace(" .", ".").replaceFirst(" ", "");
                a.sortportstem = portstem;
                a.termFreq = calTF(portstem);

                boolean first = true;
                int maxTf = 1;
                for (Map.Entry<String, Integer> entry : portstem.entrySet()) {
                    String s = entry.getKey();
                    if (first == true) {
                        maxTf = entry.getValue();
                        first = false;
                    }
                    if (df.get(s) == null) {
                        df.put(s, 1);
                        k++;
                    } else {
                        df.replace(s, df.get(s), df.get(s) + 1);
                    }
                }
                a.maxTf = maxTf;
                lis.put(docNo, a);
            } else if (listOfFile.isDirectory()) {
                System.out.println("Directory " + listOfFile.getName());
            }
        }
        for (Map.Entry<Integer, tfidf> entry : lis.entrySet()) {
            tfidf a = entry.getValue();
            a = calTfIdf(a, df, N);
            lis.replace(entry.getKey(), entry.getValue(), a);
        }
        int itr = 0;

        magnitude(lis);
        // System.out.println("Please enter the query:");
        // Scanner scan = new Scanner(System.in);
        //String myLine = scan.nextLine();

        String que = abc;
        String[] queries = que.split("[\n]");

        int q = 1;
        for (String sw : queries) {
            Map<String, Integer> qmap = obj1.tokenizeFun(sw);
            query qu = new query();
            qu.queFreq = qmap;
            qu.maxTf = Collections.max(qmap.values());
            queryMap.put(q, qu);
            q++;
        }
        q = 1;

        QueryWeight(queryMap, df, N);
        magnitudeQuery(queryMap);
        for (Map.Entry<Integer, query> entry : queryMap.entrySet()) {
            double cSim = 0;
            int i = 1;
            for (Map.Entry<Integer, tfidf> entry1 : lis.entrySet()) {
                cSim = QueryProcessor(entry.getValue().queTf_Idf, entry1.getValue().tf_idf, entry.getValue().mag, entry1.getValue().mag);
                entry.getValue().cosSim.put(i++, cSim);
            }
        }
        int i = 1;

        int k1 = 1;
        for (Map.Entry<Integer, query> entry : queryMap.entrySet()) {

            Map<Integer, Double> relMap = sortByValues(entry.getValue().cosSim);
            System.out.printf("\n------------------------------------------------------\n");
            oh = printMap(relMap, entry.getKey(), entry.getValue().relevancy, 10);

            System.out.println("\n\n");
            k1++;

        }
        
        return oh;
    }

    public static double QueryProcessor(Map<String, wordProp> q, Map<String, wordProp> d, double a, double b) {
        double cSim = 0;
        double sum = 0;
        for (Map.Entry<String, wordProp> e : q.entrySet()) {
            double docWeight = 0;
            double queWeight = e.getValue().tf_idf;
            if (d.containsKey(e.getKey())) {
                docWeight = d.get(e.getKey()).tf_idf;
                sum += docWeight * queWeight;
            }
        }
        cSim = sum / (a * b);
        return cSim;
    }

    public static int calTF(final Map<String, Integer> map) {
        int count = 0;
        for (int a : map.values()) {
            count += a;
        }
        return count;
    }

    public static tfidf calTfIdf(tfidf a, HashMap<String, Integer> df, int N) {
        tfidf a1 = a;
        Map<String, Integer> map = a.sortportstem;
        a1.Title = a.Title;
        a1.sortportstem = map;
        a1.termFreq = a.termFreq;
        a1.maxTf = a.maxTf;
        Map<String, wordProp> k = new TreeMap<>();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            int docFreq = df.get(entry.getKey());
            wordProp ab = new wordProp();
            ab.tf = (double) entry.getValue() / (double) a.maxTf;
            ab.tf_idf = ab.tf * Math.log(N / docFreq) / Math.log(2);
            k.put(entry.getKey(), ab);
        }
        a1.tf_idf = k;

        return a1;
    }

    public static <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
        Comparator<K> valueComparator = new Comparator<K>() {
            public int compare(K k1, K k2) {
                int compare = map.get(k2).compareTo(map.get(k1));
                if (compare == 0) {
                    return 1;
                } else {
                    return compare;
                }
            }
        };
        Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
    }

    public static void magnitude(Map<Integer, tfidf> a) {
        for (Map.Entry<Integer, tfidf> k : a.entrySet()) {
            k.getValue().mag = 0.0;
            double val;
            double magnitude = 0.0;
            for (Map.Entry<String, wordProp> s : k.getValue().tf_idf.entrySet()) {
                val = k.getValue().tf_idf.get(s.getKey()).tf_idf;
                magnitude += val * val;
            }
            k.getValue().mag = Math.sqrt(magnitude);
        }
    }

    public static void magnitudeQuery(Map<Integer, query> a) {
        for (Map.Entry<Integer, query> k : a.entrySet()) {
            k.getValue().mag = 0.0;
            double val;
            double magnitude = 0.0;
            for (Map.Entry<String, wordProp> s : k.getValue().queTf_Idf.entrySet()) {
                val = k.getValue().queTf_Idf.get(s.getKey()).tf_idf;
                magnitude += val * val;
            }
            k.getValue().mag = Math.sqrt(magnitude);
        }
    }

    public static void QueryWeight(Map<Integer, query> a, Map<String, Integer> df, int N) {
        for (Map.Entry<Integer, query> k : a.entrySet()) {
            double mTf = k.getValue().maxTf;
            for (Map.Entry<String, Integer> q : k.getValue().queFreq.entrySet()) {
                double tf = q.getValue() / mTf;
                double d = 0;
                double tf_idf = 0;
                if (df.get(q.getKey()) != null) {
                    d = df.get(q.getKey());
                    tf_idf = tf * Math.log(N / d) / Math.log(2);
                }
                wordProp p = new wordProp();
                p.tf = tf;
                p.tf_idf = tf_idf;
                k.getValue().queTf_Idf.put(q.getKey(), p);
            }
        }
    }

    public static printMap printMap(Map<Integer, Double> map, int n, List<Integer> a, int k) throws IOException {
        int i = 1;
        int j = 0;
        printMap oh = new printMap();
        for (Map.Entry<Integer, Double> entry : map.entrySet()) {
            j++;
            System.out.printf("%-5s: ,%-4s", i++, entry.getKey());
            File file = new File("file" + entry.getKey() + ".txt");
            System.out.println("\n");
            System.out.println(file);
            String content = "";
            String line;
            // String arr1[]=null;
            BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\Bomule\\Desktop\\IRS\\cranfieldDoc\\file" + entry.getKey() + ".txt"));
            while ((line = reader.readLine()) != null) {
                content += "\n" + line;
            }
            content = content.substring(1);

            final Pattern pattern = Pattern.compile("<TITLE>(.+?)</TITLE>");
            final Matcher matcher = pattern.matcher(content);

            matcher.find();
            //	System.out.println(matcher.group(1));
            oh.list1om.add(matcher.group(1));

            String line1;
            String content1 = "";
            BufferedReader reader1 = new BufferedReader(new FileReader("C:\\Users\\Bomule\\Desktop\\IRS\\cranfieldDoc\\file" + entry.getKey() + ".txt"));
            while ((line1 = reader1.readLine()) != null) {
                content1 += "\n" + line1;
            }
            content1 = content1.substring(1);
            final Pattern pattern1 = Pattern.compile("<URL>(.+?)</URL>");
            final Matcher matcher1 = pattern1.matcher(content1);

            matcher1.find();
            //System.out.println(matcher1.group(1));
            oh.listgo.add(matcher1.group(1));

            if (a.contains(entry.getKey())) {
                System.out.print(" \n");

            } else {
                System.out.print("\n");
            }
            if (j >= k) {
                break;
            }
        }
        return oh;
    }
}

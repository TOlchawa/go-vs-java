import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.CountDownLatch;

public class TestBookRead {

  private static String text;
  private static final String[] separators = {" ", "\n", ",", ".", "-", ";", "(", ")", "\t", "\r", "!", "?", "'"};

  public static void main(String...args) throws IOException, InterruptedException {
    initialize();
    new TestBookRead().countWords(text);
  }

  private static void initialize() throws IOException {
    String filename = "ulyss10.txt";

    byte[] data;

    try (FileInputStream fis = new FileInputStream(filename); BufferedInputStream buf = new BufferedInputStream(fis)) {
      data = buf.readAllBytes();
    }

    text = new String(data);
  }

  private int countWords(String text) throws InterruptedException {

    Map<String, Integer> occurrence = null;

    for (int i=0; i<3; i++) {
      performTestLoop(text);
    }

    final CountDownLatch countDownLach = new CountDownLatch(10);
    long startTime = System.currentTimeMillis();
    for (int i=0; i<10; i++) {
      new Thread(() -> {
        Map<String, Integer> result = performTestLoop(text);
        countDownLach.countDown();
      }).start();
    }

    occurrence = performTestLoop(text);

    countDownLach.await();

    long stopTime = System.currentTimeMillis();

    List<String> keys = new ArrayList<>(occurrence.keySet());
    final Map<String,Integer> valuesMap = occurrence;
    Collections.sort(keys, (k1, k2) -> {
      int result = valuesMap.get(k1) - valuesMap.get(k2);
      if (result == 0) {
        result = k1.compareTo(k2);
      }
      return result;
    });

    int sum = 0;
    for (String w : keys) {
      sum += occurrence.get(w);
      System.out.println(w + ", " + occurrence.get(w));
    }

    System.out.println("DURATION: " + (stopTime-startTime));
    System.out.println("SUM: " + sum);

    return sum;

  }

  private Map<String, Integer> performTestLoop(String text) {
    HashMap<String, Integer> occurrence = new HashMap<>();
    for (int i = 0; i < 100; i++) {
      split(text, occurrence, separators);
    }
    return occurrence;
  }

  private void split(String text, Map<String, Integer> occurrence, String[] separators) {
    StringTokenizer st = new StringTokenizer(text, separators[0]);
    while (st.hasMoreTokens()) {
      if (separators.length > 1) {
        split(st.nextToken(), occurrence, Arrays.copyOfRange(separators, 1, separators.length));
      } else {
        occurrence.compute(st.nextToken(),(k,v) -> v == null ? 1 : v+1);
      }
    }
  }

}

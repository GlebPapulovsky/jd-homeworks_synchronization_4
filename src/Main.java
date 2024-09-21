import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<String> queueForA = new ArrayBlockingQueue<>(100);
        BlockingQueue<String> queueForB = new ArrayBlockingQueue<>(100);
        BlockingQueue<String> queueForC = new ArrayBlockingQueue<>(100);
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        final int lengthOfGeneratedString = 100_000;
        final String collectionOfSymbolsForGenerating = "abc";
        final int numberOfIterations = 10000;
        for (int i = 0; i < collectionOfSymbolsForGenerating.length(); i++) {
            map.put(String.valueOf(collectionOfSymbolsForGenerating.charAt(i)), 0);
        }

        Thread threadForGeneratingStrings = new Thread(() -> {
            for (int j = 0; j < numberOfIterations; j++) {
                Random random = new Random();
                StringBuilder text = new StringBuilder();
                for (int i = 0; i < lengthOfGeneratedString; i++) {
                    text.append(collectionOfSymbolsForGenerating.charAt(random.nextInt(collectionOfSymbolsForGenerating.length())));
                }
                try {
                    queueForA.put(text.toString());
                    System.out.println("put A");
                    queueForB.put(text.toString());
                    System.out.println("put B");
                    queueForC.put(text.toString());
                    System.out.println("put C");

                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        List<Thread> threadsList = new ArrayList<>();
        threadsList.add(new Thread(() -> {
            for (int j = 0; j < numberOfIterations; j++) {
                String takenText;
                char symbol = collectionOfSymbolsForGenerating.charAt(0);
                try {
                    takenText = queueForB.take();
                    System.out.println("take A");
                } catch (InterruptedException e) {
                    return;
                }
                int counterOfSymbols = symbolCounter(takenText, symbol);
                if (map.get(String.valueOf(symbol)) < counterOfSymbols) {
                    map.computeIfPresent(String.valueOf(symbol), (k, v) -> counterOfSymbols);
                }
            }
        }));
        threadsList.add(new Thread(() -> {
            for (int j = 0; j < numberOfIterations; j++) {
                String takenText;
                char symbol = collectionOfSymbolsForGenerating.charAt(1);
                try {
                    takenText = queueForC.take();
                    System.out.println("take B");
                } catch (InterruptedException e) {
                    return;
                }
                int counterOfSymbols = symbolCounter(takenText, symbol);
                if (map.get(String.valueOf(symbol)) < counterOfSymbols) {
                    map.computeIfPresent(String.valueOf(symbol), (k, v) -> counterOfSymbols);
                }
            }
        }));
        threadsList.add(new Thread(() -> {
            for (int j = 0; j < numberOfIterations; j++) {
                String takenText;
                char symbol = collectionOfSymbolsForGenerating.charAt(2);
                try {
                    takenText = queueForA.take();
                    System.out.println("take C");
                } catch (InterruptedException e) {
                    return;
                }
                int counterOfSymbols = symbolCounter(takenText, symbol);
                if (map.get(String.valueOf(symbol)) < counterOfSymbols) {
                    map.computeIfPresent(String.valueOf(symbol), (k, v) -> counterOfSymbols);
                }
            }
        }));
        //threads start
        threadForGeneratingStrings.start();

        for (Thread thread : threadsList) {
            thread.start();
        }
        for (Thread thread : threadsList) {
            thread.join();
        }
        threadForGeneratingStrings.join();

        for (String key : map.keySet()) {
            System.out.println("Symbol: " + key + " .Counter of repeats of symbol: " + map.get(key) + ".");
        }
    }


    public static int symbolCounter(String input, char symbol) {
        int counter = 0;
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == symbol) {
                counter++;
            }
        }
        return counter;
    }
}
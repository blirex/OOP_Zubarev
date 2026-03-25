import java.io.*;
import java.util.Scanner;
public class java2 {
    /**Клас для зберігання даних*/
    static class CalculationData implements Serializable {

        private static final long serialVersionUID = 1L;

        private double alpha;
        private int integerValue;
        private int maxOnesSequence;

        private transient long timestamp;

        public CalculationData(double alpha) {
            this.alpha = alpha;
            this.timestamp = System.currentTimeMillis(); 
        }

        public double getAlpha() {
            return alpha;
        }

        public int getIntegerValue() {
            return integerValue;
        }

        public void setIntegerValue(int integerValue) {
            this.integerValue = integerValue;
        }

        public int getMaxOnesSequence() {
            return maxOnesSequence;
        }

        public void setMaxOnesSequence(int maxOnesSequence) {
            this.maxOnesSequence = maxOnesSequence;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    /**Клас обчислення*/
    static class Calculator {

        private CalculationData data;

        public Calculator(CalculationData data) {
            this.data = data;
        }
        public void calculate() {

            double x = 10 * Math.cos(data.getAlpha());
            double s = Math.pow(x, 2) + Math.pow(x, 3);

            int integerPart = (int) s;
            data.setIntegerValue(integerPart);

            int maxSeq = findMaxOnesSequence(integerPart);
            data.setMaxOnesSequence(maxSeq);
        }

        /** Пошук найдовшої послідовності 1*/
        private int findMaxOnesSequence(int number) {

            String binary = Integer.toBinaryString(number);

            int max = 0;
            int current = 0;

            for (char c : binary.toCharArray()) {
                if (c == '1') {
                    current++;
                    if (current > max) {
                        max = current;
                    }
                } else {
                    current = 0;
                }
            }

            return max;
        }
    }

    /**Головний метод*/
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Введiть alpha (в радiанах): ");
        double alpha = scanner.nextDouble();

        CalculationData data = new CalculationData(alpha);
        Calculator calculator = new Calculator(data);

        calculator.calculate();

        System.out.println("\n--- Результати ---");
        System.out.println("Цiла частина: " + data.getIntegerValue());
        System.out.println("Макс послiдовнiсть 1: " + data.getMaxOnesSequence());
        System.out.println("Timestamp (до): " + data.getTimestamp());

        String fileName = "data.ser";

        // --- СЕРІАЛІЗАЦІЯ ---
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(data);
            System.out.println("\nОб'єкт збережено!");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // --- ДЕСЕРІАЛІЗАЦІЯ ---
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {

            CalculationData restored = (CalculationData) ois.readObject();

            System.out.println("\n--- Пiсля десерiалiзацiї ---");
            System.out.println("Цiла частина: " + restored.getIntegerValue());
            System.out.println("Макс 1: " + restored.getMaxOnesSequence());
            System.out.println("Timestamp (пiсля): " + restored.getTimestamp());

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        scanner.close();
    }
}
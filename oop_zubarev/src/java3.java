import java.io.*;
import java.util.*;

/** Лабораторна №3 */
public class java3 {

    /* ===================== DATA ===================== */

    /** Клас даних (результат обчислення) */
    static class CalculationData implements Serializable {

        private static final long serialVersionUID = 1L;

        private double alpha;
        private int integerValue;
        private int maxOnesSequence;

        public CalculationData(double alpha, int integerValue, int maxOnesSequence) {
            this.alpha = alpha;
            this.integerValue = integerValue;
            this.maxOnesSequence = maxOnesSequence;
        }

        public double getAlpha() { return alpha; }
        public int getIntegerValue() { return integerValue; }
        public int getMaxOnesSequence() { return maxOnesSequence; }

        @Override
        public String toString() {
            return String.format("alpha=%.2f | int=%d | max1=%d",
                    alpha, integerValue, maxOnesSequence);
        }
    }

    /* ===================== CALCULATOR ===================== */

    /** Клас обчислення */
    static class Calculator {

        public static CalculationData calculate(double alpha) {

            double x = 10 * Math.cos(alpha);
            double s = Math.pow(x, 2) + Math.pow(x, 3);

            int integerPart = (int) s;
            int maxSeq = findMaxOnesSequence(integerPart);

            return new CalculationData(alpha, integerPart, maxSeq);
        }

        private static int findMaxOnesSequence(int number) {

            String binary = Integer.toBinaryString(number);

            int max = 0, current = 0;

            for (char c : binary.toCharArray()) {
                if (c == '1') {
                    current++;
                    max = Math.max(max, current);
                } else {
                    current = 0;
                }
            }
            return max;
        }
    }

    /* ===================== FACTORY METHOD ===================== */

    /** Product */
    interface View {

        void viewHeader();
        void viewBody();
        void viewFooter();
        void viewShow();

        void viewInit();
        void viewSave() throws IOException;
        void viewRestore() throws Exception;
    }

    /** Creator */
    interface Viewable {
        View getView();
    }

    /** ConcreteCreator */
    static class ViewableResult implements Viewable {
        @Override
        public View getView() {
            return new ViewResult();
        }
    }

    /* ===================== VIEW RESULT ===================== */

    /** ConcreteProduct */
    static class ViewResult implements View {

        private static final String FILE_NAME = "data.bin";

        private ArrayList<CalculationData> list = new ArrayList<>();

        @Override
        public void viewInit() {
            list.clear();

            for (int i = 0; i < 5; i++) {
                double alpha = Math.random() * Math.PI;
                list.add(Calculator.calculate(alpha));
            }
        }

        @Override
        public void viewSave() throws IOException {
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(FILE_NAME));
            os.writeObject(list);
            os.close();
        }

        @Override
        public void viewRestore() throws Exception {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(FILE_NAME));
            list = (ArrayList<CalculationData>) is.readObject();
            is.close();
        }

        @Override
        public void viewHeader() {
            System.out.println("\n=== РЕЗУЛЬТАТИ ===");
        }

        @Override
        public void viewBody() {
            for (CalculationData d : list) {
                System.out.println(d);
            }
        }

        @Override
        public void viewFooter() {
            System.out.println("==================");
        }

        @Override
        public void viewShow() {
            viewHeader();
            viewBody();
            viewFooter();
        }
    }

    /* ===================== MAIN ===================== */

    public static void main(String[] args) throws Exception {

        View view = new ViewableResult().getView();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\nМеню:");
            System.out.println("1 - Згенерувати");
            System.out.println("2 - Показати");
            System.out.println("3 - Зберегти");
            System.out.println("4 - Вiдновити");
            System.out.println("0 - Вихiд");

            System.out.print("Введiть число: ");
            int cmd = sc.nextInt();

            switch (cmd) {
                case 1:
                    view.viewInit();
                    System.out.println("Данi згенеровано!");
                    break;
                case 2:
                    view.viewShow();
                    break;
                case 3:
                    view.viewSave();
                    System.out.println("Збережено!");
                    break;
                case 4:
                    view.viewRestore();
                    System.out.println("Вiдновлено!");
                    break;
                case 0:
                    System.out.println("Вихiд...");
                    return;
                default:
                    System.out.println("Невiрна команда!");
            }
        }
    }
}
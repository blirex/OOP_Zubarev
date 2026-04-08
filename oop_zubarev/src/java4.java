import java.io.*;
import java.util.*;

public class java4 {

    /* ===================== DATA ===================== */

    static class CalculationData implements Serializable {
        private static final long serialVersionUID = 1L;

        double alpha;
        int integerValue;
        int maxOnes;    

        public CalculationData(double a, int i, int m) {
            alpha = a;
            integerValue = i;
            maxOnes = m;
        }

        public double getAlpha() { return alpha; }
        public int getIntegerValue() { return integerValue; }
        public int getMaxOnes() { return maxOnes; }
    }

    /* ===================== CALCULATOR ===================== */

    static class Calculator {

        public static CalculationData calculate(double alpha) {

            double x = 10 * Math.cos(alpha);
            double s = Math.pow(x, 2) + Math.pow(x, 3);

            int intPart = (int) s;
            int max = maxOnes(intPart);

            return new CalculationData(alpha, intPart, max);
        }

        private static int maxOnes(int n) {
            String b = Integer.toBinaryString(n);
            int max = 0, cur = 0;

            for (char c : b.toCharArray()) {
                if (c == '1') {
                    cur++;
                    max = Math.max(max, cur);
                } else cur = 0;
            }
            return max;
        }
    }

    /* ===================== FACTORY ===================== */

    interface View {
        void viewInit();
        void viewShow();
        void viewSave() throws IOException;
        void viewRestore() throws Exception;
    }

    interface Viewable {
        View getView();
    }

    static class ViewableResult implements Viewable {
        public View getView() {
            return new ViewResult();
        }
    }

    /* ===================== BASE VIEW ===================== */

    static class ViewResult implements View {
        protected ArrayList<CalculationData> list = new ArrayList<>();
        protected static final String FILE = "data.bin";
        @Override
        public void viewInit() {
            list.clear();
            for (int i = 0; i < 5; i++) {
                list.add(Calculator.calculate(Math.random() * Math.PI));
            }
        }
        @Override
        public void viewShow() {
            System.out.println("\n=== РЕЗУЛЬТАТИ ===");
            for (CalculationData d : list) {
                System.out.println("alpha = " + d.alpha +
                        " | int = " + d.integerValue +
                        " | max1 = " + d.maxOnes);
            }
        }
        @Override
        public void viewSave() throws IOException {
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(FILE));
            os.writeObject(list);
            os.close();
        }
        @Override
        public void viewRestore() throws Exception {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(FILE));
            list = (ArrayList<CalculationData>) is.readObject();
            is.close();
        }
    }

    /* ===================== TABLE VIEW ===================== */

    static class ViewTable extends ViewResult {
        private int width = 30;
        public ViewTable() {}
        public ViewTable(int width) {
            this.width = width;
        }
        // overloading
        public void init(int width) {
            this.width = width;
            viewInit();
        }
        public void init(int width, double step) {
            this.width = width;
            init(step);
        }
        // overriding
        public void init(double step) {
            System.out.println("Інiціаiізацiя...");
            super.viewInit();
        }
        private void line() {
            for (int i = 0; i < width; i++) System.out.print("-");
            System.out.println();
        }
        @Override
        public void viewShow() {
            System.out.println("\nТАБЛИЦЯ:");
            line();
            System.out.printf("%-10s | %-10s | %-10s\n",
                    "alpha", "int", "max1");
            line();
            for (CalculationData d : list) {
                System.out.printf("%-10.2f | %-10d | %-10d\n",
                        d.alpha, d.integerValue, d.maxOnes);
            }
            line();
        }
    }

    /* ===================== TABLE FACTORY ===================== */

    static class ViewableTable extends ViewableResult {
        @Override
        public View getView() {
            return new ViewTable();
        }
    }

    /* ===================== MAIN ===================== */

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);

        System.out.println("1 - Звичайний вигляд");
        System.out.println("2 - Таблиця");
        System.out.print("Виберiть режим: ");

        int mode = sc.nextInt();

        View view;

        // поліморфізм
        if (mode == 1)
            view = new ViewableResult().getView();
        else
            view = new ViewableTable().getView();

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
                    return;
            }
        }
    }
}
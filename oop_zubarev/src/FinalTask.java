import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Stack;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.lang.annotation.*;

/* ===================== ANNOTATION ===================== */

@Retention(RetentionPolicy.RUNTIME)
@interface Info {
    String value();
}

/* ===================== MAIN ===================== */

public class FinalTask {

    /* ===================== DATA ===================== */

    static class CalculationData implements Serializable {
        double alpha;
        int integerValue;
        int maxOnes;

        public CalculationData(double a, int i, int m) {
            alpha = a;
            integerValue = i;
            maxOnes = m;
        }
    }

    /* ===================== OBSERVER ===================== */

    interface Observer {
        void update(ArrayList<CalculationData> data);
    }
    interface Observable {
        void addObserver(Observer o);
        void notifyObservers();
    }

    /* ===================== MODEL ===================== */

    @Info("Модель даних")
    static class DataModel implements Observable {

        ArrayList<CalculationData> list = new ArrayList<>();
        ArrayList<Observer> observers = new ArrayList<>();

        public void generate() {
            list.clear();
            for (int i = 0; i < 10; i++) {
                double a = Math.random() * Math.PI;
                double x = 10 * Math.cos(a);
                double s = Math.pow(x, 2) + Math.pow(x, 3);

                int intPart = (int) s;
                int max = maxOnes(intPart);

                list.add(new CalculationData(a, intPart, max));
            }
            notifyObservers();
        }

        public void sort() {
            list.sort(Comparator.comparingInt(d -> d.integerValue));
            notifyObservers();
        }

        public void notifyObservers() {
            for (Observer o : observers)
                o.update(list);
        }

        public void addObserver(Observer o) {
            observers.add(o);
        }

        private int maxOnes(int n) {
            String b = Integer.toBinaryString(n);
            int max = 0, cur = 0;
            for (char c : b.toCharArray()) {
                if (c == '1') { cur++; max = Math.max(max, cur); }
                else cur = 0;
            }
            return max;
        }
    }

    /* ===================== GRAPH ===================== */

    static class GraphPanel extends JPanel implements Observer {

        ArrayList<CalculationData> data = new ArrayList<>();

        public void update(ArrayList<CalculationData> data) {
            this.data = new ArrayList<>(data);
            repaint();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            setBackground(new Color(30,30,30));
            g.setColor(Color.GREEN);

            if (data.isEmpty()) return;

            int w = getWidth();
            int h = getHeight();

            g.drawLine(40, h - 40, w - 20, h - 40);
            g.drawLine(40, 20, 40, h - 40);

            int step = (w - 60) / data.size();

            int prevX = 0, prevY = 0;

            for (int i = 0; i < data.size(); i++) {
                int val = data.get(i).integerValue;

                int x = 40 + i * step;
                int y = h - 40 - val / 2;

                g.fillOval(x - 4, y - 4, 8, 8);

                if (i > 0)
                    g.drawLine(prevX, prevY, x, y);

                prevX = x;
                prevY = y;
            }
        }
    }

    /* ===================== TABLE ===================== */

    static class TablePanel extends JPanel implements Observer {

        JTable table;
        DefaultTableModel model;

        public TablePanel() {
            setLayout(new BorderLayout());

            model = new DefaultTableModel(
                    new Object[]{"alpha", "int", "max1"}, 0);

            table = new JTable(model);
            table.setBackground(new Color(50,50,50));
            table.setForeground(Color.WHITE);

            add(new JScrollPane(table), BorderLayout.CENTER);
        }

        public void update(ArrayList<CalculationData> data) {
            SwingUtilities.invokeLater(() -> {
                model.setRowCount(0);
                for (CalculationData d : data) {
                    model.addRow(new Object[]{
                            String.format("%.2f", d.alpha),
                            d.integerValue,
                            d.maxOnes
                    });
                }
            });
        }
    }

    /* ===================== COMMAND ===================== */

    interface Command {
        void execute();
        void undo();
    }

    static class CommandManager {

        private static CommandManager instance;
        private Stack<Command> history = new Stack<>();

        public static CommandManager getInstance() {
            if (instance == null)
                instance = new CommandManager();
            return instance;
        }

        public void executeCommand(Command c) {
            c.execute();
            history.push(c);
        }

        public void undo() {
            if (!history.isEmpty())
                history.pop().undo();
        }
    }

    static class InitCommand implements Command {

        DataModel model;
        ArrayList<CalculationData> backup;

        public InitCommand(DataModel m) { model = m; }

        public void execute() {
            backup = new ArrayList<>(model.list);
            model.generate();
        }

        public void undo() {
            model.list = backup;
            model.notifyObservers();
        }
    }

    static class ShowCommand implements Command {

        DataModel model;

        public ShowCommand(DataModel m) { model = m; }

        public void execute() {
            model.notifyObservers();
        }

        public void undo() {}
    }

    static class MacroCommand implements Command {

        java.util.List<Command> commands = new ArrayList<>();

        public void add(Command c) {
            commands.add(c);
        }

        public void execute() {
            for (Command c : commands) c.execute();
        }

        public void undo() {
            for (int i = commands.size()-1; i>=0; i--)
                commands.get(i).undo();
        }
    }

    /* ===================== PARALLEL ===================== */

    static class StatsTask implements Runnable {
        DataModel model;
        JTextArea output;
        public StatsTask(DataModel m, JTextArea out) {
            model = m;
            output = out;
        }
        public void run() {
            int min = model.list.stream().mapToInt(d -> d.integerValue).min().orElse(0);
            int max = model.list.stream().mapToInt(d -> d.integerValue).max().orElse(0);
            double avg = model.list.stream().mapToInt(d -> d.integerValue).average().orElse(0);
            SwingUtilities.invokeLater(() -> {
                output.setText("Статистика:\nMin = " + min +
                        "\nMax = " + max +
                        "\nAvg = " + avg);
            });
        }
    }
    static class Worker extends Thread {
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        public void addTask(Runnable r) { queue.add(r); }
        public void run() {
            while (true) {
                try { queue.take().run(); }
                catch (Exception e) { break; }
            }
        }
    }
    /* ===================== MAIN ===================== */

    public static void main(String[] args) {

        DataModel model = new DataModel();

        JFrame frame = new JFrame("FINAL TASK");
        frame.setSize(1200, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GraphPanel graph = new GraphPanel();
        TablePanel table = new TablePanel();
        JTextArea output = new JTextArea(5, 20);
        output.setBackground(new Color(20,20,20));
        output.setForeground(Color.WHITE);

        model.addObserver(graph);
        model.addObserver(table);

        CommandManager manager = CommandManager.getInstance();
        Worker worker = new Worker();
        worker.start();

        JButton gen = new JButton("1 Генерувати");
        JButton show = new JButton("2 Показати");
        JButton save = new JButton("3 Зберегти");
        JButton load = new JButton("4 Відновити");
        JButton undo = new JButton("5 Undo");
        JButton macro = new JButton("6 Макро");
        JButton stats = new JButton("7 Статистика");

        gen.addActionListener(e -> manager.executeCommand(new InitCommand(model)));
        show.addActionListener(e -> manager.executeCommand(new ShowCommand(model)));

        save.addActionListener(e -> {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("data.bin"))) {
                oos.writeObject(model.list);
                output.setText("Збережено");
            } catch (Exception ex) {}
        });

        load.addActionListener(e -> {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("data.bin"))) {
                model.list = (ArrayList<CalculationData>) ois.readObject();
                model.notifyObservers();
                output.setText("Відновлено");
            } catch (Exception ex) {}
        });

        undo.addActionListener(e -> manager.undo());

        macro.addActionListener(e -> {
            MacroCommand m = new MacroCommand();
            m.add(new InitCommand(model));
            m.add(new ShowCommand(model));
            manager.executeCommand(m);
        });

        stats.addActionListener(e -> worker.addTask(new StatsTask(model, output)));

        JPanel top = new JPanel();
        for (JButton b : new JButton[]{gen, show, save, load, undo, macro, stats}) {
            top.add(b);
        }

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, table, graph);
        split.setDividerLocation(550);

        frame.add(top, BorderLayout.NORTH);
        frame.add(split, BorderLayout.CENTER);
        frame.add(new JScrollPane(output), BorderLayout.SOUTH);

        frame.setVisible(true);

        if (model.getClass().isAnnotationPresent(Info.class)) {
            System.out.println(model.getClass().getAnnotation(Info.class).value());
        }
    }
}
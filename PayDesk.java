import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.print.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

// ---------- Model ----------
class Employee {
    String id, name, role;
    double salary;

    Employee(String id, String name, String role, double salary) {
        this.id = id; this.name = name; this.role = role; this.salary = salary;
    }

    double hra()       { return salary * 0.20; }
    double allowance() { return salary * 0.10; }
    double deduction() { return salary * 0.05; }
    double net()       { return salary + hra() + allowance() - deduction(); }
}

// ---------- Application ----------
public class PayDesk extends JFrame {
    static final Color NAVY = new Color(0x172644), BLUE = new Color(0x3867F5), BG = new Color(0xF6F8FC);
    static final Color LINE = new Color(0xE8EDF5), MUTED = new Color(0x7C879C), TEXT = new Color(0x202B43);
    static final Color GREEN = new Color(0x188A68), NAV_HOVER = new Color(0x2D4168);
    static final String[] ROLES = {"Software Engineer", "Backend Developer", "Frontend Developer", "HR Executive",
            "Data Analyst", "Project Manager", "Accountant", "Intern", "Other"};
    static final NumberFormat FMT = NumberFormat.getNumberInstance(new Locale("en", "IN"));

    List<Employee> employees = new ArrayList<>();
    List<Employee> visible = new ArrayList<>();
    String selectedId = "E001";
    boolean updatingCombo = false;

    CardLayout cards = new CardLayout();
    JPanel content = new JPanel(cards);
    JLabel pageTitle = new JLabel("Dashboard");
    Map<String, JButton> navButtons = new LinkedHashMap<>();

    JLabel lblCount = big(), lblBasic = big(), lblNet = big(), lblCountBadge = new JLabel();
    DefaultTableModel dashModel = model(), empModel = model();
    JTable empTable;
    JTextField search = new JTextField(18);
    JComboBox<String> payBox = new JComboBox<>();
    JPanel slip;
    JLabel sName = val(), sId = val(), sRole = val(), sBasic = val(), sHra = val(), sAllow = val(),
            sDed = val(), sTotDed = val(), sNet = new JLabel();

    public PayDesk() {
        super("PayDesk - Employee Payroll System");
        employees.add(new Employee("E001", "Riya Sharma", "Software Engineer", 25000));
        employees.add(new Employee("E002", "Aman Verma", "Backend Developer", 30000));
        employees.add(new Employee("E003", "Neha Singh", "Frontend Developer", 28000));
        employees.add(new Employee("E004", "Karan Patel", "HR Executive", 22000));
        employees.add(new Employee("E005", "Simran Kaur", "Data Analyst", 26000));

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG);
        main.add(buildTopbar(), BorderLayout.NORTH);
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(20, 28, 20, 28));
        content.add(buildDashboard(), "Dashboard");
        content.add(buildEmployees(), "Employees");
        content.add(buildPayroll(), "Payroll & Payslips");
        main.add(content, BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);

        payBox.addActionListener(e -> {
            if (updatingCombo || payBox.getSelectedItem() == null) return;
            selectedId = payBox.getSelectedItem().toString().split(" - ")[0];
            updateSlip();
        });
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
        });

        refresh();
        setSize(1100, 780);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
    }

    // ---------- helpers ----------
    static String money(double n) { return "\u20B9 " + FMT.format(Math.round(n * 100) / 100.0); }

    static Font f(int style, int size) { return new Font("SansSerif", style, size); }

    static JLabel big() { JLabel l = new JLabel(); l.setFont(f(Font.BOLD, 24)); l.setForeground(TEXT); return l; }

    static JLabel val() { JLabel l = new JLabel(); l.setFont(f(Font.BOLD, 13)); l.setForeground(TEXT); return l; }

    static JLabel text(String s, int style, int size, Color c) {
        JLabel l = new JLabel(s); l.setFont(f(style, size)); l.setForeground(c); return l;
    }

    static JButton button(String s, boolean primary) {
        JButton b = new JButton(s);
        b.setFont(f(Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(new EmptyBorder(10, 16, 10, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBackground(primary ? BLUE : new Color(0xF0F3F9));
        b.setForeground(primary ? Color.WHITE : new Color(0x526079));
        return b;
    }

    static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(LINE, 1, true), new EmptyBorder(16, 18, 16, 18)));
        return p;
    }

    static DefaultTableModel model() {
        return new DefaultTableModel(new String[]{"ID", "NAME", "DESIGNATION", "BASIC SALARY", "NET SALARY"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    static JTable table(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setRowHeight(38);
        t.setFont(f(Font.PLAIN, 13));
        t.setForeground(TEXT);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(0xF0F2F7));
        t.setSelectionBackground(new Color(0xEAF0FF));
        t.setSelectionForeground(TEXT);
        t.setFillsViewportHeight(true);
        t.getTableHeader().setFont(f(Font.BOLD, 11));
        t.getTableHeader().setBackground(new Color(0xFBFCFE));
        t.getTableHeader().setForeground(MUTED);
        t.getTableHeader().setReorderingAllowed(false);
        return t;
    }

    static JScrollPane scroll(JComponent c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(new LineBorder(LINE, 1, true));
        s.getViewport().setBackground(Color.WHITE);
        return s;
    }

    // ---------- layout ----------
    JPanel buildSidebar() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(NAVY);
        p.setPreferredSize(new Dimension(230, 0));
        p.setBorder(new EmptyBorder(26, 16, 18, 16));

        JLabel brand = text("  EP   PayDesk", Font.BOLD, 20, Color.WHITE);
        brand.setBorder(new EmptyBorder(0, 6, 36, 0));
        p.add(brand);
        JLabel ws = text("WORKSPACE", Font.BOLD, 10, new Color(0x9AA9C6));
        ws.setBorder(new EmptyBorder(0, 10, 12, 0));
        p.add(ws);

        for (String name : new String[]{"Dashboard", "Employees", "Payroll & Payslips"}) {
            JButton b = new JButton(name);
            b.setFont(f(Font.PLAIN, 14));
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setBorder(new EmptyBorder(0, 14, 0, 0));
            b.setFocusPainted(false);
            b.setOpaque(true);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> show(name));
            navButtons.put(name, b);
            p.add(b);
            p.add(Box.createVerticalStrut(6));
        }
        p.add(Box.createVerticalGlue());
        JLabel admin = text("Admin User  \u2022  Administrator", Font.PLAIN, 12, new Color(0x9AA9C6));
        admin.setBorder(new EmptyBorder(14, 8, 0, 0));
        p.add(admin);
        return p;
    }

    JPanel buildTopbar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, LINE), new EmptyBorder(16, 28, 16, 28)));
        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.add(text("PAYROLL MANAGEMENT", Font.BOLD, 10, MUTED));
        pageTitle.setFont(f(Font.BOLD, 24));
        pageTitle.setForeground(TEXT);
        left.add(pageTitle);
        p.add(left, BorderLayout.WEST);
        p.add(text("\u25CF System ready", Font.PLAIN, 12, GREEN), BorderLayout.EAST);
        return p;
    }

    JPanel statCard(String label, JLabel value, String sub) {
        JPanel c = card();
        c.setLayout(new GridLayout(3, 1, 0, 4));
        c.add(text(label, Font.PLAIN, 12, MUTED));
        c.add(value);
        c.add(text(sub, Font.PLAIN, 11, new Color(0xA0A8B8)));
        return c;
    }

    JPanel buildDashboard() {
        JPanel page = new JPanel(new BorderLayout(0, 18));
        page.setOpaque(false);

        JPanel hero = new JPanel(new BorderLayout());
        hero.setBackground(new Color(0xEAF0FF));
        hero.setBorder(new CompoundBorder(new LineBorder(new Color(0xDCE5FF), 1, true), new EmptyBorder(24, 30, 24, 30)));
        JPanel txt = new JPanel(new GridLayout(4, 1, 0, 6));
        txt.setOpaque(false);
        txt.add(text("PAYROLL OVERVIEW", Font.BOLD, 10, new Color(0x4164D5)));
        txt.add(text("People first. Payroll made simple.", Font.BOLD, 24, new Color(0x19294A)));
        txt.add(text("Manage employee records and calculate monthly salaries in one place.", Font.PLAIN, 13, MUTED));
        JButton add = button("+ Add an employee", true);
        add.addActionListener(e -> { show("Employees"); employeeDialog(null); });
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnWrap.setOpaque(false);
        btnWrap.add(add);
        txt.add(btnWrap);
        hero.add(txt, BorderLayout.CENTER);

        JPanel stats = new JPanel(new GridLayout(1, 3, 16, 0));
        stats.setOpaque(false);
        stats.add(statCard("Total employees", lblCount, "People on your team"));
        stats.add(statCard("Basic salary total", lblBasic, "Before additions & deductions"));
        stats.add(statCard("Estimated net payroll", lblNet, "Based on current pay rules"));

        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);
        top.add(hero, BorderLayout.NORTH);
        top.add(stats, BorderLayout.CENTER);
        page.add(top, BorderLayout.NORTH);

        JPanel dir = new JPanel(new BorderLayout(0, 8));
        dir.setOpaque(false);
        dir.add(text("Team directory", Font.BOLD, 16, TEXT), BorderLayout.NORTH);
        dir.add(scroll(table(dashModel)), BorderLayout.CENTER);
        page.add(dir, BorderLayout.CENTER);
        return page;
    }

    JPanel buildEmployees() {
        JPanel page = new JPanel(new BorderLayout(0, 12));
        page.setOpaque(false);

        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(text("All employees", Font.BOLD, 16, TEXT));
        lblCountBadge.setFont(f(Font.BOLD, 11));
        lblCountBadge.setForeground(new Color(0x4A66DB));
        left.add(lblCountBadge);
        bar.add(left, BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(text("Search:", Font.PLAIN, 12, MUTED));
        search.setFont(f(Font.PLAIN, 13));
        right.add(search);
        bar.add(right, BorderLayout.EAST);
        page.add(bar, BorderLayout.NORTH);

        empTable = table(empModel);
        page.add(scroll(empTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        JButton add = button("+ Add employee", true), edit = button("Edit", false),
                del = button("Delete", false), slipBtn = button("View payslip", false);
        add.addActionListener(e -> employeeDialog(null));
        edit.addActionListener(e -> { Employee s = selected(); if (s != null) employeeDialog(s); });
        del.addActionListener(e -> deleteSelected());
        slipBtn.addActionListener(e -> {
            Employee s = selected();
            if (s != null) { selectedId = s.id; refresh(); show("Payroll & Payslips"); }
        });
        actions.add(add); actions.add(edit); actions.add(del); actions.add(slipBtn);
        page.add(actions, BorderLayout.SOUTH);
        return page;
    }

    JPanel buildPayroll() {
        JPanel page = new JPanel(new BorderLayout(18, 0));
        page.setOpaque(false);

        JPanel ctrl = card();
        ctrl.setLayout(new BoxLayout(ctrl, BoxLayout.Y_AXIS));
        ctrl.setPreferredSize(new Dimension(270, 0));
        JLabel l1 = text("SELECT EMPLOYEE", Font.BOLD, 10, MUTED);
        l1.setAlignmentX(Component.LEFT_ALIGNMENT);
        ctrl.add(l1);
        ctrl.add(Box.createVerticalStrut(8));
        payBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        payBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        ctrl.add(payBox);
        ctrl.add(Box.createVerticalStrut(18));
        JLabel rules = new JLabel("<html><b>Current calculation rules</b><br><br>HRA: 20% of basic<br>Allowance: 10% of basic"
                + "<br>Deduction: 5% of basic<br><br><small>Sample rates for this academic project.</small></html>");
        rules.setFont(f(Font.PLAIN, 12));
        rules.setForeground(new Color(0x46536C));
        rules.setAlignmentX(Component.LEFT_ALIGNMENT);
        ctrl.add(rules);
        ctrl.add(Box.createVerticalStrut(18));
        JButton print = button("Print / Save payslip", true);
        print.setAlignmentX(Component.LEFT_ALIGNMENT);
        print.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        print.addActionListener(e -> printSlip());
        ctrl.add(print);
        page.add(ctrl, BorderLayout.WEST);

        slip = card();
        slip.setLayout(new BoxLayout(slip, BoxLayout.Y_AXIS));
        slip.setBorder(new CompoundBorder(new LineBorder(new Color(0xE3E8F1), 1, true), new EmptyBorder(24, 28, 24, 28)));
        addLeft(slip, text("PayDesk  \u2022  SALARY SLIP", Font.BOLD, 16, BLUE));
        slip.add(Box.createVerticalStrut(6));
        addLeft(slip, text("Monthly payroll  \u2022  " + new java.text.SimpleDateFormat("MMMM yyyy").format(new Date()),
                Font.PLAIN, 12, MUTED));
        slip.add(Box.createVerticalStrut(18));
        slip.add(slipRow("Employee name", sName));
        slip.add(slipRow("Employee ID", sId));
        slip.add(slipRow("Designation", sRole));
        slip.add(Box.createVerticalStrut(16));
        addLeft(slip, text("EARNINGS", Font.BOLD, 10, MUTED));
        slip.add(slipRow("Basic salary", sBasic));
        slip.add(slipRow("HRA (20%)", sHra));
        slip.add(slipRow("Allowance (10%)", sAllow));
        slip.add(Box.createVerticalStrut(12));
        addLeft(slip, text("DEDUCTIONS", Font.BOLD, 10, MUTED));
        slip.add(slipRow("Other deduction (5%)", sDed));
        slip.add(slipRow("Total deductions", sTotDed));
        slip.add(Box.createVerticalStrut(16));
        JPanel net = new JPanel(new BorderLayout());
        net.setBackground(new Color(0xEDF2FF));
        net.setBorder(new EmptyBorder(14, 16, 14, 16));
        net.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        net.setAlignmentX(Component.LEFT_ALIGNMENT);
        net.add(text("NET SALARY PAYABLE", Font.BOLD, 11, new Color(0x7283B2)), BorderLayout.WEST);
        sNet.setFont(f(Font.BOLD, 22));
        sNet.setForeground(new Color(0x2D4FC5));
        net.add(sNet, BorderLayout.EAST);
        slip.add(net);
        slip.add(Box.createVerticalGlue());
        addLeft(slip, text("Computer-generated salary statement for demonstration purposes.", Font.PLAIN, 10, MUTED));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(slip, BorderLayout.CENTER);
        page.add(wrap, BorderLayout.CENTER);
        return page;
    }

    static void addLeft(JPanel p, JComponent c) { c.setAlignmentX(Component.LEFT_ALIGNMENT); p.add(c); }

    static JPanel slipRow(String label, JLabel value) {
        JPanel r = new JPanel(new BorderLayout());
        r.setOpaque(false);
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        r.setAlignmentX(Component.LEFT_ALIGNMENT);
        r.add(text(label, Font.PLAIN, 12, MUTED), BorderLayout.WEST);
        r.add(value, BorderLayout.EAST);
        return r;
    }

    // ---------- behaviour ----------
    void show(String name) {
        cards.show(content, name);
        pageTitle.setText(name);
        for (Map.Entry<String, JButton> en : navButtons.entrySet()) {
            boolean on = en.getKey().equals(name);
            en.getValue().setBackground(on ? NAV_HOVER : NAVY);
            en.getValue().setForeground(on ? Color.WHITE : new Color(0xBDC8DC));
        }
        refresh();
    }

    Employee find(String id) {
        for (Employee e : employees) if (e.id.equalsIgnoreCase(id)) return e;
        return null;
    }

    Employee selected() {
        int row = empTable.getSelectedRow();
        if (row < 0 || row >= visible.size()) {
            JOptionPane.showMessageDialog(this, "Please select an employee from the table first.");
            return null;
        }
        return visible.get(row);
    }

    void deleteSelected() {
        Employee e = selected();
        if (e == null) return;
        int ok = JOptionPane.showConfirmDialog(this, "Delete " + e.name + " (" + e.id + ")? This cannot be undone.",
                "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) { employees.remove(e); refresh(); }
    }

    void employeeDialog(Employee old) {
        JTextField id = new JTextField(old == null ? "" : old.id, 15);
        JTextField name = new JTextField(old == null ? "" : old.name);
        JComboBox<String> role = new JComboBox<>(ROLES);
        if (old != null) role.setSelectedItem(old.role);
        JTextField sal = new JTextField(old == null ? "" : String.valueOf(old.salary));
        id.setEnabled(old == null);

        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.add(new JLabel("Employee ID")); p.add(id);
        p.add(new JLabel("Full name")); p.add(name);
        p.add(new JLabel("Designation")); p.add(role);
        p.add(new JLabel("Basic salary (\u20B9)")); p.add(sal);

        while (true) {
            int r = JOptionPane.showConfirmDialog(this, p, old == null ? "Add a new employee" : "Edit employee details",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) return;
            String i = id.getText().trim(), n = name.getText().trim();
            double s;
            try { s = Double.parseDouble(sal.getText().trim()); } catch (NumberFormatException ex) { s = -1; }
            if (i.isEmpty() || n.isEmpty() || s < 0) {
                JOptionPane.showMessageDialog(this, "Please enter valid employee details.");
                continue;
            }
            if (old == null && find(i) != null) {
                JOptionPane.showMessageDialog(this, "That employee ID already exists.");
                continue;
            }
            if (old == null) {
                employees.add(new Employee(i, n, role.getSelectedItem().toString(), s));
                selectedId = i;
            } else {
                old.name = n; old.role = role.getSelectedItem().toString(); old.salary = s;
            }
            refresh();
            return;
        }
    }

    void refresh() {
        double basic = 0, net = 0;
        for (Employee e : employees) { basic += e.salary; net += e.net(); }
        lblCount.setText(String.valueOf(employees.size()));
        lblBasic.setText(money(basic));
        lblNet.setText(money(net));
        lblCountBadge.setText(" " + employees.size() + " ");

        dashModel.setRowCount(0);
        for (int i = 0; i < Math.min(5, employees.size()); i++) addRow(dashModel, employees.get(i));

        String q = search.getText().trim().toLowerCase();
        empModel.setRowCount(0);
        visible.clear();
        for (Employee e : employees)
            if ((e.id + " " + e.name + " " + e.role).toLowerCase().contains(q)) { visible.add(e); addRow(empModel, e); }

        updatingCombo = true;
        payBox.removeAllItems();
        for (Employee e : employees) {
            payBox.addItem(e.id + " - " + e.name);
            if (e.id.equals(selectedId)) payBox.setSelectedIndex(payBox.getItemCount() - 1);
        }
        updatingCombo = false;
        if (find(selectedId) == null && !employees.isEmpty()) {
            selectedId = employees.get(0).id;
            payBox.setSelectedIndex(0);
        }
        updateSlip();
    }

    static void addRow(DefaultTableModel m, Employee e) {
        m.addRow(new Object[]{e.id, e.name, e.role, money(e.salary), money(e.net())});
    }

    void updateSlip() {
        Employee e = find(selectedId);
        if (e == null) {
            for (JLabel l : new JLabel[]{sName, sId, sRole}) l.setText("-");
            for (JLabel l : new JLabel[]{sBasic, sHra, sAllow, sDed, sTotDed, sNet}) l.setText(money(0));
            return;
        }
        sName.setText(e.name); sId.setText(e.id); sRole.setText(e.role);
        sBasic.setText(money(e.salary)); sHra.setText(money(e.hra())); sAllow.setText(money(e.allowance()));
        sDed.setText(money(e.deduction())); sTotDed.setText(money(e.deduction())); sNet.setText(money(e.net()));
    }

    void printSlip() {
        if (find(selectedId) == null) return;
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable((g, pf, page) -> {
            if (page > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) g;
            g2.translate(pf.getImageableX(), pf.getImageableY());
            double scale = Math.min(1.0, pf.getImageableWidth() / slip.getWidth());
            g2.scale(scale, scale);
            slip.printAll(g2);
            return Printable.PAGE_EXISTS;
        });
        if (job.printDialog()) {
            try { job.print(); } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Printing failed: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PayDesk app = new PayDesk();
            app.show("Dashboard");
            app.setVisible(true);
        });
    }
}

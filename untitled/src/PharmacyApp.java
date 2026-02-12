import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.math.BigDecimal;

public class PharmacyApp extends JFrame {
    // Поля введення
    private JTextField txtType, txtBrand, txtManufacturer, txtDate, txtSupplier, txtPrice;
    private JTable table;
    private DefaultTableModel tableModel;

    public PharmacyApp() {
        setTitle("Облік медикаментів (Варіант 21)");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- Панель введення даних ---
        JPanel inputPanel = new JPanel(new GridLayout(7, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Дані про препарат"));

        inputPanel.add(new JLabel("Вид (Таблетки/Сироп...):"));
        txtType = new JTextField();
        inputPanel.add(txtType);

        inputPanel.add(new JLabel("Марка (Назва):"));
        txtBrand = new JTextField();
        inputPanel.add(txtBrand);

        inputPanel.add(new JLabel("Виробник:"));
        txtManufacturer = new JTextField();
        inputPanel.add(txtManufacturer);

        inputPanel.add(new JLabel("Термін (РРРР-ММ-ДД):"));
        txtDate = new JTextField();
        txtDate.setToolTipText("Формат: 2025-12-31");
        inputPanel.add(txtDate);

        inputPanel.add(new JLabel("Постачальник:"));
        txtSupplier = new JTextField();
        inputPanel.add(txtSupplier);

        inputPanel.add(new JLabel("Ціна (грн):"));
        txtPrice = new JTextField();
        inputPanel.add(txtPrice);

        JButton btnAdd = new JButton("Додати товар");
        inputPanel.add(btnAdd);

        JPanel leftContainer = new JPanel(new BorderLayout());
        leftContainer.add(inputPanel, BorderLayout.NORTH);
        add(leftContainer, BorderLayout.WEST);

        // --- Таблиця ---
        tableModel = new DefaultTableModel(new String[]{
                "ID", "Вид", "Назва", "Виробник", "Придатний до", "Постачальник", "Ціна"
        }, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- Кнопка Додати ---
        btnAdd.addActionListener(e -> {
            if (txtBrand.getText().isEmpty() || txtPrice.getText().isEmpty() || txtDate.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Помилка: Назва, Дата та Ціна є обов'язковими!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            saveToMySQL();
            loadData();
        });

        loadData();
    }

    private Connection connect() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "КРИТИЧНА ПОМИЛКА: Бібліотека MySQL не підключена до проекту!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return null;
        }

        // 1. НАЗВА БАЗИ ДАНИХ: farmacy
        String url = "jdbc:mysql://127.0.0.1:3306/farmacy";

        String user = "root";
        // 2. ВАШ ПАРОЛЬ
        String pass = "35122512q";

        return DriverManager.getConnection(url, user, pass);
    }

    private void saveToMySQL() {
        // 3. НАЗВА ТАБЛИЦІ: farmacy_db
        String sql = "INSERT INTO farmacy_db (medicine_type, brand_name, manufacturer, expiration_date, supplier, price) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = connect(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, txtType.getText());
            pstmt.setString(2, txtBrand.getText());
            pstmt.setString(3, txtManufacturer.getText());

            try {
                pstmt.setDate(4, Date.valueOf(txtDate.getText()));
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, "Невірний формат дати! Використовуйте РРРР-ММ-ДД (напр. 2026-05-20)");
                return;
            }
            pstmt.setString(5, txtSupplier.getText());
            pstmt.setBigDecimal(6, new BigDecimal(txtPrice.getText()));

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Препарат додано успішно!");

            txtBrand.setText("");
            txtPrice.setText("");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Помилка БД: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);

        // 4. НАЗВА ТАБЛИЦІ: farmacy_db
        try (Connection con = connect(); Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM farmacy_db")) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("medicine_type"),
                        rs.getString("brand_name"),
                        rs.getString("manufacturer"),
                        rs.getDate("expiration_date"),
                        rs.getString("supplier"),
                        rs.getBigDecimal("price")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Не вдалося завантажити дані: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PharmacyApp().setVisible(true));
    }
}
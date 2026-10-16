package com.oasis.reservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;

/**
 * OASIS INFOBYTE - Java Development
 * Task: Online Reservation System
 * Desktop GUI built with Swing and SQLite/JDBC.
 */
public class OnlineReservationApp {
    private static final String DB_URL = "jdbc:sqlite:reservations.db";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                Database.initialize();
                new LoginFrame().setVisible(true);
            } catch (Exception e) {
                showError(null, "Could not start application: " + e.getMessage());
            }
        });
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Online Reservation System", JOptionPane.INFORMATION_MESSAGE);
    }

    private static class Database {
        static void initialize() throws SQLException {
            try (Connection connection = connect(); Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL UNIQUE, " +
                        "password_hash TEXT NOT NULL)");
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS reservations (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, pnr TEXT NOT NULL UNIQUE, user_id INTEGER NOT NULL, " +
                        "passenger_name TEXT NOT NULL, train_number TEXT NOT NULL, train_name TEXT NOT NULL, " +
                        "journey_date TEXT NOT NULL, source_station TEXT NOT NULL, destination_station TEXT NOT NULL, " +
                        "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY(user_id) REFERENCES users(id))");
            }
        }
    }

    private static class User {
        final int id;
        final String username;
        User(int id, String username) { this.id = id; this.username = username; }
    }

    private static class AuthService {
        static User login(String username, String password) throws SQLException {
            String sql = "SELECT id, username FROM users WHERE username = ? AND password_hash = ?";
            try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, username.trim());
                ps.setString(2, password); // Demo project: plain-text password; use a password hash in production.
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? new User(rs.getInt("id"), rs.getString("username")) : null;
                }
            }
        }

        static void register(String username, String password) throws SQLException {
            if (username.trim().length() < 3) throw new IllegalArgumentException("Username must have at least 3 characters.");
            if (password.length() < 4) throw new IllegalArgumentException("Password must have at least 4 characters.");
            String sql = "INSERT INTO users(username, password_hash) VALUES(?, ?)";
            try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, username.trim());
                ps.setString(2, password);
                ps.executeUpdate();
            }
        }
    }

    private static class LoginFrame extends JFrame {
        private final JTextField username = new JTextField(18);
        private final JPasswordField password = new JPasswordField(18);

        LoginFrame() {
            setTitle("Online Reservation System - Login");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(440, 300);
            setLocationRelativeTo(null);
            JPanel root = new JPanel(new GridBagLayout());
            root.setBorder(new EmptyBorder(20, 28, 20, 28));
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(7, 7, 7, 7);
            g.fill = GridBagConstraints.HORIZONTAL;
            g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
            JLabel heading = new JLabel("ONLINE RESERVATION SYSTEM", SwingConstants.CENTER);
            heading.setFont(heading.getFont().deriveFont(Font.BOLD, 18f));
            root.add(heading, g);
            g.gridwidth = 1; g.gridy++;
            root.add(new JLabel("Username:"), g); g.gridx = 1; root.add(username, g);
            g.gridx = 0; g.gridy++; root.add(new JLabel("Password:"), g); g.gridx = 1; root.add(password, g);
            JButton login = new JButton("Login");
            JButton register = new JButton("Create account");
            g.gridx = 0; g.gridy++; root.add(login, g); g.gridx = 1; root.add(register, g);
            login.addActionListener(e -> doLogin());
            register.addActionListener(e -> doRegister());
            getRootPane().setDefaultButton(login);
            setContentPane(root);
        }

        private void doLogin() {
            String name = username.getText().trim();
            String pass = new String(password.getPassword());
            if (name.isEmpty() || pass.isEmpty()) { showError(this, "Enter both username and password."); return; }
            try {
                User user = AuthService.login(name, pass);
                if (user == null) { showError(this, "Invalid login. Check your details or create an account."); return; }
                dispose();
                new DashboardFrame(user).setVisible(true);
            } catch (SQLException ex) { showError(this, "Login failed: " + ex.getMessage()); }
        }

        private void doRegister() {
            String name = username.getText().trim();
            String pass = new String(password.getPassword());
            if (name.isEmpty() || pass.isEmpty()) { showError(this, "Enter a username and password to register."); return; }
            try {
                AuthService.register(name, pass);
                showInfo(this, "Account created. You can now log in.");
            } catch (SQLException ex) {
                if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("unique"))
                    showError(this, "That username already exists. Choose another one.");
                else showError(this, "Registration failed: " + ex.getMessage());
            } catch (IllegalArgumentException ex) { showError(this, ex.getMessage()); }
        }
    }

    private static class DashboardFrame extends JFrame {
        private final User user;
        private final JTextField passengerName = new JTextField(18);
        private final JTextField trainNumber = new JTextField(18);
        private final JTextField trainName = new JTextField(18);
        private final JTextField journeyDate = new JTextField(LocalDate.now().plusDays(1).toString(), 18);
        private final JTextField source = new JTextField(18);
        private final JTextField destination = new JTextField(18);
        private final DefaultTableModel tableModel = new DefaultTableModel(
                new String[]{"PNR", "Passenger", "Train No.", "Train Name", "Date", "From", "To"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        private final JTable reservationsTable = new JTable(tableModel);

        DashboardFrame(User user) {
            this.user = user;
            setTitle("Online Reservation System - " + user.username);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(950, 600);
            setLocationRelativeTo(null);
            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab("Book Reservation", buildBookingPanel());
            tabs.addTab("My Reservations / Cancel", buildReservationsPanel());
            JPanel top = new JPanel(new BorderLayout());
            top.setBorder(new EmptyBorder(8, 12, 8, 12));
            top.add(new JLabel("Logged in as: " + user.username), BorderLayout.WEST);
            JButton logout = new JButton("Logout");
            logout.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });
            top.add(logout, BorderLayout.EAST);
            setLayout(new BorderLayout());
            add(top, BorderLayout.NORTH);
            add(tabs, BorderLayout.CENTER);
            tabs.addChangeListener(e -> refreshReservations());
            refreshReservations();
        }

        private JPanel buildBookingPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(new EmptyBorder(18, 24, 18, 24));
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(7, 8, 7, 8); g.fill = GridBagConstraints.HORIZONTAL;
            JLabel note = new JLabel("Enter the journey details. Date format: YYYY-MM-DD");
            g.gridx = 0; g.gridy = 0; g.gridwidth = 2; panel.add(note, g);
            g.gridwidth = 1;
            addField(panel, g, 1, "Passenger name:", passengerName);
            addField(panel, g, 2, "Train number:", trainNumber);
            addField(panel, g, 3, "Train name:", trainName);
            addField(panel, g, 4, "Journey date:", journeyDate);
            addField(panel, g, 5, "From station:", source);
            addField(panel, g, 6, "To station:", destination);
            JButton book = new JButton("Book Reservation");
            g.gridx = 1; g.gridy = 7; panel.add(book, g);
            book.addActionListener(e -> bookReservation());
            return panel;
        }

        private void addField(JPanel panel, GridBagConstraints g, int row, String label, JTextField field) {
            g.gridx = 0; g.gridy = row; panel.add(new JLabel(label), g);
            g.gridx = 1; panel.add(field, g);
        }

        private JPanel buildReservationsPanel() {
            JPanel panel = new JPanel(new BorderLayout(8, 8));
            panel.setBorder(new EmptyBorder(12, 12, 12, 12));
            reservationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            panel.add(new JScrollPane(reservationsTable), BorderLayout.CENTER);
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton refresh = new JButton("Refresh");
            JButton cancel = new JButton("Cancel Selected Reservation");
            buttons.add(refresh); buttons.add(cancel);
            refresh.addActionListener(e -> refreshReservations());
            cancel.addActionListener(e -> cancelSelectedReservation());
            panel.add(buttons, BorderLayout.SOUTH);
            return panel;
        }

        private void bookReservation() {
            String passenger = passengerName.getText().trim();
            String number = trainNumber.getText().trim();
            String name = trainName.getText().trim();
            String dateText = journeyDate.getText().trim();
            String from = source.getText().trim();
            String to = destination.getText().trim();
            if (passenger.isEmpty() || number.isEmpty() || name.isEmpty() || dateText.isEmpty() || from.isEmpty() || to.isEmpty()) {
                showError(this, "Please fill in every field."); return;
            }
            LocalDate date;
            try { date = LocalDate.parse(dateText); }
            catch (DateTimeParseException ex) { showError(this, "Invalid date. Use YYYY-MM-DD, for example 2026-12-25."); return; }
            if (date.isBefore(LocalDate.now())) { showError(this, "Journey date cannot be in the past."); return; }
            if (from.equalsIgnoreCase(to)) { showError(this, "Source and destination must be different."); return; }

            String pnr = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
            String sql = "INSERT INTO reservations(pnr, user_id, passenger_name, train_number, train_name, journey_date, source_station, destination_station) VALUES(?,?,?,?,?,?,?,?)";
            try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, pnr); ps.setInt(2, user.id); ps.setString(3, passenger);
                ps.setString(4, number); ps.setString(5, name); ps.setString(6, date.toString());
                ps.setString(7, from); ps.setString(8, to);
                ps.executeUpdate();
                showInfo(this, "Reservation booked successfully!\nYour PNR is: " + pnr + "\nKeep this PNR for your records.");
                passengerName.setText(""); trainNumber.setText(""); trainName.setText("");
                source.setText(""); destination.setText(""); journeyDate.setText(LocalDate.now().plusDays(1).toString());
                refreshReservations();
            } catch (SQLException ex) { showError(this, "Could not save reservation: " + ex.getMessage()); }
        }

        private void refreshReservations() {
            tableModel.setRowCount(0);
            String sql = "SELECT pnr, passenger_name, train_number, train_name, journey_date, source_station, destination_station " +
                    "FROM reservations WHERE user_id = ? ORDER BY journey_date";
            try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, user.id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) tableModel.addRow(new Object[]{rs.getString("pnr"), rs.getString("passenger_name"),
                            rs.getString("train_number"), rs.getString("train_name"), rs.getString("journey_date"),
                            rs.getString("source_station"), rs.getString("destination_station")});
                }
            } catch (SQLException ex) { showError(this, "Could not load reservations: " + ex.getMessage()); }
        }

        private void cancelSelectedReservation() {
            int row = reservationsTable.getSelectedRow();
            if (row < 0) { showError(this, "Select a reservation from the table first."); return; }
            String pnr = tableModel.getValueAt(row, 0).toString();
            int answer = JOptionPane.showConfirmDialog(this, "Cancel reservation with PNR " + pnr + "?",
                    "Confirm cancellation", JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) return;
            try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM reservations WHERE pnr = ? AND user_id = ?")) {
                ps.setString(1, pnr); ps.setInt(2, user.id);
                int changed = ps.executeUpdate();
                if (changed > 0) showInfo(this, "Reservation " + pnr + " has been cancelled.");
                else showError(this, "Reservation was not found or does not belong to your account.");
                refreshReservations();
            } catch (SQLException ex) { showError(this, "Could not cancel reservation: " + ex.getMessage()); }
        }
    }
}

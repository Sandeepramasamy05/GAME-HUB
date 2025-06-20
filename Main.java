import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import java.util.regex.Pattern;
import java.util.random.*;//random api generation package

public class Main {
    public static void main(String[] args) {
        DatabaseManager.initializeDatabase();
        Game.panel_game();
        Game.panel_introduction();
    }
}
  
class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/rps";
    private static final String DB_USER = "root"; 
    private static final String DB_PASSWORD = "sandeep05"; 
    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            
            stmt.execute("CREATE DATABASE IF NOT EXISTS rps_game");
            stmt.execute("USE rps_game");
            
            
            String createTableSQL = "CREATE TABLE IF NOT EXISTS game_logs (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "human_choice VARCHAR(10) NOT NULL," +
                    "computer_choice VARCHAR(10) NOT NULL," +
                    "result VARCHAR(20) NOT NULL," +
                    "play_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
            stmt.execute(createTableSQL);
            
            
            createTableSQL = "CREATE TABLE IF NOT EXISTS game_stats (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "total_games INT DEFAULT 0," +
                    "human_wins INT DEFAULT 0," +
                    "computer_wins INT DEFAULT 0," +
                    "ties INT DEFAULT 0)";
            stmt.execute(createTableSQL);
            
            
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM game_stats");
            rs.next();
            if (rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO game_stats (total_games, human_wins, computer_wins, ties) VALUES (0, 0, 0, 0)");
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "Database initialization failed: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void logGame(String humanChoice, String computerChoice, String result) {
        String sql = "INSERT INTO game_logs (human_choice, computer_choice, result) VALUES (?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, humanChoice);
            pstmt.setString(2, computerChoice);
            pstmt.setString(3, result);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error logging game: " + e.getMessage());
        }
    }
    
    public static void updateStats(int humanWin, int computerWin, int tie) {
        String sql = "UPDATE game_stats SET " +
                     "total_games = total_games + 1, " +
                     "human_wins = human_wins + ?, " +
                     "computer_wins = computer_wins + ?, " +
                     "ties = ties + ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, humanWin);
            pstmt.setInt(2, computerWin);
            pstmt.setInt(3, tie);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error updating stats: " + e.getMessage());
        }
    }
    
    public static void resetStats() {
        String sql = "UPDATE game_stats SET total_games = 0, human_wins = 0, computer_wins = 0, ties = 0";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(sql);
            
        } catch (SQLException e) {
            System.err.println("Error resetting stats: " + e.getMessage());
        }
    }
    
    public static String getGameHistory() {
        StringBuilder history = new StringBuilder();
        String sql = "SELECT * FROM game_logs ORDER BY play_date DESC LIMIT 10";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            history.append("Last 10 Games:\n");
            history.append("Date\t\tHuman\tComputer\tResult\n");
            history.append("------------------------------------------------\n");
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm");
            
            while (rs.next()) {
                Date date = rs.getTimestamp("play_date");
                String human = rs.getString("human_choice");
                String computer = rs.getString("computer_choice");
                String result = rs.getString("result");
                
                history.append(dateFormat.format(date)).append("\t")
                       .append(human).append("\t")
                       .append(computer).append("\t\t")
                       .append(result).append("\n");
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching history: " + e.getMessage());
            return "Error loading game history";
        }
        
        return history.toString();
    }
    
    public static String getStats() {
        String sql = "SELECT * FROM game_stats";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return "Game Statistics:\n" +
                       "Total Games: " + rs.getInt("total_games") + "\n" +
                       "Human Wins: " + rs.getInt("human_wins") + "\n" +
                       "Computer Wins: " + rs.getInt("computer_wins") + "\n" +
                       "Ties: " + rs.getInt("ties");
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching stats: " + e.getMessage());
        }
        
        return "Error loading statistics";
    }
   
    public static boolean storeFeedback(String name, String dob, String email, String phone,
                                     String rating, String frequency,
                                     String recommend, String likedMost, String improvements) {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS feedback (" +
                              "id INT AUTO_INCREMENT PRIMARY KEY," +
                              "name VARCHAR(100) NOT NULL," +
                              "dob DATE NOT NULL," +
                              "email VARCHAR(100)," +
                              "phone VARCHAR(15) NOT NULL," +
                              "rating VARCHAR(10) NOT NULL," +
                              "frequency VARCHAR(20)," +
                              "recommendation VARCHAR(20)," +
                              "liked_most TEXT," +
                              "improvements TEXT," +
                              "submission_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        String insertSQL = "INSERT INTO feedback (name, dob, email, phone, rating, " +
                         "frequency, recommendation, liked_most, improvements) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            
            stmt.execute(createTableSQL);
            
            pstmt.setString(1, name);
            
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = inputFormat.parse(dob);
            pstmt.setString(2, outputFormat.format(date));
            
            pstmt.setString(3, email.isEmpty() ? null : email);
            pstmt.setString(4, phone);
            pstmt.setString(5, rating);
            pstmt.setString(6, frequency);
            pstmt.setString(7, recommend);
            pstmt.setString(8, likedMost.isEmpty() ? null : likedMost);
            pstmt.setString(9, improvements.isEmpty() ? null : improvements);
            
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Database error:");
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("Error parsing date:");
            e.printStackTrace();
            return false;
        }
    }

}



class FeedbackDialog {
    private static final Color BG_COLOR = new Color(245, 245, 245);
    private static final Color FIELD_COLOR = new Color(255, 255, 255);
    private static final Color ERROR_COLOR = new Color(255, 230, 230);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 16);

    public static void show(JFrame parent) {
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(BG_COLOR);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel title = new JLabel("Game Feedback Form", JLabel.CENTER);
        title.setFont(TITLE_FONT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        panel.add(title, gbc);
        gbc.gridwidth = 1;

        addSectionLabel("Personal Information", panel, gbc, 1);

        
        addFormField("Full Name*:", panel, gbc, 2);
        JTextField nameField = createStyledTextField();
        gbc.gridwidth = 1;
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(nameField, gbc);

        
        addFormField("Email:", panel, gbc, 3);
        JTextField emailField = createStyledTextField();
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(emailField, gbc);

        addFormField("Phone*:", panel, gbc, 4);
        JTextField phoneField = createStyledTextField();
        gbc.gridx = 1;
        gbc.gridy = 4;
        panel.add(phoneField, gbc);

        
        addFormField("Date of Birth (DD/MM/YYYY)*:", panel, gbc, 5);
        JTextField dobField = createStyledTextField();
        gbc.gridx = 1;
        gbc.gridy = 5;
        panel.add(dobField, gbc);

        
        addSectionLabel("Game Experience", panel, gbc, 6);

        addFormField("Rating*:", panel, gbc, 7);
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        ratingPanel.setBackground(BG_COLOR);
        ButtonGroup ratingGroup = new ButtonGroup();
        for (int i = 1; i <= 5; i++) {
            JRadioButton rb = new JRadioButton(String.valueOf(i));
            rb.setBackground(BG_COLOR);
            rb.setFont(FIELD_FONT);
            ratingGroup.add(rb);
            ratingPanel.add(rb);
            if (i == 4) rb.setSelected(true);
        }
        gbc.gridx = 1;
        gbc.gridy = 7;
        panel.add(ratingPanel, gbc);

        
        addFormField("Play Frequency:", panel, gbc, 8);
        JComboBox<String> frequencyCombo = new JComboBox<>(new String[]{
            "Daily", "Weekly", "Monthly", "Occasionally"
        });
        frequencyCombo.setFont(FIELD_FONT);
        frequencyCombo.setBackground(FIELD_COLOR);
        gbc.gridx = 1;
        gbc.gridy = 8;
        panel.add(frequencyCombo, gbc);

        addFormField("Would Recommend?", panel, gbc, 9);
        JComboBox<String> recommendCombo = new JComboBox<>(new String[]{
            "Definitely", "Probably", "Not Sure", "Probably Not", "Definitely Not"
        });
        recommendCombo.setFont(FIELD_FONT);
        recommendCombo.setBackground(FIELD_COLOR);
        gbc.gridx = 1;
        gbc.gridy = 9;
        panel.add(recommendCombo, gbc);

        addSectionLabel("Additional Feedback", panel, gbc, 10);

        addFormField("What did you like most?", panel, gbc, 11);
        JTextArea likedMostArea = createStyledTextArea();
        JScrollPane likedMostScroll = new JScrollPane(likedMostArea);
        likedMostScroll.setPreferredSize(new Dimension(300, 80));
        gbc.gridx = 1;
        gbc.gridy = 11;
        panel.add(likedMostScroll, gbc);

   
        addFormField("Suggestions for improvement:", panel, gbc, 12);
        JTextArea improvementsArea = createStyledTextArea();
        JScrollPane improvementsScroll = new JScrollPane(improvementsArea);
        improvementsScroll.setPreferredSize(new Dimension(300, 80));
        gbc.gridx = 1;
        gbc.gridy = 12;
        panel.add(improvementsScroll, gbc);

    
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JCheckBox termsCheck = new JCheckBox("I agree to the terms of feedback collection");
        termsCheck.setBackground(BG_COLOR);
        termsCheck.setFont(FIELD_FONT);
        panel.add(termsCheck, gbc);

        
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton submitButton = new JButton("Submit Feedback");
        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        submitButton.setBackground(new Color(70, 130, 180));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.setPreferredSize(new Dimension(200, 35));
        panel.add(submitButton, gbc);  

        JDialog dialog = new JDialog(parent, "Game Feedback", true);
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setSize(700, 850); 
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(true);
 
        submitButton.addActionListener(e -> {
        
            resetFieldStyles(nameField, emailField, phoneField, dobField);

            boolean isValid = true;
            StringBuilder errorMessage = new StringBuilder();
            
            if (nameField.getText().trim().isEmpty()) {
                markFieldError(nameField, "Name is required");
                errorMessage.append("- Name is required\n");
                isValid = false;
            } else if (!isValidName(nameField.getText())) {
                markFieldError(nameField, "Name can only contain letters and spaces");
                errorMessage.append("- Name can only contain letters and spaces\n");
                isValid = false;
            }
            
            // Email validation
            if (!emailField.getText().trim().isEmpty() && !isValidEmail(emailField.getText())) {
                markFieldError(emailField, "Invalid email format");
                errorMessage.append("- Invalid email format\n");
                isValid = false;
            }
        
            if (phoneField.getText().trim().isEmpty()) {
                markFieldError(phoneField, "Phone number is required");
                errorMessage.append("- Phone number is required\n");
                isValid = false;
            } else if (!isValidPhone(phoneField.getText())) {
                markFieldError(phoneField, "Phone must be 10 digits");
                errorMessage.append("- Phone number must be exactly 10 digits\n");
                isValid = false;
            }
            
            if (!isValidDate(dobField.getText())) {
                markFieldError(dobField, "Invalid date (DD/MM/YYYY)");
                errorMessage.append("- Invalid date format (DD/MM/YYYY)\n");
                isValid = false;
            }
            
            if (!termsCheck.isSelected()) {
                errorMessage.append("- Please agree to the terms\n");
                isValid = false;
            }
            
            if (!isValid) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please fix the following errors:\n\n" + errorMessage.toString(), 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String rating = "Not selected";
            for (Component comp : ratingPanel.getComponents()) {
                if (comp instanceof JRadioButton && ((JRadioButton)comp).isSelected()) {
                    rating = ((JRadioButton)comp).getText();
                    break;
                }
            }
            
            FeedbackData feedback = new FeedbackData(
                nameField.getText().trim(),
                dobField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                rating,
                frequencyCombo.getSelectedItem().toString(),
                recommendCombo.getSelectedItem().toString(),
                likedMostArea.getText().trim(),
                improvementsArea.getText().trim()
            );
            
            System.out.println("Feedback collected:\n" + feedback);
            
            JOptionPane.showMessageDialog(dialog, 
                "Thank you for your valuable feedback!", 
                "Submission Successful", 
                JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
            if (!isValid) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please fix the following errors:\n\n" + errorMessage.toString(), 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            boolean storedSuccessfully = DatabaseManager.storeFeedback(
                nameField.getText().trim(),
                dobField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                rating,
                frequencyCombo.getSelectedItem().toString(),
                recommendCombo.getSelectedItem().toString(),
                likedMostArea.getText().trim(),
                improvementsArea.getText().trim()
            );
            
            if (storedSuccessfully) {
                JOptionPane.showMessageDialog(dialog, 
                    "Thank you for your valuable feedback!", 
                    "Submission Successful", 
                    JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    "Failed to store feedback. Please try again.", 
                    "Storage Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        
        });

        dialog.setVisible(true);
    }

    private static class FeedbackData {
        String name;
        String dob;
        String email;
        String phone;
        String rating;
        String frequency;
        String recommendation;
        String likedMost;
        String improvements;

        public FeedbackData(String name, String dob, String email, String phone, 
                           String rating, String frequency, 
                           String recommendation, String likedMost, String improvements) {
            this.name = name;
            this.dob = dob;
            this.email = email;
            this.phone = phone;
            this.rating = rating;
            this.frequency = frequency;
            this.recommendation = recommendation;
            this.likedMost = likedMost;
            this.improvements = improvements;
        }

        @Override
        public String toString() {
            return "Name: " + name + "\n" +
                   "DOB: " + dob + "\n" +
                   "Email: " + email + "\n" +
                   "Phone: " + phone + "\n" +
                   "Rating: " + rating + "\n" +
                   "Play Frequency: " + frequency + "\n" +
                   "Recommendation: " + recommendation + "\n" +
                   "Liked Most: " + likedMost + "\n" +
                   "Improvements: " + improvements;
        }
    }

    private static void addSectionLabel(String text, JPanel panel, GridBagConstraints gbc, int yPos) {
        gbc.gridx = 0;
        gbc.gridy = yPos;
        gbc.gridwidth = 2;
        JLabel label = new JLabel(text);
        label.setFont(SECTION_FONT);
        label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        label.setForeground(new Color(70, 130, 180));
        panel.add(label, gbc);
        gbc.gridwidth = 1;
    }

    private static void addFormField(String labelText, JPanel panel, GridBagConstraints gbc, int yPos) {
        gbc.gridx = 0;
        gbc.gridy = yPos;
        JLabel label = new JLabel(labelText);
        label.setFont(LABEL_FONT);
        panel.add(label, gbc);
    }

    private static JTextField createStyledTextField() {
        JTextField field = new JTextField(25);
        field.setFont(FIELD_FONT);
        field.setBackground(FIELD_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        return field;
    }

    private static JTextArea createStyledTextArea() {
        JTextArea area = new JTextArea(3, 25);
        area.setFont(FIELD_FONT);
        area.setBackground(FIELD_COLOR);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        return area;
    }

    private static void markFieldError(JTextField field, String message) {
        field.setBackground(ERROR_COLOR);
        field.setToolTipText(message);
        field.grabFocus();
    }

    private static void resetFieldStyles(JTextField... fields) {
        for (JTextField field : fields) {
            field.setBackground(FIELD_COLOR);
            field.setToolTipText(null);
        }
    }

    private static boolean isValidName(String name) {
        return Pattern.compile("^[a-zA-Z\\s]+$").matcher(name).matches();
    }

    private static boolean isValidEmail(String email) {
        return Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matcher(email).matches();
    }

    private static boolean isValidPhone(String phone) {
       
        return Pattern.compile("^\\d{10}$").matcher(phone).matches();
    }

    private static boolean isValidDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            Date dob = sdf.parse(dateStr);
            return !dob.after(new Date()); 
        } catch (Exception e) {
            return false;
        }
    }
}
class Game {
    static int score_human;
    static int score_win = 0;
    static int score_total = 0;
    static int score_tie = 0;
    static int score_computer = 0;

    public static void panel_introduction() {
        String info_text = "Rock, Paper, Scissors! This game is fairly simple.\n"
                + "Simply pick your hands whenever you are ready.\n"
                + "Rock beats scissors, scissors beat paper,\n"
                + "and paper wraps the rock. Yes, paper beats rock.";
        JOptionPane.showMessageDialog(null, info_text, "How to play!", 1);
    }

    public static void panel_game() {
        JFrame frame_main = new JFrame("Rock, Scissors, Paper");
        frame_main.setSize(800, 600); // Set fixed size
        frame_main.setLocationRelativeTo(null); // Center on screen
        frame_main.getContentPane().setBackground(Color.BLACK);
        
        // Create a main panel with GridBagLayout for better control
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.BLACK);
        
        // Create a scroll pane and add the main panel to it
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        frame_main.add(scrollPane);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;

        String[] icon_path = new String[3];
        for (int i = 0; i <= 2; i++) {
            icon_path[i] = System.getProperty("user.dir") + "/images/" + i + ".png";
        }

        JLabel title_main = new JLabel("Rock Paper Scissors", SwingConstants.CENTER);
        title_main.setFont(new Font("Arial", Font.BOLD, 22));
        title_main.setForeground(Color.WHITE);
        mainPanel.add(title_main, gbc);

        // Top buttons row
        JPanel topButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        topButtonPanel.setBackground(Color.BLACK);
        
        JButton btn_history = new JButton("View History");
        btn_history.addActionListener(e -> {
            String history = DatabaseManager.getGameHistory();
            JOptionPane.showMessageDialog(frame_main, history, "Game History", JOptionPane.INFORMATION_MESSAGE);
        });

        JToggleButton toggle_button = new JToggleButton("Light Mode");
        toggle_button.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                int state = itemEvent.getStateChange();
                if (state == ItemEvent.SELECTED) {
                    mainPanel.setBackground(Color.WHITE);
                    topButtonPanel.setBackground(Color.WHITE);
                    title_main.setForeground(Color.BLACK);
                    toggle_button.setText("Dark Mode");
                } else {
                    mainPanel.setBackground(Color.BLACK);
                    topButtonPanel.setBackground(Color.BLACK);
                    title_main.setForeground(Color.WHITE);
                    toggle_button.setText("Light Mode");
                }
            }
        });

        topButtonPanel.add(btn_history);
        topButtonPanel.add(toggle_button);
        
        gbc.gridy++;
        mainPanel.add(topButtonPanel, gbc);

        // Game buttons row
        JPanel gameButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        gameButtonPanel.setBackground(mainPanel.getBackground());
        
        JButton btn_rock = new JButton(" ", new ImageIcon(icon_path[0]));
        btn_rock.setBackground(Color.RED);
        btn_rock.setPreferredSize(new Dimension(200, 250));
        
        JButton btn_paper = new JButton(" ", new ImageIcon(icon_path[1]));
        btn_paper.setBackground(Color.YELLOW);
        btn_paper.setPreferredSize(new Dimension(200, 250));
        
        JButton btn_scissors = new JButton(" ", new ImageIcon(icon_path[2]));
        btn_scissors.setBackground(Color.BLUE);
        btn_scissors.setPreferredSize(new Dimension(200, 250));
        
        gameButtonPanel.add(btn_rock);
        gameButtonPanel.add(btn_paper);
        gameButtonPanel.add(btn_scissors);
        
        gbc.gridy++;
        mainPanel.add(gameButtonPanel, gbc);

        // Bottom buttons row
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        bottomButtonPanel.setBackground(mainPanel.getBackground());
        
        JButton btn_exit = new JButton("Exit Game");
        btn_exit.setBackground(Color.RED);
        btn_exit.setForeground(Color.WHITE);
        btn_exit.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                frame_main, 
                "Are you sure you want to exit?", 
                "Exit Game", 
                JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                FeedbackDialog.show(frame_main); 
                frame_main.dispose(); 
            }
        });
        
        bottomButtonPanel.add(btn_exit);
        
        gbc.gridy++;
        mainPanel.add(bottomButtonPanel, gbc);

        // Add action listeners
        btn_rock.addActionListener(e -> compute_winner(1));
        btn_paper.addActionListener(e -> compute_winner(2));
        btn_scissors.addActionListener(e -> compute_winner(3));

        frame_main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame_main.setVisible(true);
    }
    public static void compute_winner(int choice_human) {
        int choice_computer = (int) (Math.random() * 3) + 1;
        String label_choice;
        String label_winner = "";
        String combo = "" + Math.min(choice_computer, choice_human) + Math.max(choice_computer, choice_human);
        
        String humanChoiceStr = getChoiceName(choice_human);
        String computerChoiceStr = getChoiceName(choice_computer);
        String result = "";

        switch (Integer.parseInt(combo)) {
            case 12:
                label_choice = "Paper wins!";
                if (choice_human == 2) {
                    score_human = 1;
                    result = "Human wins";
                } else {
                    result = "Computer wins";
                }
                break;
            case 13:
                label_choice = "Rock wins!";
                if (choice_human == 1) {
                    score_human = 1;
                    result = "Human wins";
                } else {
                    result = "Computer wins";
                }
                break;
            case 23:
                label_choice = "Scissors wins!";
                if (choice_human == 3) {
                    score_human = 1;
                    result = "Human wins";
                } else {
                    result = "Computer wins";
                }
                break;
            default:
                label_choice = "It is a tie!";
                score_human = 2;
                score_tie++;
                result = "Tie";
        }

        if (score_human == 1) {
            label_winner = "   Human wins!";
            score_win++;
            score_total++;
            DatabaseManager.updateStats(1, 0, 0);
        } else if (score_human == 2) {
            label_winner = "   No one wins!";
            score_total++;
            DatabaseManager.updateStats(0, 0, 1);
        } else {
            label_winner = "   Computer wins!";
            score_computer++;
            score_total++;
            DatabaseManager.updateStats(0, 1, 0);
        }
        
        DatabaseManager.logGame(humanChoiceStr, computerChoiceStr, result);
        
        score_human = 0;

        JFrame score_frame = new JFrame("Game Result");

score_frame.getContentPane().setBackground(new Color(255, 218, 185));
        Container panel = score_frame.getContentPane();
        panel.setLayout(null);

        JLabel result_label = new JLabel(label_choice + label_winner);
        result_label.setBounds(150, 10, 300, 30);
        panel.add(result_label);

        JLabel human_label = new JLabel("User's Choice");
        human_label.setBounds(50, 35, 150, 30);
        panel.add(human_label);

        JLabel computer_label = new JLabel("Computer's Choice");
        computer_label.setBounds(350, 35, 150, 30);
        panel.add(computer_label);

        ImageIcon iconHuman = new ImageIcon(System.getProperty("user.dir") + "/images/" + (choice_human - 1) + ".png");
        Image scaledHumanImg = iconHuman.getImage().getScaledInstance(170, 170, Image.SCALE_SMOOTH);
        JLabel image_human = new JLabel(new ImageIcon(scaledHumanImg));
        image_human.setBounds(40, 100, 170, 170);
        panel.add(image_human);

        ImageIcon iconComputer = new ImageIcon(System.getProperty("user.dir") + "/images/" + (choice_computer - 1) + "c.png");
        Image scaledCompImg = iconComputer.getImage().getScaledInstance(170, 170, Image.SCALE_SMOOTH);
        JLabel image_computer = new JLabel(new ImageIcon(scaledCompImg));
        image_computer.setBounds(340, 100, 170, 170);
        panel.add(image_computer);

        JLabel score_label = new JLabel("<html>"
                + "<div style='text-align:right;'>"
                + "Human: " + score_win + "<br>"
                + "Computer: " + score_computer + "<br>"
                + "Ties: " + score_tie + "<br>"
                + "Total: " + score_total
                + "</div></html>");
        score_label.setBounds(420, 280, 150, 80);
        panel.add(score_label);

        JButton ok_button = new JButton("OK");
        ok_button.setBackground(Color.GREEN);
        ok_button.setBounds(200, 330, 80, 40);
        ok_button.addActionListener(e -> score_frame.dispose());
        panel.add(ok_button);

        JButton reset_button = new JButton("Reset");
        reset_button.setBackground(Color.RED);
        reset_button.setBounds(300, 330, 80, 40);
        reset_button.addActionListener((ActionEvent e) -> {
            score_win = 0;
            score_computer = 0;
            score_tie = 0;
            score_total = 0;
            DatabaseManager.resetStats();
            JOptionPane.showMessageDialog(score_frame, "Scores have been reset!");
            score_frame.dispose();
        });
        panel.add(reset_button);

        score_frame.setSize(600, 450);
        score_frame.setVisible(true);
    }
    
    private static String getChoiceName(int choice) {
        switch (choice) {
            case 1: return "Rock";
            case 2: return "Paper";
            case 3: return "Scissors";
            default: return "Unknown";
        }
    }
}
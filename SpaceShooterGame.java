import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class SpaceShooterGame extends JFrame {
    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/rpsssgame";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "sandeep05";
    
   
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PLAYER_SIZE = 10;
    private static final int ENEMY_SIZE = 50;
    private static final int BULLET_SIZE = 10;
    private static final int PLAYER_SPEED = 25;
    private static final int ENEMY_SPEED = 1;
    private static final int BULLET_SPEED = 7;
    
   
    private int playerX, playerY;
    private List<Enemy> enemies;
    private List<Bullet> bullets;
    private int score;
    private boolean gameOver;
    private Timer gameTimer;
    private long lastEnemySpawn;
    
    
    private GamePanel gamePanel;
    private JLabel scoreLabel;
    
    public SpaceShooterGame() {
        initializeDatabase();
        setupMainMenu();
    }
    
    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            String sql = "CREATE TABLE IF NOT EXISTS scores (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "player_name VARCHAR(50) NOT NULL, " +
                         "score INT NOT NULL, " +
                         "date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
            stmt.execute(sql);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), 
                                         "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void setupMainMenu() {
        setTitle("Space Shooter Game");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        
        JPanel menuPanel = new JPanel(new GridBagLayout());
        menuPanel.setBackground(Color.BLACK);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 10, 0);
        
        JLabel titleLabel = new JLabel("SPACE SHOOTER");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.GREEN);
        menuPanel.add(titleLabel, gbc);
        
        JButton startButton = new JButton("Start Game");
        styleButton(startButton);
        startButton.addActionListener(e -> startGame());
        menuPanel.add(startButton, gbc);
        
        JButton scoreButton = new JButton("View Scores");
        styleButton(scoreButton);
        scoreButton.addActionListener(e -> showHighScores());
        menuPanel.add(scoreButton, gbc);
        
        JButton exitButton = new JButton("Exit");
        styleButton(exitButton);
        exitButton.addActionListener(e -> {
            dispose();
            Mainn.main(null);
        });
        
        menuPanel.add(exitButton, gbc);
        
        add(menuPanel);
        setVisible(true);
    }
    
    private void styleButton(JButton button) {
        button.setPreferredSize(new Dimension(200, 50));
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(Color.DARK_GRAY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
    }
    
    private void startGame() {
        getContentPane().removeAll();
        
        playerX = WIDTH / 2 - PLAYER_SIZE / 2;
        playerY = HEIGHT - PLAYER_SIZE - 20;
        enemies = new ArrayList<>();
        bullets = new ArrayList<>();
        score = 0;
        gameOver = false;
        lastEnemySpawn = System.currentTimeMillis();
        
        gamePanel = new GamePanel();
        gamePanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
        
        JPanel topPanel = new JPanel();
        topPanel.setBackground(Color.BLACK);
        topPanel.add(scoreLabel);
        
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(gamePanel, BorderLayout.CENTER);
        
        setupKeyListeners();
        
        gameTimer = new Timer(16, e -> updateGame());
        gameTimer.start();
        
        revalidate();
        repaint();
    }
    
    private void setupKeyListeners() {
        gamePanel.setFocusable(true);
        gamePanel.requestFocusInWindow();
        
        gamePanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    playerX = Math.max(0, playerX - PLAYER_SPEED);
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    playerX = Math.min(WIDTH - PLAYER_SIZE, playerX + PLAYER_SPEED);
                } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    bullets.add(new Bullet(playerX + PLAYER_SIZE / 2 - BULLET_SIZE / 2, playerY));
                }
            }
        });
    }
    
    private void updateGame() {
        if (gameOver) {
            gameTimer.stop();
            gameOver();
            return;
        }
        
        if (System.currentTimeMillis() - lastEnemySpawn > 1000) {
            enemies.add(new Enemy((int)(Math.random() * (WIDTH - ENEMY_SIZE)), 0));
            lastEnemySpawn = System.currentTimeMillis();
        }
        
        for (Enemy enemy : enemies) {
            enemy.y += ENEMY_SPEED;
            if (enemy.y > HEIGHT) {
                gameOver = true;
            }
        }
        
        for (Bullet bullet : bullets) {
            bullet.y -= BULLET_SPEED;
        }
        
        checkCollisions();
        
        bullets.removeIf(bullet -> bullet.y < 0);
        
        enemies.removeIf(enemy -> enemy.hit);
        
        scoreLabel.setText("Score: " + score);
        
        gamePanel.repaint();
    }
    
    private void checkCollisions() {
        List<Bullet> bulletsToRemove = new ArrayList<>();
        List<Enemy> enemiesToRemove = new ArrayList<>();
        
        for (Bullet bullet : bullets) {
            for (Enemy enemy : enemies) {
                if (!enemy.hit && bullet.intersects(enemy)) {
                    enemy.hit = true;
                    bulletsToRemove.add(bullet);
                    score += 10;
                    break;
                }
            }
        }
        
        bullets.removeAll(bulletsToRemove);
    }
    
    private void gameOver() {
        String playerName = JOptionPane.showInputDialog(this, "Game Over! Your score: " + score + "\nEnter your name:");
        
        if (playerName != null && !playerName.trim().isEmpty()) {
            saveScore(playerName.trim(), score);
        }
        
        setupMainMenu();
    }
    
    private void saveScore(String playerName, int score) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO scores (player_name, score) VALUES (?, ?)")) {
            
            stmt.setString(1, playerName);
            stmt.setInt(2, score);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to save score: " + e.getMessage(), 
                                         "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showHighScores() {
        JFrame scoresFrame = new JFrame("High Scores");
        scoresFrame.setSize(WIDTH, HEIGHT);
        scoresFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        scoresFrame.setLocationRelativeTo(null);
        scoresFrame.setResizable(false);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);
        
        JLabel titleLabel = new JLabel("HIGH SCORES", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(Color.GREEN);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Rank");
        model.addColumn("Name");
        model.addColumn("Score");
        model.addColumn("Date");
        
        JTable table = new JTable(model);
        table.setBackground(Color.BLACK);
        table.setForeground(Color.WHITE);
        table.setFont(new Font("Arial", Font.PLAIN, 16));
        table.setGridColor(Color.DARK_GRAY);
        table.setRowHeight(30);
        table.setEnabled(false);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBackground(Color.BLACK);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JButton closeButton = new JButton("Close");
        styleButton(closeButton);
        closeButton.addActionListener(e -> scoresFrame.dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.add(closeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT player_name, score, date FROM scores ORDER BY score DESC LIMIT 10")) {
        
            int rank = 1;
            while (rs.next()) {
                model.addRow(new Object[]{
                    rank++,
                    rs.getString("player_name"),
                    rs.getInt("score"),
                    rs.getTimestamp("date")
                });
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(scoresFrame, "Failed to load scores: " + e.getMessage(), 
                                         "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        scoresFrame.add(panel);
        scoresFrame.setVisible(true);
    }
    
    private class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            
            g.setColor(Color.WHITE);
            for (int i = 0; i < 100; i++) {
                g.fillRect((int)(Math.random() * getWidth()), (int)(Math.random() * getHeight()), 2, 2);
            }
            
            g.setColor(Color.GREEN);
            g.fillRect(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);
            
            for (Enemy enemy : enemies) {
                g.setColor(Color.RED);
                g.fillRect(enemy.x, enemy.y, ENEMY_SIZE, ENEMY_SIZE);
            }
            
            g.setColor(Color.YELLOW);
            for (Bullet bullet : bullets) {
                g.fillRect(bullet.x, bullet.y, BULLET_SIZE, BULLET_SIZE);
            }
        }
    }
    
    private class Bullet {
        int x, y;
        
        public Bullet(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        public boolean intersects(Enemy enemy) {
            return x < enemy.x + ENEMY_SIZE && x + BULLET_SIZE > enemy.x && y < enemy.y + ENEMY_SIZE && y + BULLET_SIZE > enemy.y;
        }
    }
    
    private class Enemy {
        int x, y;
        boolean hit;
        
        public Enemy(int x, int y) {
            this.x = x;
            this.y = y;
            this.hit = false;
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SpaceShooterGame());
    }
}

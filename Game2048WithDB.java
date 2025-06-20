import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import javax.swing.*;

public class Game2048WithDB extends JFrame {
    private int[][] board = new int[4][4];
    private Random random = new Random();
    private boolean moved = false;
    private GamePanel panel;
    private int score = 0;
    private int highScore = 0;

   
    private final String DB_URL = "jdbc:mysql://localhost:3306/rpsssgame"; 
    private final String DB_USER = "root"; 
    private final String DB_PASSWORD = "sandeep05"; 

    public Game2048WithDB() {
        setTitle("2048 Game with Scoreboard and Database");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        panel = new GamePanel();
        add(panel);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                moved = false;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT -> moveLeft();
                    case KeyEvent.VK_RIGHT -> moveRight();
                    case KeyEvent.VK_UP -> moveUp();
                    case KeyEvent.VK_DOWN -> moveDown();
                }
                if (moved) {
                    addRandomTile();
                    panel.repaint();
                    if (!canMove()) {
                        saveScoreToDatabase();
                        JOptionPane.showMessageDialog(null, "Game Over! Your score: " + score);
                        dispose(); 
                        new Main(); 
                    }                    
                }
            }
        });

        resetGame();
        fetchHighScore();
        setLocationRelativeTo(null); 
        setVisible(true);
    }

    private void resetGame() {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                board[i][j] = 0;
        score = 0;
        addRandomTile();
        addRandomTile();
        fetchHighScore();
    }

    private void addRandomTile() {
        int emptyCount = 0;
        for (int[] row : board)
            for (int val : row)
                if (val == 0)
                    emptyCount++;

        if (emptyCount == 0) return;

        int pos = random.nextInt(emptyCount);
        int num = random.nextInt(10) < 9 ? 2 : 4;

        int count = 0;
        outer:
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                if (board[i][j] == 0) {
                    if (count == pos) {
                        board[i][j] = num;
                        break outer;
                    }
                    count++;
                }
    }

    private boolean canMove() {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++) {
                if (board[i][j] == 0)
                    return true;
                if (i < 3 && board[i][j] == board[i + 1][j])
                    return true;
                if (j < 3 && board[i][j] == board[i][j + 1])
                    return true;
            }
        return false;
    }

    private void moveLeft() {
        for (int i = 0; i < 4; i++) {
            int[] newRow = new int[4];
            int index = 0;
            boolean merged = false;
            for (int j = 0; j < 4; j++) {
                if (board[i][j] != 0) {
                    if (index > 0 && newRow[index - 1] == board[i][j] && !merged) {
                        newRow[index - 1] *= 2;
                        score += newRow[index - 1];
                        merged = true;
                        moved = true;
                    } else {
                        newRow[index++] = board[i][j];
                        if (j != index - 1) moved = true;
                        merged = false;
                    }
                }
            }
            board[i] = newRow;
        }
    }

    private void moveRight() {
        for (int i = 0; i < 4; i++) {
            int[] newRow = new int[4];
            int index = 3;
            boolean merged = false;
            for (int j = 3; j >= 0; j--) {
                if (board[i][j] != 0) {
                    if (index < 3 && newRow[index + 1] == board[i][j] && !merged) {
                        newRow[index + 1] *= 2;
                        score += newRow[index + 1];
                        merged = true;
                        moved = true;
                    } else {
                        newRow[index--] = board[i][j];
                        if (j != index + 1) moved = true;
                        merged = false;
                    }
                }
            }
            board[i] = newRow;
        }
    }

    private void moveUp() {
        for (int j = 0; j < 4; j++) {
            int[] newCol = new int[4];
            int index = 0;
            boolean merged = false;
            for (int i = 0; i < 4; i++) {
                if (board[i][j] != 0) {
                    if (index > 0 && newCol[index - 1] == board[i][j] && !merged) {
                        newCol[index - 1] *= 2;
                        score += newCol[index - 1];
                        merged = true;
                        moved = true;
                    } else {
                        newCol[index++] = board[i][j];
                        if (i != index - 1) moved = true;
                        merged = false;
                    }
                }
            }
            for (int i = 0; i < 4; i++)
                board[i][j] = newCol[i];
        }
    }

    private void moveDown() {
        for (int j = 0; j < 4; j++) {
            int[] newCol = new int[4];
            int index = 3;
            boolean merged = false;
            for (int i = 3; i >= 0; i--) {
                if (board[i][j] != 0) {
                    if (index < 3 && newCol[index + 1] == board[i][j] && !merged) {
                        newCol[index + 1] *= 2;
                        score += newCol[index + 1];
                        merged = true;
                        moved = true;
                    } else {
                        newCol[index--] = board[i][j];
                        if (i != index + 1) moved = true;
                        merged = false;
                    }
                }
            }
            for (int i = 0; i < 4; i++)
                board[i][j] = newCol[i];
        }
    }

    private void saveScoreToDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "INSERT INTO scores2048 (time, score) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            stmt.setString(1, time);
            stmt.setInt(2, score);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchHighScore() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT MAX(score) FROM scores2048";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                highScore = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(new Color(0xbbada0));
            g.fillRect(0, 0, this.getWidth(), this.getHeight());

            g.setColor(Color.BLACK);
            g.setFont(new Font("SansSerif", Font.BOLD, 18));
            g.drawString("Score: " + score, 20, 30);
            g.drawString("High Score: " + highScore, 200, 30);

            for (int i = 0; i < 4; i++)
                for (int j = 0; j < 4; j++)
                    drawTile(g, board[i][j], j, i);
        }

        private void drawTile(Graphics g, int value, int x, int y) {
            int TILE_SIZE = 80;
            int TILE_MARGIN = 16;
            int xOffset = TILE_MARGIN + x * (TILE_SIZE + TILE_MARGIN);
            int yOffset = 40 + TILE_MARGIN + y * (TILE_SIZE + TILE_MARGIN); // Move tiles down a bit for score display

            g.setColor(getTileColor(value));
            g.fillRoundRect(xOffset, yOffset, TILE_SIZE, TILE_SIZE, 14, 14);
            g.setColor(Color.BLACK);
            g.drawRoundRect(xOffset, yOffset, TILE_SIZE, TILE_SIZE, 14, 14);

            if (value != 0) {
                g.setColor(Color.BLACK);
                g.setFont(new Font("SansSerif", Font.BOLD, 24));
                String s = String.valueOf(value);
                FontMetrics fm = g.getFontMetrics();
                int asc = fm.getAscent();
                int dec = fm.getDescent();
                int strWidth = fm.stringWidth(s);
                g.drawString(s, xOffset + (TILE_SIZE - strWidth) / 2, yOffset + (TILE_SIZE + asc - dec) / 2);
            }
        }

        private Color getTileColor(int value) {
            return switch (value) {
                case 2 -> new Color(0xeee4da);
                case 4 -> new Color(0xede0c8);
                case 8 -> new Color(0xf2b179);
                case 16 -> new Color(0xf59563);
                case 32 -> new Color(0xf67c5f);
                case 64 -> new Color(0xf65e3b);
                case 128 -> new Color(0xedcf72);
                case 256 -> new Color(0xedcc61);
                case 512 -> new Color(0xedc850);
                case 1024 -> new Color(0xedc53f);
                case 2048 -> new Color(0xedc22e);
                default -> new Color(0xcdc1b4);
            };
        }
    }

    public static void main(String[] args) {
        new Game2048WithDB();
    }
}
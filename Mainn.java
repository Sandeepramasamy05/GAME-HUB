import java.awt.*;
import javax.swing.*;

public class Mainn extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JButton btnHome, btnPlayGames, btnTryNewGames, btnExit;
    private JLabel headerLabel;

    public Mainn() {
        setTitle("LEVELUP STUDIOS");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // Sidebar
        JPanel sidePanel = new JPanel();
        sidePanel.setBackground(new Color(25, 25, 112));
        sidePanel.setLayout(new GridLayout(4, 1, 10, 10));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        btnHome = createSidebarButton("🏠 Home");
        btnPlayGames = createSidebarButton("🎮 Play Games");
        btnTryNewGames = createSidebarButton("🧪 Try New Games");
        btnExit = createSidebarButton("🚪 Exit");

        sidePanel.add(btnHome);
        sidePanel.add(btnPlayGames);
        sidePanel.add(btnTryNewGames);
        sidePanel.add(btnExit);

        add(sidePanel, BorderLayout.WEST);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 102, 204));
        headerPanel.setPreferredSize(new Dimension(800, 60));

        headerLabel = new JLabel("Welcome to Levelup Studios", JLabel.CENTER);
        headerLabel.setFont(new Font("Showcard Gothic", Font.BOLD, 24));  // Updated font for title only
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        add(headerPanel, BorderLayout.NORTH);

        // Main Content Area
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createHomePanel(), "Home");
        mainPanel.add(createPlayGamesPanel(), "PlayGames");
        mainPanel.add(createTryNewGamesPanel(), "TryNewGames");

        add(mainPanel, BorderLayout.CENTER);

        // Button Actions
        btnHome.addActionListener(e -> {
            cardLayout.show(mainPanel, "Home");
            headerLabel.setText("Welcome to Levelup Studios");
        });
        btnPlayGames.addActionListener(e -> {
            cardLayout.show(mainPanel, "PlayGames");
            headerLabel.setText("Play Games");
        });
        btnTryNewGames.addActionListener(e -> {
            cardLayout.show(mainPanel, "TryNewGames");
            headerLabel.setText("Try New Games");
        });
        btnExit.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Confirm Exit?", "Exit", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        setVisible(true);
    }

    private JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 18));  // Default font for sidebar
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 149, 237));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 180));
            }
        });

        return button;
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 245));

        JLabel welcomeLabel = new JLabel("<html><center><h1>Game Hub</h1><h3>Select an option from the left menu</h3></center></html>", JLabel.CENTER);
        welcomeLabel.setFont(new Font("SansSerif", Font.PLAIN, 24));  // Default font for content

        panel.add(welcomeLabel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPlayGamesPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 20, 20));
        panel.setBackground(new Color(230, 230, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        JButton game2048 = new JButton("🧩 2048 Game");
        JButton spaceShooter = new JButton("🚀 Space Shooter");

        styleGameButton(game2048);
        styleGameButton(spaceShooter);

        panel.add(new JLabel("🚀 Blast Off into Gaming — Choose Your Adventure!", JLabel.CENTER));
        panel.add(game2048);
        panel.add(spaceShooter);

        game2048.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Launching 2048 Game...");
             Game2048WithDB.main(null);
        });

        spaceShooter.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Launching Space Shooter...");
             SpaceShooterGame.main(null);
        });

        return panel;
    }

    private JPanel createTryNewGamesPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 20, 20));
        panel.setBackground(new Color(255, 228, 225));
        panel.setBorder(BorderFactory.createEmptyBorder(60, 60, 60, 60));

        JButton rpsGame = new JButton("✊🖐✌ Rock Paper Scissors");

        styleGameButton(rpsGame);

        panel.add(new JLabel("🧪 Try New Games Here!", JLabel.CENTER));
        panel.add(rpsGame);

        rpsGame.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Launching Rock Paper Scissors...");
             Main.main(null);
        });

        return panel;
    }

    private void styleGameButton(JButton button) {
        button.setFont(new Font("SansSerif", Font.BOLD, 20));  
        button.setBackground(new Color(176, 196, 222));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void main(String[] args) {
        System.out.println(">> Mainn.main() entered");
        SwingUtilities.invokeLater(() -> {
            System.out.println(">> launching UI");
            new Mainn();
        });
    }
}
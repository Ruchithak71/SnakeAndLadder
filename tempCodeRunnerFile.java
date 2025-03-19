import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SnakeAndLadderAI extends JFrame {
    private static final int BOARD_SIZE = 10;
    private Map<Integer, Integer> snakes;
    private Map<Integer, Integer> ladders;
    private Map<Integer, Boolean> traps;
    private Map<Integer, Integer> teleports;
    private Map<Integer, Boolean> powerUpTiles;
    private JButton rollDiceButton;
    private JLabel playerLabel, player2Label, aiLabel, diceResultLabel, turnLabel;
    private int playerPosition = 1, player2Position = 1, aiPosition = 1;
    private Random random;
    private boolean playerTurn = true;
    private boolean player2Turn = false;
    private boolean aiTurn = false;
    private ImageIcon[] snakeImages, ladderImages;
    private boolean skipTurnPlayer = false;
    private boolean skipTurnPlayer2 = false;
    private boolean skipTurnAI = false; 
    private ImageIcon trapImage, teleportImage;
    private JPanel boardPanel;
    private int player1PowerUps = 0;
    private int player2PowerUps = 0;
    private int aiPowerUps = 0;
    private JLabel player1PowerUpLabel, player2PowerUpLabel, aiPowerUpLabel;
    private ImageIcon powerUpImage;
   
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SnakeAndLadderAI());
    }

    public SnakeAndLadderAI() {
        setTitle("Snake and Ladder Game with 2 Players and AI 🤖");
        setSize(600, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    
        initializeSnakesAndLadders();
        loadImages();
        random = new Random();
    
        boardPanel = createBoard();
        add(boardPanel, BorderLayout.CENTER);
        add(createControls(), BorderLayout.SOUTH);
    
        setVisible(true);
    }

    private void initializeSnakesAndLadders() {
        snakes = new HashMap<>();
        ladders = new HashMap<>();
        traps = new HashMap<>();
        teleports = new HashMap<>();
        powerUpTiles = new HashMap<>();
    
        // Define snakes (moves player backward)
        snakes.put(17, 7);
        snakes.put(38, 19);
        snakes.put(53, 33);
        snakes.put(85, 43);
        snakes.put(99, 77);
    
        // Define ladders (moves player forward)
        ladders.put(4, 25);
        ladders.put(13, 34);
        ladders.put(22, 41);
        ladders.put(57, 76);
        ladders.put(72, 91);
    
        // Define trap positions (player will skip next turn)
        traps.put(29, true);
        traps.put(82, true);
    
        // Define teleport positions (player will jump forward)
        teleports.put(11, 21);
        teleports.put(44, 49);
    
        // Initialize power-up tiles (3 tiles on the board)
        powerUpTiles.put(15, true);
        powerUpTiles.put(36, true);
        powerUpTiles.put(67, true);
        
        // Debug output
        System.out.println("Game initialized with power-up tiles at: 15, 36, 67");
    }

    private void loadImages() {
        snakeImages = new ImageIcon[4];
        ladderImages = new ImageIcon[4];
    
        try {
        for (int i = 0; i < 4; i++) {
            snakeImages[i] = new ImageIcon("snake" + (i + 1) + ".png");
            ladderImages[i] = new ImageIcon("ladder" + (i + 1) + ".png");
    
            Image scaledSnake = snakeImages[i].getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            Image scaledLadder = ladderImages[i].getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
    
            snakeImages[i] = new ImageIcon(scaledSnake);
            ladderImages[i] = new ImageIcon(scaledLadder);
        }
    
        // Load trap and teleport images outside the loop
        trapImage = new ImageIcon("trap.png");
        teleportImage = new ImageIcon("teleport.png");
    
        Image scaledTrap = trapImage.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        Image scaledTeleport = teleportImage.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
    
        trapImage = new ImageIcon(scaledTrap);
        teleportImage = new ImageIcon(scaledTeleport);
        
            // Load power-up image
            powerUpImage = new ImageIcon("powerup.png");
            Image scaledPowerUp = powerUpImage.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            powerUpImage = new ImageIcon(scaledPowerUp);
        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
        }
    }

    private JPanel createBoard() {
        JPanel boardPanel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE));
        int number = 100;
        boolean reverse = true;
    
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                int cellNumber = reverse ? number - col : number - (BOARD_SIZE - 1 - col);
                JLabel cell = new JLabel(String.valueOf(cellNumber), SwingConstants.CENTER);
                cell.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                cell.setOpaque(true);
                cell.setHorizontalTextPosition(SwingConstants.CENTER);
                cell.setVerticalTextPosition(SwingConstants.TOP);
    
                // Assigning icons for ladders, snakes, traps, teleports, and power-ups
                if (ladders.containsKey(cellNumber)) {
                    cell.setIcon(ladderImages[cellNumber % 4]);
                } else if (snakes.containsKey(cellNumber)) {
                    cell.setIcon(snakeImages[cellNumber % 4]);
                } else if (traps.containsKey(cellNumber)) {
                    cell.setIcon(trapImage);
                } else if (teleports.containsKey(cellNumber)) {
                    cell.setIcon(teleportImage);
                } else if (powerUpTiles.containsKey(cellNumber) && powerUpTiles.get(cellNumber)) {
                    cell.setIcon(powerUpImage);
                    cell.setBackground(new Color(255, 223, 0, 100)); // Light gold background
                }
    
                // Highlight player, player2 and AI positions
                if (cellNumber == playerPosition) {
                    cell.setBackground(Color.BLUE);
                } else if (cellNumber == player2Position) {
                    cell.setBackground(Color.GREEN);
                } else if (cellNumber == aiPosition) {
                    cell.setBackground(Color.ORANGE);
                } else if (!powerUpTiles.containsKey(cellNumber)) {
                    cell.setBackground(Color.WHITE);
                }
    
                boardPanel.add(cell);
            }
            number -= 10;
            reverse = !reverse;
        }
        return boardPanel;
    }

    // Method to update the board display
    private void updateBoard() {
        remove(boardPanel);
        boardPanel = createBoard();
        add(boardPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel createControls() {
        JPanel controlPanel = new JPanel(new GridLayout(3, 3, 10, 10));
    
        rollDiceButton = new JButton("Roll Dice 🎲");
    
        // Create colored dots for players and AI
        JPanel playerDot = new JPanel();
        playerDot.setBackground(Color.BLUE);
        playerDot.setPreferredSize(new Dimension(20, 20));
        playerDot.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    
        JPanel player2Dot = new JPanel();
        player2Dot.setBackground(Color.GREEN);
        player2Dot.setPreferredSize(new Dimension(20, 20));
        player2Dot.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    
        JPanel aiDot = new JPanel();
        aiDot.setBackground(Color.ORANGE);
        aiDot.setPreferredSize(new Dimension(20, 20));
        aiDot.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    
        // Labels
        playerLabel = new JLabel("Player 1: " + playerPosition);
        player2Label = new JLabel("Player 2: " + player2Position);
        aiLabel = new JLabel("AI: " + aiPosition);
    
        diceResultLabel = new JLabel("Dice: 🎲 -", SwingConstants.CENTER);
        diceResultLabel.setFont(new Font("Arial", Font.BOLD, 14));
    
        turnLabel = new JLabel(playerTurn ? "Player 1's Turn 🧑" : (player2Turn ? "Player 2's Turn 👤" : "AI's Turn 🤖"), SwingConstants.CENTER);
        turnLabel.setFont(new Font("Arial", Font.BOLD, 14));
    
        // Add power-up labels
        player1PowerUpLabel = new JLabel("🎁 Power-ups: " + player1PowerUps);
        player2PowerUpLabel = new JLabel("🎁 Power-ups: " + player2PowerUps);
        aiPowerUpLabel = new JLabel("🎁 Power-ups: " + aiPowerUps);
    
        rollDiceButton.addActionListener(e -> {
            if (playerTurn) {
                rollDice(1); // Player 1 rolls dice
            } else if (player2Turn) {
                rollDice(2); // Player 2 rolls dice
            }
        });
    
        // Player panels
        JPanel playerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        playerPanel.add(playerDot);
        playerPanel.add(playerLabel);
        playerPanel.add(player1PowerUpLabel);

        JPanel player2Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        player2Panel.add(player2Dot);
        player2Panel.add(player2Label);
        player2Panel.add(player2PowerUpLabel);
    
        // AI panel
        JPanel aiPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        aiPanel.add(aiDot);
        aiPanel.add(aiLabel);
        aiPanel.add(aiPowerUpLabel);
    
        // Add components to the control panel
        controlPanel.add(rollDiceButton);
        controlPanel.add(diceResultLabel);
        controlPanel.add(turnLabel);
        controlPanel.add(playerPanel);
        controlPanel.add(player2Panel);
        controlPanel.add(aiPanel);
    
        return controlPanel;
    }
    
    private void rollDice(int playerNumber) {
        // Disable dice roll button while rolling
        rollDiceButton.setEnabled(false);
    
        // Handle skipping turn logic first
        if (handleSkipTurn(playerNumber)) {
            return;
        }
    
        // Simulate dice rolling animation
        Timer diceAnimationTimer = new Timer(100, new ActionListener() {
            int rollCount = 0;
            Random tempRandom = new Random();
    
            @Override
            public void actionPerformed(ActionEvent e) {
                if (rollCount < 10) { // Show 10 random numbers before stopping
                    int tempRoll = tempRandom.nextInt(6) + 1;
                    String playerName = playerNumber == 1 ? "Player 1" : (playerNumber == 2 ? "Player 2" : "AI");
                    diceResultLabel.setText(playerName + " rolling... 🎲 " + tempRoll);
                    rollCount++;
                } else {
                    ((Timer) e.getSource()).stop(); // Stop animation
    
                    // Generate actual dice roll
                    int diceRoll = random.nextInt(6) + 1;
                    String playerName = playerNumber == 1 ? "Player 1" : (playerNumber == 2 ? "Player 2" : "AI");
                    diceResultLabel.setText(playerName + " rolled: 🎲 " + diceRoll);
    
                    // Introduce a delay before offering power-up
                    Timer delayTimer = new Timer(1000, event -> {
                        offerPowerUp(playerNumber, diceRoll);
                    });
                    delayTimer.setRepeats(false);
                    delayTimer.start();
                }
            }
        });
        diceAnimationTimer.start(); // Start dice rolling animation
    }
    
    private boolean handleSkipTurn(int playerNumber) {
        if (playerNumber == 1 && skipTurnPlayer) {
            skipTurnPlayer = false;
            JOptionPane.showMessageDialog(this, "⏭️ Player 1 skips this turn!");
            playerTurn = false;
            player2Turn = true;
            aiTurn = false;
            turnLabel.setText("Player 2's Turn 👤");
            rollDiceButton.setEnabled(true);
            return true;
        } 
        if (playerNumber == 2 && skipTurnPlayer2) {
            skipTurnPlayer2 = false;
            JOptionPane.showMessageDialog(this, "⏭️ Player 2 skips this turn!");
            playerTurn = false;
            player2Turn = false;
            aiTurn = true;
            turnLabel.setText("AI's Turn 🤖");
            
            // Give AI a chance to play after a short delay
            Timer aiTimer = new Timer(1000, evt -> rollDice(3));
            aiTimer.setRepeats(false);
            aiTimer.start();
            return true;
        } 
        if (playerNumber == 3 && skipTurnAI) {
            skipTurnAI = false;
            JOptionPane.showMessageDialog(this, "🤖 AI skips this turn!");
            playerTurn = true;
            player2Turn = false;
            aiTurn = false;
            turnLabel.setText("Player 1's Turn 🧑");
            rollDiceButton.setEnabled(true);
            return true;
        }
        return false;
    }
    
    private void updateGameState(int playerNumber, int diceRoll) {
        int oldPosition;
        int newPosition;
        
        if (playerNumber == 1) {
            oldPosition = playerPosition;
            newPosition = updatePosition(oldPosition, diceRoll, 1);
        } else if (playerNumber == 2) {
            oldPosition = player2Position;
            newPosition = updatePosition(oldPosition, diceRoll, 2);
        } else {
            oldPosition = aiPosition;
            newPosition = updatePosition(oldPosition, diceRoll, 3);
        }
    
        animateMovement(playerNumber, oldPosition, newPosition, () -> {
            if (playerNumber == 1) {
            playerPosition = newPosition;
                playerLabel.setText("Player 1: " + playerPosition);
                playerTurn = false;
                player2Turn = true;
                aiTurn = false;
                turnLabel.setText("Player 2's Turn 👤");
                rollDiceButton.setEnabled(true);

                if (checkWin()) return;
            } else if (playerNumber == 2) {
                player2Position = newPosition;
                player2Label.setText("Player 2: " + player2Position);
            playerTurn = false;
                player2Turn = false;
                aiTurn = true;
            turnLabel.setText("AI's Turn 🤖");
            rollDiceButton.setEnabled(false);

            if (checkWin()) return;

                Timer aiTimer = new Timer(1500, evt -> rollDice(3));
            aiTimer.setRepeats(false);
            aiTimer.start();
        } else {
            aiPosition = newPosition;
            aiLabel.setText("AI: " + aiPosition);
            playerTurn = true;
                player2Turn = false;
                aiTurn = false;
                turnLabel.setText("Player 1's Turn 🧑");
            rollDiceButton.setEnabled(true);

            checkWin();
        }
    });
}

    private void animateMovement(int playerNumber, int oldPosition, int newPosition, Runnable onComplete) {
        if (oldPosition == newPosition) {
            updateBoard();
            onComplete.run();
            return;
        }
        
    Timer movementTimer = new Timer(250, new ActionListener() {
        int currentPosition = oldPosition;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (currentPosition == newPosition) {
                ((Timer) e.getSource()).stop();
                    updateBoard();
                    onComplete.run();
                return;
            }
                
                currentPosition += (currentPosition < newPosition) ? 1 : -1;
                
                if (playerNumber == 1) {
                    playerPosition = currentPosition;
                    playerLabel.setText("Player 1: " + currentPosition);
                } else if (playerNumber == 2) {
                    player2Position = currentPosition;
                    player2Label.setText("Player 2: " + currentPosition);
            } else {
                    aiPosition = currentPosition;
                aiLabel.setText("AI: " + currentPosition);
            }
                
                updateBoard();
        }
    });

    movementTimer.setRepeats(true);
    movementTimer.start();
}

private void showMessage(String message) {
    JOptionPane.showMessageDialog(this, message);
}

    private int updatePosition(int position, int diceRoll, int playerNumber) {
    int newPosition = position + diceRoll;

    // Prevent movement beyond 100
    if (newPosition > 100) {
        showMessage((playerNumber == 1 ? "Player 1" : (playerNumber == 2 ? "Player 2" : "AI")) + 
                   " needs an exact roll to reach 100! Staying at " + position);
        return position;
    }

    // Check if landed on a power-up tile - IMPORTANT: This needs to happen BEFORE checking snakes/ladders
    if (powerUpTiles.containsKey(newPosition) && powerUpTiles.get(newPosition)) {
        if (playerNumber == 1) {
            player1PowerUps++;
            player1PowerUpLabel.setText("🎁 Power-ups: " + player1PowerUps);
            showMessage("🎁 Player 1 collected a power-up! You can use it later to add 1-3 spaces to a roll.");
        } else if (playerNumber == 2) {
            player2PowerUps++;
            player2PowerUpLabel.setText("🎁 Power-ups: " + player2PowerUps);
            showMessage("🎁 Player 2 collected a power-up! You can use it later to add 1-3 spaces to a roll.");
        } else {
            aiPowerUps++;
            aiPowerUpLabel.setText("🎁 Power-ups: " + aiPowerUps);
            showMessage("🎁 AI collected a power-up! It can use it later to add 1-3 spaces to a roll.");
        }
        
        // Mark this power-up tile as collected (so it can't be collected again)
        powerUpTiles.put(newPosition, false);
    }

    if (newPosition == 11 || newPosition == 44) {
            int teleportSteps = random.nextInt(6) + 5;
        newPosition += teleportSteps;

        if (newPosition > 100) newPosition = 100;

            showMessage("🚀 " + (playerNumber == 1 ? "Player 1" : (playerNumber == 2 ? "Player 2" : "AI")) + " landed on a teleport! Moving forward by " + 
                    teleportSteps + " steps to " + newPosition);
    }

    if (snakes.containsKey(newPosition)) {
        int snakeBitePosition = snakes.get(newPosition);
            showMessage("Oh no! A snake bites " + (playerNumber == 1 ? "Player 1" : (playerNumber == 2 ? "Player 2" : "AI")) + "! 🐍 Moving back to " + snakeBitePosition);
        newPosition = snakeBitePosition;
    } else if (ladders.containsKey(newPosition)) {
        int ladderTop = ladders.get(newPosition);
            showMessage((playerNumber == 1 ? "Player 1" : (playerNumber == 2 ? "Player 2" : "AI")) + " found a ladder! 🪜 Climbing up to " + ladderTop);
        newPosition = ladderTop;
    }

    if (newPosition == 29 || newPosition == 82) {
            if (playerNumber == 1) {
            skipTurnPlayer = true;
                showMessage("⛔ Trap! Player 1 will miss their next turn.");
            } else if (playerNumber == 2) {
                skipTurnPlayer2 = true;
                showMessage("⛔ Trap! Player 2 will miss their next turn.");
        } else {
            skipTurnAI = true;
                showMessage("🤖 AI landed on a trap! It will miss its next turn.");
        }
    }

    return newPosition;
}

private boolean checkWin() {
        if (playerPosition >= 100) {
            JOptionPane.showMessageDialog(this, "Congratulations! 🎉 Player 1 won the game!");
            resetGame();
            return true;
        } else if (player2Position >= 100) {
            JOptionPane.showMessageDialog(this, "Congratulations! 🎉 Player 2 won the game!");
        resetGame();
        return true;
    } else if (aiPosition >= 100) {
        JOptionPane.showMessageDialog(this, "AI won! 🤖 Better luck next time!");
        resetGame();
        return true;
    }
    return false;
}
    
private void resetGame() {
    playerPosition = 1;
        player2Position = 1;
    aiPosition = 1;
    
    skipTurnPlayer = false;
        skipTurnPlayer2 = false;
    skipTurnAI = false;
    
        playerTurn = true;
        player2Turn = false;
        aiTurn = false;
    
        playerLabel.setText("Player 1: " + playerPosition);
        player2Label.setText("Player 2: " + player2Position);
    aiLabel.setText("AI: " + aiPosition);
        turnLabel.setText("Player 1's Turn 🧑");
    diceResultLabel.setText("Dice: 🎲 -");
    
        updateBoard();
        
    rollDiceButton.setEnabled(true);
        
        // Reset power-ups
        player1PowerUps = 0;
        player2PowerUps = 0;
        aiPowerUps = 0;
        player1PowerUpLabel.setText("🎁 Power-ups: 0");
        player2PowerUpLabel.setText("🎁 Power-ups: 0");
        aiPowerUpLabel.setText("🎁 Power-ups: 0");
    }

    private void offerPowerUp(int playerNumber, int diceRoll) {
        String playerName = playerNumber == 1 ? "Player 1" : (playerNumber == 2 ? "Player 2" : "AI");
        int currentPosition = playerNumber == 1 ? playerPosition : (playerNumber == 2 ? player2Position : aiPosition);
        int powerUps = playerNumber == 1 ? player1PowerUps : (playerNumber == 2 ? player2PowerUps : aiPowerUps);
        
        // If no power-ups or AI's turn with no strategic benefit, proceed normally
        if (powerUps == 0 || (playerNumber == 3 && !shouldAIUsePowerUp(diceRoll))) {
            updateGameState(playerNumber, diceRoll);
            return;
        }
        
        // For human players, show dialog
        if (playerNumber == 1 || playerNumber == 2) {
            // Create options for power-up usage
            String[] options = {"Use +1", "Use +2", "Use +3", "Don't use power-up"};
            
            // Show logical analysis to help player decide
            StringBuilder analysis = new StringBuilder("Logical Analysis (Modus Ponens):\n\n");
            
            // Regular move analysis
            int regularPosition = currentPosition + diceRoll;
            analysis.append("If you don't use a power-up:\n");
            appendPositionAnalysis(analysis, regularPosition);
            
            // Power-up move analysis
            for (int i = 1; i <= 3; i++) {
                int powerUpPosition = currentPosition + diceRoll + i;
                if (powerUpPosition <= 100) {
                    analysis.append("\nIf you use +" + i + " power-up:\n");
                    appendPositionAnalysis(analysis, powerUpPosition);
                }
            }
            
            // Show dialog with analysis and options
            int choice = JOptionPane.showOptionDialog(
                this, 
                analysis.toString(),
                playerName + " - Use Power-Up?", 
                JOptionPane.DEFAULT_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                null, 
                options, 
                options[3]
            );
            
            // Process choice
            if (choice >= 0 && choice <= 2) {
                // Use power-up
                int extraSpaces = choice + 1;
                if (playerNumber == 1) {
                    player1PowerUps--;
                    player1PowerUpLabel.setText("🎁 Power-ups: " + player1PowerUps);
                } else {
                    player2PowerUps--;
                    player2PowerUpLabel.setText("🎁 Power-ups: " + player2PowerUps);
                }
                showMessage(playerName + " used a +" + extraSpaces + " power-up!");
                updateGameState(playerNumber, diceRoll + extraSpaces);
            } else {
                // Don't use power-up
                updateGameState(playerNumber, diceRoll);
            }
        } 
        // For AI, use game theory to decide
        else {
            int aiDecision = getAIPowerUpDecision(diceRoll);
            if (aiDecision > 0) {
                aiPowerUps--;
                aiPowerUpLabel.setText("🎁 Power-ups: " + aiPowerUps);
                showMessage("🤖 AI used a +" + aiDecision + " power-up based on game theory analysis!");
                
                // Show AI's reasoning occasionally
                if (random.nextInt(3) == 0) {
                    showAIReasoning(diceRoll, aiDecision);
                }
                
                updateGameState(playerNumber, diceRoll + aiDecision);
            } else {
                updateGameState(playerNumber, diceRoll);
            }
        }
    }

    private void appendPositionAnalysis(StringBuilder analysis, int position) {
        if (position > 100) {
            analysis.append("  • You would exceed 100 and stay in place.\n");
            return;
        }
        
        analysis.append("  • You would land on position " + position + ".\n");
        
        if (snakes.containsKey(position)) {
            analysis.append("  • If you land on " + position + ", you will slide down to " + 
                          snakes.get(position) + ".\n");
            analysis.append("  • Therefore, you would end up at position " + snakes.get(position) + ".\n");
        } else if (ladders.containsKey(position)) {
            analysis.append("  • If you land on " + position + ", you will climb up to " + 
                          ladders.get(position) + ".\n");
            analysis.append("  • Therefore, you would end up at position " + ladders.get(position) + ".\n");
        } else if (traps.containsKey(position)) {
            analysis.append("  • If you land on " + position + ", you will skip your next turn.\n");
        } else if (teleports.containsKey(position)) {
            analysis.append("  • If you land on " + position + ", you will teleport forward 5-10 spaces.\n");
        } else if (powerUpTiles.containsKey(position)) {
            analysis.append("  • If you land on " + position + ", you will collect another power-up.\n");
        }
    }

    private boolean shouldAIUsePowerUp(int diceRoll) {
        if (aiPowerUps == 0) return false;
        
        // Calculate value of using vs. not using power-up
        int regularPosition = aiPosition + diceRoll;
        double regularMoveValue = evaluatePosition(regularPosition);
        
        // Check values for using different power-up amounts
        double bestPowerUpValue = regularMoveValue;
        int bestPowerUp = 0;
        
        for (int i = 1; i <= 3; i++) {
            int newPos = aiPosition + diceRoll + i;
            if (newPos <= 100) {
                double value = evaluatePosition(newPos);
                if (value > bestPowerUpValue) {
                    bestPowerUpValue = value;
                    bestPowerUp = i;
                }
            }
        }
        
        // Debug message to see what's happening
        System.out.println("AI Decision Analysis:");
        System.out.println("Regular move to " + regularPosition + " has value " + regularMoveValue);
        System.out.println("Best power-up move has value " + bestPowerUpValue);
        
        // Make AI more aggressive in using power-ups
        // Use power-up if:
        // 1. It provides ANY improvement, or
        // 2. Regular move lands on a snake, or
        // 3. AI is close to winning (position > 90)
        boolean shouldUse = bestPowerUpValue > regularMoveValue || 
                            snakes.containsKey(regularPosition) ||
                            (aiPosition + diceRoll + bestPowerUp > 90 && bestPowerUp > 0);
        
        System.out.println("AI will use power-up: " + shouldUse);
        return shouldUse;
    }

    private int getAIPowerUpDecision(int diceRoll) {
        if (aiPowerUps == 0) return 0;
        
        int bestPowerUp = 0;
        double bestValue = evaluatePosition(aiPosition + diceRoll);
        
        System.out.println("AI Power-Up Analysis for position " + aiPosition + " with roll " + diceRoll);
        System.out.println("Regular move value: " + bestValue);
        
        // Evaluate each power-up option
        for (int i = 1; i <= 3; i++) {
            int newPos = aiPosition + diceRoll + i;
            if (newPos <= 100) {
                double value = evaluatePosition(newPos);
                System.out.println("Power-up +" + i + " to position " + newPos + " has value " + value);
                
                if (value > bestValue) {
                    bestValue = value;
                    bestPowerUp = i;
                }
            }
        }
        
        System.out.println("AI chose power-up: +" + bestPowerUp);
        return bestPowerUp;
    }

    private double evaluatePosition(int position) {
        if (position > 100) return 0; // Invalid position
        
        // Base value is progress toward goal (0-1)
        double value = position / 100.0;
        
        // Adjust for game elements - INCREASE the impact of these adjustments
        if (snakes.containsKey(position)) {
            // Landing on snake is VERY bad
            value = snakes.get(position) / 100.0 - 0.3; // Bigger penalty
        } else if (ladders.containsKey(position)) {
            // Landing on ladder is VERY good
            value = ladders.get(position) / 100.0 + 0.3; // Bigger bonus
        } else if (traps.containsKey(position)) {
            // Traps are bad
            value -= 0.25;
        } else if (teleports.containsKey(position)) {
            // Teleports are good
            value += 0.2;
        } else if (powerUpTiles.containsKey(position) && powerUpTiles.get(position)) {
            // Power-ups are very good (only if not already collected)
            value += 0.3;
        }
        
        // Consider proximity to winning - make this more significant
        if (position > 90) {
            value += 0.3; // Much higher value for being close to winning
        } else if (position > 80) {
            value += 0.15; // Higher value for being somewhat close
        }
        
        // Consider proximity to opponents
        int distanceToPlayer1 = Math.abs(position - playerPosition);
        int distanceToPlayer2 = Math.abs(position - player2Position);
        
        // Slightly prefer positions away from opponents to avoid competition
        if (distanceToPlayer1 < 3 || distanceToPlayer2 < 3) {
            value -= 0.05;
        }
        
        return value;
    }

    private void showAIReasoning(int diceRoll, int powerUpUsed) {
        StringBuilder reasoning = new StringBuilder("🤖 AI's Game Theory Analysis:\n\n");
        
        int regularPosition = aiPosition + diceRoll;
        int powerUpPosition = regularPosition + powerUpUsed;
        
        // Regular move analysis
        reasoning.append("Without power-up:\n");
        reasoning.append("• Landing on position " + regularPosition + "\n");
        double regularValue = evaluatePosition(regularPosition);
        reasoning.append("• Value: " + String.format("%.2f", regularValue) + "\n\n");
        
        // Power-up move analysis
        reasoning.append("With +" + powerUpUsed + " power-up:\n");
        reasoning.append("• Landing on position " + powerUpPosition + "\n");
        double powerUpValue = evaluatePosition(powerUpPosition);
        reasoning.append("• Value: " + String.format("%.2f", powerUpValue) + "\n\n");
        
        // Explain decision
        reasoning.append("Decision: Use +" + powerUpUsed + " power-up\n");
        reasoning.append("Improvement: +" + String.format("%.2f", powerUpValue - regularValue));
        
        // Add specific reasoning based on what's at the destination
        if (ladders.containsKey(powerUpPosition)) {
            reasoning.append("\n\nRationale: Landing on a ladder at position " + 
                           powerUpPosition + " to climb to " + ladders.get(powerUpPosition));
        } else if (snakes.containsKey(regularPosition)) {
            reasoning.append("\n\nRationale: Avoiding a snake at position " + regularPosition);
        } else if (powerUpPosition >= 95) {
            reasoning.append("\n\nRationale: Getting closer to winning position (100)");
        }
        
        JOptionPane.showMessageDialog(this, reasoning.toString(), 
                                    "AI Game Theory Analysis", JOptionPane.INFORMATION_MESSAGE);
    }
}


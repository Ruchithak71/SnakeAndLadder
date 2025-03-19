import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;
import java.awt.BasicStroke;

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
    private boolean player1Finished = false;
    private boolean player2Finished = false;
    private boolean aiFinished = false;
    private int player1Rank = 0;
    private int player2Rank = 0;
    private int aiRank = 0;
    private int currentRank = 1;
   
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
    
        // Initialize power-up tiles - UPDATED to position 26 (not 16)
        powerUpTiles.put(26, true);  // Changed from 16 to 26
        powerUpTiles.put(42, true);
        powerUpTiles.put(78, true);
        
        // Debug output to verify initialization
        System.out.println("Ladders initialized at: 4→25, 13→34, 22→41, 57→76, 72→91");
        System.out.println("Power-ups initialized at: 26, 42, 78");
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
    
                // Highlight player positions with different colors for finished players
                if (cellNumber == playerPosition) {
                    if (player1Finished) {
                        // Use a trophy color for finished players
                        cell.setBackground(new Color(255, 215, 0)); // Gold color
                        cell.setText("P1 🏆" + player1Rank);
                    } else {
                    cell.setBackground(Color.BLUE);
                    }
                } else if (cellNumber == player2Position) {
                    if (player2Finished) {
                        cell.setBackground(new Color(255, 215, 0)); // Gold color
                        cell.setText("P2 🏆" + player2Rank);
                    } else {
                        cell.setBackground(Color.GREEN);
                    }
                } else if (cellNumber == aiPosition) {
                    if (aiFinished) {
                        cell.setBackground(new Color(255, 215, 0)); // Gold color
                        cell.setText("AI 🏆" + aiRank);
                    } else {
                    cell.setBackground(Color.ORANGE);
                    }
                } else if (!powerUpTiles.containsKey(cellNumber) || !powerUpTiles.get(cellNumber)) {
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
        // Skip turn if player has already finished
        if ((playerNumber == 1 && player1Finished) || 
            (playerNumber == 2 && player2Finished) || 
            (playerNumber == 3 && aiFinished)) {
            
            // Move to next player's turn
            if (playerNumber == 1) {
                playerTurn = false;
                player2Turn = true;
                aiTurn = false;
                turnLabel.setText("Player 2's Turn 👤");
                rollDiceButton.setEnabled(true);
                
                // If Player 2 has also finished, move to AI
                if (player2Finished) {
                    player2Turn = false;
                    aiTurn = true;
                    turnLabel.setText("AI's Turn 🤖");
                    
                    // If AI has also finished, move back to Player 1
                    if (aiFinished) {
                        aiTurn = false;
                        playerTurn = true;
                        turnLabel.setText("Player 1's Turn 🧑");
                    } else {
                        // AI's turn
                        Timer aiTimer = new Timer(1500, evt -> rollDice(3));
                        aiTimer.setRepeats(false);
                        aiTimer.start();
                    }
                }
            } else if (playerNumber == 2) {
                playerTurn = false;
                player2Turn = false;
                aiTurn = true;
                turnLabel.setText("AI's Turn 🤖");
                
                // If AI has also finished, move to Player 1
                if (aiFinished) {
                    aiTurn = false;
                    playerTurn = true;
                    turnLabel.setText("Player 1's Turn 🧑");
                    rollDiceButton.setEnabled(true);
                } else {
                    // AI's turn
                    Timer aiTimer = new Timer(1500, evt -> rollDice(3));
                    aiTimer.setRepeats(false);
                    aiTimer.start();
                }
            } else { // AI's turn
                playerTurn = true;
                player2Turn = false;
                aiTurn = false;
                turnLabel.setText("Player 1's Turn 🧑");
                rollDiceButton.setEnabled(true);
                
                // If Player 1 has also finished, move to Player 2
                if (player1Finished) {
                    playerTurn = false;
                    player2Turn = true;
                    turnLabel.setText("Player 2's Turn 👤");
                    
                    // If Player 2 has also finished, all players are done
                    if (player2Finished) {
                        // This shouldn't happen as the game should have ended
                        checkWin();
                    }
                }
            }
            
            return;
        }

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
            // Update player position
            if (playerNumber == 1) {
            playerPosition = newPosition;
                playerLabel.setText("Player 1: " + playerPosition);
                
                // Check if Player 1 won
                boolean won = checkWin();
                
                // If Player 1 didn't win or we're continuing for rankings
                if (!won || (won && !player2Finished && !aiFinished)) {
                    playerTurn = false;
                    player2Turn = true;
                    aiTurn = false;
                    turnLabel.setText("Player 2's Turn 👤");
                    rollDiceButton.setEnabled(true);
                    
                    // Skip Player 2 if already finished
                    if (player2Finished) {
                        player2Turn = false;
                        aiTurn = true;
                        turnLabel.setText("AI's Turn 🤖");
                        
                        // Skip AI if already finished
                        if (aiFinished) {
                            aiTurn = false;
                            playerTurn = true;
                            turnLabel.setText("Player 1's Turn 🧑");
                        } else {
                            // AI's turn
                            Timer aiTimer = new Timer(1500, evt -> rollDice(3));
                            aiTimer.setRepeats(false);
                            aiTimer.start();
                        }
                    }
                }
            } else if (playerNumber == 2) {
                player2Position = newPosition;
                player2Label.setText("Player 2: " + player2Position);
                
                // Check if Player 2 won
                boolean won = checkWin();
                
                // If Player 2 didn't win or we're continuing for rankings
                if (!won || (won && !player1Finished && !aiFinished)) {
            playerTurn = false;
                    player2Turn = false;
                    aiTurn = true;
            turnLabel.setText("AI's Turn 🤖");
            rollDiceButton.setEnabled(false);

                    // Skip AI if already finished
                    if (aiFinished) {
                        aiTurn = false;
                        playerTurn = true;
                        turnLabel.setText("Player 1's Turn 🧑");
                        rollDiceButton.setEnabled(true);
                        
                        // Skip Player 1 if already finished
                        if (player1Finished) {
                            playerTurn = false;
                            player2Turn = true;
                            turnLabel.setText("Player 2's Turn 👤");
                        }
                    } else {
            // AI takes its turn after a delay
                        Timer aiTimer = new Timer(1500, evt -> rollDice(3));
            aiTimer.setRepeats(false);
            aiTimer.start();
                    }
                }
            } else { // AI's turn
            aiPosition = newPosition;
            aiLabel.setText("AI: " + aiPosition);
                
                // Check if AI won
                boolean won = checkWin();
                
                // If AI didn't win or we're continuing for rankings
                if (!won || (won && !player1Finished && !player2Finished)) {
            playerTurn = true;
                    player2Turn = false;
                    aiTurn = false;
                    turnLabel.setText("Player 1's Turn 🧑");
            rollDiceButton.setEnabled(true);

                    // Skip Player 1 if already finished
                    if (player1Finished) {
                        playerTurn = false;
                        player2Turn = true;
                        turnLabel.setText("Player 2's Turn 👤");
                        
                        // Skip Player 2 if already finished
                        if (player2Finished) {
                            player2Turn = false;
                            aiTurn = true;
                            turnLabel.setText("AI's Turn 🤖");
                            
                            // This shouldn't happen as the game should have ended
            checkWin();
        }
                    }
                }
            }
            
            updatePlayerLabels();
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
        String playerName = playerNumber == 1 ? "Player 1" : (playerNumber == 2 ? "Player 2" : "AI");

        // Prevent movement beyond 100
    if (newPosition > 100) {
            showMessage(playerName + " needs an exact roll to reach 100! Staying at " + position);
            return position;
    }

        // Handle teleport on tile 11 and 44 - FIXED +10 STEPS
    if (newPosition == 11 || newPosition == 44) {
            // Always move exactly 10 steps forward when landing on a teleport
            int teleportSteps = 10; // Fixed at exactly 10 steps
        newPosition += teleportSteps;

        // Ensure teleport does not exceed 100
        if (newPosition > 100) newPosition = 100;

            showMessage("🚀 " + playerName + " landed on a teleport! Moving forward by " + 
                    teleportSteps + " steps to " + newPosition);
            
            // Debug output to verify teleport movement
            System.out.println(playerName + " teleported from " + (newPosition - teleportSteps) + 
                              " to " + newPosition + " (+10 steps)");
    }

    // Check for snakes and ladders (AFTER teleport)
    if (snakes.containsKey(newPosition)) {
        int snakeBitePosition = snakes.get(newPosition);
            showMessage("Oh no! A snake bites " + playerName + "! 🐍 Moving back to " + snakeBitePosition);
        newPosition = snakeBitePosition;
    } else if (ladders.containsKey(newPosition)) {
        int ladderTop = ladders.get(newPosition);
            showMessage(playerName + " found a ladder! 🪜 Climbing up to " + ladderTop);
        newPosition = ladderTop;
    }

    // Handle trap on tile 29 and 82 (Skip next turn)
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

        // Check if landed on a power-up tile
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

    return newPosition;
}

    private boolean checkWin() {
        boolean someoneWon = false;
        
        // Check if Player 1 reached 100 and hasn't been ranked yet
        if (playerPosition >= 100 && !player1Finished) {
            player1Finished = true;
            player1Rank = currentRank;
            currentRank++;
            
            JOptionPane.showMessageDialog(this, 
                "Player 1 finished in position " + player1Rank + "! 🎉");
            someoneWon = true;
        }
        
        // Check if Player 2 reached 100 and hasn't been ranked yet
        if (player2Position >= 100 && !player2Finished) {
            player2Finished = true;
            player2Rank = currentRank;
            currentRank++;
            
            JOptionPane.showMessageDialog(this, 
                "Player 2 finished in position " + player2Rank + "! 🎉");
            someoneWon = true;
        }
        
        // Check if AI reached 100 and hasn't been ranked yet
        if (aiPosition >= 100 && !aiFinished) {
            aiFinished = true;
            aiRank = currentRank;
            currentRank++;
            
            JOptionPane.showMessageDialog(this, 
                "AI finished in position " + aiRank + "! 🤖");
            someoneWon = true;
        }
        
        // Check if all players have finished
        if (player1Finished && player2Finished && aiFinished) {
            // Show final rankings
            StringBuilder results = new StringBuilder("Final Rankings:\n\n");
            
            // Find who got which rank
            String firstPlace = player1Rank == 1 ? "Player 1" : (player2Rank == 1 ? "Player 2" : "AI");
            String secondPlace = player1Rank == 2 ? "Player 1" : (player2Rank == 2 ? "Player 2" : "AI");
            String thirdPlace = player1Rank == 3 ? "Player 1" : (player2Rank == 3 ? "Player 2" : "AI");
            
            results.append("🥇 1st Place: ").append(firstPlace).append("\n");
            results.append("🥈 2nd Place: ").append(secondPlace).append("\n");
            results.append("🥉 3rd Place: ").append(thirdPlace);
            
            JOptionPane.showMessageDialog(this, results.toString(), "Game Complete!", JOptionPane.INFORMATION_MESSAGE);
            
            // Reset the game for a new round
        resetGame();
        return true;
    }
        
        updatePlayerLabels();
        return someoneWon;
}
    
private void resetGame() {
    // Reset player and AI positions
    playerPosition = 1;
        player2Position = 1;
    aiPosition = 1;
    
    // Reset any flags like skip turns
    skipTurnPlayer = false;
        skipTurnPlayer2 = false;
    skipTurnAI = false;
        
        // Reset turn order
        playerTurn = true;
        player2Turn = false;
        aiTurn = false;
        
        // Reset power-ups
        player1PowerUps = 0;
        player2PowerUps = 0;
        aiPowerUps = 0;
        player1PowerUpLabel.setText("🎁 Power-ups: 0");
        player2PowerUpLabel.setText("🎁 Power-ups: 0");
        aiPowerUpLabel.setText("🎁 Power-ups: 0");
        
        // IMPORTANT: Reset power-up tiles to be available again
        for (Integer tilePosition : powerUpTiles.keySet()) {
            powerUpTiles.put(tilePosition, true);
        }
        
        // Reset player rankings
        player1Finished = false;
        player2Finished = false;
        aiFinished = false;
        player1Rank = 0;
        player2Rank = 0;
        aiRank = 0;
        currentRank = 1;
    
    // Update UI labels for the initial state
        playerLabel.setText("Player 1: " + playerPosition);
        player2Label.setText("Player 2: " + player2Position);
    aiLabel.setText("AI: " + aiPosition);
        turnLabel.setText("Player 1's Turn 🧑");
    diceResultLabel.setText("Dice: 🎲 -");
        
        // Update the board
        updateBoard();
    
    // Enable the roll dice button for the next round
    rollDiceButton.setEnabled(true);
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
        
        // Calculate current positions and distances
        int regularPosition = aiPosition + diceRoll;
        int maxPlayerPosition = Math.max(playerPosition, player2Position);
        int distanceToLeader = Math.max(playerPosition - aiPosition, player2Position - aiPosition);
        
        // Don't use power-up if AI is leading and no immediate threats
        if (aiPosition > maxPlayerPosition && !snakes.containsKey(regularPosition)) {
            // Save power-up unless very close to winning
            if (aiPosition + diceRoll < 95) {
                System.out.println("AI is leading, saving power-up for critical situations");
                return false;
            }
        }
        
        // Check if any player is close to winning (position > 90)
        boolean playersNearWin = playerPosition > 90 || player2Position > 90;
        
        // Calculate value of using vs. not using power-up
        double regularMoveValue = evaluatePosition(regularPosition);
        double bestPowerUpValue = regularMoveValue;
        int bestPowerUp = 0;
        
        // Analyze each power-up option
        for (int i = 1; i <= 3; i++) {
            int newPos = aiPosition + diceRoll + i;
            if (newPos <= 100) {
                double value = evaluatePosition(newPos);
                // Increase value if position leads to immediate benefits
                if (ladders.containsKey(newPos) || teleports.containsKey(newPos)) {
                    value += 0.3; // Significant bonus for immediate advantages
                }
                if (value > bestPowerUpValue) {
                    bestPowerUpValue = value;
                    bestPowerUp = i;
                }
            }
        }
        
        // Debug information
        System.out.println("AI Decision Analysis:");
        System.out.println("Distance to leader: " + distanceToLeader);
        System.out.println("Players near win: " + playersNearWin);
        System.out.println("Regular move value: " + regularMoveValue);
        System.out.println("Best power-up value: " + bestPowerUpValue);
        
        // Decision criteria
        boolean shouldUse = false;
        
        // Use power-up if:
        // 1. Players are near winning and AI needs to catch up
        if (playersNearWin && distanceToLeader > 0) {
            shouldUse = true;
        }
        // 2. Regular move leads to a snake
        else if (snakes.containsKey(regularPosition)) {
            shouldUse = true;
        }
        // 3. Power-up leads to significant advantage (ladder/teleport)
        else if (bestPowerUpValue > regularMoveValue + 0.3) {
            shouldUse = true;
        }
        // 4. Very close to winning (>95) and power-up helps
        else if (aiPosition > 95 && bestPowerUpValue > regularMoveValue) {
            shouldUse = true;
        }
        // 5. Significantly behind other players (>20 spaces)
        else if (distanceToLeader > 20 && bestPowerUpValue > regularMoveValue) {
            shouldUse = true;
        }
        
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
        if (position > 100) return 0;
        
        // Base value is progress toward goal (0-1)
        double value = position / 100.0;
        
        // Consider distance to other players
        int distanceToPlayer1 = playerPosition - position;
        int distanceToPlayer2 = player2Position - position;
        
        // Add urgency if falling behind
        if (distanceToPlayer1 > 20 || distanceToPlayer2 > 20) {
            value += 0.2; // Increase value when significantly behind
        }
        
        // Analyze board elements
        if (snakes.containsKey(position)) {
            value = snakes.get(position) / 100.0 - 0.4; // Bigger snake penalty
        } else if (ladders.containsKey(position)) {
            value = ladders.get(position) / 100.0 + 0.4; // Bigger ladder bonus
        } else if (traps.containsKey(position)) {
            value -= 0.3; // Bigger trap penalty
        } else if (teleports.containsKey(position)) {
            value += 0.25; // Significant teleport bonus
        } else if (powerUpTiles.containsKey(position) && powerUpTiles.get(position)) {
            // Value power-ups more when behind or near win
            boolean isBehind = position < playerPosition || position < player2Position;
            boolean isNearWin = position > 80;
            if (isBehind || isNearWin) {
                value += 0.35;
            } else {
                value += 0.2;
            }
        }
        
        // Progressive scoring for end-game positions
        if (position > 95) {
            value += 0.5; // Very high value for being close to winning
        } else if (position > 90) {
            value += 0.4;
        } else if (position > 80) {
            value += 0.2;
        }
        
        // Consider relative position to other players
        boolean isLeading = position > playerPosition && position > player2Position;
        if (isLeading) {
            value += 0.1; // Bonus for maintaining lead
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

    private void updatePlayerLabels() {
        if (player1Finished) {
            playerLabel.setText("Player 1: " + playerPosition + " (Rank: " + player1Rank + ")");
        } else {
            playerLabel.setText("Player 1: " + playerPosition);
        }
        
        if (player2Finished) {
            player2Label.setText("Player 2: " + player2Position + " (Rank: " + player2Rank + ")");
        } else {
            player2Label.setText("Player 2: " + player2Position);
        }
        
        if (aiFinished) {
            aiLabel.setText("AI: " + aiPosition + " (Rank: " + aiRank + ")");
        } else {
            aiLabel.setText("AI: " + aiPosition);
        }
    }

    // Add a method to create a fallback star image if the image file is missing
    private ImageIcon createFallbackStarImage() {
        // Create a 40x40 pixel image with a transparent background
        BufferedImage starImage = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = starImage.createGraphics();
        
        // Enable anti-aliasing for smoother edges
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw a gold star
        int[] xPoints = {20, 14, 0, 8, 3, 20, 37, 32, 40, 26, 20};
        int[] yPoints = {0, 13, 13, 22, 40, 30, 40, 22, 13, 13, 0};
        
        // Fill with gold color
        g2d.setColor(new Color(255, 215, 0));  // Gold color
        g2d.fillPolygon(xPoints, yPoints, xPoints.length);
        
        // Add a darker gold border
        g2d.setColor(new Color(218, 165, 32));  // Darker gold
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawPolygon(xPoints, yPoints, xPoints.length);
        
        g2d.dispose();
        return new ImageIcon(starImage);
    }
}


import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;
import java.util.HashSet;
import java.util.ArrayList;

public class WordFilter {
    JFrame frame = new JFrame("Tag Extractor");
    JButton fileChooser = new JButton("Choose File");
    JButton saveButton = new JButton("Save Words");
    JButton clearButton = new JButton("Clear");
    JButton quitButton = new JButton("Quit");
    JTextArea textArea = new JTextArea(20, 40);
    JScrollPane textScrollBar = new JScrollPane(textArea);
    TreeMap<String, Integer> wordCounter = new TreeMap<>();
    HashSet<String> stopWords = new HashSet<>();
    String fileContent = "";
    File selectedFile;

    public WordFilter() {
        // Load stop words from file
        loadStopWords();

        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        fileChooser.addActionListener(e -> {
            JFileChooser jFileChooser = new JFileChooser();
            int result = jFileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile = jFileChooser.getSelectedFile();
                fileContent = readFile(selectedFile);
                
                // Process the file content
                wordCounter.clear();
                String[] tokens = fileContent.split("\\s+");
                for (String token : tokens) {
                    String word = token.toLowerCase().replaceAll("^[\\p{Punct}]+|[\\p{Punct}]+$", "");
                    if (word.isEmpty()) {
                        continue;
                    }
                    if (!stopWords.contains(word)) {
                        wordCounter.put(word, wordCounter.getOrDefault(word, 0) + 1);
                    }
                }
                
                // Display the unique words with their counts in the text area    
                StringBuilder uniqueWords = new StringBuilder();
                
                // Sort by frequency (value) in descending order
                ArrayList<String> sortedWords = new ArrayList<>(wordCounter.keySet());
                sortedWords.sort((word1, word2) -> wordCounter.get(word2).compareTo(wordCounter.get(word1)));
                
                for (String word : sortedWords) {
                    uniqueWords.append(word).append(": ").append(wordCounter.get(word)).append("\n");
                }
                textArea.setText(uniqueWords.toString());
                
                // Enable the save button now that words are displayed
                saveButton.setEnabled(true);
            }
        });

        saveButton.setEnabled(false);
        saveButton.addActionListener(e -> {
            JFileChooser saveChooser = new JFileChooser();
            saveChooser.setDialogTitle("Save filtered words to...");
            int saveResult = saveChooser.showSaveDialog(frame);
            if (saveResult == JFileChooser.APPROVE_OPTION) {
                File outputFile = saveChooser.getSelectedFile();
                saveWordsToFile(outputFile, wordCounter);
                JOptionPane.showMessageDialog(frame, "Words saved successfully to:\n" + outputFile.getAbsolutePath());
            }
        });

        quitButton.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(frame, "Are you sure you want to quit?", "Quit Confirmation", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        clearButton.addActionListener(e -> {
            textArea.setText("");
            wordCounter.clear();
            saveButton.setEnabled(false);
        });

        // Add components to the frame
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(fileChooser);
        buttonPanel.add(saveButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(quitButton);
        frame.add(buttonPanel, BorderLayout.NORTH);
        frame.add(textScrollBar, BorderLayout.CENTER);

        // Make the frame visible
        frame.setVisible(true);
    }

    private void loadStopWords() {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/English Stop Words.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stopWords.add(line.toLowerCase().trim());
            }
        } catch (IOException e) {
            System.err.println("Error loading stop words: " + e.getMessage());
        }
    }

    private String readFile(File file) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return content.toString();
    }

    private ArrayList<String> getWordsSortedByFrequencyDesc(TreeMap<String, Integer> words) {
        ArrayList<String> sortedWords = new ArrayList<>(words.keySet());
        sortedWords.sort((word1, word2) -> words.get(word2).compareTo(words.get(word1)));
        return sortedWords;
    }

    private void saveWordsToFile(File file, TreeMap<String, Integer> words) {
        try (FileWriter writer = new FileWriter(file)) {
            // Sort by frequency (value) in descending order
            ArrayList<String> sortedWords = getWordsSortedByFrequencyDesc(words);

            for (String word : sortedWords) {
                writer.write(word + ": " + words.get(word) + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
            JOptionPane.showMessageDialog(frame, "Error saving file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WordFilter::new);
    }
}

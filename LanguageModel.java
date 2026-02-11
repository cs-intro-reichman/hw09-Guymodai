import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
    private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     * seed value. Generating texts from this model multiple times with the 
     * same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
    public void train(String fileName) {
        String window = "";
        char c;
        In in = new In(fileName);
        // Reads just enough characters to form the first window. [cite: 380]
        for (int i = 0; i < windowLength; i++) {
            if (!in.isEmpty()) {
                window += in.readChar();
            }
        }
        // Processes the entire text, one character at a time [cite: 382]
        while (!in.isEmpty()) {
            // Gets the next character [cite: 384]
            c = in.readChar();
            // Checks if the window is already in the map [cite: 387]
            // tries to get the list of this window from the map. [cite: 388]
            // Let's call the retrieved list "probs" (it may be null) [cite: 389]
            List probs = CharDataMap.get(window);
            // If the window was not found in the map [cite: 390]
            if (probs == null) {
                // Creates a new empty list, and adds (window, list) to the map [cite: 393]
                // Let's call the newly created list "probs" [cite: 395]
                probs = new List();
                CharDataMap.put(window, probs);
            }
            // Calculates the counts of the current character. [cite: 396]
            probs.update(c);
            // Advances the window: adds c to the window's end, and deletes the [cite: 398]
            // window's first character. [cite: 398]
            window = window.substring(1) + c;
        }
        // The entire file has been processed, and all the characters have been counted. [cite: 400]
        // Proceeds to compute and set the p and cp fields of all the CharData objects [cite: 401]
        // in each linked list in the map. [cite: 402]
        for (List probs : CharDataMap.values()) {
            calculateProbabilities(probs);
        }
    }

    // Computes and sets the probabilities (p and cp fields) of all the
    // characters in the given list. */ [cite: 120, 121]
    void calculateProbabilities(List probs) { 
        int totalCount = 0;
        for (int i = 0; i < probs.getSize(); i++) {
            totalCount += probs.get(i).count;
        }
        int cumulativeCount = 0;
        for (int i = 0; i < probs.getSize(); i++) {
            CharData cd = probs.get(i);
            cd.p = (double) cd.count / totalCount;
            cumulativeCount += cd.count;
            cd.cp = (double) cumulativeCount / totalCount;
        }
    }

    // Returns a random character from the given probabilities list. [cite: 144]
    char getRandomChar(List probs) {
        double r = randomGenerator.nextDouble();
        for (int i = 0; i < probs.getSize(); i++) {
            CharData cd = probs.get(i);
            if (cd.cp > r) {
                return cd.chr;
            }
        }
        return probs.get(probs.getSize() - 1).chr;
    }

    /**
     * Generates a random text, based on the probabilities that were learned during training. 
     * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
     * doesn't appear as a key in Map, we generate no text and return only the initial text. [cite: 209]
     * @param textLength - the size of text to generate [cite: 232]
     * @return the generated text
     */
    public String generate(String initialText, int textLength) {
        if (initialText.length() < windowLength) {
            return initialText;
        }
        
        StringBuilder generatedText = new StringBuilder(initialText);
        String window = initialText.substring(initialText.length() - windowLength);
        
        while (generatedText.length() < textLength) {
            List probs = CharDataMap.get(window);
            if (probs == null) {
                break;
            }
            char nextChar = getRandomChar(probs);
            generatedText.append(nextChar);
            window = generatedText.substring(generatedText.length() - windowLength);
        }
        
        return generatedText.toString();
    }

    /** Returns a string representing the map of this language model. */
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (String key : CharDataMap.keySet()) {
            List keyProbs = CharDataMap.get(key);
            str.append(key + " : " + keyProbs + "\n");
        }
        return str.toString();
    }

    public static void main(String[] args) {
        int windowLength = Integer.parseInt(args[0]);
        String initialText = args[1];
        int generatedTextLength = Integer.parseInt(args[2]);
        Boolean randomGeneration = args[3].equals("random");
        String fileName = args[4];
        // Create the LanguageModel object [cite: 271]
        LanguageModel lm;
        if (randomGeneration) {
            lm = new LanguageModel(windowLength);
        } else {
            lm = new LanguageModel(windowLength, 20);
        }
        // Trains the model, creating the map. [cite: 276]
        lm.train(fileName);
        // Generates text, and prints it. [cite: 277]
        System.out.println(lm.generate(initialText, generatedTextLength));
    }
}
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
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
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
        for(int i = 0; i<this.windowLength; i++)
        {
            c = in.readChar();
            window += c;
        }
        while(!in.isEmpty())
        {
            c = in.readChar();
            List probs = this.CharDataMap.get(window);
            if(probs == null)
            {
                probs = new List();
                this.CharDataMap.put(window,probs);
            }

            probs.update(c);
            window = window.substring(1)+c;
        }
        for(List probs : this.CharDataMap.values()){
            calculateProbabilities(probs);
        }
        

    }
        
  

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	public void calculateProbabilities(List probs) {				
		ListIterator round = probs.listIterator(0);
        int count = 0;
        while(round.hasNext())
        {
            CharData node = (CharData)(round.next());
            count += node.count;
        }
        round = probs.listIterator(0);
        double countP = 0.0;
        while(round.hasNext())
        {
            CharData node = (CharData)(round.next());
            node.p = ((double)(node.count))/count;
            countP += node.p;
            node.cp = countP;
        }
        
	}

    // Returns a random character from the given probabilities list.
	public char getRandomChar(List probs) {
		double rnd = randomGenerator.nextDouble();
        ListIterator round = probs.listIterator(0);
        while(round.hasNext())
        {
            CharData node = (CharData)(round.next());
            if(rnd < node.cp)
                return node.chr;
        }
        return probs.get(probs.getSize()-1).chr;
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
		
        if(initialText.length()<this.windowLength)
            return initialText;
        int firstLength = initialText.length();
        String window = initialText.substring(firstLength - this.windowLength);
        while(initialText.length()<textLength + firstLength)
        {
            List probs = this.CharDataMap.get(window);
            if(probs == null)
                return initialText;
            char current = getRandomChar(probs);
            initialText += current;
            window = window.substring(1) + current;
        }
        return initialText;

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
        // Create the LanguageModel object 
        LanguageModel lm; 
        if (randomGeneration) 
            lm = new LanguageModel(windowLength); 
        else 
            lm = new LanguageModel(windowLength, 20); 
        // Trains the model, creating the map. 
        lm.train(fileName); 
        // Generates text, and prints it. 
        System.out.println(lm.generate(initialText, generatedTextLength)); 
    }
}

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.enseeiht.danck.voice_analyzer.*;
import fr.enseeiht.danck.voice_analyzer.defaults.*;

public class TestVoiceAnalyzer {

	// Liste des mots cles reconnus
	// false == n'est pas enregistre dans la base de donnees
	// true == enregistre dans la base de donnees
	private static void initTokens() {
        TokenDefault[] tokens = new TokenDefault[14];
        int i = 0;
        tokens[i++] = new TokenDefault("XXX", "Inconnu", false);
        tokens[i++] = new TokenDefault("SILENCE", "Silence", false);
        tokens[i++] = new TokenDefault("DANCK", "DANCK!", true);
        tokens[i++] = new TokenDefault("STANDBY", "Arrête-toi", true);
        tokens[i++] = new TokenDefault("FORWARD", "Avance", true);
        tokens[i++] = new TokenDefault("BACKWARD", "Recule", true);
        tokens[i++] = new TokenDefault("FLIP", "Fais un flip", true);
        tokens[i++] = new TokenDefault("ROTATE_LEFT", "Tourne à gauche", true);
        tokens[i++] = new TokenDefault("ROTATE_RIGHT", "Tourne à droite", true);
        tokens[i++] = new TokenDefault("HOVER_LEFT", "Gauche", true);
        tokens[i++] = new TokenDefault("HOVER_RIGHT", "Droite", true);
        tokens[i++] = new TokenDefault("INVERTED_PENDULUM", "Pendule inversé", true);
        tokens[i++] = new TokenDefault("SEQUENCE", "Séquence", true);
        tokens[i] = new TokenDefault("SEQUENCE_NAME", "Nom de séquence", false);
        TokenDefault.setValues(tokens);
    }

	// Liste des ordres associes au mots cles
    private static void initOrders() {
        OrderDefault[] orders = new OrderDefault[10];
        int i = 0;
        orders[i++] = new OrderDefault("STANDBY", "Arrête-toi");
        orders[i++] = new OrderDefault("FORWARD", "Avance");
        orders[i++] = new OrderDefault("BACKWARD", "Recule");
        orders[i++] = new OrderDefault("FLIP", "Fais un flip");
        orders[i++] = new OrderDefault("ROTATE_LEFT", "Tourne à gauche");
        orders[i++] = new OrderDefault("ROTATE_RIGHT", "Tourne à droite");
        orders[i++] = new OrderDefault("HOVER_LEFT", "Gauche");
        orders[i++] = new OrderDefault("HOVER_RIGHT", "Droite");
        orders[i++] = new OrderDefault("INVERTED_PENDULUM", "Pendule inversé");
        orders[i] = new OrderDefault("SEQUENCE", "Séquence");
        OrderDefault.setValues(orders);
    }
    
    // Programme principal
	public static void main(String[] args) throws IOException, InterruptedException,
		UnknownOrderException, VoiceAnalyzerRecordException {
		
		// Les fichiers sons sont stockes sous format csv
		// dans le repertoite /test_res/audio du repertoire de
		// travail
		// Il est possible de rajouter de nouveaux fichiers sons
		// en transformant les .wav en .csv 
		// par ex: sox fichier.wav fichier.csv
		List<String> files = new ArrayList<>();
        String base = "/test_res/audio/";
        files.add(base + "Alpha.csv");   // DANCK
        files.add(base + "Bravo.csv");   // FORWARD
        files.add(base + "Charlie.csv"); // SEQUENCE
        files.add(base + "Delta.csv");   // SEQUENCE_NAME
        
        // Lecture des fichiers sons et construction des fenetres 
        // de sons 
        // Chaque mot est lu et séparé par un silence (bruit aleatoire) 
        MultipleFileWindowMaker voiceMaker = new MultipleFileWindowMaker(files);
        
        DummyMetaTokenListener metaTokenListener= new DummyMetaTokenListener();

        // Initialisation des mots cles et des ordres
        initTokens();
        initOrders();
        
        Parser parser= new ParserDefault();
        
        Sorter.setSorter(new SimpleSorter());
        
        DTWHelper.setDtwHelper(new myDTW());
        MFCCHelper.setMFCCHelper(new myMFCCdistance());
       
        // Connection a la base de donnees qui contient les mots cles
        // avec la MFCC du son associee
        Connector connector = new SQliteConnector("test_res/db.sqlite3");
        
        // Initialisation du voiceAnalyzer
        VoiceAnalyzer voiceAnalyzer = new VoiceAnalyzer(voiceMaker, connector, parser, metaTokenListener);
        voiceAnalyzer.setTimeAfterRecord(200); // Delai (en ms) a attendre avant de determiner si le signal voix est termine
        voiceAnalyzer.setTimeForNothing(1000); // Delai (en ms) pour determiner s'il s'agit d'un silence
        
        // La variable test permet de lancer le test ou remplir la 
        // base de donnees
        boolean test= true;
        
        // Si test == faux on remplit la base de donnees
        // A faire la premiere fois ou le programme est lance 
        if (test == false) {
        	// Les mots sont analyses les uns après les autres 
        	// Le fichier alpha sera associe au mot cle DANCK
        	voiceAnalyzer.record(Token.valueOf("DANCK"));
        	// Le fichier bravo sera associe au mot cle FORWARD
        	voiceAnalyzer.record(Token.valueOf("FORWARD"));
        	// Le fichier charlie sera associe au mot cle SEQUENCE
        	voiceAnalyzer.record(Token.valueOf("SEQUENCE"));
        	// Le fichier delta sera associe au mot cle SEQUENCE_NAME
        	voiceAnalyzer.record(Token.valueOf("SEQUENCE_NAME"), "Delta");
        	// Dans la base de donnees les mots cles seront affectes 
        	// l'utilisateur Speaker1
        	voiceAnalyzer.commit("Speaker1");
        } else {
       
        	// Si test == vrai on analyse la séquence de sons
        	// alpha bravo charlie delta
        	// Le parser reconnait DANCK, un ORDRE ou une SEQUENCE
        	// Si DANCK est reconnu, on lit l'ordre qui suit
        	// Si l'ordre est le mot cle SEQUENCE, on lit le nom de la
        	// SEQUENCE_NAME
        	// Le test par defaut entend alpha et reconnait DANCK
        	// puis entend bravo et reconnait FORWARD et s'arrete
        	// Si on commente la ligne 59 (lecture de bravo), la 
        	// sequence devient alpha charlie delta
        	// Le test entend alpha (DANCK) puis charlie (SEQUENCE)
        	// et enfin delta (SEQUENCE_NAME)
        	String order = "Ordre inconnu";
        	try {
        		order = voiceAnalyzer.nextOrder(new HashMap<String, String>()).toString();
        	} catch (UnknownOrderException ignored) {
        	} finally {
        		System.out.println(order);
        	}
        }
	}

	// Affiche l'ordre entendu
	private static class DummyMetaTokenListener implements MetaTokenListener {
        @Override
        public void signal(MetaToken metaToken) {
            System.out.println(metaToken + " listened");
        }
    }
	
	// Affiche "V" si le signal correspond a un mot
	// ou "_" si c'est un silence
	 private static class DummyVoiceListener implements VoiceListener {
	        @Override
	        public void signal(boolean bool) {
	            System.out.print(bool ? "V" : "_");
	        }
	    }
	 
	 // Decompose les sons entendus en mots et silences
	 private static class SimpleSorter extends SorterDefault {
	        SimpleSorter() {
	            super(new DummyVoiceListener());
	            this.MFCCNumberForSpace = 5;
	        }
	    }
	
}

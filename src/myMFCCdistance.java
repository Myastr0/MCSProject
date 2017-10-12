import fr.enseeiht.danck.voice_analyzer.MFCC;
import fr.enseeiht.danck.voice_analyzer.MFCCHelper;

public class myMFCCdistance extends MFCCHelper {

	@Override
	public float distance(MFCC mfcc1, MFCC mfcc2) {
		// calcule la distance entre 2 MFCC

		if(mfcc1.getLength() != mfcc2.getLength()) {
			throw new Error("Ta daronne j'la tartine");
		}
		float res = 0f;

		for(int i = 0; i < mfcc1.getLength(); i ++) {
			res += Math.abs(mfcc1.getCoef(i) - mfcc2.getCoef(i));
		}

		return res;
	}

	@Override
	public float norm(MFCC mfcc) {
		// retourne la valeur de mesure de la MFCC (coef d'indice 0 dans la MFCC) 
		// cette mesure permet de determiner s'il s'agit d'un mot ou d'un silence
		
		return mfcc.getCoef(0);
	}

	@Override
	public MFCC unnoise(MFCC mfcc, MFCC noise) {
		// supprime le bruit de la MFCC passee en parametre
		// soustrait chaque coef du bruit a chaque coef du la MFCC
		// passee en parametre

		if(mfcc.getLength() != noise.getLength()) {
			throw new Error("Ta daronne j'la tartine");
		}

		float[] newCoef = new float[mfcc.getLength()];

		for(int i = 0; i < mfcc.getLength(); i ++) {
			newCoef[i] = mfcc.getCoef(i) - noise.getCoef(i);
		}


		return new MFCC(newCoef, mfcc.getSignal());

	}

}

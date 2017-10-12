import fr.enseeiht.danck.voice_analyzer.DTWHelper;
import fr.enseeiht.danck.voice_analyzer.Field;

import java.lang.reflect.Array;
import java.util.Vector;


public class myDTW extends DTWHelper {

	private float INF = Float.POSITIVE_INFINITY;

    private myMFCCdistance distance = new myMFCCdistance();

    private int w0, w1, w2;

    @Override
	public float DTWDistance(Field unknown, Field known) {
        // Methode qui calcule le score de la DTW
        // entre 2 ensembles de MFCC

        int I = unknown.getLength();
        int J = known.getLength();

        Float[][] g = new Float[I + 1][J + 1];

        Boolean[][] T = new Boolean[I + 1][J + 1];
        Boolean[][] L = new Boolean[I + 1][J + 1];
        Boolean[][] D = new Boolean[I + 1][J + 1];

        for (int i = 0; i <= I; i++) {

            for (int j = 0; j <= J; j++) {

                g[i][j] = INF;

                T[i][j] = false;
                L[i][j] = false;
                D[i][j] = false;
            }
        }

        g[0][0] = 0f;

        for (int i = 1; i <= I; i++) {
            for (int j = 1; j <= J; j++) {
                float top = g[i - 1][j] + w0 * this.distance.distance(unknown.getMFCC(i-1), known.getMFCC(j-1));
                float diag = g[i - 1][j - 1] + w1 * this.distance.distance(unknown.getMFCC(i-1), known.getMFCC(j-1));
                float left = g[i][j - 1] + w2 * this.distance.distance(unknown.getMFCC(i-1), known.getMFCC(j-1));

                g[i][j] = Math.min(Math.min(top, diag), left);

                if (g[i][j] == diag) {
                    D[i][j] = true;
                } else {
                    if (g[i][j] == top) {
                        T[i][j] = true;
                    } else {
                        D[i][j] = true;
                    }
                }
            }
        }

        return g[I][J]/(I + J);
    }

}

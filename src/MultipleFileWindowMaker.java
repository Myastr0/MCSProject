import java.util.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;

import fr.enseeiht.danck.voice_analyzer.*;

public final class MultipleFileWindowMaker implements WindowMaker {

	private final List<Float> data;
    private final Random rand;
    private int cursor;

    MultipleFileWindowMaker(Collection<String> paths) throws IOException {
        this.data = new ArrayList<>();
        this.cursor = 0;
        this.rand = new Random();

        for (String path : paths) {
            File file = new File(System.getProperty("user.dir") + path);
            for (String line : Files.readAllLines(file.toPath(), Charset.defaultCharset())) {
                this.data.add(Float.parseFloat(line));
            }
            for (int i = 0; i < 4000; i++) {
                this.data.add(0.001f * (this.rand.nextFloat() * 2 - 1));
            }
        }
    }
    
	@Override
	public int getSampleRateInHz() {
		return 16000;
	}

	@Override
	public float[] nextWindow() throws InterruptedException {
		float[] window = new float[WINDOW_SIZE];

        for (int i = 0; i < window.length; i++) {
            if (this.cursor + i < this.data.size()) {
                window[i] = this.data.get(this.cursor + i);
            } else {
                window[i] = 0.001f * (this.rand.nextFloat() * 2 - 1);
            }
        }
        this.cursor += window.length / 2;
        // System.out.print(2*cursor/window.length + "-");

        return window;
	}

}
